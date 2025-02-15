package buildcraft.transport.client.render;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.lib.registry.CreativeTabManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
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
    public static void onDrawScreenEventPre(ScreenEvent.Render.Pre event) {
        if (m_removeWidget == null || m_addRenderableWidget == null) {
            return;
        }
        try {
            if (event.getScreen() instanceof CreativeModeInventoryScreen screen) {
                // pipes tab
                if (screen.selectedTab == CreativeTabManager.getTab("buildcraft.pipes")) {
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
                    else if (screen.renderables.contains(button) && (button.getX() != (screen.getGuiLeft() + LEFT_POS_ADD) || button.getY() != (screen.getGuiTop() + TOP_POS_ADD))) {
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

    private static final MutableComponent ALL = Component.translatable("gui.creativetab.pipe.button.all");
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
            screen.getMenu().items.clear();
            screen.getMenu().items.addAll(screen.selectedTab.getDisplayItems());
            screen.scrollOffs = 0.0F;
            screen.getMenu().scrollTo(0.0F);
            button.setMessage(colourToComponent());
        }
        // limited
        else {
            screen.getMenu().items.clear();
            screen.getMenu().items.addAll(screen.selectedTab.getDisplayItems());
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
            return Component.literal(ColourUtil.getTextFullTooltipString(FILTERED_TRANSLATION_KEY, dyeColor));
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
            super(x, y, w, h, message, func, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int x, int y, float particleTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
//            int i = this.getYImage(this.isHoveredOrFocused()); // 1.18.2
            int i = this.getTextureY(); // 1.20.1
            // LU
            guiGraphics.blit(
                    WIDGETS_LOCATION,
                    this.getX(), this.getY(),
//                    0, 46 + i * 20, // 1.18.2
                    0, i, // 1.20.1
                    this.width / 2, this.height / 2
            );
            // LD
            guiGraphics.blit(
                    WIDGETS_LOCATION,
                    this.getX(), this.getY() + this.height / 2,
//                    0, 46 + (i + 1) * 20 - this.height / 2, // 1.18.2
                    0, i + 20 - this.height / 2, // 1.20.1
                    this.width / 2, this.height / 2
            );
            // RU
            guiGraphics.blit(
                    WIDGETS_LOCATION,
                    this.getX() + this.width / 2, this.getY(),
//                    200 - this.width / 2, 46 + i * 20, // 1.18.2
                    200 - this.width / 2, i, // 1.20.1
                    this.width / 2, this.height / 2
            );
            // RD
            guiGraphics.blit(
                    WIDGETS_LOCATION,
                    this.getX() + this.width / 2, this.getY() + this.height / 2,
//                    200 - this.width / 2, 46 + (i + 1) * 20 - this.height / 2, // 1.18.2
                    200 - this.width / 2, i + 20 - this.height / 2, // 1.20.1
                    this.width / 2, this.height / 2
            );
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            int j = getFGColor();
            this.renderString(guiGraphics, minecraft.font, j | Mth.ceil(this.alpha * 255.0F) << 24);
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

        @Override
        public void setFocused(boolean p_93693_) {
            super.setFocused(false);
        }
    }
}
