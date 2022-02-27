package net.dirtcraft.dirtlauncher.data.FTB;

import net.dirtcraft.dirtlauncher.configuration.Constants;

import javax.annotation.Nullable;

public enum FileTypes {
    MOD, CONFIG, SCRIPT, RESOURCE;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    @Nullable
    public FileTypes of(String fileType) {
        switch (fileType) {
            case "mod": return MOD;
            case "config": return CONFIG;
            case "script": return SCRIPT;
            case "resource": return RESOURCE;
        }
        if (Constants.DEBUG) System.out.println("Could not find file type of \"" + fileType + "\"");
        return null;
    }
}
