/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

// Yarn 1.20.1 renames:
//   ServerPlayerEntity        → ServerPlayerEntity   NbtCompound → NbtCompound   NbtList → NbtList
//   PacketByteBuf          → PacketByteBuf        BlockEntity     → BlockEntity   Direction → Direction
//   DyeColor          → DyeColor             ServerWorld    → ServerWorld
//   world.getTileEntity   → world.getBlockEntity     Direction.getFront(i) → Direction.byId(i)
//   color.getId()   → color.getId()        emitterSide.getIndex() → emitterSide.getId()
//   NbtElement.COMPOUND_TYPE → NbtElement.COMPOUND_TYPE
//   MessageUtil.read/writeBlockPos → PacketByteBuf native readBlockPos()/writeBlockPos()
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;

import buildcraft.lib.misc.NBTUtilBC;

public final class WireSystem {

    private static final AtomicInteger NETWORK_IDS = new AtomicInteger();
    private static int nextServerNetworkId() {
        return NETWORK_IDS.incrementAndGet();
    }

    public final ImmutableList<WireElement> elements;
    public final DyeColor color;
    public final int networkId;

    private transient final int cachedHashCode;
    private transient final int cachedWiresHashCode;

    public boolean hasElement(WireElement element) {
        return elements.contains(element);
    }

    /** Checks to see if the given holder could connect a wire across the specified side even if a matching wire wasn't
     * there. */
    public static boolean canWireConnect(IPipeHolder holder, Direction side) {
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
        for (Direction face : Direction.values()) {
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
            BlockEntity tile = world.getBlockEntity(element.blockPos);
            if (tile instanceof IPipeHolder) {
                IPipeHolder holder = (IPipeHolder) tile;
                return getConnectedElementsOfElement(holder, element);
            }
        }
        return Collections.emptyList();
    }

    public WireSystem(ImmutableList<WireElement> elements, DyeColor color) {
        this(nextServerNetworkId(), elements, color);
    }

    public WireSystem(int netId, ImmutableList<WireElement> elements, DyeColor color) {
        this.networkId = netId;
        this.elements = Objects.requireNonNull(elements, "elements");
        this.color = color;

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    public WireSystem(WorldSavedDataWireSystems wireSystems, WireElement startElement) {
        this.networkId = nextServerNetworkId();
        Map<BlockPos, IPipeHolder> holdersCache = new HashMap<>();
        Set<WireElement> walked = new HashSet<>();

        Queue<WireElement> queue = new ArrayDeque<>();
        queue.add(startElement);

        DyeColor tempColor = null;
        ImmutableList.Builder<WireElement> elementBuilder = ImmutableList.builder();

        while (!queue.isEmpty()) {
            WireElement element = queue.remove();

            if (!walked.contains(element)) {
                if (!holdersCache.containsKey(element.blockPos)) {
                    BlockEntity tile = wireSystems.getWorld().getBlockEntity(element.blockPos);
                    IPipeHolder holder = null;
                    if (tile instanceof IPipeHolder) {
                        holder = (IPipeHolder) tile;
                    }
                    holdersCache.put(element.blockPos, holder);
                }
                IPipeHolder holder = holdersCache.get(element.blockPos);
                if (holder != null) {
                    if (element.type == WireElement.Type.WIRE_PART) {
                        DyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                        if (tempColor == null) {
                            if (colorOfPart != null) {
                                tempColor = colorOfPart;
                            }
                        }
                        if (tempColor != null && colorOfPart == tempColor) {
                            DyeColor colorButFinal = tempColor; //damn you java
                            wireSystems.getWireSystemsWithElement(element).stream().filter(wireSystem -> wireSystem != this && wireSystem.color == colorButFinal).forEach(wireSystems::removeWireSystem);
                            elementBuilder.add(element);
                            queue.addAll(getConnectedElementsOfElement(wireSystems.getWorld(), element));
                            Arrays.stream(Direction.values()).forEach(side -> queue.add(new WireElement(element.blockPos, side)));
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

    // STUB(R.Chen): server→client wire sync (MessageWireSystems / MessageWireSystemsPowered) is deferred to
    // Phase 5 with the rest of the BC networking layer, so the per-player visibility check has no caller yet.
    // The Forge ServerWorld.getPlayerChunkMap().getEntry(...).containsPlayer(player) path maps to Yarn's
    // ServerChunkManager.threadedAnvilChunkStorage — restore it when the broadcast is reinstated.
    public boolean isPlayerWatching(ServerPlayerEntity player) {
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

    public NbtCompound writeToNBT() {
        NbtCompound nbt = new NbtCompound();
        NbtList elementsList = new NbtList();
        elements.stream().map(WireElement::writeToNBT).forEach(elementsList::add);
        nbt.put("elements", elementsList);
        nbt.putInt("color", color.getId());
        return nbt;
    }

    public WireSystem(NbtCompound nbt) {
        networkId = nextServerNetworkId();
        NbtList elementsList = nbt.getList("elements", NbtElement.COMPOUND_TYPE);
        //noinspection UnstableApiUsage
        elements = IntStream.range(0, elementsList.size()).mapToObj(elementsList::getCompound).map(WireElement::new).collect(ImmutableList.toImmutableList());
        color = DyeColor.byId(nbt.getInt("color"));

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
        public final Direction emitterSide;

        public WireElement(BlockPos blockPos, EnumWirePart wirePart) {
            this.type = Type.WIRE_PART;
            this.blockPos = blockPos;
            this.wirePart = wirePart;
            this.emitterSide = null;
        }

        public WireElement(BlockPos blockPos, Direction emitterSide) {
            this.type = Type.EMITTER_SIDE;
            this.blockPos = blockPos;
            this.wirePart = null;
            this.emitterSide = emitterSide;
        }

        public WireElement(PacketByteBuf buf) {
            type = Type.values()[buf.readInt()];
            blockPos = buf.readBlockPos();
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[buf.readInt()];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = Direction.byId(buf.readInt());
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public WireElement(NbtCompound nbt) {
            type = Type.values()[nbt.getInt("type")];
            blockPos = NBTUtilBC.readBlockPos(nbt.get("blockPos"));
            if (blockPos == null) {
                // Oh dear. We probably can't recover from this properly
                throw new NullPointerException("Cannot read this Wire Systems from NBT!");
            }
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[nbt.getInt("wirePart")];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = Direction.byId(nbt.getInt("emitterSide"));
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public void toBytes(PacketByteBuf buf) {
            buf.writeInt(type.ordinal());
            buf.writeBlockPos(blockPos);
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                buf.writeInt(wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                buf.writeInt(emitterSide.getId());
            }
        }

        public NbtCompound writeToNBT() {
            NbtCompound nbt = new NbtCompound();
            nbt.putInt("type", type.ordinal());
            nbt.put("blockPos", NBTUtilBC.writeBlockPos(blockPos));
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                nbt.putInt("wirePart", wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                nbt.putInt("emitterSide", emitterSide.getId());
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
