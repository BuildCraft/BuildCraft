package buildcraft.core.statements;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;

import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

public class TriggerEnginePowerStage extends BCStatement implements ITriggerExternal {

    public final EnumPowerStage stage;

    public TriggerEnginePowerStage(EnumPowerStage stage) {
        super("buildcraft:engine.stage." + stage.getName());
        this.stage = stage;
    }

    public static boolean isTriggeringTile(BlockEntity tile) {
        return tile instanceof TileEngineBase_BC8;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.engine." + stage.getName());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_POWER_STAGE.get(stage);
    }

    @Override
    public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source,
        IStatementParameter[] parameters) {
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
