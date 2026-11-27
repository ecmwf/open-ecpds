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

package ecmwf.ecpds.master.plugin.http.dao;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ecmwf.ecpds.master.plugin.http.model.FileModel;
import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.ModelSearch;

//import ecmwf.ecpds.master.plugin.http.services.log.LogService;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOHandler;
import ecmwf.ecpds.master.plugin.http.services.persistence.DAOException;

/**
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 1.0
 */

public class FileDAOHandler implements DAOHandler {

    private static Log log = LogFactory.getLog(FileDAOHandler.class);
    private static int BUFFER_SIZE = 16 * 1024;

    public ModelBean create() throws DAOException {
        throw new DAOException("Not implemented");

    }

    public ModelBean findByPrimaryKey(String key) throws DAOException {
        throw new DAOException("findByPrimaryKey:Not implemented");
    }

    public Collection findByQuery(String query) throws DAOException {
        throw new DAOException("Not implemented");
    }

    public Collection find(ModelSearch search) throws DAOException {
        throw new DAOException("Not implemented");
    }

    public void insert(ModelBean bean, Object context) throws DAOException {
        this.save(bean, context);
    }

    public void save(ModelBean bean, Object context) throws DAOException {

        try {

            FileModel fb = (FileModel) bean;
            if (fb.getSize() == 0)
                return;
            if (fb.getPhysicalPath() == null)
                throw new DAOException(
                        "The supplied FileModelBean has an empty 'Physical Path'. Don't know where to save it. ");
            InputStream is = fb.getFileInputStream();
            OutputStream os = new FileOutputStream(fb.getPhysicalPath());
            int bytesRead = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            if (log.isDebugEnabled())
                log.debug("The file has been written to \"" + fb.getPhysicalPath() + "\"");

        } catch (IOException e) {
            throw new DAOException("I/O error saving file", e);
        } catch (Exception e) {
            throw new DAOException("Unknown error saving file", e);
        }

    }

    public void delete(ModelBean bean, Object context) throws DAOException {

        try {

            FileModel fb = (FileModel) bean;
            if (fb.getPhysicalPath() == null)
                throw new DAOException("The supplied FileModelBean has an empty 'Physical Path'. Can't delete it. ");

            File f = new File(fb.getPhysicalPath());
            if (f.delete())
                log.debug("The file \"" + fb.getPhysicalPath() + "\" has been deleted.");
            else
                log.warn("The file \"" + fb.getPhysicalPath() + "\" has NOT been correctly deleted.");

        } catch (Exception e) {
            throw new DAOException("Unknown error while deleting file: ", e);
        }

    }

} // FileDAOHandler
