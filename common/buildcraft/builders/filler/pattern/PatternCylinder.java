/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.api.core.IBox;
import buildcraft.api.core.Position;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PatternCylinder extends FillerPattern {

        public PatternCylinder() {
                super("cylinder");
        }

        @Override
        public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
                int xMin = (int)box.pMin().x;
                int yMin = (int)box.pMin().y;
                int zMin = (int)box.pMin().z;

                int xMax = (int)box.pMax().x;
                int yMax = (int)box.pMax().y;
                int zMax = (int)box.pMax().z;

                int xFix = (xMax-xMin)%2;
                int zFix = (zMax-zMin)%2;

                int xCenter = (xMax+xMin)/2 + (xMax+xMin<0 && xFix==1 ? -1 : 0);
                int zCenter = (zMax+zMin)/2 + (zMax+zMin<0 && zFix==1 ? -1 : 0);

                int xRadius = (xMax-xMin)/2;
                int zRadius = (zMax-zMin)/2;

                if(xRadius == 0 || zRadius == 0) {
                        return !fill(xMin, yMin, zMin, xMax, yMax, zMax, stackToPlace, tile.getWorldObj());
                }

                int dx = xRadius, dz = 0;
                int xChange = zRadius*zRadius*(1-2*xRadius);
                int zChange = xRadius*xRadius;
                int ellipseError = 0;
                int twoASquare = 2*xRadius*xRadius;
                int twoBSquare = 2*zRadius*zRadius;
                int stoppingX = twoBSquare*xRadius;
                int stoppingZ = 0;

                while(stoppingX >= stoppingZ) {
                        if(!fillFourColumns(xCenter,zCenter,dx,dz,xFix,zFix,yMin,yMax,stackToPlace,tile.getWorldObj()))
                                return false;
                        ++dz;
                        stoppingZ += twoASquare;
                        ellipseError += zChange;
                        zChange += twoASquare;
                        if(2*ellipseError + xChange > 0) {
                                --dx;
                                stoppingX -= twoBSquare;
                                ellipseError += xChange;
                                xChange += twoBSquare;
                        }
                }

                dx = 0;
                dz = zRadius;
                xChange = zRadius*zRadius;
                zChange = xRadius*xRadius*(1-2*zRadius);
                ellipseError = 0;
                stoppingX = 0;
                stoppingZ = twoASquare*zRadius;

                while(stoppingX <= stoppingZ) {
                        if(!fillFourColumns(xCenter,zCenter,dx,dz,xFix,zFix,yMin,yMax,stackToPlace,tile.getWorldObj()))
                                return false;
                        ++dx;
                        stoppingX += twoBSquare;
                        ellipseError += xChange;
                        xChange += twoBSquare;
                        if(2*ellipseError + zChange > 0) {
                                --dz;
                                stoppingZ -= twoASquare;
                                ellipseError += zChange;
                                zChange += twoASquare;
                        }
                }

                return true;
        }

        private boolean fillFourColumns(int xCenter, int zCenter, int dx, int dz, int xFix, int zFix, int yMin, int yMax, ItemStack stackToPlace, World world) {
                int x,z;

                x = xCenter + dx + xFix;
                z = zCenter + dz + zFix;
                if(fill(x,yMin,z,x,yMax,z,stackToPlace,world))
                        return false;

                x = xCenter - dx;
                z = zCenter + dz + zFix;
                if(fill(x,yMin,z,x,yMax,z,stackToPlace,world))
                        return false;

                x = xCenter - dx;
                z = zCenter - dz;
                if(fill(x,yMin,z,x,yMax,z,stackToPlace,world))
                        return false;

                x = xCenter + dx + xFix;
                z = zCenter - dz;
                if(fill(x,yMin,z,x,yMax,z,stackToPlace,world))
                        return false;

                return true;
        }

}
