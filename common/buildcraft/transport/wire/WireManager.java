package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.api.transport.neptune.IWireManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.Map;

public class WireManager implements IWireManager {
    private final IPipeHolder holder;
    public final Map<EnumWirePart, EnumDyeColor> wires = new EnumMap<>(EnumWirePart.class);
    // TODO: Wire connections to adjacent blocks

    public WireManager(IPipeHolder holder) {
        this.holder = holder;
    }

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public boolean addWire(EnumWirePart part, EnumDyeColor colour) {
        if(getColorOfWire(part) == null) {
            wires.put(part, colour);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public EnumDyeColor removeWire(EnumWirePart part) {
        EnumDyeColor colour = getColorOfWire(part);
        if(colour == null) {
            return null;
        } else {
            wires.remove(part);
            return colour;
        }
    }

    @Override
    public EnumDyeColor getColorOfWire(EnumWirePart part) {
        return wires.get(part);
    }

    public void onBlockRemoved() {
        World world = getHolder().getPipeWorld();
    }
}
