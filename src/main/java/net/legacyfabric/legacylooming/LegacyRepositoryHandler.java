package net.legacyfabric.legacylooming;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.PluginAware;

public class LegacyRepositoryHandler implements Plugin<PluginAware> {
    @Override
    public void apply(PluginAware pluginAware) {
        if (pluginAware instanceof Settings settings) {
            declareRepositories(settings.getDependencyResolutionManagement().getRepositories());

            settings.getGradle().getPluginManager().apply(LegacyRepositoryHandler.class);
        } else if (pluginAware instanceof Project project) {
            if (project.getGradle().getPlugins().hasPlugin(LegacyRepositoryHandler.class)) {
                return;
            }

            declareRepositories(project.getRepositories());
        } else if (pluginAware instanceof Gradle) {
            return;
        } else {
            throw new IllegalArgumentException("Expected target to be a Project or Settings, but was a " + pluginAware.getClass());
        }
    }

    private void declareRepositories(RepositoryHandler repositories) {
        repositories.maven(repo -> {
            repo.setName("Legacy Fabric");
            repo.setUrl(Constants.MAVEN);
            repo.content(content -> {
                content.includeGroup("net.legacyfabric");
                content.includeGroupByRegex("net.legacyfabric.*");
                content.includeGroup("org.lwjgl.lwjgl");
            });
        });
    }
}
