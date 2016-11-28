package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.transport.plug.PluggableGate;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class WireSystem {
    public final List<Element> elements = new ArrayList<>();
    public EnumDyeColor color = null;

    public boolean hasElement(Element element) {
        return elements.contains(element);
    }

    public static List<Element> getConnectedElementsOfElement(World world, Element element) {
        if(element.type == Element.Type.WIRE_PART) {
            TileEntity tile = world.getTileEntity(element.blockPos);
            if(tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                assert element.wirePart != null;
                return element.wirePart.getAllPossibleConnections().stream().map(sidePosPart -> {
                    if(sidePosPart.getLeft() == null || holder.getPipe().isConnected(sidePosPart.getLeft())) {
                        return new Element(element.blockPos.add(sidePosPart.getMiddle()), sidePosPart.getRight());
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public WireSystem build(WorldSavedDataWireSystems wireSystems, Element element) {
        if(!elements.contains(element)) {
            TileEntity tile = wireSystems.world.getTileEntity(element.blockPos);
            if(tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                if(element.type == Element.Type.WIRE_PART) {
                    wireSystems.getWireSystemsWithElement(element).stream().filter(wireSystem -> wireSystem != this).forEach(wireSystems::removeWireSystem);
                    EnumDyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                    if(color == null) {
                        if(colorOfPart != null) {
                            color = colorOfPart;
                        }
                    }
                    if(color != null && colorOfPart == color) {
                        elements.add(element);
                        getConnectedElementsOfElement(wireSystems.world, element).forEach(localElement -> build(wireSystems, localElement));
                    }
                    Arrays.stream(EnumFacing.values()).forEach(side -> build(wireSystems, new Element(element.blockPos, side)));
                } else if(element.type == Element.Type.EMITTER_SIDE) {
                    if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                        elements.add(new Element(element.blockPos, element.emitterSide));
                    }
                }
            }
        }
        return this;
    }

    public boolean isEmpty() {
        return elements.stream().filter(element -> element.type == Element.Type.WIRE_PART).count() == 0;
    }

    public boolean update(WorldSavedDataWireSystems wireSystems) {
        return elements.stream().filter(element -> element.type == Element.Type.EMITTER_SIDE).map(element -> {
            TileEntity tile = wireSystems.world.getTileEntity(element.blockPos);
            if(tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                    PluggableGate gate = (PluggableGate) holder.getPluggable(element.emitterSide);
                    return gate.logic.isEmitting(color);
                }
            }
            return false;
        }).reduce(Boolean::logicalAnd).orElse(false);
    }

    public NBTTagCompound writeToNBT() {
        return null;
    }

    public WireSystem readFromNBT(NBTTagCompound nbt) {
        return this;
    }

    public static class Element {
        public final Type type;
        public final BlockPos blockPos;
        public final EnumWirePart wirePart;
        public final EnumFacing emitterSide;

        public Element(BlockPos blockPos, EnumWirePart wirePart) {
            this.type = Type.WIRE_PART;
            this.blockPos = blockPos;
            this.wirePart = wirePart;
            this.emitterSide = null;
        }

        public Element(BlockPos blockPos, EnumFacing emitterSide) {
            this.type = Type.EMITTER_SIDE;
            this.blockPos = blockPos;
            this.wirePart = null;
            this.emitterSide = emitterSide;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }

            Element element = (Element) o;

            if(type != element.type) {
                return false;
            }
            if(!blockPos.equals(element.blockPos)) {
                return false;
            }
            if(wirePart != element.wirePart) {
                return false;
            }
            return emitterSide == element.emitterSide;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + blockPos.hashCode();
            result = 31 * result + (wirePart != null ? wirePart.hashCode() : 0);
            result = 31 * result + (emitterSide != null ? emitterSide.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "type=" + type +
                    ", blockPos=" + blockPos +
                    ", wirePart=" + wirePart +
                    ", emitterSide=" + emitterSide +
                    '}';
        }

        public enum Type {
            WIRE_PART,
            EMITTER_SIDE
        }
    }
}
