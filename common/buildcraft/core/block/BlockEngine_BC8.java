/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;


import buildcraft.api.core.IEngineType;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

public class BlockEngine_BC8 extends BlockEngineBase_BC8<EnumEngineType> {
    public BlockEngine_BC8(String idBC, AbstractBlock.Properties properties, EnumEngineType type) {
        super(idBC, properties, type);
    }

//    @Override
//    public Property<EnumEngineType> getEngineProperty() {
//        return BuildCraftProperties.ENGINE_TYPE;
//    }

//    @Override
//    public EnumEngineType getEngineType(int meta) {
//        return EnumEngineType.fromMeta(meta);
//    }

//    @Override
//    public String getUnlocalizedName() {
////        return TagManager.getTag("block.engine.bc." + engine.unlocalizedTag, TagManager.EnumTagType.UNLOCALIZED_NAME);
//        return TagManager.getTag("block.engine.bc." + this.engineType.unlocalizedTag, TagManager.EnumTagType.UNLOCALIZED_NAME);
//    }

    public static final Map<IEngineType, ModelHolderVariable> engineModels = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void setModel(IEngineType engineType, ModelHolderVariable model) {
        engineModels.put(engineType, model);
    }

    private static final Map<IEngineType, LazyValue<TextureAtlasSprite>> engineParticles = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    private TextureAtlasSprite getEngineParticle(IEngineType engineType) {
        return engineParticles.computeIfAbsent(engineType, (e) ->
                new LazyValue<>(
                        () ->
                        {
                            for (MutableQuad quad : engineModels.get(e).getCutoutQuads()) {
                                if (quad.getFace() == Direction.DOWN) {
                                    return quad.getSprite();
                                }
                            }
                            return SpriteUtil.missingSprite();
                        }
                )

        ).get();
    }

    // Calen for particles instead of missingno
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addHitEffects(BlockState state, World worldIn, RayTraceResult hitIn, ParticleManager manager) {
        if (hitIn.getType() != RayTraceResult.Type.BLOCK) {
            return false;
        }
        BlockRayTraceResult target = (BlockRayTraceResult) hitIn;
        ClientWorld world = (ClientWorld) worldIn;
        TileEntity te = world.getBlockEntity(target.getBlockPos());
        if (te instanceof TileEngineBase_BC8) {
            double x = Math.random();
            double y = Math.random();
            double z = Math.random();

            x += target.getLocation().x;
            y += target.getLocation().y;
            z += target.getLocation().z;

            BreakingParticle particle = new BreakingParticle(world, x, y, z, 0, 0, 0, StackUtil.EMPTY);
            particle.setPos(x, y, z);
            TextureAtlasSprite texture = getEngineParticle(engineType);
            if (texture == null) {
                return false;
            }
            particle.setSprite(texture);
            particle.setPower(0.2F);
            particle.scale(0.6F);
            manager.add(particle);

            return true;
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World worldIn, BlockPos pos, ParticleManager manager) {
        ClientWorld world = (ClientWorld) worldIn;
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEngineBase_BC8) {
            int countX = 2;
            int countY = 2;
            int countZ = 2;

            TextureAtlasSprite texture = getEngineParticle(engineType);
            if (texture == null) {
                return false;
            }

            for (int x = 0; x < countX; x++) {
                for (int y = 0; y < countY; y++) {
                    for (int z = 0; z < countZ; z++) {
                        double _x = pos.getX() + 0.5;
                        double _y = pos.getY() + 0.5;
                        double _z = pos.getZ() + 0.5;

                        BreakingParticle particle = new BreakingParticle(world, _x, _y, _z, 0, 0, 0, StackUtil.EMPTY);
                        particle.setPos(_x, _y, _z);
                        particle.setSprite(texture);
                        manager.add(particle);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
