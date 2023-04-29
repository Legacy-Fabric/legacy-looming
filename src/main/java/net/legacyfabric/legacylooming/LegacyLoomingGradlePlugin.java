package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.HashMap;
import java.util.Objects;

public class LegacyLoomingGradlePlugin implements Plugin<PluginAware> {
    public static final String VERSION = Objects.requireNonNullElse(LegacyLoomingGradlePlugin.class.getPackage().getImplementationVersion(), "0.0.0+unknown");
    @Override
    public void apply(PluginAware target) {
        target.getPlugins().apply(LegacyRepositoryHandler.class);

        target.apply(ImmutableMap.of("plugin", "fabric-loom"));

        if (target instanceof Project project) {
            project.getLogger().lifecycle("Legacy Looming: " + VERSION);

            var extension = project.getExtensions().create(LegacyLoomingExtensionAPI.class, "legacyLooming", LegacyLoomingExtensionImpl.class, project);
            project.getExtensions().getByType(LoomGradleExtensionAPI.class).getIntermediaryUrl()
                    .set(Constants.getIntermediaryURL(extension.getIntermediaryVersion().get()));

            project.getExtensions().create("legacy", LegacyUtilsExtension.class, project);
            project.getExtensions().create("legacyFabricApi", LegacyFabricApiExtension.class, project);

            project.getTasks().withType(ProcessResources.class).configureEach(processResources -> {
                processResources.filesMatching("fabric.mod.json", fileCopyDetails -> {
                    var map = new HashMap<String, Object>();
                    map.put("legacyfabric:intermediary", extension.getIntermediaryVersion().get());
                    fileCopyDetails.expand(map);
                });
            });
        }
    }
}
