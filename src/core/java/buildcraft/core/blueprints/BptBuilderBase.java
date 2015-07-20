/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import java.util.BitSet;

import org.apache.logging.log4j.Level;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IAreaProvider;
import buildcraft.core.Box;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.proxy.CoreProxy;

public abstract class BptBuilderBase implements IAreaProvider {

    public BlueprintBase blueprint;
    public BptContext context;
    protected BitSet usedLocations;
    protected boolean done;
    protected BlockPos pos;
    protected boolean initialized = false;

    private long nextBuildDate = 0;

    public BptBuilderBase(BlueprintBase bluePrint, World world, BlockPos pos) {
        this.blueprint = bluePrint;
        this.pos = pos;
        this.usedLocations = new BitSet(bluePrint.sizeX * bluePrint.sizeY * bluePrint.sizeZ);
        done = false;

        Box box = new Box();
        box.initialize(this);

        context = bluePrint.getContext(world, box);
    }

    @Deprecated
    protected boolean isLocationUsed(int x, int y, int z) {
        return isLocationUsed(new BlockPos(x, y, z));
    }

    protected boolean isLocationUsed(BlockPos toTest) {
        int xCoord = toTest.getX() - pos.getX() + blueprint.anchorX;
        int yCoord = toTest.getY() - pos.getY() + blueprint.anchorY;
        int zCoord = toTest.getZ() - pos.getZ() + blueprint.anchorZ;
        return usedLocations.get((zCoord * blueprint.sizeY + yCoord) * blueprint.sizeX + xCoord);
    }

    @Deprecated
    protected void markLocationUsed(int x, int y, int z) {
        markLocationUsed(new BlockPos(x, y, z));
    }

    protected void markLocationUsed(BlockPos toMark) {
        int xCoord = toMark.getX() - pos.getX() + blueprint.anchorX;
        int yCoord = toMark.getY() - pos.getY() + blueprint.anchorY;
        int zCoord = toMark.getZ() - pos.getZ() + blueprint.anchorZ;
        usedLocations.set((zCoord * blueprint.sizeY + yCoord) * blueprint.sizeX + xCoord, true);
    }

    public void initialize() {
        if (!initialized) {
            internalInit();
            initialized = true;
        }
    }

    protected abstract void internalInit();

    protected abstract BuildingSlot reserveNextBlock(World world);

    protected abstract BuildingSlot getNextBlock(World world, TileAbstractBuilder inv);

    public boolean buildNextSlot(World world, TileAbstractBuilder builder, double x, double y, double z) {
        initialize();

        if (world.getTotalWorldTime() < nextBuildDate) {
            return false;
        }

        BuildingSlot slot = getNextBlock(world, builder);

        if (buildSlot(world, builder, slot, x + 0.5F, y + 0.5F, z + 0.5F)) {
            nextBuildDate = world.getTotalWorldTime() + slot.buildTime();
            return true;
        } else {
            return false;
        }
    }

    public boolean buildSlot(World world, IBuildingItemsProvider builder, BuildingSlot slot, double x, double y, double z) {
        initialize();

        if (slot != null) {
            slot.built = true;
            BuildingItem i = new BuildingItem();
            i.origin = new Vec3(x, y, z);
            i.destination = slot.getDestination();
            i.slotToBuild = slot;
            i.context = getContext();
            i.setStacksToDisplay(slot.getStacksToDisplay());
            builder.addAndLaunchBuildingItem(i);

            return true;
        }

        return false;
    }

    public BuildingSlot reserveNextSlot(World world) {
        initialize();

        return reserveNextBlock(world);
    }

    @Override
    public int xMin() {
        return pos.getX() - blueprint.anchorX;
    }

    @Override
    public int yMin() {
        return pos.getY() - blueprint.anchorY;
    }

    @Override
    public int zMin() {
        return pos.getZ() - blueprint.anchorZ;
    }

    @Override
    public int xMax() {
        return pos.getX() + blueprint.sizeX - blueprint.anchorX - 1;
    }

    @Override
    public int yMax() {
        return pos.getY() + blueprint.sizeY - blueprint.anchorY - 1;
    }

    @Override
    public int zMax() {
        return pos.getZ() + blueprint.sizeZ - blueprint.anchorZ - 1;
    }

    @Override
    public void removeFromWorld() {

    }

    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(xMin(), yMin(), zMin(), xMax(), yMax(), zMax());
    }

    public void postProcessing(World world) {

    }

    public BptContext getContext() {
        return context;
    }

    public boolean isDone(IBuildingItemsProvider builder) {
        return done && builder.getBuilders().size() == 0;
    }

    private int getBlockBreakEnergy(BuildingSlotBlock slot) {
        return BlockUtils.computeBlockBreakEnergy(context.world(), slot.pos);
    }

    protected final boolean canDestroy(TileAbstractBuilder builder, IBuilderContext context, BuildingSlotBlock slot) {
        return builder.energyAvailable() >= getBlockBreakEnergy(slot);
    }

    public void consumeEnergyToDestroy(TileAbstractBuilder builder, BuildingSlotBlock slot) {
        builder.consumeEnergy(getBlockBreakEnergy(slot));
    }

    public void createDestroyItems(BuildingSlotBlock slot) {
        int hardness = (int) Math.ceil(getBlockBreakEnergy(slot) / BuilderAPI.BREAK_ENERGY);

        for (int i = 0; i < hardness; ++i) {
            slot.addStackConsumed(new ItemStack(BuildCraftCore.buildToolBlock));
        }
    }

    public void useRequirements(IInventory inv, BuildingSlot slot) {

    }

    public void saveBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
        NBTTagList clearList = new NBTTagList();

        nbt.setByteArray("usedLocationList", BitSetUtils.toByteArray(usedLocations));

        NBTTagList buildingList = new NBTTagList();

        for (BuildingItem item : builder.getBuilders()) {
            NBTTagCompound sub = new NBTTagCompound();
            item.writeToNBT(sub);
            buildingList.appendTag(sub);
        }

        nbt.setTag("buildersInAction", buildingList);
    }

    public void loadBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
        if (nbt.hasKey("usedLocationList")) {
            usedLocations = BitSetUtils.fromByteArray(nbt.getByteArray("usedLocationList"));
        }

        NBTTagList buildingList = nbt.getTagList("buildersInAction", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < buildingList.tagCount(); ++i) {
            BuildingItem item = new BuildingItem();

            try {
                item.readFromNBT(buildingList.getCompoundTagAt(i));
                item.context = getContext();
                builder.getBuilders().add(item);
            } catch (MappingNotFoundException e) {
                BCLog.logger.log(Level.WARN, "can't load building item", e);
            }
        }

        // 6.4.6 and below migration

        if (nbt.hasKey("clearList")) {
            NBTTagList clearList = nbt.getTagList("clearList", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < clearList.tagCount(); ++i) {
                NBTBase cpt = clearList.get(i);
                BlockPos o = NBTUtils.readBlockPos(cpt);
                markLocationUsed(o.getX(), o.getY(), o.getZ());
            }
        }

        if (nbt.hasKey("builtList")) {
            NBTTagList builtList = nbt.getTagList("builtList", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < builtList.tagCount(); ++i) {
                NBTBase cpt = builtList.get(i);
                BlockPos o = NBTUtils.readBlockPos(cpt);
                markLocationUsed(o.getX(), o.getY(), o.getZ());
            }
        }
    }

    protected boolean isBlockBreakCanceled(World world, BlockPos pos) {
        if (!world.isAirBlock(pos)) {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), CoreProxy.proxy.getBuildCraftPlayer(
                    (WorldServer) world).get());
            MinecraftForge.EVENT_BUS.post(breakEvent);
            return breakEvent.isCanceled();
        }
        return false;
    }

    protected boolean isBlockPlaceCanceled(World world, BlockPos pos, SchematicBlockBase schematic) {
        IBlockState state = schematic instanceof SchematicBlock ? ((SchematicBlock) schematic).state : Blocks.stone.getDefaultState();

        EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world, pos).get();

        BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(new BlockSnapshot(world, pos, state), Blocks.air.getDefaultState(), player);

        MinecraftForge.EVENT_BUS.post(placeEvent);
        return placeEvent.isCanceled();
    }
}
