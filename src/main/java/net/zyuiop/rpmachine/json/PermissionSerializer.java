package net.zyuiop.rpmachine.json;

import com.google.gson.*;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.PermissionTypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * @author Louis Vialar
 */
public class PermissionSerializer implements JsonDeserializer<DelegatedPermission>, JsonSerializer<DelegatedPermission> {
    @Override
    public DelegatedPermission deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String[] data = element.getAsString().split(".");
        String head = data[0];
        String perm = data[1];

        try {
            PermissionTypes t = PermissionTypes.valueOf(head);

            return t.getPermission(perm);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(DelegatedPermission perm, Type type, JsonSerializationContext context) {
        try {
            PermissionTypes t = PermissionTypes.get(perm);

            return new JsonPrimitive(t.name() + "." + perm.name());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new JsonParseException(e);
        }
    }
}
