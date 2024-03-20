package net.legacyfabric.legacylooming;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.task.AbstractRemapJarTask;
import net.fabricmc.loom.util.ZipUtils;
import net.legacyfabric.legacylooming.providers.LWJGL2LibraryProcessor;
import net.legacyfabric.legacylooming.providers.LegacyFabricIntermediaryMappingsProvider;
import net.legacyfabric.legacylooming.tasks.MigrateLegacyMappingsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;

public class LegacyLoomingGradlePlugin implements Plugin<PluginAware> {
    public static final String VERSION = Objects.requireNonNullElse(LegacyLoomingGradlePlugin.class.getPackage().getImplementationVersion(), "0.0.0+unknown");

    @Override
    public void apply(PluginAware target) {
        target.getPlugins().apply(LegacyRepositoryHandler.class);

        target.apply(ImmutableMap.of("plugin", "fabric-loom"));

        if (target instanceof Project project) {
            project.getLogger().lifecycle("Legacy Looming: " + VERSION);

            LoomGradleExtension loom = LoomGradleExtension.get(project);

            addLibraryProcessors(loom);

            var extension = project.getExtensions().create(LegacyLoomingExtensionAPI.class, "legacyLooming", LegacyLoomingExtensionImpl.class, project);

            project.getExtensions().create("legacy", LegacyUtilsExtension.class, project);

            setIntermediary(project, extension, loom);
            setManifestAttributes(project, extension);
            overrideMigrateTask(project);
        }
    }

    private static void addLibraryProcessors(LoomGradleExtension loom) {
        loom.getLibraryProcessors().add(LWJGL2LibraryProcessor::new);
    }

    private static void setIntermediary(Project project, LegacyLoomingExtensionAPI api, LoomGradleExtensionAPI loom) {
        loom.setIntermediateMappingsProvider(LegacyFabricIntermediaryMappingsProvider.class,
            provider -> provider.configure(project, api, loom));
    }

    private static void setManifestAttributes(Project project, LegacyLoomingExtensionAPI extension) {
        project.getTasks().configureEach(task -> {
            if (task instanceof AbstractRemapJarTask remapJarTask) {
                remapJarTask.doLast(task1 -> {
                    try {
                        ZipUtils.transform(remapJarTask.getArchiveFile().get().getAsFile().toPath(), Map.of("META-INF/MANIFEST.MF", bytes -> {
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

    private static void overrideMigrateTask(Project project) {
        // override loom's migrateMappings to fix issues
        var migrateTask = project.getTasks().replace("migrateMappings", MigrateLegacyMappingsTask.class);
        migrateTask.setDescription("Migrates mappings to a new version.");
        migrateTask.getOutputs().upToDateWhen(o -> false);
    }
}
