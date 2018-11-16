/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds.resources;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.cdsframework.cds.util.MarshalUtils;
import org.cdsframework.util.LogUtils;
import org.opencds.vmr.v1_0.schema.CDSInput;

/**
 *
 * @author sdn
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class CdsInputXmlMessageBodyReader implements MessageBodyReader<CDSInput> {

//    private static LogUtils logger = LogUtils.getLogger(CdsInputXmlMessageBodyReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
//        final String METHODNAME = "isReadable ";
//        logger.info(METHODNAME, "type=", type);
//        logger.info(METHODNAME, "genericType=", genericType);
//        logger.info(METHODNAME, "annotations=", annotations);
//        logger.info(METHODNAME, "mediaType=", mediaType);
        return true;
    }

    @Override
    public CDSInput readFrom(
            Class<CDSInput> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream)
            throws IOException, WebApplicationException {
//        final String METHODNAME = "readFrom ";
//        logger.info(METHODNAME, "type=", type);
//        logger.info(METHODNAME, "genericType=", genericType);
//        logger.info(METHODNAME, "annotations=", annotations);
//        logger.info(METHODNAME, "mediaType=", mediaType);

        return MarshalUtils.unmarshal(entityStream, CDSInput.class);

    }

}
