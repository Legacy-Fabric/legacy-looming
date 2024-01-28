package net.legacyfabric.legacylooming.providers;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import net.fabricmc.loom.configuration.providers.mappings.IntermediaryMappingsProvider;
import net.legacyfabric.legacylooming.Constants;
import net.legacyfabric.legacylooming.LegacyLoomingExtensionAPI;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public abstract class LegacyFabricIntermediaryMappingsProvider extends IntermediaryMappingsProvider {
    private String name;
    private String defaultUrl = "";

    @Override
    public @NotNull String getName() {
        String defaultUrl = this.defaultUrl;
        String url = getIntermediaryUrl().get();

        if (!defaultUrl.equals(url)) {
            // make sure the name is changed when the user defines a
            // custom intermediary url, to ensure the default cache
            // file does not get corrupted with other intermediaries
            return name += "-" + Integer.toHexString(url.hashCode());
        }

        return this.name;
    }

    public void configure(Project project, LegacyLoomingExtensionAPI api, LoomGradleExtensionAPI loom) {
        this.name = api.getIntermediaryVersion().map(Constants::getIntermediaryName).get();
        this.defaultUrl = loom.getIntermediaryUrl().get();

        this.getIntermediaryUrl()
            .convention(api.getIntermediaryVersion().map(Constants::getIntermediaryURL))
            .finalizeValueOnRead();

        this.getRefreshDeps().set(project.provider(() -> LoomGradleExtension.get(project).refreshDeps()));
    }
}
