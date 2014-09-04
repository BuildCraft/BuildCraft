package buildcraft.core;

import eureka.api.EurekaKnowledge;
import eureka.api.interfaces.IEurekaBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public abstract class BlockBuildcraftEureka extends BlockBuildCraft implements IEurekaBlock {
	protected String key;

	protected BlockBuildcraftEureka(Material material, String key) {
		super(material);
		this.key = key;
	}

	protected BlockBuildcraftEureka(Material material, CreativeTabBuildCraft creativeTab, String key) {
		super(material, creativeTab);
		this.key=key;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		EurekaKnowledge.eurekaBlockEvent(world, this, x, y, z, (EntityPlayer) entity, false);
	}

	@Override
	public String getMessage() {
		return "DENIED";
	}

	@Override
	public boolean breakOnInteraction() {
		return false;
	}

	@Override
	public boolean isAllowed(EntityPlayer player) {
		return EurekaKnowledge.isFinished(player, key);
	}
}
