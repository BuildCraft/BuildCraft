package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.schematics.SchematicEntityFactory;
import buildcraft.api.schematics.SchematicEntityFactoryRegistry;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SchematicEntityManager {
    public static ISchematicEntity<?> getSchematicEntity(World world,
                                                         BlockPos basePos,
                                                         Entity entity) {
        SchematicEntityContext context = new SchematicEntityContext(world, basePos, entity);
        for (SchematicEntityFactory<?> schematicEntityFactory : Lists.reverse(SchematicEntityFactoryRegistry.getFactories())) {
            if (schematicEntityFactory.predicate.test(context)) {
                ISchematicEntity<?> schematicEntity = schematicEntityFactory.supplier.get();
                schematicEntity.init(context);
                return schematicEntity;
            }
        }
        return null;
    }

    public static void computeRequired(Blueprint blueprint) {
        FakeWorld world = FakeWorld.INSTANCE;
        world.uploadBlueprint(blueprint);
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
}
