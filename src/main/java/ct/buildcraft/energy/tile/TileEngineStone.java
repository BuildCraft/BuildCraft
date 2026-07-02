package ct.buildcraft.energy.tile;

import ct.buildcraft.core.blockEntity.TileEngineBase;
import ct.buildcraft.core.client.render.RenderEngine;
import ct.buildcraft.core.lib.BCEnergyStorage;
import ct.buildcraft.energy.BCEnergy;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEngineStone extends TileEngineBase{

	public TileEngineStone(BlockPos p_155229_, BlockState bs) {
//		super(BCEnergy.ENGINE_STONE_TILE.get(), p_155229_, bs);
		super(null,null,null);
		// TODO Auto-generated constructor stub
	}
	@Override
	public TextureAtlasSprite getTextureBack() {
		return RenderEngine.IRON_BACK;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getTextureSide() {
		return RenderEngine.IRON_SIDE;
	}

	@Override
	public void tick() {
		super.tick();
	}
	@Override
	public boolean isBurning() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int getMaxPower() {
		// TODO Auto-generated method stub
		return 10;
	}
	@Override
	public float explosionRange() {
		// TODO Auto-generated method stub
		return 0;
	}



}
