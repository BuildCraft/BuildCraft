/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for tiles that need to interface with BuildCraft
 * energy framework, a.k.a MinecraftJoule or MJ. In order to receive power,
 * tiles, need to declare a *public* double field, with the annotation
 * MjBattery. BuildCraft machines able to provide power will then connect to
 * these tiles, and feed energy up to max capacity. It's the responsibilty
 * of the implementer to manually decrease the value of the energy, as he
 * simulates energy consumption. On each cycle, per power input, machines can
 * receive up to "maxReceivedPerCyle" units of energy. As an optional behavior,
 * the system can have a minimum amount of energy consumed even if the system
 * is at max capacity, modelized by the "minimumConsumption" value.
 *
 * If the field designated by MjBattery is an object, then BuildCraft will
 * consider that this is a case of a nested battery, and will look for the
 * field in the designated object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface MjBattery {

	double maxCapacity() default 100.0;

	double maxReceivedPerCycle() default 10.0;

	double minimumConsumption() default 0.1;

}