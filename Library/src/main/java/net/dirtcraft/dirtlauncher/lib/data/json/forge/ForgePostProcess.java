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

import net.dirtcraft.dirtlauncher.lib.util.Jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ForgePostProcess {
    private final boolean isClient;
    private final Map<String, String> data;
    private final List<Processor> processors;
    private final String mcVersion;

    public ForgePostProcess(boolean client, String mcVersion, List<Processor> processors, Map<String, String> data) {
        this.isClient = client;
        this.mcVersion = mcVersion;
        this.processors = processors;
        this.data = data;
    }

    public boolean process(File librariesDir, File minecraft, Jar installer) {
        try {
            if (!data.isEmpty()) {
                StringBuilder err = new StringBuilder();
                Path temp  = Files.createTempDirectory("forge_installer");
                for (String key : data.keySet()) {
                    String value = data.get(key);

                    if (value.charAt(0) == '[' && value.charAt(value.length() - 1) == ']') { //Artifact
                        data.put(key, Artifact.from(value.substring(1, value.length() -1)).getLocalPath(librariesDir).getAbsolutePath());
                    } else if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') { //Literal
                        data.put(key, value.substring(1, value.length() -1));
                    } else {
                        File target = Paths.get(temp.toString(), value).toFile();
                        if (!extractFile(installer, value, target))
                            err.append("\n  ").append(value);
                        data.put(key, target.getAbsolutePath());
                    }
                }
                if (err.length() > 0) {
                    return false;
                }
            }
            data.put("SIDE", isClient ? "client" : "server");
            data.put("MINECRAFT_JAR", minecraft.getAbsolutePath());
            data.put("MINECRAFT_VERSION", mcVersion);
            //data.put("ROOT", instanceDir.getAbsolutePath());
            data.put("INSTALLER", installer.asFile().getAbsolutePath());
            data.put("LIBRARY_DIR", librariesDir.getAbsolutePath());

            int progress = 1;
            for (Processor proc : processors) {

                Map<String, String> outputs = new HashMap<>();
                if (!proc.getOutputs().isEmpty()) {
                    boolean miss = false;
                    for (Map.Entry<String, String> e : proc.getOutputs().entrySet()) {
                        String key = e.getKey();
                        if (key.charAt(0) == '[' && key.charAt(key.length() - 1) == ']')
                            key = Artifact.from(key.substring(1, key.length() - 1)).getLocalPath(librariesDir).getAbsolutePath();
                        else
                            key = replaceTokens(data, key);

                        String value = e.getValue();
                        if (value != null)
                            value = replaceTokens(data, value);

                        if (key == null || value == null) {
                            return false;
                        }

                        outputs.put(key, value);
                        File artifact = new File(key);
                        if (!artifact.exists()) {
                            miss = true;
                        } else {
                            String sha = getSha1(artifact);
                            if (sha.equals(value)) {

                            } else {
                                miss = true;
                                artifact.delete();
                            }
                        }
                    }
                    if (!miss) {
                        continue;
                    }
                }

                File jar = proc.getJar().getLocalPath(librariesDir);
                if (!jar.exists() || !jar.isFile()) {
                    return false;
                }

                // Locate main class in jar file
                JarFile jarFile = new JarFile(jar);
                String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                jarFile.close();

                if (mainClass == null || mainClass.isEmpty()) {
                    return false;
                }

                List<URL> classpath = new ArrayList<>();
                StringBuilder err = new StringBuilder();
                classpath.add(jar.toURI().toURL());
                for (Artifact dep : proc.getClasspath()) {
                    File lib = dep.getLocalPath(librariesDir);
                    if (!lib.exists() || !lib.isFile())
                        err.append("\n  ").append(dep.getDescriptor());
                    classpath.add(lib.toURI().toURL());
                }
                if (err.length() > 0) {
                    return false;
                }

                List<String> args = new ArrayList<>();
                for (String arg : proc.getArgs()) {
                    char start = arg.charAt(0);
                    char end = arg.charAt(arg.length() - 1);

                    if (start == '[' && end == ']') //Library
                        args.add(Artifact.from(arg.substring(1, arg.length() - 1)).getLocalPath(librariesDir).getAbsolutePath());
                    else
                        args.add(replaceTokens(data, arg));
                }
                if (err.length() > 0) {
                    return false;
                }

                ClassLoader cl = new URLClassLoader(classpath.toArray(new URL[classpath.size()]), getParentClassloader());
                // Set the thread context classloader to be our newly constructed one so that service loaders work
                Thread currentThread = Thread.currentThread();
                ClassLoader threadClassloader = currentThread.getContextClassLoader();
                currentThread.setContextClassLoader(cl);
                try {
                    Class<?> cls = Class.forName(mainClass, true, cl);
                    Method main = cls.getDeclaredMethod("main", String[].class);
                    main.invoke(null, (Object)args.toArray(new String[args.size()]));
                } catch (InvocationTargetException ite) {
                    Throwable e = ite.getCause();
                    e.printStackTrace();
                    return false;
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    // Set back to the previous classloader
                    currentThread.setContextClassLoader(threadClassloader);
                }

                if (!outputs.isEmpty()) {
                    for (Map.Entry<String, String> e : outputs.entrySet()) {
                        File artifact = new File(e.getKey());
                        if (!artifact.exists()) {
                            err.append("\n    ").append(e.getKey()).append(" missing");
                        } else {
                            String sha = getSha1(artifact);
                            if (sha.equals(e.getValue())) {

                            } else {
                                err.append("\n    ").append(e.getKey())
                                        .append("\n      Expected: ").append(e.getValue())
                                        .append("\n      Actual:   ").append(sha);
                                if (!artifact.delete())
                                    err.append("\n      Could not delete file");
                            }
                        }
                    }
                    if (err.length() > 0) {
                        return false;
                    }
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean clChecked = false;
    private static ClassLoader parentClassLoader = null;
    @SuppressWarnings("unused")
    private synchronized ClassLoader getParentClassloader() { //Reflectively try and get the platform classloader, done this way to prevent hard dep on J9.
        if (!clChecked) {
            clChecked = true;
            if (!System.getProperty("java.version").startsWith("1.")) { //in 9+ the changed from 1.8 to just 9. So this essentially detects if we're <9
                try {
                    Method getPlatform = ClassLoader.class.getDeclaredMethod("getPlatformClassLoader");
                    parentClassLoader = (ClassLoader)getPlatform.invoke(null);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                }
            }
        }
        return parentClassLoader;
    }

    public static class Processor {
        // Which side this task is to be run on, Currently know sides are "client", "server" and "extract", if this omitted, assume all sides.
        private List<String> sides;
        // The executable jar to run, The installer will run it in-process, but external tools can run it using java -jar {file}, so MANIFEST Main-Class entry must be valid.
        private Artifact jar;
        // Dependency list of files needed for this jar to run. Anything listed here SHOULD be listed in {@see Install#libraries} so the installer knows to download it.
        private Artifact[] classpath;
        /*
         * Arguments to pass to the jar, can be in the following formats:
         * [Artifact] : A artifact path in the target maven style repo, where all libraries are downloaded to.
         * {DATA_ENTRY} : A entry in the Install#data map, extract as a file, there are a few extra specified values to allow the same processor to run on both sides:
         *   {MINECRAFT_JAR} - The vanilla minecraft jar we are dealing with, /versions/VERSION/VERSION.jar on the client and /minecraft_server.VERSION.jar for the server
         *   {SIDE} - Either the exact string "client", "server", and "extract" depending on what side we are installing.
         */
        private String[] args;
        /*
         *  Files output from this task, used for verifying the process was successful, or if the task needs to be rerun.
         *  Keys are either a [Artifact] or {DATA_ENTRY}, if it is a {DATA_ENTRY} then that MUST be a [Artifact]
         *  Values are either a {DATA_ENTRY} or 'value', if it is a {DATA_ENTRY} then that entry MUST be a quoted string literal
         *    The end string literal is the sha1 hash of the specified artifact.
         */
        private Map<String, String> outputs;

        public boolean isSide(String side) {
            return sides == null || sides.contains(side);
        }

        public Artifact getJar() {
            return jar;
        }

        public Artifact[] getClasspath() {
            return classpath == null ? new Artifact[0] : classpath;
        }

        public String[] getArgs() {
            return args == null ? new String[0] : args;
        }

        public Map<String, String> getOutputs() {
            return outputs == null ? Collections.emptyMap() : outputs;
        }
    }

    public static String replaceTokens(Map<String, String> tokens, String value) {
        StringBuilder buf = new StringBuilder();

        for (int x = 0; x < value.length(); x++) {
            char c = value.charAt(x);
            if (c == '\\') {
                if (x == value.length() - 1)
                    throw new IllegalArgumentException("Illegal pattern (Bad escape): " + value);
                buf.append(value.charAt(++x));
            } else if (c == '{' || c ==  '\'') {
                StringBuilder key = new StringBuilder();
                for (int y = x + 1; y <= value.length(); y++) {
                    if (y == value.length())
                        throw new IllegalArgumentException("Illegal pattern (Unclosed " + c + "): " + value);
                    char d = value.charAt(y);
                    if (d == '\\') {
                        if (y == value.length() - 1)
                            throw new IllegalArgumentException("Illegal pattern (Bad escape): " + value);
                        key.append(value.charAt(++y));
                    } else if (c == '{' && d == '}') {
                        x = y;
                        break;
                    } else if (c == '\'' && d == '\'') {
                        x = y;
                        break;
                    } else
                        key.append(d);
                }
                if (c == '\'')
                    buf.append(key);
                else {
                    if (!tokens.containsKey(key.toString()))
                        throw new IllegalArgumentException("Illegal pattern: " + value + " Missing Key: " + key);
                    buf.append(tokens.get(key.toString()));
                }
            } else {
                buf.append(c);
            }
        }

        return buf.toString();
    }

    public static String getSha1(File target) {
        try {
            return HashFunction.SHA1.hash(Files.readAllBytes(target.toPath())).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean extractFile(ZipFile installer, String name, File target) {
        try {
            final String path = name.charAt(0) == '/' ? name.substring(1) : name;
            ZipEntry entry = installer.getEntry(path);
            final InputStream input = installer.getInputStream(entry);
            if (input == null) {
                System.out.println("File not found in installer archive: " + path);
                return false;
            }
            if (!target.getParentFile().exists()) target.getParentFile().mkdirs();
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true; //checksumValid(target, checksum); //TODO: zip checksums?
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
