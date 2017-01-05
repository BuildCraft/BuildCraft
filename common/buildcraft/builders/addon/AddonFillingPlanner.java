package buildcraft.builders.addon;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AddonFillingPlanner extends Addon {
    public List<IParameter> parameters = new ArrayList<>();

    @Override
    public IFastAddonRenderer getRenderer() {
        return new AddonDefaultRenderer();
    }

    @Override
    public void onAdded() {
        while (true) {
            Class<? extends IParameter> nextParameterClass = Filling.INSTANCE.getNextParameterClass(parameters);
            if (nextParameterClass != null) {
                // noinspection ConstantConditions
                parameters.add(nextParameterClass.getEnumConstants()[0]);
            } else {
                break;
            }
        }
    }

    @Override
    public void onPlayerRightClick(EntityPlayer player) {
        BCBuildersGuis.FILLING_PLANNER.openGUI(player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(parameters.size());
        parameters.forEach(parameter -> IParameter.toBytes(buf, parameter));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        parameters.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> IParameter.fromBytes(buf)).forEach(parameters::add);
    }
}
