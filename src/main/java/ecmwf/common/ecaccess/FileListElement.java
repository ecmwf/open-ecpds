/*
 * ECaccess Project - FileListElement.java
 *
 * Class: ecmwf.ecbatch.eis.rmi.client.FileListElement
 * Using JDK: 1.8.0_60
 *
 * Copyright (c) 2000-2016 Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 */
package ecmwf.common.ecaccess;

import java.io.Serializable;

/**
 * The Class FileListElement.
 *
 * @author <a href="mailto:syi@ecmwf.int">Laurent Gougeon</a>
 *
 * @version 4.2.0
 */

public final class FileListElement implements Serializable, Cloneable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6826982990410695017L;

    /** The right. */
    private String right = null;

    /** The comment. */
    private String comment = null;

    /** The user. */
    private String user = null;

    /** The group. */
    private String group = null;

    /** The size. */
    private String size = null;

    /** The time. */
    private long time = -1;

    /** The name. */
    private String name = null;

    /** The path. */
    private String path = null;

    /** The link. */
    private String link = null;

    /** The type. */
    private Character type = null;

    /** The dir. */
    private Boolean dir = null;

    /**
     * Gets the group.
     *
     * @return the group
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    public String getLink() {
        return this.link;
    }

    /**
     * Gets the right.
     *
     * @return the right
     */
    public String getRight() {
        return this.right;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public String getSize() {
        return this.size;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public char getType() {
        return this.type;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Sets the comment.
     *
     * @param com
     *            the new comment
     */
    public void setComment(final String com) {
        this.comment = com;
    }

    /**
     * Sets the group.
     *
     * @param group
     *            the new group
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the path.
     *
     * @param path
     *            the new path
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Sets the link.
     *
     * @param link
     *            the new link
     */
    public void setLink(final String link) {
        this.link = link;
    }

    /**
     * Sets the right.
     *
     * @param right
     *            the new right
     */
    public void setRight(final String right) {
        this.right = right;
        this.type = right.charAt(0);
        this.dir = right.charAt(0) == 'd';
    }

    /**
     * Sets the size.
     *
     * @param size
     *            the new size
     */
    public void setSize(final String size) {
        this.size = size;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(final String user) {
        this.user = user;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Checks if is directory.
     *
     * @return true, if is directory
     */
    public boolean isDirectory() {
        return this.dir != null && this.dir.booleanValue();
    }

    /**
     * Sets the time.
     *
     * @param time
     *            the new time
     */
    public void setTime(final long time) {
        this.time = time;
    }

    /**
     * Return the filename, path included.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return (path != null ? path + "/" : "") + name;
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable.
            throw new InternalError();
        }
    }
}