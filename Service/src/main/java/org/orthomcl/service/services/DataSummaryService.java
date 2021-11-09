package org.orthomcl.service.services;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.json.ToJson;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.report.config.AnswerDetails.AttributeFormat;
import org.gusdb.wdk.model.report.util.RecordFormatter;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.orthomcl.service.core.HelperRecord;
import org.orthomcl.service.core.Taxon;
import org.orthomcl.service.core.TaxonManager;

@Path("/data-summary")
public class DataSummaryService extends AbstractWdkService {

  @GET
  @Path("/taxons")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaxons() throws WdkModelException {
    Map<String, Taxon> taxons = TaxonManager.getTaxons(getWdkModel());
    return Response.ok(ToJson.mapToJson(taxons).toString()).build();
  }

  @GET
  @Path("/proteomes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProteomes() throws WdkModelException {
    return getHelperTableResponse("DataSummary");
  }

  private Response getHelperTableResponse(String tableName) throws WdkModelException {
    try {
      return Response.ok(RecordFormatter.getTableRowsJson(HelperRecord.get(getWdkModel()), tableName, AttributeFormat.DISPLAY)).build();
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Could not load helper record dynamic table: " + tableName);
    }
  }

}
