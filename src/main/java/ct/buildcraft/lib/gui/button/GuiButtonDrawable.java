package ct.buildcraft.lib.gui.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.BuildCraftGui;
import ct.buildcraft.lib.gui.ISimpleDrawable;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;

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
    public void drawBackground(PoseStack pose, float partialTicks) {
        if (!visible) {
            return;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();

        if (enabled) {
            boolean hovered = isMouseOver();
            if (active) {
                if (hovered) {
                    drActiveHovered.drawAt(pose, this);
                } else {
                    drActive.drawAt(pose, this);
                }
            } else if (hovered) {
                drHovered.drawAt(pose, this);
            } else {
                drEnabled.drawAt(pose, this);
            }
        } else if (active) {
            drDisabledActive.drawAt(pose, this);
        } else {
            drDisabled.drawAt(pose, this);
        }
    }
}
