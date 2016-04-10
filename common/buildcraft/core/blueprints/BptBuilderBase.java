/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IAreaProvider;
import buildcraft.core.Box;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.utils.BitSetUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;

public abstract class BptBuilderBase implements IAreaProvider {

    public BlueprintBase blueprint;
    public BptContext context;
    protected BitSet usedLocations;
    protected boolean done;
    protected BlockPos pos;
    protected boolean initialized = false;

    private long nextBuildDate = 0;

    private Utils.AxisOrder order = Utils.EnumAxisOrder.XZY.defaultOrder;

    public BptBuilderBase(BlueprintBase bluePrint, World world, BlockPos pos) {
        this.blueprint = bluePrint;
        this.pos = pos;
        this.usedLocations = new BitSet(bluePrint.size.getX() * bluePrint.size.getY() * bluePrint.size.getZ());
        done = false;

        Box box = new Box();
        box.initialize(this);

        context = bluePrint.getContext(world, box);
    }

    public void setOrder(Utils.AxisOrder order) {
        if (order != null) this.order = order;
    }

    public Utils.AxisOrder getOrder() {
        return order;
    }

    @Deprecated
    protected boolean isLocationUsed(int x, int y, int z) {
        return isLocationUsed(new BlockPos(x, y, z));
    }

    private int locationIndex(BlockPos worldCoord) {
        BlockPos coord = worldCoord.subtract(pos).add(blueprint.anchor);
        return (coord.getZ() * blueprint.size.getY() + coord.getY()) * blueprint.size.getX() + coord.getZ();
    }

    protected boolean isLocationUsed(BlockPos toTest) {
        return usedLocations.get(locationIndex(toTest));
    }

    protected void markLocationUsed(BlockPos toMark) {
        usedLocations.set(locationIndex(toMark), true);
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

    public boolean buildNextSlot(World world, TileAbstractBuilder builder) {
        return buildNextSlot(world, builder, Utils.convert(builder.getPos()).add(Utils.VEC_HALF));
    }

    public boolean buildNextSlot(World world, TileAbstractBuilder builder, Vec3d origin) {
        initialize();

        if (world.getTotalWorldTime() < nextBuildDate) {
            return false;
        }

        BuildingSlot slot = getNextBlock(world, builder);

        if (buildSlot(world, builder, slot, origin)) {
            nextBuildDate = world.getTotalWorldTime() + slot.buildTime();
            return true;
        } else {
            return false;
        }
    }

    public boolean buildSlot(World world, IBuildingItemsProvider builder, BuildingSlot slot, Vec3d from) {
        initialize();

        if (slot != null) {
            slot.built = true;
            BuildingItem i = new BuildingItem();
            i.origin = from;
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
    public BlockPos min() {
        return pos.subtract(blueprint.anchor);
    }

    @Override
    public BlockPos max() {
        return pos.add(blueprint.size).subtract(blueprint.anchor).subtract(Utils.POS_ONE);
    }

    @Override
    public void removeFromWorld() {}

    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(min(), max());
    }

    public void postProcessing(World world) {}

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
        int hardness = (int) Math.ceil((double) getBlockBreakEnergy(slot) / BuilderAPI.BREAK_ENERGY);

        for (int i = 0; i < hardness; ++i) {
            slot.addStackConsumed(new ItemStack(BuildCraftCore.decoratedBlock));
        }
    }

    public void useRequirements(IInventory inv, BuildingSlot slot) {

    }

    public void saveBuildStateToNBT(NBTTagCompound nbt, IBuildingItemsProvider builder) {
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
                markLocationUsed(o);
            }
        }

        if (nbt.hasKey("builtList")) {
            NBTTagList builtList = nbt.getTagList("builtList", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < builtList.tagCount(); ++i) {
                NBTBase cpt = builtList.get(i);
                BlockPos o = NBTUtils.readBlockPos(cpt);
                markLocationUsed(o);
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

    @Override
    public String toString() {
        return "BptBuilderBase [blueprint=" + blueprint + ", context=" + context + ", usedLocations=" + usedLocations + ", done=" + done + ", pos="
            + pos + ", initialized=" + initialized + ", nextBuildDate=" + nextBuildDate + "]";
    }
}
