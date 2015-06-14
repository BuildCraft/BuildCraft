/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityLaser extends Entity {

    public static final ResourceLocation LASER_RED = new ResourceLocation("buildcraft:textures/entities/laser_1.png");
    public static final ResourceLocation LASER_YELLOW = new ResourceLocation("buildcraft:textures/entities/laser_2.png");
    public static final ResourceLocation LASER_GREEN = new ResourceLocation("buildcraft:textures/entities/laser_3.png");
    public static final ResourceLocation LASER_BLUE = new ResourceLocation("buildcraft:textures/entities/laser_4.png");

    public static final ResourceLocation LASER_STRIPES_BLUE = new ResourceLocation("buildcraft:textures/entities/blue_stripes.png");
    public static final ResourceLocation LASER_STRIPES_YELLOW = new ResourceLocation("buildcraft:textures/entities/stripes.png");

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
        this(world, new Vec3(0, 0, 0), new Vec3(0, 0, 0));
    }

    public EntityLaser(World world, Vec3 head, Vec3 tail) {
        this(world, head, tail, LASER_RED);
    }

    public EntityLaser(World world, Vec3 head, Vec3 tail, LaserKind kind) {
        this(world, head, tail, getTextureFromLaserKind(kind));
    }

    public EntityLaser(World world, Vec3 head, Vec3 tail, ResourceLocation laserTexture) {
        super(world);

        data.head = head;
        data.tail = tail;

        setPositionAndRotation(head.x, head.y, head.z, 0, 0);
        setSize(10, 10);

        this.laserTexture = laserTexture;
    }

    @Override
    protected void entityInit() {
        preventEntitySpawning = false;
        noClip = true;
        isImmuneToFire = true;
        dataWatcher.addObject(8, Byte.valueOf((byte) 1));
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
        setEntityBoundingBox(new AxisAlignedBB(Math.min(data.head.x, data.tail.x), Math.min(data.head.y, data.tail.y) - 1.0D, Math.min(data.head.z,
            data.tail.z) - 1.0D, Math.max(data.head.x, data.tail.x) + 1.0D, Math.max(data.head.y, data.tail.y) + 1.0D, Math.max(data.head.z,
            data.tail.z) + 1.0D));

        data.update();
    }

    protected void updateDataClient() {
        data.isVisible = dataWatcher.getWatchableObjectByte(8) == 1;
    }

    protected void updateDataServer() {
        dataWatcher.updateObject(8, Byte.valueOf((byte) (data.isVisible ? 1 : 0)));
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

    protected int encodeDouble(double d) {
        return (int) (d * 8192);
    }

    protected double decodeDouble(int i) {
        return i / 8192D;
    }

    // The read/write to nbt seem to be useless

    // Yes it is- we never want to persist this entity.

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        double headX = nbt.getDouble("headX");
        double headY = nbt.getDouble("headZ");
        double headZ = nbt.getDouble("headY");
        data.head = new Vec3(headX, headY, headZ);

        double tailX = nbt.getDouble("tailX");
        double tailY = nbt.getDouble("tailZ");
        double tailZ = nbt.getDouble("tailY");
        data.tail = new Vec3(tailX, tailY, tailZ);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setDouble("headX", data.head.x);
        nbt.setDouble("headY", data.head.y);
        nbt.setDouble("headZ", data.head.z);

        nbt.setDouble("tailX", data.tail.x);
        nbt.setDouble("tailY", data.tail.y);
        nbt.setDouble("tailZ", data.tail.z);
    }

    // Workaround for the laser's posY loosing it's precision e.g 103.5 becomes 104
    public Vec3 renderOffset() {
        return new Vec3(0.5, 0.5, 0.5);
    }

    @Override
    public int getBrightnessForRender(float par1) {
        return 210;
    }
}
