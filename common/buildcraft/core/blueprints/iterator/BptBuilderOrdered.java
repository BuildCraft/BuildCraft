package buildcraft.core.blueprints.iterator;

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
import buildcraft.core.builders.BuildingSlotEntity;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.Utils.AxisOrder;
import buildcraft.core.lib.utils.Utils.BoxIterator;
import buildcraft.core.lib.utils.Utils.EnumAxisOrder;

/** Will build the blueprint in a given order, never deviating from it. Simple and quick, ideal for a single building
 * tile which stays in exactly the same place. */
public class BptBuilderOrdered implements IBptBuilder {
    private final BlueprintBase blueprint;
    private final IBuilderContext context;
    private final BlockPos worldOffset;
    private final Box worldOperatingBox;
    /** The order that we will go through. This is only used for {@link #internalInit()} stuffs though. */
    private final AxisOrder order;

    // Init fields
    private boolean hasInitBlocks = false, hasInitEntities = false;
    /** The next position to search within the blueprint base. */
    private BlockPos initPos;
    /** The iterator that tracks where we are while searching the blueprint. */
    private BoxIterator iterator;

    // Building fields
    private BuildRequirement[][][] clearRequirements;
    private Map<BlockPos, BuildRequirement> reservedRequirements = Maps.newHashMap();
    private Map<BlockPos, BuildingSlotPostProcess> postProcessing = Maps.newHashMap();
    private Multimap<BlockPos, BuildingSlotEntity> entities = HashMultimap.create();

    // State
    private boolean hasFinishedBuilding = false;

    public BptBuilderOrdered(BlueprintBase blueprint, BlockPos worldOffset, IBuilderContext context, AxisOrder order) {
        this.blueprint = blueprint;
        this.worldOffset = worldOffset;
        this.context = context;
        this.order = order;
        worldOperatingBox = new Box(worldOffset, worldOffset.add(blueprint.size.subtract(Utils.POS_ONE)));
    }

    @Override
    public BptBuilderOrdered readFromNBT(NBTBase nbt) {
        BptBuilderOrdered builder = new BptBuilderOrdered(blueprint, worldOffset, context, order);
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
            if (reservedRequirements.get(pos) != null) return;
            if (clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()] != null) {
                BuildRequirement req = clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()];
                req.updateFor(schematic, context, pos);
            } else {
                BuildRequirement req = new BuildRequirement();
                req.updateFor(schematic, context, pos);
                clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()] = req;
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
        if (hasInitBlocks && hasInitEntities) return -1;
        if (initPos == null || iterator == null) {
            initPos = worldOffset;
            iterator = new BoxIterator(worldOffset, blueprint.size.subtract(Utils.POS_ONE), EnumAxisOrder.XZY.defaultOrder);
            clearRequirements = new BuildRequirement[blueprint.size.getX()][blueprint.size.getY()][blueprint.size.getZ()];
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
        if (!iterator.hasNext()) return null;
        while (iterator.hasNext()) {
            BlockPos next = iterator.next();
            BuildRequirement req = clearRequirements[next.getX()][next.getY()][next.getZ()];
            if (req != null) {
                return req.getSlot();
            }
        }
        return null;
    }

    @Override
    public void reserveSlot(BuildingSlot toReserve) {
        // TODO!
        {
            // BlockPos blueprintPos = toReserve.subtract(worldOffset);
            // if (reservedRequirements.containsKey(toReserve)) return;
            // BuildRequirement req = clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()];
            // if (req == null) return;
            // clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()] = null;
            // reservedRequirements.put(toReserve, req);
        }
    }

    @Override
    public void unreserveSlot(BuildingSlot used) {
        // TODO!
        {
            // BlockPos blueprintPos = used.subtract(worldOffset);
            // if (clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()] != null) return;
            // BuildRequirement req = reservedRequirements.get(used);
            // if (req == null) return;
            // reservedRequirements.remove(used);
            // clearRequirements[blueprintPos.getX()][blueprintPos.getY()][blueprintPos.getZ()] = req;
            // // Reset the iterator so we redo the block that was unreserved
            // iterator.skipTo(blueprintPos);
        }
    }

    @Override
    public int reservedSlotCount() {
        return reservedRequirements.size();
    }

    @Override
    public boolean hasFinishedBuilding() {
        // TODO Auto-generated method stub
        return false;
    }
}
