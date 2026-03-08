/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.inventory.IItemHandlerFiltered;
import buildcraft.api.items.IList;
import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.ObjectUtilBC;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import java.util.Locale;

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {
    public TriggerType type;

    public TriggerInventoryLevel(TriggerType type) {
        super("buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ROOT),
                "buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ROOT),
                "buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ROOT)
        );
        this.type = type;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_INVENTORY_LEVEL.get(type);
    }

    @Override
    public Component getDescription() {
//        return String.format(LocaleUtil.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
        return new TranslatableComponent("gate.trigger.inventorylevel.below", (int) (type.level * 100));
    }

    // Calen
    @Override
    public String getDescriptionKey() {
//        return String.format(LocaleUtil.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
        return "gate.trigger.inventorylevel.below." + (int) (type.level * 100);
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container, IStatementParameter[] parameters) {
        IItemHandler itemHandler = tile.getCapability(CapUtil.CAP_ITEMS, side.getOpposite()).orElse(null);
        if (itemHandler == null) {
            return false;
        }
        IItemHandlerFiltered filters = ObjectUtilBC.castOrNull(itemHandler, IItemHandlerFiltered.class);
        StatementParameterItemStack param = getParam(0, parameters, new StatementParameterItemStack());
        ItemStack searchStack = param.getItemStack();

        int itemSpace = 0;
        int foundItems = 0;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty()) {
                if (searchStack.isEmpty()) {
                    itemSpace += itemHandler.getSlotLimit(slot);
                } else {
                    if (searchStack.getItem() instanceof IList) {
                        // Unfortunately lists are too generic to work properly
                        // without a simple filtered inventory.
                        ItemStack filter = filters == null ? ItemStack.EMPTY : filters.getFilter(slot);
                        if (StackUtil.matchesStackOrList(searchStack, filter)) {
                            itemSpace += Math.min(filter.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                        }
                    } else {
                        ItemStack stack = searchStack.copy();
                        int count = Math.min(itemHandler.getSlotLimit(slot), searchStack.getMaxStackSize());
                        stack.setCount(count);
                        ItemStack leftOver = itemHandler.insertItem(slot, stack, true);
                        if (leftOver.isEmpty()) {
                            itemSpace += count;
                        } else {
                            itemSpace += count - leftOver.getCount();
                        }
                    }
                }
            } else {
                if (searchStack.isEmpty() || StackUtil.matchesStackOrList(searchStack, stackInSlot)) {
                    itemSpace += Math.min(stackInSlot.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                    foundItems += stackInSlot.getCount();
                }
            }
        }

        if (itemSpace > 0) {
            float percentage = foundItems / (float) itemSpace;
            return percentage < type.level;
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_INVENTORY_ALL;
    }

    public enum TriggerType {
        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        TriggerType(float level) {
            this.level = level;
        }

        public static final TriggerType[] VALUES = values();

        public final float level;
    }
}
