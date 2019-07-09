package net.dirtcraft.dirtlauncher.backend.Utils;

import com.google.common.base.Strings;

public class Utility {

    public static boolean isEmptyOrNull(String... strings) {
        for (String string : strings) {
            if (Strings.isNullOrEmpty(string)) return true;
        }
        return false;
    }

}
