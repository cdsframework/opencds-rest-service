/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.cdsframework.rest.opencds.resources.CdsInputXmlMessageBodyReader;
import org.cdsframework.rest.opencds.resources.CdsOutputXmlMessageBodyWriter;

/**
 *
 * @author sdn
 */
@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(CdsInputXmlMessageBodyReader.class);
        resources.add(CdsOutputXmlMessageBodyWriter.class);
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.cdsframework.rest.opencds.EvaluateResource.class);
        resources.add(org.cdsframework.rest.opencds.resources.CdsInputXmlMessageBodyReader.class);
        resources.add(org.cdsframework.rest.opencds.resources.CdsOutputXmlMessageBodyWriter.class);
        resources.add(org.cdsframework.rs.provider.CoreJacksonJsonProvider.class);
    }
    
}