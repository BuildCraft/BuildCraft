/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.proxy.CoreProxy;

public final class BlockUtils {
    private static Chunk lastChunk;

    /** Deactivate constructor */
    private BlockUtils() {}

    public static List<ItemStack> getItemStackFromBlock(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == null || block.isAir(world, pos)) {
            return null;
        }

        List<ItemStack> dropsList = block.getDrops(world, pos, state, 0);
        float dropChance =
            ForgeEventFactory.fireBlockHarvesting(dropsList, world, pos, state, 0, 1.0F, false, CoreProxy.proxy.getBuildCraftPlayer(world).get());

        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        for (ItemStack s : dropsList) {
            if (world.rand.nextFloat() <= dropChance) {
                returnList.add(s);
            }
        }

        return returnList;
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos) {
        return breakBlock(world, pos, BuildCraftCore.itemLifespan * 20);
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, int forcedLifespan) {
        List<ItemStack> items = new ArrayList<ItemStack>();

        if (breakBlock(world, pos, items)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean breakBlock(WorldServer world, BlockPos pos, List<ItemStack> drops) {
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), CoreProxy.proxy.getBuildCraftPlayer(world).get());
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.isAirBlock(pos) && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
            drops.addAll(getItemStackFromBlock(world, pos));
        }
        world.setBlockToAir(pos);

        return true;
    }

    public static void dropItem(WorldServer world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.rand.nextFloat() * var + (1.0F - var) * 0.5D;
        EntityItem entityitem = new EntityItem(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.lifespan = forcedLifespan;
        entityitem.setDefaultPickupDelay();

        world.spawnEntityInWorld(entityitem);
    }

    public static boolean isAnObstructingBlock(Block block, World world, BlockPos pos) {
        if (block == null || block.isAir(world, pos)) {
            return false;
        }
        return true;
    }

    public static boolean canChangeBlock(World world, BlockPos pos) {
        return canChangeBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean canChangeBlock(IBlockState state, World world, BlockPos pos) {
        if (state == null)
            return true;

        Block block = state.getBlock();
        if (block == null || block.isAir(world, pos)) {
            return true;
        }

        if (block.getBlockHardness(world, pos) < 0) {
            return false;
        }

        // TODO: Make this support all "heavy" liquids, not just oil/lava
        if (block instanceof IFluidBlock && ((IFluidBlock) block).getFluid() != null && "oil".equals(((IFluidBlock) block).getFluid().getName())) {
            return false;
        }

        if (block == Blocks.lava || block == Blocks.flowing_lava) {
            return false;
        }

        return true;
    }

    public static boolean isUnbreakableBlock(World world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();

        return b != null && b.getBlockHardness(world, pos) < 0;
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(World world, BlockPos pos) {
        return !world.getBlockState(pos).getBlock().getMaterial().isToolNotRequired();
    }

    public static boolean isFullFluidBlock(World world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(IBlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            FluidStack fluid = ((IFluidBlock) block).drain(world, pos, false);
            if (fluid == null || fluid.amount > 0) {
                return true;
            }
            return false;
        } else if (block instanceof BlockLiquid) {
            int level = (Integer) state.getValue(BlockLiquid.LEVEL);
            return level == 0;
        }
        return false;
    }

    public static Fluid getFluid(Block block) {
        return FluidRegistry.lookupFluidForBlock(block);
    }

    public static FluidStack drainBlock(World world, BlockPos pos, boolean doDrain) {
        return drainBlock(world.getBlockState(pos), world, pos, doDrain);
    }

    public static FluidStack drainBlock(IBlockState state, World world, BlockPos pos, boolean doDrain) {
        Block block = state.getBlock();
        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

        if (fluid != null && FluidRegistry.isFluidRegistered(fluid)) {
            if (block instanceof IFluidBlock) {
                IFluidBlock fluidBlock = (IFluidBlock) block;
                if (!fluidBlock.canDrain(world, pos)) {
                    return null;
                }
                return fluidBlock.drain(world, pos, doDrain);
            } else {
                int level = (Integer) state.getValue(BlockLiquid.LEVEL);
                if (level != 0) {
                    return null;
                }

                if (doDrain) {
                    world.setBlockToAir(pos);
                }

                return new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
            }
        } else {
            return null;
        }
    }

    /** Create an explosion which only affects a single block. */
    @SuppressWarnings("unchecked")
    public static void explodeBlock(World world, BlockPos pos) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Explosion explosion = new Explosion(world, null, x, y, z, 3f, false, false);
        explosion.func_180343_e().add(pos);
        explosion.doExplosionB(true);

        for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)) {
                continue;
            }

            if (player.getDistanceSq(pos) < 4096) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S27PacketExplosion(x, y, z, 3f, explosion.func_180343_e(), null));
            }
        }
    }

    public static int computeBlockBreakEnergy(World world, BlockPos pos) {
        float hardness = world.getBlockState(pos).getBlock().getBlockHardness(world, pos);
        return (int) Math.floor(BuilderAPI.BREAK_ENERGY * BuildCraftCore.miningMultiplier * ((hardness + 1) * 2));
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */

    private static Chunk getChunkUnforced(World world, int x, int z) {
        Chunk chunk = lastChunk;
        if (chunk != null) {
            if (chunk.isLoaded()) {
                if (chunk.getWorld() == world && chunk.xPosition == x && chunk.zPosition == z) {
                    return chunk;
                }
            } else {
                lastChunk = null;
            }
        }

        chunk = world.getChunkProvider().chunkExists(x, z) ? world.getChunkProvider().provideChunk(x, z) : null;
        lastChunk = chunk;
        return chunk;
    }

    public static Block getBlock(World world, BlockPos pos) {
        return getBlock(world, pos, false);
    }

    public static Block getBlock(World world, BlockPos pos, boolean force) {
        if (!force) {
            if (pos.getY() < 0 || pos.getY() > 255) {
                return Blocks.air;
            }
            Chunk chunk = getChunkUnforced(world, pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null ? chunk.getBlock(pos.getX() & 15, pos.getY(), pos.getZ() & 15) : Blocks.air;
        } else {
            return world.getBlockState(pos).getBlock();
        }
    }

    public static IBlockState getBlockState(World world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static IBlockState getBlockState(World world, BlockPos pos, boolean force) {
        if (force) {
            return world.getBlockState(pos);
        } else {
            if (pos.getY() < 0 || pos.getY() > 255) {
                return Blocks.air.getDefaultState();
            }
            Chunk chunk = getChunkUnforced(world, pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null ? chunk.getBlockState(pos) : Blocks.air.getDefaultState();
        }
    }

    // Meta is hidden internally, so we shouldn't even try
    // public static int getBlockMetadata(World world, int x, int y, int z) {
    // return getBlockMetadata(world, x, y, z, false);
    //
    // }

    // public static int getBlockMetadata(World world, int x, int y, int z, boolean force) {
    // if (!force) {
    // if (y < 0 || y > 255) {
    // return 0;
    // }
    // Chunk chunk = getChunkUnforced(world, x >> 4, z >> 4);
    // return chunk != null ? chunk.getBlockMetadata(x & 15, y, z & 15) : 0;
    // } else {
    // return world.getBlockMetadata(x, y, z);
    // }
    // }

    public static boolean useItemOnBlock(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing direction) {
        boolean done = stack.getItem().onItemUseFirst(stack, player, world, pos, direction, 0.5F, 0.5F, 0.5F);

        if (!done) {
            done = stack.getItem().onItemUse(stack, player, world, pos, direction, 0.5F, 0.5F, 0.5F);
        }
        return done;
    }
}
