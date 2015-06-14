/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.core.IBox;
import buildcraft.core.Box;

public class BptContext implements IBuilderContext {

    public BlueprintReadConfiguration readConfiguration;
    public Box box;
    public World world;
    private MappingRegistry mappingRegistry;

    BptContext(World world, Box box, MappingRegistry registry) {
        this.world = world;
        this.box = box;
        this.mappingRegistry = registry;
    }

    @Override
    public Vec3 rotatePositionLeft(Vec3 pos) {
        return new Vec3((box.sizeZ() - 1) - pos.zCoord, pos.yCoord, pos.xCoord);
    }

    @Override
    public IBox surroundingBox() {
        return box;
    }

    @Override
    public World world() {
        return world;
    }

    public void rotateLeft() {
        box = box.rotateLeft();
    }

    @Override
    public MappingRegistry getMappingRegistry() {
        return mappingRegistry;
    }
}
