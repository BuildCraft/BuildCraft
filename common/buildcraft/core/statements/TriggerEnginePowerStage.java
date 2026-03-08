package buildcraft.core.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.engine.TileEngineBase_BC8;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public class TriggerEnginePowerStage extends BCStatement implements ITriggerExternal {

    public final EnumPowerStage stage;

    public TriggerEnginePowerStage(EnumPowerStage stage) {
        super("buildcraft:engine.stage." + stage.name().toLowerCase(Locale.ROOT));
        this.stage = stage;
    }

    public static boolean isTriggeringTile(TileEntity tile) {
        return tile instanceof TileEngineBase_BC8;
    }

    @Override
    public ITextComponent getDescription() {
//        return LocaleUtil.localize("gate.trigger.engine." + stage.name());
        return new TranslationTextComponent("gate.trigger.engine." + stage.getSerializedName());
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.engine." + stage.getSerializedName();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_POWER_STAGE.get(stage);
    }

    @Override
    public boolean isTriggerActive(TileEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof TileEngineBase_BC8) {
            return ((TileEngineBase_BC8) target).getPowerStage() == stage;
        }
        return false;
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER_STAGES;
    }
}
