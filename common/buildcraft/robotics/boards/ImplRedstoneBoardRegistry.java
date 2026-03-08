/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.robotics.BCRoboticsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {
    private static class BoardFactory {
        public RedstoneBoardNBT<?> boardNBT;
        public long energyCost;
    }

    private HashMap<ResourceLocation, BoardFactory> boards = new HashMap<ResourceLocation, BoardFactory>();
    private Map<RedstoneBoardNBT<?>, RegistryObject<? extends Item>> boardNBTItemMap = new HashMap<RedstoneBoardNBT<?>, RegistryObject<? extends Item>>();
    private RedstoneBoardRobotNBT emptyRobotBoardNBT;

    @Override
    public void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, long energyCost) {
        if (BCRoboticsConfig.blacklistedRobots.contains(redstoneBoardNBT.getID())) {
            return;
        }

        BoardFactory factory = new BoardFactory();
        factory.boardNBT = redstoneBoardNBT;
        factory.energyCost = energyCost;

        boards.put(redstoneBoardNBT.getID(), factory);
    }

    @Override
    public void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT) {
        emptyRobotBoardNBT = redstoneBoardNBT;
    }

    @Override
    public RedstoneBoardRobotNBT getEmptyRobotBoard() {
        return emptyRobotBoardNBT;
    }

    @Override
    public RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag nbt) {
        // return getRedstoneBoard(nbt.getString("id"));
        return getRedstoneBoard(new ResourceLocation(nbt.getString("id")));
    }

    @Override
    public RedstoneBoardNBT<?> getRedstoneBoard(ResourceLocation id) {
        BoardFactory factory = boards.get(id);

        if (factory != null) {
            return factory.boardNBT;
        } else {
            return emptyRobotBoardNBT;
        }
    }

    @Override
    // public Collection<RedstoneBoardNBT<?>> getAllBoardNBTs()
    public List<RedstoneBoardNBT<?>> getAllBoardNBTs() {
        ArrayList<RedstoneBoardNBT<?>> result = new ArrayList<RedstoneBoardNBT<?>>();

        for (BoardFactory f : boards.values()) {
            result.add(f.boardNBT);
        }

        return result;
    }

    @Override
    public long getPowerCost(RedstoneBoardNBT<?> board) {
        return boards.get(board.getID()).energyCost;
    }

    // Calen 1.18.2
    @Override
    public Map<RedstoneBoardNBT<?>, RegistryObject<? extends Item>> getBoardNBTItemMap() {
        return boardNBTItemMap;
    }
}
