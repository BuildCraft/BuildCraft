package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {
    public static <K, V> ImmutableMap<K, V> getSubAsImmutableMap(JsonObject obj, String sub, TypeToken<HashMap<K, V>> token, JsonDeserializationContext context) {
        if (!obj.has(sub)) {
            return ImmutableMap.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            System.out.println("Deserialising " + elem.toString() + " as a map");
            HashMap<K, V> map = context.deserialize(elem, token.getType());
            return ImmutableMap.copyOf(map);

        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserialzing it as a " + token, ise);
        }
    }

    public static <T> ImmutableList<T> getSubAsImmutableList(JsonObject obj, String sub, TypeToken<ArrayList<T>> token, JsonDeserializationContext context) {
        if (!obj.has(sub)) {
            return ImmutableList.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            System.out.println("Deserialising " + elem.toString() + " as a list");
            ArrayList<T> list = context.deserialize(elem, token.getType());
            return ImmutableList.copyOf(list);
        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserialzing it as a " + token, ise);
        }
    }
}
