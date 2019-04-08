package io.snyk.plugins.teamcity.common.runner;

import java.util.Set;
import java.util.TreeMap;

public final class Runners {

  private static final TreeMap<String, RunnerVersion> AVAILABLE_RUNNERS = new TreeMap<>();

  // all bundled versions should be initialized here
  static {
    AVAILABLE_RUNNERS.put("latest", new RunnerVersion());
    AVAILABLE_RUNNERS.put("1.143.1", new RunnerVersion());
  }

  public static RunnerVersion getRunner(String version) {
    return AVAILABLE_RUNNERS.get(version);
  }

  public Set<String> getVersions() {
    return AVAILABLE_RUNNERS.descendingKeySet();
  }
}
