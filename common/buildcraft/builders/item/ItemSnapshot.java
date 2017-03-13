package buildcraft.builders.item;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.item.ItemBC_Neptune;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class ItemSnapshot extends ItemBC_Neptune {
    public ItemSnapshot(String id) {
        super(id);
        setHasSubtypes(true);
    }

    public ItemStack getClean(Snapshot.EnumSnapshotType snapshotType, Snapshot.Header header) {
        return new ItemStack(
                this,
                1,
                EnumItemSnapshotType.get(snapshotType, false).ordinal()
        );
    }

    public ItemStack getUsed(Snapshot.EnumSnapshotType snapshotType, Snapshot.Header header) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("header", header.serializeNBT());
        ItemStack stack = new ItemStack(
                this,
                1,
                EnumItemSnapshotType.get(snapshotType, true).ordinal()
        );
        stack.setTagCompound(nbt);
        return stack;
    }

    public Snapshot.Header getHeader(ItemStack stack) {
        if (EnumItemSnapshotType.getFromStack(stack).used) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null) {
                if (nbt.hasKey("header")) {
                    Snapshot.Header header = new Snapshot.Header();
                    header.deserializeNBT(nbt.getCompoundTag("header"));
                    return header;
                }
            }
        }
        return null;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return EnumItemSnapshotType.getFromStack(stack).used ? 1 : 16;
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        IntStream.range(0, EnumItemSnapshotType.values().length).forEach(i -> subItems.add(new ItemStack(item, 1, i)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (EnumItemSnapshotType type : EnumItemSnapshotType.values()) {
            addVariant(variants, type.ordinal(), type.getName());
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        Snapshot.Header header = getHeader(stack);
        if (header != null) {
            tooltip.add("Id: " + header.id);
            EntityPlayer ownerPlayer = header.getOwnerPlayer(player.world);
            tooltip.add("Owner: " + (ownerPlayer != null ? ownerPlayer.getName() : header.owner));
            tooltip.add("Created: " + header.created);
            tooltip.add("Name: " + header.name);
        }
    }

    public enum EnumItemSnapshotType implements IStringSerializable {
        TEMPLATE_CLEAN(Snapshot.EnumSnapshotType.TEMPLATE, false),
        TEMPLATE_USED(Snapshot.EnumSnapshotType.TEMPLATE, true),
        BLUEPRINT_CLEAN(Snapshot.EnumSnapshotType.BLUEPRINT, false),
        BLUEPRINT_USED(Snapshot.EnumSnapshotType.BLUEPRINT, true);

        public Snapshot.EnumSnapshotType snapshotType;
        public boolean used;

        EnumItemSnapshotType(Snapshot.EnumSnapshotType snapshotType, boolean used) {
            this.snapshotType = snapshotType;
            this.used = used;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static EnumItemSnapshotType get(Snapshot.EnumSnapshotType snapshotType, boolean used){
            if (snapshotType == Snapshot.EnumSnapshotType.TEMPLATE) {
                return !used ? TEMPLATE_CLEAN : TEMPLATE_USED;
            } else if (snapshotType == Snapshot.EnumSnapshotType.BLUEPRINT) {
                return !used ? BLUEPRINT_CLEAN : BLUEPRINT_USED;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public static EnumItemSnapshotType getFromStack(ItemStack stack) {
            return values()[Math.abs(stack.getMetadata()) % values().length];
        }
    }
}
