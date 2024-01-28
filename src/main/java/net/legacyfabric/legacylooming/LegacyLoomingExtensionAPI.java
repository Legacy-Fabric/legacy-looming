package net.legacyfabric.legacylooming;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public interface LegacyLoomingExtensionAPI {
    static LegacyLoomingExtensionAPI get(Project project) {
        return project.getExtensions().findByType(LegacyLoomingExtensionAPI.class);
    }

    Property<Integer> getIntermediaryVersion();

    @Deprecated
    Property<Boolean> useLFIntermediary();
}
