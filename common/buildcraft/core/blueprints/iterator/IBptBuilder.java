package buildcraft.core.blueprints.iterator;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.builders.BuildingSlot;

public interface IBptBuilder extends INBTLoadable_BC8<IBptBuilder> {
    /** Iterates this builder to build the current {@link BlueprintBase}, taking up to the given time in microseconds to
     * do so. This will always iterate the builder at least once though, no matter how small the time gap is.
     * 
     * @param us The number of microseconds to wait. Most of the time this will be changed to a number of calls to the
     *            internalInit() rather than actually checking the {@link System#nanoTime()} after every iteration.
     * 
     * @return An double stating approximately how much work is left to do to init this builder, between 0 and 1. 1
     *         indicates all work left to do, 0 indicates work will probably finish shortly. Values less than 0 indicate
     *         that this init sequence has been finished. */
    double iterateInit(long us);

    /** Rechecks the block at the given position to see if it needs changing. This should be given relative to the
     * origin of the world, not the blueprint.
     * <p>
     * Useful if you receive a block break/place event and you want to rebuild it. Most of the time this will do what
     * {@link #iterateInit(long)} does, but only for blocks at the specific location. */
    void recheckBlock(BlockPos pos);

    /** @return True if this builder has completed its initialisation sequence, and can safely move on to building. */
    boolean hasInit();

    /** @return The volume that this builder affects. NEVER CHANGE THE RESULTING BOX! */
    Box operatingBox();

    /** Ticks this builder, removing all {@link BuildingSlot} that have finished building. */
    void tick();

    /** Gets the next buildable slot. The slot may be null if it has not finished initialising yet (For example
     * everything in-world correlates to what it is meant to be) or if {@link #hasFinishedBuilding()} returns true.
     * 
     * @param closestToHint The block that (Ideally) would be returned by this method, however the closer the block that
     *            needs to built is the higher a chance it has of getting called. */
    BuildingSlot getNextSlot(BlockPos closestToHint);

    /** Reserves a particular slot, indicating that it should not be returned by {@link #getNextSlot(BlockPos)}. */
    void reserveSlot(BlockPos toReserve);

    /** Stops reserving a particular slot. The */
    void unreserveSlot(BlockPos used);

    /** @return The number of slots that have been reserved, but not built. */
    int reservedSlotCount();

    /** @return True if this has completed all work that this can do. If you want to reset the builder then you should
     *         create a new instance. */
    boolean hasFinishedBuilding();

    @Override
    /** Note that this will NOT write the blueprint to NBT, as it should be given at creation time. */
    NBTTagCompound writeToNBT();

    @Override
    IBptBuilder readFromNBT(NBTBase nbt);
}
