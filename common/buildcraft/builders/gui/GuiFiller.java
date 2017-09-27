package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.tiles.IControllable.Mode;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.json.GuiJson;
import buildcraft.lib.gui.json.InventorySlotHolder;
import buildcraft.lib.gui.json.SpriteDelegate;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.filler.FillerStatementContext;
import buildcraft.core.BCCoreSprites;

public class GuiFiller extends GuiJson<ContainerFiller> {

    public static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();
    private static final SpriteDelegate SPRITE_CONTROL_MODE = new SpriteDelegate();

    public GuiFiller(ContainerFiller container) {
        super(container, LOCATION);
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        properties.put("filler.inventory", new InventorySlotHolder(container, container.tile.invResources));
        properties.put("statement.container", container.tile);
        properties.put("controllable", container.tile);
        properties.put("controllable.sprite", SPRITE_CONTROL_MODE);
        context.put_o("controllable.mode", Mode.class, container.tile::getControlMode);
        context.put_b("filler.is_finished", container.tile::isFinished);
        context.put_b("filler.is_locked", container.tile::isLocked);
        context.put_l("filler.to_break", container.tile::getCountToBreak);
        context.put_l("filler.to_place", container.tile::getCountToPlace);
        properties.put("filler.possible", FillerStatementContext.CONTEXT_ALL);
        properties.put("filler.pattern", container.tile.patternStatement);
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);

        context.put_b("filler.invert", container.tile::shouldInvert);
        properties.put("filler.invert", IButtonBehaviour.TOGGLE);
        properties.put("filler.invert", container.tile.shouldInvert());
        properties.put("filler.invert", (IButtonClickEventListener) (b, k) -> {
            container.tile.sendInvert(b.isButtonActive());
        });

        context.put_b("filler.excavate", container.tile::canExcavate);
        properties.put("filler.excavate", IButtonBehaviour.TOGGLE);
        properties.put("filler.excavate", container.tile.canExcavate());
        properties.put("filler.excavate", (IButtonClickEventListener) (b, k) -> {
            container.tile.sendCanExcavate(b.isButtonActive());
        });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        IFillerPattern pattern = container.tile.patternStatement.get();
        SPRITE_PATTERN.delegate = pattern == null ? null : pattern.getSprite();
        Mode mode = container.tile.getControlMode();
        SPRITE_CONTROL_MODE.delegate = BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
    }
}
