package net.legacyfabric.legacylooming;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

/**
 * For removal in 1.3
 */
@Deprecated(forRemoval = true, since = "1.1")
public class LegacyFabricApiExtension {
    private final LegacyUtilsExtension utils;

    public LegacyFabricApiExtension(Project project) {
        this.utils = project.getExtensions().getByType(LegacyUtilsExtension.class);
    }

    @Deprecated(forRemoval = true, since = "1.1")
    public Dependency module(String moduleName, String fabricApiVersion) {
        return this.utils.apiModule(moduleName, fabricApiVersion);
    }

    @Deprecated(forRemoval = true, since = "1.1")
    public String moduleVersion(String moduleName, String fabricApiVersion) {
        return this.utils.apiModuleVersion(moduleName, fabricApiVersion);
    }
}
