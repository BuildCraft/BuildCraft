package buildcraft.builders.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.data.NBTSquishConstants;
import buildcraft.api.items.INamedItem;
import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.bpt.PerSaveBptStorage;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.migrate.LibraryMigration;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.nbt.NbtSquisher;

public class ItemBlueprint extends ItemBC_Neptune implements INamedItem {
    public static final int META_CLEAN = 0;
    public static final int META_USED = 1;

    public static final String NBT_TYPE = "bpt-type";
    public static final String NBT_NAME = "name";

    // type 0: old storage type with a LibraryID
    public static final int TYPE_OLD = 0;

    // type 1: blueprint stored directly (compressed)
    public static final int TYPE_STORED = 1;
    public static final String NBT_STORED_DATA = "bpt-data";

    // type 2: blueprint stored in the library with an integer index
    public static final int TYPE_EXTENRAL = 2;
    public static final String NBT_EXTERNAL_INDEX = "external-index";

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

    @Override
    public String getName(ItemStack stack) {
        return NBTUtils.getItemData(stack).getString(NBT_NAME);
    }

    @Override
    public boolean setName(ItemStack stack, String name) {
        NBTUtils.getItemData(stack).setString(NBT_NAME, name);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        if (advanced) {
            if (stack.getMetadata() == META_USED) {
                NBTTagCompound nbt = NBTUtils.getItemData(stack);
                int type = nbt.getInteger(NBT_TYPE);
                if (type == TYPE_OLD) {
                    tooltip.add(type + ": BC 7.x + before");
                } else if (type == TYPE_STORED) {
                    tooltip.add(type + ": Stored direct");
                    byte[] bytes = nbt.getByteArray(NBT_STORED_DATA);
                    tooltip.add(bytes.length + " bytes");
                    tooltip.add(getByteArrayString(bytes));
                    if (bytes[0] == NBTSquishConstants.VANILLA_COMPRESSED || bytes[0] == NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                        bais.read();
                        try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
                            bytes = IOUtils.toByteArray(gzip);
                            tooltip.add(bytes.length + " bytes (uncompressed)");
                            tooltip.add(getByteArrayString(bytes));
                        } catch (IOException io) {
                            tooltip.add("Not GZip :(");
                        }
                    }

                } else if (type == TYPE_EXTENRAL) {
                    tooltip.add(type + ": Stored External");
                    int externalIndex = nbt.getInteger(NBT_EXTERNAL_INDEX);
                    tooltip.add("@ index " + externalIndex);
                }
            }
        }
    }

    private static String getByteArrayString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("= [");
        for (int i = 0; i < 20 & i < bytes.length; i++) {
            int b = Byte.toUnsignedInt(bytes[i]);
            sb.append(" ");
            String hex = Integer.toHexString(b);
            if (hex.length() != 2) {
                sb.append("0");
            }
            sb.append(hex);
        }
        if (bytes.length > 20) {
            sb.append(" ...");
        } else {
            sb.append(" ]");
        }
        return sb.toString();
    }

    public BptStorage createStorage(ItemStack stack) {
        return new BptStorage(stack);
    }

    public BptStorage createStorage(NBTTagCompound data) {
        return new BptStorage(data);
    }

    public class BptStorage {
        private NBTTagCompound saved;

        protected BptStorage(ItemStack stack) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null) {
                int type = nbt.getInteger(NBT_TYPE);
                if (type == TYPE_OLD) {
                    NBTTagCompound rnbt = LibraryMigration.getMigratedBlueprint(stack);
                    saved = rnbt;
                } else if (type == TYPE_STORED) {
                    byte[] compressed = nbt.getByteArray(NBT_STORED_DATA);
                    try {
                        saved = NbtSquisher.expand(compressed);
                    } catch (IOException io) {
                        throw new IllegalStateException(io);
                    }
                } else if (type == TYPE_EXTENRAL) {
                    int idx = nbt.getInteger(NBT_EXTERNAL_INDEX);
                    saved = PerSaveBptStorage.retrieveNbt(idx);
                    if (saved == null) {
                        BCLog.logger.warn("Uh-oh, did you migrate an item? NULL ENTRY! (id = " + idx + ")");
                    }
                }
            }
        }

        protected BptStorage(NBTTagCompound data) {
            this.saved = data;
        }

        public ItemStack save() {
            ItemStack stack = new ItemStack(ItemBlueprint.this, 1, META_USED);
            byte[] compressed = NbtSquisher.squishBuildCraftV1(saved);
            NBTTagCompound nbt = NBTUtils.getItemData(stack);
            if (compressed.length < BCBuildersConfig.bptStoreExternalThreshold) {
                nbt.setInteger(NBT_TYPE, TYPE_STORED);
                nbt.setByteArray(NBT_STORED_DATA, compressed);
            } else {
                int index = PerSaveBptStorage.storeNBT(saved);
                nbt.setInteger(NBT_TYPE, TYPE_EXTENRAL);
                nbt.setInteger(NBT_EXTERNAL_INDEX, index);
            }
            return stack;
        }

        public NBTTagCompound getSaved() {
            return saved;
        }
    }
}
