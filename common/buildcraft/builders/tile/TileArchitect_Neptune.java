/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

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
import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.builders.item.ItemBlueprint.BptStorage;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.bpt.Blueprint;
import buildcraft.lib.bpt.LibraryEntryBlueprint;
import buildcraft.lib.bpt.builder.SchematicEntityOffset;
import buildcraft.lib.bpt.vanilla.SchematicAir;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.BoxIterator;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileArchitect_Neptune extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    public static final int NET_BOX = 20;
    public static final int NET_SCAN = 21;

    public final IItemHandlerModifiable invBptIn = addInventory("bptIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final IItemHandlerModifiable invBptOut = addInventory("bptOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

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
    public String name = "<unnamed>";
    protected int progress = 0;
    public final DeltaInt deltaProgress = deltaManager.addDelta("progress", DeltaManager.EnumNetworkVisibility.GUI_ONLY);

    public TileArchitect_Neptune() {}

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invBptIn) {
            if (after != null) { // TODO: check item
                scanning = true;
            } else {
                scanning = false;
            }
        }
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer.worldObj.isRemote) {
            return;
        }
        EnumFacing facing = worldObj.getBlockState(getPos()).getValue(BlockArchitect_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = worldObj.getTileEntity(areaPos);
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
//            provider.removeFromWorld(); // TODO: uncomment
            isValid = true;
            sendNetworkUpdate(NET_BOX);
        } else {
            isValid = false;
            IBlockState state = worldObj.getBlockState(getPos());
            state = state.withProperty(BlockArchitect_Neptune.PROP_VALID, Boolean.FALSE);
            worldObj.setBlockState(getPos(), state);
        }
    }

    @Override
    public void update() {
        deltaProgress.tick();

        if (worldObj.isRemote) {
            return;
        }
        if (scanning && isValid) {
            int target = shouldScanDetails ? 30 : 10;
            int back = 3;
            if (progress == 0) {
                deltaProgress.addDelta(0, target, 100);
                deltaProgress.addDelta(target, target + back, -100);
            }
            if (progress < target + back) {
                progress++;
                return;
            }
            progress = 0;
            scanSingleBlock();
            if (!scanning) {
                if (shouldScanEntities) {
                    scanEntities();
                }
                finishScanning();
            }
        } else if (progress != -1) {
            progress = -1;
            deltaProgress.setValue(0);
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (blueprintScannedBlocks == null) {
            blueprintScannedBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
            boxIterator = new BoxIterator(box, EnumAxisOrder.XZY.defaultOrder, true);
            templateScannedBlocks = new boolean[size.getX()][size.getY()][size.getZ()];
        }

        // Read from world
        BlockPos worldScanPos = boxIterator.getCurrent();
        BlockPos schematicIndex = worldScanPos.subtract(box.min());
        if (shouldScanDetails) {
            SchematicBlock schematic = readSchematicForBlock(worldScanPos);

            blueprintScannedBlocks[schematicIndex.getX()][schematicIndex.getY()][schematicIndex.getZ()] = schematic;
        } else {
            boolean solid = !worldObj.isAirBlock(worldScanPos);
            templateScannedBlocks[schematicIndex.getX()][schematicIndex.getY()][schematicIndex.getZ()] = solid;
        }

        createAndSendMessage(false, NET_SCAN, (buffer) -> {
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
        IBlockState state = worldObj.getBlockState(worldScanPos);
        SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(state.getBlock());
        if (factory == null) {
            return SchematicAir.INSTANCE;
        } else {
            try {
                return factory.createFromWorld(getWorld(), worldScanPos);
            } catch (SchematicException e) {
                e.printStackTrace();// TEMP!
                return SchematicAir.INSTANCE;
            }
        }
    }

    private void scanEntities() {
        blueprintScannedEntities = new ArrayList<>();
        // TODO: Scan all entities
    }

    private void finishScanning() {
        EnumFacing direction = EnumFacing.NORTH;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state.getBlock() == BCBuildersBlocks.architect) {
            direction = state.getValue(BlockArchitect_Neptune.PROP_FACING);
        }
        if (shouldScanDetails) {
            Blueprint bpt = new Blueprint(blueprintScannedBlocks, blueprintScannedEntities);
            blueprintScannedBlocks = null;
            blueprintScannedEntities = null;
            Rotation rotation = Rotation.NONE;
            while (direction != EnumFacing.NORTH) {
                direction = direction.rotateY();
                rotation = rotation.add(Rotation.CLOCKWISE_90);
            }
            if (rotation != Rotation.NONE) {
                bpt.rotate(Axis.Y, rotation);
            }

            LibraryEntryBlueprint data = new LibraryEntryBlueprint(bpt);
            LibraryEntryHeader header = new LibraryEntryHeader(name, LibraryEntryBlueprint.KIND, LocalDateTime.now(), getOwner());
            BCLibDatabase.LOCAL_DB.addNew(header, data);

            BptStorage storage = new BptStorage(header);
            ItemStack stack = storage.save();

            invBptIn.setStackInSlot(0, null);
            invBptOut.setStackInSlot(0, stack);
        } else {
            // Template tpl = new Template();
            throw new IllegalStateException("// TODO: This :D");
        }

        sendNetworkUpdate(NET_RENDER_DATA);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_BOX) {
                box.writeData(buffer);
            } else if (id == NET_RENDER_DATA) {
                if(boxIterator != null) {
                    buffer.writeBoolean(true);
                    boxIterator.writeToByteBuf(buffer);
                } else {
                    buffer.writeBoolean(false);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_BOX) {
                box.readData(buffer);
            } else if (id == NET_RENDER_DATA) {
                if(buffer.readBoolean()) {
                    boxIterator = (BoxIterator) new BoxIterator().readFromByteBuf(buffer);
                } else {
                    boxIterator = null;
                }
            } else if (id == NET_SCAN) {
                BlockPos pos = buffer.readBlockPos();
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                worldObj.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
            }
        }
    }

    public void setScanDetails(boolean scanDetails) {
        if (!scanning) {
            shouldScanDetails = scanDetails;
        }
    }

    @SideOnly(Side.CLIENT)
    public Box getScanningBox() {
        return box;
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
        left.add("progress = " + progress);
    }

    public double getAllProgress() {
        if(boxIterator != null && boxIterator.getCurrent() != null) {
            // TODO: optimize?
            BoxIterator copy = new BoxIterator(boxIterator);
            int count = 0;
            while(copy.getCurrent() != null) {
                copy.advance();
                count++;
            }
            count--;
            BlockPos boxSize = new Box(boxIterator.getMin(), boxIterator.getMax()).size();
            int all = boxSize.getX() * boxSize.getY() * boxSize.getZ();
            return (int) ((float) (all - count) / all * 100);
        } else {
            return 0;
        }
    }
}
