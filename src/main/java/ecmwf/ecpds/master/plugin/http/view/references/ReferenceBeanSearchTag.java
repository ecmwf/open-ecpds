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

package ecmwf.ecpds.master.plugin.http.view.references;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.PageContext;

import ecmwf.ecpds.master.plugin.http.view.general.ModelBeanSearchTag;

import ecmwf.ecpds.master.plugin.http.model.ModelBean;
import ecmwf.ecpds.master.plugin.http.model.FileModel;
import ecmwf.ecpds.master.plugin.http.model.ModelUtils;
import ecmwf.ecpds.master.plugin.http.model.ModelException;
import ecmwf.ecpds.master.plugin.http.view.util.TagUtils;

import ecmwf.ecpds.master.plugin.http.model.references.Reference;
import ecmwf.ecpds.master.plugin.http.model.references.FieldValue;
import ecmwf.ecpds.master.plugin.http.model.references.ReferenceException;

/**
 * JSP Tag which displays the list of ReferenceBean's which match the supplied filter.
 *
 * @author <a href="mailto:dvarela@ecmwf.int">Daniel Varela Santoalla</a>
 *
 * @version 0.1.0
 */

public class ReferenceBeanSearchTag extends ModelBeanSearchTag {

    public ReferenceBeanSearchTag() {
        this.setTheClass(Reference.class.getName());
    }

    public Collection getFieldsFromBean(ModelBean bean) {

        Reference rb = (Reference) bean;
        Collection fields = ModelUtils.getFieldsFromBean(bean); // All references common "static" fields

        // EntryType-dependent "dynamic" fields.
        Iterator i = rb.getFieldValues().iterator();
        while (i.hasNext()) {
            fields.add(((FieldValue) i.next()).getField().getName());
        }

        return fields;
    }

    public String getFieldValueFromBean(PageContext context, ModelBean bean, String name) {

        try {
            if (name.equals("id") || name.equals("bibtexId") || name.equals("categoryId"))
                return ModelUtils.getFieldValueFromBean(bean, name).toString();

            if (name.equals("creationDate") || name.equals("modificationDate") || name.startsWith("creationDate::")
                    || name.startsWith("modificationDate::"))
                return super.getFieldValueFromBean(context, bean, name);

            Reference rb = (Reference) bean;
            if (name.equals("file")) {
                FileModel fmb = rb.getFile();
                if (fmb != null) {
                    return "<a href=\"" + TagUtils.getResource(context, "references.basepath", "") + "/show?id="
                            + rb.getId() + "&showfile=true\"><img src=\"" + fmb.getIcon() + "\" border=\"0\" alt=\""
                            + fmb.getName() + "\" alt=\"" + fmb.getName() + "\"></a>";
                } else {
                    return "";
                }
            }
            if (name.equals("url")) {
                if (rb.getUrl() != null) {
                    return "<a href=\"" + rb.getUrl() + "\" target=\"_new\"><img src=\""
                            + TagUtils.getResource(context, "icon.url", "") + "\" border=\"0\" alt=\"" + rb.getUrl()
                            + "\" title=\"" + rb.getUrl() + "\"></a>";
                } else {
                    return "";
                }
            }
            return rb.getFieldValueByName(name).getValue();

        } catch (ReferenceException e) {
            return "";
            // return "no field:" + e.getMessage(); // This is more possible .....
        } catch (ModelException e) {
            return "";
            // return "no field:" + e.getMessage(); // This is impossible, though.....
        }

    }

}
