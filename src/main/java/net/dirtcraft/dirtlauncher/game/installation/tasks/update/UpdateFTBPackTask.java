package net.dirtcraft.dirtlauncher.game.installation.tasks.update;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.configuration.Manifests;
import net.dirtcraft.dirtlauncher.data.FTB.FTBFile;
import net.dirtcraft.dirtlauncher.data.FTB.FTBModpackManifest;
import net.dirtcraft.dirtlauncher.game.installation.ProgressContainer;
import net.dirtcraft.dirtlauncher.game.installation.tasks.IUpdateTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.InstallationStages;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.home.login.LoginBar;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackSelector;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class UpdateFTBPackTask implements IUpdateTask {

    private final Modpack pack;
    private final File modpackFolder;
    private final File tempDir;


    public UpdateFTBPackTask(Modpack pack) {
        this.pack = pack;
        this.modpackFolder = pack.getInstanceDirectory();
        this.tempDir = new File(modpackFolder, "temp");
        if (tempDir.exists()) FileUtils.deleteDirectoryUnchecked(tempDir);
    }

    @Override
    public int getNumberSteps() {
        return 5;
    }

    @SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
    @Override
    public void executeTask(ExecutorService threadService, ProgressContainer progressContainer, Config config) throws IOException {
        // Update Progress
        progressContainer.setProgressText("Downloading Modpack Manifest");
        progressContainer.setNumMinorSteps(2);

        // Prepare Folders
        modpackFolder.mkdirs();
        tempDir.mkdirs();

        progressContainer.completeMinorStep();

        // Sort out the files
        File modsFolder = new File(modpackFolder, "mods");
        File tempManifestFile = new File(tempDir, "manifest.json");
        File currentManifestFile = new File(modpackFolder, "manifest.json");

        // Check if old manifest is in use (Used to be a curse pack)
        JsonObject currentJson = JsonUtils.readJsonFromFile(tempManifestFile);
        if (currentJson != null) {
            if (currentJson.has("projectID") || currentJson.has("overrides")) {
                Manifests.INSTANCE.remove(pack);
                try {
                    FileUtils.deleteDirectory(pack.getInstanceDirectory());
                } catch (IOException exception){
                    exception.printStackTrace();
                }
                if (pack.isFavourite()) pack.toggleFavourite();
                LoginBar loginBar = Main.getHome().getLoginBar();
                PackSelector packSelector = new PackSelector(pack);
                loginBar.setActivePackCell(packSelector);
                Install.getStage().ifPresent(Stage::close);
                packSelector.launchInstallScene();
                packSelector.getModpack().install();
                Main.getHome().update();
                return;
            }
        }

        FTBModpackManifest oldManifest = JsonUtils.parseJsonUnchecked(currentManifestFile, new TypeToken<FTBModpackManifest>() {});
        FTBModpackManifest newManifest = JsonUtils.parseJsonUnchecked(tempManifestFile, new TypeToken<FTBModpackManifest>() {});
        modsFolder.mkdirs();
        progressContainer.completeMinorStep();

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Calculating Changes");
        progressContainer.setNumMinorSteps(oldManifest.files.size() + newManifest.files.size());

        // Work out what changes need to be made to the mods and remove / add them.
        List<FTBFile> toRemove = new ArrayList<>();
        List<FTBFile> toInstall = newManifest.files.parallelStream()
                .peek(e -> progressContainer.completeMinorStep())
                .filter(file -> oldManifest.files.stream().noneMatch(oldFile -> oldFile.sha1.equals(file.sha1)))
                .filter(file -> !file.serveronly)
                .collect(Collectors.toList());

        for (FTBFile oldFile : oldManifest.files) {
            progressContainer.completeMinorStep();
            Optional<FTBFile> optionalNewFile = newManifest.files.stream()
                    .filter(newFile -> newFile.sha1.equals(oldFile.sha1))
                    .findAny();
            if (optionalNewFile.isPresent()) {
                FTBFile newFile = optionalNewFile.get();
                if (!newFile.serveronly == !oldFile.serveronly) continue;
                if (!newFile.serveronly) toInstall.add(newFile);
                else toRemove.add(oldFile);
            } else toRemove.add(oldFile);
        }

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Removing Old Mods");
        progressContainer.setNumMinorSteps(toRemove.size());

        // Remove Old Mods
        toRemove.parallelStream()
                .map(mod -> mod.name)
                .map(mod -> new File(modsFolder, mod))
                .peek(File::delete)
                .forEach(t -> progressContainer.completeMinorStep());

        // Update Progress
        progressContainer.completeMajorStep();
        progressContainer.setProgressText("Adding New Mods");
        progressContainer.setNumMinorSteps(toInstall.stream().mapToInt(file -> file.size).sum());

        // Install New Mods
        for (FTBFile mod : toInstall) mod
                .downloadAsync(modsFolder, threadService)
                .whenComplete((t,e) -> progressContainer.addMinorStepsCompleted(mod.size));

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

    @Override
    public InstallationStages getRequiredStage() {
        return InstallationStages.INSTALL;
    }
}
