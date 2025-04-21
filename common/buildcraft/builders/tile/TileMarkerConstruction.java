/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.tile;

import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.tiles.ITickable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.snapshot.*;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class TileMarkerConstruction extends TileBC_Neptune implements ITickable, ITileForBlueprintBuilder {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("marker_construction");
    public static final int NET_UPLOAD_BUILDERS_IN_ACTION = IDS.allocId("UPLOAD_BUILDERS_IN_ACTION");
    public static final int NET_LAUNCH_ITEM = IDS.allocId("LAUNCH_ITEM");
    public static final int NET_LASER_BOX_UPDATE = IDS.allocId("LASER_UPDATE");

    public static HashSet<TileMarkerConstruction> currentMarkers = new HashSet<TileMarkerConstruction>();

    @Nonnull
    public Direction direction = Direction.NORTH;

    // public LaserData_BC8 laser;
    public Pair<Vec3, Vec3> laser;
    @Nonnull
    public ItemStack itemBlueprint = StackUtil.EMPTY;
    @Nonnull
    public Box box = new Box();

    public BlueprintBuilder bluePrintBuilder;
    // public BptContext bptContext;
    public Blueprint.BuildingInfo bptContext;

    // private ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();
    private CompoundTag initNBT;

    // Calen 1.18.2: to replace LaserData#isVisible
    public boolean isLaserVisible;

    // Calen 1.18.2
    private EntityRobotBase robot = null;
    private static final MjBattery EMPTY_BATTERY = new MjBattery(0);
    private static final IItemTransactor EMPTY_INV = NoSpaceTransactor.INSTANCE;
    private static final TankManager EMPTY_TANK_MANAGER = new TankManager();
    private boolean canExcavate = true;

    public TileMarkerConstruction(BlockPos pos, BlockState blockState) {
        super(BCBuildersBlocks.markerConstructionTile.get(), pos, blockState);
        // box.kind = Kind.BLUE_STRIPES;

        // if (level.isClientSide) {
        //     MessageManager.sendToServer(new PacketCommand(this, NET_UPLOAD_BUILDERS_IN_ACTION, null));
        // }
        runWhenWorldNotNull(() -> {
                    if (level.isClientSide) {
                        MessageManager.sendToServer(createMessage(NET_UPLOAD_BUILDERS_IN_ACTION, (buffer -> {})));
                    }
                },
                false
        );
    }

    // private MessageUpdateTile createLaunchItemPacket(final BuildingItem i)
    private MessageUpdateTile createLaunchItemPacket() {
        return createMessage(NET_LAUNCH_ITEM, (data) -> getBuilder().writeToByteBuf(data));
    }

    @Override
    public void update() {
        ITickable.super.update();

//        BuildingItem toRemove = null;

//        for (BuildingItem i : buildersInAction) {
//            i.update();
//
//            if (i.isDone) {
//                toRemove = i;
//            }
//        }

//        if (toRemove != null) {
//            buildersInAction.remove(toRemove);
//        }

        if (level.isClientSide) {
            return;
        }

        // if (itemBlueprint != null && ItemSnapshot.getId(itemBlueprint) != null && bluePrintBuilder == null)
        Snapshot.Header header = BCBuildersItems.snapshotBLUEPRINT.get().getHeader(itemBlueprint);
        // if (itemBlueprint != null && header != null && bluePrintBuilder == null)
        if (!itemBlueprint.isEmpty() && header != null && bluePrintBuilder == null) {
            // BlueprintBase bpt = ItemBlueprint.loadBlueprint(itemBlueprint);
            Snapshot bpt = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
            if (bpt instanceof Blueprint) {
                // bpt = bpt.adjustToWorld(level, worldPosition, direction);
                if (bpt != null) {
                    // bluePrintBuilder = new BlueprintBuilder((Blueprint) bpt, level, worldPosition);
                    bluePrintBuilder = new BlueprintBuilder(this);
                    // bptContext = bluePrintBuilder.getContext();
                    Snapshot _bpt = bpt;
                    // Blueprint#facing is the facing of the TileArchitect, opposite to the looking direction of the player used the TileArchitect
                    Rotation rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(_bpt.facing.getOpposite()) == direction).findFirst().orElse(null);
                    bptContext = ((Blueprint) bpt).new BuildingInfo(worldPosition.relative(direction), rotation);
                    // box.initialize(bluePrintBuilder);
                    box.initialize(bptContext.box);
                    bluePrintBuilder.updateSnapshot();
                    bluePrintBuilder.tick(); // to prepare tasks
                    // sendNetworkUpdate();
                    sendNetworkUpdate(NET_LASER_BOX_UPDATE);
                }
            } else {
                return;
            }
        }

        if (laser == null && direction != null) {
            Vec3 point5 = new Vec3(0.5, 0.5, 0.5);
            Vec3 start = VecUtil.convert(worldPosition).add(point5);
            Vec3 end = start.add(VecUtil.convert(direction, 0.5));
            // laser = new LaserData_BC8(LASER_TYPE, start, end, 1.0 / 16.0);
            laser = Pair.of(start, end);
//            laser.isVisible = true;
            this.isLaserVisible = true;
            // sendNetworkUpdate();
            sendNetworkUpdate(NET_LASER_BOX_UPDATE);
        }

        if (initNBT != null) {
            if (bluePrintBuilder != null) {
                bluePrintBuilder.deserializeNBT(initNBT.getCompound("builderState"));
            }

            initNBT = null;
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putByte("direction", (byte) direction.ordinal());
        nbt.putBoolean("canExcavate", canExcavate);

        // if (itemBlueprint != null)
        if (!itemBlueprint.isEmpty()) {
            CompoundTag bptNBT = new CompoundTag();
            itemBlueprint.save(bptNBT);
            nbt.put("itemBlueprint", bptNBT);
        }

        CompoundTag bptNBT = new CompoundTag();

        if (bluePrintBuilder != null) {
            bptNBT.put("builderState", bluePrintBuilder.serializeNBT());
        }

        nbt.put("bptBuilder", bptNBT);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        direction = Direction.values()[(nbt.getByte("direction"))];
        canExcavate = nbt.getBoolean("canExcavate");

        if (nbt.contains("itemBlueprint")) {
            itemBlueprint = ItemStack.of(nbt.getCompound("itemBlueprint"));
        }

        // The rest of load has to be done upon initialize.
        initNBT = nbt.getCompound("bptBuilder").copy();
    }

    // public void setBlueprint(ItemStack currentItem)
    public void setBlueprint(@Nonnull ItemStack currentItem) {
        itemBlueprint = currentItem;
        // sendNetworkUpdate();
        sendNetworkUpdate(NET_LASER_BOX_UPDATE);
        setChanged();
    }

    @Override
    // public ArrayList<BuildingItem> getBuilders()
    public SnapshotBuilder<?> getBuilder() {
        // return buildersInAction;
        return bluePrintBuilder;
    }

    @Override
    // public void validate()
    public void clearRemoved() {
        // super.validate();
        super.clearRemoved();
        if (!level.isClientSide) {
            currentMarkers.add(this);
        }
    }

    @Override
    // public void invalidate()
    public void setRemoved() {
        // super.invalidate();
        super.setRemoved();
        if (!level.isClientSide) {
            currentMarkers.remove(this);
        }
    }

    public boolean needsToBuild() {
        // return !isRemoved() && bluePrintBuilder != null && !bluePrintBuilder.isDone(this);
        if (isRemoved() || bluePrintBuilder == null) {
            return false;
        }
        if (this.robot == null) {
            this.bluePrintBuilder.updateSnapshot();
            this.bluePrintBuilder.tick();
        }
        return !(bluePrintBuilder.leftToBreak <= 0 && bluePrintBuilder.leftToPlace <= 0);
    }

    @Override
    // public BptContext getContext()
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return bptContext;
    }

//    @Override
//    public void addAndLaunchBuildingItem(BuildingItem item) {
//        buildersInAction.add(item);
//        // BuildCraftCore.instance.sendToPlayersNear(createLaunchItemPacket(item), this);
//        MessageManager.sendToAllAround(createLaunchItemPacket(item), new PacketDistributor.TargetPoint(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 64, this.level.dimension()));
//    }

    @Override
    // public void receiveCommand(String command, Side side, Object sender, ByteBuf stream)
    public void readPayload(int command, PacketBufferBC stream, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(command, stream, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT && command == NET_RENDER_DATA) {
            readPayload(NET_LASER_BOX_UPDATE, stream, side, ctx);
        }
        // if (side.isServer() && "uploadBuildersInAction".equals(command))
        else if (side == NetworkDirection.PLAY_TO_SERVER && command == NET_UPLOAD_BUILDERS_IN_ACTION) {
            // for (BuildingItem i : buildersInAction) {
            //     MessageManager.sendTo(createLaunchItemPacket(i), ctx.getSender());
            // }
            if (bluePrintBuilder != null) {
                // MessageManager.sendTo(createLaunchItemPacket(), ctx.getSender());
                sendNetworkUpdate(NET_LAUNCH_ITEM, ctx.getSender());
            }
        }
        // else if (side.isClient() && "launchItem".equals(command))
        else if (side == NetworkDirection.PLAY_TO_CLIENT && command == NET_LAUNCH_ITEM) {
            // BuildingItem item = new BuildingItem();
            // item.readData(stream);
            // buildersInAction.add(item);
            if (bluePrintBuilder == null) {
                bluePrintBuilder = new BlueprintBuilder(this);
            }
            getBuilder().readFromByteBuf(stream);
        } else if (side == NetworkDirection.PLAY_TO_CLIENT && command == NET_LASER_BOX_UPDATE) {
            box.readData(stream);
            int flags = stream.readUnsignedByte();
            if ((flags & 1) != 0) {
//            laser = new LaserData_BC8();
//            laser.readData(stream);
                laser = Pair.of(MessageUtil.readVec3d(stream), MessageUtil.readVec3d(stream));
            } else {
                laser = null;
            }
            if ((flags & 2) != 0) {
                itemBlueprint = stream.readItem();
            } else {
                // itemBlueprint = null;
                itemBlueprint = StackUtil.EMPTY;
            }
        }
    }

//    @Override
//    public Box getBox() {
//        return box;
//    }

    @Override
    public AABB getRenderBoundingBox() {
        Box renderBox = new Box(this).extendToEncompass(box);

        return renderBox.expand(50).getBoundingBox();
    }

    // Calen: RenderMarkerConstruction#getViewDistance
//    @Override
//    public double getMaxRenderDistanceSquared() {
//        return Double.MAX_VALUE;
//    }

    @Override
    // public void writeData(FriendlyByteBuf stream)
    public void writePayload(int id, PacketBufferBC stream, Dist side) {
        super.writePayload(id, stream, side);
        if (side == Dist.DEDICATED_SERVER && id == NET_RENDER_DATA) {
            writePayload(NET_LASER_BOX_UPDATE, stream, side);
        } else if (side == Dist.DEDICATED_SERVER && id == NET_LASER_BOX_UPDATE) {
            box.writeData(stream);
            // stream.writeByte((laser != null ? 1 : 0) | (itemBlueprint != null ? 2 : 0));
            stream.writeByte((laser != null ? 1 : 0) | (!itemBlueprint.isEmpty() ? 2 : 0));
            if (laser != null) {
                // laser.writeData(stream);
                MessageUtil.writeVec3d(stream, laser.getFirst());
                MessageUtil.writeVec3d(stream, laser.getSecond());
            }
            // if (itemBlueprint != null)
            if (!itemBlueprint.isEmpty()) {
                // NetworkUtils.writeStack(stream, itemBlueprint);
                stream.writeItemStack(itemBlueprint, false);
            }
        } else if (side == Dist.DEDICATED_SERVER && id == NET_LAUNCH_ITEM) {
            getBuilder().writeToByteBuf(stream);
        }
    }

//    @Override
//    public void readData(ByteBuf stream) {
//        box.readData(stream);
//        int flags = stream.readUnsignedByte();
//        if ((flags & 1) != 0) {
//            laser = new LaserData();
//            laser.readData(stream);
//        } else {
//            laser = null;
//        }
//        if ((flags & 2) != 0) {
//            itemBlueprint = NetworkUtils.readStack(stream);
//        } else {
//            itemBlueprint = null;
//        }
//    }

//    @Override
//    public BlockState getBlockState_MIGRATION_ONLY() {
//        return BuildCraftBuilders.constructionMarkerBlock.getDefaultState();
//    }

    @Override
    public Level getWorldBC() {
        return level;
    }

    @Override
    public MjBattery getBattery() {
        if (robot != null && robot.isAlive()) {
            return this.robot == null ? EMPTY_BATTERY : this.robot.getBattery();
        } else {
            return EMPTY_BATTERY;
        }
    }

    @Override
    public BlockPos getBuilderPos() {
        return worldPosition;
    }

    @Override
    public boolean canExcavate() {
        return canExcavate;
    }

    // Calen 1.18.2
    public void preRobotBuild(EntityRobotBase robot) {
        this.robot = robot;
    }

    public void postRobotBuild(EntityRobotBase robot) {
        this.robot = null;
    }

    @Override
    public IItemTransactor getInvResources() {
        if (robot != null && robot.isAlive()) {
            return (IItemTransactor) robot.getCapability(CapUtil.CAP_ITEMS).cast().orElse(EMPTY_INV);
        } else {
            return EMPTY_INV;
        }
    }

    @Override
    public TankManager getTankManager() {
        if (robot != null && robot.isAlive()) {
            return (TankManager) robot.getCapability(CapUtil.CAP_FLUIDS).orElse(EMPTY_TANK_MANAGER);
        } else {
            return EMPTY_TANK_MANAGER;
        }
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @NotNull ItemStack before, @NotNull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (StackUtil.isSameItemSameDamageSameTagSameCount(before, after)) {
            return;
        }
        if (bluePrintBuilder != null) {
            bluePrintBuilder.resourcesChanged();
        }
    }

    public EntityRobotBase getRobotUsingThisMarker() {
        return this.robot;
    }

    public void updateClientBluePrintBuilder() {
        sendNetworkUpdate(NET_LAUNCH_ITEM);
    }
}
