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

package ecmwf.ecpds.master.plugin.http.view.general;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.home.ModelHomeBase;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.util.Util;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

/**
 * Base JSP Tag which creates and executes a ModelBeanSearch, showing the results using some supplied parameters. It can
 * be used directly or subclassed to get a more specific behaviour (see Hook Methods)
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class ModelBeanSearchTag extends TagSupport {

    private static Log log = LogFactory.getLog(ModelBeanSearchTag.class);

    private static final String ITEM_PAGE_KEY = "item";
    private static final int WD_NONE = 0;
    private static final int WD_PERCENTAGE = 1;
    private static final int WD_FIXED = 2;

    // Data settings
    private String theClass = null; // Bean class we will as DAOService for.
    private String name = null; // Key where we will find the data in the
    // Request
    private String writeSearchIntoProperty = null; // Don't display, just
    // retrieve.

    private String filter = null; // Filter used to access DAOService.
    private String base = null; // Base used to for searches. See ModelSearch.
    private int scope = -1; // Scope used for searches. See ModelSearch.
    private int limit = 100; // Max number of objects to show;
    private int offset = -1; // Where to start
    private Collection fields = null; // Fields to show.
    private int fieldLengthLimit = -1; // Limit fields to this size;
    private Collection order = null; // Order used to order.
    private ArrayList styles = null; // If set, holds the list of styles to
    // apply to the list of fields.
    private boolean recursive = false; // Searches down the channels,
    // categories, etc.
    private boolean partialMatches = false; // Search within fields.
    // Writes a JavaScript variable
    // "fieldMark" with the content of the
    // attribute named as this field of the first record retrieved. Can be used
    // to see if
    // there is new data, for example.
    private String fieldMark = null;
    // If to ignore sucessive fields with the same value, after the first is
    // displayed
    private boolean ignoreSuccesiveEqualFields = false;

    // Box display
    private String title = "Search results";
    private boolean showTitle = true;
    private int width = 600;
    private int widthType = WD_NONE;
    private int height = 0;
    private String titleClass = null;
    private String innerTableClass = null; // CSS class for the inner table.
    private String outerTableClass = null; // CSS class for the outer table.

    // Fields display.
    private boolean alternateRows = false;
    private boolean showFieldNames = false;
    private ArrayList fieldNames = null;
    private String fieldsOrientation = "columns"; // We can show the fields in
    // "rows" or "columns" or
    // using a "," separator.
    private String mode = "table"; // Can be "table" or "list"
    private String thClass = null; // CSS class for the names of the fields.
    private String tdClass = null; // CSS class for the values of the fields.
    private ArrayList columnsWidth = null; // The list of widths to apply to
    // the columns.

    private boolean showLink = false; // Show every row/item linking to some
    // place.
    private String link = null; // The the property "showLink" is true, parse
    // this expression to create it.
    private String basePathKey = null; // The the property "showLink" is true,
    // insert the value of this key (in
    // ECMWFResources.properties)
    // as the base path for the link.

    private boolean even = true;

    public void setTheClass(String name) {
        this.theClass = name;
    }

    public String getTheClass() {
        return this.theClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setQuery(String query) {
        this.setFilter(query);
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setScope(String scope) {

        if ("ONELEVEL".equals(scope))
            this.scope = ModelSearch.ONELEVEL_SCOPE;
        else if ("SUBTREE".equals(scope))
            this.scope = ModelSearch.SUBTREE_SCOPE;
        else
            this.scope = ModelSearch.OBJECT_SCOPE;

    }

    public void setLimit(String value) {
        try {
            this.limit = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'limit' property.", e);
        }
    }

    public void setPartialMatches(String value) {
        this.partialMatches = value.toLowerCase().trim().equals("true");
    }

    public boolean allowPartialMatches() {
        return this.partialMatches;
    }

    public void setRecursive(String value) {
        this.recursive = value.toLowerCase().trim().equals("true");
    }

    public void setFields(String fields) {
        this.fields = Util.listStringToCollection(fields);
    }

    public void setFieldLengthLimit(String value) {
        try {
            this.fieldLengthLimit = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'fieldLengthLimit' property.", e);
        }
    }

    public void setIgnoreSuccesiveEqualFields(String value) {
        this.ignoreSuccesiveEqualFields = value.toLowerCase().trim().equals("true");
    }

    public Collection getFieldsCollection() {
        return this.fields;
    }

    public void setOrder(String order) {
        this.order = Util.listStringToCollection(order);
    }

    public void setFieldMark(String value) {
        this.fieldMark = value;
    }

    public void setStyles(String styles) {
        this.styles = new ArrayList(Util.listStringToCollection(styles));
    }

    public ArrayList getStylesArrayList() {
        return this.styles;
    }

    public void setShowTitle(String value) {
        this.showTitle = value.toLowerCase().trim().equals("true");
    }

    public void setAlternateRows(String value) {
        this.alternateRows = value.toLowerCase().trim().equals("true");
    }

    public void setShowLink(String value) {
        this.showLink = value.toLowerCase().trim().equals("true");
    }

    public void setShowFieldNames(String value) {
        this.showFieldNames = value.toLowerCase().trim().equals("true");
    }

    public void setFieldNames(String value) {
        this.fieldNames = Util.listStringToArrayList(value);
    }

    public void setLink(String value) {
        this.link = value;
    }

    public void setBasePathKey(String value) {
        this.basePathKey = value;
    }

    public void setOuterTableClass(String outerTableClass) {
        this.outerTableClass = outerTableClass;
    }

    public void setInnerTableClass(String innerTableClass) {
        this.innerTableClass = innerTableClass;
    }

    public String getInnerTableClass() {
        return this.innerTableClass;
    }

    public void setTdClass(String tdClass) {
        this.tdClass = tdClass;
    }

    public void setThClass(String thClass) {
        this.thClass = thClass;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleClass(String titleClass) {
        this.titleClass = titleClass;
    }

    public void setWidth(String width) {
        try {
            if (width.endsWith("%")) {
                this.width = Integer.parseInt(width.substring(0, width.length() - 1));
                this.widthType = WD_PERCENTAGE;
            } else {
                this.width = Integer.parseInt(width);
                this.widthType = WD_FIXED;
            }
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'width' property.", e);
        } catch (Exception e) {
            log.error("Exception setting 'width' property.", e);
        }
    }

    public void setHeight(String h) {

        try {
            this.height = Integer.parseInt(h);
            this.widthType = WD_FIXED;
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'height' property.", e);
        } catch (Exception e) {
            log.error("Exception setting 'height' property.", e);
        }
    }

    public void setOffset(String offset) {

        try {
            this.offset = Integer.parseInt(offset);
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'offset' property.", e);
        } catch (Exception e) {
            log.error("Exception setting 'offset' property.", e);
        }
    }

    public String getWriteSearchIntoProperty() {
        return writeSearchIntoProperty;
    }

    public void setWriteSearchIntoProperty(String writeSearchIntoProperty) {
        this.writeSearchIntoProperty = writeSearchIntoProperty;
    }

    public void setFieldsOrientation(String orientation) {
        this.fieldsOrientation = orientation;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setColumnsWidth(String widths) {

        try {
            this.columnsWidth = new ArrayList();
            StringTokenizer st = new StringTokenizer(widths, ",");
            while (st.hasMoreTokens()) {
                String width = st.nextToken().trim();
                if (width.endsWith("%")) {
                    width = width.substring(0, width.length() - 1);
                    this.widthType = WD_PERCENTAGE;
                } else {
                    this.widthType = WD_FIXED;
                }
                this.columnsWidth.add(new Integer(width));
            }
        } catch (NumberFormatException e) {
            log.warn("Bad integer in 'columnsWidth' property.", e);
        } catch (Exception e) {
            log.error("Exception setting 'columnsWidth' property.", e);
        }

    }

    public ArrayList getColumnsWidthArrayList() {
        return this.columnsWidth;
    }

    /**
     * Actually get the data applying "theClass", "filter","limit" and "order" attributes.
     *
     * @return A collection of ModelBeans matching the filter.
     *
     * @throws ModelException
     */

    public Collection getResults() throws ModelException {

        if (this.filter == null)
            throw new ModelException("Either attribute 'name' or 'filter' is needed for ModelSearchBeanTag");

        try {
            ModelSearch search = ModelHomeBase.getDefaultSearch();
            search.setBase(this.base);
            search.setQuery(TagUtils.parseMultiExpressionText(pageContext, this.filter));
            search.setSizeLimit(this.limit);
            search.setSortItemsFromStrings(this.order);
            search.setPartialMatches(this.partialMatches);
            if (this.offset > 0)
                search.setOffset(this.offset);

            if (this.scope > 0)
                search.setScope(this.scope);
            else
                search.setScope(this.recursive ? ModelSearch.SUBTREE_SCOPE : ModelSearch.ONELEVEL_SCOPE);

            if (log.isDebugEnabled())
                log.debug("Search to be used: " + search.getKey());
            return DAOService.find(this.theClass, search);
        } catch (DAOException e) {
            throw new ModelException("Problem getting data from DAOService", e);
        }
    }

    // *** Displaying section *************************************************

    /**
     * TagSupport entry point
     */

    public int doStartTag() throws JspTagException {

        this.print(pageContext.getOut(), pageContext);
        this.even = true;
        return SKIP_BODY;

    }

    public void print(Writer out, PageContext context) {

        // If there is any object with the SAME KEY we are going to use to keep
        // the current
        // item when iterating, save it to restore it after.
        Object o = context.getAttribute(ITEM_PAGE_KEY, PageContext.PAGE_SCOPE);

        try {
            Collection results = null;

            // Get the data.
            if (this.name != null) {
                // First try the Request to see if we have to re-use some data.
                results = (Collection) context.getRequest().getAttribute(this.name);
            } else {
                // If not we'll try to get our own data.
                results = this.getResults();
            }

            if (this.writeSearchIntoProperty != null) {
                // Just save results in page property
                pageContext.setAttribute(this.writeSearchIntoProperty, results);
            } else {
                // Or print the results.
                out.write("\n<!-- Taglib generated HTML code.  -->");
                this.print(out, context, results);
                out.write("<!-- END of Taglib generated HTML code -->\n");
            }

        } catch (ModelException e) {
            try {
                out.write("Problem getting data: " + e.getFullMessage());
            } catch (Exception ex) {
            }
            log.error("Error retrieving Bean Data. ", e);
        } catch (IOException e) {
            log.error("Error writing bean data. ", e);
        }

        // Restore attribute to the previous value or delete it
        if (o != null)
            context.setAttribute(ITEM_PAGE_KEY, o, PageContext.PAGE_SCOPE);
        else
            context.removeAttribute(ITEM_PAGE_KEY, PageContext.PAGE_SCOPE);

    }

    /**
     * Print a Collection of results in the given out.
     *
     * @param out
     *            The JSP output Writer.
     * @param results
     *            The Collection of beans to print
     *
     * @throws IOException
     *             If anything went wrong with the writing.
     */

    protected void print(Writer out, PageContext context, Collection results) throws IOException {

        if (fieldMark != null)
            this.printFieldMark(out, context, results);
        if (this.fields == null)
            this.fields = this.getFieldsFromCollection(results);
        if (this.fieldNames == null)
            this.fieldNames = this.getFieldNamesFromCollection(results);

        if (!"list".equals(this.mode)) {

            // Container table
            out.write("\n<table " + ((this.outerTableClass != null) ? " class=\"" + this.outerTableClass + "\"" : "")
                    + " width=\"" + this.width + "\">");

            if (this.showTitle) {
                out.write(
                        "\n         <caption " + ((this.titleClass != null) ? "class=\"" + this.titleClass + "\"" : "")
                                + ">" + this.title + "</caption>");
            }

            if (this.showFieldNames && !"rows".equals(this.fieldsOrientation)) {
                out.write("\n<thead><tr>\n");
                Iterator i = this.fieldNames.iterator();
                while (i.hasNext())
                    out.write("<th " + ((this.thClass != null) ? "class=\"" + this.thClass + "\"" : "") + ">" + i.next()
                            + "</th>");

                out.write("\n</tr>\n");
                out.write("\n</thead>");
            }

            out.write("\n<tbody>");
            int total = results.size();
            int pos = 1;
            for (Iterator i = results.iterator(); i.hasNext();) {
                ModelBean bean = (ModelBean) i.next();
                context.setAttribute(ITEM_PAGE_KEY, bean, PageContext.PAGE_SCOPE);
                // out.write("\n <tr " + ((this.alternateRows) ? "class=\"" +
                // getAlternatedRowClass() + "\"" : "") + ">");
                // out.write(" <td>");
                this.printBean(out, context, bean, true, pos++, total);
                // out.write(" </td>");
                // out.write(" </tr>\n");
            }

            out.write("\n</tbody>");
            out.write("</table>\n");

        } else {

            // Container list

            if (this.showTitle)
                out.write("\n<h3 " + ((this.titleClass != null) ? "class=\"" + this.titleClass + "\"" : "") + ">"
                        + this.title + "</h3>");

            out.write(
                    "\n<ul" + ((this.outerTableClass != null) ? " class=\"" + this.outerTableClass + "\"" : "") + ">");

            int total = results.size();
            int pos = 1;
            for (Iterator i = results.iterator(); i.hasNext();) {
                ModelBean bean = (ModelBean) i.next();
                context.setAttribute(ITEM_PAGE_KEY, bean, PageContext.PAGE_SCOPE);
                // out.write("\n <li>");
                this.printBean(out, context, bean, false, pos++, total);
                // out.write(" </li>\n");
            }

            out.write("\n</ul>\n");

        }

    }

    // HOOKS for children classes **********************************************

    /**
     * Hook executed BEFORE a bean is printed.
     *
     * @param bean
     *            The bean that is going to be printed.
     */

    public void prePrintBeanHook(ModelBean bean) {
        // Empty default implementation
    }

    /**
     * Hook executed AFTER a bean is printed.
     *
     * @param bean
     *            The bean that has been printed.
     */

    public void postPrintBeanHook(ModelBean bean) {
        // Empty default implementation
    }

    /**
     * Hook executed BEFORE a field is printed
     *
     * @param bean
     *            The bean that is going to be printed.
     * @param name
     *            The name of the field that has been printed.
     * @param value
     *            The value of the field that has been printed.
     */

    public void prePrintFieldHook(ModelBean bean, String name, String value) {
        // Empty default implementation
    }

    /**
     * Hook executed AFTER a field is printed
     *
     * @param bean
     *            The bean that has been printed.
     * @param name
     *            The name of the field that has been printed.
     * @param value
     *            The value of the field that has been printed.
     */

    public void postPrintFieldHook(ModelBean bean, String name, String value) {
        // Empty default implementation
    }

    public Collection getFieldsFromCollection(Collection results) {
        if (results.size() == 0)
            return new ArrayList(0);
        else {
            return ModelUtils.getFieldsFromBean(results.iterator().next());
        }
    }

    public ArrayList getFieldNamesFromCollection(Collection results) {
        if (results.size() == 0)
            return new ArrayList(0);
        else {
            Collection fields = (this.fields != null) ? this.fields
                    : ModelUtils.getFieldsFromBean(results.iterator().next());
            ArrayList names = new ArrayList(fields.size());
            Iterator i = fields.iterator();
            while (i.hasNext())
                names.add(Util.capitalize(i.next().toString()));
            return names;
        }
    }

    // *** End of HOOKS *******************************************************

    /**
     * This method will print to the supplied JspWriter the supplied bean. If we have a field list, we will use it, or
     * else print the full fields list.
     *
     * @param out
     *            Where to write.
     * @param bean
     *            The bean to be painted.
     */

    public void printBean(Writer out, PageContext context, ModelBean bean, boolean table, int pos, int total)
            throws IOException {

        // Call the hook
        this.prePrintBeanHook(bean);

        if (table) {

            // Inner ("record") table
            // out.write("\n<table " + ((this.innerTableClass != null) ? "
            // class=\"" + this.innerTableClass + "\"" : "") + " border=\"0\"
            // cellspacing=\"0\" cellpadding=\"1\" height=\"" + this.height
            // + "\" width=\"" + this.width + "\">");

            if (!"rows".equals(this.fieldsOrientation))
                out.write("\n       <tr " + ((this.alternateRows) ? "class=\"" + getAlternatedRowClass() + "\"" : "")
                        + ">");

            Iterator i = this.fields.iterator();
            int count = 0;
            String previousValue = null;
            while (i.hasNext()) {

                // Several options now: showTitle x
                // fieldsOrientation(columns|rows)

                String field = (String) i.next();
                String value = this.getFieldValueFromBean(context, bean, field);

                String style = null;
                if (this.styles != null && this.styles.size() > count)
                    style = (String) this.styles.get(count);

                if ("rows".equals(this.fieldsOrientation)) {
                    this.printFieldInRows(out, context, field, value,
                            (count < this.fieldNames.size()) ? this.fieldNames.get(count).toString() : "", bean, style,
                            !i.hasNext(), getAlternatedRowClass());
                } else {
                    this.printFieldInColumns(out, context, value, previousValue,
                            this.getColumnWidthAttribute(count, true), bean, style);
                }
                count++;
                previousValue = value;

            }
            if (!"rows".equals(this.fieldsOrientation))
                out.write("</tr>");

            // out.write(" </table>");

        } else {

            // LIST MODE

            int nFields = this.fields.size();
            Iterator i = this.fields.iterator();
            int count = 0;

            if (nFields > 1)
                out.write("<li><ul" + ((this.innerTableClass != null) ? " class=\"" + this.innerTableClass + "\"" : "")
                        + ">");

            while (i.hasNext()) {

                String style = null;
                if (this.styles != null && this.styles.size() > count)
                    style = (String) this.styles.get(count);

                out.write("<li" + ((style != null) ? " class=\"" + style + "\"" : "") + ">");

                if (this.showLink)
                    out.write("<a href=\"" + TagUtils.getLink(context, this.link, this.basePathKey) + "\">");

                out.write(this.getFieldValueFromBean(context, bean, i.next().toString()));
                if (this.showLink)
                    out.write("</a>");

                out.write("</li>");

                count++;

            }

            if (nFields > 1)
                out.write("</ul></li>\n");

        }

        // Call the hook
        this.postPrintBeanHook(bean);

    }

    public String getFieldValueFromBean(PageContext context, ModelBean bean, String name) {
        try {
            if (this.fieldLengthLimit > 0)
                return TagUtils.getFieldValueFromBean(context, bean, name, this.fieldLengthLimit, true);
            else
                return TagUtils.getFieldValueFromBean(context, bean, name);
        } catch (ModelException e) {
            log.warn("Problem printing field: ", e);
            return "";
        }
    }

    private void printFieldInRows(Writer out, PageContext context, String name, String value, String fieldName,
            ModelBean bean, String style, boolean last, String rowStyle) throws IOException {

        // Call the hook
        this.prePrintFieldHook(bean, name, value);

        String thClass = (style != null) ? style : this.thClass;
        String tdClass = (style != null) ? style : this.tdClass;

        out.write("                           \n<tr class=\"" + rowStyle + "\">");

        if (this.showFieldNames) {
            out.write("                           <th " + this.getColumnWidthAttribute(0, false)
                    + " valign=\"top\" align=\"right\" " + ((thClass != null) ? "class=\"" + thClass + "\"" : "")
                    + ">");
            // Print the field name.
            out.write(name);
            out.write("                           </th>");
        }

        out.write("                           <td valign=\"top\" " + this.getColumnWidthAttribute(1, false) + " "
                + ((tdClass != null) ? "class=\"" + tdClass + "\"" : "") + ">");

        if (this.showLink)
            out.write("<a href=\"" + TagUtils.getLink(context, this.link, this.basePathKey) + "\">");
        // if (this.foregroundColor != null) {
        // out.write("<div style=\"color: " + this.foregroundColor + "\">");
        // }

        // Print the field value,
        out.write(value + "&nbsp;");

        // if (this.foregroundColor != null) {
        // out.write("</div>");
        // }
        if (this.showLink)
            out.write("</a>");

        out.write("                           </td>");
        out.write("                           </tr>");

        // Call the hook
        this.postPrintFieldHook(bean, name, value);

    }

    private void printFieldInColumns(Writer out, PageContext context, String value, String previousValue,
            String widthAttribute, ModelBean bean, String style) throws IOException {

        // Call the hook
        this.prePrintFieldHook(bean, name, value);

        String tdClass = (style != null) ? style : this.tdClass;

        out.write("                           <td valign=\"top\" " + widthAttribute + " "
                + ((tdClass != null) ? "class=\"" + tdClass + "\"" : "") + ">");

        if (!this.ignoreSuccesiveEqualFields || previousValue == null || !previousValue.equals(value)) {
            if (this.showLink)
                out.write("<a href=\"" + TagUtils.getLink(context, this.link, this.basePathKey) + "\">");

            out.write(value + "&nbsp; ");

            if (this.showLink)
                out.write("</a>");

        }
        out.write("                           </td>");

        // Call the hook
        this.postPrintFieldHook(bean, name, value);

    }

    /**
     * Return the relevant "width" attribute name and value pair for a given column number.
     *
     * @param columnNumber
     *            Column number, starting with 0
     * @param columnMode
     *            If the fields are shown in columns, versus in rows.
     *
     * @return The width (as in "width='100'") attribute. Empty if default values
     */

    public String getColumnWidthAttribute(int columnNumber, boolean columnMode) {

        String attribute = "";
        if (this.columnsWidth != null && this.columnsWidth.size() > columnNumber && this.widthType != WD_NONE) {
            // We have explicit column widths.
            attribute = "width = \"" + this.columnsWidth.get(columnNumber).toString()
                    + ((this.widthType == WD_PERCENTAGE) ? "%" : "") + "\"";
        } else {
            // Default column widths.
            if (columnMode && this.widthType != WD_NONE)
                if (this.widthType == WD_FIXED)
                    attribute = "width=\"" + ((this.fields != null) ? (this.width / this.fields.size()) : 150) + "\"";
                else
                    attribute = "width=\"" + ((this.fields != null) ? (100 / this.fields.size()) : 33) + "%\"";
        }

        return attribute;

    }

    private void printFieldMark(Writer out, PageContext context, Collection results) throws IOException {

        if (results != null && results.size() > 0) {
            ModelBean bean = (ModelBean) results.iterator().next();
            out.write("<script language=\"JavaScript\">");
            try {
                out.write("var fieldMark='" + TagUtils.getFieldValueFromBean(context, bean, this.fieldMark) + "'");
            } catch (ModelException e) {
                out.write("var fieldMark='Value not found'");
            }
            out.write("</script>");
        }

    }

    private String getAlternatedRowClass() {
        even = !even;
        return even ? "even" : "odd";
    }

}
