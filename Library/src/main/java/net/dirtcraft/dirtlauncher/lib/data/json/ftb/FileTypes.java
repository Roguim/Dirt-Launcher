package net.dirtcraft.dirtlauncher.lib.data.json.ftb;

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
        System.out.println("Could not find file type of \"" + fileType + "\"");
        return null;
    }
}
