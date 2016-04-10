/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityLaser extends Entity {

    public static final ResourceLocation LASER_RED = new ResourceLocation("buildcraftcore:textures/lasers/red.png");
    public static final ResourceLocation LASER_YELLOW = new ResourceLocation("buildcraftcore:textures/lasers/yellow.png");
    public static final ResourceLocation LASER_GREEN = new ResourceLocation("buildcraftcore:textures/lasers/green.png");
    public static final ResourceLocation LASER_BLUE = new ResourceLocation("buildcraftcore:textures/lasers/blue.png");

    public static final ResourceLocation LASER_STRIPES_BLUE = new ResourceLocation("buildcraftcore:textures/lasers/stripes_blue.png");
    public static final ResourceLocation LASER_STRIPES_YELLOW = new ResourceLocation("buildcraftcore:textures/lasers/stripes_yellow.png");

    private static final int NETWORK_HEAD_X = 8;
    private static final int NETWORK_HEAD_Y = 9;
    private static final int NETWORK_HEAD_Z = 10;
    private static final int NETWORK_TAIL_X = 11;
    private static final int NETWORK_TAIL_Y = 12;
    private static final int NETWORK_TAIL_Z = 13;
    private static final int NETWORK_VISIBLE = 14;

    public final LaserData data = new LaserData();

    protected boolean needsUpdate = true;

    private final ResourceLocation laserTexture;

    public static ResourceLocation getTextureFromLaserKind(LaserKind kind) {
        switch (kind) {
            case Blue:
                return LASER_BLUE;
            case Red:
                return LASER_RED;
            case Stripes:
                return LASER_STRIPES_YELLOW;
            default:
                return LASER_STRIPES_BLUE;
        }
    }

    public EntityLaser(World world) {
        this(world, new Vec3d(0, 0, 0), new Vec3d(0, 0, 0));
    }

    public EntityLaser(World world, Vec3d head, Vec3d tail) {
        this(world, head, tail, LASER_RED);
    }

    public EntityLaser(World world, Vec3d head, Vec3d tail, LaserKind kind) {
        this(world, head, tail, getTextureFromLaserKind(kind));
    }

    public EntityLaser(World world, Vec3d head, Vec3d tail, ResourceLocation laserTexture) {
        super(world);

        data.head = head;
        data.tail = tail;

        setPositionAndRotation(head.xCoord, head.yCoord, head.zCoord, 0, 0);
        setSize(10, 10);

        this.laserTexture = laserTexture;

        updateDataServer();
    }

    @Override
    protected void entityInit() {
        preventEntitySpawning = false;
        noClip = true;
        isImmuneToFire = true;
        dataWatcher.addObject(NETWORK_HEAD_X, (float) 0);
        dataWatcher.addObject(NETWORK_HEAD_Y, (float) 0);
        dataWatcher.addObject(NETWORK_HEAD_Z, (float) 0);
        dataWatcher.addObject(NETWORK_TAIL_X, (float) 0);
        dataWatcher.addObject(NETWORK_TAIL_Y, (float) 0);
        dataWatcher.addObject(NETWORK_TAIL_Z, (float) 0);

        dataWatcher.addObject(NETWORK_VISIBLE, (byte) 0);
    }

    @Override
    public void onUpdate() {
        if (data.head == null || data.tail == null) {
            return;
        }

        if (!worldObj.isRemote && needsUpdate) {
            updateDataServer();
            needsUpdate = false;
        }

        if (worldObj.isRemote) {
            updateDataClient();
        }

        // TODO (1.8): Avoid Object Overflow
        // Err... what?
        setEntityBoundingBox(new AxisAlignedBB(Math.min(data.head.xCoord, data.tail.xCoord), Math.min(data.head.yCoord, data.tail.yCoord) - 1.0D, Math
                .min(data.head.zCoord, data.tail.zCoord) - 1.0D, Math.max(data.head.xCoord, data.tail.xCoord) + 1.0D, Math.max(data.head.yCoord,
                        data.tail.yCoord) + 1.0D, Math.max(data.head.zCoord, data.tail.zCoord) + 1.0D));

        data.update();
    }

    protected void updateDataClient() {
        data.isVisible = dataWatcher.getWatchableObjectByte(NETWORK_VISIBLE) == 1;
    }

    protected void updateDataServer() {
        dataWatcher.updateObject(NETWORK_HEAD_X, (float) data.head.xCoord);
        dataWatcher.updateObject(NETWORK_HEAD_Y, (float) data.head.yCoord);
        dataWatcher.updateObject(NETWORK_HEAD_Z, (float) data.head.zCoord);
        dataWatcher.updateObject(NETWORK_TAIL_X, (float) data.tail.xCoord);
        dataWatcher.updateObject(NETWORK_TAIL_Y, (float) data.tail.yCoord);
        dataWatcher.updateObject(NETWORK_TAIL_Z, (float) data.tail.zCoord);
        dataWatcher.updateObject(NETWORK_VISIBLE, (byte) (data.isVisible ? 1 : 0));
    }

    public void setPositions(Vec3d head, Vec3d tail) {
        data.head = head;
        data.tail = tail;

        setPositionAndRotation(head.xCoord, head.yCoord, head.zCoord, 0, 0);

        needsUpdate = true;
    }

    public void show() {
        data.isVisible = true;
        needsUpdate = true;
    }

    public void hide() {
        data.isVisible = false;
        needsUpdate = true;
    }

    public boolean isVisible() {
        return data.isVisible;
    }

    public ResourceLocation getTexture() {
        return laserTexture;
    }

    protected double decodeDouble(float i) {
        return i;
    }

    // The read/write to nbt seem to be useless

    // Yes it is- we never want to persist this entity.

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        double headX = nbt.getDouble("headX");
        double headY = nbt.getDouble("headZ");
        double headZ = nbt.getDouble("headY");
        data.head = new Vec3d(headX, headY, headZ);

        double tailX = nbt.getDouble("tailX");
        double tailY = nbt.getDouble("tailZ");
        double tailZ = nbt.getDouble("tailY");
        data.tail = new Vec3d(tailX, tailY, tailZ);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setDouble("headX", data.head.xCoord);
        nbt.setDouble("headY", data.head.yCoord);
        nbt.setDouble("headZ", data.head.zCoord);

        nbt.setDouble("tailX", data.tail.xCoord);
        nbt.setDouble("tailY", data.tail.yCoord);
        nbt.setDouble("tailZ", data.tail.zCoord);
    }

    // Workaround for the laser's posY loosing it's precision e.g 103.5 becomes 104
    public Vec3d renderOffset() {
        return new Vec3d(0.5, 0.5, 0.5);
    }

    @Override
    public int getBrightnessForRender(float par1) {
        return 210;
    }
}
