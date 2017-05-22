package buildcraft.builders.addon;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.ISingleAddon;
import buildcraft.lib.misc.NBTUtilBC;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Deprecated
public class AddonFillingPlanner extends Addon implements ISingleAddon {
    public List<IParameter> parameters = new ArrayList<>();
    public boolean inverted;
    public Template.BuildingInfo buildingInfo;

    public boolean[][][] getFillingPlan() {
        BlockPos size = box.box.size();
        boolean[][][] fillingPlan = Filling.INSTANCE.getFillingPlan(size, parameters);
        if (inverted) {
            fillingPlan = Filling.INSTANCE.invertFillingPlan(size, fillingPlan);
        }
        return fillingPlan;
    }

    public void markDirty() {
        Template template = new Template();
        template.size = box.box.size();
        template.offset = BlockPos.ORIGIN;
        template.data = getFillingPlan();
        buildingInfo = template.new BuildingInfo(box.box.min(), Rotation.NONE);
    }

    @Override
    public IFastAddonRenderer<AddonFillingPlanner> getRenderer() {
        return new AddonDefaultRenderer<AddonFillingPlanner>(BCBuildersSprites.FILLING_PLANNER.getSprite())
                .then(new AddonRendererFillingPlanner());
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
        markDirty();
    }

    @Override
    public void onPlayerRightClick(EntityPlayer player) {
        BCBuildersGuis.FILLING_PLANNER.openGUI(player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag(
                "parameters",
                NBTUtilBC.writeCompoundList(
                        parameters.stream()
                                .map(parameter -> IParameter.writeToNBT(new NBTTagCompound(), parameter))
                )
        );
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTUtilBC.readCompoundList(
                nbt.getTagList(
                        "parameters",
                        Constants.NBT.TAG_COMPOUND
                )
        )
                .map(IParameter::readFromNBT)
                .forEach(parameters::add);
        markDirty();
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
        markDirty();
    }
}
