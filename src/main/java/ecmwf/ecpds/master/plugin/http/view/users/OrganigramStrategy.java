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

/**
 * Element ordering and layout strategy for OrganigramTag.
 * I've tried to put all the crap here to keep OrganigramUnitsTag tidy.
 * I failed.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 * @version 1.0
 */

import java.util.List;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import ecmwf.ecpds.master.plugin.http.services.log.LogService;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;
import ecmwf.ecpds.master.plugin.http.services.config.ConfigException;

import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgPerson;
import ecmwf.ecpds.master.plugin.http.model.users.org.OrgUnit;

public class OrganigramStrategy implements Comparator {

    private static Log log = LogFactory.getLog(OrganigramStrategy.class);
    // Hardcoded columns layout, depending on the number of elements.
    private static final int MAX_EXPECTED_ELEMENTS_NUMBER = 24;
    private static int[] columnsLayout;
    private static Hashtable theSort = null;
    private static String OWN_BOX = "";

    static {
        fillColumnsLayout();
        fillTheSort();
        fillOwnBox();
    }

    // Different layout strategys ...
    protected static final int WIDE = 0;
    // All other layouts mean the number of columns..
    private static final String OWN_BOX_ATTRIBUTE = "organigram.ownBox";

    public OrganigramStrategy() {
    }

    public int compare(Object o1, Object o2) {

        try {

            if (o1 instanceof OrgPerson && o2 instanceof OrgPerson) {
                return this.compareOrgPersons((OrgPerson) o1, (OrgPerson) o2);
            } else if (o1 instanceof OrgUnit && o2 instanceof OrgUnit) {
                return this.compareOrgUnits((OrgUnit) o1, (OrgUnit) o2);
            } else {
                log.error(
                        "OrganigramStrategy was supplied with an incorrect object type to sort. Expected OrgPerson or OrgUnit. ");
                return 0;
            }

        } catch (ClassCastException e) {
            log.error("OrganigramStrategy was supplied with an incorrect object type to sort. " + e.getMessage());
            return 0;
        }

    }

    /**
     * <p>
     * Return the layout to use for printing the remaining boxes in the list.
     * </p>
     * <p>
     * The idea is:
     * <ul>
     * <li>If this is the latest level and the element has no subordinates and is not a special (I want my own box)
     * case, or</li>
     * <li>If this is the first element and it has no subordinates (the elements are ordered by subordinates number)
     * then</li>
     * </ul>
     * Break and cluster the elements from this on.
     *
     * @param l
     *            The list of OrgPersons we are printing
     * @param alreadyPrinted
     *            The number of persons already printed
     * @param level
     *            The level we are printing now.
     * @param levels
     *            The maximum number of levels we are going to print.
     *
     * @return WIDE(0) if no grouping, the number of columns if grouping has to be made.
     */

    public int getLayout(List l, int alreadyPrinted, int level, int levels) {

        OrgPerson b = (OrgPerson) l.get(alreadyPrinted);
        if ("true".equals(b.getAttribute(OWN_BOX_ATTRIBUTE, "false"))) {
            return WIDE;
        } else {
            if (((level == levels) && (b.getSubordinates().size() == 0))
                    || ((alreadyPrinted == 0) && (b.getSubordinates().size() == 0)))
                // return getPerfectMatrixColumns(l.size()-alreadyPrinted);
                return getPerfectlyHardcodedMatrixColumns(l.size() - alreadyPrinted);
            else
                return WIDE;
        }

    }

    /**
     * Returns the "perfect" layout for a given number of elements.
     *
     * @numberOfElements The total number of elements we are going to print.
     *
     * @returns The number of columns we're going to use to paint them.
     */

    private static int getPerfectMatrixColumns(int numberOfElements) {

        // Get perfect divisors
        int MAX_TRIED = 6;
        int[] divisors = new int[MAX_TRIED];
        int found = -1;
        int columns;

        // Try all numbers from 2 to MAX_TRIED.
        for (int i = 2; i <= MAX_TRIED; i++) {
            if ((numberOfElements % i) == 0)
                divisors[++found] = i;
        }

        // Decide the best for the size
        if (found < 0) {
            // We didn't found nothing !! Must be a prime number bigger than 6.
            columns = (int) (numberOfElements / 3);
            // log.debug( "getPerfectMatrixColumn: No divisor found. Assuming (n/3): " + columns);
        } else {
            columns = divisors[0];
            if (columns == numberOfElements) // Must be a prime number lower than 6
                return 1;
            // log.debug( "getPerfectMatrixColumn: The first matching number is " + columns);
        }

        return columns;

    }

    /**
     * Returns the "perfectly hardcoded" layout for a given number of elements .
     *
     * @numberOfElements The total number of elements we are going to print.
     *
     * @returns The number of columns we're going to use to paint them.
     */

    public static int getPerfectlyHardcodedMatrixColumns(int numberOfElements) {
        if (numberOfElements < MAX_EXPECTED_ELEMENTS_NUMBER)
            return columnsLayout[numberOfElements];
        else
            return (int) (numberOfElements / 6);

    }

    private static String lookupWantsHisOwnBox(OrgPerson b) {

        try {
            return (b.getAttribute(OWN_BOX_ATTRIBUTE));
        } catch (ModelException e) {
            boolean wants = OWN_BOX.indexOf(b.getId()) >= 0;
            b.setAttribute(OWN_BOX_ATTRIBUTE, wants ? "true" : "false");
            return wants ? "true" : "false";
        }

    }

    private static void fillColumnsLayout() {

        columnsLayout = new int[MAX_EXPECTED_ELEMENTS_NUMBER];
        columnsLayout[0] = 1;
        columnsLayout[1] = 1;
        columnsLayout[2] = 1;
        columnsLayout[3] = 1;
        columnsLayout[4] = 2;
        columnsLayout[5] = 2;
        columnsLayout[6] = 3;
        columnsLayout[7] = 2;
        columnsLayout[8] = 4;
        columnsLayout[9] = 3;
        columnsLayout[10] = 4;
        columnsLayout[11] = 4;
        columnsLayout[12] = 4;
        columnsLayout[13] = 5;
        columnsLayout[14] = 5;
        columnsLayout[15] = 5;
        columnsLayout[16] = 4;
        columnsLayout[17] = 3;
        columnsLayout[18] = 5;
        columnsLayout[19] = 5;
        columnsLayout[20] = 5;
        columnsLayout[21] = 3;
        columnsLayout[22] = 4;
        columnsLayout[23] = 4;

    }

    private static void fillTheSort() {

        try {
            int i = 0;
            theSort = new Hashtable();
            String list = ConfigService.getParameter("directory", "the.sort.order");
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens()) {
                theSort.put(st.nextToken(), new Integer(i++));
            }
        } catch (ConfigException e) {
            log.error("Impossible to find init parameter: " + e.getMessage());
        }

    }

    private static void fillOwnBox() {

        try {
            OWN_BOX = ConfigService.getParameter("directory", "ownBox.list");
        } catch (ConfigException e) {
            log.error("Impossible to find init parameter: " + e.getMessage());
        }

    }

    /**
     * Returns a number representing the priority for sorting. Lower means MORE priority. The data source is the
     * directory.properties file.
     *
     * @param jobTitle
     *            The jobTitle description.
     *
     * @see fillTheSort()
     */

    private static int getSortPriority(String jobTitle) {

        String jTitle = jobTitle.toUpperCase();
        Enumeration e = theSort.keys();
        while (e.hasMoreElements()) {
            String title = (String) e.nextElement();
            if (jTitle.indexOf(title) >= 0) {
                return ((Integer) theSort.get(title)).intValue();
            }
        }
        return ((Integer) theSort.get("DEFAULT")).intValue();
    }

    /**
     * Compare OrgPersons to sort them.
     *
     * @param oub1
     *            The first person
     * @param oub2
     *            The second person.
     */

    private int compareOrgPersons(OrgPerson oub1, OrgPerson oub2) {

        try {

            String title1 = oub1.getAttribute("title");
            String title2 = oub2.getAttribute("title");

            int level1 = getSortPriority(title1);
            int level2 = getSortPriority(title2);
            if (level1 < level2)
                return -1;
            else if (level2 < level1)
                return 1;
            else if (title2.equals(title1))
                return oub1.getName().compareTo(oub2.getName());
            else
                return title2.compareTo(title1);

        } catch (ModelException e) {
            // log.warn( "OrganigramStrategy: Some object didn't have an attribute: " + e.getMessage());
            return 0;
        }

    }

    private int compareOrgUnits(OrgUnit oub1, OrgUnit oub2) {

        int level1 = oub1.getLevel();
        int level2 = oub2.getLevel();
        if (level1 > level2)
            return -1;
        else if (level2 > level1)
            return 1;
        else
            return 0;

    }

} // OrganigramStrategy
