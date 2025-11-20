package net.legacyfabric.legacylooming.tasks;

import net.fabricmc.loom.configuration.DependencyInfo;
import net.fabricmc.loom.task.MigrateMappingsTask;
import net.fabricmc.loom.task.service.MigrateSourceCodeMappingsService;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Always rerun this tasks.")
public abstract class MigrateLegacyMappingsTask extends MigrateMappingsTask {
    private Logger logger;

    @Input
    public abstract Property<String> getTempMappings();

    public MigrateLegacyMappingsTask() {
        super();
        this.getTempMappings().convention(this.getMappings().flatMap(this::transformMappings));
        this.getMigrationServiceOptions().set(MigrateSourceCodeMappingsService.createOptions(this.getProject(), this.getTempMappings(), this.getInputDir(), this.getOutputDir()));
    }

    private Provider<String> transformMappings(String mappings) {
        Project project = getProject();
        logger = project.getLogger();

        try {
            DependencyInfo minecraftDep = DependencyInfo.create(project, Constants.Configurations.MINECRAFT);
            String minecraftDepString = minecraftDep.getDepString();
            String minecraftVersion = minecraftDepString.substring(minecraftDepString.lastIndexOf(':') + 1);

            DependencyInfo mappingsDep = DependencyInfo.create(project, Constants.Configurations.MAPPINGS);
            String mappingsDepString = mappingsDep.getDepString();

            return project.provider(() -> this.checkForLegacyYarn(mappingsDepString, minecraftVersion, mappings));
        } catch (Exception e) {
            logger.warn("Failed to check if legacyfabric yarn mappings are used!", e);
        }

        return project.provider(() -> mappings);
    }

    private String checkForLegacyYarn(String mappingsDepString, String minecraftVersion, String original) {
        if (mappingsDepString.startsWith("net.legacyfabric:yarn")) {
            logger.info("Detected legacyfabric yarn mappings, adjusting target mappings");
            return this.adjustTargetMappings("net.legacyfabric:yarn", minecraftVersion, original);
        }
        else if (mappingsDepString.startsWith("net.legacyfabric.v2:yarn")) {
            logger.info("Detected legacyfabric yarn v2 mappings, adjusting target mappings");
            return this.adjustTargetMappings("net.legacyfabric.v2:yarn", minecraftVersion, original);
        }

        return original;
    }

    private String adjustTargetMappings(String yarnPath, String minecraftVersion, String original) {
        String yarnBuild;
        if (original.matches("^[0-9.]+\\+build\\.[0-9]+$")) {
            yarnBuild = original;
        }
        else if (original.matches("^[0-9]+$")) {
            yarnBuild = minecraftVersion + "+build." + original;
        }
        else {
            logger.info("The provided target mappings aren't a yarn build, skipping");
            return original;
        }

        return yarnPath + ":" + yarnBuild + ":v2";
    }
}
