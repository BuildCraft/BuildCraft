/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WorldPropertyIsOre extends WorldProperty {
    private final HashSet<Integer> ores = new HashSet<Integer>();
    private final int harvestLevel;

    public WorldPropertyIsOre(int harvestLevel) {
        initBlockHarvestTools();
        this.harvestLevel = harvestLevel;

        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("ore")) {
                List<ItemStack> oreStacks = OreDictionary.getOres(oreName);
                if (oreStacks.size() > 0) {
                    Block block = Block.getBlockFromItem(oreStacks.get(0).getItem());
                    if (block == null) {
                        continue;
                    }
                    int meta = oreStacks.get(0).getItemDamage();
                    if (meta >= 16 || meta < 0) {
                        meta = 0;
                    }
                    IBlockState state = block.getStateFromMeta(meta);
                    if ("pickaxe".equals(block.getHarvestTool(state)) && block.getHarvestLevel(state) <= harvestLevel) {
                        ores.add(OreDictionary.getOreID(oreName));
                    }
                }
            }
        }
    }

    private void initBlockHarvestTools() {
        // Make sure the static code block in the ForgeHooks class is run
        new ForgeHooks();
    }

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == null) {
            return false;
        } else {
            // Workaround for lit redstone ore
            if (state.getBlock() == Blocks.lit_redstone_ore) {
                return block.getHarvestLevel(state) <= harvestLevel;
            }

            List<ItemStack> toCheck = new ArrayList<ItemStack>();
            toCheck.add(new ItemStack(block, 1, block.getMetaFromState(state)));

            if (block.hasTileEntity(state) && blockAccess instanceof World) {
                toCheck.addAll(block.getDrops((World) blockAccess, pos, state, 0));
            }

            for (ItemStack stack : toCheck) {
                if (stack.getItem() != null) {
                    for (int id : OreDictionary.getOreIDs(stack)) {
                        if (ores.contains(id)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
