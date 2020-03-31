package org.cdsframework.rest.opencds;

import org.cdsframework.rest.opencds.utils.MarshalUtils;
import org.cdsframework.rest.opencds.pojos.UpdateResponse;
import org.cdsframework.rest.opencds.pojos.PreEvaluateHookType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.rest.opencds.pojos.UpdateResponseResult;
import org.cdsframework.rest.opencds.utils.ConfigUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.omg.dss.DSSRuntimeExceptionFault;
import org.omg.dss.EvaluationExceptionFault;
import org.omg.dss.InvalidDriDataFormatExceptionFault;
import org.omg.dss.InvalidTimeZoneOffsetExceptionFault;
import org.omg.dss.RequiredDataNotProvidedExceptionFault;
import org.omg.dss.UnrecognizedLanguageExceptionFault;
import org.omg.dss.UnrecognizedScopedEntityExceptionFault;
import org.omg.dss.UnsupportedLanguageExceptionFault;
import org.omg.dss.evaluation.Evaluate;
import org.omg.dss.evaluation.EvaluateAtSpecifiedTime;
import org.omg.dss.evaluation.EvaluateAtSpecifiedTimeResponse;
import org.omg.dss.evaluation.EvaluateResponse;
import org.omg.dss.evaluation.requestresponse.EvaluationRequest;
import org.omg.dss.evaluation.requestresponse.EvaluationResponse;
import org.opencds.config.api.ConfigurationService;
import org.opencds.dss.evaluate.EvaluationService;

/**
 * REST Web Service
 *
 * @author sdn
 */
@Path("resources")
public class EvaluateResource {

    private static final Log log = LogFactory.getLog(EvaluateResource.class);

    private static Builder preEvaluateInvocationBuilder;
    private static Builder preEvaluateFailureInvocationBuilder;
    private static PreEvaluateHookType preEvaluateHookType = null;
    private static int DEFAULT_CLIENT_TIMEOUT = 5000;

    private final EvaluationService evaluationService;
    private final ConfigurationService configurationService;

    @Context
    private ServletContext context;

    /**
     * Creates a new instance of EvaluateResource
     *
     * @param evaluationService
     * @param configurationService
     */
    public EvaluateResource(EvaluationService evaluationService, ConfigurationService configurationService) {
        this.evaluationService = evaluationService;
        this.configurationService = configurationService;
    }

    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @Path("tz")
    public String tz() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Retrieves representation of an instance of
     * org.cdsframework.rest.opencds.EvaluateResource
     *
     * @param evaluateString
     * @param header
     * @param response
     * @return
     * @throws ParseException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws InvalidDriDataFormatExceptionFault
     * @throws UnrecognizedLanguageExceptionFault
     * @throws RequiredDataNotProvidedExceptionFault
     * @throws UnsupportedLanguageExceptionFault
     * @throws UnrecognizedScopedEntityExceptionFault
     * @throws EvaluationExceptionFault
     * @throws InvalidTimeZoneOffsetExceptionFault
     * @throws DSSRuntimeExceptionFault
     * @throws JAXBException
     * @throws TransformerException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Path("evaluate")
    public Response evaluate(String evaluateString, @Context HttpHeaders header, @Context HttpServletResponse response)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {

        final String METHODNAME = "evaluate ";

        ObjectMapper mapper = new ObjectMapper();

        Evaluate evaluate;
        MediaType mediaType = header.getMediaType();

        log.debug(METHODNAME + "mediaType=" + mediaType);
        log.debug(METHODNAME + "mediaType.toString()=" + mediaType.toString());
        log.debug(METHODNAME + "MediaType.APPLICATION_JSON=" + MediaType.APPLICATION_JSON);
        log.debug(METHODNAME + "mediaType.toString().equals(MediaType.APPLICATION_JSON)="
                + mediaType.toString().equals(MediaType.APPLICATION_JSON));
        log.debug(METHODNAME + "mediaType.toString().equals(MediaType.APPLICATION_XML)="
                + mediaType.toString().equals(MediaType.APPLICATION_XML));

        switch (mediaType.toString()) {
            case MediaType.APPLICATION_JSON:
                evaluate = mapper.readValue(evaluateString, Evaluate.class);
                break;
            case MediaType.APPLICATION_XML:
                evaluate = MarshalUtils.unmarshal(new ByteArrayInputStream(evaluateString.getBytes()), Evaluate.class);
                break;
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }

        try {
            preEvaluate(evaluate);
            Response.ResponseBuilder responseBuilder;
            EvaluateResponse evaluateResponse = evaluationService.evaluate(evaluate);
            EvaluationResponse evaluationResponse = evaluateResponse.getEvaluationResponse();

            List<MediaType> acceptableMediaTypes = header.getAcceptableMediaTypes();
            log.debug(METHODNAME + "acceptableMediaTypes=" + acceptableMediaTypes);

            if (acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
                String data = mapper.writeValueAsString(evaluationResponse);
                responseBuilder = Response.ok(data).type(MediaType.APPLICATION_JSON);
            } else {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                MarshalUtils.marshal(evaluationResponse, stream);
                stream.toByteArray();
                responseBuilder = Response.ok(new String(stream.toByteArray())).type(MediaType.APPLICATION_XML);
            }
            return responseBuilder.build();
        } finally {

        }
    }

    /**
     * Retrieves representation of an instance of
     * org.cdsframework.rest.opencds.EvaluateResource
     *
     * @param evaluateAtSpecifiedTimeString
     * @param header
     * @param response
     * @return
     * @throws java.text.ParseException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.omg.dss.InvalidDriDataFormatExceptionFault
     * @throws org.omg.dss.UnrecognizedLanguageExceptionFault
     * @throws org.omg.dss.RequiredDataNotProvidedExceptionFault
     * @throws org.omg.dss.UnsupportedLanguageExceptionFault
     * @throws org.omg.dss.UnrecognizedScopedEntityExceptionFault
     * @throws org.omg.dss.EvaluationExceptionFault
     * @throws org.omg.dss.InvalidTimeZoneOffsetExceptionFault
     * @throws org.omg.dss.DSSRuntimeExceptionFault
     * @throws javax.xml.bind.JAXBException
     * @throws javax.xml.transform.TransformerException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @Path("evaluateAtSpecifiedTime")
    public Response evaluateAtSpecifiedTime(String evaluateAtSpecifiedTimeString, @Context HttpHeaders header,
            @Context HttpServletResponse response)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {

        final String METHODNAME = "evaluateAtSpecifiedTime ";

        ObjectMapper mapper = new ObjectMapper();

        EvaluateAtSpecifiedTime evaluateAtSpecifiedTime;
        MediaType mediaType = header.getMediaType();

        log.debug(METHODNAME + "mediaType=" + mediaType);
        log.debug(METHODNAME + "mediaType.toString()=" + mediaType.toString());
        log.debug(METHODNAME + "MediaType.APPLICATION_JSON=" + MediaType.APPLICATION_JSON);
        log.debug(METHODNAME + "mediaType.toString().equals(MediaType.APPLICATION_JSON)="
                + mediaType.toString().equals(MediaType.APPLICATION_JSON));
        log.debug(METHODNAME + "mediaType.toString().equals(MediaType.APPLICATION_XML)="
                + mediaType.toString().equals(MediaType.APPLICATION_XML));

        switch (mediaType.toString()) {
            case MediaType.APPLICATION_JSON:
                evaluateAtSpecifiedTime = mapper.readValue(evaluateAtSpecifiedTimeString,
                        EvaluateAtSpecifiedTime.class);
                break;
            case MediaType.APPLICATION_XML:
                evaluateAtSpecifiedTime = MarshalUtils.unmarshal(
                        new ByteArrayInputStream(evaluateAtSpecifiedTimeString.getBytes()),
                        EvaluateAtSpecifiedTime.class);
                break;
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }

        try {

            preEvaluate(evaluateAtSpecifiedTime);
            Response.ResponseBuilder responseBuilder;
            EvaluateAtSpecifiedTimeResponse evaluateAtSpecifiedTimeResponse = evaluationService
                    .evaluateAtSpecifiedTime(evaluateAtSpecifiedTime);
            EvaluationResponse evaluationResponse = evaluateAtSpecifiedTimeResponse.getEvaluationResponse();

            List<MediaType> acceptableMediaTypes = header.getAcceptableMediaTypes();
            log.debug(METHODNAME + "acceptableMediaTypes=" + acceptableMediaTypes);

            if (acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
                String data = mapper.writeValueAsString(evaluationResponse);
                responseBuilder = Response.ok(data).type(MediaType.APPLICATION_JSON);
            } else {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                MarshalUtils.marshal(evaluationResponse, stream);
                stream.toByteArray();
                responseBuilder = Response.ok(new String(stream.toByteArray())).type(MediaType.APPLICATION_XML);
            }
            return responseBuilder.build();
        } finally {

        }
    }

    /**
     * branch for evaluateAtSpecifiedTime/preEvaluate logic
     *
     * @param evaluateAtSpecifiedTime
     */
    private void preEvaluate(EvaluateAtSpecifiedTime evaluateAtSpecifiedTime) {
        final String METHODNAME = "preEvaluate ";
        if (evaluateAtSpecifiedTime == null || evaluateAtSpecifiedTime.getEvaluationRequest() == null) {
            log.debug(METHODNAME + "an evaluateAtSpecifiedTime element is null!");
            return;
        }
        preEvaluate(evaluateAtSpecifiedTime.getEvaluationRequest());
    }

    /**
     * branch for evaluate/preEvaluate logic
     *
     * @param evaluate
     */
    private void preEvaluate(Evaluate evaluate) {
        final String METHODNAME = "preEvaluate ";
        if (evaluate == null || evaluate.getEvaluationRequest() == null) {
            log.debug(METHODNAME + "an evaluate element is null!");
            return;
        }
        preEvaluate(evaluate.getEvaluationRequest());
    }

    /**
     * main preEvaluate webhook logic
     *
     * @param evaluationRequest
     */
    private void preEvaluate(EvaluationRequest evaluationRequest) {
        final String METHODNAME = "preEvaluate ";
        if (evaluationRequest == null) {
            log.debug(METHODNAME + "evaluationRequest is null!");
            return;
        }

        if (evaluationRequest.getKmEvaluationRequest() == null
                || evaluationRequest.getKmEvaluationRequest().isEmpty()) {
            log.debug(METHODNAME + "evaluationRequest.getKmEvaluationRequest() is null or empty!");
            return;
        }

        long start = System.nanoTime();
        try {
            Builder preEvaluateBuilder = getPreEvaluateInvocationBuilder(context);
            if (preEvaluateBuilder == null) {
                log.debug(METHODNAME + "builder is null - skipping preEvaluate");
                return;
            }
            Response response;
            PreEvaluateHookType hookType = getPreEvaluateHookType();

            switch (hookType) {
                case ENTITY_IDENTIFIER:
                    response = preEvaluateBuilder.put(
                            Entity.entity(evaluationRequest.getKmEvaluationRequest(), MediaType.APPLICATION_JSON_TYPE));
                    UpdateResponse updateResponse = response.readEntity(UpdateResponse.class);
                    UpdateResponseResult result = ConfigUtils.update(updateResponse, configurationService);
                    if (result.getCdm() != null || (result.getKms() != null && result.getKms().size() > 0)) {
                        Builder failureBuilder = getPreEvaluateFailureInvocationBuilder(context);
                        response = failureBuilder.put(Entity.entity(result, MediaType.APPLICATION_JSON_TYPE));
                    }
                    break;
                case EVALUATION_REQUEST:
                    response = preEvaluateBuilder
                            .put(Entity.entity(evaluationRequest, MediaType.APPLICATION_JSON_TYPE));
                    EvaluationRequest evaluationRequestResponse = response.readEntity(EvaluationRequest.class);
                    evaluationRequest.getKmEvaluationRequest().clear();
                    evaluationRequest.getKmEvaluationRequest()
                            .addAll(evaluationRequestResponse.getKmEvaluationRequest());
                    evaluationRequest.getDataRequirementItemData().clear();
                    evaluationRequest.getDataRequirementItemData()
                            .addAll(evaluationRequestResponse.getDataRequirementItemData());
                    break;
                default:
                    throw new IllegalStateException("Unhandled hook type: " + preEvaluateHookType.toString());
            }

            log.debug(METHODNAME + "response.getStatus(): " + response.getStatus());

        } finally {
            log.info(METHODNAME + "duration: " + ((System.nanoTime() - start) / 1000000) + "ms");
        }
    }

    private static Builder getPreEvaluateInvocationBuilder(ServletContext context) {
        final String METHODNAME = "getPreEvaluateInvocationBuilder ";

        if (preEvaluateInvocationBuilder == null) {

            String preEvaluateHookUri = System.getProperty("preEvaluateHookUri");
            if (preEvaluateHookUri == null || preEvaluateHookUri.trim().isEmpty()) {
                log.debug(METHODNAME + "preEvaluateHookUri is null!");
                return preEvaluateInvocationBuilder;
            } else {
                log.debug(METHODNAME + "preEvaluateHookUri: " + preEvaluateHookUri);
            }

            String preEvaluateUuid = System.getProperty("preEvaluateUuid");
            if (preEvaluateUuid == null || preEvaluateUuid.trim().isEmpty()) {
                log.error(METHODNAME + "preEvaluateUuid is null!");
                return preEvaluateInvocationBuilder;
            } else {
                log.debug(METHODNAME + "preEvaluateUuid: " + preEvaluateUuid);
            }

            log.info(METHODNAME + "initializing invocation builder");

            ClientConfig config = new ClientConfig();
            config.register(JacksonJsonProvider.class);
            Client client = ClientBuilder.newClient(config);
            WebTarget webTarget = client.target(preEvaluateHookUri);
            preEvaluateInvocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            // set the instanceid
            preEvaluateInvocationBuilder.header("instanceid", preEvaluateUuid);

            // set the environment
            boolean isTest = context.getContextPath().toLowerCase().contains("test");
            log.debug(METHODNAME + "isTest: " + isTest);
            preEvaluateInvocationBuilder.header("environment", isTest ? "TEST" : "PRODUCTION");

        }
        return preEvaluateInvocationBuilder;
    }

    private static Builder getPreEvaluateFailureInvocationBuilder(ServletContext context) {
        final String METHODNAME = "getPreEvaluateFailureInvocationBuilder ";

        if (preEvaluateFailureInvocationBuilder == null) {

            String preEvaluateHookUri = System.getProperty("preEvaluateHookUri");
            if (preEvaluateHookUri == null || preEvaluateHookUri.trim().isEmpty()) {
                log.debug(METHODNAME + "preEvaluateHookUri is null!");
                return preEvaluateFailureInvocationBuilder;
            } else {
                log.debug(METHODNAME + "preEvaluateHookUri: " + preEvaluateHookUri);
            }

            String preEvaluateUuid = System.getProperty("preEvaluateUuid");
            if (preEvaluateUuid == null || preEvaluateUuid.trim().isEmpty()) {
                log.error(METHODNAME + "preEvaluateUuid is null!");
                return preEvaluateFailureInvocationBuilder;
            } else {
                log.debug(METHODNAME + "preEvaluateUuid: " + preEvaluateUuid);
            }

            String preEvaluateTimeoutString = System.getProperty("preEvaluateTimeout");
            int preEvaluateTimeout;
            if (preEvaluateTimeoutString == null || preEvaluateTimeoutString.trim().isEmpty()) {
                log.debug(METHODNAME + "preEvaluateTimeoutString is null!");
                preEvaluateTimeout = DEFAULT_CLIENT_TIMEOUT;
            } else {
                try {
                    preEvaluateTimeout = Integer.parseInt(preEvaluateTimeoutString);
                } catch (Exception e) {
                    preEvaluateTimeout = DEFAULT_CLIENT_TIMEOUT;
                }
            }
            log.debug(METHODNAME + "preEvaluateTimeout: " + preEvaluateTimeout);

            log.info(METHODNAME + "initializing invocation builder");

            ClientConfig config = new ClientConfig();
            config.register(JacksonJsonProvider.class);
            Client client = ClientBuilder.newClient(config);
            client.property(ClientProperties.CONNECT_TIMEOUT, preEvaluateTimeout);
            client.property(ClientProperties.READ_TIMEOUT, preEvaluateTimeout);

            WebTarget webTarget = client.target(preEvaluateHookUri);
            preEvaluateFailureInvocationBuilder = webTarget.path("failed").request(MediaType.APPLICATION_JSON);

            // set the instanceid
            preEvaluateFailureInvocationBuilder.header("instanceid", preEvaluateUuid);

            // set the environment
            boolean isTest = context.getContextPath().toLowerCase().contains("test");
            log.debug(METHODNAME + "isTest: " + isTest);
            preEvaluateFailureInvocationBuilder.header("environment", isTest ? "TEST" : "PRODUCTION");

        }
        return preEvaluateFailureInvocationBuilder;
    }

    private static PreEvaluateHookType getPreEvaluateHookType() {
        final String METHODNAME = "getPreEvaluateHookType ";

        String preEvaluateHookTypeProperty = System.getProperty("preEvaluateHookType");
        if (preEvaluateHookTypeProperty == null || preEvaluateHookTypeProperty.trim().isEmpty()) {
            preEvaluateHookTypeProperty = "ENTITY_IDENTIFIER";
        }
        preEvaluateHookType = PreEvaluateHookType.valueOf(preEvaluateHookTypeProperty);
        log.debug(METHODNAME + "preEvaluateHookType: " + preEvaluateHookType);
        return preEvaluateHookType;
    }

}
