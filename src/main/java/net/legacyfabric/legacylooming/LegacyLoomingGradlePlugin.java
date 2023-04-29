package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

public class LegacyLoomingGradlePlugin implements Plugin<PluginAware> {
    @Override
    public void apply(PluginAware target) {
        target.getPlugins().apply(LegacyRepositoryHandler.class);

        target.apply(ImmutableMap.of("plugin", "fabric-loom"));

        if (target instanceof Project project) {
            var extension = project.getExtensions().create(LegacyLoomingExtensionAPI.class, "legacyLooming", LegacyLoomingExtensionImpl.class, project);
            project.getExtensions().getByType(LoomGradleExtensionAPI.class).getIntermediaryUrl()
                    .set(Constants.getIntermediaryURL(extension.getIntermediaryVersion().get()));

            project.getExtensions().create("legacyFabricApi", LegacyFabricApiExtension.class, project);
            project.getExtensions().create("legacy", LegacyUtilsExtension.class, project);
        }
    }
}
