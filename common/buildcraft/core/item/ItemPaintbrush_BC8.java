/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import javax.annotation.Nullable;

import buildcraft.lib.item.ItemStackHelper;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blocks.CustomPaintHelper;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.ParticleUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.VecUtil;

import java.util.List;

public class ItemPaintbrush_BC8 extends ItemBC_Neptune {
    private static final String DAMAGE = "damage";
    private static final int MAX_USES = 64;

    public ItemPaintbrush_BC8(String id) {
        super(id);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    @Override
    protected void addSubItems(CreativeTabs tab, List<ItemStack> subItems) {
        for (int i = 0; i < 17; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "clean");
        for (EnumDyeColor colour : EnumDyeColor.values()) {
            addVariant(variants, colour.getMetadata() + 1, colour.getName());
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        Brush brush = new Brush(stack);
        Vec3d hitPos = VecUtil.add(new Vec3d(hitX, hitY, hitZ), pos);
        if (brush.useOnBlock(world, pos, world.getBlockState(pos), hitPos, facing, player)) {
            ItemStack newStack = brush.save(stack);
            if (!ItemStackHelper.isEmpty(newStack)) {
                player.setHeldItem(hand, newStack);
            }
            // We just changed the damage NBT value
            player.inventoryContainer.detectAndSendChanges();
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public Brush getBrushFromStack(ItemStack stack) {
        return new Brush(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Brush brush = getBrushFromStack(stack);
        String colourComponent = "";
        if (brush.colour != null) {
            colourComponent = ColourUtil.getTextFullTooltipSpecial(brush.colour) + " ";
        }
        return colourComponent + super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }

    @Override
    public int getDamage(ItemStack stack) {
        Brush brush = new Brush(stack);
        return MAX_USES - brush.usesLeft;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        // Explicitly disallow this- some core use cases mistake this for metadata and fail
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        Brush brush = new Brush(stack);
        return brush.colour != null && brush.usesLeft < MAX_USES;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return isDamaged(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        Brush brush = new Brush(stack);
        return 1 - (brush.usesLeft / (double) MAX_USES);
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return super.getDamage(stack);
    }

    /** Delegate class for handling */
    public class Brush {
        public EnumDyeColor colour;
        public int usesLeft;

        public Brush(EnumDyeColor colour) {
            this.colour = colour;
            usesLeft = MAX_USES;
        }

        public Brush(ItemStack stack) {
            int meta = stack.getMetadata();
            if (meta > 0 && meta <= 16) {
                colour = EnumDyeColor.byMetadata(meta - 1);
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    usesLeft = MAX_USES;
                } else {
                    usesLeft = MAX_USES - nbt.getByte(DAMAGE);
                }
            } else {
                usesLeft = 0;
            }
        }

        @Nullable
        public ItemStack save() {
            return save(null);
        }

        @Nullable
        public ItemStack save(ItemStack existing) {
            ItemStack stack = existing;
            if (ItemStackHelper.isEmpty(existing) || existing.getMetadata() != getMeta()) {
                stack = new ItemStack(ItemPaintbrush_BC8.this, 1, getMeta());
            }
            if (usesLeft != MAX_USES && colour != null) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                    stack.setTagCompound(nbt);
                }
                nbt.setByte(DAMAGE, (byte) (MAX_USES - usesLeft));
            }
            return stack == existing ? null : stack;
        }

        public int getMeta() {
            return (usesLeft <= 0 || colour == null) ? 0 : colour.getMetadata() + 1;
        }

        public boolean useOnBlock(World world, BlockPos pos, IBlockState state, Vec3d hitPos, EnumFacing side, EntityPlayer player) {
            if (colour != null && usesLeft <= 0) {
                return false;
            }

            EnumActionResult result = CustomPaintHelper.INSTANCE.attemptPaintBlock(world, pos, state, hitPos, side, colour);

            if (result == EnumActionResult.SUCCESS) {
                ParticleUtil.showChangeColour(world, hitPos, colour);
                SoundUtil.playChangeColour(world, pos, colour);

                if (!player.isCreative()) {
                    usesLeft--;
                }

                if (usesLeft <= 0) {
                    colour = null;
                    usesLeft = 0;
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "[" + usesLeft + " of " + (colour == null ? "nothing" : colour.getName()) + "]";
        }
    }
}
