package org.orthomcl.service;

import java.util.Set;

import org.eupathdb.common.service.EuPathServiceApplication;
import org.gusdb.fgputil.SetBuilder;
import org.gusdb.wdk.service.service.SessionService;
import org.orthomcl.service.services.GroupLayoutService;
import org.orthomcl.service.services.DataSummaryService;
import org.orthomcl.service.services.NewickProteinTreeService;
import org.orthomcl.service.services.OrthoSessionService;

public class OrthoMCLServiceApplication extends EuPathServiceApplication {

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()
      .addAll(super.getClasses())
      .add(DataSummaryService.class)
      .add(GroupLayoutService.class)
      .add(NewickProteinTreeService.class)
      .replace(SessionService.class, OrthoSessionService.class)
      .toSet();
  }
}
