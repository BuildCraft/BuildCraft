/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.statements.*;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.StringUtilBC;

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {

    public enum TriggerType {

        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        public static final TriggerType[] VALUES = values();

        public final float level;

        TriggerType(float level) {
            this.level = level;
        }
    }

    public TriggerType type;

    public TriggerInventoryLevel(TriggerType type) {
        super("buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ROOT),//
                "buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ROOT), //
                "buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ROOT));
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
        return String.format(StringUtilBC.localize("gate.trigger.inventorylevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
        // A parameter is required
        if (parameters == null || parameters.length < 1 || parameters[0] == null) {
            return false;
        }

        if (tile instanceof IInventory) {
            IInventory inventory = (IInventory) tile;
            ItemStack searchStack = parameters[0].getItemStack();

            if (searchStack == null) {
                return false;
            }

            int stackSpace = 0;
            int foundItems = 0;
            for (IInvSlot slot : InventoryIterator.getIterable(inventory, side.getOpposite())) {
                if (slot.canPutStackInSlot(searchStack)) {
                    ItemStack stackInSlot = slot.getStackInSlot();
                    if (stackInSlot == null || StackUtil.canStacksOrListsMerge(stackInSlot, searchStack)) {
                        stackSpace++;
                        foundItems += stackInSlot == null ? 0 : stackInSlot.stackSize;
                    }
                }
            }

            if (stackSpace > 0) {
                float percentage = foundItems / ((float) stackSpace * (float) Math.min(searchStack.getMaxStackSize(), inventory.getInventoryStackLimit()));
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
}
