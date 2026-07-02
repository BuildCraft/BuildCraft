package ct.buildcraft.builders.gui;

import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.api.tiles.IControllable.Mode;
import ct.buildcraft.builders.filler.FillerStatementContext;
import ct.buildcraft.builders.menu.ContainerFiller;
import ct.buildcraft.core.BCCoreSprites;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.button.IButtonBehaviour;
import ct.buildcraft.lib.gui.button.IButtonClickEventListener;
import ct.buildcraft.lib.gui.json.BuildCraftJsonGui;
import ct.buildcraft.lib.gui.json.InventorySlotHolder;
import ct.buildcraft.lib.gui.json.SpriteDelegate;
import ct.buildcraft.lib.misc.collect.TypedKeyMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;

public class GuiFiller extends GuiBC8<ContainerFiller> {
    private static final ResourceLocation LOCATION = new ResourceLocation("buildcraftbuilders:gui/filler.json");
    private static final SpriteDelegate SPRITE_PATTERN = new SpriteDelegate();
    private static final SpriteDelegate SPRITE_CONTROL_MODE = new SpriteDelegate();

    public GuiFiller(ContainerFiller container, Inventory inv, Component title) {
        super(container, LOCATION, inv, title);
        BuildCraftJsonGui jsonGui = (BuildCraftJsonGui) mainGui;
        preLoad(jsonGui);
        jsonGui.load();
        imageWidth = jsonGui.getSizeX();
        imageHeight = jsonGui.getSizeY();
        GuiHelpUtil.addRoot(mainGui, 8, 85, 162, 54, "buildcraft.help.filler.resources.title", 0xFF_88_CC_88, "buildcraft.help.filler.resources.desc");
        GuiHelpUtil.addRoot(mainGui, 9, 35, 158, 30, "buildcraft.help.filler.pattern.title", 0xFF_66_AA_FF, "buildcraft.help.filler.pattern.desc");
        GuiHelpUtil.addRoot(mainGui, 8, 64, 162, 14, "buildcraft.help.filler.work.title", 0xFF_DD_CC_55, "buildcraft.help.filler.work.desc");
    }

    protected void preLoad(BuildCraftJsonGui json) {
        TypedKeyMap<String, Object> properties = json.properties;
        FunctionContext context = json.context;
        properties.put("filler.inventory", new InventorySlotHolder(container, container.getResources()));
        properties.put("statement.container", container.tile);
        properties.put("controllable", container.tile);
        properties.put("controllable.sprite", SPRITE_CONTROL_MODE);
        context.put_o("controllable.mode", Mode.class, () -> container.tile == null ? Mode.OFF : container.tile.getControlMode());
        context.put_b("filler.has_box", () -> container.tile != null && container.tile.hasBox());
        context.put_b("filler.is_finished", () -> container.tile != null && container.tile.isFinished());
        context.put_b("filler.is_locked", container::isLocked);
        context.put_l("filler.to_break", () -> container.tile == null ? 0 : container.tile.getCountToBreak());
        context.put_l("filler.to_place", () -> container.tile == null ? 0 : container.tile.getCountToPlace());
        properties.put("filler.possible", FillerStatementContext.CONTEXT_ALL);
        properties.put("filler.pattern", container.getPatternStatementClient());
        properties.put("filler.pattern.sprite", SPRITE_PATTERN);

        context.put_b("filler.invert", container::isInverted);
        properties.put("filler.invert", IButtonBehaviour.TOGGLE);
        properties.put("filler.invert", container.isInverted());
        properties.put("filler.invert",
            (IButtonClickEventListener) (b, k) -> container.sendInverted(b.isButtonActive()));

        context.put_b("filler.excavate", () -> container.tile != null && container.tile.canExcavate());
        properties.put("filler.excavate", IButtonBehaviour.TOGGLE);
        properties.put("filler.excavate", container.tile != null && container.tile.canExcavate());
        properties.put("filler.excavate",
            (IButtonClickEventListener) (b, k) -> {
                if (container.tile != null) {
                    container.tile.sendCanExcavate(b.isButtonActive());
                }
            });
    }

    @Override
    public void containerTick() {
        super.containerTick();
        IFillerPattern pattern = container.getPatternStatementClient().get();
        SPRITE_PATTERN.delegate = pattern == null ? null : pattern.getSprite();
        SPRITE_CONTROL_MODE.delegate = BCCoreSprites.ACTION_MACHINE_CONTROL.get(container.tile == null ? Mode.OFF : container.tile.getControlMode());
    }
}
