package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.builders.tile.TileLibrary_Neptune;
import buildcraft.lib.BCLibDatabase;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.library.book.LibraryEntryBook;

public class BlockLibrary_Neptune extends BlockBCTile_Neptune {
    public BlockLibrary_Neptune(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLibrary_Neptune();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return false;
        }

        if (heldItem == null || heldItem.getItem() != Items.WRITTEN_BOOK) {
            return false;
        }

        LibraryEntryBook entry = LibraryEntryBook.create(heldItem);

        if (entry == null) {
            return false;
        }

        LibraryEntryHeader header = entry.getHeader();

        BCLibDatabase.LOCAL_DB.add(header, entry);

        return true;
    }
}
