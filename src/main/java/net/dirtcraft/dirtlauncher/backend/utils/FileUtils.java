package net.dirtcraft.dirtlauncher.backend.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static JsonObject parseJsonFromFile(File file) {
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

}
