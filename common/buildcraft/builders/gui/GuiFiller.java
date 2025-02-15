package buildcraft.builders.gui;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.filler.FillerStatementContext;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.gui.json.InventorySlotHolder;
import buildcraft.lib.gui.json.SpriteDelegate;
import buildcraft.lib.misc.collect.TypedKeyMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFiller extends GuiBC8<ContainerFiller> {
    private static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();
    private static final SpriteDelegate SPRITE_CONTROL_MODE = new SpriteDelegate();

    public GuiFiller(ContainerFiller container, Inventory inventory, Component component) {
        super(container, LOCATION, inventory, component);

        BuildCraftJsonGui jsonGui = (BuildCraftJsonGui) mainGui;
        preLoad(jsonGui);
        jsonGui.load();
//        xSize = jsonGui.getSizeX();
        imageWidth = jsonGui.getSizeX();
//        ySize = jsonGui.getSizeY();
        imageHeight = jsonGui.getSizeY();
    }

    protected void preLoad(BuildCraftJsonGui json) {

        TypedKeyMap<String, Object> properties = json.properties;
        FunctionContext context = json.context;

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
        properties.put("filler.pattern", container.getPatternStatementClient());
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);

        context.put_b("filler.invert", container::isInverted);
        properties.put("filler.invert", IButtonBehaviour.TOGGLE);
        properties.put("filler.invert", container.isInverted());
        properties.put("filler.invert",
                (IButtonClickEventListener) (b, k) -> container.sendInverted(b.isButtonActive()));

        context.put_b("filler.excavate", container.tile::canExcavate);
        properties.put("filler.excavate", IButtonBehaviour.TOGGLE);
        properties.put("filler.excavate", container.tile.canExcavate());
        properties.put("filler.excavate",
                (IButtonClickEventListener) (b, k) -> container.tile.sendCanExcavate(b.isButtonActive()));
    }

    @Override
//    public void updateScreen()
    public void containerTick() {
//        super.updateScreen();
        super.containerTick();
        IFillerPattern pattern = container.getPatternStatementClient().get();
        SPRITE_PATTERN.delegate = pattern == null ? null : pattern.getSprite();
        SPRITE_CONTROL_MODE.delegate = BCCoreSprites.ACTION_MACHINE_CONTROL.get(container.tile.getControlMode());
    }
}
