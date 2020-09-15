package net.dirtcraft.dirtlauncher.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

public class JsonUtils {

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
}
