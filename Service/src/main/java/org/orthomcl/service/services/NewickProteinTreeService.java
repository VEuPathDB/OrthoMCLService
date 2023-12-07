package org.orthomcl.service.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONObject;

@Path ("/newick-protein-tree")
public class NewickProteinTreeService extends AbstractWdkService {

    @GET
    @Path ("/{orthoGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewickProteinTree() throws WdkModelException {
        String projectId = getWdkModel().getProjectId();
        String buildNumber = getWdkModel().getBuildNumber();
        String webservicesDir = getWdkModel().getProperties().get("WEBSERVICEMIRROR");
        // TODO need to add orthoGroupId to this path, update to reflect actual webservice mirror
        // this should be considered a placeholder really
        String newickPath = webservicesDir + "/" + projectId + "/" + "build-" + buildNumber + "/data/newick";

        try (Stream<String> newick = 
            Files.lines(Files.newBufferedReader(Paths.get(newickPath), StandardCharsets.UTF_8)
                .lines())) {
             lines.forEach(System.out::println); //debug
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("newick", newick);
        Response response = Response.ok(jsonObject.toString()).build();
    }
   
}