package net.legacyfabric.legacylooming;

public class Constants {
    public static final String MAVEN = "https://maven.legacyfabric.net/";
    public static final String INTERMEDIARY_PROPERTY = "Legacy-Fabric-Intermediary-Version";
    public static final String VERSION_PROPERTY = "Legacy-Looming-Version";

    public static String getIntermediaryURL(int version) {
        return MAVEN + "net/legacyfabric/" + switch (version) {
            case 2 -> "v2/";
            default -> "";
        } + "intermediary/%1$s/intermediary-%1$s-v2.jar";
    }

    public static String getYarnGroup(int version) {
        return "net.legacyfabric" + switch (version) {
            case 2 -> ".v2:yarn";
            default -> ":yarn";
        };
    }
}
