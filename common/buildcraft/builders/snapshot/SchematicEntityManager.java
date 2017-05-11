package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.schematics.SchematicEntityFactory;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SchematicEntityManager {
    public static ISchematicEntity<?> getSchematicEntity(SchematicEntityContext context) {
        for (SchematicEntityFactory<?> schematicEntityFactory : Lists.reverse(SchematicEntityFactoryRegistry.getFactories())) {
            if (schematicEntityFactory.predicate.test(context)) {
                ISchematicEntity<?> schematicEntity = schematicEntityFactory.supplier.get();
                schematicEntity.init(context);
                return schematicEntity;
            }
        }
        return null;
    }

    public static ISchematicEntity<?> getSchematicEntity(World world,
                                                         BlockPos basePos,
                                                         Entity entity) {
        SchematicEntityContext context = new SchematicEntityContext(world, basePos, entity);
        ISchematicEntity<?> schematicEntity = getSchematicEntity(context);
        if (schematicEntity != null) return schematicEntity;
        return null;
    }

    public static void computeRequired(Blueprint blueprint) {
        FakeWorld world = FakeWorld.INSTANCE;
        world.uploadBlueprint(blueprint, true);
        for (ISchematicEntity<?> schematicEntity : blueprint.entities) {
            Entity entity = schematicEntity.buildWithoutChecks(world, FakeWorld.BLUEPRINT_OFFSET);
            if (entity != null) {
                world.editable = false;
                schematicEntity.computeRequiredItemsAndFluids(new SchematicEntityContext(world, FakeWorld.BLUEPRINT_OFFSET, entity));
                world.editable = true;
                world.removeEntity(entity);
            }
        }
        world.clear();
    }

    @Nonnull
    public static NBTTagCompound writeToNBT(ISchematicEntity<?> schematicEntity) {
        NBTTagCompound schematicEntityTag = new NBTTagCompound();
        schematicEntityTag.setString(
                "name",
                SchematicEntityFactoryRegistry
                        .getFactoryByInstance(schematicEntity)
                        .name
                        .toString()
        );
        schematicEntityTag.setTag("data", schematicEntity.serializeNBT());
        return schematicEntityTag;
    }

    @Nonnull
    public static ISchematicEntity<?> readFromNBT(NBTTagCompound schematicEntityTag) {
        ISchematicEntity<?> schematicEntity = SchematicEntityFactoryRegistry
                .getFactoryByName(new ResourceLocation(schematicEntityTag.getString("name")))
                .supplier
                .get();
        schematicEntity.deserializeNBT(schematicEntityTag.getCompoundTag("data"));
        return schematicEntity;
    }
}
