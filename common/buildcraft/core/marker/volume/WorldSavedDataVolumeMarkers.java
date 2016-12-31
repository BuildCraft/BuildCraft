package buildcraft.core.marker.volume;

import buildcraft.api.core.BCLog;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.misc.data.Box;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
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

    public VolumeBox getCurrentEditing(String player) {
        return boxes.stream().filter(box -> player.equals(box.player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        boxes.stream().filter(VolumeBox::isEditing).forEach(box -> {
            EntityPlayer player = world.getPlayerEntityByName(box.player);
            if (player != null) {
                if (world.getPlayerEntityByName(box.player) == null) {
                    box.player = null;
                    box.resetEditing();
                    dirty.set(true);
                } else {
                    AxisAlignedBB oldBox = box.box.getBoundingBox();
                    box.box.reset();
                    box.box.extendToEncompass(box.held);
                    BlockPos lookingAt = new BlockPos(player.getPositionVector().addVector(0, player.getEyeHeight(), 0).add(player.getLookVec().scale(box.dist)));
                    box.box.extendToEncompass(lookingAt);
                    if (!box.box.getBoundingBox().equals(oldBox)) {
                        dirty.set(true);
                    }
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
        boxes.stream().map(box -> {
            NBTTagCompound boxTag = new NBTTagCompound();
            boxTag.setTag("box", box.box.writeToNBT());
            if (box.player != null) {
                boxTag.setString("player", box.player);
            }
            if (box.oldMin != null) {
                boxTag.setTag("oldMin", NBTUtil.createPosTag(box.oldMin));
            }
            if (box.oldMax != null) {
                boxTag.setTag("oldMax", NBTUtil.createPosTag(box.oldMax));
            }
            if (box.held != null) {
                boxTag.setTag("held", NBTUtil.createPosTag(box.held));
            }
            boxTag.setDouble("dist", box.dist);
            return boxTag;
        }).forEach(boxesTag::appendTag);
        nbt.setTag("boxes", boxesTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        boxes.clear();
        NBTTagList boxesTag = nbt.getTagList("boxes", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, boxesTag.tagCount()).mapToObj(boxesTag::getCompoundTagAt).map(boxTag -> {
            Box boxBox = new Box();
            boxBox.initialize(boxTag.getCompoundTag("box"));
            VolumeBox box = new VolumeBox(boxBox, boxTag.hasKey("player") ? boxTag.getString("player") : null);
            if (boxTag.hasKey("oldMin")) {
                box.oldMin = NBTUtil.getPosFromTag(boxTag.getCompoundTag("oldMin"));
            }
            if (boxTag.hasKey("oldMax")) {
                box.oldMax = NBTUtil.getPosFromTag(boxTag.getCompoundTag("oldMax"));
            }
            if (boxTag.hasKey("held")) {
                box.held = NBTUtil.getPosFromTag(boxTag.getCompoundTag("held"));
            }
            box.dist = boxTag.getDouble("dist");
            return box;
        }).forEach(boxes::add);
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
