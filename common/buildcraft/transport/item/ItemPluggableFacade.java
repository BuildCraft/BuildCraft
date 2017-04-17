package buildcraft.transport.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.LoadingException;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggableFacade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemPluggableFacade extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableFacade(String id) {
        super(id);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public ItemStack createItem(IBlockState state, boolean isHollow) {
        ItemStack item = new ItemStack(this);
        item.setTagCompound(new NBTTagCompound());
        // noinspection ConstantConditions
        item.getTagCompound().setTag("state", NBTUtilBC.writeEntireBlockState(state));
        item.getTagCompound().setBoolean("isHollow", isHollow);
        return item;
    }

    public static IBlockState getState(ItemStack item) {
        // noinspection ConstantConditions
        if (!item.hasTagCompound() || !item.getTagCompound().hasKey("state")) {
            return null;
        }
        try {
            return NBTUtilBC.readEntireBlockState(item.getTagCompound().getCompoundTag("state"));
        } catch (LoadingException ignored) {
            return null;
        }
    }

    public static boolean getIsHollow(ItemStack item) {
        // noinspection ConstantConditions
        if (!item.hasTagCompound() || !item.getTagCompound().hasKey("isHollow")) {
            return false;
        }
        return item.getTagCompound().getBoolean("isHollow");
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side) {
        IBlockState state = getState(stack);
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
            Item.REGISTRY.forEach(itemBlock -> { // FIXME: probably not this registry
                if (itemBlock instanceof ItemBlock) {
                    IBlockState state = ((ItemBlock) itemBlock).block.getDefaultState();
                    subItems.add(((ItemPluggableFacade) item).createItem(state, false));
                    subItems.add(((ItemPluggableFacade) item).createItem(state, true));
                }
            });
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add("State: " + getState(stack));
        if (getIsHollow(stack)) {
            tooltip.add("Hollow");
        }
    }
}
