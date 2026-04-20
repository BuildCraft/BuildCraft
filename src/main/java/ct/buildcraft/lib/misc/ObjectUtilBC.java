package ct.buildcraft.lib.misc;

import javax.annotation.Nullable;

public class ObjectUtilBC {

    /** @param obj The object to check
     * @param clazz the type that is needed
     * @return Either the object as an instance of the specified type, or null if it is not of the specified type. */
    @Nullable
    public static <T> T castOrNull(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        } else {
            return null;
        }
    }

    /** @param obj The object to check
     * @param clazz the type that is needed
     * @param _default The default type to use if the object was not an instance.
     * @return Either the object as an instance of the specified type, or the default object given. */
    public static <T> T castOrDefault(Object obj, Class<T> clazz, T _default) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        } else {
            return _default;
        }
    }
}
