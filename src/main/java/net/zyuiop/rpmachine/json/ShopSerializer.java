package net.zyuiop.rpmachine.json;

import com.google.gson.*;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;

import java.lang.reflect.Type;

/**
 * @author Louis Vialar
 */
public class ShopSerializer implements JsonDeserializer<AbstractShopSign>, JsonSerializer<AbstractShopSign> {
    @Override
    public AbstractShopSign deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String clazz = element.getAsJsonObject().get("signClass").getAsString();
        try {
            return context.deserialize(element, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(AbstractShopSign sign, Type type, JsonSerializationContext context) {
        JsonObject ser = context.serialize(sign).getAsJsonObject();
        ser.addProperty("signClass", sign.getClass().getName());
        return ser;
    }
}
