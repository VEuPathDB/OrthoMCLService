package org.orthomcl.service;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.eupathdb.common.controller.EuPathSiteSetup;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.fgputil.web.servlet.HttpServletApplicationContext;
import org.gusdb.wdk.controller.WdkInitializer;

/**
 * A class that is initialized at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 */
public class ApplicationInitListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ApplicationContext context = new HttpServletApplicationContext(sce.getServletContext());
    WdkInitializer.initializeWdk(context);
    EuPathSiteSetup.initialize(WdkInitializer.getWdkModel(context));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ApplicationContext context = new HttpServletApplicationContext(sce.getServletContext());
    WdkInitializer.terminateWdk(context);
  }
}
