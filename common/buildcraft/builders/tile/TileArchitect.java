/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.block.BlockArchitect;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.schematic.*;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TileArchitect extends TileBC_Neptune implements ITickable, IDebuggable {
    public AddonFillingPlanner addon;
    public static final int NET_BOX = 20;
    public static final int NET_SCAN = 21;

    private static final int BLOCKS_PER_TICK = 3;

    public final ItemHandlerSimple invBptIn = itemManager.addInvHandler("bptIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invBptOut = itemManager.addInvHandler("bptOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public boolean shouldScanEntities = false;
    private Snapshot.EnumSnapshotType snapshotType = Snapshot.EnumSnapshotType.BLUEPRINT;
    private final Box box = new Box();
//    private List<SchematicEntityOffset> blueprintScannedEntities;
    private SchematicBlock[][][] blueprintScannedBlocks;
    private boolean[][][] templateScannedBlocks;
    private BoxIterator boxIterator;
    private boolean isValid = false;
    private boolean scanning = false;
    private boolean shouldStartScanning = false;
    public String name = "<unnamed>";
    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.GUI_ONLY);

    public TileArchitect() {}

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invBptIn) {
            if (after.getItem() instanceof ItemSnapshot) {
                shouldStartScanning = true;
            } else {
                scanning = false;
            }
        }
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.world.isRemote) {
            return;
        }
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(world);
        IBlockState blockState = world.getBlockState(getPos());
        VolumeBox volumeBox = volumeBoxes.getBoxAt(getPos().offset(blockState.getValue(BlockArchitect.PROP_FACING).getOpposite()));
        if (volumeBox != null) {
            box.reset();
            box.setMin(volumeBox.box.min());
            box.setMax(volumeBox.box.max());
            isValid = true;
            volumeBox.locks.add(
                    new Lock(
                            new Lock.LockCause.LockCauseBlock(pos, blockState.getBlock()),
                            new Lock.LockTarget.LockTargetResize(),
                            new Lock.LockTarget.LockTargetUsedByMachine()
                    )
            );
            volumeBoxes.markDirty();
            sendNetworkUpdate(NET_BOX);
        } else {
            isValid = false;
            IBlockState state = world.getBlockState(getPos());
            state = state.withProperty(BlockArchitect.PROP_VALID, Boolean.FALSE);
            world.setBlockState(getPos(), state);
        }
    }

    @Override
    public void update() {
        deltaProgress.tick();

        if (world.isRemote) {
            return;
        }

        if (shouldStartScanning && isValid) {
            int size = box.size().getX() * box.size().getY() * box.size().getZ();
            size /= snapshotType.maxPerTick;
            deltaProgress.addDelta(0, size, 100);
            deltaProgress.addDelta(size, size + 10, -100);
            shouldStartScanning = false;
            scanning = true;
        }

        if (scanning) {
            scanMultipleBlocks();
            if (!scanning) {
                if (shouldScanEntities) {
                    scanEntities();
                }
                finishScanning();
            }
        }
    }

    private void scanMultipleBlocks() {
        for (int i = snapshotType.maxPerTick; i > 0; i--) {
            scanSingleBlock();
            if (!scanning) {
                break;
            }
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (blueprintScannedBlocks == null) {
            blueprintScannedBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
            boxIterator = new BoxIterator(box, EnumAxisOrder.XZY.getMinToMaxOrder(), true);
            templateScannedBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
        }

        // Read from world
        BlockPos worldScanPos = boxIterator.getCurrent();
        BlockPos schematicIndex = worldScanPos.subtract(box.min());
        if (snapshotType == Snapshot.EnumSnapshotType.BLUEPRINT) {
            SchematicBlock schematic = readSchematicForBlock(worldScanPos);
            blueprintScannedBlocks[schematicIndex.getX()][schematicIndex.getY()][schematicIndex.getZ()] = schematic;
        } else {
            boolean solid = !world.isAirBlock(worldScanPos);
            templateScannedBlocks[schematicIndex.getX()][schematicIndex.getY()][schematicIndex.getZ()] = solid;
        }

        createAndSendMessage(NET_SCAN, (buffer) -> {
            buffer.writeBlockPos(worldScanPos);
        });

        sendNetworkUpdate(NET_RENDER_DATA);

        // Move scanPos along
        boxIterator.advance();

        if (boxIterator.hasFinished()) {
            scanning = false;
            boxIterator = null;
        }
    }

    private SchematicBlock readSchematicForBlock(BlockPos worldScanPos) {
//        IBlockState state = worldObj.getBlockState(worldScanPos);
//        SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(state.getBlock());
//        if (factory == null) {
//            return SchematicAir.INSTANCE;
//        } else {
//            try {
//                return factory.createFromWorld(getWorld(), worldScanPos);
//            } catch (SchematicException e) {
//                e.printStackTrace();// TEMP!
//                return SchematicAir.INSTANCE;
//            }
//        }
        Block block = world.getBlockState(worldScanPos).getBlock();
        SchematicBlockContext schematicBlockContext = new SchematicBlockContext(world, worldScanPos, pos);
        return SchematicsLoader.INSTANCE.schematicFactories.get(block).apply(schematicBlockContext);
    }

    private void scanEntities() {
//        blueprintScannedEntities = new ArrayList<>();
        // TODO: Scan all entities
    }

    private void finishScanning() {
        EnumFacing facing = world.getBlockState(getPos()).getValue(BlockArchitect.PROP_FACING);
        Snapshot snapshot = snapshotType.create.get();
        snapshot.header = new Snapshot.Header();
        snapshot.header.id = UUID.randomUUID();
        snapshot.header.owner = getOwner().getId();
        snapshot.header.created = new Date();
        snapshot.header.name = name;
        GlobalSavedDataSnapshots.get(world).snapshots.add(snapshot);
        invBptIn.setStackInSlot(0, ItemStack.EMPTY);
        invBptOut.setStackInSlot(0, BCBuildersItems.snapshot.getUsed(snapshotType, snapshot.header));
        blueprintScannedBlocks = null;
        templateScannedBlocks = null;
        boxIterator = null;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                box.writeData(buffer);
            }
            if (id == NET_BOX) {
                box.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                box.readData(buffer);
            } else if (id == NET_BOX) {
                box.readData(buffer);
            } else if (id == NET_SCAN) {
                BlockPos pos = buffer.readBlockPos();
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", box.writeToNBT());
        if (boxIterator != null) {
            nbt.setTag("iter", boxIterator.writeToNbt());
        }
        nbt.setBoolean("shouldStartScanning", shouldStartScanning);
        nbt.setBoolean("scanning", scanning);
        nbt.setTag("snapshotType", NBTUtilBC.writeEnum(snapshotType));
        nbt.setBoolean("shouldScanEntities", shouldScanEntities);
        nbt.setBoolean("isValid", isValid);
        nbt.setString("name", name);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        box.initialize(nbt.getCompoundTag("box"));
        if (nbt.hasKey("iter")) {
            boxIterator = BoxIterator.readFromNbt(nbt.getCompoundTag("iter"));
        }
        shouldStartScanning = nbt.getBoolean("shouldStartScanning");
        scanning = nbt.getBoolean("scanning");
        snapshotType = NBTUtilBC.readEnum(nbt.getTag("snapshotType"), Snapshot.EnumSnapshotType.class);
        shouldScanEntities = nbt.getBoolean("shouldScanEntities");
        isValid = nbt.getBoolean("isValid");
        name = nbt.getString("name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("scanning = " + scanning);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return BoundingBoxUtil.makeFrom(getPos(), box);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }
}
