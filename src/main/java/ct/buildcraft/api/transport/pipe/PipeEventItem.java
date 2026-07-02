package ct.buildcraft.api.transport.pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PipeEventItem extends PipeEvent {

    public final IFlowItems flow;

    protected PipeEventItem(IPipeHolder holder, IFlowItems flow) {
        super(holder);
        this.flow = flow;
    }

    /** @deprecated Because cancellation is going to be removed (at some point in the future) */
    @Deprecated
    protected PipeEventItem(boolean canBeCancelled, IPipeHolder holder, IFlowItems flow) {
        super(canBeCancelled, holder);
        this.flow = flow;
    }

    // ################
    //
    // Misc events
    //
    // ################

    /** Fires whenever item insertion is attempted. The item might have been extracted by the pipe behaviour, inserted
     * by another pipe or even a different kind of tile. Note that you have no way of telling what caused this event. */
    public static class TryInsert extends PipeEventItem {
        public final DyeColor colour;
        public final Direction from;
        /** The itemstack that is attempting to be inserted. NEVER CHANGE THIS! */
        @Nonnull
        public final ItemStack attempting;
        /** The count of items that are being accepted into this pipe. Starts off at the stack count of
         * {@link #attempting} */
        public int accepted;

        public TryInsert(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from,
            @Nonnull ItemStack attempting) {
            super(true, holder, flow);
            this.colour = colour;
            this.from = from;
            this.attempting = attempting;
            this.accepted = attempting.getCount();
        }

        /** Stops the item from being accepted. */
        @Override
        public void cancel() {
            super.cancel();
        }
    }

    public static abstract class ReachDest extends PipeEventItem {
        public DyeColor colour;
        @Nonnull
        private ItemStack stack;

        public ReachDest(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack) {
            super(holder, flow);
            this.colour = colour;
            this.stack = stack;
        }

        @Nonnull
        public ItemStack getStack() {
            return this.stack;
        }

        public void setStack(ItemStack stack) {
            if (stack == null) {
                throw new NullPointerException("stack");
            } else {
                this.stack = stack;
            }
        }
    }

    /** Fired after {@link TryInsert} (if some items were allowed in) to modify the incoming itemstack or its colour. */
    public static class OnInsert extends ReachDest {
        public final Direction from;

        public OnInsert(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack,
            Direction from) {
            super(holder, flow, colour, stack);
            this.from = from;
        }
    }

    /** Fired whenever an item reaches the centre of a pipe. Note that you *can* change the itemstack or the colour. */
    public static class ReachCenter extends ReachDest {
        public final Direction from;

        public ReachCenter(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack,
            Direction from) {
            super(holder, flow, colour, stack);
            this.from = from;
        }
    }

    /** Fired whenever an item reaches the end of a pipe. Note that you *can* change the itemstack or the colour. */
    public static class ReachEnd extends ReachDest {
        public final Direction to;

        public ReachEnd(IPipeHolder holder, IFlowItems flow, DyeColor colour, @Nonnull ItemStack stack,
            Direction to) {
            super(holder, flow, colour, stack);
            this.to = to;
        }
    }

    /** Fired whenever the item exists from this pipe in a normal manner (inserted into another pipe or inventory, this
     * does not overlap with {@link Drop}.) NOTE: This event is fired *after* the item has been ejected, not before, so
     * modifying {@link Ejected#inserted} won't do anything. You are free to modify {@link Ejected#excess} however. */
    public static abstract class Ejected extends PipeEventItem {
        /** The stack that has been inserted */
        public final ItemStack inserted;

        /** The stack that was refused by the inventory or pipe. */
        @Nonnull
        private ItemStack excess;

        /** The side that the item has been ejected to. */
        public final Direction to;

        protected Ejected(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to) {
            super(holder, flow);
            this.inserted = inserted;
            this.excess = excess;
            this.to = to;
        }

        @Nonnull
        public ItemStack getExcess() {
            return this.excess;
        }

        public void setExcess(ItemStack stack) {
            if (stack == null) {
                throw new NullPointerException("stack");
            } else {
                this.excess = stack;
            }
        }

        /** Fired when an item is injected into a pipe. (Refer to {@link Ejected} for more details) */
        public static class IntoPipe extends Ejected {
            public final IFlowItems otherPipe;

            public IntoPipe(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to,
                IFlowItems otherPipe) {
                super(holder, flow, inserted, excess, to);
                this.otherPipe = otherPipe;
            }
        }

        /** Fired when an item is injected into a tile entity. (Refer to {@link Ejected} for more details) */
        public static class IntoTile extends Ejected {
            public final BlockEntity tile;

            public IntoTile(IPipeHolder holder, IFlowItems flow, ItemStack inserted, ItemStack excess, Direction to,
                BlockEntity tile) {
                super(holder, flow, inserted, excess, to);
                this.tile = tile;
            }
        }
    }

    // ############################
    //
    // Destination related
    //
    // ############################

    /** Fired after {@link ReachCenter} to determine what sides are the items NOT allowed to go to, and the order of
     * priority for the allowed sides. */
    public static class SideCheck extends PipeEventItem {
        public final DyeColor colour;
        public final Direction from;
        @Nonnull
        public final ItemStack stack;

        /** The priorities of each side. Stored inversely to the values given, so a higher priority will have a lower
         * value than a lower priority. */
        private final int[] priority = new int[6];
        private final EnumSet<Direction> allowed = EnumSet.allOf(Direction.class);

        public SideCheck(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from,
            @Nonnull ItemStack stack) {
            super(holder, flow);
            this.colour = colour;
            this.from = from;
            this.stack = stack;
        }

        /** Checks to see if a side if allowed. Note that this may return true even though a later handler might
         * disallow a side, so you should only use this to skip checking a side (for example a diamond pipe might not
         * check the filters for a specific side if its already been disallowed) */
        public boolean isAllowed(Direction side) {
            return allowed.contains(side);
        }

        /** Disallows the specific side(s) from being a destination for the item. If no sides are allowed, then
         * {@link TryBounce} will be fired to test if the item can bounce back. */
        public void disallow(Direction... sides) {
            for (Direction side : sides) {
                allowed.remove(side);
            }
        }

        public void disallowAll(Collection<Direction> sides) {
            allowed.removeAll(sides);
        }

        public void disallowAllExcept(Direction... sides) {
            allowed.retainAll(Lists.newArrayList(sides));
        }

        public void disallowAll() {
            allowed.clear();
        }

        public void increasePriority(Direction side) {
            increasePriority(side, 1);
        }

        public void increasePriority(Direction side, int by) {
            priority[side.ordinal()] -= by;
        }

        public void decreasePriority(Direction side) {
            decreasePriority(side, 1);
        }

        public void decreasePriority(Direction side, int by) {
            increasePriority(side, -by);
        }

        public List<EnumSet<Direction>> getOrder() {
            // Skip the calculations if the size is simple
            switch (allowed.size()) {
                case 0:
                    return ImmutableList.of();
                case 1:
                    return ImmutableList.of(allowed);
                default:
            }
            priority_search: {
                int val = priority[0];
                for (int i = 1; i < priority.length; i++) {
                    if (priority[i] != val) {
                        break priority_search;
                    }
                }
                // No need to work out the order when all destinations have the same priority
                return ImmutableList.of(allowed);
            }

            int[] ordered = Arrays.copyOf(priority, 6);
            Arrays.sort(ordered);
            int last = 0;
            List<EnumSet<Direction>> list = Lists.newArrayList();
            for (int i = 0; i < 6; i++) {
                int current = ordered[i];
                if (i != 0 && current == last) {
                    continue;
                }
                last = current;
                EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
                for (Direction face : Direction.values()) {
                    if (allowed.contains(face)) {
                        if (priority[face.ordinal()] == current) {
                            set.add(face);
                        }
                    }
                }
                if (set.size() > 0) {
                    list.add(set);
                }
            }
            return list;
        }
    }

    /** Fired after {@link SideCheck} (if all sides were disallowed) to see if the item is allowed to bounce back to
     * where it was inserted. */
    public static class TryBounce extends PipeEventItem {
        public final DyeColor colour;
        public final Direction from;
        @Nonnull
        public final ItemStack stack;
        public boolean canBounce = false;

        public TryBounce(IPipeHolder holder, IFlowItems flow, DyeColor colour, Direction from,
            @Nonnull ItemStack stack) {
            super(holder, flow);
            this.colour = colour;
            this.from = from;
            this.stack = stack;
        }
    }

    public static class Drop extends PipeEventItem {
        private final ItemEntity entity;

        public Drop(IPipeHolder holder, IFlowItems flow, ItemEntity entity) {
            super(holder, flow);
            this.entity = entity;
        }

        @Nonnull
        public ItemStack getStack() {
            ItemStack item = entity.getItem();
            return item.isEmpty() ? ItemStack.EMPTY : item;
        }

        public void setStack(ItemStack stack) {
            if (stack == null) {
                throw new NullPointerException("stack");
            } else if (stack.isEmpty()) {
                entity.setItem(ItemStack.EMPTY);
            } else {
                entity.setItem(stack);
            }
        }

        public ItemEntity getEntity() {
            return this.entity;
        }
    }

    /** Base class for {@link Split} and {@link FindDest}. Do not listen to this directly! */
    public static abstract class OrderedEvent extends PipeEventItem {
        public final List<EnumSet<Direction>> orderedDestinations;

        public OrderedEvent(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> orderedDestinations) {
            super(holder, flow);
            this.orderedDestinations = orderedDestinations;
        }

        public EnumSet<Direction> getAllPossibleDestinations() {
            EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
            for (EnumSet<Direction> e : orderedDestinations) {
                set.addAll(e);
            }
            return set;
        }

        public ImmutableList<Direction> generateRandomOrder() {
            ImmutableList.Builder<Direction> builder = ImmutableList.builder();
            for (EnumSet<Direction> set : orderedDestinations) {
                List<Direction> faces = new ArrayList<>(set);
                Collections.shuffle(faces);
                builder.addAll(faces);
            }
            return builder.build();
        }
    }

    /** Fired after {@link SideCheck} (if at least one valid side was found) or after {@link TryBounce} if no valid
     * sides were detected, but it was allowed to bounce back. This event is for splitting up (or modifying) the input
     * itemstack. This is most helpful for implementing full round-robin behaviour, or diamond-pipe based splitting. If
     * you need to generate a random facing for each one then use {@link OrderedEvent#generateRandomOrder()}. */
    public static class Split extends OrderedEvent {
        public final List<ItemEntry> items = new ArrayList<>();

        public Split(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> order, ItemEntry toSplit) {
            super(holder, flow, order);
            items.add(toSplit);
        }
    }

    /** Fired after {@link Split}. This event is for assigning a destination to each {@link ItemEntry} in
     * {@link Split#items}. If you need to generate a random facing for each one then use
     * {@link OrderedEvent#generateRandomOrder()}. */
    public static class FindDest extends OrderedEvent {
        public final ImmutableList<ItemEntry> items;

        public FindDest(IPipeHolder holder, IFlowItems flow, List<EnumSet<Direction>> orderedDestinations,
            ImmutableList<ItemEntry> items) {
            super(holder, flow, orderedDestinations);
            this.items = items;
        }
    }

    /** Fired after {@link FindDest}. */
    public static class ModifySpeed extends PipeEventItem {
        public final ItemEntry item;
        public final double currentSpeed;
        public double targetSpeed = 0;
        public double maxSpeedChange = 0;

        public ModifySpeed(IPipeHolder holder, IFlowItems flow, ItemEntry item, double initSpeed) {
            super(holder, flow);
            this.item = item;
            currentSpeed = initSpeed;
        }

        public void modifyTo(double target, double maxDelta) {
            targetSpeed = target;
            maxSpeedChange = maxDelta;
        }
    }

    /** Mostly immutable holding class for item stacks. */
    public static class ItemEntry {
        public final DyeColor colour;
        @Nonnull
        public final ItemStack stack;
        public final Direction from;
        /** The list of the destinations to try, in order. */
        @Nullable
        public List<Direction> to;

        public ItemEntry(DyeColor colour, @Nonnull ItemStack stack, Direction from) {
            this.colour = colour;
            this.stack = stack;
            this.from = from;
        }
    }
}
