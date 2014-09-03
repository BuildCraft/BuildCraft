package eureka.api.interfaces;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftAdditions.wordpress.com/
 * Eureka is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftAdditions.wordpress.com/wiki/licensing-stuff/
 */
public interface IEurekaTileEntity {

    void makeProgress(EntityPlayer player, String key);
}
