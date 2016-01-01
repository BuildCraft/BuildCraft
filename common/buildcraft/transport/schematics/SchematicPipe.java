/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.schematics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.blueprints.*;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe.SideProperties;

public class SchematicPipe extends SchematicTile {

    private BuildingPermission permission = BuildingPermission.ALL;

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        Pipe<?> pipe = BlockGenericPipe.getPipe(context.world(), pos);

        if (BlockGenericPipe.isValid(pipe)) {
            return pipe.item == Item.getItemById(tileNBT.getInteger("pipeId"));
        } else {
            return false;
        }
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        SideProperties props = new SideProperties();

        props.readFromNBT(tileNBT);
        props.rotateLeft();
        props.writeToNBT(tileNBT);

        Item pipeItem = Item.getItemById(tileNBT.getInteger("pipeId"));

        if (BptPipeExtension.contains(pipeItem)) {
            BptPipeExtension.get(pipeItem).rotateLeft(this, context);
        }

        if (tileNBT.hasKey("Gate")) {
            // This code is handling specifically BuildCraft 6.0 gates. Sided
            // gates starting BuildCraft 6.1 work differently.

            NBTTagCompound gateNBT = tileNBT.getCompoundTag("Gate");
            rotateGateLeft(gateNBT);
        } else {
            // Post 6.1 treatment

            NBTTagCompound[] gatesNBT = new NBTTagCompound[6];

            for (int i = 0; i < 6; ++i) {
                if (tileNBT.hasKey("Gate[" + i + "]")) {
                    gatesNBT[i] = tileNBT.getCompoundTag("Gate[" + i + "]");
                }
            }

            for (int i = 0; i < 6; ++i) {
                EnumFacing face = EnumFacing.VALUES[i];
                if (face.getAxis() != Axis.Y) {
                    face = face.rotateY();
                }

                int newI = face.ordinal();

                if (gatesNBT[i] != null) {
                    rotateGateLeft(gatesNBT[i]);
                    tileNBT.setTag("Gate[" + newI + "]", gatesNBT[i]);
                } else {
                    tileNBT.removeTag("Gate[" + newI + "]");
                }
            }
        }
    }

    private void rotateGateLeft(NBTTagCompound gateNBT) {
        for (int i = 0; i < Gate.MAX_STATEMENTS; ++i) {
            if (gateNBT.hasKey("trigger[" + i + "]")) {
                IStatement t = StatementManager.statements.get(gateNBT.getString("trigger[" + i + "]"));
                t = t.rotateLeft();
                gateNBT.setString("trigger[" + i + "]", t.getUniqueTag());
            }

            if (gateNBT.hasKey("action[" + i + "]")) {
                IStatement a = StatementManager.statements.get(gateNBT.getString("action[" + i + "]"));
                a = a.rotateLeft();
                gateNBT.setString("action[" + i + "]", a.getUniqueTag());
            }

            for (int j = 0; j < Gate.MAX_PARAMETERS; ++j) {
                if (gateNBT.hasKey("triggerParameters[" + i + "][" + j + "]")) {
                    NBTTagCompound cpt = gateNBT.getCompoundTag("triggerParameters[" + i + "][" + j + "]");
                    IStatementParameter parameter = StatementManager.createParameter(cpt.getString("kind"));
                    parameter.readFromNBT(cpt);

                    parameter = parameter.rotateLeft();

                    parameter.writeToNBT(cpt);
                    gateNBT.setTag("triggerParameters[" + i + "][" + j + "]", cpt);
                }

                if (gateNBT.hasKey("actionParameters[" + i + "][" + j + "]")) {
                    NBTTagCompound cpt = gateNBT.getCompoundTag("actionParameters[" + i + "][" + j + "]");
                    IStatementParameter parameter = StatementManager.createParameter(cpt.getString("kind"));
                    parameter.readFromNBT(cpt);

                    parameter = parameter.rotateLeft();

                    parameter.writeToNBT(cpt);
                    gateNBT.setTag("actionParameters[" + i + "][" + j + "]", cpt);
                }
            }
        }

        if (gateNBT.hasKey("direction")) {
            EnumFacing face = EnumFacing.VALUES[gateNBT.getInteger("direction")];
            if (face.getAxis() != Axis.Y) {
                face = face.rotateY();
            }

            gateNBT.setInteger("direction", face.ordinal());
        }
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        tileNBT.setInteger("x", pos.getX());
        tileNBT.setInteger("y", pos.getY());
        tileNBT.setInteger("z", pos.getZ());

        context.world().setBlockState(pos, state, 3);

        TileEntity tile = context.world().getTileEntity(pos);
        tile.readFromNBT(tileNBT);
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        TileEntity tile = context.world().getTileEntity(pos);
        Pipe<?> pipe = BlockGenericPipe.getPipe(context.world(), pos);

        if (BlockGenericPipe.isValid(pipe)) {
            tile.writeToNBT(tileNBT);

            // remove all pipe contents

            tileNBT.removeTag("travelingEntities");

            for (EnumFacing direction : EnumFacing.values()) {
                tileNBT.removeTag("tank[" + direction.ordinal() + "]");
                tileNBT.removeTag("transferState[" + direction.ordinal() + "]");
            }

            for (int i = 0; i < 6; ++i) {
                tileNBT.removeTag("powerQuery[" + i + "]");
                tileNBT.removeTag("nextPowerQuery[" + i + "]");
                tileNBT.removeTag("internalPower[" + i + "]");
                tileNBT.removeTag("internalNextPower[" + i + "]");
            }
        }
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        Pipe<?> pipe = BlockGenericPipe.getPipe(context.world(), pos);

        if (BlockGenericPipe.isValid(pipe)) {
            ArrayList<ItemStack> items = pipe.computeItemDrop();
            storedRequirements = new ItemStack[items.size() + 1];
            items.toArray(storedRequirements);
            storedRequirements[storedRequirements.length - 1] = new ItemStack(pipe.item, 1, pipe.container.getItemMetadata());
        }
    }

    @Override
    public void postProcessing(IBuilderContext context, BlockPos pos) {
        Item pipeItem = Item.getItemById(tileNBT.getInteger("pipeId"));

        if (BptPipeExtension.contains(pipeItem)) {
            BptPipeExtension.get(pipeItem).postProcessing(this, context);
        }
    }

    @Override
    public BuildingStage getBuildStage() {
        return BuildingStage.STANDALONE;
    }

    @Override
    public void idsToBlueprint(MappingRegistry registry) {
        super.idsToBlueprint(registry);

        if (tileNBT.hasKey("pipeId")) {
            Item item = Item.getItemById(tileNBT.getInteger("pipeId"));

            tileNBT.setInteger("pipeId", registry.getIdForItem(item));
        }
    }

    @Override
    public void idsToWorld(MappingRegistry registry) {
        super.idsToWorld(registry);

        if (tileNBT.hasKey("pipeId")) {
            try {
                Item item = registry.getItemForId(tileNBT.getInteger("pipeId"));

                tileNBT.setInteger("pipeId", Item.getIdFromItem(item));
            } catch (MappingNotFoundException e) {
                tileNBT.removeTag("pipeId");
            }
        }
    }

    @Override
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.writeSchematicToNBT(nbt, registry);
        nbt.setInteger("version", 2);
    }

    @Override
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.readSchematicFromNBT(nbt, registry);

        if (!nbt.hasKey("version") || nbt.getInteger("version") < 2) {
            // Schematics previous to the fixes in version 2 had item id
            // translation badly broken. We need to flush out information that
            // would be otherwise corrupted - that is the inventory (with the
            // old formalism "items") and gate parameters.
            tileNBT.removeTag("items");

            if (tileNBT.hasKey("Gate")) {
                NBTTagCompound gateNBT = tileNBT.getCompoundTag("Gate");

                for (int i = 0; i < 8; ++i) {
                    if (gateNBT.hasKey("triggerParameters[" + i + "]")) {
                        NBTTagCompound parameterNBT = gateNBT.getCompoundTag("triggerParameters[" + i + "]");

                        if (parameterNBT.hasKey("stack")) {
                            parameterNBT.removeTag("stack");
                        }
                    }
                }
            }
        }
    }

    @Override
    public BuildingPermission getBuildingPermission() {
        return permission;
    }
}
