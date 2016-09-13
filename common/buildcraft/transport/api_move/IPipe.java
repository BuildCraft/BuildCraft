package buildcraft.transport.api_move;

import net.minecraft.item.EnumDyeColor;

public interface IPipe {
    IPipeHolder getHolder();

    PipeDefinition getDefinition();

    PipeBehaviour getBehaviour();

    EnumDyeColor getColour();

    void setColour(EnumDyeColor colour);

    public enum ConnectedType {
        TILE,
        PIPE;
    }
}
