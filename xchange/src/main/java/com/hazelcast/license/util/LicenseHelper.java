package com.hazelcast.license.util;

import com.hazelcast.license.domain.Feature;
import com.hazelcast.license.domain.License;
import com.hazelcast.license.domain.LicenseType;
import com.hazelcast.license.domain.LicenseVersion;
import com.hazelcast.license.exception.InvalidLicenseException;
import com.hazelcast.license.nlc.BuiltInLicenseProvider;
import com.hazelcast.license.nlc.BuiltInLicenseProviderFactory;

import java.util.*;

/** Created by Aaron Kuai on 2019/9/25. */
public class LicenseHelper {

  public static final char[] chars =
      "dsx4MZQvo2tqegGLpWhCDXnPYzciBR3murF5SA7KIObaw1Vf6JNyjEl0UTkH".toCharArray();
  public static final char[] charsV5 =
      "az{Vh}r4:t$e]>_3%?@o|^p=.x*~<R;&c[s!+L-F7£IvY6ulkJB9jfQSdTWqGbX58ECw21nNM0PHKZmyUiOAgD"
          .toCharArray();
  public static final char[] digits = "0123456789".toCharArray();
  public static final int lengthV5;
  public static final int length;
  public static final int yearBase = 2000;
  public static final String INF_STR_ENTERPRISE = "HazelcastEnterprise";
  public static final String INF_STR_ENTERPRISE_HD = "HazelcastEnterpriseHD";
  public static final String INF_STR_MANCENTER = "ManagementCenter";
  public static final String INF_STR_SECURITY = "SecurityOnlyEnterprise";

  private LicenseHelper() {}

  public static char[] hash(char[] a) {
    if (a == null) {
      return new char[] {'0'};
    } else {
      int result = 1;
      char[] var2 = a;
      int var3 = a.length;

      for (int var4 = 0; var4 < var3; ++var4) {
        char element = var2[var4];
        result = 31 * result + element;
      }

      return Integer.toString(Math.abs(result)).toCharArray();
    }
  }

  public static int extractHazelcastMajorMinorVersionAsInt(String version) {
    String[] parts = version.split("\\.");
    Integer versionPart1;
    Integer versionPart2;
    if (parts.length > 2) {
      versionPart1 = Integer.parseInt(parts[0]);
      versionPart2 = Integer.parseInt(parts[1]);
      return generateFinalStateOfIntVersion(versionPart1, versionPart2);
    } else {
      String[] betaParts = parts[1].split("-");
      versionPart1 = Integer.parseInt(parts[0]);
      versionPart2 = Integer.parseInt(betaParts[0]);
      return generateFinalStateOfIntVersion(versionPart1, versionPart2);
    }
  }

  private static int generateFinalStateOfIntVersion(int versionPart1, int versionPart2) {
    boolean isDoubleDigit = versionPart2 > 9 && versionPart2 < 100;
    return isDoubleDigit ? versionPart1 * 100 + versionPart2 : versionPart1 * 10 + versionPart2;
  }

  public static LicenseVersion extractLicenseVersion(String version) {
    try {
      String[] parts = version.split("\\.");
      // Integer versionPart2;
      Integer versionPart2;
      if (parts.length > 2) {
        Integer versionPart1 = Integer.parseInt(parts[0]);
        versionPart2 = Integer.parseInt(parts[1]);
        versionPart2 = Integer.parseInt(parts[2]);
        if (versionPart1 >= 3 && versionPart2 >= 10) {
          return LicenseVersion.V5;
        } else if (versionPart1 >= 3 && versionPart2 >= 7) {
          return LicenseVersion.V4;
        } else if (versionPart1 == 3 && versionPart2 == 6 && versionPart2 >= 1) {
          return LicenseVersion.V4;
        } else if (versionPart1 == 3 && versionPart2 == 6) {
          return LicenseVersion.V3;
        } else {
          return versionPart1 >= 3 && versionPart2 >= 5 ? LicenseVersion.V2 : LicenseVersion.V1;
        }
      } else {
        String[] rcParts = parts[1].split("-");
        versionPart2 = Integer.parseInt(parts[0]);
        versionPart2 = Integer.parseInt(rcParts[0]);
        if (versionPart2 >= 3 && versionPart2 >= 10) {
          return LicenseVersion.V5;
        } else if (versionPart2 >= 3 && versionPart2 >= 7) {
          return LicenseVersion.V4;
        } else if (versionPart2 == 3 && versionPart2 == 6) {
          return LicenseVersion.V3;
        } else {
          return versionPart2 >= 3 && versionPart2 >= 5 ? LicenseVersion.V2 : LicenseVersion.V1;
        }
      }
    } catch (Exception var5) {
      var5.printStackTrace();
      return LicenseVersion.V1;
    }
  }

  public static boolean isExpired(License license) {
    return System.currentTimeMillis() > getExpiryDateWithGracePeriod(license).getTime();
  }

  public static Date getExpiryDateWithGracePeriod(License license) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(license.getExpiryDate());
    calendar.add(2, license.getGracePeriod());
    return calendar.getTime();
  }

  public static Date generateCreationDateFromExpiryDate(
      int monthDiffCreationAndExpDates, Date expiryDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(expiryDate);
    calendar.add(2, -monthDiffCreationAndExpDates);
    return calendar.getTime();
  }

  public static License getLicense(String licenseKey, String versionString) {

    Calendar cal = Calendar.getInstance();
    cal.set(1, 2099);
    cal.set(2, 11);
    cal.set(5, 12);
    cal.set(11, 23);
    cal.set(12, 59);
    cal.set(13, 59);
    cal.set(14, 999);
    Date expiryDate = cal.getTime();

    cal.set(1, 2018);

    License license =
        new License(
            0,
            licenseKey,
            (String) null,
            cal.getTime(),
            expiryDate,
            false,
            (String) null,
            (String) null,
            2000,
            99999999,
            LicenseType.ENTERPRISE_HD,
            99,
            false,
            1000);
    license.setVersion(LicenseVersion.V5);

    List<Feature> featureList = new ArrayList();

    featureList.add(Feature.MAN_CENTER);
    featureList.add(Feature.CLUSTERED_JMX);
    featureList.add(Feature.CLUSTERED_REST);
    featureList.add(Feature.SECURITY);
    featureList.add(Feature.WAN);
    featureList.add(Feature.HD_MEMORY);
    license.setFeatures(featureList);

    return license;
  }

  public static License getBuiltInLicense() {
    BuiltInLicenseProviderFactory providerFactory = new BuiltInLicenseProviderFactory();
    BuiltInLicenseProvider licenseProvider = providerFactory.create();
    return licenseProvider != null ? licenseProvider.provide() : null;
  }

  public static boolean isBuiltInLicense(License license) {
    if (license == null) {
      return false;
    } else {
      return license.getVersion() == LicenseVersion.V5
          && license.getType() == LicenseType.CUSTOM
          && license.isOem()
          && license.getKey() == null
          && license.getCreationDate() == null;
    }
  }

  public static License checkLicenseKey(
      String licenseKey, String versionString, LicenseType... requiredLicenseTypes) {
    License license = getLicense(licenseKey, versionString);
    if (!Arrays.asList(requiredLicenseTypes).contains(license.getType())) {
      throw new InvalidLicenseException("Invalid License Type! Please contact sales@hazelcast.com");
    } else {
      return license;
    }
  }

  public static License checkLicenseKeyPerFeature(
      String licenseKey, String versionString, Feature feature) {
    License license = getLicense(licenseKey, versionString);
    checkLicensePerFeature(license, feature);
    return license;
  }

  public static void checkLicensePerFeature(License license, Feature feature) {
    if (!license.getFeatures().contains(feature)) {
      throw new InvalidLicenseException(
          "The Feature "
              + feature.getText()
              + " is not enabled for your license key.Please contact sales@hazelcast.com");
    }
  }

  public static License mapFeaturesForLegacyLicense(License license) {
    List<Feature> featureList = new ArrayList();
    if (license.getType() == LicenseType.MANAGEMENT_CENTER) {
      featureList.add(Feature.MAN_CENTER);
    }

    if (license.getType() == LicenseType.ENTERPRISE_SECURITY_ONLY) {
      featureList.add(Feature.SECURITY);
    }

    if (license.getType() == LicenseType.ENTERPRISE) {
      addEnterpriseFeatures(featureList);
      if (license.getVersion() == LicenseVersion.V2) {
        featureList.add(Feature.HD_MEMORY);
      }
    }

    if (license.getVersion() == LicenseVersion.V3
        && license.getType() == LicenseType.ENTERPRISE_HD) {
      addEnterpriseFeatures(featureList);
      featureList.add(Feature.HD_MEMORY);
      featureList.add(Feature.HOT_RESTART);
    }

    license.setFeatures(featureList);
    return license;
  }

  private static void addEnterpriseFeatures(List<Feature> featureList) {
    featureList.add(Feature.MAN_CENTER);
    featureList.add(Feature.WEB_SESSION);
    featureList.add(Feature.CLUSTERED_REST);
    featureList.add(Feature.CLUSTERED_JMX);
    featureList.add(Feature.SECURITY);
    featureList.add(Feature.WAN);
    featureList.add(Feature.ROLLING_UPGRADE);
  }

  static {
    lengthV5 = charsV5.length;
    length = chars.length;
  }
}
