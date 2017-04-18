package buildcraft.transport.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggableFacade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public ItemStack createItem(ItemStack stack, boolean isHollow) {
        ItemStack item = new ItemStack(this);
        item.setTagCompound(new NBTTagCompound());
        // noinspection ConstantConditions
        item.getTagCompound().setTag("stack", stack.serializeNBT());
        item.getTagCompound().setBoolean("isHollow", isHollow);
        return item;
    }

    public static ItemStack getStack(ItemStack item) {
        // noinspection ConstantConditions
        if (!item.hasTagCompound() || !item.getTagCompound().hasKey("stack")) {
            return null;
        }
        return new ItemStack(item.getTagCompound().getCompoundTag("stack"));
    }

    public static boolean getIsHollow(ItemStack item) {
        // noinspection ConstantConditions
        if (!item.hasTagCompound() || !item.getTagCompound().hasKey("isHollow")) {
            return false;
        }
        return item.getTagCompound().getBoolean("isHollow");
    }

    @SideOnly(Side.CLIENT)
    public static IBlockState getState(ItemStack item, World world, BlockPos pos, EnumFacing side, EntityPlayer player, EnumHand hand) {
        ItemStack stack = getStack(item);
        if (stack == null || !(stack.getItem() instanceof ItemBlock)) {
            return null;
        }
        return ((ItemBlock) stack.getItem()).block.getStateForPlacement(
                world,
                pos,
                side,
                0,
                0,
                0,
                stack.getMetadata(),
                player,
                hand
        );
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        IBlockState state = getState(stack, holder.getPipeWorld(), holder.getPipePos(), side, player, hand);
        boolean isHollow = getIsHollow(stack);
        if (state == null) {
            return null;
        }
        return new PluggableFacade(BCTransportPlugs.facade, holder, side, state, isHollow);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (item instanceof ItemPluggableFacade) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            for (Item currentItem : Item.REGISTRY) {
                for (CreativeTabs currentTab : item.getCreativeTabs()) {
                    if (currentItem != item) {
                        currentItem.getSubItems(currentItem, currentTab, stacks);
                    }
                }
            }
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) stack.getItem()).block;
                    // FIXME: better check?
                    if (Block.FULL_BLOCK_AABB.equals(block.getCollisionBoundingBox(block.getDefaultState(), new IBlockAccess() {
                        @Nullable
                        @Override
                        public TileEntity getTileEntity(BlockPos pos) {
                            return null;
                        }

                        @Override
                        public int getCombinedLight(BlockPos pos, int lightValue) {
                            return 0;
                        }

                        @Override
                        public IBlockState getBlockState(BlockPos pos) {
                            return pos == BlockPos.ORIGIN ? block.getDefaultState() : Blocks.AIR.getDefaultState();
                        }

                        @Override
                        public boolean isAirBlock(BlockPos pos) {
                            return pos != BlockPos.ORIGIN;
                        }

                        @Override
                        public Biome getBiome(BlockPos pos) {
                            return null;
                        }

                        @Override
                        public int getStrongPower(BlockPos pos, EnumFacing direction) {
                            return 0;
                        }

                        @Override
                        public WorldType getWorldType() {
                            return WorldType.DEFAULT;
                        }

                        @Override
                        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
                            return false;
                        }
                    }, BlockPos.ORIGIN))) {
                        subItems.add(((ItemPluggableFacade) item).createItem(stack, false));
                        subItems.add(((ItemPluggableFacade) item).createItem(stack, true));
                    }
                }
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add("State: " + getStack(stack));
        if (getIsHollow(stack)) {
            tooltip.add("Hollow");
        }
    }
}
