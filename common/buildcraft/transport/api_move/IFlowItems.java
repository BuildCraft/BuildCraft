package buildcraft.transport.api_move;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IStackFilter;

public interface IFlowItems {

    void setTargetSpeed(double targetSpeed);

    double getTargetSpeed();

    void setSpeedDelta(double speedDelta);

    double getSpeedDelta();

    /** @param count
     * @param from
     * @param filter The filter to determine what can be extracted.
     * @return The number of items extracted. */
    int tryExtractStack(int count, EnumFacing from, IStackFilter filter);
}
