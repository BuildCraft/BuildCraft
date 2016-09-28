package buildcraft.transport.wire;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;

import buildcraft.transport.api_move.EnumWirePart;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.IWireManager;

public class WireManager implements IWireManager {
    private final IPipeHolder holder;
    private final Map<EnumWirePart, EnumDyeColor> wiresByPart = new EnumMap<>(EnumWirePart.class);
    private final Map<EnumDyeColor, EnumWirePart> wiresByColour = new EnumMap<>(EnumDyeColor.class);
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
        if (getWireByPart(part) == null && getWireByColour(colour) == null) {
            wiresByPart.put(part, colour);
            wiresByColour.put(colour, part);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public EnumDyeColor removeWireByPart(EnumWirePart part) {
        EnumDyeColor colour = getWireByPart(part);
        if (colour == null) {
            return null;
        } else if (part == getWireByColour(colour)) {
            wiresByColour.remove(colour);
            wiresByPart.remove(part);
            return colour;
        } else {
            throw new IllegalStateException("Mismatched colour with part!");
        }
    }

    @Override
    public EnumWirePart removeWireByColour(EnumDyeColor colour) {
        EnumWirePart part = getWireByColour(colour);
        if (part == null) {
            return null;
        } else if (colour == getWireByPart(part)) {
            wiresByColour.remove(colour);
            wiresByPart.remove(part);
            return part;
        } else {
            throw new IllegalStateException("Mismatched colour with part!");
        }
    }

    @Override
    public EnumDyeColor getWireByPart(EnumWirePart part) {
        return wiresByPart.get(part);
    }

    @Override
    public EnumWirePart getWireByColour(EnumDyeColor colour) {
        return wiresByColour.get(colour);
    }

    public void onBlockRemoved() {
        World world = getHolder().getPipeWorld();
    }
}
