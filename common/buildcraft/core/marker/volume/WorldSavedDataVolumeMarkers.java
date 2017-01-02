package buildcraft.core.marker.volume;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class WorldSavedDataVolumeMarkers extends WorldSavedData {
    public static final String DATA_NAME = "buildcraft_volume_markers";
    public World world;
    public final List<VolumeBox> boxes = new ArrayList<>();

    public WorldSavedDataVolumeMarkers() {
        super(DATA_NAME);
    }

    public WorldSavedDataVolumeMarkers(String name) {
        super(name);
    }

    public VolumeBox getBoxAt(BlockPos pos) {
        return boxes.stream().filter(box -> box.box.contains(pos)).findFirst().orElse(null);
    }

    public VolumeBox addBox(BlockPos pos) {
        VolumeBox box = new VolumeBox(pos);
        boxes.add(box);
        return box;
    }

    public VolumeBox getCurrentEditing(EntityPlayer player) {
        return boxes.stream().filter(box -> box.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        boxes.stream().filter(VolumeBox::isEditing).forEach(box -> {
            EntityPlayer player = box.getPlayer(world);
            if (player == null) {
                box.pauseEditing();
                dirty.set(true);
            } else {
                AxisAlignedBB oldBox = box.box.getBoundingBox();
                box.box.reset();
                box.box.extendToEncompass(box.getHeld());
                BlockPos lookingAt = new BlockPos(player.getPositionVector().addVector(0, player.getEyeHeight(), 0).add(player.getLookVec().scale(box.getDist())));
                box.box.extendToEncompass(lookingAt);
                if (!box.box.getBoundingBox().equals(oldBox)) {
                    dirty.set(true);
                }
            }
        });
        if (dirty.get()) {
            markDirty();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        BCMessageHandler.netWrapper.sendToDimension(new MessageVolumeMarkers(boxes), world.provider.getDimension());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList boxesTag = new NBTTagList();
        boxes.stream().map(VolumeBox::writeToNBT).forEach(boxesTag::appendTag);
        nbt.setTag("boxes", boxesTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        boxes.clear();
        NBTTagList boxesTag = nbt.getTagList("boxes", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, boxesTag.tagCount()).mapToObj(boxesTag::getCompoundTagAt).map(VolumeBox::new).forEach(boxes::add);
    }

    public static WorldSavedDataVolumeMarkers get(World world) {
        if(world.isRemote) {
            BCLog.logger.warn("Creating VolumeMarkers on client, this is a bug");
        }
        MapStorage storage = world.getPerWorldStorage();
        WorldSavedDataVolumeMarkers instance = (WorldSavedDataVolumeMarkers) storage.getOrLoadData(WorldSavedDataVolumeMarkers.class, DATA_NAME);
        if(instance == null) {
            instance = new WorldSavedDataVolumeMarkers();
            storage.setData(DATA_NAME, instance);
        }
        instance.world = world;
        return instance;
    }
}
