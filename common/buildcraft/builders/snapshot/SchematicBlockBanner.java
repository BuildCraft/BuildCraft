package buildcraft.builders.snapshot;

import buildcraft.api.schematics.SchematicBlockContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// Calen
public class SchematicBlockBanner extends SchematicBlockDefault {
    private ItemStack requiredItem;

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = null;
        TileEntity tileEntity = context.world.getBlockEntity(context.pos);
        if (tileEntity instanceof BannerTileEntity) {
            BannerTileEntity banner = (BannerTileEntity) tileEntity;
            tileNbt = banner.serializeNBT();
            // Calen
            requiredItem = banner.getItem(context.blockState);
        }
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
//        return Collections.singletonList(ItemBanner.makeBanner(
//                EnumDyeColor.byDyeDamage(tileNbt.getInteger("Base")), tileNbt.getTagList("Patterns", 10)));
        return Collections.singletonList(requiredItem);
    }
}
