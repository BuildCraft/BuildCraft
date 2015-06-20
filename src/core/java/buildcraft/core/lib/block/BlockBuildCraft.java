/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.XorShift128Random;

public abstract class BlockBuildCraft extends BlockBuildCraftBase implements ITileEntityProvider {
    protected static boolean keepInventory = false;

    protected final XorShift128Random rand = new XorShift128Random();
    private final boolean hasPowerLed;

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
        super(material, bcCreativeTab, properties);
        hasPowerLed = propertyList.contains(LED_POWER);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        if (!hasPowerLed) {
            return state;
        }
        TileBuildCraft tile = (TileBuildCraft) access.getTileEntity(pos);
        if (tile == null) {
            return state;
        } else {
            return state.withProperty(LED_POWER, tile.ledPower);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        FMLCommonHandler.instance().bus().post(new BlockPlacedDownEvent((EntityPlayer) entity, pos, state));
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileBuildCraft) {
            ((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        BlockInteractionEvent event = new BlockInteractionEvent(player, state);
        FMLCommonHandler.instance().bus().post(event);
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
}
