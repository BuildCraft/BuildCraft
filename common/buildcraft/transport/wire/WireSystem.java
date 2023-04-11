/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Predicates;

import com.google.common.collect.ImmutableList;
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

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;

public final class WireSystem {
    public final ImmutableList<WireElement> elements;
    public final EnumDyeColor color;

    private transient final int cachedHashCode;
    private transient final int cachedWiresHashCode;

    public boolean hasElement(WireElement element) {
        return elements.contains(element);
    }

    /** Checks to see if the given holder could connect a wire across the specified side even if a matching wire wasn't
     * there. */
    public static boolean canWireConnect(IPipeHolder holder, EnumFacing side) {
        // TODO: Expand to pipeless wires (8.1.x)
        IPipe pipe = holder.getPipe();
        if (pipe == null) {
            return false;
        }
        IPipe oPipe = holder.getNeighbourPipe(side);
        if (oPipe == null) {
            return false;
        }
        if (pipe.isConnected(side)) {
            return true;
        }
        if ((holder.getPluggable(side) != null && holder.getPluggable(side).isBlocking()) //
            || (oPipe.getHolder().getPluggable(side.getOpposite()) != null && oPipe.getHolder().getPluggable(side.getOpposite()).isBlocking())) {
            return false;
        }
        if (pipe.getDefinition().flowType == PipeApi.flowStructure || oPipe.getDefinition().flowType == PipeApi.flowStructure) {
            return pipe.getColour() == null || oPipe.getColour() == null || pipe.getColour() == oPipe.getColour();
        }
        return false;
    }

    public static List<WireElement> getConnectedElementsOfElement(IPipeHolder holder, WireElement element) {
        assert element.wirePart != null;
        WireNode node = new WireNode(element.blockPos, element.wirePart);

        List<WireElement> list = new ArrayList<>();
        for (EnumFacing face : EnumFacing.VALUES) {
            WireNode oNode = node.offset(face);
            // equality check is fine here -- WireNode.offset returns the same blockpos (identity wise) if its the same
            if (oNode.pos == node.pos || canWireConnect(holder, face)) {
                list.add(new WireElement(oNode.pos, oNode.part));
            }
        }
        return list;
    }

    public static List<WireElement> getConnectedElementsOfElement(World world, WireElement element) {
        if (element.type == WireElement.Type.WIRE_PART) {
            TileEntity tile = world.getTileEntity(element.blockPos);
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                return getConnectedElementsOfElement(holder, element);
            }
        }
        return Collections.emptyList();
    }

    public WireSystem(ImmutableList<WireElement> elements, EnumDyeColor color) {
        this.elements = Objects.requireNonNull(elements, "elements");
        this.color = color;

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    public WireSystem(WorldSavedDataWireSystems wireSystems, WireElement startElement) {
        long time = System.currentTimeMillis();
        Map<BlockPos, IPipeHolder> holdersCache = new HashMap<>();
        Set<WireElement> walked = new HashSet<>();

        Queue<WireElement> queue = new ArrayDeque<>();
        queue.add(startElement);

        EnumDyeColor tempColor = null;
        ImmutableList.Builder<WireElement> elementBuilder = ImmutableList.builder();

        while (!queue.isEmpty()) {
            WireElement element = queue.remove();

            if (!walked.contains(element)) {
                if (!holdersCache.containsKey(element.blockPos)) {
                    TileEntity tile = wireSystems.world.getTileEntity(element.blockPos);
                    IPipeHolder holder = null;
                    if (tile instanceof IPipeHolder) {
                        holder = (IPipeHolder) tile;
                    }
                    holdersCache.put(element.blockPos, holder);
                }
                IPipeHolder holder = holdersCache.get(element.blockPos);
                if (holder != null) {
                    if (element.type == WireElement.Type.WIRE_PART) {
                        EnumDyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                        if (tempColor == null) {
                            if (colorOfPart != null) {
                                tempColor = colorOfPart;
                            }
                        }
                        if (tempColor != null && colorOfPart == tempColor) {
                            EnumDyeColor colorButFinal = tempColor; //damn you java
                            wireSystems.getWireSystemsWithElement(element).stream().filter(wireSystem -> wireSystem != this && wireSystem.color == colorButFinal).forEach(wireSystems::removeWireSystem);
                            elementBuilder.add(element);
                            queue.addAll(getConnectedElementsOfElement(wireSystems.world, element));
                            Arrays.stream(EnumFacing.VALUES).forEach(side -> queue.add(new WireElement(element.blockPos, side)));
                        }
                    } else if (element.type == WireElement.Type.EMITTER_SIDE) {
                        if (holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                            elementBuilder.add(new WireElement(element.blockPos, element.emitterSide));
                        }
                    }
                }
                walked.add(element);
            }
        }

        this.elements = elementBuilder.build();
        this.color = tempColor;

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    public boolean isEmpty() {
        return elements.stream().filter(element -> element.type == WireElement.Type.WIRE_PART).count() == 0;
    }

    public boolean update(WorldSavedDataWireSystems wireSystems) {
        return elements.stream().filter(element -> element.type == WireElement.Type.EMITTER_SIDE).map(element -> wireSystems.isEmitterEmitting(element, color)).reduce(Boolean::logicalOr).orElse(
            false);
    }

    public List<ChunkPos> getChunkPoses() {
        return this.getChunkPosesAsStream().collect(Collectors.toList());
    }

    public Stream<ChunkPos> getChunkPosesAsStream() {
        return elements.stream().map(element -> new ChunkPos(element.blockPos));
    }

    public boolean isPlayerWatching(EntityPlayerMP player) {
        if (player.world instanceof WorldServer) {
            WorldServer world = (WorldServer) player.world;
            return getChunkPosesAsStream().map(chunkPos -> world.getPlayerChunkMap().getEntry(chunkPos.x, chunkPos.z))
                    .anyMatch(playerChunkMapEntry -> playerChunkMapEntry != null && playerChunkMapEntry.containsPlayer(player));
        }
        return false;
    }

    public int getWiresHashCode() {
        return this.cachedWiresHashCode;
    }

    private int computeCachedWiresHashCode() {
        return elements.stream().filter(element -> element.type == WireElement.Type.WIRE_PART)
                //the following is equivalent to .collect(Collectors.toList()).hashCode(), by the definition of List#hashCode():
                .mapToInt(WireElement::hashCode).reduce(1, (hashCode, elementHashCode) -> hashCode * 31 + elementHashCode);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList elementsList = new NBTTagList();
        elements.stream().map(WireElement::writeToNBT).forEach(elementsList::appendTag);
        nbt.setTag("elements", elementsList);
        nbt.setInteger("color", color.getMetadata());
        return nbt;
    }

    public WireSystem(NBTTagCompound nbt) {
        NBTTagList elementsList = nbt.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        //noinspection UnstableApiUsage
        elements = IntStream.range(0, elementsList.tagCount()).mapToObj(elementsList::getCompoundTagAt).map(WireElement::new).collect(ImmutableList.toImmutableList());
        color = EnumDyeColor.byMetadata(nbt.getInteger("color"));

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WireSystem that = (WireSystem) o;

        if (this.cachedHashCode != that.cachedHashCode) {
            //both have a cached hashCode, and the hash codes don't match
            return false;
        }

        if (!elements.equals(that.elements)) {
            return false;
        }
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    private int computeHashCode() {
        int result = elements.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    public static class WireElement {
        public final Type type;
        public final BlockPos blockPos;
        public final EnumWirePart wirePart;
        public final EnumFacing emitterSide;

        public WireElement(BlockPos blockPos, EnumWirePart wirePart) {
            this.type = Type.WIRE_PART;
            this.blockPos = blockPos;
            this.wirePart = wirePart;
            this.emitterSide = null;
        }

        public WireElement(BlockPos blockPos, EnumFacing emitterSide) {
            this.type = Type.EMITTER_SIDE;
            this.blockPos = blockPos;
            this.wirePart = null;
            this.emitterSide = emitterSide;
        }

        public WireElement(PacketBuffer buf) {
            type = Type.values()[buf.readInt()];
            blockPos = MessageUtil.readBlockPos(buf);
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[buf.readInt()];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = EnumFacing.getFront(buf.readInt());
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public WireElement(NBTTagCompound nbt) {
            type = Type.values()[nbt.getInteger("type")];
            blockPos = NBTUtilBC.readBlockPos(nbt.getTag("blockPos"));
            if (blockPos == null) {
                // Oh dear. We probably can't recover from this properly
                throw new NullPointerException("Cannot read this Wire Systems from NBT!");
            }
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[nbt.getInteger("wirePart")];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = EnumFacing.getFront(nbt.getInteger("emitterSide"));
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public void toBytes(PacketBuffer buf) {
            buf.writeInt(type.ordinal());
            MessageUtil.writeBlockPos(buf, blockPos);
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                buf.writeInt(wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                buf.writeInt(emitterSide.getIndex());
            }
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("type", type.ordinal());
            nbt.setTag("blockPos", NBTUtilBC.writeBlockPos(blockPos));
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                nbt.setInteger("wirePart", wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                nbt.setInteger("emitterSide", emitterSide.getIndex());
            }
            return nbt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            WireElement element = (WireElement) o;

            if (type != element.type) {
                return false;
            }
            if (!blockPos.equals(element.blockPos)) {
                return false;
            }
            if (wirePart != element.wirePart) {
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
            return "Element{" + "type=" + type + ", blockPos=" + blockPos + ", wirePart=" + wirePart + ", emitterSide=" + emitterSide + '}';
        }

        public enum Type {
            WIRE_PART,
            EMITTER_SIDE
        }
    }
}
