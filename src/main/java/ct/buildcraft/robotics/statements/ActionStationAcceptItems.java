package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.core.statements.StatementParameterItemStackExact;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionStationAcceptItems extends ActionStationInputItems {

    public ActionStationAcceptItems() {
        super("buildcraft:station.accept_items");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.station.acceptItems");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        // Marks the station as willing to accept items - used by robot AI to decide where to unload
        // The actual logic is in the robot board checking active actions
    }

    /** Returns true if the station accepts the given item stack (checked by robot AI). */
    public static boolean accepts(IStatementParameter[] parameters, ItemStack stack) {
        if (parameters == null) return false;
        boolean hasFilter = false;
        for (IStatementParameter p : parameters) {
            if (p instanceof StatementParameterItemStackExact e) {
                ItemStack filter = e.getItemStack();
                if (!filter.isEmpty()) {
                    hasFilter = true;
                    if (ItemStack.isSameItemSameTags(filter, stack)) return true;
                }
            }
        }
        return !hasFilter;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_ACCEPT_ITEMS;
    }
}
