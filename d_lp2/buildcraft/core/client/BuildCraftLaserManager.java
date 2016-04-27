package buildcraft.core.client;

import buildcraft.lib.client.render.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.LaserData_BC8.LaserSide;
import buildcraft.lib.client.render.LaserData_BC8.LaserType;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BuildCraftLaserManager {
    public static final LaserType LASER_MARKER_VOLUME;
    public static final LaserType LASER_MARKER_PATH;

    static {
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_volume");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 2, 2);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 2);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 2, 16, 4), new LaserRow(sprite, 0, 4, 16, 6), new LaserRow(sprite, 0, 6, 16, 8), //
                new LaserRow(sprite, 0, 8, 16, 10), new LaserRow(sprite, 0, 10, 16, 12), new LaserRow(sprite, 0, 12, 16, 14) //
            };
            LaserRow end = new LaserRow(sprite, 0, 14, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 14, 14, 16, 16);
            LASER_MARKER_VOLUME = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftcore:lasers/marker_path");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 3, 3);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 3);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 3, 16, 6, LaserSide.TOP, LaserSide.BOTTOM),//
                new LaserRow(sprite, 0, 6, 16, 9, LaserSide.LEFT, LaserSide.RIGHT) //
            };
            LaserRow end = new LaserRow(sprite, 0, 12, 16, 15);
            LaserRow capEnd = new LaserRow(sprite, 13, 12, 16, 15);
            LASER_MARKER_PATH = new LaserType(capStart, start, middle, end, capEnd);
        }
    }

    public static void fmlPreInit() {
        // Maybe at some point this will do something
    }
}
