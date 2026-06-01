/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;

public class SchematicEntityDefault implements ISchematicEntity {
    private NbtCompound entityNbt;
    private Vec3d pos;
    private BlockPos hangingPos;
    private Direction hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    public static boolean predicate(SchematicEntityContext context) {
        Identifier registryName = EntityList.getKey(context.entity);
        return registryName != null &&
            RulesLoader.READ_DOMAINS.contains(registryName.getNamespace()) &&
            RulesLoader.getRules(
                EntityList.getKey(context.entity),
                context.entity.createNbt()
            )
                .stream()
                .anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        entityNbt = context.entity.createNbt();
        pos = context.entity.getPos().subtract(new Vec3d(context.basePos.getX(), context.basePos.getY(), context.basePos.getZ()));
        if (context.entity instanceof EntityHanging) {
            EntityHanging entityHanging = (EntityHanging) context.entity;
            hangingPos = entityHanging.getHangingPosition().subtract(context.basePos);
            hangingFacing = entityHanging.getHorizontalFacing();
        } else {
            hangingPos = new BlockPos(pos);
            hangingFacing = Direction.NORTH;
        }
    }

    @Override
    public Vec3d getPos() {
        return pos;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = RulesLoader.getRules(
            new Identifier(entityNbt.getString("id")),
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
    public List<FluidStackBC> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(
            new Identifier(entityNbt.getString("id")),
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
    public SchematicEntityDefault getRotated(net.minecraft.util.BlockRotation rotation) {
        SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.add(rotation);
        return schematicEntity;
    }

    @Override
    public Entity build(World world, BlockPos basePos) {
        Set<JsonRule> rules = RulesLoader.getRules(
            new Identifier(entityNbt.getString("id")),
            entityNbt
        );
        NbtCompound replaceNbt = rules.stream()
            .map(rule -> rule.replaceNbt)
            .filter(Objects::nonNull)
            .map(NbtElement.class::cast)
            .reduce(NBTUtilBC::merge)
            .map(NbtCompound.class::cast)
            .orElse(null);
        Vec3d placePos = new Vec3d(basePos.getX(), basePos.getY(), basePos.getZ()).add(pos);
        BlockPos placeHangingPos = basePos.add(hangingPos);
        NbtCompound newEntityNbt = new NbtCompound();
        entityNbt.getKeySet().stream()
            .map(key -> Pair.of(key, entityNbt.get(key)))
            .forEach(kv -> newEntityNbt.put(kv.getKey(), kv.getValue()));
        newEntityNbt.put("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.setUniqueId("UUID", UUID.randomUUID());
        boolean rotate = false;
        if (Stream.of("TileX", "TileY", "TileZ", "Facing").allMatch(newEntityNbt::hasKey)) {
            newEntityNbt.putInt("TileX", placeHangingPos.getX());
            newEntityNbt.putInt("TileY", placeHangingPos.getY());
            newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
            newEntityNbt.putByte("Facing", (byte) hangingFacing.getHorizontalIndex());
        } else {
            rotate = true;
        }
        Entity entity = EntityList.createEntityFromNBT(
            replaceNbt != null
                ? (NbtCompound) NBTUtilBC.merge(newEntityNbt, replaceNbt)
                : newEntityNbt,
            world
        );
        if (entity != null) {
            if (rotate) {
                entity.setLocationAndAngles(
                    placePos.x,
                    placePos.y,
                    placePos.z,
                    entity.rotationYaw + (entity.rotationYaw - entity.getRotatedYaw(entityRotation)),
                    entity.rotationPitch
                );
            }
            world.spawnEntity(entity);
        }
        return entity;
    }

    @Override
    public Entity buildWithoutChecks(World world, BlockPos basePos) {
        return build(world, basePos);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public NbtCompound createNbt() { return serializeNBT(); }
    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("entityNbt", entityNbt);
        nbt.put("pos", NBTUtilBC.writeVec3d(pos));
        nbt.put("hangingPos", net.minecraft.nbt.NbtHelper.fromBlockPos(hangingPos));
        nbt.put("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.put("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) throws InvalidInputDataException {
        entityNbt = nbt.getCompound("entityNbt");
        pos = NBTUtilBC.readVec3d(nbt.get("pos"));
        hangingPos = net.minecraft.nbt.NbtHelper.toBlockPos(nbt.getCompound("hangingPos"));
        hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
        entityRotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), net.minecraft.util.BlockRotation.class);
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
