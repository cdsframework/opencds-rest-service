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

import org.apache.commons.lang3.time.StopWatch;
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
public class EvaluateResource
{

  private static LogUtils logger = LogUtils.getLogger(EvaluateResource.class);

  @Context
  private UriInfo context;

  private int cdsConnectTimeout;
  private int cdsRequestTimeout;

  /**
   * Creates a new instance of EvaluateResource
   */
  public EvaluateResource()
  {
    cdsConnectTimeout = Integer.parseInt(System.getProperty("cds.timeout.connect", "10000"));
    cdsRequestTimeout = Integer.parseInt(System.getProperty("cds.timeout.request", "60000"));
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
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
  @Path("evaluate/{environment}/{scopingEntityId}/{businessId}/{version}/{executionDate}")
  public Response evaluate(CDSInput cdsInput,
      @PathParam("scopingEntityId") final String scopingEntityId,
      @PathParam("businessId") final String businessId,
      @PathParam("version") final String version,
      @PathParam("executionDate") final String executionDateString,
      @PathParam("environment") final String environment,
      @Context HttpHeaders header,
      @Context HttpServletResponse response) throws ParseException, UnsupportedEncodingException, IOException
  {

    StopWatch taskTimer = new StopWatch();
    StopWatch totalTimer = StopWatch.createStarted();
    final String METHODNAME = "evaluate ";

    CDSOutput result;
    taskTimer.start();
    byte[] payload = CdsObjectAssist.cdsObjectToByteArray(cdsInput, CDSInput.class);
    int cdsInputPayloadSize = payload.length;
    long cdsInputMarshalTime = taskTimer.getTime();
    taskTimer.reset();

    DeploymentEnvironment deploymentEnvironment = DeploymentEnvironment.valueOf(environment.toUpperCase());
    String endPoint = System.getProperty("cds.endpoint." + deploymentEnvironment.toString().toLowerCase());
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date executionDate = format.parse(executionDateString);

    OpenCdsService service = OpenCdsService.getOpenCdsService(endPoint, cdsRequestTimeout, cdsConnectTimeout);

    Response.ResponseBuilder responseBuilder = null;
    long openCdsTime = 0;
    long cdsOutputUnmarshalTime = 0;
    int cdsOutputPayloadSize = 0;
    String error = "";
    try
    {
      taskTimer.start();
      byte[] evaluation = service.evaluate(payload, scopingEntityId, businessId, version, executionDate);
      openCdsTime = taskTimer.getTime();
      taskTimer.reset();
      cdsOutputPayloadSize = evaluation.length;

      taskTimer.start();
      result = CdsObjectAssist.cdsObjectFromByteArray(evaluation, CDSOutput.class);
      cdsOutputUnmarshalTime = taskTimer.getTime();

      responseBuilder = Response.ok(result);
      responseBuilder.header("ERRORP", "foo");

    }
    catch (Exception e)
    {
      error = e.getMessage();
      logger.error(METHODNAME + " e.getMessage()=" + error, e);
      responseBuilder = Response.serverError().entity(error).type(MediaType.TEXT_PLAIN);
    }

    Response res = responseBuilder.build();
    long totalTime = totalTimer.getTime();
    String info = String.format(" scopingEntityId=%s; businessId=%s; version=%s; executionDateString=%s; environment=%s; " +
            "endPoint=%s; cdsInputMarshalTime=%d; cdsInputPayloadSize=%d; openCdsTime=%d; cdsOutputPayloadSize=%d; " +
            "cdsOutputUnmarshalTime=%d; totalTime=%d; status=%d; error=%s",
        scopingEntityId, businessId, version, executionDateString, environment, endPoint, cdsInputMarshalTime, cdsInputPayloadSize,
        openCdsTime, cdsOutputPayloadSize, cdsOutputUnmarshalTime, totalTime, res.getStatus(), error);
    logger.info(METHODNAME, info);
    return res;
  }
}
