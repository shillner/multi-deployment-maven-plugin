package com.itemis.maven.plugins.multiDeploy;

import org.apache.maven.plugins.annotations.Parameter;

public class Repository {
  @Parameter(required = true)
  private String id;
  @Parameter(required = true)
  private String url;
  @Parameter(required = true, defaultValue = "true")
  private boolean snapshotsEnabled = true;
  @Parameter(required = true, defaultValue = "true")
  private boolean releasesEnabled = true;

  public String getId() {
    return this.id;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean isSnapshotsEnabled() {
    return this.snapshotsEnabled;
  }

  public boolean isReleasesEnabled() {
    return this.releasesEnabled;
  }
}
