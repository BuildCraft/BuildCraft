/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicEntityContext;

import buildcraft.lib.misc.data.Box;

import buildcraft.builders.snapshot.Blueprint;

public class BlueprintCalculator implements Callable<BlueprintCalculator.BuildingInfoData> {
    private final Blueprint blueprint;

    public BlueprintCalculator(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public BuildingInfoData call() throws Exception {
        FakeWorldServer fakeServer = FakeWorldServer.INSTANCE;
        @SuppressWarnings("unchecked")
        List<ItemStack>[][][] requiredItems = (List<ItemStack>[][][]) new List
                [blueprint.size.getX()]
                [blueprint.size.getY()]
                [blueprint.size.getZ()];
        @SuppressWarnings("unchecked")
        List<FluidStack>[][][] requiredFluids = (List<FluidStack>[][][]) new List
                [blueprint.size.getX()]
                [blueprint.size.getY()]
                [blueprint.size.getZ()];
        BlueprintLocationManager.BlueprintLocation location = BlueprintLocationManager.getLocationFor(blueprint);
        fakeServer.uploadBlueprint(blueprint, location, true);
        Box box = new Box(location.startPos, location.startPos.add(location.size));
        fakeServer.unlock(box);
        for (int z = 0; z < blueprint.size.getZ(); z++) {
            for (int y = 0; y < blueprint.size.getY(); y++) {
                for (int x = 0; x < blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(location.startPos);
                    ISchematicBlock<?> schematicBlock = blueprint.palette.get(
                            blueprint.data
                                    [pos.getX() - location.startPos.getX()]
                                    [pos.getY() - location.startPos.getY()]
                                    [pos.getZ() - location.startPos.getZ()]
                    );
                    IBlockState blockState = fakeServer.getBlockState(pos);
                    Block block = blockState.getBlock();
                    SchematicBlockContext schematicBlockContext = new SchematicBlockContext(
                            fakeServer,
                            location.startPos,
                            pos,
                            blockState,
                            block
                    );
                    requiredItems[x][y][z] =
                            schematicBlock.computeRequiredItems(schematicBlockContext);
                    requiredFluids[x][y][z] =
                            schematicBlock.computeRequiredFluids(schematicBlockContext);
                }
            }

        }

        List<List<ItemStack>> requiredItemsForEntities = new ArrayList<>(
                Collections.nCopies(
                        blueprint.entities.size(),
                        Collections.emptyList()
                )
        );
        List<List<FluidStack>> requiredFluidsForEntities = new ArrayList<>(
                Collections.nCopies(
                        blueprint.entities.size(),
                        Collections.emptyList()
                )
        );

        int i = 0;
        for (ISchematicEntity<?> schematicEntity : blueprint.entities) {
            Entity entity = schematicEntity.buildWithoutChecks(fakeServer, location.startPos);
            if (entity != null) {
                fakeServer.lock(box);
                SchematicEntityContext schematicEntityContext = new SchematicEntityContext(fakeServer, location.startPos, entity);
                requiredItemsForEntities.set(i, schematicEntity.computeRequiredItems(schematicEntityContext));
                requiredFluidsForEntities.set(i, schematicEntity.computeRequiredFluids(schematicEntityContext));
                fakeServer.unlock(box);
                fakeServer.removeEntity(entity);
            }
            i++;
        }

        fakeServer.clear(location);
        BlueprintLocationManager.releaseLocation(location);
        return new BuildingInfoData(Pair.of(requiredItems, requiredFluids), Pair.of(requiredItemsForEntities, requiredFluidsForEntities));
    }

    public static class BuildingInfoData {
        public final Pair<List<ItemStack>[][][], List<FluidStack>[][][]> blockRequirements;
        public final Pair<List<List<ItemStack>>, List<List<FluidStack>>> entityRequirements;

        public BuildingInfoData(Pair<List<ItemStack>[][][], List<FluidStack>[][][]> blockRequirements, Pair<List<List<ItemStack>>, List<List<FluidStack>>> entityRequirements) {
            this.blockRequirements = blockRequirements;
            this.entityRequirements = entityRequirements;
        }
    }
}
