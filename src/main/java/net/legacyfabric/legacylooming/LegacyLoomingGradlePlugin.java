package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
                processResources.doLast(task -> {
                    if (processResources.getOutputs().getFiles().getFiles().stream().anyMatch(file -> file.getName().equals("fabric.mod.json"))) {
                        processResources.getOutputs().getFiles().getFiles()
                                .stream().filter(file -> file.getName().equals("fabric.mod.json")).forEach(file -> {
                                    try {
                                        var object = LoomGradlePlugin.GSON.fromJson(new InputStreamReader(new FileInputStream(file)), JsonObject.class);

                                        if (!object.has("custom")) {
                                            object.add("custom", new JsonObject());
                                        }

                                        object.getAsJsonObject("custom")
                                                .addProperty(Constants.INTERMEDIARY_PROPERTY, extension.getIntermediaryVersion().get());

                                        var bytes = LoomGradlePlugin.GSON.toJson(object, JsonObject.class).getBytes(StandardCharsets.UTF_8);
                                        var writer = new OutputStreamWriter(new FileOutputStream(file));
                                        writer.write(bytes.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                });
            });
        }
    }
}
