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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {
    public TriggerType type;

    public TriggerInventoryLevel(TriggerType type) {
        super(
            "buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ROOT),
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
    public int minParameters() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.TRIGGER_INVENTORY_LEVEL.get(type);
    }

    @Override
    public String getDescription() {
        return String.format(LocaleUtil.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
        if (parameters == null || parameters.length < 1 || parameters[0] == null) {
            return false;
        }

        if (tile.hasCapability(CapUtil.CAP_ITEMS, side.getOpposite())) {
            IItemHandler itemHandler = tile.getCapability(CapUtil.CAP_ITEMS, side.getOpposite());
            if (itemHandler == null) {
                return false;
            }
            ItemStack searchStack = parameters[0].getItemStack();

            if (searchStack.isEmpty()) {
                return false;
            }

            int stackSpace = 0;
            int foundItems = 0;
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
                if (stackInSlot.isEmpty() || StackUtil.canStacksOrListsMerge(stackInSlot, searchStack)) {
                    stackSpace++;
                    foundItems += stackInSlot.isEmpty() ? 0 : stackInSlot.getCount();
                }
            }

            if (stackSpace > 0) {
                float percentage = foundItems / ((float) stackSpace * (float) searchStack.getMaxStackSize());
                return percentage < type.level;
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
