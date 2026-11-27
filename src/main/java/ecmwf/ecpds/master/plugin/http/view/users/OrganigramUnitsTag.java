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

package ecmwf.ecpds.master.plugin.http.view.users;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.ECMWFException;
import ecmwf.ecpds.master.plugin.http.home.users.org.OrgUnitHome;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPerson;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPersonException;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * <p>
 * Prints the given levels of the organigram, starting from a given point. Used for the <search:organigram /> tags.
 * </p>
 * <p>
 * While this may work, it is quite <em>crappy</em> design indeed. It should be refactored as:
 * <ul>
 * <li>An <code>Organigram</code> TAG</li>
 * <li>Several strategies to create a <code>Layout</code> out of a data structure.</li>
 * <li>Several classes which <code>Transform</code> a Layout into a viewable representation (HTML, GIF, PDF, etc)</li>
 * </ul>
 * </p>
 *
 * @author Daniel Varela
 *
 * @version 1.0
 */

public class OrganigramUnitsTag extends TagSupport {

    private static Log log = LogFactory.getLog(OrganigramUnitsTag.class);
    private int levels = 3;
    private String defaultStartIdParameterName = "dn.top";
    private String currentPage = "";
    private String lineColor = "#cccccc";
    private String background;
    private String backBackground = "#fcfcfc";

    private String startUnitId = null; // Start the orgo in the unit with this id
    private String startUnitKey = null; // Start the orgo in the unit coming in a request attribute with this name
    private String config = null;

    private int managerBoxWidth = 130;
    private int managerBoxHeight = 120;

    private int normalBoxWidth = 130;
    private int normalBoxHeight = 120;

    private String imageDirectory = null;
    private String imageWidth = null;
    private String imageHeight = null;
    private boolean showLeavesInOneColumn = false;
    private boolean showOrgUnitsOnly = false;
    private boolean showArrows = true;

    private OrganigramStrategy strategy = null;

    protected static final String BACKGROUND = "organigram.background";
    private static Hashtable HEADS_COLORS = new Hashtable();

    private static Collection UPPER_CASE = null;
    private static Collection LOWER_CASE = null;

    private String dirBasepath = "";
    private String orgoNavigationFunction = null;
    private String orgoDetailFunction = null;

    // --------------------------------------------------------------

    static {

        try {
            Collection setList = ConfigService.getCollectionParameter("directory", "set.list");
            Iterator i = setList.iterator();
            while (i.hasNext()) {
                String set = (String) i.next();
                HEADS_COLORS.put(set, ConfigService.getParameter("directory", set + ".color"));
            }

            UPPER_CASE = ConfigService.getCollectionParameter("directory", "title.upperCaseWords");
            LOWER_CASE = ConfigService.getCollectionParameter("directory", "title.lowerCaseWords");

        } catch (ConfigException e) {
            log.error("Impossible to find init parameter: ", e);
        }

    }

    public OrganigramUnitsTag() {

        this.strategy = new OrganigramStrategy();
        try {
            this.background = ConfigService.getParameter("directory", "DEFAULT.color");
        } catch (ConfigException e) {
            this.background = "#ffffff";
        }

    }

    // Property setters ************************************************************************

    public void setLevels(String levels) {
        this.levels = Integer.parseInt(levels);
    }

    public void setStartUnitId(String id) {
        this.startUnitId = id;
    }

    public void setStartUnitKey(String key) {
        this.startUnitKey = key;
    }

    public void setDefaultStartIdParameterName(String defaultStartIdParameterName) {
        this.defaultStartIdParameterName = defaultStartIdParameterName;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public void setManagerBoxWidth(String value) {
        try {
            this.managerBoxWidth = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'managerBoxWidth' property: " + e.getMessage());
        }
    }

    public void setManagerBoxHeight(String value) {
        try {
            this.managerBoxHeight = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'managerBoxHeight' property: " + e.getMessage());
        }
    }

    public void setNormalBoxWidth(String value) {
        try {
            this.normalBoxWidth = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'normalBoxWidth' property: " + e.getMessage());
        }
    }

    public void setNormalBoxHeight(String value) {
        try {
            this.normalBoxHeight = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'normalBoxHeight' property: " + e.getMessage());
        }
    }

    public void setImageDirectory(String value) {
        this.imageDirectory = value;
    }

    public void setImageWidth(String value) {
        this.imageWidth = value;
    }

    public void setImageHeight(String value) {
        this.imageHeight = value;
    }

    public void setShowLeavesInOneColumn(String value) {
        this.showLeavesInOneColumn = "true".equals(value);
    }

    public void setShowOrgUnitsOnly(String value) {
        this.showOrgUnitsOnly = "true".equals(value);
    }

    public void setShowArrows(String value) {
        this.showArrows = "true".equals(value);
    }

    public void setOrgoNavigationFunction(String f) {
        this.orgoNavigationFunction = f;
    }

    public void setOrgoDetailFunction(String f) {
        this.orgoDetailFunction = f;
    }

    // *** Public Methods
    // ***************************************************************************************************

    /**
     * Process the start tag.
     *
     * @exception JspException
     *                if a JSP exception has occurred
     */

    public int doStartTag() throws JspException {

        this.dirBasepath = TagUtils.getResource(pageContext, "directory.basepath", "");

        try {

            try {

                Object config = pageContext.getRequest().getAttribute("config");
                if (config == null)
                    config = pageContext.getRequest().getParameter("config");
                if (config == null)
                    config = this.config;
                if (config == null)
                    config = ConfigService.getParameter("directory", "default.config");

                OrgUnit topUnit = null;
                if (this.startUnitKey != null) {
                    topUnit = (OrgUnit) TagUtils.resolveQualifiedName(pageContext, this.startUnitKey);
                } else {
                    String topUnitUid = null;
                    if (this.startUnitId == null || this.startUnitId.equals("null") || this.startUnitId.equals(""))
                        topUnitUid = ConfigService.getParameter("directory", config + ".top");
                    else
                        topUnitUid = this.startUnitId;
                    topUnit = OrgUnitHome.findByPrimaryKey(topUnitUid);
                }

                if (topUnit.getLevel() == OrgUnit.SECTION)
                    levels++;

                this.print(pageContext.getOut(), pageContext, topUnit, topUnit, 1);

            } catch (ModelException e) {
                log.error("Error getting organigram data: ", e);
                pageContext.getOut().println("<!-- Error getting organigram data: " + e.getMessage() + " -->");
            }

        } catch (IOException e) {
            throw new JspException("Problem printing info: " + e.getMessage());
        } catch (ConfigException e) {
            throw new JspException("Problem getting parameter: " + e.getMessage());
        }

        // Continue processing this page
        return (SKIP_BODY);

    }

    // Private Methods ******************************************************

    /**
     * Prints the current person AND its subordinates.
     *
     * @param out
     *            The Writer where to print
     * @param u
     *            The OrgPerson to print
     * @param topBean
     *            The top of the hierarchy (the BOSS of this branch of the org).
     * @param level
     *            The depth of level we are in the whole tree.
     */

    public void print(Writer out, PageContext context, OrgUnit u, OrgUnit topUnit, int level)
            throws IOException, ModelException, ConfigException {

        if (log.isDebugEnabled())
            log.debug("Printing OrgUnit '" + u.getId() + "' with level '" + u.getLevel() + "'");

        Collection subUnits = u.getChildren(); // Another departments
        Collection members = null;
        if (!this.showOrgUnitsOnly)
            members = u.getMembers(); // People directly under this department.
        else
            members = new ArrayList(0);

        // OrgPerson manager = u.getManager();
        String parentId = u.getParent().getId();
        Vector wantMyOwnBoxMembers = new Vector();
        Vector normalMembers = new Vector();
        Iterator i = members.iterator();
        while (i.hasNext()) {
            OrgPerson p = (OrgPerson) i.next();
            if (p.getLevel() < OrgPerson.I_WANT_MY_OWN_BOX)
                normalMembers.add(p);
            else
                wantMyOwnBoxMembers.add(p);
        }

        Collection associatedMembers = u.getAssociatedMembers();
        normalMembers.addAll(associatedMembers);

        int columns = subUnits.size() + wantMyOwnBoxMembers.size() + (normalMembers.size() > 0 ? 1 : 0);

        // String parent = u.getParentId();

        boolean absoluteTop = u.getId().toUpperCase()
                .equals(ConfigService.getParameter("directory", this.defaultStartIdParameterName).toUpperCase()); // REALLY
                                                                                                                  // crappy
                                                                                                                  // way
                                                                                                                  // of
                                                                                                                  // seeing
                                                                                                                  // if
                                                                                                                  // not
                                                                                                                  // absolute
                                                                                                                  // top.
        boolean relativeTop = u.getId().toUpperCase().equals(topUnit.getId().toUpperCase());

        // Location zone.
        out.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        // out.write("<table border=\"0\" >");

        // Top [arrow|line|nothing]?
        if (!absoluteTop && relativeTop && this.showArrows) {
            out.write("      <tr><td align=\"center\" colspan=\"" + columns + "\"><a href=\"" + getOrgoLink(parentId)
                    + "\"><img style=\"display:block; margin:auto;\" src=\""
                    + TagUtils.getResource(context, "directory.images.arrow_up", "")
                    + "\" border=\"0\" alt=\"Arrow pointing upwards\" title=\""
                    + ((OrgUnit) u.getParent()).getAttribute("ou") + "\"></a></td></tr>");
        } else if (!absoluteTop && (!relativeTop || this.showArrows)) {
            out.write("<tr><td colspan=\"" + columns + "\"><img style=\"display:block; margin:auto;\" src=\""
                    + TagUtils.getResource(context, "directory.images.vertical", "") + "\"></td></tr>");
        }
        out.write("<tr><td  align=\"center\" valign=\"top\" colspan=\"" + columns + "\">");

        // *************************************************************
        // One moooore table, printed if "siblings" links are needed. (Netscape 4.x will suffer... oh yeah!)
        if (relativeTop && this.showArrows) {
            out.write("      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.write("      <tr>");
            out.write("         <td>");
            try {
                OrgUnit prior = u.getPriorTreeWideSibling();
                out.write("      <a href=\"" + getOrgoLink(prior.getId()) + "\"><img src=\""
                        + TagUtils.getResource(context, "directory.images.arrow_left", "")
                        + "\" border=\"0\" alt=\"Arrow pointing left\" title=\"" + prior.getAttribute("ou")
                        + "\"></a>");
            } catch (OrgPersonException e) {
                // log.debug("No PriorTreeWideSibling",e);
                out.write("      <img src=\"" + TagUtils.getResource(context, "directory.images.arrow_transparent", "")
                        + "\" border=\"0\" alt=\"Nothing\" title=\"Nothing\">");
            }
            out.write("         </td>");
            out.write("         <td>");
        }
        // *******

        try {
            OrgPerson manager = u.getManager();
            this.printBox(out, context, u.getManager(), u, true, false, true);
        } catch (ModelException e) {
            this.printTroubleBox(out, e);
        }

        // *******
        if (relativeTop && this.showArrows) {
            out.write("         </td>");
            out.write("         <td>");
            try {
                OrgUnit next = u.getNextTreeWideSibling();
                out.write("      <a href=\"" + getOrgoLink(next.getId()) + "\"><img src=\""
                        + TagUtils.getResource(context, "directory.images.arrow_right", "")
                        + "\" border=\"0\" alt=\"Arrow pointing right\" title=\"" + next.getAttribute("ou")
                        + "\"></a>");
            } catch (OrgPersonException e) {
                out.write("      <img src=\"" + TagUtils.getResource(context, "directory.images.arrow_transparent", "")
                        + "\" border=\"0\" alt=\"Nothing\" title=\"Nothing\">");
            }
            out.write("         </td>");
            out.write("      </tr>");
            out.write("      </table>");
        }

        // **************************************************************

        out.write("</td>");

        if (!relativeTop) // Separator
            out.write("         <td>&nbsp;</td>");

        out.write("</tr>");

        if ((++level) <= levels && columns > 0) {

            out.write("<tr>");
            out.write("<td style=\"border-bottom: 1px solid " + this.lineColor + ";\" colspan=\"" + columns
                    + "\"><img style=\"display:block;margin: auto;\" src=\""
                    + TagUtils.getResource(context, "directory.images.vertical", "") + "\"></td>");
            out.write("</tr>");

            // Location for children
            out.write("<tr>");

            Vector orderedSubUnits = new Vector(subUnits);
            // The subordinates will be sorted by its own number of subordinates and, after that, by name.
            Collections.sort(orderedSubUnits, strategy);

            i = orderedSubUnits.iterator();
            while (i.hasNext()) {
                OrgUnit ou = (OrgUnit) i.next();
                out.write("       <td  align=\"center\" valign=\"top\">");
                this.print(out, context, ou, topUnit, level);
                out.write("       </td>");

            }

            i = wantMyOwnBoxMembers.iterator();
            while (i.hasNext()) {
                // ******************************************

                out.write("<td  align=\"center\" valign=\"top\">");
                out.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
                out.write("<tr><td colspan=\"2\"><img style=\"display:block; margin:auto;\" src=\""
                        + TagUtils.getResource(context, "directory.images.vertical", "") + "\"></td></tr>");
                out.write("<tr>");
                out.write("       <td  align=\"center\" valign=\"top\">");
                this.printBox(out, context, (OrgPerson) i.next(), topUnit, true, false, false);
                out.write("       </td>");
                out.write("         <td>&nbsp;</td>");
                out.write("</tr>");
                out.write("</table>");
                out.write("</td>");

                // ********************************************

            }
            Collections.sort(normalMembers, strategy);
            i = normalMembers.iterator();
            if (i.hasNext()) {
                if (this.showLeavesInOneColumn)
                    this.printClusteredBoxes(out, context, i, u, associatedMembers, 1);
                else
                    this.printClusteredBoxes(out, context, i, u, associatedMembers,
                            OrganigramStrategy.getPerfectlyHardcodedMatrixColumns(normalMembers.size()));
            }

            out.write("   </tr>");

        } else if (level >= levels && columns > 0 && this.showArrows) {
            // Paint the arrow down
            out.write("      <tr><td align=\"center\"><a href=\"" + getOrgoLink(u.getId()) + "\"><img src=\""
                    + TagUtils.getResource(context, "directory.images.arrow_down", "")
                    + "\" border=\"0\" alt=\"Arrow pointing downwards\" title=\"Subtree\"></a></td></tr>");
        }

        // End of Location
        out.write("</table>");

    }

    /**
     * Prints a cluster of boxes, of variable number of columns.
     *
     * @param out
     *            Write here
     * @param i
     *            The iterator which iterates through the paintable boxes
     * @param columns
     *            The number of columns of the cluster.
     */

    private void printClusteredBoxes(Writer out, PageContext context, Iterator i, OrgUnit unit,
            Collection associatedMembers, int columns) throws IOException, ModelException {

        // log.debug( "Printing clustered boxes with " + columns + " columns.");

        out.write("       <td align=\"center\" valign=\"top\">");
        out.write("       <img style=\"display:block; margin:auto;\" src=\""
                + TagUtils.getResource(context, "directory.images.vertical", "") + "\">");
        out.write(
                "       <table bgcolor=\"" + this.lineColor + "\" border=\"0\" cellspacing=\"1\" cellpadding=\"0\" >");

        int column = 1;
        int rows = 0;
        int painted = 0;
        while (i.hasNext()) {

            if (column == 1) {
                out.write("       <tr>");
                rows++;
            }

            out.write("       <td  align=\"center\" valign=\"top\" width=\"" + normalBoxWidth + "\" height=\""
                    + this.normalBoxHeight + "\">");
            OrgPerson p = (OrgPerson) i.next();
            this.printBox(out, context, p, unit, false, associatedMembers.contains(p), false);
            out.write("       </td>");

            if (column == columns) {
                out.write("       </tr>");
                column = 1;
            } else {
                column++;
            }
            painted++;

        }

        int boxesToGo = (columns * rows) - painted;

        // log.debug( "Boxes to go: (" + columns + "*" + rows + ")-" + painted + " = " + boxesToGo);
        if (boxesToGo > 0) {
            out.write("       <td  bgcolor=\"" + this.backBackground + "\" align=\"center\" valign=\"top\" height=\""
                    + this.normalBoxHeight + "\" colspan=\"" + boxesToGo + "\">");
            out.write("       &nbsp;");
            out.write("       </td>");
            out.write("       </tr>");
        }

        out.write("       </table></td>");

    }

    /**
     * Draw a box representing a person in the directory.
     *
     * @param out
     *            The output stream where to write
     * @param u
     *            The person to write.
     * @param unit
     *            The unit to which the person belongs
     * @param independent
     *            Is this box a independent one (not part of a cluster)
     * @param associated
     *            Is this person associated (not full member) of the unit
     * @param boss
     *            Is this person an unit manager?
     *
     * @throws IOException
     * @throws ModelException
     */

    private void printBox(Writer out, PageContext context, OrgPerson u, OrgUnit unit, boolean independent,
            boolean associated, boolean boss) throws IOException, ModelException {

        String title = this.getTitle(u, unit, associated);
        String name = u.getAttribute(OrgPerson.NAME, "unknown");
        String unitIdForDetail = unit.getId();

        // Draw Zone.
        if (independent) {
            out.write("\n<table bgcolor=\"" + this.lineColor
                    + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" width=\"" + managerBoxWidth + "\" height=\""
                    + managerBoxHeight + "\">");
            out.write("    <tr><td width=\"100%\" height=\""
                    + ((independent) ? this.managerBoxHeight : this.normalBoxHeight) + "\" >");
        }

        out.write("      <table bgcolor=\"" + this.lookupBackgroundColor(boss)
                + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" width=\"100%\" height=\""
                + ((independent) ? this.managerBoxHeight : this.normalBoxHeight) + "\">");

        out.write("      <tr><td align" + "=\"center\" colspan=\"2\"><img src=\""
                + TagUtils.getResource(context, "directory.images.spacer", "") + "\" width=\""
                + ((independent) ? this.managerBoxWidth : this.normalBoxWidth) + "\" height=\"1\"></td></tr>");
        out.write("      <tr><td align=\"center\" colspan=\"2\">" + title + "</td></tr>");

        out.write("      <tr><td align=\"center\" ><a href=\"" + getDetailLink(u.getId(), unitIdForDetail) + "\">"
                + this.getImage(context, u) + "</a></td>");
        out.write("          <td align=\"center\"><a href=\"" + getDetailLink(u.getId(), unitIdForDetail) + "\">" + name
                + "</a></td></tr>");

        // If this person is just an associated member.
        if (associated)
            out.write("          <tr><td colspan=\"2\" align=\"center\">* From <i>" + u.getAttribute("ou", "-")
                    + "</i></td></tr>");

        out.write("      </table>");

        if (independent) {
            out.write("   </td>");
            out.write("   </tr>");
            out.write("</table>\n");
        }
    }

    /*
     * private String lookupBackgroundColor(OrgPerson b) {
     *
     * String level = ""; switch (b.getLevel()) { case OrgPerson.DIRECTOR : level = "DIRECTOR"; break; case
     * OrgPerson.HEAD_DEPARTMENT : level = "HEAD_DEPARTMENT"; break; case OrgPerson.HEAD_DIVISION : level =
     * "HEAD_DIVISION"; break; case OrgPerson.HEAD_SECTION : level = "HEAD_SECTION"; break; case
     * OrgPerson.HEAD_SUBSECTION : level = "HEAD_SUBSECTION"; break; case OrgPerson.I_WANT_MY_OWN_BOX: level =
     * "HEAD_SUBSECTION"; break; default : return this.background;
     *
     * }
     *
     * String color = HEADS_COLORS.get(level).toString(); return (color != null) ? color : this.background;
     *
     * }
     */

    private String lookupBackgroundColor(boolean b) {
        return b ? HEADS_COLORS.get("HEAD_SECTION").toString() : this.background;
    }

    private String encode(String what) {
        return URLEncoder.encode(what);
    }

    private String getTitle(OrgPerson u, OrgUnit unit, boolean associated) throws ModelException {

        String title = u.getAttribute(OrgPerson.TITLE, u.getAttribute("employeeType", ""));
        String function = u.getAttribute(OrgPerson.FUNCTION, u.getAttribute("employeeType", ""));
        // String ou = unit.getAttribute("ou","")+" ("+unit.getLevel()+")";
        String ou = unit.getAttribute("ou", "");

        if (!associated && function.toUpperCase().startsWith("HEAD OF PROJECT TEAM"))
            return "Head of " + ou + " Project Team";
        else if (!associated && function.toUpperCase().startsWith("HEAD OF"))
            return "Head of " + ou;
        else if (!associated && function.toUpperCase().startsWith("GROUP LEADER"))
            return ou + " <br/> Group Leader";
        else if (!associated && ou.toUpperCase().startsWith("DIRECTOR"))
            return cleanString(title);
        else if (!associated && unit.getManager().getId().equals(u.getId()))
            return ou;
        else if (!"NONE".equals(u.getAttribute("uid", "NONE")))
            return cleanString(title);
        else
            return ou;

    }

    private static String cleanString(String before) {

        // Capitalize the first letters of every word, lowercasing the rest
        StringBuffer out = new StringBuffer(before);
        int i = 0, lastBoundary = 0;
        char current = ' ';
        while (i < out.length()) {
            current = out.charAt(i);
            if (current == ' ' || current == '(' || current == '/' || current == '-' || current == '.') {
                // new word boundary, process from last word boundary up to here
                if (lastBoundary < i)
                    out.replace(lastBoundary, i, capitalizeWord(out.substring(lastBoundary, i)));
                lastBoundary = i + 1;
            }

            i++;

        }

        if (lastBoundary < i)
            out.replace(lastBoundary, i, capitalizeWord(out.substring(lastBoundary, i)));

        return out.toString();

    }

    private static String capitalizeWord(String original) {
        if (UPPER_CASE.contains(original))
            return original.toUpperCase();
        else if (LOWER_CASE.contains(original.toLowerCase()))
            return original.toLowerCase();
        else {
            return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
        }
    }

    private static void printTroubleBox(Writer out, ECMWFException e) throws IOException {
        out.write("\n<table><tr><th>Problem accessing LDAP data</th></tr>");
        out.write("<tr><td>Please notify the Calldesk</td></tr></table>");
        out.write("\n<!-- EXCEPTION LIST:  " + e.getMessages() + "-->\n");
    }

    private String getImage(PageContext context, OrgPerson u) {

        String photo = u.getAttribute("photo", "");
        String size = "";
        String dirKey = "directory.images.small.basedir";

        if (this.imageHeight != null)
            size += " height=\"" + this.imageHeight + "\" ";
        if (this.imageWidth != null)
            size += " width=\"" + this.imageWidth + "\" ";

        if (this.imageDirectory != null) {
            if ("micro".equals(this.imageDirectory)) {
                dirKey = "directory.images.micro.basedir";
            } else if ("normal".equals(this.imageDirectory)) {
                dirKey = "directory.images.basedir";
            } else {
                dirKey = "directory.images.small.basedir";
            }
        }

        return "<img src=\"" + TagUtils.getResource(context, dirKey, "") + "" + photo + "\" border=\"0\" alt=\"Photo\" "
                + size + ">";

    }

    private String getOrgoLink(String id) {
        if (orgoNavigationFunction == null)
            return currentPage + "?startId=" + encode(id);
        else
            return "javascript:" + orgoNavigationFunction + "('" + encode(id) + "')";
    }

    private String getDetailLink(String id, String unitId) {
        if (orgoDetailFunction == null)
            return dirBasepath + "/show?id=" + encode(id) + "&unit=" + encode(unitId);
        else
            return "javascript:" + orgoDetailFunction + "('" + encode(id) + "','" + encode(unitId) + "')";
    }

    /*
     *
     * private OrgPerson findInListOfPersonsAndRemove(Collection c, String uid) {
     *
     * OrgPerson candidate = null; Iterator i = c.iterator(); while (i.hasNext()) { candidate = (OrgPerson)i.next(); if
     * (candidate.getAttribute("uid","-").equals(uid)) { c.remove(candidate); return candidate; } } return null; }
     */

}
