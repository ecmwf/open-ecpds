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

/*
 * Created on 06-Apr-2005
 *
 */
package ecmwf.ecpds.master.plugin.http.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.services.config.ConfigService;

/**
 * @author sy8
 *
 *         TODO: Comment ME !!!
 */
public class Util {

    private static Log log = LogFactory.getLog(Util.class);

    public static ArrayList listStringToArrayList(String data) {
        return listStringToArrayList(data, ",");
    }

    public static Collection listStringToCollection(String data) {
        return listStringToArrayList(data, ",");
    }

    public static ArrayList listStringToArrayList(String data, String separator) {

        if (data == null || separator == null)
            return null;

        ArrayList l = new ArrayList(1);
        StringTokenizer st = new StringTokenizer(data, separator);
        while (st.hasMoreTokens())
            l.add(st.nextToken().trim());
        return l;

    }

    public static Collection listStringToCollection(String data, String separator) {
        return listStringToArrayList(data, separator);
    }

    public static String collectionToListString(Collection data, String separator) {

        if (data == null || data.size() == 0)
            return "";
        else {
            String out = "";
            Iterator i = data.iterator();
            while (i.hasNext())
                out += (String) i.next() + separator;
            return out.substring(0, out.length() - separator.length());

        }

    }

    public static String capitalize(String before) {

        String after = before.toLowerCase();

        // Capitalize the first letters of every word, lowercasing the rest
        StringBuffer out = new StringBuffer(after);
        int i = 0;
        char previous;
        char current = out.charAt(i);
        while (i < out.length()) {
            if (i == 0 && Character.isLetter(current)) {
                out.setCharAt(i, Character.toUpperCase(current));
            } else {
                previous = out.charAt(i - 1);
                if (previous == ' ' || previous == '(' || previous == '/' || previous == '-' || previous == '.') {
                    current = out.charAt(i);
                    out.setCharAt(i, Character.toUpperCase(current));
                }
            }
            i++;
        }

        return out.toString();

    }

    public static String getGetterName(String property) {
        return "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }

    public static void delete(File dir) throws IOException {

        if (!dir.exists())
            return;

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            if (files == null)
                throw new IOException("Error getting the listing of directory " + dir);

            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
            if (log.isDebugEnabled())
                log.debug("Deleting directory " + dir);

            if (!dir.delete())
                throw new IOException("Error deleting directory " + dir);
        } else {
            if (log.isDebugEnabled())
                log.debug("Deleting file " + dir);
            if (!dir.delete())
                throw new IOException("Error deleting file " + dir);
        }

    }

    public static void move(File origin, File destination) throws IOException {
        copy(origin, destination);
        delete(origin);
    }

    public static void copy(File origin, File destination) throws IOException {
        copy(origin, destination, origin.isDirectory() ? origin : origin.getParentFile(),
                origin.isDirectory() ? destination : destination.getParentFile());
    }

    public static void copy(File origin, File destination, File originBase, File destinationBase) throws IOException {

        if (log.isDebugEnabled())
            log.debug("Copying file " + origin + " to " + destination + ", basedirs orig " + originBase + " and dest "
                    + destinationBase);

        if (!origin.exists()) {
            throw new IOException(origin + " doesn't exist. Can't copy it");
        }

        if (origin.isDirectory()) {
            if (!destination.exists())
                if (!destination.mkdirs())
                    throw new IOException("Error creating destination directory " + destination);

            File[] files = origin.listFiles();
            for (int i = 0; i < files.length; i++) {
                File orig = files[i];
                File dest = new File(destinationBase, getFilePathDifference(orig, originBase));
                copy(orig, dest);
            }

        } else {
            copyFile(origin, destination);
        }

    }

    public static void copyFile(File in, File out) throws IOException {

        if (log.isDebugEnabled())
            log.debug("Copying file " + in + " to " + out);

        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[32 * 1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }

    public static String getFilePathDifference(File f, File base) throws IOException {
        int lenBase = base.getCanonicalPath().length(), lenFile = f.getCanonicalPath().length();
        if (lenBase < lenFile) {
            return f.getCanonicalPath().substring(lenBase, lenFile);
        } else
            return "";
    }

    public static String getExtension(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0 && fileName.indexOf(File.separator, pos) < 0)
            return fileName.substring(pos + 1);
        else
            return "";
    }

    public static String guessContentType(String extension) {
        return ConfigService.getOptionalParameter("uploads", "type." + extension, "text/plain");
    }

    public static String readFully(InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        StringBuffer out = new StringBuffer();
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            out.append(new String(buffer, 0, read));
        }
        is.close();
        return out.toString();

    }

    public static final boolean isNumeric(final String s) {

        if (s == null)
            return false;

        char[] numbers = s.toCharArray();
        for (int x = 0; x < numbers.length; x++) {
            final char c = numbers[x];
            if ((c >= '0') && (c <= '9'))
                continue;
            return false; // invalid
        }
        return true; // valid
    }

    public static void enforceClassAccess(String theOnlyAllowedClass) throws IllegalAccessException {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        String accessor = trace[2].getClassName();
        if (!accessor.equals(theOnlyAllowedClass))
            throw new IllegalAccessException("Access to this method is only allowed to class '" + theOnlyAllowedClass
                    + "'. Class '" + accessor + "' is not allowed.");
    }

    public static void enforceClassesAccess(String allowedClass1, String allowedClass2) throws IllegalAccessException {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        String accessor = trace[2].getClassName();
        if (!accessor.equals(allowedClass1) && !accessor.equals(allowedClass2))
            throw new IllegalAccessException("Access to this method is only allowed to classes '" + allowedClass1
                    + "' and '" + allowedClass2 + "'. Class '" + accessor + "' is not allowed.");
    }

    public static String sanitizeUrl(String path) {
        while (path.indexOf("//") > 0)
            path = path.replace("//", "/");
        return path;
    }

    public static void main(String[] args) throws IOException {

        String command = args[0];
        if ("copy".equals(command)) {
            copy(new File(args[1]), new File(args[2]));
        } else if ("move".equals(command)) {
            move(new File(args[1]), new File(args[2]));
        } else if ("delete".equals(command)) {
            delete(new File(args[1]));
        } else if ("renameTo".equals(command)) {
            System.out.println("'renameTo' returns " + (new File(args[1]).renameTo(new File(args[2]))));
        } else {
            System.err.println("Command '" + command + "' unknown");
            System.exit(-1);
        }

    }

}
