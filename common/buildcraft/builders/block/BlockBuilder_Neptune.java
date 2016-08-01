/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import buildcraft.api.enums.EnumBlueprintType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class BlockBuilder_Neptune extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<EnumBlueprintType> BLUEPRINT_TYPE = BuildCraftProperties.BLUEPRINT_TYPE;

    public BlockBuilder_Neptune(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(BLUEPRINT_TYPE, EnumBlueprintType.NONE));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(BLUEPRINT_TYPE);
    }

}
