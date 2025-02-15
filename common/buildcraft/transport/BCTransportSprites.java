/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.client.model.PipeModelCacheAll;
import buildcraft.transport.client.model.PipeModelCacheBase;
import buildcraft.transport.client.render.PipeFlowRendererItems;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.RegisterEvent;

import java.util.EnumMap;
import java.util.Locale;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class BCTransportSprites {
    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder PIPE_COLOUR, COLOUR_ITEM_BOX;
    public static final SpriteHolder PIPE_COLOUR_BORDER_OUTER;
    public static final SpriteHolder PIPE_COLOUR_BORDER_INNER;

    public static final SpriteHolder TRIGGER_POWER_REQUESTED;
    public static final SpriteHolder TRIGGER_ITEMS_TRAVERSING;
    public static final SpriteHolder TRIGGER_FLUIDS_TRAVERSING;

    public static final SpriteHolder[] ACTION_PIPE_COLOUR;
    public static final EnumMap<SlotIndex, SpriteHolder> ACTION_EXTRACTION_PRESET;
    private static final EnumMap<DyeColor, SpriteHolder> PIPE_SIGNAL_ON;
    private static final EnumMap<DyeColor, SpriteHolder> PIPE_SIGNAL_OFF;
    private static final EnumMap<Direction, SpriteHolder> ACTION_PIPE_DIRECTION;

    public static final SpriteHolder POWER_FLOW;
    public static final SpriteHolder POWER_FLOW_OVERLOAD;

    static {
        EMPTY_FILTERED_BUFFER_SLOT = getHolder("gui/empty_filtered_buffer_slot");
        NOTHING_FILTERED_BUFFER_SLOT = getHolder("gui/nothing_filtered_buffer_slot");
        PIPE_COLOUR = getHolder("pipes/overlay_stained");
        COLOUR_ITEM_BOX = getHolder("pipes/colour_item_box");
        PIPE_COLOUR_BORDER_OUTER = getHolder("pipes/colour_border_outer");
        PIPE_COLOUR_BORDER_INNER = getHolder("pipes/colour_border_inner");

        ACTION_PIPE_COLOUR = new SpriteHolder[ColourUtil.COLOURS.length];
        for (DyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "item/paintbrush/" + colour.getName());
        }

        PIPE_SIGNAL_OFF = new EnumMap<>(DyeColor.class);
        PIPE_SIGNAL_ON = new EnumMap<>(DyeColor.class);

        for (DyeColor colour : ColourUtil.COLOURS) {
            String pre = "triggers/trigger_pipesignal_" + colour.getName().toLowerCase(Locale.ROOT) + "_";
            PIPE_SIGNAL_OFF.put(colour, getHolder(pre + "inactive"));
            PIPE_SIGNAL_ON.put(colour, getHolder(pre + "active"));
        }

        ACTION_EXTRACTION_PRESET = new EnumMap<>(SlotIndex.class);
        for (SlotIndex index : SlotIndex.VALUES) {
            ACTION_EXTRACTION_PRESET.put(index, getHolder("triggers/extraction_preset_" + index.colour.getName()));
        }

        ACTION_PIPE_DIRECTION = new EnumMap<>(Direction.class);
        for (Direction face : Direction.VALUES) {
            ACTION_PIPE_DIRECTION.put(face,
                    getHolder("core", "triggers/trigger_dir_" + face.getName().toLowerCase(Locale.ROOT)));
        }

        POWER_FLOW = getHolder("pipes/power_flow");
        POWER_FLOW_OVERLOAD = getHolder("pipes/power_flow_overload");

        TRIGGER_POWER_REQUESTED = getHolder("transport", "triggers/trigger_pipecontents_requestsenergy");
        TRIGGER_ITEMS_TRAVERSING = getHolder("transport", "triggers/trigger_pipecontents_containsitems");
        TRIGGER_FLUIDS_TRAVERSING = getHolder("transport", "triggers/trigger_pipecontents_containsfluids");
    }

    private static SpriteHolder getHolder(String loc) {
        return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
    }

    private static SpriteHolder getHolder(String module, String loc) {
        return SpriteHolderRegistry.getHolder("buildcraft" + module + ":" + loc);
    }

    public static void fmlPreInit() {
        // 1.18.2: following events are IModBusEvent
//        MinecraftForge.EVENT_BUS.register(BCTransportSprites.class);
        IEventBus modEventBus = ((FMLModContainer) ModList.get().getModContainerById(BCTransport.MODID).get()).getEventBus();
        modEventBus.register(BCTransportSprites.class);
    }

    @SubscribeEvent
//    public static void onTextureStitchPre(TextureStitchEvent.Pre event)
    public static void onTextureStitchPre(ModelEvent.ModifyBakingResult event) {
        PipeModelCacheBase.generator.onTextureStitchPre();
    }

    // Calen 1.20.1
    public static void onDatagenTextureRegister(Consumer<ResourceLocation> consumer) {
        PipeModelCacheBase.generator.onDatagenTextureRegister(consumer);
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
//        PipeModelCacheBase.generator.onTextureStitchPre();
        PipeModelCacheBase.generator.onTextureStitchPost(event);
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        PipeModelCacheAll.clearModels();
        PipeFlowRendererItems.onModelBake();
    }

    public static SpriteHolder getPipeSignal(boolean active, DyeColor colour) {
        return (active ? PIPE_SIGNAL_ON : PIPE_SIGNAL_OFF).get(colour);
    }

    public static SpriteHolder getPipeDirection(Direction face) {
        return ACTION_PIPE_DIRECTION.get(face);
    }
}
