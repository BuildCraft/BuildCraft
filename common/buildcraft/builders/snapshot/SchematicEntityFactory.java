package buildcraft.builders.snapshot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SchematicEntityFactory {
    private static Set<JsonRule> getRules(Entity entity) {
        //noinspection ConstantConditions
        return RulesLoader.INSTANCE.rules.stream()
                .filter(rule -> rule.selectors != null)
                .filter(rule -> rule.selectors.stream().anyMatch(EntityList.getKey(entity).toString()::equals))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static SchematicEntity getSchematicEntity(
            World world,
            BlockPos basePos,
            Entity entity
    ) {
        SchematicEntity schematicEntity = new SchematicEntity();
        boolean ignore = false;
        ResourceLocation registryName = EntityList.getKey(entity);
        if (registryName == null) {
            ignore = true;
        }
        if (!ignore) {
            if (!RulesLoader.INSTANCE.readDomains.contains(registryName.getResourceDomain())) {
                ignore = true;
            }
        }
        if (!ignore) {
            Set<JsonRule> rules = getRules(entity);
            if (rules.stream().noneMatch(rule -> rule.capture)) {
                ignore = true;
            }
        }
        if (!ignore) {
            schematicEntity.entityNbt = entity.serializeNBT();
            schematicEntity.pos = entity.getPositionVector().subtract(new Vec3d(basePos));
            if (entity instanceof EntityHanging) {
                EntityHanging entityHanging = (EntityHanging) entity;
                schematicEntity.hangingPos = entityHanging.getHangingPosition().subtract(basePos);
                schematicEntity.hangingFacing = entityHanging.getHorizontalFacing();
            } else {
                schematicEntity.hangingPos = new BlockPos(schematicEntity.pos);
                schematicEntity.hangingFacing = EnumFacing.NORTH;
            }
        }
        if (ignore) {
            schematicEntity = null;
        }
        return schematicEntity;
    }
}
