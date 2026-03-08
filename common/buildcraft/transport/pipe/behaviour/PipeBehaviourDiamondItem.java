/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventItem.ItemEntry;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PipeBehaviourDiamondItem extends PipeBehaviourDiamond {

    public PipeBehaviourDiamondItem(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamondItem(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck sideCheck) {
        ItemStack toCompare = sideCheck.stack;
        for (Direction face : Direction.values()) {
            if (sideCheck.isAllowed(face) && pipe.isConnected(face)) {
                int offset = FILTERS_PER_SIDE * face.ordinal();
                boolean sideAllowed = false;
                boolean foundItem = false;
                for (int i = 0; i < FILTERS_PER_SIDE; i++) {
                    ItemStack compareTo = filters.getStackInSlot(offset + i);
                    if (compareTo.isEmpty()) continue;
                    foundItem = true;
                    if (StackUtil.isMatchingItemOrList(compareTo, toCompare)) {
                        sideAllowed = true;
                        break;
                    }
                }
                if (foundItem) {
                    if (sideAllowed) {
                        sideCheck.increasePriority(face, 12);
                    } else {
                        sideCheck.disallow(face);
                    }
                }
            }
        }
    }

    @PipeEventHandler
    public void split(PipeEventItem.Split split) {
        Direction[] allSides = split.getAllPossibleDestinations().toArray(new Direction[0]);

        if (allSides.length == 0 || allSides.length == 1) {
            // Nothing to split
            return;
        }

        ItemEntry[] items = split.items.toArray(new ItemEntry[0]);
        split.items.clear();
        // Note that the order doesn't matter
        for (ItemEntry item : items) {
            int[] countPerSide = new int[allSides.length];
            int totalCount = 0;
            for (int s = 0; s < allSides.length; s++) {
                int offset = FILTERS_PER_SIDE * allSides[s].ordinal();
                for (int i = 0; i < FILTERS_PER_SIDE; i++) {
                    ItemStack compareTo = filters.getStackInSlot(offset + i);
                    if (compareTo.isEmpty()) continue;
                    if (StackUtil.isMatchingItemOrList(compareTo, item.stack)) {
                        int count = compareTo.getCount();
                        totalCount += count;
                        countPerSide[s] += count;
                    }
                }
            }
            if (totalCount == 0) {
                totalCount = allSides.length;
                Arrays.fill(countPerSide, 1);
            } else {
                int hcf = countPerSide[0];
                for (int c : countPerSide) {
                    hcf = MathUtil.findHighestCommonFactor(hcf, c);
                }
                if (hcf != 1) {
                    totalCount /= hcf;
                    for (int i = 0; i < countPerSide.length; i++) {
                        countPerSide[i] /= hcf;
                    }
                }
            }
            /* If the stack count is divisible by the possible directions then we can just split the stack up evenly -
             * making the distribution perfect. */
            ItemEntry[] entries = new ItemEntry[allSides.length];
            ItemStack toSplit = item.stack;
            if (toSplit.getCount() >= totalCount) {
                int leftOver = toSplit.getCount() % totalCount;
                int multiples = (toSplit.getCount() - leftOver) / totalCount;
                for (int s = 0; s < allSides.length; s++) {
                    ItemStack toSide = toSplit.copy();
                    toSide.setCount(countPerSide[s] * multiples);
                    entries[s] = new ItemEntry(item.colour, toSide, item.from);

                    List<Direction> dests = new ArrayList<>(1);
                    dests.add(allSides[s]);
                    entries[s].to = dests;
                }
                toSplit.setCount(leftOver);
            }
            if (!toSplit.isEmpty()) {
                int[] randLookup = new int[totalCount];
                int j = 0;
                for (int s = 0; s < allSides.length; s++) {
                    int len = countPerSide[s];
                    Arrays.fill(randLookup, j, j + len, s);
                    j += len;
                }

                while (!toSplit.isEmpty()) {
                    // Pick a random number between 0 and total count.
                    int rand = split.holder.getPipeWorld().random.nextInt(totalCount);
                    int face = randLookup[rand];
                    if (entries[face] == null) {
                        ItemStack stack = toSplit.copy();
                        stack.setCount(1);
                        ItemEntry entry = new ItemEntry(item.colour, stack, item.from);
                        List<Direction> dests = entry.to = new ArrayList<>(1);
                        dests.add(allSides[face]);
                        entries[face] = entry;
                    } else {
                        entries[face].stack.grow(1);
                    }
                    toSplit.shrink(1);
                }
            }
            for (int s = 0; s < allSides.length; s++) {
                ItemEntry entry = entries[s];
                if (entry == null) {
                    continue;
                }
                split.items.add(entry);
            }
        }
    }
}
