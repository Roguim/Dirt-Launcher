package net.dirtcraft.dirtlauncher.lib.data.json.forge;

import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.FileTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class LegacyForgeManifest implements ForgeVersion {
    public Install install;
    public VersionInfo versionInfo;

    @Override
    public String getFileName() {
        return "install_profile.json";
    }

    @Override
    public Stream<FileTask> getClientLibraries(File folder, ZipFile installer) {
        if (versionInfo == null) return null;
        Stream<FileTask> jar = install == null? Stream.empty() : install.getJar(folder);
        return Stream.concat(jar, versionInfo.getDownloads(folder));
    }

    @Override
    public Optional<ForgeInstallManifest> getPostProcess(ZipFile jar) {
        return Optional.empty();
    }

    @Override
    public String getMainClass() {
        return versionInfo == null? null : versionInfo.mainClass;
    }

    @Override
    public String getReleaseType() {
        return versionInfo == null? null : versionInfo.type;
    }

    @Override
    public String getForgeVersion() {
        return versionInfo == null? null : versionInfo.id;
    }

    @Override
    public String getMinecraftVersion() {
        return versionInfo == null? null : versionInfo.inheritsFrom;
    }

    @Override
    public String[] getMinecraftArgs() {
        return versionInfo == null? new String[0] : versionInfo.getArgs();
    }

    @Override
    public String[] getRuntimeArgs() {
        return new String[0];
    }

    public static class Install {
        String filePath;
        String path;

        public Stream<FileTask> getJar(File folder) {
            try {
                String maven = "https://maven.minecraftforge.net/";
                String[] artifact = path.split(":");
                artifact[0] = artifact[0].replace('.', '/');
                String qualified = String.join("/", artifact) + '/' + filePath;
                return Stream.of(new DownloadTask(new URL(maven + qualified), new File(folder, qualified)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //https://maven.minecraftforge.net/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10-universal.jar
    //https://maven.minecraftforge.net/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10/forge-1.7.10-10.13.4.1614-1.7.10.jar

    public static class VersionInfo{
        String mainClass;
        String id;
        String inheritsFrom;
        String type;
        String minecraftArguments;
        List<Download> libraries;

        public Stream<FileTask> getDownloads(File folder) {
            return libraries.stream()
                    .filter(s->!s.name.startsWith("net.minecraftforge:forge:"))
                    .map(d->d.getTask(folder));
        }

        public String[] getArgs() {
            String[] complete = minecraftArguments.split(" ");
            String[] arr = new String[complete.length];
            int j = 0;
            for (int i = 0; i < complete.length; i++) {
                String key = complete[i];
                String val = i+1 < complete.length? complete[++i] : null;
                if (val == null) {
                    arr[j++] = key;
                } else if (!val.matches("\\$\\{.+}")) {
                    arr[j] = key;
                    arr[j + 1] = val;
                    j += 2;
                }
            }
            return Arrays.copyOf(arr, j);
        }
    }

    public static class Download {
        String name;
        String url;

        public FileTask getTask(File folder) {
            try {
                String maven = url == null ? "https://libraries.minecraft.net/" : url;
                String[] artifact = name.split(":");
                artifact[0] = artifact[0].replace('.', '/');
                String qualified = String.format("%1$s/%2$s/%3$s/%2$s-%3$s.jar", (Object[]) artifact);
                URL url = new URL(maven + qualified);
                return new DownloadTask(url, new File(folder, qualified));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
