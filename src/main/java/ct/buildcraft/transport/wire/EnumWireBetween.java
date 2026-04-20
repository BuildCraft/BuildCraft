/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.wire;

import java.util.Arrays;
import java.util.function.Function;

import ct.buildcraft.api.transport.EnumWirePart;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Holds all of the possible boxes that a wire can occupy - excluding the ones in EnumWirePart. */
public enum EnumWireBetween {
    // Centre
    X_UP_SOUTH(Axis.X, true, true),//000 x++
    X_UP_NORTH(Axis.X, true, false),//001 x+-
    X_DOWN_SOUTH(Axis.X, false, true),//010 x-+
    X_DOWN_NORTH(Axis.X, false, false),//011 x--

    Y_SOUTH_EAST(Axis.Y, true, true),//100 yzx
    Y_SOUTH_WEST(Axis.Y, true, false),
    Y_NORTH_EAST(Axis.Y, false, true),
    Y_NORTH_WEST(Axis.Y, false, false),

    Z_UP_EAST(Axis.Z, true, true),//200
    Z_UP_WEST(Axis.Z, true, false),
    Z_DOWN_EAST(Axis.Z, false, true),
    Z_DOWN_WEST(Axis.Z, false, false),

    // Between pipes
    EAST_UP_SOUTH(Direction.EAST, true, true),
    EAST_UP_NORTH(Direction.EAST, true, false),
    EAST_DOWN_SOUTH(Direction.EAST, false, true),
    EAST_DOWN_NORTH(Direction.EAST, false, false),

    WEST_UP_SOUTH(Direction.WEST, true, true),
    WEST_UP_NORTH(Direction.WEST, true, false),
    WEST_DOWN_SOUTH(Direction.WEST, false, true),
    WEST_DOWN_NORTH(Direction.WEST, false, false),

    UP_SOUTH_EAST(Direction.UP, true, true),
    UP_SOUTH_WEST(Direction.UP, true, false),
    UP_NORTH_EAST(Direction.UP, false, true),
    UP_NORTH_WEST(Direction.UP, false, false),

    DOWN_SOUTH_EAST(Direction.DOWN, true, true),
    DOWN_SOUTH_WEST(Direction.DOWN, true, false),
    DOWN_NORTH_EAST(Direction.DOWN, false, true),
    DOWN_NORTH_WEST(Direction.DOWN, false, false),

    SOUTH_UP_EAST(Direction.SOUTH, true, true),
    SOUTH_UP_WEST(Direction.SOUTH, true, false),
    SOUTH_DOWN_EAST(Direction.SOUTH, false, true),
    SOUTH_DOWN_WEST(Direction.SOUTH, false, false),

    NORTH_UP_EAST(Direction.NORTH, true, true),
    NORTH_UP_WEST(Direction.NORTH, true, false),
    NORTH_DOWN_EAST(Direction.NORTH, false, true),
    NORTH_DOWN_WEST(Direction.NORTH, false, false);

    public static final EnumWireBetween[] VALUES = values();
    public static final EnumWireBetween[] CENTRES = Arrays.copyOfRange(VALUES, 0, 12, EnumWireBetween[].class);
    public static final EnumWireBetween[] CONNECTIONS = Arrays.copyOfRange(VALUES, 12, 36, EnumWireBetween[].class);

    public final Axis mainAxis;
    public final Direction to;
    public final boolean xy;
    public final boolean yz;
    public final VoxelShape boundingBox;
    public final EnumWirePart[] parts;

    EnumWireBetween(Axis mainAxis, boolean xy, boolean yz) {
        this.mainAxis = mainAxis;
        this.to = null;
        this.xy = xy;
        this.yz = yz;
        int x1 = mainAxis == Axis.X ? 4 : (xy ? 12 : 3);
        int y1 = mainAxis == Axis.Y ? 4 : ((mainAxis == Axis.X ? xy : yz) ? 12 : 3);
        int z1 = mainAxis == Axis.Z ? 4 : (yz ? 12 : 3);
        int x2 = x1 + (mainAxis == Axis.X ? 8 : 1);
        int y2 = y1 + (mainAxis == Axis.Y ? 8 : 1);
        int z2 = z1 + (mainAxis == Axis.Z ? 8 : 1);
        boundingBox = Shapes.box(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
        parts = getParts();
    }

    EnumWireBetween(Direction to, boolean xy, boolean yz) {
        this.mainAxis = to.getAxis();
        this.to = to;
        this.xy = xy;
        this.yz = yz;
        int start = to.getAxisDirection() == AxisDirection.POSITIVE ? 13 : 0;
        int x1 = mainAxis == Axis.X ? start : (xy ? 12 : 3);
        int y1 = mainAxis == Axis.Y ? start : ((mainAxis == Axis.X ? xy : yz) ? 12 : 3);
        int z1 = mainAxis == Axis.Z ? start : (yz ? 12 : 3);
        int x2 = x1 + (mainAxis == Axis.X ? 3 : 1);
        int y2 = y1 + (mainAxis == Axis.Y ? 3 : 1);
        int z2 = z1 + (mainAxis == Axis.Z ? 3 : 1);
        boundingBox = Shapes.box(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0);
        parts = getParts();
    }

    private EnumWirePart[] getParts() {
        Function<AxisDirection[], EnumWirePart> getPartFromDirections = directions -> Arrays.stream(EnumWirePart.VALUES).filter(part -> part.x == directions[0] && part.y == directions[1] && part.z == directions[2]).findFirst().orElse(null);
        EnumWirePart[] arr = new EnumWirePart[2];
        for(int i = 0; i < arr.length; i++) {
            AxisDirection[] directions = new AxisDirection[3];
            boolean found = false;
            for(int j = 0; j < directions.length; j++) {
                if(mainAxis.ordinal() == j) {
                    if(to == null) {
                        directions[j] = i == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE;
                    } else {
                        directions[j] = i == 0 ? to.getAxisDirection() : to.getOpposite().getAxisDirection();
                    }
                } else if(!found) {
                    directions[j] = xy ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
                    found = true;
                } else {
                    directions[j] = yz ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
                }
            }
            arr[i] = getPartFromDirections.apply(directions);
        }
        return arr;
    }
    
    public EnumWireBetween rotate(Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return this;
        }
        
        // 处理中心线段（to == null）
        if (this.to == null) {
            return rotateCentre(rotation);
        }
        
        // 处理连接线段（to != null）
        return rotateConnection(rotation);
    }

    private EnumWireBetween rotateCentre(Rotation rotation) {
        // 旋转主轴
        Axis newMainAxis;
        if (mainAxis == Axis.Y) {
        	newMainAxis = Axis.Y;  // Y 轴不变
        }
        
        newMainAxis = switch (rotation) {
            case CLOCKWISE_90 ->
            	mainAxis == Axis.X ? Axis.Z : Axis.X;
            case CLOCKWISE_180 ->
            	mainAxis;  // X→X, Z→Z（但方向取反）
            case COUNTERCLOCKWISE_90 ->
            	newMainAxis = mainAxis == Axis.X ? Axis.Z : Axis.X;
            default->
            	mainAxis;
        };
        
        // 根据旋转重新计算 xy 和 yz
        // 原坐标系统：主轴方向决定长度，xy 和 yz 决定垂直面的偏移
        boolean newXy = this.xy;
        boolean newYz = this.yz;
        
        switch (rotation) {
            case CLOCKWISE_90:
                // X → Z, Z → -X
                if (this.mainAxis == Axis.X) {
                    // 原 X 轴中心线变为 Z 轴中心线
                    newMainAxis = Axis.Z;
                    // xy 和 yz 需要重新映射
                    // 原 xy (X-Y平面) → 变为 yz (Z-Y平面)
                    newXy = this.yz;  // 原 yz 成为新的 xy
                    newYz = this.xy;  // 原 xy 成为新的 yz（可能需要取反）
                } else if (this.mainAxis == Axis.Z) {
                    newMainAxis = Axis.X;
                    newXy = this.yz;
                    newYz = this.xy;
                }
                // Y 轴不变
                break;
                
            case CLOCKWISE_180:
                // X → -X, Z → -Z
                if (this.mainAxis == Axis.X || this.mainAxis == Axis.Z) {
                    // 主轴方向取反，但主轴本身不变
                    // xy 和 yz 标志取反（因为方向反转）
                    newXy = !this.xy;
                    newYz = !this.yz;
                }
                // Y 轴不变
                break;
                
            case COUNTERCLOCKWISE_90:
                // X → -Z, Z → X
                if (this.mainAxis == Axis.X) {
                    newMainAxis = Axis.Z;
                    newXy = !this.yz;  // 需要取反
                    newYz = this.xy;
                } else if (this.mainAxis == Axis.Z) {
                    newMainAxis = Axis.X;
                    newXy = this.yz;
                    newYz = !this.xy;
                }
                break;
        }
        
        return CENTRES[(newMainAxis.ordinal()<<2) | (newXy ? 0 : 1) | (newYz ? 0 : 0b10)];
    }

    private EnumWireBetween rotateConnection(Rotation rotation) {
        // 旋转目标方向
        Direction newTo = rotation.rotate(this.to);
        
        // 旋转主轴（与 to 的方向轴一致）
        Axis newMainAxis = newTo.getAxis();
        
        // 重新计算 xy 和 yz
        // 这些标志相对于当前的坐标系
        boolean newXy = this.xy;
        boolean newYz = this.yz;
        
        // 根据旋转角度和原主轴进行变换
        if (this.mainAxis != newMainAxis) {
            // 主轴发生了变化（X ↔ Z）
            switch (rotation) {
                case CLOCKWISE_90:
                    // 原 X 轴连接变为 Z 轴连接
                    if (this.mainAxis == Axis.X) {
                        // xy (X-Y平面) → yz (Z-Y平面)
                        newXy = this.yz;
                        newYz = this.xy;
                    } else if (this.mainAxis == Axis.Z) {
                        newXy = this.yz;
                        newYz = this.xy;
                    }
                    break;
                    
                case COUNTERCLOCKWISE_90:
                    if (this.mainAxis == Axis.X) {
                        newXy = !this.yz;
                        newYz = this.xy;
                    } else if (this.mainAxis == Axis.Z) {
                        newXy = this.yz;
                        newYz = !this.xy;
                    }
                    break;
            }
        } else if (rotation == Rotation.CLOCKWISE_180) {
            // 主轴不变但方向相反时，标志取反
            newXy = !this.xy;
            newYz = !this.yz;
        }
        
        return CONNECTIONS[(newTo.getAxis().ordinal()<<3) | (newTo.getAxisDirection().ordinal()<<2)| (newXy ? 0 : 1) | (newYz ? 0 : 0b10)];
    }

}
