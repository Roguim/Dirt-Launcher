package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.manifests.platforms.CurseManifest;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class UpdateCursePackTask implements IUpdateTask {

    private final Modpack pack;

    public UpdateCursePackTask(Modpack pack) {
        this.pack = pack;
    }

    @SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Files");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        final File modpackFolder = pack.getInstanceDirectory();
        final File modpackZip = new File(modpackFolder.getPath(), "modpack.zip");
        final File tempDir = new File(modpackFolder.getPath(), "temp");

        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Download Modpack Zip
        FileUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();

        // Update Progress
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        progressContainer.setNumMinorSteps(4);

        // Extract Modpack Zip
        new ZipFile(modpackZip).extractAll(tempDir.getPath());
        progressContainer.completeMinorStep();

        // Delete the Modpack Zip
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Sort out the files
        FileUtils.copyDirectory(new File(tempDir.getPath(), "overrides"), modpackFolder);
        File modsFolder = new File(modpackFolder.getPath(), "mods");
        File tempManifestFile = new File(tempDir, "manifest.json");
        File currentManifestFile = new File(modpackFolder, "manifest.json");
        CurseManifest oldManifest = FileUtils.parseJsonUnchecked(currentManifestFile, new TypeToken<CurseManifest>() {});
        CurseManifest newManifest = FileUtils.parseJsonUnchecked(tempManifestFile, new TypeToken<CurseManifest>() {});
        modsFolder.mkdirs();

        //Work out what changes need to be made to the mods and remove / add them.
        List<CurseManifest.CurseMetadataReference> toRemove = new ArrayList<>();
        List<CurseManifest.CurseMetadataReference> toInstall = newManifest.files.stream()
                .filter(file->oldManifest.files.stream().noneMatch(file::equals))
                .filter(file->file.required)
                .collect(Collectors.toList());

        for (CurseManifest.CurseMetadataReference oldFile : oldManifest.files) {
            Optional<CurseManifest.CurseMetadataReference> optNewFile = newManifest.files.stream()
                    .filter(oldFile::equals)
                    .findAny();
            if (optNewFile.isPresent()) {
                CurseManifest.CurseMetadataReference newFile = optNewFile.get();
                if (newFile.required == oldFile.required) continue;
                else if (newFile.required) toInstall.add(newFile);
                else toRemove.add(oldFile);
            } else {
                toRemove.add(oldFile);
            }
        }

        toRemove.stream()
                .map(m->m.getManifestAsync(threadService))
                .map(this::getFutureUnchecked)
                .map(m->m.fileName)
                .peek(fn->System.out.println("removed: " + fn))
                .map(m->new File(modsFolder, m))
                .forEach(File::delete);

        toInstall.stream()
                .map(m->m.getManifestAsync(threadService))
                .map(this::getFutureUnchecked)
                .peek(fn->System.out.println("added: " + fn.fileName))
                .forEach(m->m.downloadAsync(modsFolder, threadService));

        org.apache.commons.io.FileUtils.copyFile(tempManifestFile, currentManifestFile);

        // Delete the temporary files
        FileUtils.deleteDirectory(tempDir);
        progressContainer.completeMinorStep();
        progressContainer.completeMajorStep();
    }

    private  <T> T getFutureUnchecked(CompletableFuture<T> t){
        try {
            return t.get();
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public int getNumberSteps() {
        return 4;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
