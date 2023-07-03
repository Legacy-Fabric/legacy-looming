package net.legacyfabric.legacylooming.providers;

import net.fabricmc.loom.configuration.providers.minecraft.library.Library;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryContext;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryProcessor;
import net.fabricmc.loom.util.Platform;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class LWJGL2LibraryProcessor extends LibraryProcessor {
    public static final String VERSION = "2.9.4+legacyfabric.5";
    public LWJGL2LibraryProcessor(Platform platform, LibraryContext context) {
        super(platform, context);
    }

    @Override
    public ApplicationResult getApplicationResult() {
        if (!context.usesLWJGL3()) {

            return ApplicationResult.MUST_APPLY;
        }

        return ApplicationResult.DONT_APPLY;
    }

    @Override
    public Predicate<Library> apply(Consumer<Library> dependencyConsumer) {
        return library -> {
            if (library.group().equals("org.lwjgl.lwjgl")) {
                if (library.name().equals("lwjgl-platform") && library.classifier() == null) return false;
                final Library.Target target = library.target() == Library.Target.NATIVES ? Library.Target.NATIVES : Library.Target.RUNTIME;
                final Library upgradedLibrary = library.withVersion(VERSION).withTarget(target);
                dependencyConsumer.accept(upgradedLibrary);

                return target != Library.Target.NATIVES;
            }

            return true;
        };
    }

    @Override
    public void applyRepositories(RepositoryHandler repositories) {
        repositories.exclusiveContent(repository -> {
            repository.forRepositories(repositories.findByName("Legacy Fabric"));
            repository.filter(filter -> {
                filter.includeGroup("org.lwjgl.lwjgl");
            });
        });
    }
}
