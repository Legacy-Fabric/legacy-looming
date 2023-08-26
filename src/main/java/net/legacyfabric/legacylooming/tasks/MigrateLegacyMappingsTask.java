package net.legacyfabric.legacylooming.tasks;

import net.fabricmc.loom.configuration.DependencyInfo;
import net.fabricmc.loom.task.MigrateMappingsTask;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Always rerun this tasks.")
public abstract class MigrateLegacyMappingsTask extends MigrateMappingsTask {
    private Logger logger;
    private String targetMappings;

    @Override
    @Option(option = "mappings", description = "Target mappings")
    public void setMappings(String mappings) {
        super.setMappings(mappings);
        this.targetMappings = mappings;
    }

    @Override
    @TaskAction
    public void doTask() throws Throwable {
        Project project = getProject();
        logger = project.getLogger();

        try {
            DependencyInfo minecraftDep = DependencyInfo.create(project, Constants.Configurations.MINECRAFT);
            String minecraftDepString = minecraftDep.getDepString();
            String minecraftVersion = minecraftDepString.substring(minecraftDepString.lastIndexOf(':') + 1);

            DependencyInfo mappingsDep = DependencyInfo.create(project, Constants.Configurations.MAPPINGS);
            String mappingsDepString = mappingsDep.getDepString();

            this.checkForLegacyYarn(mappingsDepString, minecraftVersion);
        } catch (Exception e) {
            logger.warn("Failed to check if legacyfabric yarn mappings are used!", e);
        }

        super.doTask();
    }

    private void checkForLegacyYarn(String mappingsDepString, String minecraftVersion) {
        if (mappingsDepString.startsWith("net.legacyfabric:yarn")) {
            logger.info("Detected legacyfabric yarn mappings, adjusting target mappings");
            this.adjustTargetMappings("net.legacyfabric:yarn", minecraftVersion);
        }
        else if (mappingsDepString.startsWith("net.legacyfabric.v2:yarn")) {
            logger.info("Detected legacyfabric yarn v2 mappings, adjusting target mappings");
            this.adjustTargetMappings("net.legacyfabric.v2:yarn", minecraftVersion);
        }
    }

    private void adjustTargetMappings(String yarnPath, String minecraftVersion) {
        String yarnBuild;
        if (targetMappings.matches("^[0-9.]+\\+build\\.[0-9]+$")) {
            yarnBuild = targetMappings;
        }
        else if (targetMappings.matches("^[0-9]+$")) {
            yarnBuild = minecraftVersion + "+build." + targetMappings;
        }
        else {
            logger.info("The provided target mappings aren't a yarn build, skipping");
            return;
        }

        this.setMappings(yarnPath + ":" + yarnBuild + ":v2");
    }
}
