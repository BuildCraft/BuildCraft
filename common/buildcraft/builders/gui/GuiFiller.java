package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.tiles.IControllable.Mode;

import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.elem.ToolTip;
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
        properties.put("filler.pattern", container.tile.pattern);
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);
    }

    @Override
    protected void postLoad() {
        super.postLoad();
        setupButton("filler.no_excavate", b -> {
            b.setBehaviour(IButtonBehaviour.TOGGLE);
            final ToolTip active = ToolTip.createLocalized("tip.filler.excavate.off");
            final ToolTip inactive = ToolTip.createLocalized("tip.filler.excavate.on");
            b.setActive(!container.tile.canExcavate());
            IButtonClickEventListener listener = (b2, k) -> {
                b.setToolTip(b.active ? active : inactive);
                container.tile.sendCanExcavate(!b.active);
            };
            listener.handleButtonClick(b, 0);
            b.registerListener(listener);
        });
        setupButton("filler.invert", b -> {
            b.setBehaviour(IButtonBehaviour.TOGGLE);
            final ToolTip on = ToolTip.createLocalized("tip.filler.invert.on");
            final ToolTip off = ToolTip.createLocalized("tip.filler.invert.off");
            b.setActive(container.tile.shouldInvert());
            IButtonClickEventListener listener = (b2, k) -> {
                b.setToolTip(b.active ? on : off);
                container.tile.sendInvert(b.active);
            };
            listener.handleButtonClick(b, 0);
            b.registerListener(listener);
        });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        SPRITE_PATTERN.delegate = container.tile.pattern.get().getSprite();
        Mode mode = container.tile.getControlMode();
        SPRITE_CONTROL_MODE.delegate = BCCoreSprites.ACTION_MACHINE_CONTROL.get(mode);
    }
}
