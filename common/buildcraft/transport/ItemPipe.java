/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.List;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.BuildCraftTransport;

public class ItemPipe extends ItemBuildCraft implements IItemPipe {

    @SideOnly(Side.CLIENT)
    private IIconProvider iconProvider;
    private int pipeIconIndex;

    public ItemPipe(BCCreativeTab creativeTab) {
        super(creativeTab);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, BlockPos pos, EnumFacing side, float par8, float par9,
            float par10) {
        Block block = BuildCraftTransport.genericPipeBlock;

        Block worldBlock = world.getBlockState(pos).getBlock();

        if (worldBlock == Blocks.snow) {
            side = EnumFacing.UP;
        } else if (worldBlock != Blocks.vine && worldBlock != Blocks.tallgrass && worldBlock != Blocks.deadbush && (worldBlock == null || !worldBlock
                .isReplaceable(world, pos))) {
            pos = pos.offset(side);
        }

        if (itemstack.stackSize == 0) {
            return false;
        }

        if (world.canBlockBePlaced(block, pos, false, side, entityplayer, itemstack)) {
            Pipe<?> pipe = BlockGenericPipe.createPipe(this);

            if (pipe == null) {
                BCLog.logger.log(Level.WARN, "Pipe failed to create during placement at {0}", pos);
                return true;
            }

            if (BlockGenericPipe.placePipe(pipe, world, pos, block.getDefaultState(), entityplayer)) {
                block.onBlockPlacedBy(world, pos, block.getDefaultState(), entityplayer, itemstack);

                if (!world.isRemote) {
                    TileEntity tile = world.getTileEntity(pos);
                    ((TileGenericPipe) tile).initializeFromItemMetadata(itemstack.getItemDamage());
                }

                world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, block.stepSound.getPlaceSound(), (block.stepSound
                        .getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);

                itemstack.stackSize--;
            }

            return true;
        } else {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public void setPipesIcons(IIconProvider iconProvider) {
        this.iconProvider = iconProvider;
    }

    public void setPipeIconIndex(int index) {
        this.pipeIconIndex = index;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSprite() {
        if (iconProvider != null) { // invalid pipes won't have this set
            return iconProvider.getIcon(pipeIconIndex);
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        if (stack.getItemDamage() >= 1) {
            int color = (stack.getItemDamage() - 1) & 15;
            list.add(ColorUtils.getFormattingTooltip(color) + EnumChatFormatting.ITALIC + StringUtils.localize("color." + ColorUtils.getName(color)));
        }
        Class<? extends Pipe> pipe = BlockGenericPipe.pipes.get(this);
        List<String> toolTip = PipeToolTipManager.getToolTip(pipe, advanced);
        list.addAll(toolTip);
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerModels() {
//        ModelHelper.registerItemModel(this, 0, "");
//    }
}
