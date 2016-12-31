/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.SchematicBlock;
import buildcraft.api.bpt.SchematicException;
import buildcraft.api.bpt.SchematicFactoryWorldBlock;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.builders.item.ItemBlueprint.BptStorage;
import buildcraft.lib.bpt.Blueprint;
import buildcraft.lib.bpt.builder.SchematicEntityOffset;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.BoxIterator;
import buildcraft.lib.misc.data.EnumAxisOrder;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileArchitect_Neptune extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final int NET_BOX = 20;
    public static final int NET_SCAN = 21;

    private static final int BLOCKS_PER_TICK = 3;

    public final IItemHandlerModifiable invBptIn = itemManager.addInvHandler("bptIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final IItemHandlerModifiable invBptOut = itemManager.addInvHandler("bptOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public boolean shouldScanEntities = false;
    /** Details- if true then this will create a blueprint, otherwise it will be a template. */
    private boolean shouldScanDetails = true;
    private final Box box = new Box();
    private List<SchematicEntityOffset> blueprintScannedEntities;
    private SchematicBlock[][][] blueprintScannedBlocks;
    private boolean[][][] templateScannedBlocks;
    private BoxIterator boxIterator;
    private boolean isValid = false;
    private boolean scanning = false;
    private boolean shouldStartScanning = false;
    public String name = "<unnamed>";
    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.GUI_ONLY);

    public TileArchitect_Neptune() {}

    private int maxBlocksPerTick() {
        return shouldScanDetails ? BLOCKS_PER_TICK : BLOCKS_PER_TICK * 3;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invBptIn) {
            if (after != null) { // TODO: check item
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
        EnumFacing facing = world.getBlockState(getPos()).getValue(BlockArchitect_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = world.getTileEntity(areaPos);
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            provider.removeFromWorld();
            isValid = true;
            sendNetworkUpdate(NET_BOX);
        } else {
            isValid = false;
            IBlockState state = world.getBlockState(getPos());
            state = state.withProperty(BlockArchitect_Neptune.PROP_VALID, Boolean.FALSE);
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
            size /= maxBlocksPerTick();
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
        for (int i = maxBlocksPerTick(); i > 0; i--) {
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
        if (shouldScanDetails) {
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
        IBlockState state = world.getBlockState(worldScanPos);
        SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(state.getBlock());
        if (factory == null) {
            return null;// SchematicAir.INSTANCE;
        } else {
            try {
                return factory.createFromWorld(getWorld(), worldScanPos);
            } catch (SchematicException e) {
                e.printStackTrace();// TEMP!
                return null;//SchematicAir.INSTANCE;
            }
        }
    }

    private void scanEntities() {
        blueprintScannedEntities = new ArrayList<>();
        // TODO: Scan all entities
    }

    private void finishScanning() {
        EnumFacing direction = EnumFacing.NORTH;
        IBlockState state = world.getBlockState(getPos());
        if (state.getBlock() == BCBuildersBlocks.architect) {
            direction = state.getValue(BlockArchitect_Neptune.PROP_FACING);
        }
        if (shouldScanDetails) {
            BlockPos bptPos = getPos().add(direction.getOpposite().getDirectionVec());

            BlockPos diff = box.min().subtract(bptPos);
            Blueprint bpt = new Blueprint(diff, blueprintScannedBlocks, blueprintScannedEntities);
            blueprintScannedBlocks = null;
            blueprintScannedEntities = null;
            bpt.facing = direction;

            NBTTagCompound tag = bpt.serializeNBT();
            BptStorage storage = BCBuildersItems.blueprint.createStorage(tag);
            ItemStack stack = storage.save();
            BCBuildersItems.blueprint.setName(stack, name);

            invBptIn.setStackInSlot(0, null);
            invBptOut.setStackInSlot(0, stack);
        } else {
            // Template tpl = new Template();
            throw new IllegalStateException("// TODO: This :D");
        }

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
        nbt.setBoolean("shouldScanDetails", shouldScanDetails);
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
        shouldScanDetails = nbt.getBoolean("shouldScanDetails");
        shouldScanEntities = nbt.getBoolean("shouldScanEntities");
        isValid = nbt.getBoolean("isValid");
        name = nbt.getString("name");
    }

    public void setScanDetails(boolean scanDetails) {
        if (!scanning) {
            shouldScanDetails = scanDetails;
        }
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

    @SideOnly(Side.CLIENT)
    public Box getScanningBox() {
        return box;
    }

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
