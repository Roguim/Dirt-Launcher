/*
 * Installer
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.dirtcraft.dirtlauncher.utils.forge;

import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.Minecraft.Library;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadManager;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadLocal;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;

import java.io.File;
import java.net.URL;
import java.util.*;

public class ForgeInstallManifest {
    protected String minecraft;
    private List<ForgePostProcess.Processor> processors;
    protected Map<String, DataFile> data;
    protected Library[] libraries;

    public Map<String, String> getData(boolean client) {
        Map<String, String> specific = new HashMap<>();
        if (data != null) this.data.forEach((k,v)->specific.put(k, v.get(client)));
        return specific;
    }

    public ForgePostProcess getPostProcess() {
        return new ForgePostProcess(true, minecraft, processors, getData(true));
    }

    public void run(File libraryDir, File installerJar, ConfigurationManager config, DownloadManager manager) {
        List<DownloadTask> downloads = new ArrayList<>();
        for (Library lib : libraries) lib.getArtifact()
                .map(d->{
                    DownloadTask task = d.getDownload(libraryDir.toPath());
                    if (task == null && d.getUrl() == null) {
                        return new DownloadLocal(installerJar.getParentFile().toPath().resolve("maven").resolve(d.path.getPath()).toFile(), libraryDir.toPath().resolve(d.path.getPath()).toFile()).getDownload();
                    }
                    return task;
                })
                .ifPresent(downloads::add);
        manager.download(t->{}, downloads);
        ForgePostProcess p = getPostProcess();
        p.process(libraryDir, config.getVersionManifest().get(minecraft).map(VersionManifest.Entry::getVersionJarFile).get(), installerJar);
    }

    public static class DataFile {
        private String client;
        private String server;

        public String get(boolean client) {
            return client? this.client : this.server;
        }
    }
}
