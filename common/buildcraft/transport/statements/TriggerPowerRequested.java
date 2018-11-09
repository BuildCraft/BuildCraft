package buildcraft.transport.statements;

import net.minecraft.util.EnumFacing;

import java.util.Objects;
import java.util.stream.Stream;

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

        long requested = Stream.of(EnumFacing.VALUES)
            .map(f -> tile.getCapability(MjAPI.CAP_RECEIVER, f))
            .filter(Objects::nonNull)
            .filter(IMjReceiver::canReceive)
            .mapToLong(IMjReceiver::getPowerRequested)
            .max().orElse(0);

        return requested >= MjAPI.MJ;
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
