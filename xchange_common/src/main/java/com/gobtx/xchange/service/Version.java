package com.gobtx.xchange.service;

import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/13. */
public class Version {
  protected final int version;
  protected final int majorVersion;
  protected final int minVersion;

  public Version(int version, int majorVersion, int minVersion) {
    this.version = version;
    this.majorVersion = majorVersion;
    this.minVersion = minVersion;
  }

  public int getVersion() {
    return version;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinVersion() {
    return minVersion;
  }

  @Override
  public String toString() {
    return "Version{"
        + "version="
        + version
        + ", majorVersion="
        + majorVersion
        + ", minVersion="
        + minVersion
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Version version1 = (Version) o;
    return version == version1.version
        && majorVersion == version1.majorVersion
        && minVersion == version1.minVersion;
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, majorVersion, minVersion);
  }
}
