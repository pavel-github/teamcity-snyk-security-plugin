package io.snyk.plugins.teamcity.server;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;

public class SnykSecurityReportTab extends ViewLogTab {

  private static final String TAB_TITLE = "Snyk Security Report";
  private static final String TAB_CODE = "snykSecurityReport";

  public SnykSecurityReportTab(@NotNull PagePlaces pagePlaces, @NotNull SBuildServer server, @NotNull PluginDescriptor pluginDescriptor) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server);

    // setIncludeUrl("/artifactsViewer.jsp");
    setIncludeUrl(pluginDescriptor.getPluginResourcesPath("tab/snykSecurityReport.jsp"));
    setPosition(PositionConstraint.after("artifacts"));
    addCssFile(pluginDescriptor.getPluginResourcesPath("tab/snykSecurityReport.css"));
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> map, @NotNull HttpServletRequest httpServletRequest, @NotNull SBuild build) {
    //map.put("something", getSnykHtmlReport(build));
  }

  @Override
  protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
    SBuildType buildType = build.getBuildType();
    if (buildType == null || !build.isFinished()) {
      return false;
    }
    return buildType.getRunnerTypes().contains(SnykSecurityRunnerConstants.RUNNER_TYPE);
  }

  private String getSnykHtmlReport(SBuild build) {
    BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(SnykSecurityRunnerConstants.SNYK_REPORT_HTML_FILE);
    return artifact != null ? artifact.getRelativePath() : SnykSecurityRunnerConstants.SNYK_REPORT_HTML_FILE;
  }
}
