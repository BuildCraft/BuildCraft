// STUB(R.Chen): Forge GameRegistry — compile shim.
package net.minecraftforge.fml.common.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class GameRegistry {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    public @interface ObjectHolder {
        String value();
    }

    public static <T extends BlockEntity> BlockEntityType<T> registerTileEntity(Class<T> tileEntityClass, String key) {
        return null;
    }
    public static void addShapelessRecipe(net.minecraft.item.ItemStack output, Object... components) {}
    public static void addShapedRecipe(net.minecraft.item.ItemStack output, Object... components) {}
}
