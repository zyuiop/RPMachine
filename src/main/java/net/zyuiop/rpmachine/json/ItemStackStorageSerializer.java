package net.zyuiop.rpmachine.json;

import com.google.gson.*;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.shops.types.ItemStackDataStorage;

import java.lang.reflect.Type;

/**
 * @author Louis Vialar
 */
public class ItemStackStorageSerializer implements JsonDeserializer<ItemStackDataStorage>, JsonSerializer<ItemStackDataStorage> {
    @Override
    public ItemStackDataStorage deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (element.isJsonNull())
            return null;

        String clazz = element.getAsJsonObject().get("clazz").getAsString();
        try {
            return context.deserialize(element, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(ItemStackDataStorage storage, Type type, JsonSerializationContext context) {
        if (storage == null)
            return JsonNull.INSTANCE;

        JsonObject ser = context.serialize(storage).getAsJsonObject();
        ser.addProperty("clazz", storage.getClass().getName());
        return ser;
    }
}
