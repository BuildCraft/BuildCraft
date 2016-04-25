package buildcraft.lib.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;

public abstract class TileBuildCraft_BC8 extends TileEntity {
    public TileBuildCraft_BC8() {

    }

    public static <T extends TileBuildCraft_BC8> void registerTile(Class<T> tileClass, String id) {
        String regName = TagManager.getTag(id, EnumTagType.REGISTRY_NAME);
        String[] alternatives = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
        GameRegistry.registerTileEntityWithAlternatives(tileClass, regName, alternatives);
    }

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasWorldObj();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void redrawBlock() {
        if (hasWorldObj()) worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
    }
    
    public void sendNetworkUpdate() {
        
    }
    
    public MessageUpdateTile createUpdateTileMessage() {
        return null;
    }
}
