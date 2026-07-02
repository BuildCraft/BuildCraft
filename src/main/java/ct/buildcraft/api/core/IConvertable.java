package ct.buildcraft.api.core;

import javax.annotation.Nullable;

/** An object that can be converted into another type. Implementing this interface makes no guarantees that
 * {@link #convertTo(Class)} will actually return anything other than this or null. */
public interface IConvertable {

    /** Attempts to convert this object to the given class. Returns this object if it is already an instance of the
     * given class, a separate object if it can be converted, or null if no conversion is possible. */
    @Nullable
    default <T> T convertTo(Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        return null;
    }
}
