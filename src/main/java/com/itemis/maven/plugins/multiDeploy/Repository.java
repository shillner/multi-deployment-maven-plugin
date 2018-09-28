package com.itemis.maven.plugins.multiDeploy;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.base.Splitter;

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

  public static Optional<Repository> parseFromProperty(String value) {
    Repository repo = new Repository();

    Splitter.on(',').split(value).forEach(s -> {
      int i = s.indexOf('=');
      if (i > 0 && i < s.length() - 1) {
        String k = s.substring(0, i).trim();
        String v = s.substring(i + 1, s.length()).trim();
        switch (k) {
          case "id":
            repo.id = v;
            break;
          case "url":
            repo.url = v;
            break;
          case "releases":
            repo.releasesEnabled = Boolean.parseBoolean(v);
            break;
          case "snapshots":
            repo.snapshotsEnabled = Boolean.parseBoolean(v);
            break;
        }
      }
    });

    if (StringUtils.isNotBlank(repo.id) && StringUtils.isNoneBlank(repo.url)) {
      return Optional.of(repo);
    }
    return Optional.empty();
  }
}
