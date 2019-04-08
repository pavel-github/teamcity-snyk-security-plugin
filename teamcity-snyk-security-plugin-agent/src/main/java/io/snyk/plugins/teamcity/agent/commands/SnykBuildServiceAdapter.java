package io.snyk.plugins.teamcity.agent.commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.snyk.plugins.teamcity.common.runner.Platform;
import io.snyk.plugins.teamcity.common.runner.RunnerVersion;
import io.snyk.plugins.teamcity.common.runner.Runners;
import jetbrains.buildServer.TeamCityRuntimeException;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import org.jetbrains.annotations.NotNull;

import static io.snyk.plugins.teamcity.common.SnykSecurityRunnerConstants.VERSION;
import static java.lang.String.format;

abstract class SnykBuildServiceAdapter extends BuildServiceAdapter {

  abstract List<String> getArguments();

  String getSnykToolPath() {
    String version = getRunnerParameters().get(VERSION);
    RunnerVersion runner = Runners.getRunner(version);
    if (runner == null) {
      throw new TeamCityRuntimeException(format("Snyk Security runner with version '%s' was not found. Please configure the build properly and retry.", version));
    }

    String agentToolsDirectory = getAgentConfiguration().getAgentToolsDirectory().getAbsolutePath();
    Platform platform = detectAgentPlatform();
    Path snykToolPath = Paths.get(agentToolsDirectory, "teamcity-snyk-security-plugin-runner", "bin", version, runner.getSnykToolFileName(platform));
    if (!snykToolPath.toFile().exists()) {
      throw new TeamCityRuntimeException(format("Could not found '%s'", snykToolPath.toString()));
    }
    return snykToolPath.toString();
  }

  String getReportMapperPath() {
    String version = getRunnerParameters().get(VERSION);
    RunnerVersion runner = Runners.getRunner(version);
    if (runner == null) {
      throw new TeamCityRuntimeException(format("Snyk Security runner with version '%s' was not found. Please configure the build properly and retry.", version));
    }

    String agentToolsDirectory = getAgentConfiguration().getAgentToolsDirectory().getAbsolutePath();
    Platform platform = detectAgentPlatform();
    Path reportMapperPath = Paths.get(agentToolsDirectory, "teamcity-snyk-security-plugin-runner", "bin", version, runner.getReportMapperFileName(platform));
    if (!reportMapperPath.toFile().exists()) {
      throw new TeamCityRuntimeException(format("Could not found '%s'", reportMapperPath.toString()));
    }
    return reportMapperPath.toString();
  }

  private Platform detectAgentPlatform() {
    BuildAgentSystemInfo buildAgentSystemInfo = getAgentConfiguration().getSystemInfo();
    if (buildAgentSystemInfo.isUnix()) {
      return Platform.LINUX;
    } else if (buildAgentSystemInfo.isMac()) {
      return Platform.MAC_OS;
    } else if (buildAgentSystemInfo.isWindows()) {
      return Platform.WINDOWS;
    } else {
      throw new TeamCityRuntimeException("Could not detect OS on build agent: " + getAgentConfiguration().getName());
    }
  }

  @Override
  public boolean isCommandLineLoggingEnabled() {
    return true;
  }

  @NotNull
  @Override
  public BuildFinishedStatus getRunResult(int exitCode) {
    if (exitCode == 0) {
      return BuildFinishedStatus.FINISHED_SUCCESS;
    }
    return getBuild().getFailBuildOnExitCode() ? BuildFinishedStatus.FINISHED_WITH_PROBLEMS : BuildFinishedStatus.FINISHED_SUCCESS;
  }
}
