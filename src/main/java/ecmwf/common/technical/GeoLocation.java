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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * The Class GeoLocation.
 */
public final class GeoLocation {
    /** The Constant ICON_URL_RED_CIRCLE. */
    public static final String ICON_URL_RED_CIRCLE = Cnf.at("GeoLocation", "iconRedCircle",
            "https://maps.google.com/mapfiles/kml/paddle/red-circle.png");

    /** The Constant ICON_URL_PINK_CIRCLE. */
    public static final String ICON_URL_PINK_CIRCLE = Cnf.at("GeoLocation", "iconPinkCircle",
            "https://maps.google.com/mapfiles/kml/paddle/pink-circle.png");

    /** The Constant ICON_URL_BLUE_CIRCLE. */
    public static final String ICON_URL_BLUE_CIRCLE = Cnf.at("GeoLocation", "iconBlueCircle",
            "https://maps.google.com/mapfiles/kml/paddle/blu-circle.png");

    /** The Constant ICON_URL_GREEN_CIRCLE. */
    public static final String ICON_URL_GREEN_CIRCLE = Cnf.at("GeoLocation", "iconGreenCircle",
            "https://maps.google.com/mapfiles/kml/paddle/grn-circle.png");

    /** The Constant ICON_URL_YELLOW_CIRCLE. */
    public static final String ICON_URL_YELLOW_CIRCLE = Cnf.at("GeoLocation", "iconYellowCircle",
            "https://maps.google.com/mapfiles/kml/paddle/ylw-circle.png");

    /** The Constant ICON_URL_WHITE_CIRCLE. */
    public static final String ICON_URL_WHITE_CIRCLE = Cnf.at("GeoLocation", "iconWhiteCircle",
            "https://maps.google.com/mapfiles/kml/paddle/wht-circle.png");

    /**
     * The Class GeoEntry.
     */
    public static class GeoEntry {
        /** The name. */
        public String name;

        /** The description. */
        public String description;

        /** The latitude. */
        public Double latitude;

        /** The longitude. */
        public Double longitude;

        /** The icon url. */
        public String iconUrl;

        /** The icon scale. */
        public double iconScale;

        /**
         * Gets the icon id.
         *
         * @return the icon id
         */
        long getIconId() {
            return Math.abs((long) (iconUrl + iconScale).hashCode());
        }
    }

    // public static void main(String[] args) throws IOException {
    // GeoEntry entry = new GeoEntry();
    // entry.name = "ECMWF";
    // entry.descritpion = "Weather Center";
    // entry.longitute = new Float(-0.950822);
    // entry.latitude = new Float(51.419697);
    // entry.iconScale = 1.1;
    // entry.iconUrl = ICON_URL_RED_CIRCLE;
    // System.out.print(createKML("ECPDS Hosts", new GeoEntry[] { entry }));
    // }

    /**
     * Creates the kml.
     *
     * @param name
     *            the name
     * @param entries
     *            the entries
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String createKML(final String name, final GeoEntry[] entries) throws IOException {
        final var writer = new ByteArrayOutputStream();
        createKML(writer, name, entries);
        return writer.toString();
    }

    /**
     * Creates the kml.
     *
     * @param filename
     *            the filename
     * @param name
     *            the name
     * @param entries
     *            the entries
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void createKML(final String filename, final String name, final GeoEntry[] entries)
            throws IOException {
        createKML(new FileOutputStream(filename), name, entries);
    }

    /**
     * Creates the kml.
     *
     * @param out
     *            the out
     * @param name
     *            the name
     * @param entries
     *            the entries
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void createKML(final OutputStream out, final String name, final GeoEntry[] entries)
            throws IOException {
        // Generate XML stub
        final var ns = Namespace.getNamespace("", "http://earth.google.com/kml/2.2");
        final var kml = new Element("kml", ns);
        final var kmlDocument = new Document(kml);
        final var document = new Element("Document", ns);
        kml.addContent(document);
        final var elementName = new Element("name", ns);
        elementName.addContent(new CDATA(name));
        document.addContent(elementName);
        // Add Style elements
        final var iconIds = new Vector<Long>();
        for (final GeoEntry line : entries) {
            final var iconId = line.getIconId();
            if (!iconIds.contains(iconId)) {
                final var style = new Element("Style", ns);
                style.setAttribute("id", String.valueOf(iconId));
                document.addContent(style);
                final var iconStyle = new Element("IconStyle", ns);
                style.addContent(iconStyle);
                final var iconScale = new Element("Scale", ns);
                iconScale.setText(String.valueOf(line.iconScale));
                iconStyle.addContent(iconScale);
                final var icon = new Element("Icon", ns);
                iconStyle.addContent(icon);
                final var href = new Element("href", ns);
                href.setText(line.iconUrl);
                icon.addContent(href);
                iconIds.add(iconId);
            }
        }
        // Add Placemarks
        for (final GeoEntry line : entries) {
            final var placemark = new Element("Placemark", ns);
            document.addContent(placemark);
            final var pmName = new Element("name", ns);
            pmName.addContent(new CDATA(line.name));
            placemark.addContent(pmName);
            final var pmDescription = new Element("description", ns);
            pmDescription.addContent(new CDATA(line.description));
            placemark.addContent(pmDescription);
            final var pmStyleUrl = new Element("styleUrl", ns);
            pmStyleUrl.setText("#" + line.getIconId());
            placemark.addContent(pmStyleUrl);
            final var pmPoint = new Element("Point", ns);
            placemark.addContent(pmPoint);
            final var pmCoordinates = new Element("coordinates", ns);
            pmCoordinates.setText(line.longitude + "," + line.latitude + ",0");
            pmPoint.addContent(pmCoordinates);
        }
		// Write the XML file
		final var outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(kmlDocument, out);
		out.flush();
    }
}
