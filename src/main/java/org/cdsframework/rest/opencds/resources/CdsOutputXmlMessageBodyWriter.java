package org.cdsframework.rest.opencds.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.cdsframework.cds.util.MarshalUtils;
import org.cdsframework.util.LogUtils;
import org.opencds.vmr.v1_0.schema.CDSOutput;

/**
 *
 * @author sdn
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class CdsOutputXmlMessageBodyWriter implements MessageBodyWriter<CDSOutput>  {

    private static LogUtils logger = LogUtils.getLogger(CdsOutputXmlMessageBodyWriter.class);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        final String METHODNAME = "isWriteable ";
        logger.info(METHODNAME, "type=", type);
        logger.info(METHODNAME, "genericType=", genericType);
        logger.info(METHODNAME, "annotations=", annotations);
        logger.info(METHODNAME, "mediaType=", mediaType);
        return type == CDSOutput.class;
    }

    @Override
    public long getSize(CDSOutput t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        final String METHODNAME = "getSize ";
        logger.info(METHODNAME, "type=", type);
        logger.info(METHODNAME, "genericType=", genericType);
        logger.info(METHODNAME, "annotations=", annotations);
        logger.info(METHODNAME, "mediaType=", mediaType);
        return 0;
    }

    @Override
    public void writeTo(
            CDSOutput cdsOutput,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream out)
            throws IOException, WebApplicationException {
        final String METHODNAME = "writeTo ";
        logger.info(METHODNAME, "type=", type);
        logger.info(METHODNAME, "genericType=", genericType);
        logger.info(METHODNAME, "annotations=", annotations);
        logger.info(METHODNAME, "mediaType=", mediaType);
        logger.info(METHODNAME, "httpHeaders=", httpHeaders);
        MarshalUtils.marshal(cdsOutput, out);
    }
    
}
