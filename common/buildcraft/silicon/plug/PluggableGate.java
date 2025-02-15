/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.net.IMessage;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo.CacheType;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoBoolean.BooleanPossibilities;
import buildcraft.lib.expression.info.VariableInfo.VariableInfoObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.gate.*;
import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.transport.pipe.PluggableHolder;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class PluggableGate extends PipePluggable implements IWireEmitter, IBCTileMenuProvider {
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

        BOXES[Direction.DOWN.ordinal()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = Shapes.box(ul, min, min, uu, max, max);

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
        Collections.addAll(infoSide.possibleValues, Direction.VALUES);

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
        logic = new GateLogic(this, PacketBufferBC.asPacketBufferBc(buffer));
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        logic.writeCreationToBuf(PacketBufferBC.asPacketBufferBc(buffer));
    }

    public void sendMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) ->
        {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    public void sendGuiMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) ->
        {
            /* The pluggable holder receives this message and requires the ID '1' (UPDATE) to forward the message onto
             * ourselves */
            buffer.writeByte(PluggableHolder.ID_UPDATE_PLUG);
            writer.write(PacketBufferBC.asPacketBufferBc(buffer));
        });
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Dist side) {
        throw new Error("All messages must have an ID, and we can't just write a payload directly!");
    }

    @Override
//    public void readPayload(PacketBuffer b, Dist side, MessageContext ctx) throws IOException
    public void readPayload(FriendlyByteBuf b, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        logic.readPayload(PacketBufferBC.asPacketBufferBc(b), side, ctx);
    }

    // PipePluggable

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
//        return BCSiliconItems.plugGate.get().getStack(logic.variant);
        return ItemPluggableGate.getStack(logic.variant);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) {
            return new KeyPlugGate(side, logic.variant);
        }
        return null;
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_GATE);
            if (logic.variant.numActionArgs >= 1) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_ADV_GATE);
            }
        }
    }

    @Override
    public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
        if (!player.level().isClientSide) {
            if (interactWithCopier(player, player.getMainHandItem())) {
                return true;
            }
            if (interactWithCopier(player, player.getOffhandItem())) {
                return true;
            }

            BlockPos pos = holder.getPipePos();

            // Calen 1.18.2: moved from ContainerGate#<init>
            // to recreate plug object before gui packed received
            // Client call in BCSiliconMenuTypes#GATE
            IMessage msg = this.logic.getPipeHolder().onServerPlayerOpenNoSend(player);
//            BCSiliconGuis.GATE.openGui(player, pos, side.ordinal());
            MessageUtil.serverOpenGUIWithMsg(player, this, pos, side.ordinal(), msg);
        }
        return true;
    }

    private boolean interactWithCopier(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemGateCopier)) {
            return false;
        }

        CompoundTag stored = ItemGateCopier.getCopiedGateData(stack);

        if (stored != null) {

            logic.readConfigData(stored);

            player.sendSystemMessage(Component.translatable("chat.gateCopier.gatePasted"));

        } else {
            stored = logic.writeToNbt();
            stored.remove("wireBroadcasts");

            if (stored.size() == 1) {
                player.sendSystemMessage(Component.translatable("chat.gateCopier.noInformation"));
                return false;
            }

            ItemGateCopier.setCopiedGateData(stack, stored);
            player.sendSystemMessage(Component.translatable("chat.gateCopier.gateCopied"));
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
        if (holder.getPipeWorld().isClientSide) {
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

    // MenuProvider

    @NotNull
    @Override
    public Component getDisplayName() {
        return this.logic.variant.getLocalizedName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        // Calen: from BCSiliconProxy implements IGuiHandler
        ContainerGate container = new ContainerGate(BCSiliconMenuTypes.GATE, id, player, this.logic);
        // to enable statements
        MessageUtil.doDelayedServer(() ->
        {
            container.sendMessage(ContainerGate.ID_VALID_STATEMENTS);
        });
        return container;
    }

    @Override
    public IMessage onServerPlayerOpenNoSend(Player player) {
        return holder.onServerPlayerOpenNoSend(player);
    }
}
