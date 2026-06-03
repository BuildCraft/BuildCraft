/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.block.MapColor;
import net.minecraft.util.Identifier;

import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidStackBC;

import buildcraft.lib.misc.LocaleUtil;

public class BCFluid extends Fluid {
    private int colour = 0xFFFFFFFF, light = 0xFF_FF_FF_FF, dark = 0xFF_FF_FF_FF;
    private boolean isFlammable = false;
    private int lightOpacity = 0;
    private MapColor mapColour = MapColor.BLACK;
    private int heat;
    private boolean heatable;
    private String blockName;

    public BCFluid(String fluidName, Identifier still, Identifier flowing) {
        super(fluidName, still, flowing);
        blockName = fluidName;
    }

    public String getBareLocalizedName(FluidStackBC stack) {
        return super.getLocalizedName(stack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public String getLocalizedName(FluidStackBC stack) {
        if (heat <= 0 && !isHeatable()) return getBareLocalizedName(stack);
        String name = getBareLocalizedName(stack);
        return LocaleUtil.localize("buildcraft.fluid.heat_" + heat, name);
    }

    public void setMapColour(MapColor mapColour) {
        this.mapColour = mapColour;
    }

    public final MapColor getMapColour() {
        return this.mapColour;
    }

    public void setFlammable(boolean isFlammable) {
        this.isFlammable = isFlammable;
    }

    public final boolean isFlammable() {
        return isFlammable;
    }

    public void setLightOpacity(int lightOpacity) {
        this.lightOpacity = lightOpacity;
    }

    public final int getLightOpacity() {
        return lightOpacity;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getBlockName() {
        return blockName;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public int getColor() {
        return colour;
    }

    public int getLightColour() {
        return light;
    }

    public int getDarkColour() {
        return dark;
    }

    public BCFluid setColour(int colour) {
        this.colour = colour;
        return this;
    }

    public BCFluid setColour(int light, int dark) {
        this.light = light;
        this.dark = dark;
        this.colour = 0xFF_FF_FF_FF;
        return this;
    }

    public BCFluid setHeat(int heat) {
        this.heat = heat;
        return this;
    }

    public int getHeatValue() {
        return heat;
    }

    public BCFluid setHeatable(boolean value) {
        heatable = value;
        return this;
    }

    public boolean isHeatable() {
        return heatable;
    }

    // ---- Forge fluid-block compat ----
    private net.minecraft.block.Block fluidBlock;

    public void setBlock(net.minecraft.block.Block block) {
        this.fluidBlock = block;
    }

    public net.minecraft.block.Block getBlock() {
        return fluidBlock;
    }
}
