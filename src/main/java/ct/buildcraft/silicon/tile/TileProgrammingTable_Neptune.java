/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.lib.gui.ItemProvider;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.robotics.BCRoboticsBoards;
import ct.buildcraft.robotics.BCRoboticsBoards.BoardEntry;
import ct.buildcraft.robotics.BCRoboticsItems;
import ct.buildcraft.robotics.item.ItemRedstoneBoard;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.container.ContainerProgrammingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class TileProgrammingTable_Neptune extends TileLaserTableBase implements MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("programming_table");
    public static final int NET_SELECTED_OPTION = IDS.allocId("SELECTED_OPTION");

    public static final int WIDTH = 6;
    public static final int HEIGHT = 4;
    public static final int OPTION_COUNT = WIDTH * HEIGHT;

    public final ItemHandlerSimple invInput = itemManager.addInvHandler(
            "input",
            1,
            (slot, stack) -> stack.isEmpty() || stack.is(BCRoboticsItems.REDSTONE_BOARD.get()),
            ItemHandlerManager.EnumAccess.INSERT,
            EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invOutput = itemManager.addInvHandler(
            "output",
            1,
            (slot, stack) -> stack.isEmpty(),
            ItemHandlerManager.EnumAccess.EXTRACT,
            EnumPipePart.VALUES
    );

    public final ItemProvider optionDisplay = new ItemProvider(this::getOptionStack, OPTION_COUNT);
    private List<BoardEntry> options = List.of();
    public int selectedOption = -1;

    public TileProgrammingTable_Neptune(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.PROGRAMMING_TABLE_TILE.get(), pos, state);
        invInput.setCallback((handler, slot, before, after) -> {
            selectedOption = -1;
            refreshOptions();
            setChanged();
            sendNetworkGuiUpdate(NET_GUI_DATA);
        });
        refreshOptions();
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public List<BoardEntry> getOptions() {
        refreshOptions();
        return options;
    }

    public ItemStack getOptionStack(int index) {
        List<BoardEntry> opts = getOptions();
        if (index < 0 || index >= opts.size()) {
            return ItemStack.EMPTY;
        }
        return ItemRedstoneBoard.createStack(opts.get(index));
    }

    public BoardEntry getSelectedBoard() {
        List<BoardEntry> opts = getOptions();
        if (selectedOption < 0 || selectedOption >= opts.size()) {
            return BCRoboticsBoards.EMPTY;
        }
        return opts.get(selectedOption);
    }

    public boolean canProgramInput() {
        ItemStack input = invInput.getStackInSlot(0);
        return !input.isEmpty() && input.is(BCRoboticsItems.REDSTONE_BOARD.get());
    }

    public boolean canPutOutput(ItemStack stack) {
        ItemStack output = invOutput.getStackInSlot(0);
        return output.isEmpty() || (StackUtil.canMerge(stack, output) && output.getCount() + stack.getCount() <= output.getMaxStackSize());
    }

    public void selectOption(int option) {
        List<BoardEntry> opts = getOptions();
        if (option < 0 || option >= opts.size()) {
            selectedOption = -1;
        } else {
            selectedOption = option;
        }
        power = Math.min(power, getTarget());
        sendNetworkGuiUpdate(NET_GUI_DATA);
        setChanged();
    }

    private void refreshOptions() {
        BCRoboticsBoards.init();
        if (!canProgramInput()) {
            options = List.of();
            return;
        }
        options = new ArrayList<>(BCRoboticsBoards.robotEntries());
        options.sort((a, b) -> {
            int byCost = Integer.compare(a.energyCost(), b.energyCost());
            return byCost != 0 ? byCost : a.id().compareTo(b.id());
        });
        if (selectedOption >= options.size()) {
            selectedOption = -1;
        }
    }

    @Override
    public long getTarget() {
        BoardEntry selected = getSelectedBoard();
        if (selected == BCRoboticsBoards.EMPTY || !canProgramInput()) {
            return 0;
        }
        ItemStack programmed = ItemRedstoneBoard.createStack(selected);
        return canPutOutput(programmed) ? selected.energyCost() * MjAPI.MJ : 0;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        refreshOptions();
        long target = getTarget();
        if (target > 0 && power >= target) {
            BoardEntry selected = getSelectedBoard();
            ItemStack programmed = ItemRedstoneBoard.createStack(selected);
            invInput.extractItem(0, 1, false);
            ItemStack output = invOutput.getStackInSlot(0);
            if (output.isEmpty()) {
                invOutput.setStackInSlot(0, programmed);
            } else {
                output.grow(programmed.getCount());
                invOutput.setStackInSlot(0, output);
            }
            power -= target;
            selectedOption = -1;
            refreshOptions();
            setChanged();
            sendNetworkGuiUpdate(NET_GUI_DATA);
            setChanged();
        }
    }

    @Override
    public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && !player.level.isClientSide) {
            NetworkHooks.openScreen(serverPlayer, this, worldPosition);
        }
        return super.onActivated(player, hand, hit);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerProgrammingTable(id, inventory, invInput, invOutput, optionDisplay,
                ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("selectedOption", selectedOption);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        selectedOption = nbt.getInt("selectedOption");
        refreshOptions();
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (id == NET_GUI_DATA || id == NET_SELECTED_OPTION) {
            buffer.writeVarInt(selectedOption);
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (id == NET_GUI_DATA || id == NET_SELECTED_OPTION) {
            selectedOption = buffer.readVarInt();
            refreshOptions();
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("selected - " + selectedOption);
        left.add("options - " + getOptions().size());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }
}
