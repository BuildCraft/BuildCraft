/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib;

import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO (PASS 0): Rewrite to allow for specifying texture for each side + dimensions, to + from etc...
public class EntityResizableCuboid extends Entity {

    public float shadowSize = 0;
    public float rotationX = 0;
    public float rotationY = 0;
    public float rotationZ = 0;
    public double xSize, ySize, zSize;
    private int brightness = -1;
    public ResourceLocation resource;
    public IBlockState blockState;

    /** The texture that is used for all sides of the cube */
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite texture;

    /** This should be a size 6 array of textures for each side. If a texture is null, it is not rendered. */
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite[] textures;

    /** Where the texture is considered to start. Essentially is used to point the position of the starting point of the
     * texture */
    @SideOnly(Side.CLIENT)
    public double textureStartX = 0, textureStartY = 0, textureStartZ = 0;

    /** The size of the texture before going back to the start. */
    @SideOnly(Side.CLIENT)
    public double textureSizeX = 16, textureSizeY = 16, textureSizeZ = 16;

    /** What the texture should be offset by. Essentially where should the texture start from. (However it repeats back
     * around to textureStart* on the texture)
     * <p>
     * For example, a 1 dimensional texture that looks like this:
     * <p>
     * 0123
     * <p>
     * With textureOffsetX set to 2 (and a textureXSize of 4) would repeat like this:
     * <p>
     * 230123 */
    @SideOnly(Side.CLIENT)
    public double textureOffsetX = 0, textureOffsetY = 0, textureOffsetZ = 0;

    /** An array containing the flips of the textures for each of the sides. 0 is none, 1 flips the U's and 2 flips the
     * V's (3 flips both) */
    @SideOnly(Side.CLIENT)
    public int[] textureFlips;

    public EntityResizableCuboid(World world) {
        super(world);
        ignoreFrustumCheck = true;
        preventEntitySpawning = false;
        noClip = true;
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
    }

    public EntityResizableCuboid(World world, double xPos, double yPos, double zPos) {
        super(world);
        setPositionAndRotation(xPos, yPos, zPos, 0, 0);
    }

    public EntityResizableCuboid(World world, double i, double j, double k, double iSize, double jSize, double kSize) {
        this(world);
        this.xSize = iSize;
        this.ySize = jSize;
        this.zSize = kSize;
        setPositionAndRotation(i, j, k, 0, 0);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
    }

    /** A simple method to initialise all client side only variables if they have not already been initialised. */
    @SideOnly(Side.CLIENT)
    public void makeClient() {
        if (textures == null) {
            textures = new TextureAtlasSprite[6];
            Arrays.fill(textures, texture);
        }
        if (textureFlips == null) {
            textureFlips = new int[6];
        }
    }

    @Override
    public void setPosition(double d, double d1, double d2) {
        super.setPosition(d, d1, d2);
        this.setEntityBoundingBox(AxisAlignedBB.fromBounds(posX, posY, posZ, posX + xSize, posY + ySize, posZ + zSize));
    }

    @Override
    public void moveEntity(double d, double d1, double d2) {
        setPosition(posX + d, posY + d1, posZ + d2);
    }

    public void setSize(Vec3 size) {
        xSize = size.xCoord;
        ySize = size.yCoord;
        zSize = size.zCoord;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    protected void entityInit() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound data) {
        xSize = data.getDouble("iSize");
        ySize = data.getDouble("jSize");
        zSize = data.getDouble("kSize");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound data) {
        data.setDouble("iSize", xSize);
        data.setDouble("jSize", ySize);
        data.setDouble("kSize", zSize);
    }

    @Override
    public int getBrightnessForRender(float par1) {
        return brightness > 0 ? brightness : super.getBrightnessForRender(par1);
    }
}
