/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.schematics;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.properties.BuildCraftProperties;

public class BptPipeWooden extends BptPipeExtension {

    public BptPipeWooden(Item i) {
        super(i);
    }

    @Override
    public void rotateLeft(SchematicTile slot, IBuilderContext context) {
        int meta = BuildCraftProperties.GENERIC_PIPE_DATA.getValue(slot.state);
        int orientation = meta & 7;
        int others = meta - orientation;

        EnumFacing face = EnumFacing.values()[orientation];

        if (face.getAxis() != Axis.Y) {
            face = face.rotateY();
        }

        meta = face.ordinal() + others;
        slot.state = slot.state.withProperty(BuildCraftProperties.GENERIC_PIPE_DATA, meta);
    }

}
