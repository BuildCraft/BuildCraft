package buildcraft.builders.item;

import buildcraft.builders.BCBuildersItems;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ItemBlueprint extends ItemBC_Neptune {
    public static final int META_CLEAN = 0;
    public static final int META_USED = 1;
    public static final String NBT_HEADER = "bpt-header";

    public ItemBlueprint(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(16);
    }

    public static BptStorage getBptStorageForStack(ItemStack stack) {
        return new BptStorage(stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta == META_CLEAN) {
            return 16;
        }
        return 1;
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
        subItems.add(new ItemStack(item, 1, META_CLEAN));
        subItems.add(new ItemStack(item, 1, META_USED));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, META_CLEAN, "clean");
        addVariant(variants, META_USED, "used");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        LibraryEntryHeader header = getBptStorageForStack(stack).getHeader();
        if(header != null) {
            // TODO: localization
            tooltip.add(header.name + " created by " + header.author.getOwnerName() + " on " + header.creation.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " "));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking() && stack.getMetadata() == META_USED) {
            return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(stack.getItem(), 1, META_CLEAN));
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public static class BptStorage {
        private LibraryEntryHeader header;

        public BptStorage(LibraryEntryHeader header) {
            this.header = header;
        }

        public BptStorage(ItemStack stack) {
            if (stack != null) {
                NBTTagCompound nbt = stack.getTagCompound();
                NBTTagCompound sub = nbt == null ? null : nbt.getCompoundTag(NBT_HEADER);
                if (sub != null) {
                    header = new LibraryEntryHeader(sub);
                }
            }
        }

        public LibraryEntryHeader getHeader() {
            return header;
        }

        public ItemStack save() {
            if (BCBuildersItems.blueprint == null) {
                return null;
            }
            ItemStack stack = new ItemStack(BCBuildersItems.blueprint);
            if (header == null) {
                stack.setItemDamage(META_CLEAN);
            } else {
                NBTTagCompound nbt = NBTUtils.getItemData(stack);
                nbt.setTag(NBT_HEADER, header.writeToNBT());
                stack.setItemDamage(META_USED);
            }
            return stack;
        }
    }
}
