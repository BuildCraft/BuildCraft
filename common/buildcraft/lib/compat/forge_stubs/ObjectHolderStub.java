/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat.forge_stubs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * STUB(R.Chen): needs full implementation.
 *
 * Forge {@code @ObjectHolder} populates a static field with a registered object
 * by ID after registry events fire. Fabric has no equivalent — registration is
 * eager and you simply assign the field at registration time:
 *
 *   public static final Block FOO = Registry.register(Registries.BLOCK, new Identifier(MODID, "foo"), new FooBlock(...));
 *
 * This annotation is retained as a marker so legacy fields can be flagged for
 * the porting pass; it has no runtime effect.
 *
 * TODO(R.Chen): grep for {@code @ObjectHolder} usages once block/item modules are migrated and remove the annotation in favour of direct assignment.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface ObjectHolderStub {
    String value() default "";
}
