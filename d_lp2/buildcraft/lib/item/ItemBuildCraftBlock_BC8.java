package buildcraft.lib.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.block.BlockBuildCraftBase_BC8;

/** Basically a copy of {@link ItemBlock}, but extends {@link ItemBuildCraft_BC8}. */
public class ItemBuildCraftBlock_BC8 extends ItemBuildCraft_BC8 {
    protected final BlockBuildCraftBase_BC8 block;

    public ItemBuildCraftBlock_BC8(BlockBuildCraftBase_BC8 block) {
        super("item." + block.id);
        this.block = block;
    }

    // ###############################
    //
    // ItemBlock code
    //
    // ###############################

    /** Sets the unlocalised name of this item to the string passed as the parameter, prefixed by "item." */
    @Override
    public ItemBuildCraftBlock_BC8 setUnlocalizedName(String unlocalizedName) {
        super.setUnlocalizedName(unlocalizedName);
        return this;
    }

    /** Called when a Block is right-clicked with this Item */
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack) && worldIn.canBlockBePlaced(this.block, pos, false, facing, (Entity) null, stack)) {
            int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, i, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
                SoundType soundtype = this.block.getSoundType();
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                --stack.stackSize;
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    public static boolean setTileEntityNBT(World worldIn, EntityPlayer pos, BlockPos stack, ItemStack stackIn) {
        MinecraftServer minecraftserver = worldIn.getMinecraftServer();

        if (minecraftserver == null) {
            return false;
        } else {
            if (stackIn.hasTagCompound() && stackIn.getTagCompound().hasKey("BlockEntityTag", 10)) {
                TileEntity tileentity = worldIn.getTileEntity(stack);

                if (tileentity != null) {
                    if (!worldIn.isRemote && tileentity.func_183000_F() && (pos == null || !minecraftserver.getPlayerList().canSendCommands(pos.getGameProfile()))) {
                        return false;
                    }

                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttagcompound.copy();
                    tileentity.writeToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound2 = (NBTTagCompound) stackIn.getTagCompound().getTag("BlockEntityTag");
                    nbttagcompound.merge(nbttagcompound2);
                    nbttagcompound.setInteger("x", stack.getX());
                    nbttagcompound.setInteger("y", stack.getY());
                    nbttagcompound.setInteger("z", stack.getZ());

                    if (!nbttagcompound.equals(nbttagcompound1)) {
                        tileentity.readFromNBT(nbttagcompound);
                        tileentity.markDirty();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }

        return worldIn.canBlockBePlaced(this.block, pos, false, side, (Entity) null, stack);
    }

    /** Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.block.getUnlocalizedName();
    }

    /** Returns the unlocalized name of this item. */
    @Override
    public String getUnlocalizedName() {
        return this.block.getUnlocalizedName();
    }

    /** gets the CreativeTab this item is displayed on */
    @Override
    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab() {
        return this.block.getCreativeTabToDisplayOn();
    }

    /** returns a list of items with the same ID, but different meta (eg: dye returns 16 items) */
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        this.block.getSubBlocks(itemIn, tab, subItems);
    }

    public Block getBlock() {
        return this.block;
    }

    /** Called to actually place the block, after the location is determined and all permission checks have been made.
     *
     * @param stack The item stack that was used to place the block. This can be changed inside the method.
     * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
     * @param side The side the player (or machine) right-clicked on. */
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 3)) return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block) {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }

        return true;
    }

    /** allows items to add custom lines of information to the mouseover description
     * 
     * @param advanced If the client has advanced tooltips (debug) enabled */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        block.addInformation(stack, playerIn, tooltip, advanced);
    }
}
