/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

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
import javax.ws.rs.core.UriInfo;
import org.cdsframework.cds.service.OpenCdsService;
import org.cdsframework.cds.vmr.CdsObjectAssist;
import org.cdsframework.util.LogUtils;
import org.cdsframework.util.support.cds.Config;
import org.opencds.vmr.v1_0.schema.CDSInput;
import org.opencds.vmr.v1_0.schema.CDSOutput;

/**
 * REST Web Service
 *
 * @author sdn
 */
@Path("resources")
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
     * @param header
     * @param response
     * @return
     * @throws java.text.ParseException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("evaluate/{scopingEntityId}/{businessId}/{version}/{executionDate}")
    public CDSOutput evaluate(CDSInput cdsInput,
            @PathParam("scopingEntityId") final String scopingEntityId,
            @PathParam("businessId") final String businessId,
            @PathParam("version") final String version,
            @PathParam("executionDate") final String executionDateString,
            @Context HttpHeaders header,
            @Context HttpServletResponse response) throws ParseException {

        final String METHODNAME = "evaluate ";

        byte[] payload = CdsObjectAssist.cdsObjectToByteArray(cdsInput, CDSInput.class);

//        logger.info(METHODNAME, "payload=", new String(payload));
        logger.info(METHODNAME, "scopingEntityId=", scopingEntityId);
        logger.info(METHODNAME, "businessId=", businessId);
        logger.info(METHODNAME, "version=", version);
        logger.info(METHODNAME, "executionDateString=", executionDateString);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date executionDate = format.parse(executionDateString);

        logger.info("Starting evaluate...");

        String endPoint = Config.getCdsDefaultEndpoint();

        OpenCdsService service = OpenCdsService.getOpenCdsService(endPoint);

        byte[] evaluation = service.evaluate(payload, scopingEntityId, businessId, version, executionDate);

//        logger.info(METHODNAME, "evaluation=", new String(evaluation));
        CDSOutput result = CdsObjectAssist.cdsObjectFromByteArray(evaluation, CDSOutput.class);

        return result;
    }
}
