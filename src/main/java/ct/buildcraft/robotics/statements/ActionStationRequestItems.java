package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.core.statements.StatementParameterItemStackExact;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionStationRequestItems extends ActionStationInputItems {

    public ActionStationRequestItems() {
        super("buildcraft:station.request_items");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.station.requestItems");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {}

    public static boolean requests(IStatementParameter[] parameters, ItemStack stack) {
        if (parameters == null) return false;
        for (IStatementParameter p : parameters) {
            if (p instanceof StatementParameterItemStackExact e) {
                ItemStack filter = e.getItemStack();
                if (!filter.isEmpty() && ItemStack.isSameItemSameTags(filter, stack)) return true;
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_REQUEST_ITEMS;
    }
}
