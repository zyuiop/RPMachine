package net.zyuiop.rpmachine.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zyuiop.rpmachine.permissions.Permission;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.shops.types.ItemStackDataStorage;

/**
 * @author Louis Vialar
 */
public class Json {
    public static final Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(AbstractShopSign.class, new ShopSerializer())
                    .registerTypeAdapter(ItemStackDataStorage.class, new ItemStackStorageSerializer())
                    .registerTypeHierarchyAdapter(Permission.class, new PermissionSerializer())
                    .addSerializationExclusionStrategy(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                            return fieldAttributes.getAnnotation(JsonExclude.class) != null;
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> aClass) {
                            return aClass.getAnnotation(JsonExclude.class) != null;
                        }
                    })
                    .create();

}
