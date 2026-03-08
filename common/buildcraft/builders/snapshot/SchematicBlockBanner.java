package buildcraft.builders.snapshot;

import buildcraft.api.schematics.SchematicBlockContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        if (context.blockState.hasBlockEntity()) {
            BlockEntity tileEntity = context.world.getBlockEntity(context.pos);
            if (tileEntity instanceof BannerBlockEntity banner) {
                tileNbt = banner.serializeNBT();
                // Calen
                requiredItem = banner.getItem();
            }
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
