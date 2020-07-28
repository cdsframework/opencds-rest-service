package org.cdsframework.rest.opencds;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
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

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.HeaderParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.cds.vmr.CdsObjectAssist;
import org.cdsframework.messageconverter.Fhir2Vmr;
import org.cdsframework.messageconverter.Vmr2Fhir;
import org.cdsframework.rest.opencds.utils.MarshalUtils;
import org.cdsframework.util.support.cds.Config;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationEvaluation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Observation;
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
import org.opencds.vmr.v1_0.schema.AdministrableSubstance;
import org.opencds.vmr.v1_0.schema.CD;
import org.opencds.vmr.v1_0.schema.CDSContext;
import org.opencds.vmr.v1_0.schema.CDSInput;
import org.opencds.vmr.v1_0.schema.CDSOutput;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson;
import org.opencds.vmr.v1_0.schema.II;
import org.opencds.vmr.v1_0.schema.ObservationResult;
import org.opencds.vmr.v1_0.schema.SubstanceAdministrationEvent;
import org.opencds.vmr.v1_0.schema.VMR;

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

        CDSInput input = this.fhir2Vmr.getCdsInputFromFhir(patient, immunizations, new ArrayList<>());
        EvaluateAtSpecifiedTime evaluateAtSpecifiedTime = this.createEvaluateAtSpecifiedTime(specifiedTime, input);

        EvaluationResponse evaluationResponse = evaluateResource.evaluateAtSpecifiedTimeBase(evaluateAtSpecifiedTime);

        Parameters out = this.buildParameters(evaluationResponse, accept);

        final String outdata = outParser.encodeResourceToString(out);

        log.debug(String.format("%s outdata=%s", METHODNAME, outdata));

        log.info(String.format("%s duration: %sms", METHODNAME, (System.nanoTime() - start) / 1000000));
        return Response.ok(outdata).type(accept).build();
    }

    protected Parameters buildParameters(EvaluationResponse evaluationResponse, String mediaType)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {

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

        evaluations.stream().map((evaluation) -> {
            ParametersParameterComponent evaluationPart = new ParametersParameterComponent();
            evaluationPart.setResource(evaluation);
            return evaluationPart;
        }).forEachOrdered((evaluationPart) -> {
            evaluationParameter.addPart(evaluationPart);
        });

        parameters.addParameter(evaluationParameter);
        return parameters;
    }

    protected <U extends DomainResource> List<BundleEntryComponent> convertToBundleComponents(List<U> resources) {
        List<BundleEntryComponent> entries = new ArrayList<>();

        resources.forEach((resource) -> {
            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(resource);
        });

        return entries;
    }

    private void fixCDSInput(CDSInput input) {
        CDSContext cdsContext = new CDSContext();
        CD cdsSystemUserPreferredLanguage = new CD();
        cdsSystemUserPreferredLanguage.setCode(Config.getDefaultLanguageCode());
        cdsSystemUserPreferredLanguage.setCodeSystem(Config.getCodeSystemOid("LANG"));
        cdsSystemUserPreferredLanguage.setDisplayName(Config.getDefaultLanguageDisplayName());
        cdsContext.setCdsSystemUserPreferredLanguage(cdsSystemUserPreferredLanguage);
        input.setCdsContext(cdsContext);

        II templateId = new II();
        templateId.setRoot(Config.getCodeSystemOid("CDS_INPUT_ROOT"));
        input.getTemplateId().add(templateId);

        VMR vmrInput = input.getVmrInput();

        II vmrInputTemplateId = new II();
        vmrInputTemplateId.setRoot(Config.getCodeSystemOid("CDS_INPUT_ROOT"));
        vmrInput.getTemplateId().add(vmrInputTemplateId);

        EvaluatedPerson patient = vmrInput.getPatient();

        II patientTemplateId = new II();
        patientTemplateId.setRoot(Config.getCodeSystemOid("EVALUATED_PERSON_ROOT"));
        patient.getTemplateId().add(patientTemplateId);

        II patientId = new II();
        patientId.setRoot(UUID.randomUUID().toString());
        patient.setId(patientId);

        EvaluatedPerson.Demographics demographics = patient.getDemographics();
        CD gender = demographics.getGender();
        if (gender == null) {
            gender = new CD();
            demographics.setGender(gender);
        }
        if (gender.getCodeSystem() == null) {
            gender.setCodeSystem(Config.getCodeSystemOid("GENDER"));
        }

        List<ObservationResult> observationResults = patient.getClinicalStatements().getObservationResults().getObservationResult();
        for (ObservationResult observationResult : observationResults) {
            if (observationResult.getId() == null) {
                II observationId = new II();
                observationId.setRoot(UUID.randomUUID().toString());
                observationResult.setId(observationId);
            }
            List<II> observationTemplateIds = observationResult.getTemplateId();
            if (observationTemplateIds.isEmpty()) {
                II observationTemplateId = new II();
                observationTemplateId.setRoot(Config.getCodeSystemOid("OBSERVATION_RESULT_ROOT"));
                observationTemplateIds.add(observationTemplateId);
            }
        }

        EvaluatedPerson.ClinicalStatements.SubstanceAdministrationEvents substanceAdministrationEventsInstance = patient.getClinicalStatements().getSubstanceAdministrationEvents();
        if (substanceAdministrationEventsInstance == null) {
            substanceAdministrationEventsInstance = new EvaluatedPerson.ClinicalStatements.SubstanceAdministrationEvents();
            patient.getClinicalStatements().setSubstanceAdministrationEvents(substanceAdministrationEventsInstance);
        }
        List<SubstanceAdministrationEvent> substanceAdministrationEvents = substanceAdministrationEventsInstance.getSubstanceAdministrationEvent();

        for (SubstanceAdministrationEvent substanceAdministrationEvent : substanceAdministrationEvents) {
            if (substanceAdministrationEvent.getId() == null) {
                II saeId = new II();
                saeId.setRoot(UUID.randomUUID().toString());
                substanceAdministrationEvent.setId(saeId);
            }
            List<II> saeTemplateIds = substanceAdministrationEvent.getTemplateId();
            if (saeTemplateIds.isEmpty()) {
                II saeTemplateId = new II();
                saeTemplateId.setRoot(Config.getCodeSystemOid("SUBSTANCE_ADMINISTRATION_EVENT_ROOT"));
                saeTemplateIds.add(saeTemplateId);
            }

            CD substanceAdministrationGeneralPurpose = substanceAdministrationEvent.getSubstanceAdministrationGeneralPurpose();
            if (substanceAdministrationGeneralPurpose == null) {
                substanceAdministrationGeneralPurpose = new CD();
                substanceAdministrationEvent.setSubstanceAdministrationGeneralPurpose(substanceAdministrationGeneralPurpose);
            }
            if (substanceAdministrationGeneralPurpose.getCode() == null || substanceAdministrationGeneralPurpose.getCode().isEmpty()) {
                substanceAdministrationGeneralPurpose.setCode(Config.getGeneralPurposeCode());
                substanceAdministrationGeneralPurpose.setCodeSystem(Config.getCodeSystemOid("GENERAL_PURPOSE"));
            }
            AdministrableSubstance substance = substanceAdministrationEvent.getSubstance();
            if (substance != null) {
                II id = substance.getId();
                if (id == null) {
                    id = new II();
                    id.setRoot(UUID.randomUUID().toString());
                    substance.setId(id);
                }
                CD substanceCode = substance.getSubstanceCode();
                substanceCode.setCodeSystem(Config.getCodeSystemOid("VACCINE"));
            }
        }
    }

    protected EvaluateAtSpecifiedTime createEvaluateAtSpecifiedTime(XMLGregorianCalendar specifiedTime, CDSInput input)
            throws DatatypeConfigurationException {
        final String METHODNAME = "createEvaluateAtSpecifiedTime ";

        fixCDSInput(input);

        String payload = CdsObjectAssist.cdsObjectToString(input, CDSInput.class);

        log.info(String.format("%s payload=%s", METHODNAME, payload));

        SemanticPayload semanticPayload = new SemanticPayload();
        semanticPayload.getBase64EncodedPayload().add(payload.getBytes());

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
        item.setData(semanticPayload); // cehck
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

        GregorianCalendar calendar = new GregorianCalendar();
        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

        InteractionIdentifier interactionId = new InteractionIdentifier();
        interactionId.setScopingEntityId("org.nyc.cir");
        interactionId.setInteractionId("123456");
        interactionId.setSubmissionTime(xmlDate);

        EvaluateAtSpecifiedTime evaluateAtSpecifiedTime = new EvaluateAtSpecifiedTime();
        evaluateAtSpecifiedTime.setEvaluationRequest(evaluationRequest);
        evaluateAtSpecifiedTime.setInteractionId(interactionId);
        evaluateAtSpecifiedTime.setSpecifiedTime(specifiedTime);

        return evaluateAtSpecifiedTime;
    }

}
