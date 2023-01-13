package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

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
