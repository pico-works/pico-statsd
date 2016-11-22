package org.pico.statsd;

public class Tags {
    /**
     * Generate a suffix conveying the given tag list to the client
     */
    public static String tagString(final String[] tags, final String tagPrefix) {
        final StringBuilder sb;

        if (tagPrefix != null) {
            if ((tags == null) || (tags.length == 0)) {
                return tagPrefix;
            }

            sb = new StringBuilder(tagPrefix);
            sb.append(",");
        } else {
            if ((tags == null) || (tags.length == 0)) {
                return "";
            }

            sb = new StringBuilder("|#");
        }

        for (int n=tags.length - 1; n>=0; n--) {
            sb.append(tags[n]);

            if (n > 0) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
