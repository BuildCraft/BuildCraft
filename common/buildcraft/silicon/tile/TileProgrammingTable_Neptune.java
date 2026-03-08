/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.net.IMessage;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerProgrammingTable_Neptune;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TileProgrammingTable_Neptune extends TileLaserTableBase implements IHasWork {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("assembly_table");
    public static final int NET_RECIPE_STATE = IDS.allocId("RECIPE_STATE");

    public static final int WIDTH = 6;
    public static final int HEIGHT = 4;

    // public String currentRecipeId = "";
    @Nonnull
    public final List<ResourceLocation> availableRecipeIds = Lists.newArrayList();
    // public IProgrammingRecipe currentRecipe;
    @Nonnull
    // public List<ItemStack> options = List.of();
    public List<IProgrammingRecipe> optionRecipes = List.of();
    public int optionId;
    private boolean queuedNetworkUpdate = false;

    public final ItemHandlerSimple input = itemManager.addInvHandler(
            "input",
            1,
            ItemHandlerManager.EnumAccess.BOTH,
            EnumPipePart.VALUES
    );

    public final ItemHandlerSimple output = itemManager.addInvHandler(
            "output",
            1,
            ItemHandlerManager.EnumAccess.EXTRACT,
            EnumPipePart.VALUES
    );

    private void queueNetworkUpdate() {
        queuedNetworkUpdate = true;
    }

    public TileProgrammingTable_Neptune(BlockPos pos, BlockState blockState) {
        super(BCSiliconBlocks.programmingTableTile.get(), pos, blockState);
    }

    @Override
    public void update() {
        super.update();

        if (level.isClientSide) {
            return;
        }

        if (queuedNetworkUpdate) {
            // sendNetworkUpdate();
            sendNetworkUpdate(NET_GUI_DATA);
            queuedNetworkUpdate = false;
        }

        // if (currentRecipe == null)
        if (optionRecipes.isEmpty()) {
            return;
        }

        // if (this.getStackInSlot(0) == null)
        if (this.input.getStackInSlot(0).isEmpty()) {
            // currentRecipe = null;
            optionRecipes = List.of();
            return;
        }

        // if (optionId >= 0 && getEnergy() >= currentRecipe.getEnergyCost(options.get(optionId)))
        if (optionId >= 0 && power >= optionRecipes.get(optionId).getEnergyCost()) {
            // if (currentRecipe.canCraft(this.getStackInSlot(0)))
            if (optionRecipes.get(optionId).canCraft(this.input.getStackInSlot(0))) {
                // ItemStack remaining = currentRecipe.craft(this.getStackInSlot(0), options.get(optionId));
                ItemStack remaining = optionRecipes.get(optionId).craft(this.input.getStackInSlot(0));
                if (remaining != null && remaining.getCount() > 0) {
                    // setEnergy(0);
                    power = 0;
                    // decrStackSize(0, remaining.stackSize);
                    this.input.extractItem(0, remaining.getCount(), false);
                    // outputStack(remaining, this, 1, false);
                    outputStack(remaining, this.output, 0);
                }
            }
            findRecipe();
        }
    }

    protected void outputStack(ItemStack remaining, ItemHandlerSimple inv, int slot) {
        if (inv != null && !remaining.isEmpty()) {
            ItemStack inside = inv.getStackInSlot(slot);

            if (inside.isEmpty() || inside.getCount() <= 0) {
                inv.setStackInSlot(slot, remaining);
                return;
            } else if (StackUtil.canMerge(inside, remaining)) {
                remaining.shrink(StackUtil.mergeStacks(remaining, inside, true));
            }
            InventoryUtil.addToBestAcceptor(level, getBlockPos(), null, remaining.copy());
        }
    }

//    /* IINVENTORY */
//    @Override
//    public int getSizeInventory() {
//        return 2;
//    }

//    @Override
//    public void setInventorySlotContents(int slot, ItemStack stack) {
//        super.setInventorySlotContents(slot, stack);
//
//        if (slot == 0) {
//            findRecipe();
//        }
//    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);

        // if (slot == 0)
        if (handler == input && !StackUtil.isSameItemSameDamageSameTagSameCount(before, after)) {
            findRecipe();
        }
    }

//    @Override
//    public String getInventoryName() {
//        return BCStringUtils.localize("tile.programmingTableBlock.name");
//    }

//    @Override
//    public void readData(ByteBuf stream) {
//        super.readData(stream);
//        currentRecipeId = NetworkUtils.readUTF(stream);
//        optionId = stream.readByte();
//        updateRecipe();
//    }

//    @Override
//    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
//        if (side.isServer() && "select".equals(command)) {
//            optionId = stream.readByte();
//            if (optionId >= options.size()) {
//                optionId = -1;
//            } else if (optionId < -1) {
//                optionId = -1;
//            }
//
//            queueNetworkUpdate();
//        }
//    }

    @Override
    public void readPayload(int id, PacketBufferBC stream, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, stream, side, ctx);

        if (id == NET_GUI_DATA) {
            // currentRecipeId = stream.readUtf();
            if (stream.readBoolean()) {
                // currentRecipeId = stream.readResourceLocation();
                int size = stream.readInt();
                availableRecipeIds.clear();
                for (int i = 0; i < size; i++) {
                    availableRecipeIds.add(stream.readResourceLocation());
                }
            } else {
                // currentRecipeId = null;
                availableRecipeIds.clear();
            }
            optionId = stream.readByte();
            updateRecipe();
        } else if (side == NetworkDirection.PLAY_TO_SERVER && id == NET_RECIPE_STATE) {
            optionId = stream.readByte();
            // if (optionId >= options.size())
            if (optionId >= optionRecipes.size()) {
                optionId = -1;
            } else if (optionId < -1) {
                optionId = -1;
            }

            queueNetworkUpdate();
        }
    }

    @Override
    // public void writeData(ByteBuf stream)
    public void writePayload(int id, PacketBufferBC stream, Dist side) {
        // super.writeData(stream);
        super.writePayload(id, stream, side);
        if (id == NET_GUI_DATA) {
            // stream.writeUtf(currentRecipeId);
            stream.writeBoolean(!availableRecipeIds.isEmpty());
            // if (currentRecipeId != null)
            if (!availableRecipeIds.isEmpty()) {
                stream.writeInt(availableRecipeIds.size());
                // stream.writeResourceLocation(currentRecipeId);
                availableRecipeIds.forEach(stream::writeResourceLocation);
            }
            stream.writeByte(optionId);
        }
    }

    @Override
    // public void readFromNBT(NBTTagCompound nbt)
    public void load(CompoundTag nbt) {
        // super.readFromNBT(nbt);
        super.load(nbt);

        if (nbt.contains("recipeId") && nbt.contains("optionId")) {
            // currentRecipeId = nbt.getString("recipeId");
            ListTag recipeIdTag = nbt.getList("recipeId", Tag.TAG_STRING);
            availableRecipeIds.addAll(NBTUtilBC.readStringList(recipeIdTag).map(ResourceLocation::new).collect(Collectors.toList()));
            optionId = nbt.getByte("optionId");
        } else {
            // currentRecipeId = null;
            availableRecipeIds.clear();
        }
        // updateRecipe();
        runWhenWorldNotNull(this::updateRecipe, false);
    }

    @Override
    // public void writeToNBT(NBTTagCompound nbt)
    public void saveAdditional(CompoundTag nbt) {
        // super.writeToNBT(nbt);
        super.saveAdditional(nbt);

        // if (currentRecipeId != null)
        if (!availableRecipeIds.isEmpty()) {
            // nbt.putString("recipeId", currentRecipeId);
            ListTag recipeIdTag = NBTUtilBC.writeStringList(availableRecipeIds.stream().map(ResourceLocation::toString));
            nbt.put("recipeId", recipeIdTag);
            nbt.putByte("optionId", (byte) optionId);
        }
    }

    @Override
    // public int getRequiredEnergy()
    public long getTarget() {
        if (hasWork()) {
            // return currentRecipe.getEnergyCost(options.get(optionId));
            return optionRecipes.get(optionId).getEnergyCost();
        } else {
            return 0;
        }
    }

    public void findRecipe() {
        // String oldId = currentRecipeId;
        List<ResourceLocation> oldIds = List.copyOf(availableRecipeIds);
        // currentRecipeId = null;
        availableRecipeIds.clear();

        // if (getStackInSlot(0) != null)
        if (!this.input.getStackInSlot(0).isEmpty()) {
            // for (IProgrammingRecipe recipe : BuildcraftRecipeRegistry.programmingTable.getRecipes())
            for (IProgrammingRecipe recipe : BuildcraftRecipeRegistry.programmingRecipes.getRecipes(level)) {
                // if (recipe.canCraft(getStackInSlot(0)))
                if (recipe.canCraft(this.input.getStackInSlot(0))) {
                    // currentRecipeId = recipe.getId();
                    // break;
                    availableRecipeIds.add(recipe.getId());
                }
            }
        }

        // if ((oldId != null && currentRecipeId != null && !oldId.equals(currentRecipeId)) || (oldId == null && currentRecipeId != null) || (oldId != null && currentRecipeId == null))
        if (
                (
                        !oldIds.isEmpty()
                                && !availableRecipeIds.isEmpty() &&
                                !(oldIds.containsAll(availableRecipeIds) && availableRecipeIds.containsAll(oldIds))
                )
                        || (oldIds.isEmpty() && !availableRecipeIds.isEmpty())
                        || (!oldIds.isEmpty() && availableRecipeIds.isEmpty())
        ) {
            optionId = -1;
            updateRecipe();
            queueNetworkUpdate();
        }
    }

    public void updateRecipe() {
        // currentRecipe = BuildcraftRecipeRegistry.programmingTable.getRecipe(currentRecipeId);
        List<IProgrammingRecipe> currentRecipes = Lists.newArrayList();
        availableRecipeIds.forEach(id -> currentRecipes.add(BuildcraftRecipeRegistry.programmingRecipes.getRecipe(level, id)));
        // if (currentRecipe != null)
        if (!currentRecipes.isEmpty()) {
            // options = currentRecipe.getOptions(WIDTH, HEIGHT);
            optionRecipes = BuildcraftRecipeRegistry.programmingRecipes.getOptions(currentRecipes, WIDTH, HEIGHT);
        } else {
            // options = null;
            optionRecipes = List.of();
        }
    }

    public void rpcSelectOption(final int pos) {
//        BuildCraftCore.instance.sendToServer(new PacketCommand(this, "select", new CommandWriter() {
//            @Override
//            public void write(ByteBuf data) {
//                data.writeByte(pos);
//            }
//        }));
        IMessage message = createMessage(NET_RECIPE_STATE, (data) ->
        {
            data.writeByte(pos);
        });
        MessageManager.sendToServer(message);
    }

    @Override
    public boolean hasWork() {
        // return currentRecipe != null && optionId >= 0 && this.getStackInSlot(1) == null;
        return !optionRecipes.isEmpty() && optionId >= 0 && this.output.getStackInSlot(0).isEmpty();
    }

//    @Override
//    public boolean canCraft() {
//        return hasWork();
//    }

//    @Override
//    public boolean isItemValidForSlot(int slot, ItemStack stack) {
//        return slot == 0 || stack == null;
//    }

//    @Override
//    public int[] getSlotsForFace(EnumFacing side) {
//        return new int[] { 0, 1 };
//    }

//    @Override
//    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {
//        return slot == 0;
//    }

//    @Override
//    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {
//        return slot == 1;
//    }

//    @Override
//    public IBlockState getBlockState_MIGRATION_ONLY() {
//        return BuildCraftSilicon.assemblyTableBlock.getDefaultState().withProperty(BlockBuildCraftBase.LASER_TABLE_TYPE, EnumLaserTableType.PROGRAMMING_TABLE);
//    }

    // MenuProvider

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerProgrammingTable_Neptune(BCSiliconMenuTypes.PROGRAMMING_TABLE, id, player, this);
    }

//    @Override
//    public boolean hasFastRenderer() {
//        return true;
//    }
}
