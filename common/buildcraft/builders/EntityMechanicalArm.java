/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.core.lib.EntityResizableCuboid;

public class EntityMechanicalArm extends Entity {

    protected TileQuarry parent;

    private double armSizeX;
    private double armSizeZ;
    private Vec3 root, head;

    private EntityResizableCuboid xArm, yArm, zArm, headEntity;

    public EntityMechanicalArm(World world) {
        super(world);
        makeParts(world);
        noClip = true;
    }

    public EntityMechanicalArm(World world, Vec3 root, double width, double height, TileQuarry parent) {
        this(world);
        setPositionAndRotation(parent.getPos().getX(), parent.getPos().getY(), parent.getPos().getZ(), 0, 0);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.root = root;
        setHead(root.addVector(0, -2, 0));
        setArmSize(width, height);
        this.parent = parent;
        parent.setArm(this);
        updatePosition();
    }

    public void setHead(Vec3 vec) {
        this.head = vec;
    }

    private void setArmSize(double x, double z) {
        armSizeX = x;
        xArm.xSize = x;
        armSizeZ = z;
        zArm.zSize = z;
        updatePosition();
    }

    private void makeParts(World world) {
        xArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 1, 0.5, 0.5);
        yArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 1, 0.5);
        zArm = BuilderProxy.proxy.newDrill(world, 0, 0, 0, 0.5, 0.5, 1);

        headEntity = BuilderProxy.proxy.newDrillHead(world, 0, 0, 0, 0.2, 1, 0.2);
        headEntity.shadowSize = 1.0F;

        world.spawnEntityInWorld(xArm);
        world.spawnEntityInWorld(yArm);
        world.spawnEntityInWorld(zArm);
        world.spawnEntityInWorld(headEntity);
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        double xRoot = nbttagcompound.getDouble("xRoot");
        double yRoot = nbttagcompound.getDouble("yRoot");
        double zRoot = nbttagcompound.getDouble("zRoot");
        root = new Vec3(xRoot, yRoot, zRoot);
        armSizeX = nbttagcompound.getDouble("armSizeX");
        armSizeZ = nbttagcompound.getDouble("armSizeZ");
        setArmSize(armSizeX, armSizeZ);
        updatePosition();
    }

    private void findAndJoinQuarry() {
        TileEntity te = worldObj.getTileEntity(new BlockPos((int) posX, (int) posY, (int) posZ));
        if (te instanceof TileQuarry) {
            parent = (TileQuarry) te;
            parent.setArm(this);
        } else {
            setDead();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setDouble("xRoot", root.xCoord);
        nbttagcompound.setDouble("yRoot", root.yCoord);
        nbttagcompound.setDouble("zRoot", root.zCoord);
        nbttagcompound.setDouble("armSizeX", armSizeX);
        nbttagcompound.setDouble("armSizeZ", armSizeZ);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        updatePosition();
        if (parent == null) {
            findAndJoinQuarry();
        }

        if (parent == null) {
            setDead();
            return;
        }
    }

    public void updatePosition() {
        // HEAD MAY BE NULL!
        this.xArm.setPosition(root.xCoord, root.yCoord, head.zCoord + 0.25);
        this.yArm.ySize = root.yCoord - head.yCoord - 1;
        this.yArm.setPosition(head.xCoord + 0.25, head.yCoord + 1, head.zCoord + 0.25);
        this.zArm.setPosition(head.xCoord + 0.25, root.yCoord, root.zCoord);
        this.headEntity.setPosition(head.xCoord + 0.4, head.yCoord - 0.01, head.zCoord + 0.4);
    }

    @Override
    public void setDead() {
        if (worldObj != null && worldObj.isRemote) {
            xArm.setDead();
            yArm.setDead();
            zArm.setDead();
            headEntity.setDead();
        }
        super.setDead();
    }
}
