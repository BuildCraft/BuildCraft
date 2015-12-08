package buildcraft.robotics.render;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;

public class RedstoneBoardMeshDefinition implements ItemMeshDefinition {
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        RedstoneBoardNBT<?> board;
        if (stack == null || !stack.hasTagCompound()) {
            board = RedstoneBoardRegistry.instance.getEmptyRobotBoard();
        } else {
            NBTTagCompound nbt = stack.getTagCompound();
            board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
        }

        ModelResourceLocation loc = new ModelResourceLocation(board.getItemModelLocation(), "inventory");
        return loc;
    }
}
