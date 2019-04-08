package io.snyk.plugins.teamcity.agent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.snyk.plugins.teamcity.agent.commands.SnykMonitorCommand;
import io.snyk.plugins.teamcity.agent.commands.SnykReportCommand;
import io.snyk.plugins.teamcity.agent.commands.SnykTestCommand;
import io.snyk.plugins.teamcity.agent.tool.SnykInstaller;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.TeamCityRuntimeException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.runner.CommandExecution;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.MONITOR_PROJECT_ON_BUILD;
import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.SNYK_MONITOR_REPORT_JSON_FILE;
import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.SNYK_REPORT_HTML_FILE;
import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.SNYK_TEST_REPORT_JSON_FILE;
import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.VERSION;
import static java.util.Objects.requireNonNull;
import static jetbrains.buildServer.util.PropertiesUtil.getBoolean;

public class SnykCommandBuildSession implements MultiCommandBuildSession {

  private final ArtifactsWatcher artifactsWatcher;
  private final BuildRunnerContext buildRunnerContext;

  private Iterator<CommandExecutionAdapter> buildSteps;
  private CommandExecutionAdapter lastCommand;

  SnykCommandBuildSession(@NotNull ArtifactsWatcher artifactsWatcher, @NotNull BuildRunnerContext buildRunnerContext) {
    this.artifactsWatcher = artifactsWatcher;
    this.buildRunnerContext = requireNonNull(buildRunnerContext);
  }

  @Override
  public void sessionStarted() {
    String version = buildRunnerContext.getRunnerParameters().get(VERSION);
    if ("latest".equals(version)) {
      SnykInstaller snykInstaller = new SnykInstaller(buildRunnerContext);
      snykInstaller.performInstallation();
    }

    buildSteps = getBuildSteps();
  }

  @Nullable
  @Override
  public CommandExecution getNextCommand() {
    if (buildSteps.hasNext()) {
      lastCommand = buildSteps.next();
      return lastCommand;
    }
    return null;
  }

  @Nullable
  @Override
  public BuildFinishedStatus sessionFinished() {
    String buildTempDirectory = buildRunnerContext.getBuild().getBuildTempDirectory().getAbsolutePath();
    Path snykReportHtml = Paths.get(buildTempDirectory, SNYK_REPORT_HTML_FILE);
    artifactsWatcher.addNewArtifactsPath(snykReportHtml.toAbsolutePath().toString());

    return lastCommand.getResult();
  }

  private Iterator<CommandExecutionAdapter> getBuildSteps() {
    List<CommandExecutionAdapter> steps = new ArrayList<>(3);
    String buildTempDirectory = buildRunnerContext.getBuild().getBuildTempDirectory().getAbsolutePath();

    // Disable for development process
    // SnykVersionCommand snykVersionCommand = new SnykVersionCommand();
    // steps.add(addCommand(snykVersionCommand, Paths.get(buildTempDirectory, "version.txt")));

    SnykTestCommand snykTestCommand = new SnykTestCommand();
    steps.add(addCommand(snykTestCommand, Paths.get(buildTempDirectory, SNYK_TEST_REPORT_JSON_FILE)));

    String monitorProjectOnBuild = buildRunnerContext.getRunnerParameters().get(MONITOR_PROJECT_ON_BUILD);
    if (getBoolean(monitorProjectOnBuild)) {
      SnykMonitorCommand snykMonitorCommand = new SnykMonitorCommand();
      steps.add(addCommand(snykMonitorCommand, Paths.get(buildTempDirectory, SNYK_MONITOR_REPORT_JSON_FILE)));
    }

    SnykReportCommand snykReportCommand = new SnykReportCommand();
    steps.add(addCommand(snykReportCommand, Paths.get(buildTempDirectory, "report.output")));

    return steps.iterator();
  }

  private CommandExecutionAdapter addCommand(CommandLineBuildService buildService, Path commandOutputPath) {
    try {
      buildService.initialize(buildRunnerContext.getBuild(), buildRunnerContext);
    } catch (RunBuildException ex) {
      throw new TeamCityRuntimeException(ex);
    }
    return new CommandExecutionAdapter(buildService, commandOutputPath);
  }
}
