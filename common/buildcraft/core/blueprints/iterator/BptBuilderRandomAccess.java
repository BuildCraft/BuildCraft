package buildcraft.core.blueprints.iterator;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.builders.BuildingSlot;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.BuildingSlotEntity;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.Utils.BoxIterator;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;

public class BptBuilderRandomAccess implements IBptBuilder {
    private final BlueprintBase blueprint;
    private final IBuilderContext context;
    private final BlockPos worldOffset;
    private final Box worldOperatingBox;

    // Init fields
    private boolean hasInitBlocks = false, hasInitEntities = false;
    /** The next position to search within the blueprint base. */
    private BlockPos initPos;
    /** The iterator that tracks where we are while searching the blueprint. */
    private BoxIterator iterator;

    // Building fields
    /** A map of positions to requirements. This contains all unreserved unbuilt requirements. */
    private Map<BlockPos, BuildRequirement> neededRequirements = Maps.newHashMap();
    /** A map of all positions that have been reserved by {@link #reserveSlot(BuildingSlot)} */
    private Map<BlockPos, BuildRequirement> reservedRequirements = Maps.newHashMap();
    /** A map of all the positions that require post-processing. This map is built up as reservedRequirements is
     * cleared. */
    private Map<BlockPos, BuildingSlotPostProcess> postProcessing = Maps.newHashMap();
    private Multimap<BlockPos, BuildingSlotEntity> entities = HashMultimap.create();

    /** @param blueprint The blueprint to build.
     * @param world The world to build it in
     * @param worldOffset The minimum position to build this blueprint from. This should ALWAYS be the MINUMUM of the
     *            blueprint, and is INSIDE of the blueprint's volume */
    public BptBuilderRandomAccess(BlueprintBase blueprint, BlockPos worldOffset, IBuilderContext context) {
        this.blueprint = blueprint;
        this.worldOffset = worldOffset;
        this.context = context;
        worldOperatingBox = new Box(worldOffset, worldOffset.add(blueprint.size.subtract(Utils.POS_ONE)));
    }

    @Override
    public BptBuilderRandomAccess readFromNBT(NBTBase nbt) {
        BptBuilderRandomAccess builder = new BptBuilderRandomAccess(blueprint, worldOffset, context);
        return builder;
    }

    @Override
    public NBTTagCompound writeToNBT() {
        return null;
    }

    @Override
    public Box operatingBox() {
        return worldOperatingBox;
    }

    @Override
    public void recheckBlock(BlockPos pos) {
        if (!worldOperatingBox.contains(pos)) return;
        BlockPos blueprintPos = pos.subtract(worldOffset);
        SchematicBlockBase schematic = blueprint.get(blueprintPos);
        if (schematic != null) {
            if (schematic.isAlreadyBuilt(context, blueprintPos)) return;
            if (reservedRequirements.containsKey(pos)) return;
            if (neededRequirements.containsKey(pos)) {
                BuildRequirement req = neededRequirements.get(pos);
                req.updateFor(schematic, context, pos);
            } else {
                BuildRequirement req = new BuildRequirement();
                req.updateFor(schematic, context, pos);
                neededRequirements.put(pos, req);
            }
        }
    }

    /** Runs a single init cycle. */
    protected void internalInit() {
        if (!hasInitBlocks) {
            if (!iterator.hasNext()) {
                hasInitBlocks = true;
            } else {
                BlockPos next = iterator.next();
                recheckBlock(next);
            }
        } else if (!hasInitEntities) {
            // TODO
            hasInitEntities = true;// TEMP!
        }
    }

    @Override
    public double iterateInit(long us) {
        if (hasInit()) return -1;
        if (initPos == null || iterator == null) {
            initPos = worldOffset;
            iterator = new BoxIterator(worldOffset, worldOffset.add(blueprint.size.subtract(Utils.POS_ONE)), EnumAxisOrder.XZY.defaultOrder);
        }
        long start = System.nanoTime() / 1000;
        do {
            /* Just iterate 64 times to save perf a bit- nanoTime() is apparently not too cheap to do so we won't bother
             * checking after every iteration. */
            for (int i = 0; i < 64; i++) {
                internalInit();
            }
        } while ((System.nanoTime() / 1000) - start < us);
        return 1;
    }

    @Override
    public boolean hasInit() {
        return hasInitBlocks && hasInitEntities;
    }

    @Override
    public void tick() {
        Set<BlockPos> finished = Sets.newHashSet();
        for (Entry<BlockPos, BuildRequirement> entry : reservedRequirements.entrySet()) {
            BuildRequirement req = entry.getValue();
            req.tick();
            if (req.hasBuilt()) {
                finished.add(entry.getKey());
            }
        }
        for (BlockPos pos : finished) {
            BuildRequirement req = reservedRequirements.remove(pos);
            postProcessing.put(pos, req.forPostProcess());
        }
    }

    @Override
    public BuildingSlot getNextSlot(BlockPos closestToHint) {
        if (!neededRequirements.isEmpty()) {
            BlockPos closest = Utils.findClosestTo(neededRequirements.keySet(), closestToHint);
            BuildRequirement req = neededRequirements.get(closest);
            return req.getSlot();
        }
        if (reservedRequirements.isEmpty()) {
            if (!postProcessing.isEmpty()) {
                BlockPos closest = Utils.findClosestTo(postProcessing.keySet(), closestToHint);
                BuildingSlotPostProcess post = postProcessing.remove(closest);
                post.writeToWorld(context);
                return post;
            } else {
                BlockPos closest = Utils.findClosestTo(entities.keySet(), closestToHint);
                Collection<BuildingSlotEntity> lst = entities.get(closest);
                if (lst.size() != 0) return lst.iterator().next();
            }
        }
        return null;
    }

    @Override
    public void reserveSlot(BuildingSlot slot) {
        if (slot instanceof BuildingSlotBlock) {
            BlockPos pos = ((BuildingSlotBlock) slot).pos;
            if (reservedRequirements.containsValue(pos)) return;
            if (!neededRequirements.containsKey(pos)) return;
            BuildRequirement req = neededRequirements.remove(pos);
            reservedRequirements.put(pos, req);
        } else {/* TODO: Entities! */}
    }

    @Override
    public void unreserveSlot(BuildingSlot slot) {
        if (slot instanceof BuildingSlotBlock) {
            BlockPos used = ((BuildingSlotBlock) slot).pos;
            if (neededRequirements.containsKey(used)) return;
            if (!reservedRequirements.containsKey(used)) return;
            BuildRequirement req = reservedRequirements.remove(used);
            neededRequirements.put(used, req);
        }
    }

    @Override
    public int reservedSlotCount() {
        return reservedRequirements.size();
    }

    @Override
    public boolean hasFinishedBuilding() {
        return hasInit() && neededRequirements.isEmpty() && reservedRequirements.isEmpty() && postProcessing.isEmpty() && entities.isEmpty();
    }
}
