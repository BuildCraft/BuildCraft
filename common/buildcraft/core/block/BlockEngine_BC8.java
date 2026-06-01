/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// TODO(R.Chen): blocked by lib.engine.BlockEngineBase_BC8 (not yet migrated to Fabric 1.20.1)
package buildcraft.core.block;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.state.property.Property;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
    public BlockEngine_BC8(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    public Property<EnumEngineType> getEngineProperty() {
        return BuildCraftProperties.ENGINE_TYPE;
    }

    @Override
    public EnumEngineType getEngineType(int meta) {
        return EnumEngineType.fromMeta(meta);
    }

    @Override
    public String getUnlocalizedName(EnumEngineType engine) {
        return TagManager.get("block.engine.bc." + engine.unlocalizedTag, EnumTagType.UNLOCALIZED_NAME);
    }
}
