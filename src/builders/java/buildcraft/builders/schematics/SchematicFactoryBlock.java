/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.core.blueprints.SchematicRegistry;

public class SchematicFactoryBlock extends SchematicFactory<SchematicBlock> {

    @Override
    protected SchematicBlock loadSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException {
        int blockId = nbt.getInteger("blockId");
        Block b = registry.getBlockForId(blockId);

        if (b == Blocks.air) {
            SchematicBlock s = new SchematicBlock();
            s.state = Blocks.air.getDefaultState();

            return s;
        } else {
            SchematicBlock s = SchematicRegistry.INSTANCE.createSchematicBlock(b.getStateFromMeta(nbt.getInteger("blockMeta")));

            if (s != null) {
                s.readSchematicFromNBT(nbt, registry);
                return s;
            }
        }

        return null;
    }

    @Override
    public void saveSchematicToWorldNBT(NBTTagCompound nbt, SchematicBlock object, MappingRegistry registry) {
        super.saveSchematicToWorldNBT(nbt, object, registry);

        nbt.setInteger("blockId", registry.getIdForBlock(object.state.getBlock()));
        object.writeSchematicToNBT(nbt, registry);
    }

}
