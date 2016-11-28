package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.transport.plug.PluggableGate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WireSystem {
    public final List<Element> elements = new ArrayList<>();

    public boolean hasElement(Element element) {
        return elements.contains(element);
    }

    public WireSystem build(WorldSavedDataWireSystems wireSystems, Element element) {
        if(elements.contains(element)) {
            return this;
        }
        if(element.type == Element.Type.WIRE_PART) {
            wireSystems.getWireSystemsWithElement(element).stream().filter(wireSystem -> wireSystem != this).forEach(wireSystems::removeWireSystem);
            if(wireSystems.world.getTileEntity(element.blockPos) instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(element.blockPos);
                //noinspection ConstantConditions
                if(holder.getWireManager().getColorOfPart(element.wirePart) != null) {
                    elements.add(element);
                }
            }
            for(EnumWirePart part : EnumWirePart.VALUES) {
                EnumFacing.Axis axis = null;
                if(element.wirePart != part) {
                    //noinspection ConstantConditions
                    if(element.wirePart.y == part.y && element.wirePart.z == part.z) {
                        axis = EnumFacing.Axis.X;
                    } else if(element.wirePart.z == part.z && element.wirePart.x == part.x) {
                        axis = EnumFacing.Axis.Y;
                    } else if(element.wirePart.x == part.x && element.wirePart.y == part.y) {
                        axis = EnumFacing.Axis.Z;
                    }
                }
                if(axis != null) {
                    if(wireSystems.world.getTileEntity(element.blockPos) instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(element.blockPos);
                        //noinspection ConstantConditions
                        if(holder.getWireManager().getColorOfPart(element.wirePart) != null && holder.getWireManager().getColorOfPart(element.wirePart) == holder.getWireManager().getColorOfPart(part)) {
                            build(wireSystems, new Element(element.blockPos, part));
                        }
                    }
                    BlockPos otherPos = element.blockPos.offset(EnumFacing.getFacingFromAxis(element.wirePart.getDirection(axis), axis));
                    if(wireSystems.world.getTileEntity(otherPos) instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(otherPos);
                        //noinspection ConstantConditions
                        if(holder.getWireManager().getColorOfPart(element.wirePart) != null && holder.getWireManager().getColorOfPart(element.wirePart) == holder.getWireManager().getColorOfPart(part)) {
                            build(wireSystems, new Element(otherPos, part));
                        }
                    }
                }
            }
            Arrays.stream(EnumFacing.values()).forEach(side -> build(wireSystems, new Element(element.blockPos, side)));
        } else if(element.type == Element.Type.EMITTER_SIDE) {
            if(wireSystems.world.getTileEntity(element.blockPos) instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(element.blockPos);
                //noinspection ConstantConditions
                if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                    elements.add(new Element(element.blockPos, element.emitterSide));
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
            if(wireSystems.world.getTileEntity(element.blockPos) instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(element.blockPos);
                //noinspection ConstantConditions
                if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                    PluggableGate gate = (PluggableGate) holder.getPluggable(element.emitterSide);
                    return gate.logic.isEmitting(holder.getWireManager().getColorOfPart(
                            elements.stream()
                                    .filter(localElement -> localElement.type == Element.Type.WIRE_PART && localElement.blockPos.equals(element.blockPos))
                                    .findAny().orElse(null).wirePart)
                    );
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
