package buildcraft.transport.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import buildcraft.transport.item.ItemPluggableFacade;
import buildcraft.transport.plug.FacadeInstance;
import buildcraft.transport.plug.FacadePhasedState;

public enum FacadeItemColours implements IItemColor {
    INSTANCE;

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        FacadeInstance states = ItemPluggableFacade.getStates(stack);
        FacadePhasedState state = states.getCurrentStateForStack();
        int colour = -1;
        try {
            colour = Minecraft.getMinecraft().getBlockColors().getColor(state.stateInfo.state, null, null);
        } catch (NullPointerException ex) {
            // the block didn't like the null world or player
        }
        return colour;
    }
}
