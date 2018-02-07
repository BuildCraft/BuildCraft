package buildcraft.lib.gui.button;

import buildcraft.lib.gui.BuildCraftGui;
import net.minecraft.client.renderer.GlStateManager;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

public final class GuiButtonDrawable extends GuiAbstractButton {
    private final ISimpleDrawable drEnabled, drActive, drHovered, drActiveHovered, drDisabled, drDisabledActive;

    public static class Builder {
        public final IGuiArea rect;
        public final ISimpleDrawable enabled;
        public ISimpleDrawable active;
        public ISimpleDrawable hovered;
        public ISimpleDrawable activeHovered;
        public ISimpleDrawable disabled;
        public ISimpleDrawable disabledActive;

        public Builder(IGuiArea rect, ISimpleDrawable enabled) {
            this.rect = rect;
            this.enabled = enabled;
        }
    }

    public GuiButtonDrawable(BuildCraftGui gui, String id, IGuiPosition parent, Builder args) {
        super(gui, id, args.rect.offset(parent));
        this.drEnabled = args.enabled;
        this.drActive = getFirstNonnull(args.active, args.enabled);
        this.drHovered = getFirstNonnull(args.hovered, args.enabled);
        this.drActiveHovered = getFirstNonnull(args.activeHovered, args.hovered, args.active, args.enabled);
        this.drDisabled = getFirstNonnull(args.disabled, args.enabled);
        this.drDisabledActive = getFirstNonnull(args.disabledActive, args.disabled, args.enabled);
    }

    private static ISimpleDrawable getFirstNonnull(ISimpleDrawable... of) {
        for (ISimpleDrawable d : of) {
            if (d != null) {
                return d;
            }
        }
        throw new NullPointerException("No non-null elements found!");
    }

    @Override
    public void drawBackground(float partialTicks) {
        if (!visible) {
            return;
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        if (enabled) {
            boolean hovered = isMouseOver();
            if (active) {
                if (hovered) {
                    drActiveHovered.drawAt(this);
                } else {
                    drActive.drawAt(this);
                }
            } else if (hovered) {
                drHovered.drawAt(this);
            } else {
                drEnabled.drawAt(this);
            }
        } else if (active) {
            drDisabledActive.drawAt(this);
        } else {
            drDisabled.drawAt(this);
        }
    }
}
