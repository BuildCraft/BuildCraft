package buildcraft.transport.api.impl;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeToolTipManager;

public class ItemPipe extends ItemBlock implements IItemPipe, IModelRegister {
    @SideOnly(Side.CLIENT)
    private IIconProvider iconProvider;
    private int pipeIconIndex;

    public ItemPipe(BCCreativeTab creativeTab) {
        super(BuildCraftTransport.pipeBlock);
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
}
