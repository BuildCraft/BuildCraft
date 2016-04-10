/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;

public class PipeItemsObsidian extends Pipe<PipeTransportItems> implements IEnergyReceiver {
    private final RFBattery battery = new RFBattery(2560, 640, 0);
    private final WeakHashMap<Entity, Long> entityDropTime = new WeakHashMap<>();

    public PipeItemsObsidian(Item item) {
        super(new PipeTransportItems(), item);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsObsidian.ordinal();
    }

    @Override
    public void onEntityCollidedWithBlock(Entity entity) {
        super.onEntityCollidedWithBlock(entity);

        if (entity.isDead) {
            return;
        }

        if (canSuck(entity, 0)) {
            pullItemIntoPipe(entity, 0);
        }
    }

    private AxisAlignedBB getSuckingBox(EnumFacing orientation, int distance) {
        if (orientation == null) {
            return null;
        }
        Vec3d p1 = Utils.convert(container.getPos()).add(Utils.VEC_HALF);
        Vec3d p2 = p1;

        switch (orientation) {
            case EAST:
                p1 = p1.addVector(distance, 0, 0);
                p2 = p2.addVector(distance + 1, 0, 0);
                break;
            case WEST:
                p1 = p1.addVector(-distance - 1, 0, 0);
                p2 = p2.addVector(-distance, 0, 0);
                break;
            case UP:
            case DOWN:
                p1 = p1.addVector(distance + 1, 0, distance + 1);
                p2 = p2.addVector(-distance, 0, -distance);
                break;
            case SOUTH:
                p1 = p1.addVector(0, 0, distance);
                p2 = p2.addVector(0, 0, distance + 1);
                break;
            case NORTH:
            default:
                p1 = p1.addVector(0, 0, -distance - 1);
                p2 = p2.addVector(0, 0, -distance);
                break;
        }

        switch (orientation) {
            case EAST:
            case WEST:
                p1 = p1.addVector(0, distance + 1, distance + 1);
                p2 = p2.addVector(0, -distance, -distance);
                break;
            case UP:
                p1 = p1.addVector(0, distance + 1, 0);
                p2 = p2.addVector(0, distance, 0);
                break;
            case DOWN:
                p1 = p1.addVector(0, -distance - 1, 0);
                p2 = p2.addVector(0, -distance, 0);
                break;
            case SOUTH:
            case NORTH:
            default:
                p1 = p1.addVector(distance + 1, distance + 1, 0);
                p2 = p2.addVector(-distance, -distance, 0);
                break;
        }

        Vec3d min = Utils.min(p1, p2);
        Vec3d max = Utils.max(p1, p2);

        return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (battery.getEnergyStored() > 0) {
            for (int j = 1; j < 5; ++j) {
                if (suckItem(j)) {
                    return;
                }
            }

            battery.useEnergy(0, 5, false);
        }
    }

    private boolean suckItem(int distance) {
        EnumFacing openOrientation = getOpenOrientation();
        if (openOrientation == null) {
            return false;
        }
        AxisAlignedBB box = getSuckingBox(openOrientation, distance);

        if (box == null) {
            return false;
        }

        List<Entity> discoveredEntities = container.getWorld().getEntitiesWithinAABB(Entity.class, box);

        for (Entity entity : discoveredEntities) {
            if (canSuck(entity, distance)) {
                pullItemIntoPipe(entity, distance);
                return true;
            }

            if (distance == 1 && entity instanceof EntityMinecart && entity instanceof IInventory) {
                EntityMinecart cart = (EntityMinecart) entity;
                if (!cart.isDead) {
                    ITransactor trans = Transactor.getTransactorFor(cart, openOrientation);
                    ItemStack stack = trans.remove(StackFilter.ALL, false);

                    if (stack != null && battery.useEnergy(10, 10, false) > 0) {
                        stack = trans.remove(StackFilter.ALL, true);
                        if (stack != null) {
                            TravelingItem item = TravelingItem.make(0.5f, stack);
                            return transport.injectItem(item, openOrientation.getOpposite(), true);
                        }
                    }
                }
            }
        }

        return false;
    }

    public void pullItemIntoPipe(Entity entity, int distance) {
        // TODO: Add injectItem error handling

        if (container.getWorld().isRemote) {
            return;
        }

        EnumFacing orientation = getOpenOrientation();
        if (orientation != null) {
            orientation = orientation.getOpposite();
            container.getWorld().playSoundAtEntity(entity, "random.pop", 0.2F, ((container.getWorld().rand.nextFloat() - container.getWorld().rand
                    .nextFloat()) * 0.7F + 1.0F) * 2.0F);

            ItemStack stack;

            double speed = 0.01F;

            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                ItemStack contained = item.getEntityItem();

                if (contained == null) {
                    return;
                }

                TransportProxy.proxy.obsidianPipePickup(container.getWorld(), item, this.container);

                int energyUsed = Math.min(10 * contained.stackSize * distance, battery.getEnergyStored());

                if (distance == 0 || energyUsed / distance / 10 == contained.stackSize) {
                    stack = contained;
                    CoreProxy.proxy.removeEntity(entity);
                } else {
                    stack = contained.splitStack(energyUsed / distance / 10);
                }

                battery.useEnergy(energyUsed, energyUsed, false);

                speed = Math.sqrt(item.motionX * item.motionX + item.motionY * item.motionY + item.motionZ * item.motionZ);
                speed = speed / 2F - 0.05;

                if (speed < 0.01) {
                    speed = 0.01;
                }
            } else if (entity instanceof EntityArrow && battery.useEnergy(distance * 10, distance * 10, false) > 0) {
                stack = new ItemStack(Items.arrow, 1);
                CoreProxy.proxy.removeEntity(entity);
            } else {
                return;
            }

            if (stack == null) {
                return;
            }

            TravelingItem item = TravelingItem.make(0.5f, stack);

            item.setSpeed((float) speed);
            transport.injectItem(item, orientation, true);
        }
    }

    public void eventHandler(PipeEventItem.DropItem event) {
        entityDropTime.put(event.entity, event.entity.worldObj.getTotalWorldTime() + 200);
    }

    public boolean canSuck(Entity entity, int distance) {
        if (!entity.isEntityAlive()) {
            return false;
        }
        if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem) entity;

            if (item.getEntityItem().stackSize <= 0) {
                return false;
            }

            long wt = entity.worldObj.getTotalWorldTime();
            if (entityDropTime.containsKey(entity) && entityDropTime.get(entity) >= wt) {
                return false;
            }

            return battery.getEnergyStored() >= distance * 10;
        } else if (entity instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) entity;
            return arrow.canBePickedUp == 1 && battery.getEnergyStored() >= distance * 10;
        }
        return false;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return battery.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return battery.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return battery.getMaxEnergyStored();
    }
}
