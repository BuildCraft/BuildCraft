/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): BlockWaterGel tick/spread logic deferred
package buildcraft.factory.block;

import java.util.Locale;

import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.StateManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.StringIdentifiable;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockWaterGel extends BlockBCBase_Neptune {

    public enum GelStage implements StringIdentifiable {
        SPREAD_0(3), SPREAD_1(3), SPREAD_2(3), SPREAD_3(3),
        GELLING_0(0), GELLING_1(0), GEL(0);

        public static final GelStage[] VALUES = values();
        public final BlockSoundGroup soundType = BlockSoundGroup.SLIME;
        public final String modelName = name().toLowerCase(Locale.ROOT);
        public final boolean spreading;

        GelStage(int spreadCount) {
            this.spreading = spreadCount > 0;
        }

        @Override
        public String asString() {
            return modelName;
        }
    }

    public static final EnumProperty<GelStage> PROP_STAGE = EnumProperty.of("stage", GelStage.class);

    public BlockWaterGel(AbstractBlock.Settings settings, String id) {
        super(settings, id);
        setDefaultState(getStateManager().getDefaultState().with(PROP_STAGE, GelStage.SPREAD_0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(PROP_STAGE);
    }
}
