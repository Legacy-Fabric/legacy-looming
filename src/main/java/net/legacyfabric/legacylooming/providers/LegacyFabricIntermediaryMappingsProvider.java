package net.legacyfabric.legacylooming.providers;

import net.fabricmc.loom.configuration.providers.mappings.IntermediaryMappingsProvider;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public abstract class LegacyFabricIntermediaryMappingsProvider extends IntermediaryMappingsProvider {
    public abstract Property<String> getNameProperty();

    @Override
    public @NotNull String getName() {
        return this.getNameProperty().get();
    }
}
