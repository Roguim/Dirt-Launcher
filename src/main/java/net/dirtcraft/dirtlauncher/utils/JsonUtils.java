package net.dirtcraft.dirtlauncher.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.logging.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class JsonUtils {

    public static <T> Optional<T> getJsonElement(JsonObject json, Function<JsonElement, T> function, String... keys){
        return getJsonElement(json, keys).map(function);
    }

    public static Optional<JsonElement> getJsonElement(JsonObject json, String... keys){
        if (keys.length == 0) return Optional.empty();
        JsonObject jsonObject = json;
        for(int i = 0; i < keys.length - 2;i++){
            String key = keys[i];
            if (!jsonObject.has(key) || !json.get(key).isJsonObject()) return Optional.empty();
            jsonObject = jsonObject.getAsJsonObject(key);
        }
        return jsonObject.has(keys[keys.length-1]) ? Optional.ofNullable(jsonObject.get(keys[keys.length-1])) : Optional.empty();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> parseJson(File file, TypeToken<T> type, Function<JsonObject, T> migrate) {
        return parseJson(file, type, migrate, t->true);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> Optional<T> parseJson(File file, TypeToken<T> type, Function<JsonObject, T> migrate, Function<T, Boolean> validator) {
        T t = null;
        try{
            t = parseJsonUnchecked(file, type);
        } catch (Exception ignored){
        }
        if (t == null || !validator.apply(t)){
            t = tryMigrate(file, migrate);
            if (t != null) toJson(file, t, type);
        }
        return Optional.ofNullable(t);
    }

    @SuppressWarnings("UnstableApiUsage")
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
            return Optional.empty();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> T parseJsonUnchecked(File file, TypeToken<T> type) throws IOException {
        final Gson gson = Main.gson;
        try(
                FileReader fileReader = new FileReader(file);
                JsonReader jsonReader = new JsonReader(fileReader)
        ){
            return gson.fromJson(jsonReader, type.getType());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> T parseJsonUnchecked(File file, Class<T> clazz) throws IOException {
        final Gson gson = Main.gson;
        try(
                FileReader fileReader = new FileReader(file);
                JsonReader jsonReader = new JsonReader(fileReader)
        ){
            return gson.fromJson(jsonReader, clazz);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T> void toJson(File file, T t, TypeToken<T> type){
        final Gson gson = Main.gson;
        try(
                FileWriter fileWriter = new FileWriter(file);
                JsonWriter jsonWriter = new JsonWriter(fileWriter)
        ){
            jsonWriter.setIndent("  ");
            gson.toJson(t, type.getType(), jsonWriter);

            System.out.println("!");
        } catch (IOException e){
            e.printStackTrace();
        }
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

    private static @Nullable <T> T tryMigrate(File file, Function<JsonObject, T> migrate){
        if (!file.exists()) return null;
        try{
            JsonObject jsonObject = readJsonFromFile(file);
            return migrate.apply(jsonObject);
        } catch (Exception ignored){
            Logger.INSTANCE.error(ignored);
            return null;
        }
    }
}
