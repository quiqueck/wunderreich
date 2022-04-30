package de.ambertation.wunderreich.utils;

import de.ambertation.wunderreich.Wunderreich;

import net.minecraft.util.FastColor;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    public final String version;

    public Version(String version) {
        if ("${version}".equals(version)) version = "0.0.0";
        this.version = version;
    }

    public Version(int major, int minor, int patch) {
        this(String.format(Locale.ROOT, "%d.%d.%d", major, minor, patch));
    }

    private static int toInt(String version) {
        if (version == null || version.isEmpty()) return 0;

        try {
            final String semanticVersionPattern = "(\\d+)\\.(\\d+)(\\.(\\d+))?\\D*";
            final Matcher matcher = Pattern.compile(semanticVersionPattern).matcher(version);

            int major = 0;
            int minor = 0;
            int patch = 0;
            if (matcher.find()) {
                if (matcher.groupCount() > 0)
                    major = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1));
                if (matcher.groupCount() > 1)
                    minor = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
                if (matcher.groupCount() > 3)
                    patch = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
            }

            return FastColor.ARGB32.color(0, major, minor, patch);
        } catch (Exception e) {
            Wunderreich.LOGGER.error("Failed to parse Version '" + version + "'.");
            return 0;
        }
    }

    public static int major(int version) {
        return FastColor.ARGB32.red(version);
    }

    public static int minor(int version) {
        return FastColor.ARGB32.green(version);
    }

    public static int patch(int version) {
        return FastColor.ARGB32.blue(version);
    }

    public static Version fromInt(int version) {
        return new Version(major(version), minor(version), patch(version));
    }

    public int toInt() {
        return toInt(version);
    }

    public boolean isLargerThan(Version v2) {
        return toInt() > v2.toInt();
    }

    public boolean isLargerOrEqualVersion(Version v2) {
        return toInt() >= v2.toInt();
    }

    public boolean isLargerThan(String v2) {
        return toInt() > toInt(v2);
    }

    public boolean isLargerOrEqualVersion(String v2) {
        return toInt() >= toInt(v2);
    }

    public boolean isLessThan(Version v2) {
        return toInt() < v2.toInt();
    }

    public boolean isLessOrEqualVersion(Version v2) {
        return toInt() <= v2.toInt();
    }

    public boolean isLessThan(String v2) {
        return toInt() < toInt(v2);
    }

    public boolean isLessOrEqualVersion(String v2) {
        return toInt() <= toInt(v2);
    }

    @Override
    public String toString() {
        return version;
    }
}
