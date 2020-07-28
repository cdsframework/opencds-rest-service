package org.cdsframework.rest.opencds;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;

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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import javax.ws.rs.HeaderParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.cds.vmr.CdsObjectAssist;
import org.cdsframework.messageconverter.Fhir2Vmr;
import org.cdsframework.messageconverter.Vmr2Fhir;
import org.cdsframework.rest.opencds.utils.MarshalUtils;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationEvaluation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.omg.dss.DSSRuntimeExceptionFault;
import org.omg.dss.EvaluationExceptionFault;
import org.omg.dss.InvalidDriDataFormatExceptionFault;
import org.omg.dss.InvalidTimeZoneOffsetExceptionFault;
import org.omg.dss.RequiredDataNotProvidedExceptionFault;
import org.omg.dss.UnrecognizedLanguageExceptionFault;
import org.omg.dss.UnrecognizedScopedEntityExceptionFault;
import org.omg.dss.UnsupportedLanguageExceptionFault;
import org.omg.dss.common.EntityIdentifier;
import org.omg.dss.common.InteractionIdentifier;
import org.omg.dss.common.ItemIdentifier;
import org.omg.dss.common.SemanticPayload;
import org.omg.dss.evaluation.EvaluateAtSpecifiedTime;
import org.omg.dss.evaluation.requestresponse.DataRequirementItemData;
import org.omg.dss.evaluation.requestresponse.EvaluationRequest;
import org.omg.dss.evaluation.requestresponse.EvaluationResponse;
import org.omg.dss.evaluation.requestresponse.KMEvaluationRequest;
import org.opencds.vmr.v1_0.schema.CDSInput;
import org.opencds.vmr.v1_0.schema.CDSOutput;

public class ExtendedOperationResource {

    private static final Log log = LogFactory.getLog(ExtendedOperationResource.class);

    private final EvaluateResource evaluateResource;

    protected Fhir2Vmr fhir2Vmr = new Fhir2Vmr();
    protected Vmr2Fhir vmr2Fhir = new Vmr2Fhir();

    /**
     * Creates a new instance of EvaluateResource
     *
     * @param evaluateResource
     */
    public ExtendedOperationResource(final EvaluateResource evaluateResource) {
        this.evaluateResource = evaluateResource;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("$immds-forecast")
    public Response getImmdsForecast(
            @HeaderParam("content-type") final String contentType,
            @HeaderParam("accept") final String accept,
            final String fhirData,
            @Context final HttpHeaders header,
            @Context final HttpServletResponse response)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException, DatatypeConfigurationException {
        final String METHODNAME = "getImmdsForecast ";
        final long start = System.nanoTime();

        log.info(String.format("%s fhirData=%s", METHODNAME, fhirData));

        final FhirContext ctx = FhirContext.forR4();
        IParser inParser;
        if (contentType == null) {
            throw new IllegalArgumentException("content-type header is null!");
        } else if (MediaType.APPLICATION_JSON.equalsIgnoreCase(contentType)) {
            inParser = ctx.newJsonParser();
        } else if (MediaType.APPLICATION_XML.equalsIgnoreCase(contentType)) {
            inParser = ctx.newXmlParser();
        } else {
            throw new IllegalArgumentException("content-type header illegal value: " + contentType);
        }
        
        IParser outParser;
        if (accept == null) {
            throw new IllegalArgumentException("accept header is null!");
        } else if (MediaType.APPLICATION_JSON.equalsIgnoreCase(accept)) {
            outParser = ctx.newJsonParser();
        } else if (MediaType.APPLICATION_XML.equalsIgnoreCase(accept)) {
            outParser = ctx.newXmlParser();
        } else {
            throw new IllegalArgumentException("accept header illegal value: " + accept);
        }

        Parameters in = inParser.parseResource(Parameters.class, fhirData);

        DateType assessmentDateType = (DateType) in.getParameter("assessmentDate");
        Date assessmentDate = assessmentDateType.getValue();
        if (assessmentDate == null) {
            assessmentDate = new Date();
        }
        XMLGregorianCalendar specifiedTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(assessmentDate.toInstant().toString());

        Patient patient = null;
        List<Immunization> immunizations = new ArrayList<>();

        List<ParametersParameterComponent> parameters = in.getParameter();
        for (ParametersParameterComponent parameter : parameters) {
            Resource resource = parameter.getResource();
            if (resource instanceof Patient) {
                if (patient == null) {
                    patient = (Patient) resource;
                } else {
                    throw new IllegalArgumentException("multiple patient resources submitted!");
                }
            } else if (resource instanceof Immunization) {
                immunizations.add((Immunization) resource);
            }
        }
        log.info(String.format("%s specifiedTime=%s", METHODNAME, specifiedTime));
        log.info(String.format("%s patient=%s", METHODNAME, patient));
        log.info(String.format("%s immunizations=%s", METHODNAME, immunizations));

        CDSInput input = this.fhir2Vmr.getCdsInputFromFhir(patient, immunizations);
        String packet = this.createEvaluateAtSpecifiedTime(specifiedTime, input);

        Response evaluateAtSpecifiedTimeResponse
                = evaluateResource.evaluateAtSpecifiedTime(packet, header, response);

        Parameters out = this.buildParameters(evaluateAtSpecifiedTimeResponse, accept);

        final String outdata = outParser.encodeResourceToString(out);

        log.debug(String.format("%s outdata=%s", METHODNAME, outdata));

        log.info(String.format("%s duration: %sms", METHODNAME, (System.nanoTime() - start) / 1000000));
        return Response.ok(outdata).type(accept).build();
    }

    protected Parameters buildParameters(Response response, String mediaType)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {
        final ObjectMapper mapper = new ObjectMapper();

        EvaluationResponse evaluationResponse = mapper.readValue(response.readEntity(String.class),
                EvaluationResponse.class);
        String data = MarshalUtils.getCdsOutputStringFromEvaluationResponse(evaluationResponse);

        CDSOutput output = CdsObjectAssist.cdsObjectFromByteArray(data.getBytes(), CDSOutput.class);

        List<ImmunizationEvaluation> evaluations = this.vmr2Fhir.getEvaluations(output);
        ImmunizationRecommendation recommendation = this.vmr2Fhir.getRecommendation(output);

        Parameters parameters = new Parameters();

        ParametersParameterComponent recommendationParameter = new ParametersParameterComponent();
        recommendationParameter.setName("recommendation");
        recommendationParameter.setResource(recommendation);
        parameters.addParameter(recommendationParameter);

        ParametersParameterComponent evaluationParameter = new ParametersParameterComponent();
        evaluationParameter.setName("evaluation");

        for (ImmunizationEvaluation evaluation : evaluations) {
            ParametersParameterComponent evaluationPart = new ParametersParameterComponent();
            evaluationPart.setResource(evaluation);

            evaluationParameter.addPart(evaluationPart);
        }

        parameters.addParameter(evaluationParameter);
        return parameters;
    }

    protected <U extends DomainResource> List<BundleEntryComponent> convertToBundleComponents(List<U> resources) {
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();

        for (DomainResource resource : resources) {
            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(resource);
        }

        return entries;
    }

    protected String createEvaluateAtSpecifiedTime(XMLGregorianCalendar specifiedTime, CDSInput input) throws DatatypeConfigurationException {
        String payload = CdsObjectAssist.cdsObjectToString(input, CDSInput.class);
        String encodedPacket = Base64.getEncoder().encodeToString(payload.getBytes());

        SemanticPayload semanticPayload = new SemanticPayload();
        semanticPayload.getBase64EncodedPayload().add(encodedPacket.getBytes());

        EntityIdentifier informationModelSSId = new EntityIdentifier();
        informationModelSSId.setScopingEntityId("org.opencds.vmr");
        informationModelSSId.setBusinessId("VMR");
        informationModelSSId.setVersion("1.0");

        semanticPayload.setInformationModelSSId(informationModelSSId);

        EntityIdentifier containingEntityId = new EntityIdentifier();
        containingEntityId.setScopingEntityId("org.nyc.cir");
        containingEntityId.setBusinessId("ICEData");
        containingEntityId.setVersion("1.0.0");

        ItemIdentifier driId = new ItemIdentifier();
        driId.setContainingEntityId(containingEntityId);
        driId.setItemId("cdsPayload");

        DataRequirementItemData item = new DataRequirementItemData();
        item.setData(semanticPayload);
        item.setDriId(driId);

        EntityIdentifier kmId = new EntityIdentifier();
        kmId.setScopingEntityId("org.nyc.cir");
        kmId.setBusinessId("ICE");
        kmId.setVersion("1.0.0");

        KMEvaluationRequest kmEvaluationRequest = new KMEvaluationRequest();
        kmEvaluationRequest.setKmId(kmId);

        EvaluationRequest evaluationRequest = new EvaluationRequest();
        evaluationRequest.getDataRequirementItemData().add(item);
        evaluationRequest.getKmEvaluationRequest().add(kmEvaluationRequest);
        evaluationRequest.setClientLanguage("en");
        evaluationRequest.setClientTimeZoneOffset("+0000");

        InteractionIdentifier interactionId = new InteractionIdentifier();
        interactionId.setScopingEntityId("org.nyc.cir");
        interactionId.setInteractionId("123456");

        try {
            GregorianCalendar calendar = new GregorianCalendar();
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            interactionId.setSubmissionTime(xmlDate);
        } catch (DatatypeConfigurationException exception) {
            log.debug("Cannot create date");
        }

        EvaluateAtSpecifiedTime evaluateAtSpecifiedTime = new EvaluateAtSpecifiedTime();
        evaluateAtSpecifiedTime.setEvaluationRequest(evaluationRequest);
        evaluateAtSpecifiedTime.setInteractionId(interactionId);
        evaluateAtSpecifiedTime.setSpecifiedTime(specifiedTime);

        return CdsObjectAssist.cdsObjectToString(evaluateAtSpecifiedTime, EvaluateAtSpecifiedTime.class);
    }

}
