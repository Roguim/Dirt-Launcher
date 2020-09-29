package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.google.common.reflect.TypeToken;
import com.therandomlabs.utils.io.NetUtils;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.data.Curse.CurseFile;
import net.dirtcraft.dirtlauncher.data.Curse.CurseMetaFileReference;
import net.dirtcraft.dirtlauncher.data.Curse.CurseModpackManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
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
    private final File modpackFolder;
    private final File modpackZip;
    private final File tempDir;

    public UpdateCursePackTask(Modpack pack) {
        this.pack = pack;
        this.modpackFolder = pack.getInstanceDirectory();
        this.modpackZip = new File(modpackFolder, "modpack.zip");
        this.tempDir = new File(modpackFolder, "temp");
        if (tempDir.exists()) FileUtils.deleteDirectoryUnchecked(tempDir);
    }

    @SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, ConfigurationManager config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Files");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Download Modpack Zip
        WebUtils.copyURLToFile(NetUtils.getRedirectedURL(new URL(pack.getLink())).toString().replace("%2B", "+"), modpackZip);
        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText(String.format("Extracting %s Files", pack.getName()));
        progressContainer.setNumMinorSteps(3);

        // Extract Modpack Zip
        new ZipFile(modpackZip).extractAll(tempDir.getPath());
        progressContainer.completeMinorStep();

        // Delete the Modpack Zip
        modpackZip.delete();
        progressContainer.completeMinorStep();

        // Sort out the files
        FileUtils.copyDirectory(new File(tempDir, "overrides"), modpackFolder);
        File modsFolder = new File(modpackFolder, "mods");
        File tempManifestFile = new File(tempDir, "manifest.json");
        File currentManifestFile = new File(modpackFolder, "manifest.json");
        CurseModpackManifest oldManifest = JsonUtils.parseJsonUnchecked(currentManifestFile, new TypeToken<CurseModpackManifest>() {});
        CurseModpackManifest newManifest = JsonUtils.parseJsonUnchecked(tempManifestFile, new TypeToken<CurseModpackManifest>() {});
        modsFolder.mkdirs();
        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Calculating Changes");
        progressContainer.setNumMinorSteps(oldManifest.files.size() + newManifest.files.size());

        //Work out what changes need to be made to the mods and remove / add them.
        List<CurseMetaFileReference> toRemove = new ArrayList<>();
        List<CurseMetaFileReference> toInstall = newManifest.files.parallelStream()
                .peek(e->progressContainer.completeMinorStep())
                .filter(file->oldManifest.files.stream().noneMatch(file::equals))
                .filter(file->file.required)
                .collect(Collectors.toList());

        for (CurseMetaFileReference oldFile : oldManifest.files) {
            progressContainer.completeMinorStep();
            Optional<CurseMetaFileReference> optNewFile = newManifest.files.stream()
                    .filter(oldFile::equals)
                    .findAny();
            if (optNewFile.isPresent()) {
                CurseMetaFileReference newFile = optNewFile.get();
                if (newFile.required == oldFile.required) continue;
                else if (newFile.required) toInstall.add(newFile);
                else toRemove.add(oldFile);
            } else toRemove.add(oldFile);
        }

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Removing Old Mods");
        progressContainer.setNumMinorSteps(toRemove.size());

        //remove old mods
        toRemove.parallelStream()
                .map(m->m.getManifestAsync(threadService))
                .map(this::getFutureUnchecked)
                .map(CurseFile::getFileName)
                //.peek(fn->System.out.println("removed: " + fn))
                .map(m->new File(modsFolder, m))
                .peek(File::delete)
                .forEach(t->progressContainer.completeMinorStep());

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Adding New Mods");
        progressContainer.setNumMinorSteps(toInstall.size());

        //install new mods
        toInstall.stream()
                .map(m->m.getManifestAsync(threadService))
                .map(this::getFutureUnchecked)
                //.peek(fn->System.out.println("added: " + fn.fileName))
                .forEach(m->m.downloadAsync(modsFolder, threadService).whenComplete((t,e)->progressContainer.completeMinorStep()));


        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Cleaning Up");
        progressContainer.setNumMinorSteps(1);
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
        return 6;
    }

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
