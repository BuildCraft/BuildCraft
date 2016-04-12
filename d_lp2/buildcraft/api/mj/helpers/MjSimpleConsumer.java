package buildcraft.api.mj.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import buildcraft.api.IUniqueReader;
import buildcraft.api.mj.EnumMjPowerType;
import buildcraft.api.mj.IConnectionLogic;
import buildcraft.api.mj.IMjConnection;
import buildcraft.api.mj.IMjMachineConsumer;
import buildcraft.api.mj.helpers.task.IMjTask;
import buildcraft.api.mj.helpers.task.MjTaskRegistry;

public class MjSimpleConsumer extends MjSimpleMachine implements IMjMachineConsumer, INBTSerializable<NBTTagCompound> {
    private final EnumMjPowerType powerType;
    protected final List<IMjTask> tasks = new ArrayList<>();

    public MjSimpleConsumer(TileEntity tile, IConnectionLogic logic, EnumFacing[] faces, EnumMjPowerType powerType) {
        super(tile, logic, faces);
        this.powerType = powerType;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (IMjTask task : tasks) {
            NBTTagCompound taskNbt = new NBTTagCompound();
            taskNbt.setString("reg", getTaskReaderId(task));
            taskNbt.setString("id", task.getRegistryName().toString());
            taskNbt.setTag("data", task.serializeNBT());
            list.appendTag(taskNbt);
        }
        nbt.setTag("tasks", list);
        return nbt;
    }

    /** Gets the task reader for this task. Used for handling multiple types of task that use additional arguments
     * related to the reader. (for example IBptTask has an additional IBuilder argument) */
    protected String getTaskReaderId(IMjTask task) {
        return "mj";
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound taskNbt = list.getCompoundTagAt(i);
            String reg = taskNbt.getString("reg");
            String id = taskNbt.getString("id");
            NBTTagCompound data = taskNbt.getCompoundTag("data");
            IMjTask task = deserializeTask(reg, id, data);
            if (task == null) {
                // TODO: throw exception
            } else {
                tasks.add(task);
            }
        }
    }

    private IMjTask deserializeTask(String reg, String id, NBTTagCompound data) {
        if ("mj".equals(reg)) {
            IUniqueReader<IMjTask> reader = MjTaskRegistry.INSTANCE.getReaderFor(id);
            if (reader == null) {
                //  TODO: throw exception
            } else {
                return reader.deserialize(data);
            }
        } else return null;
    }

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
