package org.orthomcl.service.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONObject;
import org.orthomcl.service.core.layout.GroupLayout;
import org.orthomcl.service.core.layout.GroupLayoutManager;

@Path("/group")
public class GroupLayoutService extends AbstractWdkService {

  @GET
  @Path("/{groupName}/layout")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleRequest(@PathParam("groupName") String groupName) throws Exception {
    // get the layout data
    GroupLayoutManager layoutManager = new GroupLayoutManager(getWdkModel());
    GroupLayout layout = layoutManager.getLayout(getSessionUser(), groupName);

    // format response
    JSONObject layoutJson = layout == null
      ? new JSONObject().put("layoutOffered", false)
      : layout.toJson().put("layoutOffered", true);

    return Response.ok(layoutJson.toString()).build();
  }
}
