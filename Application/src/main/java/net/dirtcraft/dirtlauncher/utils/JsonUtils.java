package net.dirtcraft.dirtlauncher.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.logging.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

public class JsonUtils {

    public static <T> Optional<T> getJsonElement(JsonObject json, Function<JsonElement, T> function, String... keys){
        return getJsonElement(json, keys).map(function);
    }

    public static Optional<JsonElement> getJsonElement(JsonObject json, String... keys){
        if (keys.length == 0) return Optional.empty();
        for(int i = 0; i < keys.length - 1; i++){
            String key = keys[i];
            if (!json.has(key) || !(json.get(key)).isJsonObject()) return Optional.empty();
            json = json.getAsJsonObject(key);
        }
        return json.has(keys[keys.length-1]) ? Optional.ofNullable(json.get(keys[keys.length-1])) : Optional.empty();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> parseJson(File file, TypeToken<T> type, Function<JsonObject, T> migrate) {
        return parseJson(file, type, migrate, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> parseJson(File file, TypeToken<T> type, Function<JsonObject, T> migrate, Function<T, Boolean> validator) {
        T t = null;
        try{
            t = parseJsonUnchecked(file, type);
        } catch (Exception ignored){}
        try {
            if (migrate != null && t == null || validator != null && !validator.apply(t)) {
                t = tryMigrate(file, migrate);
                if (t != null) toJson(file, t, type);
            }
        } catch (Exception ignored){}
        return Optional.ofNullable(t);
    }

    public static <T> Optional<T> parseJson(File file, Class<T> clazz) {
        try {
            return Optional.ofNullable(parseJsonUnchecked(file, clazz));
        } catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> parseJson(File file, TypeToken<T> type) {
        try {
            return Optional.ofNullable(parseJsonUnchecked(file, type));
        } catch (Exception e){
            Logger.INSTANCE.error(e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> T parseJsonUnchecked(File file, TypeToken<T> type) throws IOException {
        final Gson gson = DirtLauncher.getGson();
        try(
                FileReader fileReader = new FileReader(file);
                JsonReader jsonReader = new JsonReader(fileReader)
        ){
            return gson.fromJson(jsonReader, type.getType());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> T parseJsonUnchecked(File file, Class<T> clazz) throws IOException {
        final Gson gson = DirtLauncher.getGson();
        try(
                FileReader fileReader = new FileReader(file);
                JsonReader jsonReader = new JsonReader(fileReader)
        ){
            return gson.fromJson(jsonReader, clazz);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> void toJson(File file, T t, TypeToken<T> type){
        toJson(file, t, type.getType());
    }

    public static <T> void toJson(File file, T t, Class<T> type){
        toJson(file, t, (Type) type);
    }

    public static <T> void toJson(File file, T t, Type type){
        final Gson gson = DirtLauncher.getGson();
        try(
                FileWriter fileWriter = new FileWriter(file);
                JsonWriter jsonWriter = new JsonWriter(fileWriter)
        ){
            jsonWriter.setIndent("  ");
            gson.toJson(t, type, jsonWriter);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public static @Nullable JsonObject readJsonFromFile(File file) {
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

    private static @Nullable <T> T tryMigrate(File file, Function<JsonObject, T> migrate){
        if (!file.exists()) return null;
        try{
            JsonObject jsonObject = readJsonFromFile(file);
            return migrate.apply(jsonObject);
        } catch (Exception e){
            Logger.INSTANCE.error(e);
            return null;
        }
    }
}
