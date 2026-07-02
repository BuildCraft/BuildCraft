package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementParameterItemStack;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionStationAcceptFluids extends BCStatement implements IActionInternal {

    public ActionStationAcceptFluids() {
        super("buildcraft:station.accept_fluids");
    }

    @Override
    public int maxParameters() { return 3; }
    @Override
    public int minParameters() { return 1; }

    @Override
    public IStatementParameter createParameter(int index) {
        return StatementParameterItemStack.EMPTY;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.station.acceptFluids");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_STATION_ACCEPT_FLUIDS;
    }
}
