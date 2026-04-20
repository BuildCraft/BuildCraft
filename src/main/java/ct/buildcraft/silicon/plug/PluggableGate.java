/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.plug;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import ct.buildcraft.api.transport.IWireEmitter;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.lib.expression.DefaultContexts;
import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.info.ContextInfo;
import ct.buildcraft.lib.expression.info.VariableInfo.CacheType;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean.BooleanPossibilities;
import ct.buildcraft.lib.expression.info.VariableInfo.VariableInfoObject;
import ct.buildcraft.lib.expression.node.value.NodeVariableBoolean;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.data.ModelVariableData;
import ct.buildcraft.lib.net.IPayloadWriter;
import ct.buildcraft.lib.statement.ActionWrapper;
import ct.buildcraft.lib.statement.TriggerWrapper;
import ct.buildcraft.silicon.BCSiliconItems;
import ct.buildcraft.silicon.client.model.key.KeyPlugGate;
import ct.buildcraft.silicon.container.ContainerGate;
import ct.buildcraft.silicon.gate.EnumGateLogic;
import ct.buildcraft.silicon.gate.EnumGateMaterial;
import ct.buildcraft.silicon.gate.EnumGateModifier;
import ct.buildcraft.silicon.gate.GateLogic;
import ct.buildcraft.silicon.gate.GateLogic.StatementPair;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.silicon.item.ItemGateCopier;
import ct.buildcraft.transport.pipe.PluggableHolder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PluggableGate extends PipePluggable implements IWireEmitter , MenuProvider{
    public static final FunctionContext MODEL_FUNC_CTX_STATIC, MODEL_FUNC_CTX_DYNAMIC;
    private static final NodeVariableObject<String> MODEL_MATERIAL;
    private static final NodeVariableObject<String> MODEL_MODIFIER;
    private static final NodeVariableObject<String> MODEL_LOGIC;
    private static final NodeVariableObject<Direction> MODEL_SIDE;
    private static final NodeVariableBoolean MODEL_IS_ON;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    private static final ResourceLocation ADVANCEMENT_PLACE_GATE
        = new ResourceLocation("buildcrafttransport:pipe_logic");

    private static final ResourceLocation ADVANCEMENT_PLACE_ADV_GATE
        = new ResourceLocation("buildcrafttransport:extended_logic");

    public final GateLogic logic;

    public final ModelVariableData clientModelData = new ModelVariableData();

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = Shapes.box(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX_STATIC = DefaultContexts.createWithAll();
        MODEL_MATERIAL = MODEL_FUNC_CTX_STATIC.putVariableString("material");
        MODEL_MODIFIER = MODEL_FUNC_CTX_STATIC.putVariableString("modifier");
        MODEL_LOGIC = MODEL_FUNC_CTX_STATIC.putVariableString("logic");
        MODEL_SIDE = MODEL_FUNC_CTX_STATIC.putVariableObject("side", Direction.class);

        MODEL_FUNC_CTX_DYNAMIC = new FunctionContext(MODEL_FUNC_CTX_STATIC);
        MODEL_IS_ON = MODEL_FUNC_CTX_DYNAMIC.putVariableBoolean("on");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX_DYNAMIC);
        VariableInfoObject<String> infoMaterial = MODEL_VAR_INFO.createInfoObject(MODEL_MATERIAL);
        infoMaterial.cacheType = CacheType.ALWAYS;
        infoMaterial.setIsComplete = true;
        infoMaterial.possibleValues
            .addAll(Arrays.stream(EnumGateMaterial.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoModifier = MODEL_VAR_INFO.createInfoObject(MODEL_MODIFIER);
        infoModifier.cacheType = CacheType.ALWAYS;
        infoModifier.setIsComplete = true;
        infoModifier.possibleValues
            .addAll(Arrays.stream(EnumGateModifier.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<String> infoLogic = MODEL_VAR_INFO.createInfoObject(MODEL_LOGIC);
        infoLogic.cacheType = CacheType.ALWAYS;
        infoLogic.setIsComplete = true;
        infoLogic.possibleValues
            .addAll(Arrays.stream(EnumGateLogic.VALUES).map(m -> m.tag).collect(Collectors.toList()));

        VariableInfoObject<Direction> infoSide = MODEL_VAR_INFO.createInfoObject(MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        Collections.addAll(infoSide.possibleValues, Direction.values());

        VariableInfoBoolean infoIsOn = MODEL_VAR_INFO.createInfoBoolean(MODEL_IS_ON);
        infoIsOn.cacheType = CacheType.ALWAYS;
        infoIsOn.setIsComplete = true;
        infoIsOn.possibleValues = BooleanPossibilities.FALSE_TRUE;
    }

    // Manual constructor (called by the specific item pluggable gate code)

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    // Saving + Loading

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompound("data"));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("data", logic.writeToNbt());
        return nbt;
    }

    // Networking

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(def, holder, side);
        logic = new GateLogic(this, buffer);
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        logic.writeCreationToBuf(buffer);
    }

    public void sendMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    public void sendGuiMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) -> {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        throw new Error("All messages must have an ID, and we can't just write a payload directly!");
    }

    @Override
    public void readPayload(FriendlyByteBuf b, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        logic.readPayload(b, side, ctx);
    }

    // PipePluggable

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.PLUG_GATE_ITEM.get().getStack(logic.variant);
    }

    @Override
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) {
            return new KeyPlugGate(side, logic.variant);
        }
        return null;
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_GATE);
            if (logic.variant.numActionArgs >= 1) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_ADV_GATE);
            }
        }
    }

    @Override
    public InteractionResult onPluggableActivate(Player player, BlockHitResult trace, Level level) {
        if (!level.isClientSide&&player instanceof ServerPlayer splayer) {
            if (interactWithCopier(player, player.getMainHandItem())) {
                return InteractionResult.SUCCESS;
            }
            if (interactWithCopier(player, player.getOffhandItem())) {
                return InteractionResult.SUCCESS;
            }

            BlockPos pos = holder.getPipePos();
            NetworkHooks.openScreen(splayer, this, buf ->{
            	buf.writeBlockPos(pos);
            	buf.writeEnum(side);
            });
        }
        return InteractionResult.CONSUME;
    }

    private boolean interactWithCopier(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemGateCopier)) {
            return false;
        }

        CompoundTag stored = ItemGateCopier.getCopiedGateData(stack);

        if (stored != null) {

            logic.readConfigData(stored);
            
           	player.displayClientMessage(Component.translatable("chat.gateCopier.gatePasted"), true);

        } else {
            stored = logic.writeToNbt();
            stored.remove("wireBroadcasts");

            if (stored.size() == 1) {
                player.displayClientMessage(Component.translatable("chat.gateCopier.noInformation"), true);
                return false;
            }

            ItemGateCopier.setCopiedGateData(stack, stored);
            player.displayClientMessage(Component.translatable("chat.gateCopier.gateCopied"), true);
        }

        return true;
    }

    @Override
    public boolean isEmitting(DyeColor colour) {
        return logic.isEmitting(colour);
    }

    @Override
    public void emitWire(DyeColor colour) {
        logic.emitWire(colour);
    }

    // Gate methods

    @Override
    public void onTick() {
        logic.onTick();
        if (holder.getPipeWorld().isClientSide()) {
            clientModelData.tick();
        }
    }

    @Override
    public boolean canConnectToRedstone(@Nullable Direction to) {
        return true;
    }

    // Model

    public static void setClientModelVariables(Direction side, GateVariant variant) {
        MODEL_SIDE.value = side;
        MODEL_MATERIAL.value = variant.material.tag;
        MODEL_MODIFIER.value = variant.modifier.tag;
        MODEL_LOGIC.value = variant.logic.tag;
        MODEL_IS_ON.value = false;// Used by the item
    }

    public void setClientModelVariables() {
        setClientModelVariables(side, logic.variant);
        MODEL_IS_ON.value = logic.isOn;
    }

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        ContainerGate container = new ContainerGate(id, inv, logic);
        MessageUtil.doDelayedServer(() -> {
            container.sendMessage(ContainerGate.ID_VALID_STATEMENTS);
        });
		return container;
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("PluggableGate:TODO");
	}

	@Override
	public void rotate(Rotation axis) {
		super.rotate(axis);
		switch (axis) {
		case CLOCKWISE_90 ->{
			for(StatementPair pair : logic.statements) {
				TriggerWrapper triggerWrapper = pair.trigger.get();
				if(triggerWrapper != null)
					pair.trigger.set((TriggerWrapper) triggerWrapper.rotateLeft());
				ActionWrapper actionWrapper = pair.action.get();
				if(actionWrapper != null)
					pair.action.set((ActionWrapper) actionWrapper.rotateLeft());
			}
		}
		case CLOCKWISE_180 ->{
			for(StatementPair pair : logic.statements) {
				TriggerWrapper triggerWrapper = pair.trigger.get();
				if(triggerWrapper != null) 
					pair.trigger.set((TriggerWrapper) triggerWrapper.rotateLeft().rotateLeft());
				ActionWrapper actionWrapper = pair.action.get();
				if(actionWrapper != null) 
					pair.action.set((ActionWrapper) actionWrapper.rotateLeft().rotateLeft());
			}
		}
		case COUNTERCLOCKWISE_90 ->{
			for(StatementPair pair : logic.statements) {
				TriggerWrapper triggerWrapper = pair.trigger.get();
				if(triggerWrapper != null) 
					pair.trigger.set((TriggerWrapper) triggerWrapper.rotateLeft().rotateLeft().rotateLeft());
				ActionWrapper actionWrapper = pair.action.get();
				if(actionWrapper != null)
					pair.action.set((ActionWrapper) actionWrapper.rotateLeft().rotateLeft().rotateLeft());
			}
		}
		case NONE ->{}
		}
	}
	
	
}
