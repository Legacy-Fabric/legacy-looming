package net.legacyfabric.legacylooming;

public enum OperatingSystem {
    windows(new String[]{"windows", "Windows"}, Arch.i386, Arch.amd64, Arch.aarch64),
    macos(new String[]{"mac os x", "mac", "osx", "os x", "Mac OS X", "Mac", "OSX", "OS X", "darwin", "Darwin"}, Arch.amd64, Arch.aarch64),
    linux(new String[]{"linux", "Linux"}, Arch.i386, Arch.amd64, Arch.armhf, Arch.armel, Arch.aarch64, Arch.riscv64, Arch.ppc64el),
    freebsd(new String[]{"freebsd", "FreeBSD"}),
    openbsd(new String[]{"openbsd", "OpenBSD"}),
    unknown(new String[]{});

    public static final OperatingSystem CURRENT_OS = getOs();
    public static final Arch CURRENT_ARCH = CURRENT_OS.getArch();

    final String[] aliases;
    final Arch[] supportedArches;

    OperatingSystem(String[] aliases, Arch... supportedArches) {
        this.aliases = aliases;
        this.supportedArches = supportedArches;
    }
    OperatingSystem(String alias, Arch... supportedArches) {
        this(new String[]{alias}, supportedArches);
    }

    public boolean isUnknown() {
        return this == unknown;
    }

    public boolean isWindows() {
        return this == windows;
    }

    public boolean isMacOs() {
        return this == macos;
    }

    public boolean isLinux() {
        return this == linux;
    }

    public boolean isFreeBSD() {
        return this == freebsd;
    }

    public boolean isOpenBSD() {
        return this == openbsd;
    }

    public boolean isUnixLike() {
        return isMacOs() || isLinux() || isBSDLike();
    }

    public boolean isBSDLike() {
        return isFreeBSD() || isOpenBSD();
    }

    private Arch getArch() {
        for (Arch arch : supportedArches) {
            if (arch.match()) return arch;
        }

        boolean detected = false;

        for (Arch arch : Arch.values()) {
            if (arch.match()) {
                detected = true;
                System.out.println("Detected unsupported architecture " + arch.name() + " on operating system " + this.name());
                break;
            }
        }

        if (!detected) {
            System.out.println("Detected unknown and unsupported architecture: " + System.getProperty("os.arch"));
        }

        return Arch.unknown;
    }

    private boolean match() {
        String name = System.getProperty("os.name");

        for (String alias : aliases) {
            if (name.contains(alias)) return true;
        }

        return false;
    }

    private static OperatingSystem getOs() {
        for (OperatingSystem os: values()) {
            if (os.match()) return os;
        }

        System.out.println("Detected unknown and unsupported operating system: " + System.getProperty("os.name"));

        return OperatingSystem.unknown;
    }

    public static String getPlatformSuffix() {
        return "-" + CURRENT_OS.name() + "-" + CURRENT_ARCH.name();
    }

    enum Arch {
        i386(new String[]{"i386", "x86"}),
        amd64(new String[]{"amd64", "x86_64", "x64"}),
        armhf(new String[]{"armhf", "armv7"}),
        armel("armel"),
        aarch64(new String[]{"aarch64", "armv8", "arm64"}),
        ppc64el(new String[]{"ppc64el", "ppc64le"}),
        riscv64("riscv64"),
        unknown("");

        final String[] aliases;

        Arch(String[] aliases) {
            this.aliases = aliases;
        }
        Arch(String alias) {
            this(new String[]{alias});
        }

        public boolean isUnknown() {
            return this == unknown;
        }

        public boolean is64bit() {
            return this == amd64 || this == aarch64 || this == ppc64el || this == riscv64;
        }

        public boolean isArm() {
            return this == armhf || this == armel || this == aarch64;
        }

        public boolean isPPC() {
            return this == ppc64el;
        }

        public boolean isRiscv() {
            return this == riscv64;
        }

        public boolean match() {
            String name = System.getProperty("os.arch");

            for (String alias : aliases) {
                if (name.equals(alias)) return true;
            }

            return false;
        }
    }
}
