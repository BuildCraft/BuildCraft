package buildcraft.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.bpt.*;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.block.BlockArchitect_Neptune;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;
import buildcraft.lib.bpt.vanilla.SchematicAir;
import buildcraft.lib.misc.BoxIterator;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

public class TileArchitect_Neptune extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    public static final int NET_BOX = 20;
    public static final int NET_SCAN = 21;
    private static final int BLOCKS_PER_TICK = 0;

    protected final IItemHandlerModifiable invBptIn = addInventory("bptIn", 1, EnumAccess.INSERT, EnumPipePart.VALUES);
    protected final IItemHandlerModifiable invBptOut = addInventory("bptOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

    private final Box box = new Box();
    private List<SchematicEntity<?>> scannedEntitites;
    private SchematicBlock[][][] scannedBlocks;
    private BoxIterator boxIterator;
    private boolean isValid = false;
    private boolean scanning = false;

    public TileArchitect_Neptune() {}

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invBptIn) {
            if (after != null) {
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
        BCLog.logger.info("onPlacedBy " + placer + ", " + stack);
        EnumFacing facing = worldObj.getBlockState(getPos()).getValue(BlockArchitect_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = worldObj.getTileEntity(areaPos);
        BCLog.logger.info("foundPos = " + areaPos + " -> " + (tile == null ? "null" : tile.getClass()));
        if (tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            provider.removeFromWorld();
            scanning = true;// TEMP!
            isValid = true;
            sendNetworkUpdate(NET_BOX);
        } else {
            isValid = false;
        }
    }

    @Override
    public void update() {
        if (scanning && isValid) {
            scanMultipleBlocks();
            if (!scanning) {
                scanEntities();
                finishScanning();
            }
        }
    }

    private void scanMultipleBlocks() {
        for (int i = BLOCKS_PER_TICK; i > 0; i--) {
            scanSingleBlock();
            if (!scanning) {
                break;
            }
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (scannedBlocks == null) {
            scannedBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
            boxIterator = new BoxIterator(box, EnumAxisOrder.XZY.defaultOrder);
        }

        // Read from world
        BlockPos worldScanPos = boxIterator.getCurrent();
        SchematicBlock schematic = readSchematicForBlock(worldScanPos);

        BlockPos schematicIndex = worldScanPos.subtract(box.min());
        scannedBlocks[schematicIndex.getX()][schematicIndex.getY()][schematicIndex.getZ()] = schematic;

        createAndSendMessage(false, NET_SCAN, (buffer) -> {
            buffer.writeBlockPos(worldScanPos);
        });

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
        scannedEntitites = new ArrayList<>();
        // TODO: Scan all entities
    }

    private void finishScanning() {
        // TODO: write out the blueprint
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_BOX) {
                box.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_BOX) {
                box.readData(buffer);
            } else if (id == NET_SCAN) {
                BlockPos pos = buffer.readBlockPos();
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                worldObj.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("Box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("Scanning = " + scanning);
        left.add("Scan Pos = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }
}
