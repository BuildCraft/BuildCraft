package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipeHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WireSystem {
    public final List<Element> elements = new ArrayList<>();

    public boolean hasElement(Element element) {
        return elements.contains(element);
    }

    public WireSystem build(WorldSavedDataWireSystems wireSystems, Element startElement) {
        if(elements.contains(startElement)) {
            return this;
        } else {
            System.out.println(startElement);
        }
        wireSystems.getWireSystemsWithElement(startElement).stream().filter(wireSystem -> wireSystem != this).forEach(wireSystems::removeWireSystem);
        if(startElement.type == Element.Type.WIRE_PART) {
            if(wireSystems.world.getTileEntity(startElement.blockPos) instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(startElement.blockPos);
                //noinspection ConstantConditions
                if(holder.getWireManager().getColorOfPart(startElement.wirePart) != null) {
                    elements.add(startElement);
                }
            }
            for(EnumWirePart part : EnumWirePart.VALUES) {
                EnumFacing.Axis axis = null;
                if(startElement.wirePart != part) {
                    //noinspection ConstantConditions
                    if(startElement.wirePart.y == part.y && startElement.wirePart.z == part.z) {
                        axis = EnumFacing.Axis.X;
                    } else if(startElement.wirePart.z == part.z && startElement.wirePart.x == part.x) {
                        axis = EnumFacing.Axis.Y;
                    } else if(startElement.wirePart.x == part.x && startElement.wirePart.y == part.y) {
                        axis = EnumFacing.Axis.Z;
                    }
                }
                if(axis != null) {
                    if(wireSystems.world.getTileEntity(startElement.blockPos) instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(startElement.blockPos);
                        //noinspection ConstantConditions
                        if(holder.getWireManager().getColorOfPart(startElement.wirePart) != null && holder.getWireManager().getColorOfPart(startElement.wirePart) == holder.getWireManager().getColorOfPart(part)) {
                            build(wireSystems, new Element(startElement.blockPos, part));
                        }
                    }
                    BlockPos otherPos = startElement.blockPos.offset(EnumFacing.getFacingFromAxis(startElement.wirePart.getDirection(axis), axis));
                    if(wireSystems.world.getTileEntity(otherPos) instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) wireSystems.world.getTileEntity(otherPos);
                        //noinspection ConstantConditions
                        if(holder.getWireManager().getColorOfPart(startElement.wirePart) != null && holder.getWireManager().getColorOfPart(startElement.wirePart) == holder.getWireManager().getColorOfPart(part)) {
                            build(wireSystems, new Element(otherPos, part));
                        }
                    }
                }
            }
        }
        return this;
    }

    public boolean isEmpty() {
        return elements.isEmpty();
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
            this.type = Type.WIRE_PART;
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
            EMITTER
        }
    }
}
