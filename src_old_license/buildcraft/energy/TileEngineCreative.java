/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.FakePlayer;

import buildcraft.BuildCraftCore;
import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.PowerMode;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.lib.misc.StringUtilBC;

public class TileEngineCreative extends TileEngineBase {
    private PowerMode powerMode = PowerMode.M2;

    public TileEngineCreative() {
        energyStage = EnumEnergyStage.BLACK;
    }

    @Override
    protected EnumEnergyStage computeEnergyStage() {
        return EnumEnergyStage.BLACK;
    }

    @Override
    public EnumEngineType getEngineType() {
        return EnumEngineType.CREATIVE;
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, EnumFacing side) {
        if (!getWorld().isRemote) {
            Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

            if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, pos)) {
                powerMode = powerMode.getNext();
                energy = 0;

                if (!(player instanceof FakePlayer)) {
                    if (BuildCraftCore.hidePowerNumbers) {
                        player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode.numberless", StringUtilBC.localize(
                                "chat.pipe.power.iron.level." + powerMode.maxPower)));
                    } else {
                        player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode", powerMode.maxPower));
                    }
                }

                sendNetworkUpdate();

                ((IToolWrench) equipped).wrenchUsed(player, pos);
                return true;
            }
        }

        return !player.isSneaking();
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        powerMode = PowerMode.fromId(data.getByte("mode"));
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setByte("mode", (byte) powerMode.ordinal());
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);
        powerMode = PowerMode.fromId(stream.readUnsignedByte());
    }

    @Override
    public void writeData(ByteBuf stream) {
        super.writeData(stream);
        stream.writeByte(powerMode.ordinal());
    }

    @Override
    public void updateHeat() {

    }

    @Override
    public float getPistonSpeed() {
        return 0.02F * (powerMode.ordinal() + 1);
    }

    @Override
    public void engineUpdate() {
        super.engineUpdate();

        if (isRedstonePowered) {
            addEnergy(getIdealOutput());
        }
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    public int getMaxEnergy() {
        return getIdealOutput();
    }

    @Override
    public int getIdealOutput() {
        return powerMode.maxPower;
    }
}
