package io.snyk.plugins.teamcity.agent.tool;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.snyk.plugins.teamcity.common.runner.Platform;
import jetbrains.buildServer.TeamCityRuntimeException;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SnykInstaller {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String SNYK_RELEASES_LATEST = "https://api.github.com/repos/snyk/snyk/releases/latest";
  private static final String SNYK_RELEASES_DOWNLOAD = "https://github.com/snyk/snyk/releases/download/%s/%s";
  private static final String SNYK_HTML_RELEASES_LATEST = "https://api.github.com/repos/snyk/snyk-to-html/releases/latest";
  private static final String SNYK_HTML_RELEASES_DOWNLOAD = "https://github.com/snyk/snyk-to-html/releases/download/%s/%s";
  private static final String TIMESTAMP_FILE = ".timestamp";

  private final BuildRunnerContext buildRunnerContext;

  public SnykInstaller(@NotNull BuildRunnerContext buildRunnerContext) {
    this.buildRunnerContext = buildRunnerContext;
  }

  public void performInstallation() {
    Path snykToolLocation = preferredLocation();
    try {
      BuildProgressLogger buildLogger = buildRunnerContext.getBuild().getBuildLogger();
      if (isUpToDate(snykToolLocation)) {
        buildLogger.message("Snyk Security tool is UP-TO-DATE");
        return;
      }

      buildLogger.message("Installing Snyk Security tool...");
      BuildAgentSystemInfo systemInfo = buildRunnerContext.getBuild().getAgentConfiguration().getSystemInfo();
      Platform platform;
      if (systemInfo.isWindows()) {
        platform = Platform.WINDOWS;
      } else if (systemInfo.isMac()) {
        platform = Platform.MAC_OS;
      } else if (systemInfo.isUnix()) {
        platform = Platform.LINUX;
      } else {
        throw new TeamCityRuntimeException("Could not determine agent platform");
      }
      URL downloadUrlForSnykTool = getDownloadUrlForSnykTool(platform);
      Path snykToolPath = Paths.get(preferredLocation().toAbsolutePath().toString(), "snyk-" + platform.getSuffix());
      FileUtils.copyURLToFile(downloadUrlForSnykTool, snykToolPath.toFile());
      URL downloadUrlForReportMapper = getDownloadUrlForReportMapper(platform);
      Path reportMapperPath = Paths.get(preferredLocation().toAbsolutePath().toString(), "snyk-to-html-" + platform.getSuffix());
      FileUtils.copyURLToFile(downloadUrlForReportMapper, reportMapperPath.toFile());
      if (platform != Platform.WINDOWS) {
        boolean result = snykToolPath.toFile().setExecutable(true, false);
        if (!result) {
          throw new TeamCityRuntimeException(format("Could not set executable flag for the file: %s", snykToolPath.toAbsolutePath().toString()));
        }
        result = reportMapperPath.toFile().setExecutable(true, false);
        if (!result) {
          throw new TeamCityRuntimeException(format("Could not set executable flag for the file: %s", reportMapperPath.toAbsolutePath().toString()));
        }
      }
      Path marker = Paths.get(snykToolLocation.toString(), TIMESTAMP_FILE);
      String content = valueOf(Instant.now().toEpochMilli());
      Files.write(marker, content.getBytes(UTF_8));
    } catch (IOException ex) {
      throw new TeamCityRuntimeException("Could not install Snyk tool", ex);
    }
  }

  private Path preferredLocation() {
    String agentToolsDirectory = buildRunnerContext.getBuild().getAgentConfiguration().getAgentToolsDirectory().getAbsolutePath();
    return Paths.get(agentToolsDirectory, "teamcity-snyk-security-plugin-runner", "bin", "latest");
  }

  private boolean isUpToDate(Path toolLocation) throws IOException {
    Path marker = Paths.get(toolLocation.toString(), TIMESTAMP_FILE);
    if (!marker.toFile().exists()) {
      return false;
    }

    String content = new String(Files.readAllBytes(marker), UTF_8);
    long timestampFromFile;
    try {
      timestampFromFile = Long.parseLong(content);
    } catch (NumberFormatException ex) {
      // corrupt of modified .timestamp file => force new installation
      timestampFromFile = 0;
    }

    long timestampNow = Instant.now().toEpochMilli();
    long timestampDifference = timestampNow - timestampFromFile;
    if (timestampDifference <= 0) {
      return true;
    }
    // currently we support 24 hour update interval only
    long updateInterval = TimeUnit.HOURS.toMillis(24);
    return timestampDifference < updateInterval;
  }

  private URL getDownloadUrlForSnykTool(@NotNull Platform platform) throws IOException {
    URL sourceUrl = new URL(SNYK_RELEASES_LATEST);
    JsonNode release = MAPPER.readTree(sourceUrl);
    String tagName = release.get("tag_name").asText();
    return new URL(format(SNYK_RELEASES_DOWNLOAD, tagName, "snyk-" + platform.getSuffix()));
  }

  private URL getDownloadUrlForReportMapper(@NotNull Platform platform) throws IOException {
    URL sourceUrl = new URL(SNYK_HTML_RELEASES_LATEST);
    JsonNode release = MAPPER.readTree(sourceUrl);
    String tagName = release.get("tag_name").asText();
    return new URL(format(SNYK_HTML_RELEASES_DOWNLOAD, tagName, "snyk-to-html-" + platform.getSuffix()));
  }
}
