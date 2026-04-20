package ct.buildcraft.core.statements;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.enums.EnumPowerStage;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerExternal;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.core.BCCoreStatements;
import ct.buildcraft.lib.engine.TileEngineBase_BC8;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerEnginePowerStage extends BCStatement implements ITriggerExternal {

    public final EnumPowerStage stage;

    public TriggerEnginePowerStage(EnumPowerStage stage) {
        super("buildcraft:engine.stage." + stage.getModelName());
        this.stage = stage;
    }

    public static boolean isTriggeringTile(BlockEntity tile) {
        return tile instanceof TileEngineBase_BC8;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.engine." + stage.getModelName());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
