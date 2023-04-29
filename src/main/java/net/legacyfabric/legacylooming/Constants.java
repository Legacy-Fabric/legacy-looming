package net.legacyfabric.legacylooming;

public class Constants {
    public static final String MAVEN = "https://repo.legacyfabric.net/repository/legacyfabric/";
    public static final String INTERMEDIARY_PROPERTY = "legacyfabric:intermediary";

    public static String getIntermediaryURL(int version) {
        return MAVEN + "net/legacyfabric/" + switch (version) {
            case 2 -> "v2/intermediary";
            default -> "intermediary";
        } + "/%1$s/intermediary-%1$s-v2.jar";
    }

    public static String getYarnGroup(int version) {
        return "net.legacyfabric" + switch (version) {
            case 2 -> ".v2:yarn";
            default -> ":yarn";
        };
    }
}
