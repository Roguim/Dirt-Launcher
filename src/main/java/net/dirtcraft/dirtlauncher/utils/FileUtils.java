package net.dirtcraft.dirtlauncher.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.dirtcraft.dirtlauncher.utils.Constants.MAX_DOWNLOAD_ATTEMPTS;

public class FileUtils {

    public static String getFileSha1(File file) throws IOException {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            try (InputStream input = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int len = input.read(buffer);
                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }

                return new HexBinaryAdapter().marshal(sha1.digest());
            }
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }

    @Nullable
    public static JsonObject readJsonFromFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeJsonToFile(File file, JsonObject jsonObject) {
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mirrors just in case the file imports this instead of the commons-io FileUtils. Makes things easier than using paths every declaration...
    public static void deleteDirectory(File file) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(file);
    }

    public static void copyURLToFile(String URL, File file) throws IOException{
        copyURLToFile(URL, file, 0);
    }

    private static void copyURLToFile(String URL, File file, int attempts) throws IOException {
        try {
            org.apache.commons.io.FileUtils.copyURLToFile(new URL(URL), file);
        } catch (IOException e){
            e.printStackTrace();
            if (attempts < MAX_DOWNLOAD_ATTEMPTS) copyURLToFile(URL, file, attempts+1);
            else throw e;
        }
    }

    public static void copyDirectory(File src, File dest) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(src, dest);
    }

    public static void moveDirectory(File src, File dest, boolean createDir) throws IOException {
        org.apache.commons.io.FileUtils.moveDirectoryToDirectory(src, dest, createDir);
    }

    public static void extractJar(String jarFile, String destDir) throws IOException {
        JarFile jar = new JarFile(jarFile);
        Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry file = (JarEntry) enumEntries.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            InputStream is = jar.getInputStream(file); // get the input stream
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
        jar.close();
    }

    public static JsonObject extractForgeJar(File jarFile, File destDir) throws IOException {
        JsonObject output = new JsonObject();
        JarFile jar = new JarFile(jarFile);
        Enumeration enumEntries = jar.entries();
        if (Constants.DEBUG) System.out.println("Extracing Jar: " + jar.getName());
        while(enumEntries.hasMoreElements()) {
            JarEntry file = (JarEntry) enumEntries.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            if (file.isDirectory()) {
                f.mkdir();
                continue;
            }
            if (file.getName().contains(".jar")) {
                f = new File(destDir + File.separator + file.getName().replace("-1.7.10-universal", "-universal"));

                InputStream is = jar.getInputStream(file);
                FileOutputStream fos = new FileOutputStream(f);
                while(is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            } else if(file.getName().contains(".json")) {
                InputStream is = jar.getInputStream(file);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                JsonParser jsonParser = new JsonParser();
                output = (JsonObject) jsonParser.parse(isr);
                isr.close();
                is.close();
            }
        }
        jar.close();
        return output;
    }

    public static void unpackPackXZ(File packXZFile) throws IOException {
        // XZ -> Pack
        InputStream fileIn = Files.newInputStream(packXZFile.toPath());
        BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
        OutputStream packOutputStream = Files.newOutputStream(Paths.get(packXZFile.getPath().replace(".pack.xz", ".pack")));
        XZCompressorInputStream xzIn = new XZCompressorInputStream(bufferedIn);
        final byte[] buffer = new byte[8192];
        int n = 0;
        while (-1 != (n = xzIn.read(buffer))) {
            packOutputStream.write(buffer, 0, n);
        }
        packOutputStream.close();
        xzIn.close();
        bufferedIn.close();
        fileIn.close();

        // Pack -> .jar
        InputStream packFileIn = Files.newInputStream(Paths.get(packXZFile.getPath().replace(".pack.xz", ".pack")));
        BufferedInputStream packBufferedIn = new BufferedInputStream(packFileIn);
        OutputStream jarOutputStream = Files.newOutputStream(Paths.get(packXZFile.getPath().replace(".jar.pack.xz", ".jar")));
        Pack200CompressorInputStream packIn = new Pack200CompressorInputStream(packBufferedIn);
        final byte[] buffer2 = new byte[8192];
        int m = 0;
        while (-1 != (m = packIn.read(buffer2))) {
            jarOutputStream.write(buffer2, 0, m);
        }

        jarOutputStream.close();
        packIn.close();
        packBufferedIn.close();
        packFileIn.close();
    }

}
