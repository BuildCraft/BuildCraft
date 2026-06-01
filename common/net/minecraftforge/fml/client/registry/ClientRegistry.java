// STUB(R.Chen): Forge ClientRegistry — compile shim. TESR registration replaced by Fabric BlockEntityRendererFactories.
package net.minecraftforge.fml.client.registry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class ClientRegistry {
    public static <T extends BlockEntity> void bindTileEntitySpecialRenderer(Class<T> tileEntityClass, Object renderer) {
        // no-op — replaced by BlockEntityRendererFactories.register() in BCLib client init
    }
}
