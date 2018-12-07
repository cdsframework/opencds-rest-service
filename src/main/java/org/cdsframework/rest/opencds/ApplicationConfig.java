/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

import javax.ws.rs.ApplicationPath;
import org.cdsframework.opencds.rest.message.body.CdsInputXmlMessageBodyReader;
import org.cdsframework.opencds.rest.message.body.CdsInputXmlMessageBodyWriter;
import org.cdsframework.opencds.rest.message.body.CdsOutputXmlMessageBodyReader;
import org.cdsframework.opencds.rest.message.body.CdsOutputXmlMessageBodyWriter;
import org.cdsframework.rs.provider.CORSResponseFilter;
import org.cdsframework.rs.support.CoreRsConstants;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author sdn
 */
@ApplicationPath(CoreRsConstants.RESOURCE_ROOT)
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        super();
        register(CdsInputXmlMessageBodyReader.class);
        register(CdsInputXmlMessageBodyWriter.class);
        register(CdsOutputXmlMessageBodyWriter.class);
        register(CdsOutputXmlMessageBodyReader.class);
        register(EvaluateResource.class);
        register(CORSResponseFilter.class);
        register(DeflateEncoder.class);
        register(GZipEncoder.class);
        register(EncodingFilter.class);
    }
}
