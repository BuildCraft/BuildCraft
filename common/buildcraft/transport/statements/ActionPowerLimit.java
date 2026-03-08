package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ActionPowerLimit extends BCStatement implements IActionInternal {

    public final PipeDefinition pipe;

    /** Behaves identically to {@link PipeBehaviourLimiter} */
    public final int limitShift;

    public ActionPowerLimit(PipeDefinition pipe, int limitShift, String... uniqueTags) {
        super(uniqueTags);
        this.pipe = pipe;
        this.limitShift = limitShift;
    }

    public ActionPowerLimit(String suffix, PipeDefinition pipe, int limitShift) {
        this(pipe, limitShift, "buildcraft:pipe.power_limit." + suffix + "_s" + limitShift);
    }

    protected boolean isRf() {
        return false;
    }

    @Override
    public ITextComponent getDescription() {
        if (isRf()) {
            RedstoneFluxTransferInfo pipeInfo = PipeApi.rfTransferData.get(pipe);
            final Object max;
            if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
                max = 0;
            } else if (pipeInfo == null) {
                max = "??[INVALID_PIPE]??";
            } else {
                max = pipeInfo.transferPerTick >> limitShift;
            }
            return new TranslationTextComponent("gate.action.pipe.rf_limit", max);
        }
        PowerTransferInfo pipeInfo = PipeApi.powerTransferData.get(pipe);
        final Object max;
        if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
            max = 0;
        } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
        } else {
            max = (int) ((pipeInfo.transferPerTick >> limitShift) / MjAPI.MJ);
        }
        return new TranslationTextComponent("gate.action.pipe.power_limit", max);
    }

    @Override
    public String getDescriptionKey() {
        if (isRf()) {
            RedstoneFluxTransferInfo pipeInfo = PipeApi.rfTransferData.get(pipe);
            final Object max;
            if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
                max = 0;
            } else if (pipeInfo == null) {
                max = "??[INVALID_PIPE]??";
            } else {
                max = pipeInfo.transferPerTick >> limitShift;
            }
            return LocaleUtil.localize("gate.action.pipe.rf_limit", max);
        }
        PowerTransferInfo pipeInfo = PipeApi.powerTransferData.get(pipe);
        final Object max;
        if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
            max = 0;
        } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
        } else {
            max = (int) ((pipeInfo.transferPerTick >> limitShift) / MjAPI.MJ);
        }
        return LocaleUtil.localize("gate.action.pipe.power_limit", max);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        SpriteHolder[] sprites;
        if (BCTransportConfig.powerPipeUseOldMjTexture || !isRf()) {
            sprites = BCTransportSprites.POWER_LIMIT;
        } else {
            sprites = BCTransportSprites.POWER_LIMIT_RF;
        }
        return sprites[limitShift];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // The behaviour handles this
    }

    @Override
    public abstract IStatement[] getPossible();

    public static class ActionIronPowerLimit extends ActionPowerLimit {

        public ActionIronPowerLimit(int limitShift) {
            super("iron", BCTransportPipes.ironPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_POWER_LIMIT;
        }
    }

    public static class ActionDiamondPowerLimit extends ActionPowerLimit {

        public ActionDiamondPowerLimit(int limitShift) {
            super("diamond", BCTransportPipes.diamondPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT;
        }
    }

    public static class ActionIronRfLimit extends ActionPowerLimit {

        public ActionIronRfLimit(int limitShift) {
            super("iron_rf", BCTransportPipes.ironRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }

    public static class ActionDiamondRfLimit extends ActionPowerLimit {

        public ActionDiamondRfLimit(int limitShift) {
            super("diamond_rf", BCTransportPipes.diamondRf, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_RF_LIMIT;
        }

        @Override
        protected boolean isRf() {
            return true;
        }
    }
}
