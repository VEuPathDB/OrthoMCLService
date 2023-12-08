package org.orthomcl.service.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONObject;

@Path ("/newick-protein-tree")
public class NewickProteinTreeService extends AbstractWdkService {

    private static final Logger LOG = Logger.getLogger(NewickProteinTreeService.class);

    @GET
    @Path ("/{orthoGroupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewickProteinTree(@PathParam("orthoGroupId") String orthoGroupId) throws WdkModelException {
        String projectId = getWdkModel().getProjectId();
        String buildNumber = getWdkModel().getBuildNumber();
        String webservicesDir = getWdkModel().getProperties().get("WEBSERVICEMIRROR");
        // TODO need to add orthoGroupId to this path, update to reflect actual webservice mirror directory structure
        // this should be considered a placeholder really
        String newickPath = webservicesDir + "/" + projectId + "/" + "build-" + buildNumber + "/data/newick";

        try (BufferedReader br = new BufferedReader(new FileReader(newickPath))) {
            String newick = new StringBuilder();
            while ((String line = br.readLine()) != null) {
                // not even sure we should have more than one line?
                // need to check files when we have them
                newick.append(line);
            }
            newick = newick.toString();
            LOG.debug("Newick: " + newick);
        }
        catch (IOException e) {
            LOG.error("Could not read newick file: " + newickPath, e);
            throw new WdkModelException("Could not read newick file: " + newickPath, e);
        }
        catch (FileNotFoundException e) {
            LOG.error("Could not find newick file: " + newickPath, e);
            throw new NotFoundException("Could not find newick file: " + newickPath, e);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("newick", newick);
        Response response = Response.ok(jsonObject.toString()).build();
    }
   
}