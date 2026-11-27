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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.dao.AttributedModelBean;
import ecmwf.ecpds.master.plugin.http.dao.ModelSearchBase;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOService;
import ecmwf.ecpds.master.plugin.http.util.search.AttributedBeanComparator;

/**
 * This class implements a JSP Tag whose main objective is to take a Collection of AttributedModelBean and display them
 * in a table using some supplied parameters.
 *
 * @deprecated Use something like "displaytable" tag for the same.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

@Deprecated
public class AttributeBeanSearch extends TagSupport {

    private static Log log = LogFactory.getLog(AttributeBeanSearch.class);
    private String theClass;
    private String filter;
    private Vector fields = null;
    private Vector order = null;
    private boolean reverseSortOrder = false;
    private Vector labels = null;
    private String thClass = null;
    private String tdClass = null;

    private String title = "Search results";
    private String width = "400";
    private String leftColumnWidth = "30%";
    private String rightColumnWidth = "70%";
    private String titleClass = null;
    private String outerLineColor = "#FFF1B3";
    private String innerLineColor = "#FFF1B3";
    private String backgroundColor = "#FFFFFF";

    /**
     * Hook method called by the taglib container when a "class" attribute is found in the Tag.
     *
     * @param theClass
     *            The Bean class we are going to search.
     */

    public void setTheClass(String theClass) {

        this.theClass = theClass;

    }

    /**
     * Hook method called by the taglib container when a "filter" attribute is found in the Tag.
     *
     * @param filter
     *            The filter to apply to the bean attributes.
     */

    public void setFilter(String filter) {

        this.filter = filter;

    }

    /**
     * Hook method called by the taglib container when a "fields" attribute is found in the Tag.
     *
     * @param fields
     *            The fields to show as a result of the search. If any of then doesn't exist, a warning message will be
     *            shown, but the execution won't be interrupted.
     */

    public void setFields(String fields) {

        this.fields = new Vector();
        StringTokenizer st = new StringTokenizer(fields, ",");
        while (st.hasMoreElements()) {
            this.fields.addElement(((String) st.nextElement()).trim());
        }

    }

    /**
     * Hook method called by the taglib container when a "order" attribute is found in the Tag.
     *
     * @param order
     *            The ordering fields
     */

    public void setOrder(String order) {

        this.order = new Vector();
        StringTokenizer st = new StringTokenizer(order, ",");
        while (st.hasMoreElements()) {
            this.order.addElement(((String) st.nextElement()).trim());
        }

    }

    /**
     * Hook method called by the taglib container when a "sortOrder" attribute is found in the Tag.
     *
     * @param sortOrder
     *            If the ordering is ascending ("ASC") or descending ("DESC").
     */

    public void setSortOrder(String sortOrder) {

        this.reverseSortOrder = sortOrder.toLowerCase().trim().equals("desc");

    }

    /**
     * Hook method called by the taglib container when a "labels" attribute is found in the Tag.
     *
     * @param fields
     *            The fields names used to identify every value shown. They should be specified in the same order than
     *            in the "fields" property.
     */

    public void setLabels(String labels) {

        this.labels = new Vector();
        StringTokenizer st = new StringTokenizer(labels, ",");
        while (st.hasMoreElements()) {
            this.labels.addElement(((String) st.nextElement()).trim());
        }

    }

    /**
     * Hook method called by the taglib container when a "tdClass" attribute is found in the Tag.
     *
     * @param tdClass
     *            The CSS class to be used for the "value" cells of the inner table.
     */

    public void setTdClass(String tdClass) {

        this.tdClass = tdClass;

    }

    /**
     * Hook method called by the taglib container when a "thClass" attribute is found in the Tag.
     *
     * @param thClass
     *            The CSS class to be used for the "label" cells of the inner table.
     */

    public void setThClass(String thClass) {

        this.thClass = thClass;

    }

    /**
     * Hook method called by the taglib container when a "title" attribute is found in the Tag.
     *
     * @param title
     *            The string literal to show in the main box title.
     */

    public void setTitle(String title) {

        this.title = title;

    }

    /**
     * Hook method called by the taglib container when a "titleClass" attribute is found in the Tag.
     *
     * @param titleClass
     *            The CSS class to use for the title field, on top of the table.
     */

    public void setTitleClass(String titleClass) {

        this.titleClass = titleClass;

    }

    /**
     * Hook method called by the taglib container when a "width" attribute is found in the Tag.
     *
     * @param width
     *            The width of the main table.
     */

    public void setWidth(String width) {

        this.width = width;

    }

    /**
     * Hook method called by the taglib container when a "colWidthRelation" attribute is found in the Tag.
     *
     * @param colWidthRelation
     *            The relation between the width of the two columns: Left column width / Right column width.
     */

    public void setColWidthRelation(String colWidthRelation) {

        try {
            float relation = Float.parseFloat(colWidthRelation);
            float leftPct = 100 * relation / (1 + relation);
            float rightPct = 100 - leftPct;
            this.leftColumnWidth = Integer.toString(new Float(leftPct).intValue()) + "%";
            this.rightColumnWidth = Integer.toString(new Float(rightPct).intValue()) + "%";
        } catch (NumberFormatException e) {
            log.warn("Invalid number in AttributeBeanSearch.setColWidthRelation:" + e.getMessage());
        }

    }

    /**
     * Hook method called by the taglib container when a "outerLineColor" attribute is found in the Tag.
     *
     * @param width
     *            The color of the outer table line.
     */

    public void setOuterLineColor(String outerLineColor) {

        this.outerLineColor = outerLineColor;

    }

    /**
     * Hook method called by the taglib container when a "innerLineColor" attribute is found in the Tag.
     *
     * @param width
     *            The color of the inner table line.
     */

    public void setInnerLineColor(String innerLineColor) {

        this.innerLineColor = innerLineColor;

    }

    /**
     * Hook method called by the taglib container at the start of the tag. Access the DAOService to retrieve a vector of
     * the Bean instances, filtered by the filter. Then print the info using the configurable values set by the
     * attributes.
     *
     * @return SKIP_BODY, since this tag has no body.
     */

    public int doStartTag() throws JspTagException {

        // RealmService.log(RealmService.DEBUG," AttributeBeanSearch startTag() !!!");

        try {
            // Get data. A way of retrieving data from the session should also be defined, since this breaks
            // a little the MVC pattern.
            Collection results = DAOService.find(this.theClass, new ModelSearchBase(this.filter));
            if (this.order != null) // Sort them if needed. This could be optimized for the case in which there is only
                                    // one attribute in "order".
                Collections.sort((List) results, new AttributedBeanComparator(this.order));
            if (this.reverseSortOrder)
                Collections.reverse((List) results);

            // And print it.
            pageContext.getOut().println("\n<!-- Taglib generated HTML code.  -->");
            this.print(pageContext.getOut(), results);
            pageContext.getOut().println("<!-- END of Taglib generated HTML code -->\n");

        } catch (DAOException e) {
            log.error(" AttributeBeanSearch: Error retrieving Bean Data: " + e.getMessage());
        } catch (IOException e) {
            log.error(" AttributeBeanSearch: Error writing bean data: " + e.getMessage());
        }

        return SKIP_BODY;

    }

    protected void print(JspWriter out, Collection results) throws IOException {

        out.println("<table bgcolor=\"" + this.outerLineColor + "\" width=\"" + this.width
                + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" >");
        out.println("   <tr>");
        out.println("   <td bgcolor=\"" + this.outerLineColor + "\">");
        out.println("      <table bgcolor=\"" + this.backgroundColor
                + "\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" >");
        out.println("         <tr><td align=\"center\" bgcolor=\"" + this.outerLineColor + "\" "
                + ((this.titleClass != null) ? "class=\"" + this.titleClass + "\"" : "") + ">" + this.title
                + "</td></tr>");

        for (Iterator i = results.iterator(); i.hasNext();) {
            AttributedModelBean bean = (AttributedModelBean) i.next();
            out.println("         <tr>");
            out.println("         <td>");
            this.printBean(out, bean);
            out.println("         </td>");
            out.println("         </tr>");
        }

        out.println("      </table>");
        out.println("   </td>");
        out.println("   </tr>");
        out.println("</table>");

    }

    protected void printBean(JspWriter out, AttributedModelBean bean) throws IOException {

        Iterator e, f;
        out.println("                          <table bgcolor=\"" + this.innerLineColor
                + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" width=\"100%\">");
        out.println("                            <tr><td><table bgcolor=\"" + this.backgroundColor
                + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" width=\"100%\">");

        if (this.fields == null)
            e = bean.getAttributeNames();
        else
            e = this.fields.iterator();

        if (this.labels == null)
            f = new Vector().iterator();
        else
            f = this.labels.iterator();

        while (e.hasNext()) {

            String name = (String) e.next();
            String label = f.hasNext() ? (String) f.next() : name;

            // If NONE was supplied as a label, it means that the value will be shown full width (colspan=2).
            if (!"NONE".equals(label)) {

                out.println("                           <tr>");
                out.println("                           <th width=\"" + this.leftColumnWidth
                        + "\" valign=\"top\" align=\"right\" "
                        + ((this.thClass != null) ? "class=\"" + this.thClass + "\"" : "") + ">");

                out.println(label);

                out.println("                           </th>");
                out.println("                           <td width=\"" + this.rightColumnWidth + "\" "
                        + ((this.tdClass != null) ? "class=\"" + this.tdClass + "\"" : "") + ">");

            } else {

                out.println("                           <tr><td colspan=\"2\" >&nbsp;</td></tr>");
                out.println("                           <tr>");

                out.println("                           <td colspan=\"2\" width=\"" + this.rightColumnWidth + "\" "
                        + ((this.tdClass != null) ? "class=\"" + this.tdClass + "\"" : "") + ">");
            }

            // The attribute value, if defined.
            try {
                out.println("&nbsp; " + bean.getAttribute(name));
            } catch (ModelException ex) {
                out.println("&nbsp;");
                // log.warn("The bean "+bean+" does not have an attribute called '"+name+"': "+ex.getMessage());
            }
            out.println("                           </td>");
            out.println("                           </tr>");

        }

        out.println("                            </td></tr></table>");
        out.println("                          </table>");

    }

}
