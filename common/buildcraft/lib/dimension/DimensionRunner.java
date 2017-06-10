/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

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

import buildcraft.builders.snapshot.Blueprint;

public class DimensionRunner extends Thread {
    private final FakeWorldServer fakeServer;
    public static final ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();

    public DimensionRunner(FakeWorldServer fakeServer) {
        super("Blueprint calculator");
        this.fakeServer = fakeServer;
    }

    public static void addToQueue(Blueprint toScan, Consumer<BuildingInfoData> callback) {
        queue.add(new Task(toScan, callback));
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task = queue.poll();
                if (task != null) {
                    Blueprint blueprint = task.blueprint;
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
                    fakeServer.uploadBlueprint(blueprint, true);
                    fakeServer.editable = false;
                    for (int z = 0; z < blueprint.size.getZ(); z++) {
                        for (int y = 0; y < blueprint.size.getY(); y++) {
                            for (int x = 0; x < blueprint.size.getX(); x++) {
                                BlockPos pos = new BlockPos(x, y, z).add(FakeWorldServer.BLUEPRINT_OFFSET);
                                ISchematicBlock<?> schematicBlock = blueprint.palette.get(
                                        blueprint.data
                                                [pos.getX() - FakeWorldServer.BLUEPRINT_OFFSET.getX()]
                                                [pos.getY() - FakeWorldServer.BLUEPRINT_OFFSET.getY()]
                                                [pos.getZ() - FakeWorldServer.BLUEPRINT_OFFSET.getZ()]
                                );
                                IBlockState blockState = fakeServer.getBlockState(pos);
                                Block block = blockState.getBlock();
                                SchematicBlockContext schematicBlockContext = new SchematicBlockContext(
                                        fakeServer,
                                        FakeWorldServer.BLUEPRINT_OFFSET,
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
                        Entity entity = schematicEntity.buildWithoutChecks(fakeServer, FakeWorldServer.BLUEPRINT_OFFSET);
                        if (entity != null) {
                            fakeServer.editable = false;
                            SchematicEntityContext schematicEntityContext = new SchematicEntityContext(fakeServer, FakeWorldServer.BLUEPRINT_OFFSET, entity);
                            requiredItemsForEntities.set(i, schematicEntity.computeRequiredItems(schematicEntityContext));
                            requiredFluidsForEntities.set(i, schematicEntity.computeRequiredFluids(schematicEntityContext));
                            fakeServer.editable = true;
                            fakeServer.removeEntity(entity);
                        }
                        i++;
                    }

                    fakeServer.editable = true;
                    fakeServer.clear();
                    task.callback.accept(new BuildingInfoData(Pair.of(requiredItems, requiredFluids), Pair.of(requiredItemsForEntities, requiredFluidsForEntities)));
                } else {
                    sleep(500);
                }
            }
        } catch (InterruptedException ex) {
        }

    }

    public void terminate() {
        queue.clear();
        stop();
    }

    public static class Task {
        public final Blueprint blueprint;
        public final Consumer<BuildingInfoData> callback;

        public Task(Blueprint blueprint, Consumer<BuildingInfoData> callback) {
            this.blueprint = blueprint;
            this.callback = callback;
        }
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
