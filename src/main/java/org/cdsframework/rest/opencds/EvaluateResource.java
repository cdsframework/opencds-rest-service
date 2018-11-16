/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdsframework.rest.opencds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.cdsframework.cds.service.OpenCdsService;
import org.cdsframework.cds.vmr.CdsInputWrapper;
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
     * @param payload
     * @param scopingEntityId
     * @param businessId
     * @param version
     * @param executionDate
     * @return an instance of java.lang.String
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("evaluate/{scopingEntityId}/{businessId}/{version}/{executionDate}")
    public byte[] evaluate(byte[] payload,
            @PathParam("scopingEntityId") final String scopingEntityId,
            @PathParam("businessId") final String businessId,
            @PathParam("version") final String version,
            @PathParam("executionDate") final String executionDateString) throws ParseException {

        final String METHODNAME = "evaluate ";

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

        if (logger.isDebugEnabled()) {
            CDSOutput result = CdsObjectAssist.cdsObjectFromByteArray(evaluation, CDSOutput.class);
        }

        return evaluation;
    }
}
