/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multiset;

import net.minecraft.world.WorldSettings;

public class BuildingSlotMapIterator {
    private static final int MAX_PER_ITEM = 80;
    private final Map<BuilderItemMetaPair, List<BuildingSlotBlock>> slots;
    private final Set<BuilderItemMetaPair> availablePairs = new HashSet<BuilderItemMetaPair>();
    private final Multiset<Integer> buildStageOccurences;
    private final boolean isCreative;
    private Iterator<BuilderItemMetaPair> impIterator;
    private BuilderItemMetaPair pair;
    private List<BuildingSlotBlock> current;
    private int position, returnsThisCurrent;

    public BuildingSlotMapIterator(Map<BuilderItemMetaPair, List<BuildingSlotBlock>> slots, TileAbstractBuilder builder,
            Multiset<Integer> buildStageOccurences) {
        this.slots = slots;
        this.impIterator = slots.keySet().iterator();
        this.buildStageOccurences = buildStageOccurences;
        this.isCreative = builder.getWorld().getWorldInfo().getGameType() == WorldSettings.GameType.CREATIVE;

        // Generate available pairs
        availablePairs.add(new BuilderItemMetaPair(null));
        for (int i = 0; i < builder.getSizeInventory(); i++) {
            availablePairs.add(new BuilderItemMetaPair(builder.getStackInSlot(i)));
        }

        findNewCurrent();
    }

    public void skipList() {
        findNewCurrent();
    }

    private void findNewCurrent() {
        position = -1;
        returnsThisCurrent = 0;
        while (impIterator.hasNext()) {
            pair = impIterator.next();
            if (isCreative || availablePairs.contains(pair)) {
                current = slots.get(pair);
                return;
            }
        }
        current = null;
    }

    public BuildingSlotBlock next() {
        while (current != null) {
            position++;
            while (returnsThisCurrent < MAX_PER_ITEM && position < current.size()) {
                BuildingSlotBlock b = current.get(position);
                if (b != null) {
                    returnsThisCurrent++;
                    return b;
                }
                position++;
            }
            findNewCurrent();
        }
        return null;
    }

    public void remove() {
        buildStageOccurences.remove(current.get(position).buildStage);
        current.set(position, null);
    }

    public void reset() {
        this.impIterator = slots.keySet().iterator();
        this.pair = null;
        this.current = null;
        findNewCurrent();
    }
}
