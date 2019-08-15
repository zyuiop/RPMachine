package net.zyuiop.rpmachine.json;

import com.google.gson.*;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import net.zyuiop.rpmachine.common.regions.Region;

import java.lang.reflect.Type;

/**
 * @author Louis Vialar
 */
public class RegionSerializer implements JsonDeserializer<Region>, JsonSerializer<Region> {
    @Override
    public Region deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String clazz;
        if (element.getAsJsonObject().has("regionClass")) {
            clazz = element.getAsJsonObject().get("regionClass").getAsString();
        } else {
            clazz = RectangleRegion.class.getName();
        }

        try {
            return context.deserialize(element, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Region region, Type type, JsonSerializationContext context) {
        JsonObject ser = context.serialize(region).getAsJsonObject();
        ser.addProperty("regionClass", region.getClass().getName());
        return ser;
    }
}
