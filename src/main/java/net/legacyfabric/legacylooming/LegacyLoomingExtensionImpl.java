package net.legacyfabric.legacylooming;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class LegacyLoomingExtensionImpl implements LegacyLoomingExtensionAPI {
    protected final Property<Integer> intermediaryVersion;

    public LegacyLoomingExtensionImpl(Project project) {
        this.intermediaryVersion = project.getObjects().property(Integer.class).convention(1);
    }
    @Override
    public Property<Integer> getIntermediaryVersion() {
        return this.intermediaryVersion;
    }
}
