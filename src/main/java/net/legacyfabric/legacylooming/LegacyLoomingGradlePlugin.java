package net.legacyfabric.legacylooming;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap;

public class LegacyLoomingGradlePlugin implements Plugin<PluginAware> {
    @Override
    public void apply(PluginAware target) {
        target.getPlugins().apply(LegacyRepositoryHandler.class);

        target.apply(ImmutableMap.of("plugin", "fabric-loom"));

        if (target instanceof Project project) {
            project.getExtensions().create("legacyFabricApi", LegacyFabricApiExtension.class, project);
        }
    }
}
