/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.factory.tile.TileTank;

public class BlockTank extends BlockBuildCraft {
    private static final boolean DEBUG_MODE = false;
    private TextureAtlasSprite textureStackedSide;

    public BlockTank() {
        super(Material.glass, JOINED_BELOW);
        setBlockBounds(0.125F, 0F, 0.125F, 0.875F, 1F, 0.875F);
        setHardness(0.5F);
        setCreativeTab(BCCreativeTab.get("main"));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof TileTank) {
            TileTank tank = (TileTank) tile;
            tank.onBlockBreak();
        }

        TileEntity tileAbove = world.getTileEntity(x, y + 1, z);
        TileEntity tileBelow = world.getTileEntity(x, y - 1, z);

        super.breakBlock(world, pos, block, par6);

        if (tileAbove instanceof TileTank) {
            ((TileTank) tileAbove).updateComparators();
        }

        if (tileBelow instanceof TileTank) {
            ((TileTank) tileBelow).updateComparators();
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileTank();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, state, entityplayer, side, par7, par8, par9)) {
            return true;
        }

        ItemStack current = entityplayer.inventory.getCurrentItem();

        if (current != null) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileTank) {
                TileTank tank = (TileTank) tile;
                // Handle FluidContainerRegistry
                if (FluidContainerRegistry.isContainer(current)) {
                    FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
                    // Handle filled containers
                    if (liquid != null) {
                        int qty = tank.fill(null, liquid, true);

                        if (qty != 0 && !BuildCraftCore.debugWorldgen && !entityplayer.capabilities.isCreativeMode) {
                            if (current.stackSize > 1) {
                                if (!entityplayer.inventory.addItemStackToInventory(FluidContainerRegistry.drainFluidContainer(current))) {
                                    entityplayer.dropPlayerItemWithRandomChoice(FluidContainerRegistry.drainFluidContainer(current), false);
                                }

                                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils.consumeItem(current));
                            } else {
                                entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, FluidContainerRegistry
                                    .drainFluidContainer(current));
                            }
                        }

                        return true;

                        // Handle empty containers
                    } else {
                        FluidStack available = tank.getTankInfo(null)[0].fluid;

                        if (available != null) {
                            ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

                            liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

                            if (liquid != null) {
                                if (!BuildCraftCore.debugWorldgen && !entityplayer.capabilities.isCreativeMode) {
                                    if (current.stackSize > 1) {
                                        if (!entityplayer.inventory.addItemStackToInventory(filled)) {
                                            return false;
                                        } else {
                                            entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils
                                                .consumeItem(current));
                                        }
                                    } else {
                                        entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, InvUtils
                                            .consumeItem(current));
                                        entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, filled);
                                    }
                                }

                                tank.drain(null, liquid.amount, true);

                                return true;
                            }
                        }
                    }
                } else if (current.getItem() instanceof IFluidContainerItem) {
                    if (current.stackSize != 1) {
                        return false;
                    }

                    if (!world.isRemote) {
                        IFluidContainerItem container = (IFluidContainerItem) current.getItem();
                        FluidStack liquid = container.getFluid(current);
                        FluidStack tankLiquid = tank.getTankInfo(null)[0].fluid;
                        boolean mustDrain = liquid == null || liquid.amount == 0;
                        boolean mustFill = tankLiquid == null || tankLiquid.amount == 0;
                        if (mustDrain && mustFill) {
                            // Both are empty, do nothing
                        } else if (mustDrain || !entityplayer.isSneaking()) {
                            liquid = tank.drain(null, 1000, false);
                            int qtyToFill = container.fill(current, liquid, true);
                            tank.drain(null, qtyToFill, true);
                        } else if (mustFill || entityplayer.isSneaking()) {
                            if (liquid != null && liquid.amount > 0) {
                                int qty = tank.fill(null, liquid, false);
                                tank.fill(null, container.drain(current, qty, true), true);
                            }
                        }
                    }

                    return true;
                }
            }
        } else if (DEBUG_MODE) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TileTank) {
                TileTank tank = (TileTank) tile;
                if (tank.getTankInfo(null)[0].fluid != null) {
                    entityplayer.addChatComponentMessage(new ChatComponentText("Amount: " + tank.getTankInfo(null)[0].fluid.amount + " mB"));
                }
            }
        }

        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y) {
            return world.getBlockState(pos).getBlock() != this;
        } else {
            return super.shouldSideBeRendered(world, pos, side);
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileTank) {
            TileTank tank = (TileTank) tile;
            return tank.getFluidLightLevel();
        }

        return super.getLightValue(world, pos);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileTank) {
            TileTank tank = (TileTank) tile;
            return tank.getComparatorInputOverride();
        }

        return 0;
    }
}
