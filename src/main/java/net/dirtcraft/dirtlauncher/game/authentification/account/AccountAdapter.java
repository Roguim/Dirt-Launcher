package net.dirtcraft.dirtlauncher.game.authentification.account;

import com.google.gson.*;

import java.lang.reflect.Type;

public class AccountAdapter implements JsonSerializer<Account>, JsonDeserializer<Account> {


    @Override
    public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Account.AccountType type = Account.AccountType.valueOf(json.getAsJsonObject().get("type").getAsString());
        return context.deserialize(json, type.type);
    }

    @Override
    public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src, src.type.type);
    }
}
