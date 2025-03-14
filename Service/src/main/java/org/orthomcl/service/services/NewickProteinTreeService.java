package org.orthomcl.service.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Types;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
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
    if (!newickFile.exists()) createNewickTreeFile(newickPath, orthoGroupId);

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

  private void createNewickTreeFile(String newickFile, String orthoGroupId) throws WdkModelException {
    java.nio.file.Path errorFilePath;
    java.nio.file.Path fastaFilePath;
    try {
      java.nio.file.Path tempDirPath = getWdkModel().getModelConfig().getWdkTempDir();
      fastaFilePath = Files.createTempFile(tempDirPath, null, null);
      errorFilePath = Files.createTempFile(tempDirPath, null, null);

      createFastaFile(orthoGroupId, fastaFilePath);
      String command = String.format("singularity exec orthofinder.sif mafft --auto --anysymbol %s 2> %s | fasttree -mlnni 4 > %s 2>> %s",
              fastaFilePath, errorFilePath, newickFile, errorFilePath);
      // Start the process
      Process process = Runtime.getRuntime().exec(new String[]{command});
      int exitCode = process.waitFor();
      if (exitCode == 0) new File(errorFilePath.toString()).delete();
      else throw new WdkModelException("For group " + orthoGroupId +
              ", failed executing command '" + command + "' with code: " + exitCode +
              ".  See error file " + errorFilePath);
    } catch (IOException | InterruptedException e) {
      throw new WdkModelException(e);
    }
  }

  void createFastaFile(String groupId, java.nio.file.Path fileName) {
    String sql =
            "SELECT eas.secondary_identifier, eas.sequence" + "\n" +
                    "FROM dots.Orthoaasequence eas, apidbtuning.sequenceAttributes sa" + "\n" +
                    "where sa.full_id = eas.secondary_identifier" + "\n" +
                    "and sa.group_name = ?";

    new SQLRunner(getWdkModel().getAppDb().getDataSource(), sql, "select-protein-aa-sequence").executeQuery(
            new Object[]{groupId},
            new Integer[]{Types.VARCHAR},
            rs -> {
              try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName.toString(), true))) {
                while (rs.next()) {
                  String seqId = rs.getString(1);
                  String seqSeq = rs.getString(2);
                  String formattedSeq = addNewlines(seqSeq, 80);
                  try {
                    writer.write(">" + seqId);
                    writer.newLine();
                    writer.write(formattedSeq);
                    writer.newLine();
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
                return null;
              } catch (SQLRunnerException sre) {
                throw new RuntimeException(sre.getCause().getMessage(), sre.getCause());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  public static String addNewlines(String input, int lineLength) {
    StringBuilder result = new StringBuilder();
    int start = 0;

    while (start < input.length()) {
      int end = Math.min(start + lineLength, input.length());
      result.append(input, start, end).append(System.lineSeparator());
      start = end;
    }

    return result.toString();
  }
}
