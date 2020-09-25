package net.dirtcraft.dirtlauncher.configuration;

import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;

public class Manifests {
    public static final InstanceManifest INSTANCE = new InstanceManifest();
    public static final VersionManifest VERSION = new VersionManifest();
    public static final ForgeManifest FORGE = new ForgeManifest();
}
