package buildcraft.transport.api.impl;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IItemPipe;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;

public class ItemPipe extends Item implements IItemPipe, IModelRegister {
    private final PipeDefinition_BC8 defintion;

    public ItemPipe(PipeDefinition_BC8 definition) {
        setUnlocalizedName("pipe_" + definition.modUniqueTag);
        this.defintion = definition;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSprite() {
        return defintion.getSprite(defintion.itemSpriteIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        if (stack.getItemDamage() >= 1) {
            int color = (stack.getItemDamage() - 1) & 15;
            list.add(ColorUtils.getFormattingTooltip(color) + EnumChatFormatting.ITALIC + BCStringUtils.localize("color." + ColorUtils.getName(
                    color)));
        }
        // Class<? extends Pipe> pipe = BlockGenericPipe.pipes.get(this);
        // List<String> toolTip = PipeToolTipManager.getToolTip(pipe, advanced);
        // list.addAll(toolTip);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return renderPass;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (int i = 0; i < 17; i++) {
            ModelHelper.registerItemModel(this, i, "_" + i);
        }
    }

    // Copied from ItemBlock (mostly)
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.snow_layer && ((Integer) iblockstate.getValue(BlockSnow.LAYERS)).intValue() < 1) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }

        Block thisblock = BuildCraftTransport.pipeBlock;

        if (stack.stackSize == 0) {
            return false;
        } else if (!playerIn.canPlayerEdit(pos, side, stack)) {
            return false;
        } else if (pos.getY() == 255 && thisblock.getMaterial().isSolid()) {
            return false;
        } else if (worldIn.canBlockBePlaced(thisblock, pos, false, side, (Entity) null, stack)) {
            int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = thisblock.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, i, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, iblockstate1)) {
                worldIn.playSoundEffect((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ()
                    + 0.5F), thisblock.stepSound.getPlaceSound(), (thisblock.stepSound.getVolume() + 1.0F) / 2.0F, thisblock.stepSound.getFrequency()
                        * 0.8F);
                --stack.stackSize;
            }

            return true;
        } else {
            return false;
        }
    }

    // Also from item block
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        if (!world.setBlockState(pos, newState, 3)) return false;
        Block thisblock = BuildCraftTransport.pipeBlock;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == thisblock) {
            thisblock.onBlockPlacedBy(world, pos, state, player, stack);
        }

        return true;
    }
}
