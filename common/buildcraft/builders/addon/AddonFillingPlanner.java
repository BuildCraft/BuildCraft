package buildcraft.builders.addon;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.lib.misc.NBTUtilBC;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AddonFillingPlanner extends Addon {
    public List<IParameter> parameters = new ArrayList<>();
    public boolean inverted;

    public List<BlockPos> getBlocksShouldBe(boolean trueIfPlacedOrFalseIfBroken) {
        List<BlockPos> blockShouldBePlaced = new ArrayList<>();
        BlockPos size = box.box.size();
        boolean[][][] fillingPlan = Filling.INSTANCE.getFillingPlan(size, parameters);
        if (inverted == trueIfPlacedOrFalseIfBroken) {
            fillingPlan = Filling.INSTANCE.invertFillingPlan(size, fillingPlan);
        }
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    if (fillingPlan[x][y][z]) {
                        blockShouldBePlaced.add(new BlockPos(x, y, z).add(box.box.min()));
                    }
                }
            }
        }
        return blockShouldBePlaced;
    }

    public List<BlockPos> getBlocksShouldBePlaced() {
        return getBlocksShouldBe(true);
    }

    public List<BlockPos> getBlocksShouldBeBroken() {
        return getBlocksShouldBe(false);
    }

    @Override
    public IFastAddonRenderer<AddonFillingPlanner> getRenderer() {
        return new AddonDefaultRenderer<AddonFillingPlanner>().then(new AddonRendererFillingPlanner());
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
        NBTTagList parametersTag = new NBTTagList();
        parameters.stream().map(parameter -> IParameter.writeToNBT(new NBTTagCompound(), parameter)).forEach(parametersTag::appendTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList parametersTag = new NBTTagList();
        IntStream.range(0, parametersTag.tagCount()).mapToObj(parametersTag::getCompoundTagAt).map(IParameter::readFromNBT).forEach(parameters::add);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(parameters.size());
        parameters.forEach(parameter -> IParameter.toBytes(buf, parameter));
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        parameters.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> IParameter.fromBytes(buf)).forEach(parameters::add);
        inverted = buf.readBoolean();
    }
}
