/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.data.XorShift128Random;

/**
 * Deprecated as most stuff can be done better now
 */
@Deprecated
public abstract class BlockBuildCraft extends BlockBuildCraftBase implements ITileEntityProvider {
    protected static boolean keepInventory = false;

    protected final XorShift128Random rand = new XorShift128Random();
    private final boolean hasPowerLed, hasDoneLed;

    protected BlockBuildCraft(Material material) {
        this(material, BCCreativeTab.get("main"), new BuildCraftProperty<?>[0]);
    }

    protected BlockBuildCraft(Material material, BCCreativeTab creativeTab) {
        this(material, creativeTab, new BuildCraftProperty<?>[0]);
    }

    protected BlockBuildCraft(Material material, BuildCraftProperty<?>... properties) {
        this(material, BCCreativeTab.get("main"), properties);
    }

    protected BlockBuildCraft(Material material, BCCreativeTab bcCreativeTab, BuildCraftProperty<?>... properties) {
        this(material, bcCreativeTab, false, properties);
    }

    protected BlockBuildCraft(Material material, BCCreativeTab bcCreativeTab, boolean hasExtended, BuildCraftProperty<?>... properties) {
        super(material, bcCreativeTab, hasExtended, properties);
        hasPowerLed = propertyList.contains(LED_POWER);
        hasDoneLed = propertyList.contains(LED_DONE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        if (!hasPowerLed || !hasDoneLed) {
            return state;
        }
        TileEntity tile = access.getTileEntity(pos);
        if (!(tile instanceof TileBuildCraft)) {
            return state;
        }
        TileBuildCraft tileBC = (TileBuildCraft) tile;
        if (hasPowerLed) {
            state = state.withProperty(LED_POWER, tileBC.ledPower);
        }
        if (hasDoneLed) {
            state = state.withProperty(LED_DONE, tileBC.ledDone);
        }

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        MinecraftForge.EVENT_BUS.post(new BlockPlacedDownEvent((EntityPlayer) entity, pos, state));
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileBuildCraft) {
            ((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        BlockInteractionEvent event = new BlockInteractionEvent(player, state);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        Utils.preDestroyBlock(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IHasWork && ((IHasWork) tile).hasWork()) {
            return super.getLightValue(world, pos) + 8;
        } else {
            return super.getLightValue(world, pos);
        }
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return this instanceof IComparatorInventory;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IInventory) {
            int count = 0;
            int countNonEmpty = 0;
            float power = 0.0F;
            for (EnumFacing face : EnumFacing.values()) {
                for (IInvSlot slot : InventoryIterator.getIterable((IInventory) tile, face)) {
                    if (((IComparatorInventory) this).doesSlotCountComparator(tile, slot.getIndex(), slot.getStackInSlot())) {
                        count++;
                        if (slot.getStackInSlot() != null) {
                            countNonEmpty++;
                            power += (float) slot.getStackInSlot().stackSize / (float) Math.min(((IInventory) tile).getInventoryStackLimit(), slot
                                    .getStackInSlot().getMaxStackSize());
                        }
                    }
                }
            }

            power /= count;
            return MathHelper.floor_float(power * 14.0F) + (countNonEmpty > 0 ? 1 : 0);
        }

        return 0;
    }
}
