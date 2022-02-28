package net.dirtcraft.dirtlauncher.providers;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseModpackManifest;
import net.dirtcraft.dirtlauncher.lib.data.json.curse.CurseProject;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CurseProvider implements IPackProvider {
    @Override
    public boolean isOutdated(Modpack modpack) {
        return false;
    }

    public CompletableFuture<Optional<Modpack>> getModpackFromUrlAsync(URL url){
        return CompletableFuture.supplyAsync(()->getModpackFromUrl(url));
    }

    private Optional<Modpack> getModpackFromUrl(URL url){
        File tempFile = null;
        File tempDir = null;
        try {
            tempFile = File.createTempFile("Dirt-Launcher-", null);
            tempDir = new File(tempFile.getAbsolutePath().replace(".tmp", ""));
            CurseProject project = getProjectFromUrl(url).orElseThrow(InvalidParameterException::new);
            CompletableFuture<String> latestFile = project.getLatestFileUrl();
            project.getLatestFile(tempFile).execute().join();

            tempDir.mkdirs();
            ZipFile zipFile = new ZipFile(tempFile);
            zipFile.extractAll(tempDir.getPath());

            File tempManifest = new File(tempDir, "manifest.json");
            CurseModpackManifest manifest = JsonUtils.parseJson(tempManifest, CurseModpackManifest.class).orElseThrow(InvalidParameterException::new);

            return Optional.of(new Modpack(manifest, latestFile.join()));
        } catch (IOException e){
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (tempFile != null) tempFile.delete();
            if (tempDir != null) FileUtils.deleteDirectoryUnchecked(tempDir);
        }
    }

    public CompletableFuture<Boolean> getLatestFileFromUrl(URL url, File file){
        final Optional<CurseProject> project = getProjectFromUrl(url);
        final boolean $ = file.delete();
        if (project.isPresent()) return project.get().getLatestFile(file).execute().thenApply(v->file.exists());
        else return CompletableFuture.supplyAsync(()->false);
    }

    private Optional<CurseProject> getProjectFromUrl(URL url) {
        final String name = url.getPath().replaceAll("/minecraft/modpacks/(.*)/?", "$1");
        final String pattern = "^(?i)" + name.replaceAll("[-. ]", "") + "$";
        final Optional<ArrayList<CurseProject>> optListing = getProjectsMatching(name);
        Optional<CurseProject> optProject = optListing.flatMap(curseProjects -> curseProjects.stream()
                .filter(i -> i.name.replaceAll("[-. ]", "").matches(pattern))
                .findFirst());

        return optProject;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Optional<ArrayList<CurseProject>> getProjectsMatching(String name){
        try {
            final String query = Constants.CURSE_API_URL + "search?categoryId=0&gameId=432&gameVersion=&index=0&pageSize=2500&searchFilter=" + name + "&sectionId=4471&sort=0";
            return WebUtils.getGsonFromUrl(query, new TypeToken<ArrayList<CurseProject>>(){});
        } catch (Exception e){
            return Optional.empty();
        }
    }

}
