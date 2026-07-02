/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicEntity;
import ct.buildcraft.api.schematics.SchematicEntityContext;
import ct.buildcraft.api.schematics.SchematicEntityFactory;
import ct.buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class SchematicEntityManager {
    public static ISchematicEntity getSchematicEntity(SchematicEntityContext context) {
        for (SchematicEntityFactory<?> schematicEntityFactory : Lists.reverse(SchematicEntityFactoryRegistry.getFactories())) {
            if (schematicEntityFactory.predicate.test(context)) {
                ISchematicEntity schematicEntity = schematicEntityFactory.supplier.get();
                schematicEntity.init(context);
                return schematicEntity;
            }
        }
        return null;
    }

    public static <S extends ISchematicEntity> S createCleanCopy(S schematicBlock) {
        return SchematicEntityFactoryRegistry
            .getFactoryByInstance(schematicBlock)
            .supplier
            .get();
    }

    @Nonnull
    public static <S extends ISchematicEntity> CompoundTag writeToNBT(S schematicEntity) {
        CompoundTag schematicEntityTag = new CompoundTag();
        schematicEntityTag.putString(
            "name",
            SchematicEntityFactoryRegistry
                .getFactoryByInstance(schematicEntity)
                .name
                .toString()
        );
        schematicEntityTag.put("data", schematicEntity.serializeNBT());
        return schematicEntityTag;
    }

    @Nonnull
    public static ISchematicEntity readFromNBT(CompoundTag schematicEntityTag) throws InvalidInputDataException {
        ResourceLocation name = new ResourceLocation(schematicEntityTag.getString("name"));
        SchematicEntityFactory<?> factory = SchematicEntityFactoryRegistry.getFactoryByName(name);
        if (factory == null) {
            throw new InvalidInputDataException("Unknown schematic type " + name);
        }
        ISchematicEntity schematicEntity = factory.supplier.get();
        CompoundTag data = schematicEntityTag.getCompound("data");
        try {
            schematicEntity.deserializeNBT(data);
            return schematicEntity;
        } catch (InvalidInputDataException e) {
            throw new InvalidInputDataException("Failed to load the schematic from " + data, e);
        }
    }
}
