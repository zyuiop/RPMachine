package net.zyuiop.rpmachine.json;

import com.google.gson.*;
import net.zyuiop.rpmachine.cities.voting.VotationFinishHook;

import java.lang.reflect.Type;

/**
 * @author Louis Vialar
 */
public class VoteHookSerializer implements JsonDeserializer<VotationFinishHook>, JsonSerializer<VotationFinishHook> {
    @Override
    public VotationFinishHook deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String clazz = element.getAsJsonObject().get("hookClass").getAsString();

        try {
            return context.deserialize(element, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(VotationFinishHook hook, Type type, JsonSerializationContext context) {
        JsonObject ser = context.serialize(hook).getAsJsonObject();
        ser.addProperty("hookClass", hook.getClass().getName());
        return ser;
    }
}
