package net.dirtcraft.dirtlauncher.lib.parsing;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.dirtcraft.dirtlauncher.lib.data.json.forge.Artifact;
import net.dirtcraft.dirtlauncher.lib.data.json.serializers.MultiMapAdapter;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JsonUtils {
    public static IOException LOOP_FAILED = new IOException("Failed to initialize webconnect attempt loop @ " + JsonUtils.class.getName());
    public static Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Artifact.class, new Artifact.Adapter())
            .registerTypeAdapter(Multimap.class, new MultiMapAdapter<>())
            .create();

    public static <T> T parseJsonOnline(String url, Class<T> clazz) throws IOException {
        return attemptParseWebJson(new URL(url), clazz);
    }

    public static <T> T parseJson(URL src, Class<T> clazz) throws IOException {
        return attemptParseWebJson(src, clazz);
    }

    private static <T> T attemptParseWebJson(URL src, Class<T> clazz) throws IOException {
        IOException cached = LOOP_FAILED;
        for (int i = 0; i < 9; i++) {
            try {
                return parseJson(src.openStream(), clazz);
            } catch (IOException e) {
                cached = e;
            }
        }
        throw cached;
    }

    public static <T> T parseJson(ZipFile arch, String file, Class<T> clazz) throws IOException {
        return parseJson(arch, arch.getEntry(file), clazz);
    }

    public static <T> T parseJson(ZipFile arch, ZipEntry src, Class<T> clazz) throws IOException {
        try(
                InputStream is = arch.getInputStream(src);
                InputStreamReader isr = new InputStreamReader(is);
                JsonReader jsonReader = new JsonReader(isr)
        ){
            return GSON.fromJson(jsonReader, clazz);
        }
    }

    public static <T> T parseJson(File src, Class<T> clazz) throws IOException {
        try(
                FileReader fileReader = new FileReader(src);
                JsonReader jsonReader = new JsonReader(fileReader)
        ){
            return GSON.fromJson(jsonReader, clazz);
        }
    }

    public static <T> T parseJson(InputStream src, Class<T> clazz) throws IOException {
        try(
                InputStream is = src;
                InputStreamReader isr = new InputStreamReader(is);
                JsonReader jr = new JsonReader(isr)
        ){
            return GSON.fromJson(jr, clazz);
        }
    }

    public static void writeJson(File dest, Object obj) throws IOException {
        try (
                FileWriter fw = new FileWriter(dest)
        ) {
            GSON.toJson(obj, fw);
        }
    }

}
