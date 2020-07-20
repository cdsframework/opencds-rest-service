package org.cdsframework.rest.opencds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.dss.DSSRuntimeExceptionFault;
import org.omg.dss.EvaluationExceptionFault;
import org.omg.dss.InvalidDriDataFormatExceptionFault;
import org.omg.dss.InvalidTimeZoneOffsetExceptionFault;
import org.omg.dss.RequiredDataNotProvidedExceptionFault;
import org.omg.dss.UnrecognizedLanguageExceptionFault;
import org.omg.dss.UnrecognizedScopedEntityExceptionFault;
import org.omg.dss.UnsupportedLanguageExceptionFault;
import org.omg.dss.evaluation.requestresponse.EvaluationResponse;

public class ExtendedOperationResource {

    private static final Log log = LogFactory.getLog(ExtendedOperationResource.class);

    private final EvaluateResource evaluateResource;

    /**
     * Creates a new instance of EvaluateResource
     *
     * @param evaluateResource
     */
    public ExtendedOperationResource(final EvaluateResource evaluateResource) {
        this.evaluateResource = evaluateResource;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Path("Patient/{patientId}/$evaluate")
    public Response evaluate(final String fhirPatient, @Context final HttpHeaders header,
            @Context final HttpServletResponse response)
            throws UnsupportedEncodingException, ParseException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {

        final String METHODNAME = "evaluate ";
        final ObjectMapper mapper = new ObjectMapper();
        try {
            Response evaluate = evaluateResource.evaluate(fhirPatient, header, response);
            final EvaluationResponse evaluationResponse = evaluate.readEntity(EvaluationResponse.class);
            final String data = mapper.writeValueAsString(evaluationResponse);
            log.info(METHODNAME + "data=" + data);
            return Response.ok(data).type(MediaType.APPLICATION_JSON_TYPE).build();
        } finally {
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Path("Patient/{patientId}/$evaluateAtSpecifiedTime")
    public Response evaluateAtSpecifiedTime(final String fhirPatient, @Context final HttpHeaders header,
            @Context final HttpServletResponse response)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {

        final String METHODNAME = "evaluateAtSpecifiedTime ";

        final ObjectMapper mapper = new ObjectMapper();
        try {
            Response evaluateAtSpecifiedTimeResponse = evaluateResource.evaluateAtSpecifiedTime(fhirPatient, header,
                    response);
            final EvaluationResponse evaluationResponse = evaluateAtSpecifiedTimeResponse
                    .readEntity(EvaluationResponse.class);
            final String data = mapper.writeValueAsString(evaluationResponse);
            log.info(METHODNAME + "data=" + data);
            return Response.ok(data).type(MediaType.APPLICATION_JSON_TYPE).build();
        } finally {
        }
    }
}