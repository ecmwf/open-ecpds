/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class ActiveDirectory.
 */
class ActiveDirectory {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ActiveDirectory.class);

    /** The Constant LDAP_USER. */
    private static final String LDAP_USER = Cnf.at("MonitorPlugin", "ldapUser", "");

    /** The Constant LDAP_PASSWORD. */
    private static final String LDAP_PASSWORD = Cnf.at("MonitorPlugin", "ldapPassword", "");

    /** The Constant LDAP_HOST. */
    private static final String LDAP_HOST = Cnf.at("MonitorPlugin", "ldapHost", "localhost");

    /** The Constant LDAP_PORT. */
    private static final int LDAP_PORT = Cnf.at("MonitorPlugin", "ldapPort", 389);

    /** The Constant LDAP_DOMAIN. */
    private static final String LDAP_DOMAIN = Cnf.at("MonitorPlugin", "ldapDomain", "my.domain");

    /** The Constant EXTRA_EMAILS. */
    private static final String[] EXTRA_EMAILS = Cnf.stringListAt("MonitorPlugin", "extraEmails", "delay@my.domain");

    /** The Constant EXTENSION. */
    private static final String EXTENSION = Cnf.at("MonitorPlugin", "extension", "");

    /**
     * Instantiates a new active directory.
     */
    private ActiveDirectory() {
    }

    /**
     * Synchronize.
     *
     * @param contactsFileName
     *            the contacts file name
     *
     * @throws NamingException
     *             the naming exception
     */
    public static void synchronize(final String contactsFileName) throws NamingException {
        if (LDAP_USER.length() > 0) { // Only process if credentials are defined!
            synchronize(LDAP_USER, LDAP_PASSWORD, LDAP_DOMAIN, LDAP_HOST, LDAP_PORT, contactsFileName, true, true,
                    EXTENSION);
        }
    }

    /**
     * Synchronize.
     *
     * @param username
     *            the username
     * @param password
     *            the password
     * @param domainName
     *            the domain name
     * @param serverName
     *            the server name
     * @param port
     *            the port
     * @param contactsFileName
     *            the contacts file name
     * @param removeUnusedContacts
     *            the remove unused contacts
     * @param removeUnusedGroups
     *            the remove unused groups
     * @param extension
     *            the extension
     *
     * @throws NamingException
     *             the naming exception
     */
    public static void synchronize(final String username, final String password, final String domainName,
            final String serverName, final int port, final String contactsFileName, final boolean removeUnusedContacts,
            final boolean removeUnusedGroups, final String extension) throws NamingException {
        // Connecting to LDAP
        final var ctx = getConnection(username, password, domainName, serverName, port);
        // Getting all emails per product from configuration file!
        final var emailsPerProduct = getEmailListPerProduct(contactsFileName, extension);
        // Create missing contacts in LDAP!
        final Set<String> allMails = new LinkedHashSet<>();
        for (final Set<String> mails : emailsPerProduct.values()) {
            allMails.addAll(mails);
        }
        final var contactsFromLDAP = Contact.getAll(ctx);
        for (final String mail : allMails) {
            var exists = false;
            for (final Contact contact : contactsFromLDAP) {
                if (contact.mail.equalsIgnoreCase(mail)) {
                    log.debug("Mail {} already in LDAP", contact.mail);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                log.info("Creating Contact: {}", mail);
                try {
                    new Contact(mail).create(ctx);
                } catch (final NamingException e) {
                    log.warn("Cannot create Contact: {}", mail, e);
                }
            }
        }
        // Create missing groups or update modified groups in LDAP!
        final var groupsFromLDAP = Group.getAll(ctx);
        for (final String productName : emailsPerProduct.keySet()) {
            Group groupFound = null;
            for (final Group group : groupsFromLDAP) {
                if (("ECPDS " + productName).equalsIgnoreCase(group.cn)) {
                    log.debug("Group {} already in LDAP", group.cn);
                    groupFound = group;
                    break;
                }
            }
            if (groupFound == null) {
                // The group does not exists in LDAP
                log.info("Creating Group: ECPDS {}", productName);
                try {
                    new Group(productName, emailsPerProduct.get(productName)).create(ctx);
                } catch (final NamingException e) {
                    log.warn("Cannot create Group: ECPDS {}", productName, e);
                }
            } else {
                // The group is already in LDAP
                final List<String> fromLDAP = new ArrayList<>(groupFound.member);
                final var emails = toContactCNsList(emailsPerProduct.get(productName));
                var updated = false;
                for (final String mail : fromLDAP) {
                    if (!emails.contains(mail)) {
                        final var removed = groupFound.member.remove(mail);
                        if (removed) {
                            log.debug("Email {} to remove", mail);
                        }
                        updated = updated || removed;
                    }
                }
                for (final String mail : emails) {
                    if (!fromLDAP.contains(mail)) {
                        log.debug("Email {} to add", mail);
                        groupFound.member.add(mail);
                        updated = true;
                    }
                }
                if (updated) {
                    if (!groupFound.member.isEmpty()) {
                        log.info("Updating Group: ECPDS {}", productName);
                        try {
                            groupFound.updateMember(ctx);
                        } catch (final NamingException e) {
                            log.warn("Cannot update Group: ECPDS {}", productName, e);
                        }
                    } else if (removeUnusedGroups) {
                        log.info("Deleting Group (no contacts found): ECPDS {}", productName);
                        try {
                            groupFound.delete(ctx);
                        } catch (final NamingException e) {
                            log.warn("Cannot delete Group: ECPDS {}", productName, e);
                        }
                    }
                }
            }
        }
        // Remove contacts not used from LDAP
        if (removeUnusedContacts) {
            for (final Contact contact : contactsFromLDAP) {
                var exists = false;
                for (final String mail : allMails) {
                    if (contact.mail.equalsIgnoreCase(mail)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    log.info("Deleting Contact: {}", contact.cn);
                    try {
                        contact.delete(ctx);
                    } catch (final NamingException e) {
                        log.warn("Cannot delete Contact: {}", contact.cn, e);
                    }
                }
            }
        }
        // Remove groups not used from LDAP
        if (removeUnusedGroups) {
            for (final Group group : groupsFromLDAP) {
                var exists = false;
                for (final String productName : emailsPerProduct.keySet()) {
                    if (("ECPDS " + productName).equalsIgnoreCase(group.cn)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists && !"ECPDS All Destinations".equals(group.cn)) {
                    log.info("Deleting Group: {}", group.cn);
                    try {
                        group.delete(ctx);
                    } catch (final NamingException e) {
                        log.warn("Cannot delete Group: {}", group.cn, e);
                    }
                }
            }
        }
    }

    /**
     * Gets the connection.
     *
     * @param username
     *            the username
     * @param password
     *            the password
     * @param domainName
     *            the domain name
     * @param serverName
     *            the server name
     * @param port
     *            the port
     *
     * @return the connection
     *
     * @throws NamingException
     *             the naming exception
     */
    private static LdapContext getConnection(final String username, String password, final String domainName,
            final String serverName, final int port) throws NamingException {
        if (password != null) {
            password = password.trim();
            if (password.length() == 0) {
                password = null;
            }
        }
        // bind by using the specified username/password
        final var props = new Hashtable<String, String>();
        final var principalName = username + "@" + domainName;
        props.put(Context.SECURITY_PRINCIPAL, principalName);
        if (password != null) {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }
        final var ldapURL = "ldap://" + (serverName == null ? domainName : serverName + "." + domainName) + ":" + port
                + "/";
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapURL);
        try {
            return new InitialLdapContext(props, null);
        } catch (final Exception e) {
            final var message = "Failed to connect " + principalName + " to " + ldapURL;
            log.error(message, e);
            throw new NamingException(message);
        }
    }

    /**
     * Gets the attribute.
     *
     * @param attr
     *            the attr
     * @param name
     *            the name
     *
     * @return the attribute
     *
     * @throws NamingException
     *             the naming exception
     */
    private static final String getAttribute(final Attributes attr, final String name) throws NamingException {
        final var nameAttribute = attr.get(name);
        if (nameAttribute != null) {
            return (String) nameAttribute.get();
        }
        return null;
    }

    // #REQUIRED
    // dn: CN=ECPDS Contact 1,OU=Contacts,OU=ECPDS,OU=External Exchange
    // Recipients,DC=ecmwf,DC=int
    // objectClass: contact
    // cn: ECPDS Contact 1
    // #displayName - Recipients will see this in the TO: field of received emails
    // displayName: ECPDS Contact 1
    // name: ECPDS Contact 1
    // mail: name@ext-address.com
    // msExchRequireAuthToSendTo: TRUE
    // #OPTIONAL
    // #department - if not set, the contact will appear in the Outlook Global
    // Address Book, if set to "ECPDS Contacts" this contact will
    // #only be visible for specific accounts, newops etc.
    // department: ECPDS Contacts

    /**
     * The Class Contact.
     */
    public static class Contact {

        /** The contact attributes. */
        private static String[] contactAttributes = { "objectClass", "cn", "displayName", "name", "mail",
                "msExchRequireAuthToSendTo", "department", "extensionAttribute1" };

        /** The object class. */
        public final String objectClass;

        /** The cn. */
        public final String cn;

        /** The display name. */
        public final String displayName;

        /** The name. */
        public final String name;

        /** The mail. */
        public final String mail;

        /** The ms exch require auth to send to. */
        public final boolean msExchRequireAuthToSendTo;

        /** The department. */
        public final String department;

        /** The extension attribute 1. */
        public final String extensionAttribute1;

        /**
         * Instantiates a new contact.
         *
         * @param attr
         *            the attr
         *
         * @throws NamingException
         *             the naming exception
         */
        public Contact(final Attributes attr) throws javax.naming.NamingException {
            objectClass = getAttribute(attr, "objectClass");
            cn = getAttribute(attr, "cn");
            displayName = getAttribute(attr, "displayName");
            name = getAttribute(attr, "name");
            mail = getAttribute(attr, "mail");
            msExchRequireAuthToSendTo = "TRUE".equalsIgnoreCase(getAttribute(attr, "msExchRequireAuthToSendTo"));
            department = getAttribute(attr, "department");
            extensionAttribute1 = getAttribute(attr, "extensionAttribute1");
        }

        /**
         * Instantiates a new contact.
         *
         * @param objectClass
         *            the object class
         * @param cn
         *            the cn
         * @param displayName
         *            the display name
         * @param name
         *            the name
         * @param mail
         *            the mail
         * @param msExchRequireAuthToSendTo
         *            the ms exch require auth to send to
         * @param department
         *            the department
         * @param extensionAttribute1
         *            the extension attribute 1
         */
        public Contact(final String objectClass, final String cn, final String displayName, final String name,
                final String mail, final boolean msExchRequireAuthToSendTo, final String department,
                final String extensionAttribute1) {
            this.objectClass = objectClass;
            this.cn = cn;
            this.displayName = displayName;
            this.name = name;
            this.mail = mail;
            this.msExchRequireAuthToSendTo = msExchRequireAuthToSendTo;
            this.department = department;
            this.extensionAttribute1 = extensionAttribute1;
        }

        /**
         * Instantiates a new contact.
         *
         * @param mail
         *            the mail
         */
        public Contact(final String mail) {
            this("contact", "ECPDS " + mail, "ECPDS " + mail, "ECPDS " + mail, mail, true, "ECPDS Contacts",
                    "Restricted: ECPDS Contacts");
        }

        /**
         * Gets the distinguished name.
         *
         * @return the distinguished name
         */
        public String getDistinguishedName() {
            return "CN=" + cn + ",OU=Contacts,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int";
        }

        /**
         * Gets the all.
         *
         * @param context
         *            the context
         *
         * @return the all
         *
         * @throws NamingException
         *             the naming exception
         */
        public static List<Contact> getAll(final LdapContext context) throws NamingException {
            final var contacts = new ArrayList<Contact>();
            final var controls = new SearchControls();
            controls.setSearchScope(SUBTREE_SCOPE);
            controls.setReturningAttributes(contactAttributes);
            final var answer = context.search("OU=Contacts,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int",
                    "(objectClass=contact)", controls);
            try {
                while (answer.hasMore()) {
                    final var attr = answer.next().getAttributes();
                    contacts.add(new Contact(attr));
                }
            } catch (final Exception e) {
                // Ignore!
            }
            return contacts;
        }

        /**
         * Creates the.
         *
         * @param context
         *            the context
         *
         * @throws NamingException
         *             the naming exception
         */
        public void create(final LdapContext context) throws NamingException {
            final Attributes entry = new BasicAttributes();
            entry.put(new BasicAttribute("objectClass", objectClass));
            entry.put(new BasicAttribute("cn", cn));
            entry.put(new BasicAttribute("displayName", displayName));
            entry.put(new BasicAttribute("name", name));
            entry.put(new BasicAttribute("mail", mail));
            entry.put(new BasicAttribute("msExchRequireAuthToSendTo", msExchRequireAuthToSendTo ? "TRUE" : "FALSE"));
            entry.put(new BasicAttribute("department", department));
            entry.put(new BasicAttribute("extensionAttribute1", extensionAttribute1));
            context.createSubcontext(getDistinguishedName(), entry);
        }

        /**
         * Delete.
         *
         * @param context
         *            the context
         *
         * @throws NamingException
         *             the naming exception
         */
        public void delete(final LdapContext context) throws NamingException {
            context.destroySubcontext(getDistinguishedName());
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return new StringBuilder().append(objectClass).append(";").append(cn).append(";").append(displayName)
                    .append(";").append(name).append(";").append(mail).append(";").append(msExchRequireAuthToSendTo)
                    .append(";").append(department).append(";").append(extensionAttribute1).toString();
        }
    }

    // #REQUIRED
    // dn: CN=ECPDS Destination 1,OU=Distribution Lists,OU=ECPDS,OU=External
    // Exchange Recipients,DC=ecmwf,DC=int
    // objectClass: group
    // cn: ECPDS Destination 1
    // displayName: ECPDS Destination 1
    // name: ECPDS Destination 1
    // groupType: 2
    // mail: ecpds-destination1@ecmwf.int
    // msExchRequireAuthToSendTo: TRUE
    // #OPTIONAL
    // #member - DN of contacts that are members of this distribution list
    // member: CN=ECPDS Contact 1,OU=Contacts,OU=ECPDS,OU=External Exchange
    // Recipients,DC=ecmwf,DC=int
    // member: CN=ECPDS Contact 2,OU=Contacts,OU=ECPDS,OU=External Exchange
    // Recipients,DC=ecmwf,DC=int
    // #department - if not set, the contact will appear in the Outlook Global
    // Address Book, if set to "ECPDS Contacts" this contact will
    // #only be visible for specific accounts, newops etc.
    // department: ECPDS Contacts
    // #description - Additional description visible in the Outlook Address book
    // description: ECPDS Destination 1, updated 11/01/23
    // #dLMemSubmitPerms - if set restrict who can send to this distribution list to
    // members of the named distribution list (should include
    // #newops etc.)
    // dLMemSubmitPerms: CN=ECPDS Authorised Senders,OU=Distribution
    // Lists,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int

    /**
     * The Class Group.
     */
    public static class Group {

        /** The group attributes. */
        private static String[] groupAttributes = { "dn", "objectClass", "cn", "displayName", "name", "groupType",
                "mail", "msExchRequireAuthToSendTo", "member", "department", "description", "dLMemSubmitPerms" };

        /** The object class. */
        public final String objectClass;

        /** The cn. */
        public final String cn;

        /** The display name. */
        public final String displayName;

        /** The name. */
        public final String name;

        /** The group type. */
        public final String groupType;

        /** The mail. */
        public final String mail;

        /** The ms exch require auth to send to. */
        public final boolean msExchRequireAuthToSendTo;

        /** The member. */
        public final List<String> member = new ArrayList<>();

        /** The department. */
        public final String department;

        /** The description. */
        public final String description;

        /** The d L mem submit perms. */
        public final String dLMemSubmitPerms;

        /**
         * Instantiates a new group.
         *
         * @param attr
         *            the attr
         *
         * @throws NamingException
         *             the naming exception
         */
        public Group(final Attributes attr) throws javax.naming.NamingException {
            objectClass = getAttribute(attr, "objectClass");
            cn = getAttribute(attr, "cn");
            displayName = getAttribute(attr, "displayName");
            name = getAttribute(attr, "name");
            groupType = getAttribute(attr, "groupType");
            mail = getAttribute(attr, "mail");
            msExchRequireAuthToSendTo = "TRUE".equalsIgnoreCase(getAttribute(attr, "msExchRequireAuthToSendTo"));
            final var memberAttribute = attr.get("member");
            if (memberAttribute != null) {
                final NamingEnumeration<?> enumeration = memberAttribute.getAll();
                while (enumeration.hasMore()) {
                    final var value = enumeration.next().toString();
                    if (value.length() > 0) {
                        member.add(value);
                    }
                }
            }
            department = getAttribute(attr, "department");
            description = getAttribute(attr, "description");
            dLMemSubmitPerms = getAttribute(attr, "dLMemSubmitPerms");
        }

        /**
         * Instantiates a new group.
         *
         * @param objectClass
         *            the object class
         * @param cn
         *            the cn
         * @param displayName
         *            the display name
         * @param name
         *            the name
         * @param groupType
         *            the group type
         * @param mail
         *            the mail
         * @param msExchRequireAuthToSendTo
         *            the ms exch require auth to send to
         * @param member
         *            the member
         * @param department
         *            the department
         * @param description
         *            the description
         * @param dLMemSubmitPerms
         *            the d L mem submit perms
         */
        public Group(final String objectClass, final String cn, final String displayName, final String name,
                final String groupType, final String mail, final boolean msExchRequireAuthToSendTo,
                final List<String> member, final String department, final String description,
                final String dLMemSubmitPerms) {
            this.objectClass = objectClass;
            this.cn = cn;
            this.displayName = displayName;
            this.name = name;
            this.groupType = groupType;
            this.mail = mail;
            this.msExchRequireAuthToSendTo = msExchRequireAuthToSendTo;
            if (member != null && !member.isEmpty()) {
                this.member.addAll(member);
            }
            this.department = department;
            this.description = description;
            this.dLMemSubmitPerms = dLMemSubmitPerms;
        }

        /**
         * Instantiates a new group.
         *
         * @param productName
         *            the product name
         * @param contactEmails
         *            the contact emails
         */
        public Group(final String productName, final Set<String> contactEmails) {
            this("group", "ECPDS " + productName, "ECPDS " + productName, "ECPDS " + productName, "2",
                    "ecpds-product-" + productName.toLowerCase() + "@ecmwf.int", true, toContactCNsList(contactEmails),
                    "ECPDS Contacts", "ECPDS " + productName + " updated " + Format.formatCurrentTime(),
                    "CN=ECPDS Authorised Senders,OU=Distribution Lists,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int");
        }

        /**
         * Gets the distinguished name.
         *
         * @return the distinguished name
         */
        public String getDistinguishedName() {
            return "CN=" + cn + ",OU=Distribution Lists,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int";
        }

        /**
         * Gets the all.
         *
         * @param context
         *            the context
         *
         * @return the all
         *
         * @throws NamingException
         *             the naming exception
         */
        public static List<Group> getAll(final LdapContext context) throws NamingException {
            final var groups = new ArrayList<Group>();
            final var controls = new SearchControls();
            controls.setSearchScope(SUBTREE_SCOPE);
            controls.setReturningAttributes(groupAttributes);
            final var answer = context.search(
                    "OU=Distribution Lists,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int",
                    "(&(objectClass=group)(groupType=2))", controls);
            try {
                while (answer.hasMore()) {
                    groups.add(new Group(answer.next().getAttributes()));
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return groups;
        }

        /**
         * Creates the.
         *
         * @param context
         *            the context
         *
         * @throws NamingException
         *             the naming exception
         */
        public void create(final LdapContext context) throws NamingException {
            final Attributes entry = new BasicAttributes();
            entry.put(new BasicAttribute("objectClass", objectClass));
            entry.put(new BasicAttribute("cn", cn));
            entry.put(new BasicAttribute("displayName", displayName));
            entry.put(new BasicAttribute("name", name));
            entry.put(new BasicAttribute("groupType", groupType));
            entry.put(new BasicAttribute("mail", mail));
            entry.put(new BasicAttribute("msExchRequireAuthToSendTo", msExchRequireAuthToSendTo ? "TRUE" : "FALSE"));
            if (!member.isEmpty()) {
                entry.put(getMultiValuesAttribute(member));
            }
            entry.put(new BasicAttribute("department", department));
            entry.put(new BasicAttribute("description", description));
            entry.put(new BasicAttribute("dLMemSubmitPerms", dLMemSubmitPerms));
            context.createSubcontext(getDistinguishedName(), entry);
        }

        /**
         * Update member.
         *
         * @param context
         *            the context
         *
         * @throws NamingException
         *             the naming exception
         */
        public void updateMember(final LdapContext context) throws NamingException {
            context.modifyAttributes(getDistinguishedName(), new ModificationItem[] {
                    new ModificationItem(DirContext.REPLACE_ATTRIBUTE, getMultiValuesAttribute(member)) });
        }

        /**
         * Delete.
         *
         * @param context
         *            the context
         *
         * @throws NamingException
         *             the naming exception
         */
        public void delete(final LdapContext context) throws NamingException {
            context.destroySubcontext(getDistinguishedName());
        }

        /**
         * Gets the multi values attribute.
         *
         * @param list
         *            the list
         *
         * @return the multi values attribute
         */
        private static BasicAttribute getMultiValuesAttribute(final List<String> list) {
            final var attribute = new BasicAttribute("member", list.get(0));
            for (var i = 1; i < list.size(); i++) {
                attribute.add(list.get(i));
            }
            return attribute;
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return new StringBuilder().append(objectClass).append(";").append(cn).append(";").append(displayName)
                    .append(";").append(name).append(";").append(groupType).append(";").append(mail).append(";")
                    .append(msExchRequireAuthToSendTo).append(";").append(member).append(";").append(department)
                    .append(";").append(description).append(";").append(dLMemSubmitPerms).toString();
        }
    }

    /**
     * Gets the email list per product.
     *
     * @param configFileName
     *            the config file name
     * @param extension
     *            the extension
     *
     * @return the email list per product
     */
    private static HashMap<String, Set<String>> getEmailListPerProduct(final String configFileName,
            final String extension) {
        final var result = new HashMap<String, Set<String>>();
        try {
            final var scanner = new Scanner(new File(configFileName));
            while (scanner.hasNextLine()) {
                final var line = scanner.nextLine().split(",");
                if (line.length > 2) {
                    final Set<String> list = new LinkedHashSet<>();
                    final var productName = line[0].split("@");
                    result.put(extension + productName[1] + "-" + productName[0], list);
                    list.addAll(Arrays.asList(EXTRA_EMAILS));
                    for (var i = 2; i < line.length; i++) {
                        final var email = line[i].split("=")[0];
                        if (!list.contains(email)) {
                            list.add(email);
                        }
                    }
                }
            }
            scanner.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * To contact C ns list.
     *
     * @param emails
     *            the emails
     *
     * @return the list
     */
    private static List<String> toContactCNsList(final Set<String> emails) {
        final Set<String> result = new LinkedHashSet<>(); // Remove duplicates!
        for (final String email : emails) {
            result.add("CN=ECPDS " + email.toLowerCase()
                    + ",OU=Contacts,OU=ECPDS,OU=External Exchange Recipients,DC=ecmwf,DC=int");
        }
        return new ArrayList<>(result);
    }

    /**
     * The main method. Can be used to test locally!
     *
     * @param args
     *            the arguments
     *
     * @throws NumberFormatException
     *             the number format exception
     * @throws NamingException
     *             the naming exception
     */
    public static void main(final String[] args) throws NumberFormatException, NamingException {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
        // **** -> user/password
        final var params = args.length == 7 ? args
                : new String[] { "****", "****", "ecmwf.int", "localhost", "5389", "/tmp/contacts.txt", "" };
        synchronize(params[0], params[1], params[2], params[3], Integer.parseInt(params[4]), params[5], false, false,
                params[6]);
    }
}