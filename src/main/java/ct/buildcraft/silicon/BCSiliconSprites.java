package ct.buildcraft.silicon;

import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import ct.buildcraft.lib.misc.ColourUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.MinecraftForge;

public class BCSiliconSprites {

    public static final SpriteHolder TRIGGER_LIGHT_LOW;
    public static final SpriteHolder TRIGGER_LIGHT_HIGH;

    public static final SpriteHolder ACTION_PULSAR_CONSTANT;
    public static final SpriteHolder ACTION_PULSAR_SINGLE;
    public static final SpriteHolder[] ACTION_PIPE_COLOUR;

    static {
        TRIGGER_LIGHT_LOW = getHolder("triggers/trigger_light_dark");
        TRIGGER_LIGHT_HIGH = getHolder("triggers/trigger_light_bright");

        ACTION_PULSAR_CONSTANT = getHolder("triggers/action_pulsar_on");
        ACTION_PULSAR_SINGLE = getHolder("triggers/action_pulsar_single");
        ACTION_PIPE_COLOUR = new SpriteHolder[ColourUtil.COLOURS.length];
        for (DyeColor colour : DyeColor.values()) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "items/paintbrush/" + colour.getName());
        }
    }

    private static SpriteHolder getHolder(String loc) {
        return SpriteHolderRegistry.getHolder("buildcraftsilicon:" + loc);
    }

    private static SpriteHolder getHolder(String module, String loc) {
        return SpriteHolderRegistry.getHolder("buildcraft" + module + ":" + loc);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCSiliconSprites.class);
    }

}
