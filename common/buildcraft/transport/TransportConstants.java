/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

public final class TransportConstants {

	public static final float FACADE_THICKNESS = 2F / 16F;

	/*
	public static final float PIPE_MIN_SPEED = (1.0F / 80.0F);
	public static final float PIPE_MAX_SPEED = (1.0F / 7.0F);
	public static final float PIPE_SLOWDOWN_SPEED = 0.008F;
	public static final float PIPE_DEFAULT_SPEED = (1.0F / 25.0F);
	*/

	public static final int PIPE_POWER_BASE_CAP = 80;
	public static final float PIPE_SPEEDUP_MULTIPLIER = 4F;
	public static final float PIPE_MIN_SPEED = 0.01F;
	public static final float PIPE_MAX_SPEED = 0.15F;
	public static final float PIPE_SLOWDOWN_SPEED = 0.01F;
	public static final float PIPE_DEFAULT_SPEED = PIPE_MIN_SPEED;
	/**
	 * Deactivate constructor
	 */
	private TransportConstants() {
	}
}
