/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import ct.buildcraft.api.robots.EntityRobotBase;

public abstract class RobotEvent extends Event {
    public final EntityRobotBase robot;

    public RobotEvent(EntityRobotBase robot) {
        this.robot = robot;
    }

    @Cancelable
    public static class Place extends RobotEvent {
        public final Player player;

        public Place(EntityRobotBase robot, Player player) {
            super(robot);
            this.player = player;
        }
    }

    @Cancelable
    public static class Interact extends RobotEvent {
        public final Player player;
        public final ItemStack item;

        public Interact(EntityRobotBase robot, Player player, ItemStack item) {
            super(robot);
            this.player = player;
            this.item = item;
        }
    }

    @Cancelable
    public static class Dismantle extends RobotEvent {
        public final Player player;

        public Dismantle(EntityRobotBase robot, Player player) {
            super(robot);
            this.player = player;
        }
    }
}
