package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.task.AbstractRemapJarTask;
import net.fabricmc.loom.util.ZipUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;

import static net.fabricmc.loom.task.AbstractRemapJarTask.MANIFEST_PATH;

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
                    .set(extension.getIntermediaryVersion().map(it->Constants.getIntermediaryURL(it)));

            project.getExtensions().create("legacy", LegacyUtilsExtension.class, project);
            project.getExtensions().create("legacyFabricApi", LegacyFabricApiExtension.class, project);

            if (LWJGL2VersionOverride.overrideByDefault(((LoomGradleExtension) project.getExtensions()
                    .getByType(LoomGradleExtensionAPI.class))
                    .getMinecraftProvider().getVersionInfo())) {
                project.getConfigurations().forEach(conf -> {
                    conf.getDependencies().removeIf(dependency -> Objects.equals(dependency.getGroup(), "org.lwjgl.lwjgl"));
                });
                LWJGL2VersionOverride.applyOverride(project);
            }

            project.getTasks().configureEach(task -> {
                if (task instanceof AbstractRemapJarTask remapJarTask) {
                    remapJarTask.doLast(task1 -> {
                        try {
                            ZipUtils.transform(remapJarTask.getArchiveFile().get().getAsFile().toPath(), Map.of(MANIFEST_PATH, bytes -> {
                                var manifest = new Manifest(new ByteArrayInputStream(bytes));

                                var attributes = manifest.getMainAttributes();
                                attributes.putValue(Constants.VERSION_PROPERTY, VERSION);
                                attributes.putValue(Constants.INTERMEDIARY_PROPERTY, extension.getIntermediaryVersion().get().toString());

                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                manifest.write(out);
                                return out.toByteArray();
                            }));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        }
    }
}
