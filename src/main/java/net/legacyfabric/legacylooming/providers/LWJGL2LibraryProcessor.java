package net.legacyfabric.legacylooming.providers;

import net.fabricmc.loom.configuration.providers.minecraft.library.Library;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryContext;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryProcessor;
import net.fabricmc.loom.util.Platform;
import net.legacyfabric.legacylooming.OperatingSystem;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class LWJGL2LibraryProcessor extends LibraryProcessor {
    public static final String VERSION = "2.9.4+legacyfabric.5";
    private static boolean applied = false;
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
            if (!applied) {
                applied = true;
                final Library[] libs = new Library[]{
                        Library.fromMaven("org.lwjgl.lwjgl:lwjgl-platform:" + VERSION + ":" + getNativeClassifier(), Library.Target.NATIVES),
                        Library.fromMaven("org.lwjgl.lwjgl:lwjgl_util:" + VERSION, Library.Target.RUNTIME),
                        Library.fromMaven("org.lwjgl.lwjgl:lwjgl:" + VERSION, Library.Target.RUNTIME)
                };

                for (Library lib : libs) {
                    dependencyConsumer.accept(lib);
                }
            }

            return !library.group().equals("org.lwjgl.lwjgl") || !library.name().equals("lwjgl-platform");
        };
    }

    @Override
    public void applyRepositories(RepositoryHandler repositories) {
//        repositories.exclusiveContent(repository -> {
//            repository.forRepositories(repositories.findByName("Legacy Fabric"));
//            repository.filter(filter -> {
//                filter.includeGroup("org.lwjgl.lwjgl");
//            });
//        });
    }

    public static String getNativeClassifier() {
        switch (OperatingSystem.CURRENT_OS) {
            case macos -> {
                return "natives-osx";
            }
            case linux, freebsd, openbsd -> {
                return "natives-linux";
            }
            default -> {
                return "natives-windows";
            }
        }
    }
}
