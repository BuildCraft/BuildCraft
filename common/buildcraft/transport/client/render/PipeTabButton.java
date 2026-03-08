package buildcraft.transport.client.render;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.lib.registry.CreativeTabManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.Iterator;

public class PipeTabButton {
    private static int LEFT_POS_ADD = 104;
    private static int TOP_POS_ADD = 3;
    private static int BUTTON_W = 84;
    private static int BUTTON_H = 14;
    private static CreativeModeInventoryScreen screen = null;
    private static Button button;
    /**
     * 0 -> all
     * 1 -> colorless
     * 2-18 -> colours
     */
    private static int colour = 0;
    private static Method m_removeWidget;
    private static Method m_addRenderableWidget;


    static {
        try {
            // srg
            m_removeWidget = Screen.class.getDeclaredMethod("m_169411_", GuiEventListener.class);
            m_removeWidget.setAccessible(true);
            m_addRenderableWidget = Screen.class.getDeclaredMethod("m_142416_", GuiEventListener.class);
            m_addRenderableWidget.setAccessible(true);
        } catch (NoSuchMethodException e) {
            try {
                // official
                m_removeWidget = Screen.class.getDeclaredMethod("removeWidget", GuiEventListener.class);
                m_removeWidget.setAccessible(true);
                m_addRenderableWidget = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
                m_addRenderableWidget.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                BCLog.logger.error(e);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onDrawScreenEventPre(ScreenEvent.DrawScreenEvent.Pre event) {
        if (m_removeWidget == null || m_addRenderableWidget == null) {
            return;
        }
        try {
            if (event.getScreen() instanceof CreativeModeInventoryScreen screen) {
                // pipes tab
                if (screen.getSelectedTab() == CreativeTabManager.getTab("buildcraft.pipes").getId()) {
                    // screen changed
                    if (screen != PipeTabButton.screen) {
                        if (PipeTabButton.screen != null) {
                            removeButton(PipeTabButton.screen, button);
                        }
                        PipeTabButton.screen = screen;
                        button = newButton(screen);
                        m_addRenderableWidget.invoke(screen, button);
                        updateTabItems(screen);
                    }
                    // button not created
                    else if (!screen.renderables.contains(button) || button == null) {
                        button = newButton(screen);
                        m_addRenderableWidget.invoke(screen, button);
                        updateTabItems(screen);
                    }
                    // window size changed
                    else if (screen.renderables.contains(button) && (button.x != (screen.getGuiLeft() + LEFT_POS_ADD) || button.y != (screen.getGuiTop() + TOP_POS_ADD))) {
                        // Calen: accesstransformer doesn't work for removeWidget & addRenderableWidget
                        // When gradle refreshed, runClient will be not able to launch mc
                        removeButton(screen, button);
                        button = newButton(screen);
                        m_addRenderableWidget.invoke(screen, button);
                        updateTabItems(screen);
                    }
                }
                // no longer pipes tab
                else {
                    removeButton(screen, button);
                }
            }
            // CreativeModeInventoryScreen closed
            else {
                if (PipeTabButton.screen != null) {
                    removeButton(PipeTabButton.screen, button);
                    button = null;
                    PipeTabButton.screen = null;
                }
            }
        } catch (ReflectiveOperationException e) {
            BCLog.logger.error(e);
        }
    }

    public static void removeButton(CreativeModeInventoryScreen screen, Button button) throws ReflectiveOperationException {
        m_removeWidget.invoke(screen, button);
        screen.renderables.remove(button);
    }

    private static final TranslatableComponent ALL = new TranslatableComponent("gui.creativetab.pipe.button.all");
    private static final String FILTERED_TRANSLATION_KEY = "gui.creativetab.pipe.button.filtered";

    private static void onPress(CreativeModeInventoryScreen screen, Button button, int pMouseButton) {
        if (pMouseButton == InputConstants.MOUSE_BUTTON_LEFT) {
            colourSHL();
        } else if (pMouseButton == InputConstants.MOUSE_BUTTON_RIGHT) {
            colourSHR();
        } else {
            return;
        }
        updateTabItems(screen);
    }

    private static void updateTabItems(CreativeModeInventoryScreen screen) {
        // show all
        if (colour == 0) {
            CreativeModeTab tab = CreativeModeTab.TABS[screen.getSelectedTab()];
            screen.getMenu().items.clear();
            tab.fillItemList(screen.getMenu().items);
            screen.scrollOffs = 0.0F;
            screen.getMenu().scrollTo(0.0F);
            button.setMessage(colourToComponent());
        }
        // limited
        else {
            CreativeModeTab tab = CreativeModeTab.TABS[screen.getSelectedTab()];
            screen.getMenu().items.clear();
            tab.fillItemList(screen.getMenu().items);
            DyeColor dyeColor = colour == 1 ? null : DyeColor.byId(colour - 2);
            TagKey<Item> tag = OreDictionaryTags.pipeColorTags.get(dyeColor);
            Iterator<ItemStack> itr = screen.getMenu().items.iterator();
            while (itr.hasNext()) {
                ItemStack stack = itr.next();
                if (!stack.is(tag)) {
                    itr.remove();
                }
            }
            screen.scrollOffs = 0.0F;
            screen.getMenu().scrollTo(0.0F);
            button.setMessage(colourToComponent());
        }
    }

    private static MutableComponent colourToComponent() {
        if (colour == 0) {
            return ALL;
        } else {
            DyeColor dyeColor = colour == 1 ? null : DyeColor.byId(colour - 2);
            return new TextComponent(ColourUtil.getTextFullTooltipString(FILTERED_TRANSLATION_KEY, dyeColor));
        }
    }

    private static void colourSHL() {
        colour = (colour + 1) % 18;
    }

    private static void colourSHR() {
        colour = (colour - 1 + 18) % 18;
    }

    private static Button newButton(CreativeModeInventoryScreen screen) {
        return new ColourFilterButton(screen.getGuiLeft() + LEFT_POS_ADD, screen.getGuiTop() + TOP_POS_ADD, BUTTON_W, BUTTON_H, colourToComponent(), (button) ->
        {
        });
    }

    public static class ColourFilterButton extends Button {
        public ColourFilterButton(int x, int y, int w, int h, Component message, OnPress func) {
            super(x, y, w, h, message, func);
        }

        @Override
        public void renderButton(PoseStack poseStack, int x, int y, float particleTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            SpriteUtil.bindTexture(WIDGETS_LOCATION);
            RenderUtil.color(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHoveredOrFocused());
            RenderUtil.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            // LU
            this.blit(poseStack,
                    this.x, this.y,
                    0, 46 + i * 20,
                    this.width / 2, this.height / 2
            );
            // LD
            this.blit(poseStack,
                    this.x, this.y + this.height / 2,
                    0, 46 + (i + 1) * 20 - this.height / 2,
                    this.width / 2, this.height / 2
            );
            // RU
            this.blit(poseStack,
                    this.x + this.width / 2, this.y,
                    200 - this.width / 2, 46 + i * 20,
                    this.width / 2, this.height / 2
            );
            // RD
            this.blit(poseStack,
                    this.x + this.width / 2, this.y + this.height / 2,
                    200 - this.width / 2, 46 + (i + 1) * 20 - this.height / 2,
                    this.width / 2, this.height / 2
            );
            this.renderBg(poseStack, minecraft, x, y);
            int j = getFGColor();
            drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pMouseButton) {
            if (this.active && this.visible) {
                if (this.isValidClickButton(pMouseButton)) {
                    boolean flag = this.clicked(pMouseX, pMouseY);
                    if (flag) {
                        this.playDownSound(Minecraft.getInstance().getSoundManager());
                        PipeTabButton.onPress(screen, this, pMouseButton);
                        return true;
                    }
                }

                return false;
            } else {
                return false;
            }
        }

        @Override
        public void onClick(double p_93371_, double p_93372_) {
            this.onPress();
        }

        @Override
        public void onPress() {
            this.onPress.onPress(this);
        }

        @Override
        protected boolean isValidClickButton(int mouseButton) {
            return mouseButton == InputConstants.MOUSE_BUTTON_LEFT || mouseButton == InputConstants.MOUSE_BUTTON_RIGHT;
        }
    }
}
