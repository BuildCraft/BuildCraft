/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.HashMap;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.TooltipContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import net.minecraft.nbt.NbtElement;

public class ItemSnapshot extends ItemBC_Neptune {
    public ItemSnapshot(String id) {
        super(id);
        setHasSubtypes(true);
    }

    public ItemStack getClean(EnumSnapshotType snapshotType) {
        return new ItemStack(this, 1);
    }

    public ItemStack getUsed(EnumSnapshotType snapshotType, Header header) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("header", header.createNbt());
        ItemStack stack = new ItemStack(this, 1);
        stack.setNbt(nbt);
        return stack;
    }

    public Header getHeader(ItemStack stack) {
        if (stack.getItem() instanceof ItemSnapshot) {
            if (EnumItemSnapshotType.getFromStack(stack).used) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null) {
                    if (nbt.contains("header", NbtElement.COMPOUND_TYPE)) {
                        return new Header(nbt.getCompound("header"));
                    }
                }
            }
        }
        return null;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getItemStackLimit(ItemStack stack) {
        return EnumItemSnapshotType.getFromStack(stack).used ? 1 : 16;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void addSubItems(ItemGroup tab, DefaultedList<ItemStack> subItems) {
        subItems.add(getClean(EnumSnapshotType.BLUEPRINT));
        subItems.add(getClean(EnumSnapshotType.TEMPLATE));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    @Environment(EnvType.CLIENT)
    public void addModelVariants(HashMap<Integer, ModelIdentifier> variants) {
        for (EnumItemSnapshotType type : EnumItemSnapshotType.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getUnlocalizedName(ItemStack stack) {
        EnumItemSnapshotType type = EnumItemSnapshotType.getFromStack(stack);
        if (type.snapshotType == EnumSnapshotType.BLUEPRINT) {
            return "item.blueprintItem";
        }
        return "item.templateItem";
    }

    @Environment(EnvType.CLIENT)
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void addInformation(ItemStack stack, World world, List<String> tooltip, TooltipContext flag) {
        Snapshot.Header header = getHeader(stack);
        if (header == null) {
            tooltip.add(LocaleUtil.localize("item.blueprint.blank"));
        } else {
            tooltip.add(header.name);
            PlayerEntity owner = header.getOwnerPlayer(world);
            if (owner != null) {
                tooltip.add(LocaleUtil.localize("item.blueprint.author") + " " + owner.getName());
            }
            if (flag.isAdvanced()) {
                tooltip.add("Hash: " + HashUtil.convertHashToString(header.key.hash));
                tooltip.add("Date: " + header.created);
                tooltip.add("Owner UUID: " + header.owner);
            }
        }
    }

    public enum EnumItemSnapshotType implements StringIdentifiable {
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

        // @Override -- removed: method does not exist in Fabric 1.20.1
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
            return values()[Math.abs(stack.getDamage()) % values().length];
        }
    }

    @Override
    public String asString() { return getName(); }
}
