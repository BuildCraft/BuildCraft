/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class WireSystem {
    public final ImmutableList<WireElement> elements;
    public final DyeColor color;

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
        for (Direction face : Direction.VALUES) {
            WireNode oNode = node.offset(face);
            // equality check is fine here -- WireNode.offset returns the same blockpos (identity wise) if its the same
            if (oNode.pos == node.pos || canWireConnect(holder, face)) {
                list.add(new WireElement(oNode.pos, oNode.part));
            }
        }
        return list;
    }

    public static List<WireElement> getConnectedElementsOfElement(Level world, WireElement element) {
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

        DyeColor tempColor = null;
        ImmutableList.Builder<WireElement> elementBuilder = ImmutableList.builder();

        while (!queue.isEmpty()) {
            WireElement element = queue.remove();

            if (!walked.contains(element)) {
                if (!holdersCache.containsKey(element.blockPos)) {
                    BlockEntity tile = wireSystems.world.getBlockEntity(element.blockPos);
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
                            queue.addAll(getConnectedElementsOfElement(wireSystems.world, element));
                            Arrays.stream(Direction.VALUES).forEach(side -> queue.add(new WireElement(element.blockPos, side)));
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

    public boolean isPlayerWatching(ServerPlayer player) {
        if (player.level() instanceof ServerLevel world) {
//            return getChunkPosesAsStream().map(chunkPos -> world.getPlayerChunkMap().getEntry(chunkPos.x, chunkPos.z))
//                    .anyMatch(playerChunkMapEntry -> playerChunkMapEntry != null && playerChunkMapEntry.containsPlayer(player));
            return getChunkPosesAsStream().map(
                            chunkPos -> world.getChunkSource().chunkMap.getPlayers(chunkPos, /*pBoundaryOnly*/ false)
                    )
                    .anyMatch(
                            players -> !players.isEmpty()
                    );
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

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag elementsList = new ListTag();
        elements.stream().map(WireElement::writeToNBT).forEach(elementsList::add);
        nbt.put("elements", elementsList);
        nbt.putInt("color", color.getId());
        return nbt;
    }

    public WireSystem(CompoundTag nbt) {
        ListTag elementsList = nbt.getList("elements", Tag.TAG_COMPOUND);
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

        public WireElement(FriendlyByteBuf buf) {
            type = Type.values()[buf.readInt()];
            blockPos = MessageUtil.readBlockPos(buf);
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[buf.readInt()];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = Direction.from3DDataValue(buf.readInt());
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public WireElement(CompoundTag nbt) {
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
                emitterSide = Direction.from3DDataValue(nbt.getInt("emitterSide"));
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(type.ordinal());
            MessageUtil.writeBlockPos(buf, blockPos);
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                buf.writeInt(wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                buf.writeInt(emitterSide.ordinal());
            }
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("type", type.ordinal());
            nbt.put("blockPos", NBTUtilBC.writeBlockPos(blockPos));
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                nbt.putInt("wirePart", wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                nbt.putInt("emitterSide", emitterSide.ordinal());
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
