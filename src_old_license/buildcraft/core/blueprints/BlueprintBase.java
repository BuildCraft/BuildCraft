/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.blueprints;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.utils.Utils;
<<<<<<< HEAD
import buildcraft.lib.misc.Matrix4i;
import buildcraft.lib.misc.NBTUtilBC;
=======
import buildcraft.lib.misc.NBTUtils;
>>>>>>> 4c6b9beacc58bd9c05649db40ea24df03d21fc98
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.Matrix4i;

public abstract class BlueprintBase {

    public ArrayList<NBTTagCompound> subBlueprintsNBT = new ArrayList<>();

    public BlockPos anchor = Utils.POS_ZERO;
    public BlockPos size = Utils.POS_ONE;
    public LibraryId id = new LibraryId();
    public String author;
    public boolean rotate = true;
    public boolean excavate = true;
    public BuildingPermission buildingPermission = BuildingPermission.ALL;
    public boolean isComplete = true;

    protected MappingRegistry mapping = new MappingRegistry();
    private SchematicBlockBase[][][] contents;

    private NBTTagCompound nbt;
    private EnumFacing mainDir = EnumFacing.EAST;

    public BlueprintBase() {}

    @Deprecated
    public BlueprintBase(int sizeX, int sizeY, int sizeZ) {
        this(new BlockPos(sizeX, sizeY, sizeZ));
    }

    public BlueprintBase(BlockPos size) {
        contents = new SchematicBlockBase[size.getX()][size.getY()][size.getZ()];
        this.size = size;
        this.anchor = Utils.POS_ZERO;
    }

    public SchematicBlockBase get(BlockPos pos) {
        String error = "Tried to access the " + pos + " when the maximum ";
        if (contents.length <= pos.getX()) throw new ArrayIndexOutOfBoundsException(error + "X coord was " + (contents.length - 1));
        SchematicBlockBase[][] arr2 = contents[pos.getX()];
        if (arr2.length <= pos.getY()) throw new ArrayIndexOutOfBoundsException(error + "Y coord was " + (arr2.length - 1));
        SchematicBlockBase[] arr1 = arr2[pos.getY()];
        if (arr1.length <= pos.getZ()) throw new ArrayIndexOutOfBoundsException(error + "Z coord was " + (arr1.length - 1));
        return arr1[pos.getZ()];
    }

    public void set(BlockPos pos, SchematicBlockBase schematic) {
        contents[pos.getX()][pos.getY()][pos.getZ()] = schematic;
    }

    public void translateToBlueprint(Vec3d transform) {
        for (SchematicBlockBase[][] arr2 : contents)
            for (SchematicBlockBase[] arr1 : arr2)
                for (SchematicBlockBase content : arr1)
                    if (content != null) content.translateToBlueprint(transform);
    }

    public void translateToWorld(Vec3d transform) {
        for (SchematicBlockBase[][] arr2 : contents)
            for (SchematicBlockBase[] arr1 : arr2)
                for (SchematicBlockBase content : arr1)
                    if (content != null) content.translateToWorld(transform);
    }

    public void rotateLeft(BptContext context) {
        SchematicBlockBase[][][] newContents = new SchematicBlockBase[size.getZ()][size.getY()][size.getX()];

        Matrix4i leftRot = Matrix4i.makeRotLeftTranslatePositive(new Box(BlockPos.ORIGIN, size.subtract(Utils.POS_ONE)));

        for (BlockPos internal : BlockPos.getAllInBox(Utils.POS_ZERO, size.subtract(Utils.POS_ONE))) {
            BlockPos rotated = leftRot.multiplyPosition(internal);

            SchematicBlockBase oldContents = contents[internal.getX()][internal.getY()][internal.getZ()];
            if (oldContents != null) {
                oldContents.rotateLeft(context);
                newContents[rotated.getX()][rotated.getY()][rotated.getZ()] = oldContents;
            }
        }

        contents = newContents;
        size = new BlockPos(size.getZ(), size.getY(), size.getX());

        BlockPos newAnchor = leftRot.multiplyPosition(anchor);

        for (NBTTagCompound sub : subBlueprintsNBT) {
            EnumFacing dir = EnumFacing.values()[sub.getByte("dir")];

            if (dir.getAxis() != Axis.Y) dir = dir.rotateY();

            Vec3d pos = new Vec3d(sub.getInteger("x"), sub.getInteger("y"), sub.getInteger("z"));
            Vec3d rotated = context.rotatePositionLeft(pos);

            sub.setInteger("x", (int) rotated.xCoord);
            sub.setInteger("z", (int) rotated.zCoord);
            sub.setByte("dir", (byte) dir.ordinal());
        }

        context.rotateLeft();

        anchor = newAnchor;

        if (mainDir.getAxis() != Axis.Y) mainDir = mainDir.rotateY();
    }

    private void writeToNBTInternal(NBTTagCompound nbt) {
        nbt.setString("version", DefaultProps.VERSION);

        if (this instanceof Template) {
            nbt.setString("kind", "template");
        } else {
            nbt.setString("kind", "blueprint");
        }

        nbt.setBoolean("rotate", rotate);
        nbt.setBoolean("excavate", excavate);

        nbt.setTag("size", NBTUtilBC.writeBlockPos(size));
        nbt.setTag("anchor", NBTUtilBC.writeBlockPos(anchor));

        if (author != null) {
            nbt.setString("author", author);
        }

        saveContents(nbt);

        NBTTagList subBptList = new NBTTagList();

        for (NBTTagCompound subBpt : subBlueprintsNBT) {
            subBptList.appendTag(subBpt);
        }

        nbt.setTag("subBpt", subBptList);
    }

    public static BlueprintBase loadBluePrint(NBTTagCompound nbt) {
        String kind = nbt.getString("kind");

        BlueprintBase bpt;

        if ("template".equals(kind)) {
            bpt = new Template();
        } else {
            bpt = new Blueprint();
        }

        bpt.readFromNBT(nbt);

        return bpt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("sizeX")) {
            size = new BlockPos(nbt.getInteger("sizeX"), nbt.getInteger("sizeY"), nbt.getInteger("sizeZ"));
        } else size = NBTUtilBC.readBlockPos(nbt.getTag("size"));

        if (nbt.hasKey("anchorX")) {
            anchor = new BlockPos(nbt.getInteger("anchorX"), nbt.getInteger("anchorY"), nbt.getInteger("anchorZ"));
        } else anchor = NBTUtilBC.readBlockPos(nbt.getTag("anchor"));

        author = nbt.getString("author");

        if (nbt.hasKey("rotate")) {
            rotate = nbt.getBoolean("rotate");
        } else {
            rotate = true;
        }

        if (nbt.hasKey("excavate")) {
            excavate = nbt.getBoolean("excavate");
        } else {
            excavate = true;
        }

        contents = new SchematicBlockBase[size.getX()][size.getY()][size.getZ()];

        try {
            loadContents(nbt);
        } catch (BptError e) {
            e.printStackTrace();
        }

        if (nbt.hasKey("subBpt")) {
            NBTTagList subBptList = nbt.getTagList("subBpt", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < subBptList.tagCount(); ++i) {
                subBlueprintsNBT.add(subBptList.getCompoundTagAt(i));
            }
        }
    }

    public Box getBoxForPos(BlockPos pos) {
        BlockPos min = pos.add(anchor);
        BlockPos max = min.add(size).subtract(Utils.POS_ONE);
        return new Box(min, max);
    }

    public BptContext getContext(World world, Box box) {
        return new BptContext(world, box, mapping);
    }

    public void addSubBlueprint(BlueprintBase subBpt, BlockPos pos, EnumFacing dir) {
        NBTTagCompound subNBT = new NBTTagCompound();

        subNBT.setInteger("x", pos.getX());
        subNBT.setInteger("y", pos.getY());
        subNBT.setInteger("z", pos.getZ());
        subNBT.setByte("dir", (byte) dir.ordinal());
        subNBT.setTag("bpt", subBpt.getNBT());

        subBlueprintsNBT.add(subNBT);
    }

    public NBTTagCompound getNBT() {
        if (nbt == null) {
            nbt = new NBTTagCompound();
            writeToNBTInternal(nbt);
        }
        return nbt;
    }

    public BlueprintBase adjustToWorld(World world, BlockPos pos, EnumFacing o) {
        if (buildingPermission == BuildingPermission.NONE || (buildingPermission == BuildingPermission.CREATIVE_ONLY && world.getWorldInfo()
                .getGameType() != GameType.CREATIVE)) {
            return null;
        }

        BptContext context = getContext(world, getBoxForPos(pos));

        if (rotate) {
            if (o == EnumFacing.EAST) {
                // Do nothing
            } else if (o == EnumFacing.SOUTH) {
                rotateLeft(context);
            } else if (o == EnumFacing.WEST) {
                rotateLeft(context);
                rotateLeft(context);
            } else if (o == EnumFacing.NORTH) {
                rotateLeft(context);
                rotateLeft(context);
                rotateLeft(context);
            }
        }

        Vec3d transform = Utils.convert(pos).subtract(new Vec3d(anchor));

        translateToWorld(transform);

        return this;
    }

    public abstract void loadContents(NBTTagCompound nbt) throws BptError;

    public abstract void saveContents(NBTTagCompound nbt);

    public abstract void readFromWorld(IBuilderContext context, TileEntity anchorTile, BlockPos pos);

    public abstract ItemStack getStack();

    public void readEntitiesFromWorld(IBuilderContext context, TileEntity anchorTile) {

    }
}
