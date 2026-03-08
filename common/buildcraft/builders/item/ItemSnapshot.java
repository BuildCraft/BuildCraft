/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.item;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.HashUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Locale;

public class ItemSnapshot extends ItemBC_Neptune {
    // public final EnumItemSnapshotType TYPE;
    public final EnumSnapshotType TYPE;

    // Calen
    public static final String TAG_KEY = "header";

    // public ItemSnapshot(String idBC, Item.Properties properties, EnumItemSnapshotType type)
    public ItemSnapshot(String idBC, Item.Properties properties, EnumSnapshotType type) {
        super(idBC, properties);
//        setHasSubtypes(true);
        this.TYPE = type;
    }

    public ItemStack getClean(EnumSnapshotType snapshotType) {
////        return new ItemStack(this, 1, EnumItemSnapshotType.get(snapshotType, false).ordinal());
//        ItemStack stack = new ItemStack(this, 1);
//        CompoundNBT nbt = new CompoundNBT();
//        nbt.putInt("meta", EnumItemSnapshotType.get(snapshotType, false).ordinal());
//        stack.setTag(nbt);
        // Calen
        ItemStack stack = null;
        switch (snapshotType) {
            case BLUEPRINT:
//                stack = new ItemStack(BCBuildersItems.snapshotBLUEPRINT_CLEAN.get());
                stack = new ItemStack(BCBuildersItems.snapshotBLUEPRINT.get());
                break;
            case TEMPLATE:
//                stack = new ItemStack(BCBuildersItems.snapshotTEMPLATE_CLEAN.get());
                stack = new ItemStack(BCBuildersItems.snapshotTEMPLATE.get());
                break;
        }
        return stack;
    }

    public ItemStack getUsed(EnumSnapshotType snapshotType, Header header) {
        CompoundNBT nbt = new CompoundNBT();
//        nbt.put("header", header.serializeNBT());
        nbt.put(TAG_KEY, header.serializeNBT());
////        ItemStack stack = new ItemStack(this, 1, EnumItemSnapshotType.get(snapshotType, true).ordinal());
//        ItemStack stack = new ItemStack(this, 1);
//        nbt.putInt("meta", EnumItemSnapshotType.get(snapshotType, true).ordinal());
//        stack.setTag(nbt);
        // Calen
        ItemStack stack = null;
        switch (snapshotType) {
            case BLUEPRINT:
//                stack = new ItemStack(BCBuildersItems.snapshotBLUEPRINT_USED.get());
                stack = new ItemStack(BCBuildersItems.snapshotBLUEPRINT.get());
                break;
            case TEMPLATE:
//                stack = new ItemStack(BCBuildersItems.snapshotTEMPLATE_USED.get());
                stack = new ItemStack(BCBuildersItems.snapshotTEMPLATE.get());
                break;
        }
        stack.setTag(nbt);
        return stack;
    }

    public Header getHeader(ItemStack stack) {
        if (stack.getItem() instanceof ItemSnapshot) {
            if (EnumItemSnapshotType.getFromStack(stack).used) {
                CompoundNBT nbt = stack.getTag();
                if (nbt != null) {
//                    if (nbt.contains("header", Tag.TAG_COMPOUND))
                    if (nbt.contains(TAG_KEY, Constants.NBT.TAG_COMPOUND)) {
//                        return new Header(nbt.getCompound("header"));
                        return new Header(nbt.getCompound(TAG_KEY));
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

    // 1.18.2: different item obj
//    @Override
//    protected void addSubItems(ItemGroup tab, NonNullList<ItemStack> subItems) {
//        subItems.add(getClean(EnumSnapshotType.BLUEPRINT));
//        subItems.add(getClean(EnumSnapshotType.TEMPLATE));
//    }

    // Calen: not still useful in 1.18.2
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants)
//    {
////        for (EnumItemSnapshotType type : EnumItemSnapshotType.values())
////        {
//////            addVariant(variants, type.ordinal(), type.getName());
////            addVariant(variants, type.ordinal(), type.getSerializedName());
////        }
//    }

    @Override
//    public String getUnlocalizedName(ItemStack stack)
    public String getDescriptionId(ItemStack stack) {
//        EnumItemSnapshotType type = EnumItemSnapshotType.getFromStack(stack);
//        if (type.snapshotType == EnumSnapshotType.BLUEPRINT) {
//            return "item.blueprintItem";
//        }
//        return "item.templateItem";
        switch (this.TYPE) {
            case BLUEPRINT:
                return "item.blueprintItem.name";
            case TEMPLATE:
                return "item.templateItem.name";
        }
        throw new RuntimeException("[builders.snapshot] Unexpected snapshot type: [" + this.TYPE + "]");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        Header header = getHeader(stack);
        if (header == null) {
            tooltip.add(new TranslationTextComponent("item.blueprint.blank"));
        } else {
            tooltip.add(new StringTextComponent(header.name));
            PlayerEntity owner = header.getOwnerPlayer(world);
            if (owner != null) {
                tooltip.add(new TranslationTextComponent("item.blueprint.author").append(" ").append(owner.getName()));
            }
            if (flag.isAdvanced()) {
                tooltip.add(new StringTextComponent("Hash: " + HashUtil.convertHashToString(header.key.hash)));
                tooltip.add(new StringTextComponent("Date: " + header.created));
                tooltip.add(new StringTextComponent("Owner UUID: " + header.owner));
            }
        }
    }

    // public enum EnumItemSnapshotType implements IStringSerializable
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
//        public String getName()
        public String getSerializedName() {
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
//            return values()[Math.abs(stack.getMetadata()) % values().length];

            if (stack.getItem() instanceof ItemSnapshot) {
                ItemSnapshot snapshot = (ItemSnapshot) stack.getItem();
                boolean hasHeaderTag = stack.hasTag() && stack.getTag().contains(TAG_KEY);
                switch (snapshot.TYPE) {
                    case TEMPLATE:
                        return hasHeaderTag ? TEMPLATE_USED : TEMPLATE_CLEAN;
                    case BLUEPRINT:
                        return hasHeaderTag ? BLUEPRINT_USED : BLUEPRINT_CLEAN;
                }
            }
            return null;
        }
    }
}
