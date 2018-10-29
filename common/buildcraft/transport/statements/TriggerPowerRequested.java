package buildcraft.transport.statements;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.tile.TilePipeHolder;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {

    public TriggerPowerRequested() {
        super("buildcraft:powerRequested");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        final TilePipeHolder tile = (TilePipeHolder) source.getTile();

        IMjReceiver recv = null;
        // this doesn't like null facing so we have to cycle all facings
        for (EnumFacing f : EnumFacing.VALUES) {
            recv = tile.getCapability(MjAPI.CAP_RECEIVER, f);
            if (recv != null) break;
        }

        if (recv != null) {
            return recv.canReceive() && recv.getPowerRequested() >= MjAPI.MJ;
        }

        return false;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.pipe.requestsEnergy");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCTransportSprites.POWER_REQUESTED;
    }

}
