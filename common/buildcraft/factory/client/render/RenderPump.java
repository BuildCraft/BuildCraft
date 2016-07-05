package buildcraft.factory.client.render;

import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.tile.RenderMultiTile;
import buildcraft.lib.client.render.tile.RenderPartElement;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class RenderPump extends RenderMultiTile<TilePump> {
    private static final int[] COLOUR_POWER = new int[16];
    private static final int COLOUR_STATUS_ON = 0xFF_77_DD_77; // a light green
    private static final int COLOUR_STATUS_PAUSED = 0xFF_47_B3_FF; // a light orange
    private static final int COLOUR_STATUS_DONE = 0xFF_1f_10_1b; // black-ish

    private static final int BLOCK_LIGHT_STATUS_OFF = 0x0;
    private static final int BLOCK_LIGHT_STATUS_TODO = 0x1;

    private static final double POWER = 1.5 / 16.0;
    private static final double STATUS = 3.5 / 16.0;
    private static final double Y = 13.5 / 16.0;

    static {
        for (int i = 0; i < COLOUR_POWER.length; i++) {
            int c = (i * 0x40) / COLOUR_POWER.length;
            int r = (i * 0xE0) / COLOUR_POWER.length + 0x1F;
            int colour = (0xFF << 24) + (c << 16) + (c << 8) + r;
            COLOUR_POWER[i] = colour;
        }
    }

    private double ledX, ledZ;
    private int dX = 1, dZ = 0;

    public RenderPump() {
        parts.add(new RenderPartElement<>((tile, part) -> {
            part.center.positiond(ledX + dX * POWER, Y, ledZ + dZ * POWER);
            float percentFilled = tile.getPercentFilledForRender();
            int colourIndex = (int) (percentFilled * (COLOUR_POWER.length - 1));
            part.center.colouri(COLOUR_POWER[colourIndex]);
            part.center.lightf(percentFilled, 0);
        }));
        parts.add(new RenderPartElement<>((tile, part) -> {
            part.center.positiond(ledX + dX * STATUS, Y, ledZ + dZ * STATUS);
            boolean more = tile.hasWork();
            boolean paused = tile.getControlMode() == Mode.Off;
            part.center.colouri(more ? (paused ? COLOUR_STATUS_PAUSED : COLOUR_STATUS_ON) : COLOUR_STATUS_DONE);
            part.center.lightf(more ? (BLOCK_LIGHT_STATUS_TODO) : BLOCK_LIGHT_STATUS_OFF, 0);
        }));
        parts.add(new RenderPartElement<>((tile, part) -> {
            float percentFilled = tile.getFluidPercentFilledForRender();
            part.center.positiond(ledX + dX * 0.0 / 16.0, 6.0 / 16.0 + percentFilled * 4.0 / 2.0 / 16.0, ledZ + dZ * 0.0 / 16.0);
            part.sizeY = percentFilled * 4.0 / 16.0F;
            if(dZ == 0) {
                part.sizeX = 8.0 / 16.0;
                part.sizeZ = 1.0 / 16.0;
            } else if(dX == 0) {
                part.sizeZ = 8.0 / 16.0;
                part.sizeX = 1.0 / 16.0;
            }
            if(percentFilled > 0) {
                part.center.colouri(tile.getFluidColorForRender());
            } else {
                part.center.colouri(0);
            }
//            part.center.lightf(percentFilled, 0);
        }));
    }

    @Override
    public void renderTileEntityFast(TilePump tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        for(EnumFacing facing : new EnumFacing[]{EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST}) {
            if(facing.getAxis() == Axis.X) {
                dX = 0;
                dZ = facing.getAxisDirection().getOffset();
                ledZ = 0.5;
                if(facing == EnumFacing.EAST) {
                    ledX = 15.6 / 16.0;
                } else {
                    ledX = 0.4 / 16.0;
                }
            } else {
                dX = -facing.getAxisDirection().getOffset();
                dZ = 0;
                ledX = 0.5;
                if(facing == EnumFacing.SOUTH) {
                    ledZ = 15.6 / 16.0;
                } else {
                    ledZ = 0.4 / 16.0;
                }
            }
            super.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, buffer);
        }
    }
}
