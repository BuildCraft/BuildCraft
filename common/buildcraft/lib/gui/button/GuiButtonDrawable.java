package buildcraft.lib.gui.button;

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

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
    public void drawBackground(float partialTicks, MatrixStack poseStack) {
        if (!visible) {
            return;
        }

//        GlStateManager.color(1, 1, 1, 1);
        RenderUtil.color(1, 1, 1, 1);
//        GlStateManager.enableAlpha();
        RenderUtil.enableAlpha();
//        GlStateManager.disableBlend();
        RenderUtil.disableBlend();

        if (enabled) {
            boolean hovered = isMouseOver();
            if (active) {
                if (hovered) {
                    drActiveHovered.drawAt(this, poseStack);
                } else {
                    drActive.drawAt(this, poseStack);
                }
            } else if (hovered) {
                drHovered.drawAt(this, poseStack);
            } else {
                drEnabled.drawAt(this, poseStack);
            }
        } else if (active) {
            drDisabledActive.drawAt(this, poseStack);
        } else {
            drDisabled.drawAt(this, poseStack);
        }
    }
}
