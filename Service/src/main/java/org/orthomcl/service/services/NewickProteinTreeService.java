package org.orthomcl.service.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

/**
 * Retrieves the newick protein tree for a given orthoGroupId.
 *
 * @param  orthoGroupId  the ID of the orthoGroup
 * @return               a Response object containing the newick protein tree as a string in JSON format
 * @throws WdkModelException if there is an error retrieving the newick protein tree
 * @throws NotFoundException if the newick protein tree for the orthoGroup does not exist
 */
@GET
@Path ("/{orthoGroupId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getNewickProteinTree(@PathParam("orthoGroupId") String orthoGroupId) throws WdkModelException {
    String projectId = getWdkModel().getProjectId();
    //String buildNumber = getWdkModel().getBuildNumber();
    String webservicesDir = getWdkModel().getProperties().get("WEBSERVICEMIRROR");
    
    orthoGroupId = validateOrthoGroupId(orthoGroupId);
    String newickPath = String.format("%s/%s/build-current/newick/%s.fasta.fas.tree", webservicesDir, projectId, orthoGroupId);
    LOG.debug("Newick path: " + newickPath);

    try (BufferedReader br = new BufferedReader(new FileReader(newickPath))) {
        StringBuilder newick = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            newick.append(line);
        }
        String newickString = newick.toString();
        LOG.debug("Newick: " + newickString);
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("newick", newickString);
        return Response.ok(jsonObject.toString()).build();
    }
    catch (FileNotFoundException e) {
        LOG.error("Could not find newick file: " + newickPath, e);
        throw new NotFoundException("Could not find newick file: " + newickPath, e);
    }
    catch (IOException e) {
        LOG.error("Could not read newick file: " + newickPath, e);
        throw new WdkModelException("Could not read newick file: " + newickPath, e);
    }
    
}
   
/**
 * Validates the orthoGroupId parameter. The orthoGroupId must not be null or
 * empty. The orthoGroupId may not contain any of the following characters:
 * / .. # : @
 *
 * @param  orthoGroupId  the orthoGroupId to be validated
 * @return               the validated orthoGroupId
 */
private String validateOrthoGroupId(String orthoGroupId) {
    if (orthoGroupId == null || orthoGroupId.isEmpty()) {
        throw new IllegalArgumentException("orthoGroupId is required");
    }
    if (orthoGroupId.contains("/") || orthoGroupId.contains("..") || orthoGroupId.contains("#") || 
        orthoGroupId.contains(":") || orthoGroupId.contains("@") || orthoGroupId.contains(" ")) {
        throw new IllegalArgumentException("orthoGroupId contains invalid characters");
    }
    return orthoGroupId;
}
}
