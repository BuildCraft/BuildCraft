package buildcraft.core.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    public static boolean isTriggeringTile(TileEntity tile) {
        return tile instanceof TileEngineBase_BC8;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.engine." + stage.getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_POWER_STAGE.get(stage);
    }

    @Override
    public boolean isTriggerActive(TileEntity target, EnumFacing side, IStatementContainer source,
        IStatementParameter[] parameters) {
        return target instanceof TileEngineBase_BC8 && ((TileEngineBase_BC8) target).getPowerStage() == stage;
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_POWER_STAGES;
    }
}
