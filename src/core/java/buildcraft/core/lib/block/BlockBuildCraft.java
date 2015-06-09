/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.XorShift128Random;

public abstract class BlockBuildCraft extends BlockBuildCraftBase implements ITileEntityProvider {
    protected static boolean keepInventory = false;

    protected final XorShift128Random rand = new XorShift128Random();
    protected int renderPass;

    protected int maxPasses = 1;

    private boolean alphaPass = false;

    protected BlockBuildCraft(Material material) {
        this(material, BCCreativeTab.get("main"), new IProperty[0], new IUnlistedProperty<?>[0]);
    }

    protected BlockBuildCraft(Material material, BCCreativeTab creativeTab) {
        this(material, creativeTab, new IProperty[0], new IUnlistedProperty<?>[0]);
    }

    protected BlockBuildCraft(Material material, IProperty... properties) {
        this(material, BCCreativeTab.get("main"), properties, new IUnlistedProperty<?>[0]);
    }

    protected BlockBuildCraft(Material material, IProperty[] properties, IUnlistedProperty<?>... nonMetaProperties) {
        this(material, BCCreativeTab.get("main"), properties, nonMetaProperties);
    }

    protected BlockBuildCraft(Material material, BCCreativeTab bcCreativeTab, IProperty[] properties, IUnlistedProperty<?>... nonMetaProperties) {
        super(material, bcCreativeTab, properties, nonMetaProperties);
    }

    public boolean hasAlphaPass() {
        return alphaPass;
    }

    public void setAlphaPass(boolean alphaPass) {
        this.alphaPass = alphaPass;
    }

    public void setPassCount(int maxPasses) {
        this.maxPasses = maxPasses;
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

    public boolean canRenderInPassBC(int pass) {
        if (pass >= maxPasses) {
            renderPass = 0;
            return false;
        } else {
            renderPass = pass;
            return true;
        }
    }

    public int getCurrentRenderPass() {
        return renderPass;
    }

    public int getIconGlowLevel() {
        return -1;
    }

    public int getIconGlowLevel(IBlockAccess access, BlockPos pos) {
        return getIconGlowLevel();
    }

    public int getFrontSide(int meta) {
        if (!isRotatable()) {
            return -1;
        }
        return meta >= 2 && meta <= 5 ? meta : 3;
    }
}
