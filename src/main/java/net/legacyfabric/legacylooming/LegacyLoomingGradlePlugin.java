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
import org.gradle.api.provider.Provider;

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

            if (!OperatingSystem.CURRENT_OS.isWindows()) {
                LoomGradleExtension.get(project).getLibraryProcessors().add(LWJGL2LibraryProcessor::new);
            }

            var extension = project.getExtensions().create(LegacyLoomingExtensionAPI.class, "legacyLooming", LegacyLoomingExtensionImpl.class, project);

            project.getExtensions().getByType(LoomGradleExtensionAPI.class)
                    .setIntermediateMappingsProvider(LegacyFabricIntermediaryMappingsProvider.class, provider -> {
                        provider.getIntermediaryUrl()
                                .convention(extension.useLFIntermediary().map( val -> {
                                    if (val) {
                                        return extension.getIntermediaryVersion().map(Constants::getIntermediaryURL);
                                    } else {
                                        return project.provider(() -> "https://maven.fabricmc.net/net/fabricmc/intermediary/%1$s/intermediary-%1$s-v2.jar");
                                    }
                                }).map(Provider::get))
                                .finalizeValueOnRead();

                        provider.getNameProperty()
                                .convention(extension.getIntermediaryVersion().map(Constants::getIntermediaryName))
                                .finalizeValueOnRead();

                        provider.getRefreshDeps().set(project.provider(() -> LoomGradleExtension.get(project).refreshDeps()));
                    });

            project.getExtensions().create("legacy", LegacyUtilsExtension.class, project);

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

            // override loom's migrateMappings to fix issues
            var migrateTask = project.getTasks().replace("migrateMappings", MigrateLegacyMappingsTask.class);
            migrateTask.setDescription("Migrates mappings to a new version.");
            migrateTask.getOutputs().upToDateWhen(o -> false);
        }
    }
}
