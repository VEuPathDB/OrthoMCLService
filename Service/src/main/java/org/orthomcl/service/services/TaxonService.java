package org.orthomcl.service.services;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.json.ToJson;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.orthomcl.service.core.Taxon;
import org.orthomcl.service.core.TaxonManager;

@Path("/taxons")
public class TaxonService extends AbstractWdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaxons() throws WdkModelException {
    Map<String, Taxon> taxons = TaxonManager.getTaxons(getWdkModel());
    return Response.ok(ToJson.mapToJson(taxons).toString()).build();
  }

}
