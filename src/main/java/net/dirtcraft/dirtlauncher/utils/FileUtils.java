package net.dirtcraft.dirtlauncher.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.dirtcraft.dirtlauncher.Main;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.dirtcraft.dirtlauncher.utils.Constants.MAX_DOWNLOAD_ATTEMPTS;
import static org.apache.commons.io.FileUtils.ONE_MB;

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

    // Mirrors just in case the file imports this instead of the commons-io FileUtils. Makes things easier than using paths every declaration...
    public static void deleteDirectory(File file) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(file);
    }

    // Mirrors just in case the file imports this instead of the commons-io FileUtils. Makes things easier than using paths every declaration...
    public static void deleteDirectoryUnchecked(File file) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(file);
        } catch (Exception ignored){

        }
    }

    public static void copyDirectory(File src, File dest) throws IOException {
        //org.apache.commons.io.FileUtils.copyDirectory(src, dest);
        copyOrReplaceDirectory(src, dest);
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

    //A modified version of the apache copyDirectory code.
    private static void copyOrReplaceDirectory(File srcDir, File destDir) throws IOException{
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles =  srcDir.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, exclusionList);

    }

    //A modified version of the apache copyDirectory code.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void doCopyDirectory(final File srcDir, final File destDir, final List<String> exclusionList)
            throws IOException {
        // recurse
        final File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, exclusionList);
                } else {
                    if (dstFile.exists()) dstFile.delete();
                    doCopyFile(srcFile, dstFile);
                }
            }
        }
    }

    //A modified version of the apache copyDirectory code.
    private static void doCopyFile(final File srcFile, final File destFile)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        try (FileInputStream fis = new FileInputStream(srcFile);
             FileChannel input = fis.getChannel();
             FileOutputStream fos = new FileOutputStream(destFile);
             FileChannel output = fos.getChannel()) {
            final long size = input.size(); // TODO See IO-386
            long pos = 0;
            long count = 0;
            while (pos < size) {
                final long remain = size - pos;
                count = Math.min(remain, ONE_MB * 30);
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
                    break; // ensure we don't loop forever
                }
                pos += bytesCopied;
            }
        }

        final long srcLen = srcFile.length(); // TODO See IO-386
        final long dstLen = destFile.length(); // TODO See IO-386
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

}
