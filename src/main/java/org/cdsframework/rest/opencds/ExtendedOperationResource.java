package org.cdsframework.rest.opencds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
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
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cdsframework.cds.vmr.CdsObjectAssist;
import org.cdsframework.messageconverter.Fhir2Vmr;
import org.cdsframework.messageconverter.Vmr2Fhir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cdsframework.rest.opencds.utils.MarshalUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationEvaluation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.omg.dss.DSSRuntimeExceptionFault;
import org.omg.dss.EvaluationExceptionFault;
import org.omg.dss.InvalidDriDataFormatExceptionFault;
import org.omg.dss.InvalidTimeZoneOffsetExceptionFault;
import org.omg.dss.RequiredDataNotProvidedExceptionFault;
import org.omg.dss.UnrecognizedLanguageExceptionFault;
import org.omg.dss.UnrecognizedScopedEntityExceptionFault;
import org.omg.dss.UnsupportedLanguageExceptionFault;
import org.omg.dss.evaluation.requestresponse.EvaluationResponse;
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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    @Path("Patient/{patientId}/$evaluate")
    public Response evaluate(Patient patient, List<Immunization> immunizations, List<Immunization> observations,
            @Context final HttpHeaders header, @Context final HttpServletResponse response)
            throws UnsupportedEncodingException, ParseException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {
        try {
            CDSInput input = this.fhir2Vmr.getCdsInputFromFhir(patient, immunizations, observations);
            String packet = CdsObjectAssist.cdsObjectToString(input, CDSInput.class);

            Response evaluate = evaluateResource.evaluate(packet, header, response);

            return this.buildResponse(evaluate);
        } finally {
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
    @Path("Patient/{patientId}/$evaluateAtSpecifiedTime")
    public Response evaluateAtSpecifiedTime(Patient patient, List<Immunization> immunizations,
            List<Immunization> observations, @Context final HttpHeaders header,
            @Context final HttpServletResponse response)
            throws ParseException, UnsupportedEncodingException, IOException, InvalidDriDataFormatExceptionFault,
            UnrecognizedLanguageExceptionFault, RequiredDataNotProvidedExceptionFault,
            UnsupportedLanguageExceptionFault, UnrecognizedScopedEntityExceptionFault, EvaluationExceptionFault,
            InvalidTimeZoneOffsetExceptionFault, DSSRuntimeExceptionFault, JAXBException, TransformerException {
        try {
            CDSInput input = this.fhir2Vmr.getCdsInputFromFhir(patient, immunizations, observations);
            String packet = CdsObjectAssist.cdsObjectToString(input, CDSInput.class);

            Response evaluateAtSpecifiedTimeResponse = evaluateResource.evaluateAtSpecifiedTime(packet, header,
                    response);
            return this.buildResponse(evaluateAtSpecifiedTimeResponse);
        } finally {
        }
    }

    protected Response buildResponse(Response response)
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
        List<ImmunizationRecommendation> recommendations = this.vmr2Fhir.getRecommendations(output);

        Bundle bundle = new Bundle();
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();

        entries.addAll(this.convertToBundleComponents(evaluations));
        entries.addAll(this.convertToBundleComponents(recommendations));

        bundle.setEntry(entries);

        return Response.ok(bundle).type(MediaType.APPLICATION_XML).build();
    }

    protected <U extends DomainResource> List<BundleEntryComponent> convertToBundleComponents(List<U> resources) {
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();

        for (DomainResource resource : resources) {
            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(resource);
        }

        return entries;
    }
}