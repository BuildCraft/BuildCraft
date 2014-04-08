/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

public interface IBox {

	public IBox expand(int amount);

	public IBox contract(int amount);

	public boolean contains(double x, double y, double z);

	public Position pMin();

	public Position pMax();

	public void createLaserData();

}
