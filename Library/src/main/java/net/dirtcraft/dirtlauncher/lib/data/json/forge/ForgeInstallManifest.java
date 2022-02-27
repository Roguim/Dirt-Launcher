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
package net.dirtcraft.dirtlauncher.lib.data.json.forge;

import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Library;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getMinecraft() {
        return minecraft;
    }

    public Library[] getLibraries() {
        return libraries == null? new Library[0] : libraries;
    }

    public static class DataFile {
        private String client;
        private String server;

        public String get(boolean client) {
            return client? this.client : this.server;
        }
    }
}
