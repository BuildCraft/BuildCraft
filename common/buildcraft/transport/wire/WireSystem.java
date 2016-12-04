package buildcraft.transport.wire;

import buildcraft.api.transport.neptune.EnumWirePart;
import buildcraft.api.transport.neptune.IPipeHolder;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStructure;
import buildcraft.transport.plug.PluggableGate;
import com.google.common.base.Predicates;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WireSystem {
    public final List<Element> elements = new ArrayList<>();
    public EnumDyeColor color = null;

    public boolean hasElement(Element element) {
        return elements.contains(element);
    }

    public static boolean canWireConnect(IPipeHolder holder, EnumFacing side, boolean recursive) {
        TileEntity otherTile = holder.getPipeWorld().getTileEntity(holder.getPipePos().offset(side));
        if(otherTile instanceof IPipeHolder) {
            IPipeHolder otherHolder = (IPipeHolder) otherTile;
            if((otherHolder.getPipe() != null && otherHolder.getPipe().getBehaviour() instanceof PipeBehaviourStructure) ||
                    (!recursive && canWireConnect(otherHolder, side.getOpposite(), true))) {
                return true;
            }
        }
        return holder.getPipe() != null && holder.getPipe().isConnected(side);
    }

    public static boolean canWireConnect(IPipeHolder holder, EnumFacing side) {
        return canWireConnect(holder, side, false);
    }

    public static List<Element> getConnectedElementsOfElement(IPipeHolder holder, Element element) {
        assert element.wirePart != null;
        return element.wirePart.getAllPossibleConnections().stream().map(sidePosPart -> {
            if(sidePosPart.getLeft() == null || canWireConnect(holder, sidePosPart.getLeft())) {
                return new Element(element.blockPos.add(sidePosPart.getMiddle()), sidePosPart.getRight());
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<Element> getConnectedElementsOfElement(World world, Element element) {
        if(element.type == Element.Type.WIRE_PART) {
            TileEntity tile = world.getTileEntity(element.blockPos);
            if(tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                return getConnectedElementsOfElement(holder, element);
            }
        }
        return Collections.emptyList();
    }

    public WireSystem build(WorldSavedDataWireSystems wireSystems, Element startElement) {
        long time = System.currentTimeMillis();
        Map<BlockPos, IPipeHolder> holdersCache = new HashMap<>();
        Set<Element> walked = new HashSet<>();
        Queue<Element> queue = new ArrayDeque<>();
        Consumer<Element> build = element -> {
            if(!walked.contains(element)) {
                if(!holdersCache.containsKey(element.blockPos)) {
                    TileEntity tile = wireSystems.world.getTileEntity(element.blockPos);
                    IPipeHolder holder = null;
                    if(tile instanceof IPipeHolder) {
                        holder = (IPipeHolder) tile;
                    }
                    holdersCache.put(element.blockPos, holder);
                }
                IPipeHolder holder = holdersCache.get(element.blockPos);
                if(holder != null) {
                    if(element.type == Element.Type.WIRE_PART) {
                        EnumDyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                        if(color == null) {
                            if(colorOfPart != null) {
                                color = colorOfPart;
                            }
                        }
                        if(color != null && colorOfPart == color) {
                            wireSystems.getWireSystemsWithElement(element).stream().filter(wireSystem -> wireSystem != this && wireSystem.color == this.color).forEach(wireSystems::removeWireSystem);
                            elements.add(element);
                            queue.addAll(getConnectedElementsOfElement(wireSystems.world, element));
                            Arrays.stream(EnumFacing.values()).forEach(side -> queue.add(new Element(element.blockPos, side)));
                        }
                    } else if(element.type == Element.Type.EMITTER_SIDE) {
                        if(holder.getPluggable(element.emitterSide) instanceof PluggableGate) {
                            elements.add(new Element(element.blockPos, element.emitterSide));
                        }
                    }
                }
                walked.add(element);
            }
        };
        queue.add(startElement);
        while(!queue.isEmpty()) {
            build.accept(queue.poll());
        }
        System.out.println("Building take: " + (System.currentTimeMillis() - time) + "ms");
        return this;
    }

    public boolean isEmpty() {
        return elements.stream().filter(element -> element.type == Element.Type.WIRE_PART).count() == 0;
    }

    public boolean update(WorldSavedDataWireSystems wireSystems) {
        return elements.stream()
                .filter(element -> element.type == Element.Type.EMITTER_SIDE)
                .map(element -> wireSystems.isEmitterEmitting(element, color))
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    public List<ChunkPos> getChunkPoses() {
        return elements.stream().map(element -> element.blockPos).map(ChunkPos::new).collect(Collectors.toList());
    }

    public boolean isPlayerWatching(EntityPlayerMP player) {
        if(player.worldObj instanceof WorldServer) {
            WorldServer world = (WorldServer) player.worldObj;
            // noinspection Guava
            return getChunkPoses().stream()
                    .map(chunkPos -> world.getPlayerChunkMap().getEntry(chunkPos.chunkXPos, chunkPos.chunkZPos))
                    .filter(Objects::nonNull)
                    .anyMatch(playerChunkMapEntry -> playerChunkMapEntry.hasPlayerMatching(Predicates.equalTo(player)));
        }
        return false;
    }

    public int getWiresHashCode() {
        return elements.stream().filter(element -> element.type == Element.Type.WIRE_PART).collect(Collectors.toList()).hashCode();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList elementsList = new NBTTagList();
        elements.stream().map(Element::writeToNBT).forEach(elementsList::appendTag);
        nbt.setTag("elements", elementsList);
        nbt.setInteger("color", color.getMetadata());
        return nbt;
    }

    public WireSystem readFromNBT(NBTTagCompound nbt) {
        elements.clear();
        NBTTagList elementsList = nbt.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        IntStream.range(0, elementsList.tagCount()).mapToObj(elementsList::getCompoundTagAt).map(Element::new).forEach(elements::add);
        color = EnumDyeColor.byMetadata(nbt.getInteger("color"));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        WireSystem that = (WireSystem) o;

        if(!elements.equals(that.elements)) {
            return false;
        }
        return color == that.color;
    }

    @Override
    public int hashCode() {
        int result = elements.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
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

        public Element(ByteBuf buf) {
            type = Type.values()[buf.readInt()];
            blockPos = new PacketBuffer(buf).readBlockPos();
            if(type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[buf.readInt()];
                this.emitterSide = null;
            } else if(type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = EnumFacing.getFront(buf.readInt());
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public Element(NBTTagCompound nbt) {
            type = Type.values()[nbt.getInteger("type")];
            blockPos = NBTUtils.readBlockPos(nbt.getTag("blockPos"));
            if(type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[nbt.getInteger("wirePart")];
                this.emitterSide = null;
            } else if(type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = EnumFacing.getFront(nbt.getInteger("emitterSide"));
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public void toBytes(ByteBuf buf) {
            buf.writeInt(type.ordinal());
            new PacketBuffer(buf).writeBlockPos(blockPos);
            if(type == Type.WIRE_PART) {
                assert wirePart != null;
                buf.writeInt(wirePart.ordinal());
            } else if(type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                buf.writeInt(emitterSide.getIndex());
            }
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("type", type.ordinal());
            nbt.setTag("blockPos", NBTUtils.writeBlockPos(blockPos));
            if(type == Type.WIRE_PART) {
                assert wirePart != null;
                nbt.setInteger("wirePart", wirePart.ordinal());
            } else if(type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                nbt.setInteger("emitterSide", emitterSide.getIndex());
            }
            return nbt;
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
