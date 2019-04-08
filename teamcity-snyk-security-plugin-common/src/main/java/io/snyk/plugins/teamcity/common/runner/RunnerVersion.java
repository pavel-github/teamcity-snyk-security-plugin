package io.snyk.plugins.teamcity.common.runner;

public class RunnerVersion {

  RunnerVersion() {
  }

  /**
   * Returns the path to <code>snyk</code> CLI binary file
   */
  public String getSnykToolFileName(Platform platform) {
    if (platform == null) {
      return "snyk-linux";
    }
    return "snyk-" + platform.getSuffix();
  }

  /**
   * Returns the path to <code>snyk-to-html</code> binary file
   */
  public String getReportMapperFileName(Platform platform) {
    if (platform == null) {
      return "snyk-to-html-linux";
    }
    return "snyk-to-html-" + platform.getSuffix();
  }
}
