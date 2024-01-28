package net.legacyfabric.legacylooming;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class LegacyLoomingExtensionImpl implements LegacyLoomingExtensionAPI {
    protected final Property<Integer> intermediaryVersion;

    @Deprecated
    protected final Property<Boolean> useLFIntermediary;

    public LegacyLoomingExtensionImpl(Project project) {
        this.intermediaryVersion = project.getObjects().property(Integer.class).convention(1);
        this.useLFIntermediary = project.getObjects().property(Boolean.class).convention(true);
    }
    @Override
    public Property<Integer> getIntermediaryVersion() {
        return this.intermediaryVersion;
    }

    @Deprecated
    @Override
    public Property<Boolean> useLFIntermediary() {
        return this.useLFIntermediary;
    }
}
