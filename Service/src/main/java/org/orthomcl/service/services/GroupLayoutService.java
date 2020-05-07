package org.orthomcl.service.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.service.service.AbstractWdkService;
import org.orthomcl.service.core.layout.GroupLayout;
import org.orthomcl.service.core.layout.GroupLayoutManager;

public class GroupLayoutService extends AbstractWdkService {

  @GET
  @Path("/group/{groupName}/layout")
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleRequest(@PathParam("groupName") String groupName) throws Exception {

    // get the layout data
    GroupLayoutManager layoutManager = new GroupLayoutManager(getWdkModel());
    GroupLayout layout = layoutManager.getLayout(getSessionUser(), groupName);

    // format response
    return Response.ok(layout.toJson().toString()).build();
  }
}
