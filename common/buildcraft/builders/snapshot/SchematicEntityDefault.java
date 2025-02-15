/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchematicEntityDefault implements ISchematicEntity {
    private CompoundTag entityNbt;
    private Vec3 pos;
    private BlockPos hangingPos;
    private Direction hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    public static boolean predicate(SchematicEntityContext context) {
//        ResourceLocation registryName = EntityList.getKey(context.entity);
        ResourceLocation registryName = EntityUtil.getRegistryName(context.entity.getType());
        return registryName != null &&
//                RulesLoader.READ_DOMAINS.contains(registryName.getResourceDomain()) &&
                RulesLoader.READ_DOMAINS.contains(registryName.getNamespace()) &&
                RulesLoader.getRules(
//                                EntityList.getKey(context.entity),
                                EntityUtil.getRegistryName(context.entity.getType()),
                                context.entity.serializeNBT()
                        )
                        .stream()
                        .anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        entityNbt = context.entity.serializeNBT();
//        pos = context.entity.getPositionVector().subtract(new Vec3(context.basePos));
        pos = context.entity.position().subtract(Vec3.atLowerCornerOf(context.basePos));
        if (context.entity instanceof HangingEntity) {
//            EntityHanging entityHanging = (EntityHanging) context.entity;
            HangingEntity entityHanging = (HangingEntity) context.entity;
//            hangingPos = entityHanging.getHangingPosition().subtract(context.basePos);
            hangingPos = entityHanging.getPos().subtract(context.basePos);
//            hangingFacing = entityHanging.getHorizontalFacing();
            hangingFacing = entityHanging.getDirection();
        } else {
            hangingPos = BlockPos.containing(pos);
            hangingFacing = Direction.NORTH;
        }
    }

    @Override
    public Vec3 getPos() {
        return pos;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = RulesLoader.getRules(
                new ResourceLocation(entityNbt.getString("id")),
                entityNbt
        );
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("Rules are empty");
        }
        return rules.stream()
                .map(rule -> rule.requiredExtractors)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromEntity(entityNbt).stream())
                .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(
                new ResourceLocation(entityNbt.getString("id")),
                entityNbt
        );
        return rules.stream()
                .map(rule -> rule.requiredExtractors)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromEntity(entityNbt).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public SchematicEntityDefault getRotated(Rotation rotation) {
        SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
//        schematicEntity.entityRotation = entityRotation.add(rotation);
        schematicEntity.entityRotation = entityRotation.getRotated(rotation);
        return schematicEntity;
    }

    @Override
    public Entity build(Level world, BlockPos basePos) {
        Set<JsonRule> rules = RulesLoader.getRules(
                new ResourceLocation(entityNbt.getString("id")),
                entityNbt
        );
        CompoundTag replaceNbt = rules.stream()
                .map(rule -> rule.replaceNbt)
                .filter(Objects::nonNull)
                .map(Tag.class::cast)
                .reduce(NBTUtilBC::merge)
                .map(CompoundTag.class::cast)
                .orElse(null);
        Vec3 placePos = Vec3.atLowerCornerOf(basePos).add(pos);
        BlockPos placeHangingPos = basePos.offset(hangingPos);
        CompoundTag newEntityNbt = new CompoundTag();
        entityNbt.getAllKeys().stream()
                .map(key -> Pair.of(key, entityNbt.get(key)))
                .forEach(kv -> newEntityNbt.put(kv.getKey(), kv.getValue()));
        newEntityNbt.put("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.putUUID("UUID", UUID.randomUUID());
        boolean rotate = false;
        if (Stream.of("TileX", "TileY", "TileZ", "Facing").allMatch(newEntityNbt::contains)) {
            newEntityNbt.putInt("TileX", placeHangingPos.getX());
            newEntityNbt.putInt("TileY", placeHangingPos.getY());
            newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
            newEntityNbt.putByte("Facing", (byte) hangingFacing.get2DDataValue());
        } else {
            rotate = true;
        }
//        Entity entity = EntityList.createEntityFromNBT(
        Entity entity = EntityType.create(
                replaceNbt != null
                        ? (CompoundTag) NBTUtilBC.merge(newEntityNbt, replaceNbt)
                        : newEntityNbt,
                world
        ).orElse(null);
        if (entity != null) {
            if (rotate) {
//                entity.setLocationAndAngles(
                entity.setPos(
                        placePos.x,
                        placePos.y,
                        placePos.z

                );
                entity.setXRot(
//                        entity.rotationYaw + (entity.rotationYaw - entity.getRotatedYaw(entityRotation)),
                        entity.xRotO + (entity.xRotO - entity.rotate(entityRotation))

                );
                entity.setYRot(
//                        entity.rotationPitch
                        entity.yRotO
                );
            }
//            world.spawnEntity(entity);
            world.addFreshEntity(entity);
        }
        return entity;
    }

    @Override
    public Entity buildWithoutChecks(Level world, BlockPos basePos) {
        return build(world, basePos);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("entityNbt", entityNbt);
        nbt.put("pos", NBTUtilBC.writeVec3d(pos));
        nbt.put("hangingPos", NbtUtils.writeBlockPos(hangingPos));
        nbt.put("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.put("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        entityNbt = nbt.getCompound("entityNbt");
        pos = NBTUtilBC.readVec3d(nbt.get("pos"));
        hangingPos = NbtUtils.readBlockPos(nbt.getCompound("hangingPos"));
        hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
        entityRotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), Rotation.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicEntityDefault that = (SchematicEntityDefault) o;

        return entityNbt.equals(that.entityNbt) &&
                pos.equals(that.pos) &&
                hangingPos.equals(that.hangingPos) &&
                hangingFacing == that.hangingFacing &&
                entityRotation == that.entityRotation;
    }

    @Override
    public int hashCode() {
        int result = entityNbt.hashCode();
        result = 31 * result + pos.hashCode();
        result = 31 * result + hangingPos.hashCode();
        result = 31 * result + hangingFacing.hashCode();
        result = 31 * result + entityRotation.hashCode();
        return result;
    }
}
