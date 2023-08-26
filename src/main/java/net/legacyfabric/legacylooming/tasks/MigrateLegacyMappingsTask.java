package net.legacyfabric.legacylooming.tasks;

import net.fabricmc.loom.configuration.DependencyInfo;
import net.fabricmc.loom.task.MigrateMappingsTask;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Always rerun this tasks.")
public abstract class MigrateLegacyMappingsTask extends MigrateMappingsTask {
    private String mappings;

    @Override
    @Option(option = "mappings", description = "Target mappings")
    public void setMappings(String mappings) {
        super.setMappings(mappings);
        this.mappings = mappings;
    }

    @Override
    @TaskAction
    public void doTask() throws Throwable {
        Project project = getProject();

        DependencyInfo minecraftDep = DependencyInfo.create(project, Constants.Configurations.MINECRAFT);
        String minecraftDepString = minecraftDep.getDepString();
        String minecraftVersion = minecraftDepString.substring(minecraftDepString.lastIndexOf(':') + 1);

        DependencyInfo mappingsDep = DependencyInfo.create(project, Constants.Configurations.MAPPINGS);
        String mappingsDepString = mappingsDep.getDepString();

        this.checkForLegacyYarn(project, mappingsDepString, minecraftVersion);

        super.doTask();
    }

    private void checkForLegacyYarn(Project project, String mappingsDepString, String minecraftVersion) {
        if (mappingsDepString.startsWith("net.legacyfabric:yarn")) {
            project.getLogger().info("Detected legacyfabric yarn mappings, adjusting target mappings");

            String yarnBuild;
            if (mappings.matches("^[0-9.]+\\+build\\.[0-9]+$")) {
                yarnBuild = mappings;
            }
            else if (mappings.matches("^[0-9]+$")) {
                yarnBuild = minecraftVersion + "+build." + mappings;
            }
            else {
                project.getLogger().info("The provided target mappings aren't a yarn build, skipping");
                return;
            }

            setMappings("net.legacyfabric:yarn:" + yarnBuild + ":v2");
        }
    }
}
