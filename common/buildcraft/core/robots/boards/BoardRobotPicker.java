package buildcraft.core.robots.boards;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.RobotAIMoveTo;
import buildcraft.core.robots.RobotAIReturnToDock;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public class BoardRobotPicker implements IRedstoneBoardRobot<EntityRobot> {

	private static Set<Integer> targettedItems = new HashSet<Integer>();

	private SafeTimeTracker scanTracker = new SafeTimeTracker(40, 10);
	private SafeTimeTracker pickTracker = new SafeTimeTracker(20, 0);
	private SafeTimeTracker unloadTracker = new SafeTimeTracker(20, 0);
	private EntityItem target;
	private int pickTime = -1;

	private NBTTagCompound data;

	private RedstoneBoardNBT board;
	private IBoardParameter[] params;
	private int range;
	private IStackFilter stackFilter;
	private boolean initialized = false;

	public BoardRobotPicker(NBTTagCompound nbt) {
		data = nbt;

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
		params = board.getParameters(nbt);
	}

	@Override
	public void updateBoard(EntityRobot robot) {
		TransactorSimple inventoryInsert = new TransactorSimple(robot);

		if (robot.worldObj.isRemote) {
			return;
		}

		if (!initialized) {
			range = data.getInteger("range");

			IBoardParameter[] params = board.getParameters(data);
			ItemStack[] stacks = new ItemStack[params.length];

			for (int i = 0; i < stacks.length; ++i) {
				IBoardParameterStack pStak = (IBoardParameterStack) params[i];
				stacks[i] = pStak.getStack();
			}

			if (stacks.length > 0) {
				stackFilter = new ArrayStackFilter(stacks);
			} else {
				stackFilter = null;
			}

			initialized = true;
		}

		if (target != null) {
			if (target.isDead) {
				targettedItems.remove(target.getEntityId());
				target = null;
				robot.setMainAI(new RobotAIReturnToDock(robot));
				scan(robot);
			} else if (pickTime == -1) {
				if (robot.currentAI.isDone()) {
					robot.setLaserDestination((float) target.posX, (float) target.posY, (float) target.posZ);
					pickTracker = new SafeTimeTracker(200);
					pickTime = 0;
				}
			} else {
				pickTime++;

				if (pickTime > 20) {
					target.getEntityItem().stackSize -= inventoryInsert.inject(
							target.getEntityItem(), ForgeDirection.UNKNOWN,
							true);

					if (target.getEntityItem().stackSize <= 0) {
						target.setDead();
					}
				}
			}
		} else {
			if (robot.isDocked) {
				TileGenericPipe pipe = (TileGenericPipe) robot.worldObj
						.getTileEntity(robot.dockingStation.x, robot.dockingStation.y,
								robot.dockingStation.z);

				if (pipe != null && pipe.pipe.transport instanceof PipeTransportItems) {
					if (unloadTracker.markTimeIfDelay(robot.worldObj)) {
						for (int i = 0; i < robot.getSizeInventory(); ++i) {
							if (robot.getStackInSlot(i) != null) {
								float cx = robot.dockingStation.x + 0.5F + 0.2F * robot.dockingStation.side.offsetX;
								float cy = robot.dockingStation.y + 0.5F + 0.2F * robot.dockingStation.side.offsetY;
								float cz = robot.dockingStation.z + 0.5F + 0.2F * robot.dockingStation.side.offsetZ;

								TravelingItem item = TravelingItem.make(cx, cy,
										cz, robot.getStackInSlot(i));

								((PipeTransportItems) pipe.pipe.transport)
										.injectItem(item, robot.dockingStation.side.getOpposite());

								robot.setInventorySlotContents(i, null);

								break;
							}
						}
					}
				}
			}

			if (scanTracker.markTimeIfDelay(robot.worldObj)) {
				scan(robot);
			}
		}
	}

	public void scan(EntityRobot robot) {
		TransactorSimple inventoryInsert = new TransactorSimple(robot);

		for (Object o : robot.worldObj.loadedEntityList) {
			Entity e = (Entity) o;

			if (!e.isDead && e instanceof EntityItem && !targettedItems.contains(e.getEntityId())) {
				double dx = e.posX - robot.posX;
				double dy = e.posY - robot.posY;
				double dz = e.posZ - robot.posZ;

				double sqrDistance = dx * dx + dy * dy + dz * dz;
				double maxDistance = range * range;

				if (sqrDistance >= maxDistance) {
					continue;
				} else if (stackFilter != null && !stackFilter.matches(((EntityItem) e).getEntityItem())) {
					continue;
				} else {
					EntityItem item = (EntityItem) e;

					if (inventoryInsert.inject(item.getEntityItem(),
							ForgeDirection.UNKNOWN, false) > 0) {

						target = item;
						targettedItems.add(e.getEntityId());
						robot.isDocked = false;
						robot.setMainAI(new RobotAIMoveTo(robot, (float) e.posX,
								(float) e.posY, (float) e.posZ));
						pickTime = -1;

						break;
					}
				}
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPickerNBT.instance;
	}
}
