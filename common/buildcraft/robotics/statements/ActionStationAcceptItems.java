/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.EntityRobot;

public class ActionStationAcceptItems extends ActionStationInputItems {

    public ActionStationAcceptItems() {
        super("buildcraft:station.accept_items");
        setLocation("buildcraftrobotics:triggers/action_station_accept_items");
        StatementManager.statements.put("buildcraft:station.drop_in_pipe", this);
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("gate.action.station.accept_items");
    }

    // @Override
    // public void registerIcons(TextureAtlasSpriteRegister iconRegister) {
    // icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_station_accept_items");
    // }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public boolean insert(DockingStation station, EntityRobot robot, StatementSlot actionSlot, IInvSlot invSlot, boolean doInsert) {
        if (!super.insert(station, robot, actionSlot, invSlot, doInsert)) {
            return false;
        }

        IInjectable injectable = station.getItemOutput();

        if (injectable == null) {
            return false;
        }

        EnumFacing injectSide = station.side().getOpposite();

        if (!injectable.canInjectItems(injectSide)) {
            return false;
        }

        if (!doInsert) {
            return true;
        }

        ItemStack stack = invSlot.getStackInSlot();
        int used = injectable.injectItem(stack, doInsert, injectSide, null);
        if (used > 0) {
            stack.stackSize -= used;
            if (stack.stackSize > 0) {
                invSlot.setStackInSlot(stack);
            } else {
                invSlot.setStackInSlot(null);
            }
            return true;
        }

        return false;
    }

}
