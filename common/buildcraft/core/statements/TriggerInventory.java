/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.IItemHandler;

import buildcraft.api.items.IList;
import buildcraft.api.statements.*;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

public class TriggerInventory extends BCStatement implements ITriggerExternal {

    public enum State {

        Empty,
        Contains,
        Space,
        Full;

        public static final State[] VALUES = values();
    }

    public State state;

    public TriggerInventory(State state) {
        super("buildcraft:inventory." + state.name().toLowerCase(Locale.ENGLISH), "buildcraft.inventory." + state.name().toLowerCase(Locale.ENGLISH));
        this.state = state;
    }

    @Override
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_INVENTORY.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.Contains || state == State.Space ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.inventory." + state.name().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
        ItemStack searchedStack = StackUtil.EMPTY;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null) {
            searchedStack = parameters[0].getItemStack();
        }

        IItemHandler handler = tile.getCapability(CapUtil.CAP_ITEMS, side.getOpposite());

        if (handler != null) {
            boolean hasSlots = false;
            boolean foundItems = false;
            boolean foundSpace = false;

            for (int i = 0; i < handler.getSlots(); i++) {
                hasSlots = true;
                ItemStack stack = handler.getStackInSlot(i);

                // TODO: Replace some of this with
                foundItems |= !stack.isEmpty() && (searchedStack.isEmpty() || StackUtil.canStacksOrListsMerge(stack, searchedStack));

                foundSpace |= (stack.isEmpty() | (StackUtil.canStacksOrListsMerge(stack, searchedStack) && stack.getCount() < stack.getMaxStackSize()))//
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
                case Empty:
                    return !foundItems;
                case Contains:
                    return foundItems;
                case Space:
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
}
