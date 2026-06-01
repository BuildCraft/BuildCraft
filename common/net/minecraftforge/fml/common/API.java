// STUB(R.Chen): Forge @API annotation — compile shim.
package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface API {
    String apiVersion();
    String owner();
    String provides() default "";
}
