package net.dirtcraft.dirtlauncher.data.Minecraft;

import org.apache.commons.lang3.SystemUtils;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Library {
    private static final String OS_KEY = getOsKey();
    public Library(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    final String name;
    final Downloads downloads;
    //final ExtractInfo extract; <-- usually to just remove META-INF.

    public boolean isRequired(){
        return downloads.rules == null || downloads.rules.stream().allMatch(Rule::canDownload);
    }

    public boolean notRequired(){
        return !isRequired();
    }

    public Optional<Download> getArtifact(){
        return Optional.ofNullable(downloads.artifact);
    }

    public Optional<Download> getNative(){
        if (downloads.classifiers == null) return Optional.empty();
        else return Optional.ofNullable(downloads.classifiers.get(OS_KEY));
    }

    private static String getOsKey(){
        if (SystemUtils.IS_OS_WINDOWS) return  "natives-windows";
        else if (SystemUtils.IS_OS_MAC) return  "natives-osx";
        else if (SystemUtils.IS_OS_LINUX) return  "natives-linux";
        else throw new InvalidStateException("Could not determine operating system.");
    }

    public static class Downloads{
        public Downloads(int i) throws InstantiationException{
            throw new InstantiationException("Gson data class. Not to be manually created.");
        }
        final List<Rule> rules;
        final Download artifact;
        final Map<String, Download> classifiers;

    }
}
