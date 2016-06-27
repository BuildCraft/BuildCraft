package buildcraft.builders.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.builders.BCBuildersItems;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.library.LibraryEntryHeader;
import buildcraft.lib.misc.NBTUtils;

public class ItemBlueprint extends ItemBC_Neptune {
    public static final int META_CLEAN = 0;
    public static final int META_USED = 1;
    public static final String NBT_HEADER = "bpt-header";

    public ItemBlueprint(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(16);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta == META_CLEAN) {
            return 16;
        }
        return 1;
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
