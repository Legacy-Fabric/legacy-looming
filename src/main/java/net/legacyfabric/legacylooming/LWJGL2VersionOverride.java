package net.legacyfabric.legacylooming;

import net.fabricmc.loom.configuration.providers.minecraft.MinecraftVersionMeta;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.Project;

import java.util.List;

public class LWJGL2VersionOverride {
    public static final String VERSION = "2.9.4+legacyfabric.4";
//    @Nullable
//    public static final String NATIVE_CLASSIFIER = getNativeClassifier();
    public static final List<String> DEPS = List.of(
        "org.lwjgl.lwjgl:lwjgl:" + VERSION,
        "org.lwjgl.lwjgl:lwjgl_util:" + VERSION
    );
    public static final List<String> NATIVE_DEPS = List.of(
            "org.lwjgl.lwjgl:lwjgl-platform:" + VERSION + ":natives-windows",
            "org.lwjgl.lwjgl:lwjgl-platform:" + VERSION + ":natives-osx",
            "org.lwjgl.lwjgl:lwjgl-platform:" + VERSION + ":natives-linux"
    );

    public static boolean overrideByDefault(MinecraftVersionMeta versionMeta) {
        if (OperatingSystem.CURRENT_OS.isUnknown() || OperatingSystem.CURRENT_ARCH.isUnknown()) {
            return false;
        }

        return versionMeta.libraries().stream()
                .anyMatch(library -> library.name().startsWith("org.lwjgl.lwjgl"));
    }

    public static void applyOverride(Project project) {
        DEPS.forEach(s -> project.getDependencies().add(Constants.Configurations.MINECRAFT_DEPENDENCIES, s));
        NATIVE_DEPS.forEach(s -> project.getDependencies().add(Constants.Configurations.MINECRAFT_NATIVES, s));
    }

//    public static String getNativeClassifier() {
//        return switch (OperatingSystem.CURRENT_OS) {
//            case windows -> "windows";
//            case macos -> "osx";
//            case linux -> "linux";
//            case freebsd, openbsd, unknown -> null;
//        };
//    }
}
