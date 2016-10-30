package buildcraft.lib.client.render;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.ItemStackKey;

public class ItemRenderUtil {

    private static final LoadingCache<ItemStackKey, Integer> glListCache;

    static {
        glListCache = CacheBuilder.newBuilder()//
                .expireAfterAccess(40, TimeUnit.SECONDS)//
                .removalListener(ItemRenderUtil::onStackRemove)//
                .build(CacheLoader.from(ItemRenderUtil::makeItemGlList));
    }

    private static final EntityItem dummyEntityItem = new EntityItem(null);
    private static final RenderEntityItem customItemRenderer = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
        @Override
        public boolean shouldSpreadItems() {
            return false;
        }

        @Override
        public boolean shouldBob() {
            return false;
        }
    };

    private static Integer makeItemGlList(ItemStackKey item) {
        int list = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        renderItemImpl(0, -0.1, 0, item.baseStack);
        GL11.glEndList();
        return Integer.valueOf(list);
    }

    private static void onStackRemove(RemovalNotification<ItemStackKey, Integer> notification) {
        Integer val = notification.getValue();
        if (val != null) {
            GLAllocation.deleteDisplayLists(val.intValue());
        }
    }

    private static void renderItemImpl(double x, double y, double z, ItemStack stack) {
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.1, 0);
        GL11.glScaled(0.9, 0.9, 0.9);

        // This is broken - some stacks render too big but some render way too small.
        // Also the stacks seem to be offset at a specific angle each time?

        dummyEntityItem.setEntityItemStack(stack);
        customItemRenderer.doRender(dummyEntityItem, x, y, z, 0, 0);

        GL11.glPopMatrix();
    }

    public static void renderItem(double x, double y, double z, ItemStack stack) {
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        model = model.getOverrides().handleItemState(model, stack, null, null);

        RenderHelper.enableStandardItemLighting();
        RenderHelper.disableStandardItemLighting();

        if (stack.hasEffect() || model.isBuiltInRenderer()) {
            renderItemImpl(x, y, z, stack);
        } else {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glCallList(glListCache.getUnchecked(new ItemStackKey(stack)));
            glListCache.cleanUp();

            GL11.glPopMatrix();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }
}
