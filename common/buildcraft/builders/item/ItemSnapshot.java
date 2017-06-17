/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import java.util.List;
import java.util.Locale;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;

public class ItemSnapshot extends ItemBC_Neptune {
    public ItemSnapshot(String id) {
        super(id);
        setHasSubtypes(true);
    }

    public ItemStack getClean(EnumSnapshotType snapshotType) {
        return new ItemStack(this, 1, EnumItemSnapshotType.get(snapshotType, false).ordinal());
    }

    public ItemStack getUsed(EnumSnapshotType snapshotType, Header header) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("header", header.serializeNBT());
        ItemStack stack = new ItemStack(this, 1, EnumItemSnapshotType.get(snapshotType, true).ordinal());
        stack.setTagCompound(nbt);
        return stack;
    }

    public Header getHeader(ItemStack stack) {
        if (stack.getItem() instanceof ItemSnapshot) {
            if (EnumItemSnapshotType.getFromStack(stack).used) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null) {
                    if (nbt.hasKey("header", Constants.NBT.TAG_COMPOUND)) {
                        return new Header(nbt.getCompoundTag("header"));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return EnumItemSnapshotType.getFromStack(stack).used ? 1 : 16;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        subItems.add(new ItemStack(this, 1, 2));// clean blueprint
        subItems.add(new ItemStack(this, 1));// clean template
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumItemSnapshotType type : EnumItemSnapshotType.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        EnumItemSnapshotType type = EnumItemSnapshotType.getFromStack(stack);
        if (type.snapshotType == EnumSnapshotType.BLUEPRINT) {
            return "item.blueprintItem";
        }
        return "item.templateItem";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        Snapshot.Header header = getHeader(stack);
        if (header == null) {
            tooltip.add(LocaleUtil.localize("item.blueprint.blank"));
        } else {
            tooltip.add(header.name);
            EntityPlayer author = header.getOwnerPlayer(world);
            if (author != null) {
                tooltip.add(LocaleUtil.localize("item.blueprint.author") + " " + author.getName());
            }
            if (flag.isAdvanced()) {
                tooltip.add("Hash: " + HashUtil.convertHashToString(header.hash));
                tooltip.add("Date: " + header.created);
                tooltip.add("AuthorId: " + header.owner);
            }
        }
    }

    public enum EnumItemSnapshotType implements IStringSerializable {
        TEMPLATE_CLEAN(EnumSnapshotType.TEMPLATE, false),
        TEMPLATE_USED(EnumSnapshotType.TEMPLATE, true),
        BLUEPRINT_CLEAN(EnumSnapshotType.BLUEPRINT, false),
        BLUEPRINT_USED(EnumSnapshotType.BLUEPRINT, true);

        public final EnumSnapshotType snapshotType;
        public final boolean used;

        EnumItemSnapshotType(EnumSnapshotType snapshotType, boolean used) {
            this.snapshotType = snapshotType;
            this.used = used;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static EnumItemSnapshotType get(EnumSnapshotType snapshotType, boolean used) {
            if (snapshotType == EnumSnapshotType.TEMPLATE) {
                return !used ? TEMPLATE_CLEAN : TEMPLATE_USED;
            } else if (snapshotType == EnumSnapshotType.BLUEPRINT) {
                return !used ? BLUEPRINT_CLEAN : BLUEPRINT_USED;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public static EnumItemSnapshotType getFromStack(ItemStack stack) {
            return values()[Math.abs(stack.getMetadata()) % values().length];
        }
    }
}
