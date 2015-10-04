/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tools.IToolWrench;
import buildcraft.BuildCraftCore;
import buildcraft.core.PowerMode;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.statements.ActionPowerLimiter;

public class PipePowerIron extends Pipe<PipeTransportPower> {

    public PipePowerIron(Item item) {
        super(new PipeTransportPower(), item);
        transport.initFromPipe(getClass());
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        if (container == null) {
            return PipeIconProvider.TYPE.PipePowerIronM128.ordinal();
        }
        return PipeIconProvider.TYPE.PipePowerIronM2.ordinal() + container.getBlockMetadata();
    }

    @Override
    public boolean blockActivated(EntityPlayer player) {
        Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
        if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.getPos())) {
            if (player.isSneaking()) {
                setMode(getMode().getPrevious());
            } else {
                setMode(getMode().getNext());
            }
            if (getWorld().isRemote && !(player instanceof FakePlayer)) {
                if (BuildCraftCore.hidePowerNumbers) {
                    player.addChatMessage(new ChatComponentText(String.format(StringUtils.localize("chat.pipe.power.iron.mode.numberless"),
                            StringUtils.localize("chat.pipe.power.iron.level." + getMode().maxPower))));
                } else {
                    player.addChatMessage(new ChatComponentText(String.format(StringUtils.localize("chat.pipe.power.iron.mode"),
                            getMode().maxPower)));
                }
            }

            ((IToolWrench) equipped).wrenchUsed(player, container.getPos());
            return true;
        }

        return false;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        transport.maxPower = getMode().maxPower;
    }

    public PowerMode getMode() {
        return PowerMode.fromId(container.getBlockMetadata());
    }

    public void setMode(PowerMode mode) {
        IBlockState state = container.getWorld().getBlockState(container.getPos());
        if (mode.ordinal() != BuildCraftProperties.GENERIC_PIPE_DATA.getValue(state).intValue()) {
            container.getWorld().setBlockState(container.getPos(), state.withProperty(BuildCraftProperties.GENERIC_PIPE_DATA, mode.ordinal()));
            container.scheduleRenderUpdate();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    protected void actionsActivated(Collection<StatementSlot> actions) {
        super.actionsActivated(actions);

        for (StatementSlot action : actions) {
            if (action.statement instanceof ActionPowerLimiter) {
                setMode(((ActionPowerLimiter) action.statement).limit);
                break;
            }
        }
    }

    @Override
    public LinkedList<IActionInternal> getActions() {
        LinkedList<IActionInternal> action = super.getActions();
        // TODO: A bit of a hack
        for (PowerMode mode : PowerMode.VALUES) {
            action.add(BuildCraftTransport.actionPowerLimiter[mode.ordinal()]);
        }
        return action;
    }
}
