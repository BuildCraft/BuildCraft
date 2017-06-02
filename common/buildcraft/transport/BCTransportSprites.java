/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.EnumMap;
import java.util.Locale;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.ColourUtil;

import buildcraft.transport.client.model.PipeModelCacheAll;
import buildcraft.transport.client.model.PipeModelCacheBase;
import buildcraft.transport.client.render.PipeFlowRendererItems;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class BCTransportSprites {
    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT;
    public static final SpriteHolder PIPE_COLOUR, COLOUR_ITEM_BOX;

    public static final SpriteHolder TRIGGER_LIGHT_LOW;
    public static final SpriteHolder TRIGGER_LIGHT_HIGH;

    public static final SpriteHolder ACTION_PULSAR_CONSTANT;
    public static final SpriteHolder ACTION_PULSAR_SINGLE;
    public static final SpriteHolder[] ACTION_PIPE_COLOUR;

    public static final EnumMap<SlotIndex, SpriteHolder> ACTION_EXTRACTION_PRESET;
    private static final EnumMap<EnumDyeColor, SpriteHolder> PIPE_SIGNAL_ON;
    private static final EnumMap<EnumDyeColor, SpriteHolder> PIPE_SIGNAL_OFF;
    private static final EnumMap<EnumFacing, SpriteHolder> ACTION_PIPE_DIRECTION;

    public static final SpriteHolder POWER_FLOW;

    static {
        EMPTY_FILTERED_BUFFER_SLOT = getHolder("gui/empty_filtered_buffer_slot");
        NOTHING_FILTERED_BUFFER_SLOT = getHolder("gui/nothing_filtered_buffer_slot");
        PIPE_COLOUR = getHolder("pipes/overlay_stained");
        COLOUR_ITEM_BOX = getHolder("pipes/colour_item_box");

        TRIGGER_LIGHT_LOW = getHolder("triggers/trigger_light_dark");
        TRIGGER_LIGHT_HIGH = getHolder("triggers/trigger_light_bright");

        ACTION_PULSAR_CONSTANT = getHolder("triggers/action_pulsar_on");
        ACTION_PULSAR_SINGLE = getHolder("triggers/action_pulsar_single");
        ACTION_PIPE_COLOUR = new SpriteHolder[ColourUtil.COLOURS.length];
        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "items/paintbrush/" + colour.getName());
        }

        PIPE_SIGNAL_OFF = new EnumMap<>(EnumDyeColor.class);
        PIPE_SIGNAL_ON = new EnumMap<>(EnumDyeColor.class);

        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            String pre = "triggers/trigger_pipesignal_" + colour.getName().toLowerCase(Locale.ROOT) + "_";
            PIPE_SIGNAL_OFF.put(colour, getHolder(pre + "inactive"));
            PIPE_SIGNAL_ON.put(colour, getHolder(pre + "active"));
        }

        ACTION_EXTRACTION_PRESET = new EnumMap<>(SlotIndex.class);
        for (SlotIndex index : SlotIndex.VALUES) {
            ACTION_EXTRACTION_PRESET.put(index, getHolder("triggers/extraction_preset_" + index.colour.getName()));
        }

        ACTION_PIPE_DIRECTION = new EnumMap<>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            ACTION_PIPE_DIRECTION.put(face, getHolder("core", "triggers/trigger_dir_" + face.getName().toLowerCase(Locale.ROOT)));
        }

        POWER_FLOW = getHolder("core", "blocks/misc/texture_cyan");
    }

    private static SpriteHolder getHolder(String loc) {
        return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
    }

    private static SpriteHolder getHolder(String module, String loc) {
        return SpriteHolderRegistry.getHolder("buildcraft" + module + ":" + loc);
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCTransportSprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        PipeModelCacheBase.generator.onTextureStitchPre(event.getMap());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        PipeModelCacheAll.clearModels();
        PipeFlowRendererItems.onModelBake();
    }

    public static SpriteHolder getPipeSignal(boolean active, EnumDyeColor colour) {
        return (active ? PIPE_SIGNAL_ON : PIPE_SIGNAL_OFF).get(colour);
    }

    public static SpriteHolder getPipeDirection(EnumFacing face) {
        return ACTION_PIPE_DIRECTION.get(face);
    }
}
