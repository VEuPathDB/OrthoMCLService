package org.orthomcl.service;

import java.util.Set;

import org.eupathdb.common.service.EuPathServiceApplication;
import org.gusdb.fgputil.SetBuilder;
import org.orthomcl.service.services.GroupLayoutService;
import org.orthomcl.service.services.TaxonService;

public class OrthoMCLServiceApplication extends EuPathServiceApplication {

  @Override
  public Set<Class<?>> getClasses() {
    return new SetBuilder<Class<?>>()
      .addAll(super.getClasses())
      .add(TaxonService.class)
      .add(GroupLayoutService.class)
      .toSet();
  }
}
