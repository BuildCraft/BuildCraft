/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.RenderEngine;
import net.minecraft.src.TextureFX;
import net.minecraft.src.forge.MinecraftForgeClient;

public class TextureLiquidsFX extends TextureFX {

	private int int_numPixels = 256;
	private int int_size = 16;
	private int int_sizeMinus1 = 0xF;

	private final int redMin, redMax, greenMin, greenMax, blueMin, blueMax;
	private final String texture;

	public TextureLiquidsFX(int redMin, int redMax, int greenMin, int greenMax,
			int blueMin, int blueMax, int spriteIndex, String texture) {
		super(spriteIndex);

		try {
			Class<? extends Object> sizeClass = Class
					.forName("com.pclewis.mcpatcher.mod.TileSize");

			int_numPixels = sizeClass.getDeclaredField("int_numPixels").getInt(
					sizeClass);
			int_size = sizeClass.getDeclaredField("int_size").getInt(sizeClass);
			int_sizeMinus1 = sizeClass.getDeclaredField("int_sizeMinus1")
					.getInt(sizeClass);
		} catch (Throwable t) {

		}

		this.redMin = redMin;
		this.redMax = redMax;
		this.greenMin = greenMin;
		this.greenMax = greenMax;
		this.blueMin = blueMin;
		this.blueMax = blueMax;
		this.texture = texture;

		field_1158_g = new float[int_numPixels];
		field_1157_h = new float[int_numPixels];
		field_1156_i = new float[int_numPixels];
		field_1155_j = new float[int_numPixels];
	}

	@Override
	public void bindImage(RenderEngine renderengine) {
		MinecraftForgeClient.bindTexture(texture);
	}

	@Override
	public void onTick() {
		for (int i = 0; i < int_size; i++)
			for (int k = 0; k < int_size; k++) {
				float f = 0.0F;
				for (int j1 = i - 1; j1 <= i + 1; j1++) {
					int k1 = j1 & int_sizeMinus1;
					int i2 = k & int_sizeMinus1;
					f += field_1158_g[k1 + i2 * int_size];
				}

				field_1157_h[i + k * int_size] = f / 3.3F
						+ field_1156_i[i + k * int_size] * 0.8F;
			}

		for (int j = 0; j < int_size; j++)
			for (int l = 0; l < int_size; l++) {
				field_1156_i[j + l * int_size] += field_1155_j[j + l * int_size] * 0.05F;
				if (field_1156_i[j + l * int_size] < 0.0F)
					field_1156_i[j + l * int_size] = 0.0F;
				field_1155_j[j + l * int_size] -= 0.1F;
				if (Math.random() < 0.050000000000000003D)
					field_1155_j[j + l * int_size] = 0.5F;
			}

		float af[] = field_1157_h;
		field_1157_h = field_1158_g;
		field_1158_g = af;
		for (int i1 = 0; i1 < int_numPixels; i1++) {
			float f1 = field_1158_g[i1];
			if (f1 > 1.0F)
				f1 = 1.0F;
			if (f1 < 0.0F)
				f1 = 0.0F;
			float f2 = f1 * f1;
			int r = (int) (redMin + f2 * (redMax - redMin));
			int g = (int) (greenMin + f2 * (greenMax - greenMin));
			int b = (int) (blueMin + f2 * (blueMax - blueMin));
			if (anaglyphEnabled) {
				int i3 = (r * 30 + g * 59 + b * 11) / 100;
				int j3 = (r * 30 + g * 70) / 100;
				int k3 = (r * 30 + b * 70) / 100;
				r = i3;
				g = j3;
				b = k3;
			}

			imageData[i1 * 4 + 0] = (byte) r;
			imageData[i1 * 4 + 1] = (byte) g;
			imageData[i1 * 4 + 2] = (byte) b;
			imageData[i1 * 4 + 3] = /* (byte)l2 */(byte) 255;
		}

	}

	protected float field_1158_g[];
	protected float field_1157_h[];
	protected float field_1156_i[];
	protected float field_1155_j[];
}
