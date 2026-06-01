/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by api.blocks.CustomPaintHelper, lib.misc.SoundUtil, lib.misc.ParticleUtil (not yet migrated)
package buildcraft.core.item;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.blocks.CustomPaintHelper;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.ParticleUtil;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;

public class ItemPaintbrush_BC8 extends ItemBC_Neptune {
    private static final String DAMAGE = "damage";
    private static final int MAX_USES = 64;

    public ItemPaintbrush_BC8(String id) {
        super(id);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void addSubItems(ItemGroup tab, DefaultedList<ItemStack> subItems) {
        for (int i = 0; i < 17; i++) {
            subItems.add(new ItemStack(this, 1));
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public void addModelVariants(HashMap<Integer, ModelIdentifier> variants) {
        addVariant(variants, 0, "clean");
        for (DyeColor colour : DyeColor.values()) {
            addVariant(variants, colour.getId() + 1, colour.getName());
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public ActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = StackUtil.asNonNull(player.getStackInHand(hand));
        Brush brush = new Brush(stack);
        Vec3d hitPos = VecUtil.add(new Vec3d(hitX, hitY, hitZ), pos);
        if (brush.useOnBlock(world, pos, world.getBlockState(pos), hitPos, facing, player)) {
            ItemStack newStack = brush.save(stack);
            if (!newStack.isEmpty()) {
                player.setHeldItem(hand, newStack);
            }
            // We just changed the damage NBT value
            player.inventoryContainer.detectAndSendChanges();
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    public Brush getBrushFromStack(ItemStack stack) {
        return new Brush(stack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getItemStackDisplayName(ItemStack stack) {
        Brush brush = getBrushFromStack(stack);
        String colourComponent = "";
        if (brush.colour != null) {
            colourComponent = ColourUtil.getTextFullTooltipSpecial(brush.colour) + " ";
        }
        return colourComponent + super.getItemStackDisplayName(stack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public TextRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getDamage(ItemStack stack) {
        Brush brush = new Brush(stack);
        return MAX_USES - brush.usesLeft;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void setDamage(ItemStack stack, int damage) {
        // Explicitly disallow this- some core use cases mistake this for metadata and fail
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isDamaged(ItemStack stack) {
        Brush brush = new Brush(stack);
        return brush.colour != null && brush.usesLeft < MAX_USES;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean showDurabilityBar(ItemStack stack) {
        return isDamaged(stack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public double getDurabilityForDisplay(ItemStack stack) {
        Brush brush = new Brush(stack);
        return 1 - (brush.usesLeft / (double) MAX_USES);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getMetadata(ItemStack stack) {
        return super.getDamage(stack);
    }

    /** Delegate class for handling */
    public class Brush {
        public DyeColor colour;
        public int usesLeft;

        public Brush(DyeColor colour) {
            this.colour = colour;
            usesLeft = MAX_USES;
        }

        public Brush(ItemStack stack) {
            int meta = stack.getId();
            if (meta > 0 && meta <= 16) {
                colour = DyeColor.byId(meta - 1);
                NbtCompound nbt = stack.getNbt();
                if (nbt == null) {
                    usesLeft = MAX_USES;
                } else {
                    usesLeft = MAX_USES - nbt.getByte(DAMAGE);
                }
            } else {
                usesLeft = 0;
            }
        }

        @Nonnull
        public ItemStack save() {
            return save(StackUtil.EMPTY);
        }

        @Nonnull
        public ItemStack save(@Nonnull ItemStack existing) {
            ItemStack stack = existing;
            if (existing.isEmpty() || existing.getId() != getMeta()) {
                stack = new ItemStack(ItemPaintbrush_BC8.this, 1);
            }
            if (usesLeft != MAX_USES && colour != null) {
                NbtCompound nbt = stack.getNbt();
                if (nbt == null) {
                    nbt = new NbtCompound();
                    stack.setTagCompound(nbt);
                }
                nbt.putByte(DAMAGE, (byte) (MAX_USES - usesLeft));
            }
            return stack == existing ? StackUtil.EMPTY : stack;
        }

        public int getMeta() {
            return (usesLeft <= 0 || colour == null) ? 0 : colour.getId() + 1;
        }

        public boolean useOnBlock(World world, BlockPos pos, BlockState state, Vec3d hitPos, Direction side, PlayerEntity player) {
            if (colour != null && usesLeft <= 0) {
                return false;
            }

            ActionResult result = CustomPaintHelper.INSTANCE.attemptPaintBlock(world, pos, state, hitPos, side, colour);

            if (result == ActionResult.SUCCESS) {
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
