/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.dimension.FakeWorldServer;

public class SchematicBlockDefaultState extends SchematicBlockDefault {

    public static boolean predicate(SchematicBlockContext context) {
        ResourceLocation registryName = context.block.getRegistryName();
        return registryName != null && RulesLoader.getRules(context.blockState).stream().noneMatch(rule -> rule.ignore) &&
                context.block.hasTileEntity(context.blockState) && FakeWorldServer.INSTANCE.isAcceptableForBlueprint(context);
    }

    @Override
    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = new NBTTagCompound();
    }



}
