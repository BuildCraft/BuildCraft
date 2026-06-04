// STUB(R.Chen): Forge @CapabilityInject — compile shim.
package net.minecraftforge.common.capabilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface CapabilityInject {
    Class<?> value();
}
