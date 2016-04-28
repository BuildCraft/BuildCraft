package buildcraft.core.client;

import buildcraft.lib.client.render.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.LaserData_BC8.LaserSide;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BuildCraftLaserManager {

    public static final LaserType MARKER_VOLUME_CONNECTED;
    public static final LaserType MARKER_VOLUME_POSSIBLE;
    public static final LaserType MARKER_VOLUME_SIGNAL;
    public static final LaserType MARKER_PATH_CONNECTED;
    public static final LaserType MARKER_PATH_POSSIBLE;

    public static final LaserType MARKER_DEFAULT_POSSIBLE;

    static {
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_volume_connected");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 2, 2);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 2);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 2, 16, 4), new LaserRow(sprite, 0, 4, 16, 6), new LaserRow(sprite, 0, 6, 16, 8), //
                new LaserRow(sprite, 0, 8, 16, 10), new LaserRow(sprite, 0, 10, 16, 12), new LaserRow(sprite, 0, 12, 16, 14) //
            };
            LaserRow end = new LaserRow(sprite, 0, 14, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 14, 14, 16, 16);
            MARKER_VOLUME_CONNECTED = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_volume_signal");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 2, 2);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 2);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 2, 16, 4), new LaserRow(sprite, 0, 4, 16, 6), new LaserRow(sprite, 0, 6, 16, 8), //
                new LaserRow(sprite, 0, 8, 16, 10), new LaserRow(sprite, 0, 10, 16, 12), new LaserRow(sprite, 0, 12, 16, 14) //
            };
            LaserRow end = new LaserRow(sprite, 0, 14, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 14, 14, 16, 16);
            MARKER_VOLUME_SIGNAL = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_path_connected");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 3, 3);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 3);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 4, 16, 7, LaserSide.TOP, LaserSide.BOTTOM),//
                new LaserRow(sprite, 0, 8, 16, 11, LaserSide.LEFT, LaserSide.RIGHT) //
            };
            LaserRow end = new LaserRow(sprite, 0, 12, 16, 15);
            LaserRow capEnd = new LaserRow(sprite, 13, 12, 16, 15);
            MARKER_PATH_CONNECTED = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_volume_possible");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 4, 4);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 4);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 4, 16, 8),//
                new LaserRow(sprite, 0, 8, 16, 12)//
            };
            LaserRow end = new LaserRow(sprite, 0, 12, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 12, 12, 16, 16);
            MARKER_VOLUME_POSSIBLE = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_path_possible");
            MARKER_PATH_POSSIBLE = new LaserType(MARKER_VOLUME_POSSIBLE, sprite);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_default_possible");
            MARKER_DEFAULT_POSSIBLE = new LaserType(MARKER_VOLUME_POSSIBLE, sprite);
        }
    }

    public static void fmlPreInit() {
        // Maybe at some point this will do something
    }
}
