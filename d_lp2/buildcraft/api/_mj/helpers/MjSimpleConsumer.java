package buildcraft.api._mj.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.api._mj.EnumMjPowerType;
import buildcraft.api._mj.IConnectionLogic;
import buildcraft.api._mj.IMjConnection;
import buildcraft.api._mj.IMjMachineConsumer;

public class MjSimpleConsumer extends MjSimpleMachine implements IMjMachineConsumer, INBTSerializable<NBTTagCompound> {
    private final EnumMjPowerType powerType;

    private final Queue<Integer> requests = new LinkedList<>();
    private final List<IMjConnection> powerAddingConnections = new ArrayList<>();
    private int milliWattsAddedCache = 0, milliWattsAddedLast = 0;

    public MjSimpleConsumer(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces, EnumMjPowerType powerType) {
        super(tile, logic, faces);
        this.powerType = powerType;
    }

    // ###################################
    //
    // Usage (Public API, use these)
    //
    // ###################################

    // ###################################
    //
    // Usage (Public API, override these)
    //
    // ###################################

    // ###################################
    //
    // NBT reading + writing
    //
    // ###################################

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    // ###################################
    //
    // Internal methods
    //
    // ###################################

    @Override
    public boolean onConnectionCreate(IMjConnection connection) {
        return false;
    }

    @Override
    public void onConnectionActivate(IMjConnection connection) {

    }

    @Override
    public void onConnectionBroken(IMjConnection connection) {

    }

    @Override
    public EnumMjPowerType getPowerType() {
        return powerType;
    }
}
