package net.legacyfabric.legacylooming.providers;

import net.fabricmc.loom.configuration.providers.minecraft.library.Library;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryContext;
import net.fabricmc.loom.configuration.providers.minecraft.library.LibraryProcessor;
import net.fabricmc.loom.util.Platform;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class LWJGL2LibraryProcessor extends LibraryProcessor {
    private static final String GROUP = "org.lwjgl.lwjgl";
    public static final String VERSION = "2.9.4+legacyfabric.5";
    public LWJGL2LibraryProcessor(Platform platform, LibraryContext context) {
        super(platform, context);
    }

    @Override
    public ApplicationResult getApplicationResult() {
        if (context.usesLWJGL3()) {
            return ApplicationResult.DONT_APPLY;
        }

        return ApplicationResult.MUST_APPLY;
    }

    @Override
    public Predicate<Library> apply(Consumer<Library> dependencyConsumer) {
        return library -> {
            if (library.is(GROUP) && library.name().startsWith("lwjgl")) {
                final Library.Target target = library.target() == Library.Target.NATIVES ? Library.Target.NATIVES : Library.Target.RUNTIME;
                final Library upgradedLibrary = library.withVersion(VERSION).withTarget(target);
                dependencyConsumer.accept(upgradedLibrary);
            }

            return true;
        };
    }
}
