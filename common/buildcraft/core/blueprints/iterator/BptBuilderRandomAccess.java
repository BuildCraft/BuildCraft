package buildcraft.core.blueprints.iterator;

import java.util.Map;

import com.google.common.collect.Multimap;

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
    /** A map of positions to requirements. This contains all unreserved unbuilt unseen requirements. The key is bound
     * to the blueprint (SO this will be values from (0,0,0) to the blueprints size) */
    private Map<BlockPos, BuildRequirement> cleanChanges;
    /** A map of all positions that have been returned by {@link #getNextSlot(BlockPos)}. */
    private Map<BlockPos, BuildRequirement> returnedChanges;

    /** A map of all positions that have been reserved by {@link #reserveSlot(BuildingSlot)} */
    private Map<BlockPos, BuildRequirement> reservedRequirements;
    private Multimap<BlockPos, BuildingSlotEntity> entities;

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
        SchematicBlockBase block = blueprint.get(blueprintPos);
        if (block != null) {
            if (block.isAlreadyBuilt(context, blueprintPos)) return;
            if (changesNeeded.containsKey(pos)) return;
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
        }
    }

    @Override
    public double iterateInit(long us) {
        if (hasInitBlocks && hasInitEntities) return -1;
        if (initPos == null || iterator == null) {
            initPos = worldOffset;
            iterator = new BoxIterator(worldOffset, blueprint.size.subtract(Utils.POS_ONE), EnumAxisOrder.XZY.defaultOrder);
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
        // TODO Auto-generated method stub
    }

    @Override
    public BuildingSlot getNextSlot(BlockPos closestToHint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reserveSlot(BlockPos pos) {
        // TODO Auto-generated method stub
    }

    @Override
    public void unreserveSlot(BlockPos used) {
        // TODO Auto-generated method stub

    }

    @Override
    public int reservedSlotCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasFinishedBuilding() {
        // TODO Auto-generated method stub
        return false;
    }
}
