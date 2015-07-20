package buildcraft.transport.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.statements.BCStatement;

/** Created by asie on 3/14/15. */
public class TriggerLightSensor extends BCStatement implements ITriggerInternal {
    private final boolean bright;

    public TriggerLightSensor(boolean bright) {
        super("buildcraft:light_" + (bright ? "bright" : "dark"));
        location = new ResourceLocation("buildcrafttransport:triggers/trigger_light_" + (bright ? "bright" : "dark"));
        this.bright = bright;
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("gate.trigger.light." + (bright ? "bright" : "dark"));
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        TileEntity tile = source.getTile();
        Vec3 pos = Utils.convert(tile.getPos()).add(Utils.convert(((ISidedStatementContainer) source).getSide()));

        // TODO (PASS 1): Is this is right method (#getLight)
        int lightLevel = tile.getWorld().getLight(Utils.convertFloor(pos));

        return (lightLevel < 8) ^ bright;
    }
}
