package ct.buildcraft.transport.statements;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.gates.IGate;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.api.statements.StatementParameterItemStack;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.transport.BCTransportSprites;
import ct.buildcraft.transport.pipe.flow.PipeFlowFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class TriggerFluidsTraversing extends BCStatement implements ITriggerInternal {

    public TriggerFluidsTraversing() {
        super("buildcraft:pipe_contains_fluids");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.pipe.containsFluids");
    }

    @Override
    public ISprite getSprite() {
        return BCTransportSprites.TRIGGER_FLUIDS_TRAVERSING;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
    	FluidStack searchedFluid = FluidStack.EMPTY;
        if (parameters != null && parameters.length >= 1 && parameters[0] != null) {
        	ItemStack searchedStack = parameters[0].getItemStack();
            searchedFluid = FluidUtil.getFluidContained(searchedStack).orElse(FluidStack.EMPTY);
        }
        return source instanceof IGate gate 
        		&& gate.getPipeHolder().getPipe().getFlow() instanceof PipeFlowFluids fluidflow
        		&& fluidflow.doesContainFluid(searchedFluid);
    }
    
    @Override
    public int maxParameters() {
        return 1;
    }
    
    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }
}
