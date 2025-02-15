/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.items.IList;
import buildcraft.api.statements.*;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.Locale;

public class TriggerInventory extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerInventory(State state) {
        super(
                "buildcraft:inventory." + state.name().toLowerCase(Locale.ROOT),
                "buildcraft.inventory." + state.name().toLowerCase(Locale.ROOT)
        );
        this.state = state;
    }

    @Override
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_INVENTORY.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public Component getDescription() {
//        return LocaleUtil.localize("gate.trigger.inventory." + state.name().toLowerCase(Locale.ROOT));
        return Component.translatable("gate.trigger.inventory." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.inventory." + state.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container, IStatementParameter[] parameters) {
        ItemStack searchedStack = StackUtil.EMPTY;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null) {
            searchedStack = parameters[0].getItemStack();
        }

        IItemHandler handler = tile.getCapability(CapUtil.CAP_ITEMS, side.getOpposite()).orElse(null);

        if (handler != null) {
            boolean hasSlots = false;
            boolean foundItems = false;
            boolean foundSpace = false;

            for (int i = 0; i < handler.getSlots(); i++) {
                hasSlots = true;
                ItemStack stack = handler.getStackInSlot(i);

                // TODO: Replace some of this with
                foundItems |= !stack.isEmpty() && (searchedStack.isEmpty() || StackUtil.canStacksOrListsMerge(stack, searchedStack));

                foundSpace |= (stack.isEmpty() || (StackUtil.canStacksOrListsMerge(stack, searchedStack) && stack.getCount() < stack.getMaxStackSize()))//
                        && (searchedStack.isEmpty() || searchedStack.getItem() instanceof IList || handler.insertItem(i, searchedStack, true).isEmpty());
                // On the test above, we deactivate item list as inventories
                // typically don't check for lists possibility. This is a
                // heuristic which is more desirable than expensive computation
                // of list components or possibility of extension
            }

            if (!hasSlots) {
                return false;
            }

            switch (state) {
                case EMPTY:
                    return !foundItems;
                case CONTAINS:
                    return foundItems;
                case SPACE:
                    return foundSpace;
                default:
                    return !foundSpace;
            }
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

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
