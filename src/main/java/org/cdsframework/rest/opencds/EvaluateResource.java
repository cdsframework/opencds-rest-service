/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.cdsframework.cds.service.OpenCdsService;
import org.cdsframework.cds.vmr.CdsObjectAssist;
import org.cdsframework.enumeration.DeploymentEnvironment;
import org.cdsframework.rs.support.CoreRsConstants;
import org.cdsframework.util.LogUtils;
import org.opencds.vmr.v1_0.schema.CDSInput;
import org.opencds.vmr.v1_0.schema.CDSOutput;

/**
 * REST Web Service
 *
 * @author sdn
 */
@Path(CoreRsConstants.GENERAL_RS_ROOT)
public class EvaluateResource {

    private static LogUtils logger = LogUtils.getLogger(EvaluateResource.class);

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of EvaluateResource
     */
    public EvaluateResource() {
    }

    /**
     * Retrieves representation of an instance of
     * org.cdsframework.rest.opencds.EvaluateResource
     *
     * @param cdsInput
     * @param scopingEntityId
     * @param businessId
     * @param version
     * @param executionDateString
     * @param environment
     * @param header
     * @param response
     * @return
     * @throws java.text.ParseException
     * @throws java.io.UnsupportedEncodingException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("evaluate/{environment}/{scopingEntityId}/{businessId}/{version}/{executionDate}")
    public Response evaluate(CDSInput cdsInput,
            @PathParam("scopingEntityId") final String scopingEntityId,
            @PathParam("businessId") final String businessId,
            @PathParam("version") final String version,
            @PathParam("executionDate") final String executionDateString,
            @PathParam("environment") final String environment,
            @Context HttpHeaders header,
            @Context HttpServletResponse response) throws ParseException, UnsupportedEncodingException, IOException {

        final String METHODNAME = "evaluate ";

        CDSOutput result;
        byte[] payload = CdsObjectAssist.cdsObjectToByteArray(cdsInput, CDSInput.class);

//        logger.info(METHODNAME, "payload=", new String(payload));
        logger.info(METHODNAME, "scopingEntityId=", scopingEntityId);
        logger.info(METHODNAME, "businessId=", businessId);
        logger.info(METHODNAME, "version=", version);
        logger.info(METHODNAME, "executionDateString=", executionDateString);
        logger.info(METHODNAME, "environment=", environment);

        DeploymentEnvironment deploymentEnvironment = DeploymentEnvironment.valueOf(environment.toUpperCase());

        String endPoint = System.getProperty("cds.endpoint." + deploymentEnvironment.toString().toLowerCase());

        logger.info(METHODNAME, "endPoint=", endPoint);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date executionDate = format.parse(executionDateString);

        OpenCdsService service = OpenCdsService.getOpenCdsService(endPoint);

        try {
            byte[] evaluation = service.evaluate(payload, scopingEntityId, businessId, version, executionDate);

//        logger.info(METHODNAME, "evaluation=", new String(evaluation));
            result = CdsObjectAssist.cdsObjectFromByteArray(evaluation, CDSOutput.class);
            Response.ResponseBuilder responseBuilder = Response.ok(result);
            responseBuilder.header("ERRORP", "foo");
            return responseBuilder.build();

        } catch (Exception e) {
            System.out.println(METHODNAME + "e.getMessage()=" + e.getMessage());
            
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(e.getMessage()).type(MediaType.TEXT_PLAIN);
            return responseBuilder.build();
        }
    }
}
