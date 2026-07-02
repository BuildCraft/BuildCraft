/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.item;

import java.util.List;
import java.util.Locale;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.builders.snapshot.Snapshot;
import ct.buildcraft.builders.snapshot.Snapshot.Header;
import ct.buildcraft.lib.misc.HashUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSnapshot extends Item {
	
	public final EnumSnapshotType snapshotType;
	public static final ItemSnapshot[] snapshots = new ItemSnapshot[2];
	
    public ItemSnapshot(Item.Properties prop, EnumSnapshotType snapshotType) {
    	super(prop);
    	this.snapshotType = snapshotType;
    	snapshots[snapshotType.ordinal()] = this;
        //setHasSubtypes(true);
    }

    public static ItemStack getClean(EnumSnapshotType snapshotType) {
    	int id = snapshotType.ordinal();
    	if(snapshots[id] == null) {
    		BCLog.logger.warn("ItemSnapshot : getClean called to early");//TODO
    		return ItemStack.EMPTY;
    	}
    	//CompoundTag nbt = new CompoundTag();
    	//nbt.putInt("type", EnumItemSnapshotType.get(snapshotType, false).ordinal());
        return new ItemStack(snapshots[id], 1);
    }

    public static ItemStack getUsed(EnumSnapshotType snapshotType, Header header) {
    	int id = snapshotType.ordinal();
    	if(snapshots[id] == null) {
    		BCLog.logger.warn("ItemSnapshot : getUsed called to early");
    		return ItemStack.EMPTY;
    	}
        CompoundTag nbt = new CompoundTag();
        nbt.put("header", header.serializeNBT());
        //nbt.putInt("type", EnumItemSnapshotType.get(snapshotType, true).ordinal());
		ItemStack stack = new ItemStack(snapshots[id], 1);
		stack.setTag(nbt);
        return stack;
    }

    public static Header getHeader(ItemStack stack) {
        if (stack.getItem() instanceof ItemSnapshot) {
            if (EnumItemSnapshotType.getFromStack(stack).used) {
                CompoundTag nbt = stack.getTag();
                if (nbt != null) {
                    if (nbt.contains("header", Tag.TAG_COMPOUND)) {
                        return new Header(nbt.getCompound("header"));
                    }
                }
            }
        }
        return null;
    }
    
    @Override
	public int getMaxStackSize(ItemStack stack) {
    	return EnumItemSnapshotType.getFromStack(stack).used ? 1 : 16;
	}

    
	@Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
		if(this.allowedIn(tab))
			subItems.add(getClean(snapshotType));
    }

 /*   @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(Int2ObjectMap<ModelResourceLocation> variants) {
        for (EnumItemSnapshotType type : EnumItemSnapshotType.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }*/
    
    

    @Override
	public String getDescriptionId(ItemStack stack) {
        EnumItemSnapshotType type = EnumItemSnapshotType.getFromStack(stack);
        if (type.snapshotType == EnumSnapshotType.BLUEPRINT) {
            return "item.buildcraftbuilders.blueprint";
        }
        return "item.buildcraftbuilders.template";
	}
    
    
    @OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        EnumItemSnapshotType type = EnumItemSnapshotType.getFromStack(stack);
        Snapshot.Header header = getHeader(stack);
        if (header == null) {
            tooltip.add(Component.translatable("item.blueprint.blank").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.buildcraftbuilders.snapshot.clean_hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(getDisplayName(header).withStyle(ChatFormatting.GRAY));

        Player owner = world == null ? null : header.getOwnerPlayer(world);
        if (owner != null) {
            tooltip.add(Component.translatable("item.blueprint.author", owner.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.translatable(
            type.snapshotType == EnumSnapshotType.BLUEPRINT
                ? "item.buildcraftbuilders.blueprint.used_hint"
                : "item.buildcraftbuilders.template.used_hint"
        ).withStyle(ChatFormatting.DARK_GRAY));

        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable(
                "item.buildcraftbuilders.snapshot.hash",
                HashUtil.convertHashToString(header.key.hash)
            ).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable(
                "item.buildcraftbuilders.snapshot.created",
                header.created.toInstant().toString()
            ).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable(
                "item.buildcraftbuilders.snapshot.owner_uuid",
                header.owner.toString()
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
	}

    private static MutableComponent getDisplayName(Snapshot.Header header) {
        if (header.name == null || header.name.isBlank() || "<unnamed>".equals(header.name)) {
            return Component.translatable("item.blueprint.unnamed");
        }
        return Component.literal(header.name);
    }

    public enum EnumItemSnapshotType implements StringRepresentable {
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
  /*      	int meta = 0;
        	CompoundTag tag = stack.getTagElement("type");
 //       	BCLog.logger.debug(Boolean.toString(tag == null));
        	if(tag != null&&tag.contains("type")) {
        		meta = tag.getInt("type");
        	}
            return values()[Math.abs(meta) % values().length];*/
        	Item item = stack.getItem();
        	if(item instanceof ItemSnapshot snapshot) {
        		return get(snapshot.snapshotType, stack.getTagElement("header") != null);
        	}
        	BCLog.logger.warn("ItemSnapshot.EnumItemSnapshotType : Not a snapshot ItemStack!");
        	return BLUEPRINT_CLEAN;
        }
    }
}
