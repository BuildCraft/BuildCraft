/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicBlockFactory;
import buildcraft.api.schematics.SchematicBlockFactoryRegistry;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SchematicBlockManager {
    @SuppressWarnings("WeakerAccess")
    public static ISchematicBlock getSchematicBlock(SchematicBlockContext context) {
        for (SchematicBlockFactory<?> schematicBlockFactory : Lists.reverse(SchematicBlockFactoryRegistry.getFactories())) {
            if (schematicBlockFactory.predicate.test(context)) {
                ISchematicBlock schematicBlock = schematicBlockFactory.supplier.get();
                schematicBlock.init(context);
                return schematicBlock;
            }
        }
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("WeakerAccess")
    public static <S extends ISchematicBlock> S createCleanCopy(S schematicBlock) {
        return SchematicBlockFactoryRegistry
                .getFactoryByInstance(schematicBlock)
                .supplier
                .get();
    }

    @Nonnull
    public static <S extends ISchematicBlock> CompoundTag writeToNBT(S schematicBlock) {
        CompoundTag schematicBlockTag = new CompoundTag();
        schematicBlockTag.putString(
                "name",
                SchematicBlockFactoryRegistry
                        .getFactoryByInstance(schematicBlock)
                        .name
                        .toString()
        );
        schematicBlockTag.put("data", schematicBlock.serializeNBT());
        return schematicBlockTag;
    }

    @Nonnull
    public static ISchematicBlock readFromNBT(CompoundTag schematicBlockTag) throws InvalidInputDataException {
        ResourceLocation name = new ResourceLocation(schematicBlockTag.getString("name"));
        SchematicBlockFactory<?> factory = SchematicBlockFactoryRegistry.getFactoryByName(name);
        if (factory == null) {
            throw new InvalidInputDataException("Unknown schematic type " + name);
        }
        ISchematicBlock schematicBlock = factory.supplier.get();
        CompoundTag data = schematicBlockTag.getCompound("data");
        try {
            schematicBlock.deserializeNBT(data);
            return schematicBlock;
        } catch (InvalidInputDataException e) {
            throw new InvalidInputDataException("Failed to load the schematic from " + data, e);
        }
    }
}
