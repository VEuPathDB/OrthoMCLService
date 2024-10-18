package org.orthomcl.service.services;

import java.io.File;
import java.nio.file.Files;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.service.AbstractWdkService;

@Path("/newick-protein-tree")
public class NewickProteinTreeService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(NewickProteinTreeService.class);

  /**
   * Retrieves the newick protein tree for a given orthoGroupId.
   *
   * @param orthoGroupId the ID of the orthoGroup
   * @return The newick protein tree as a string
   * @throws WdkModelException if there is an error retrieving the newick protein
   *                           tree
   * @throws NotFoundException if the newick protein tree for the orthoGroup does
   *                           not exist
   */
  @GET
  @Path("/{orthoGroupId}")
  @Produces("text/x-nh")
  public Response getNewickProteinTree(@PathParam("orthoGroupId") String orthoGroupId) throws WdkModelException {
    String projectId = getWdkModel().getProjectId();
    String buildNumber = getWdkModel().getBuildNumber();
    String webservicesDir = getWdkModel().getProperties().get("WEBSERVICEMIRROR");

    // check for any characters that could break things (will throw an error)
    orthoGroupId = validateOrthoGroupId(orthoGroupId);
    // Now find and load the file
    String newickPath = String.format("%s/%s/build-%s/geneTrees/%s.fasta.tree", webservicesDir, projectId, buildNumber,
        orthoGroupId);
    LOG.debug("Newick path: " + newickPath);
    File newickFile = new File(newickPath);
    if (!newickFile.exists()) {
      LOG.error("Could not find newick file: " + newickPath);
      throw new NotFoundException("Could not find newick file: " + newickPath);
    }
    StreamingOutput output = out -> {
      Files.copy(newickFile.toPath(), out);
      out.flush();
    };
    return Response.ok(output, "text/x-nh")
        .header("content-disposition", "attachment; filename = " + orthoGroupId + ".fasta.tree")
        .build();
  }

  /**
   * Validates the orthoGroupId parameter. The orthoGroupId must not be null or
   * empty. The orthoGroupId may not contain any of the following characters:
   * / .. # : @
   *
   * @param orthoGroupId the orthoGroupId to be validated
   * @return the validated orthoGroupId
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
