/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DrawingUtil {
    @SuppressWarnings("Duplicates")
    public static void drawEllipse(int cx,
                                   int cy,
                                   int rx,
                                   int ry,
                                   boolean filled,
                                   BiConsumer<Integer, Integer> drawPixel) {
        rx = Math.max(1, rx);
        ry = Math.max(1, ry);
        int rx2 = rx * rx;
        int ry2 = ry * ry;

        for (int x = 0, y = ry, sigma = 2 * ry2 + rx2 * (1 - 2 * ry); ry2 * x <= rx2 * y; x++) {
            if (filled) {
                for (int i = -x; i <= x; i++) {
                    drawPixel.accept(cx + i, cy + y);
                    drawPixel.accept(cx + i, cy - y);
                }
                for (int i = -y; i <= y; i++) {
                    drawPixel.accept(cx + x, cx + i);
                    drawPixel.accept(cx - x, cx + i);
                }
            } else {
                drawPixel.accept(cx + x, cy + y);
                drawPixel.accept(cx - x, cy + y);
                drawPixel.accept(cx + x, cy - y);
                drawPixel.accept(cx - x, cy - y);
            }
            if (sigma >= 0) {
                sigma += 4 * rx2 * (1 - y);
                y--;
            }
            sigma += ry2 * ((4 * x) + 6);
        }

        for (int x = rx, y = 0, sigma = 2 * rx2 + ry2 * (1 - 2 * rx); rx2 * y <= ry2 * x; y++) {
            if (filled) {
                for (int i = -x; i <= x; i++) {
                    drawPixel.accept(cx + i, cy + y);
                    drawPixel.accept(cx + i, cy - y);
                }
                for (int i = -y; i <= y; i++) {
                    drawPixel.accept(cx + x, cx + i);
                    drawPixel.accept(cx - x, cx + i);
                }
            } else {
                drawPixel.accept(cx + x, cy + y);
                drawPixel.accept(cx - x, cy + y);
                drawPixel.accept(cx + x, cy - y);
                drawPixel.accept(cx - x, cy - y);
            }
            if (sigma >= 0) {
                sigma += 4 * ry2 * (1 - x);
                x--;
            }
            sigma += rx2 * ((4 * y) + 6);
        }
    }

    public static void drawLine(int x1, int y1, int x2, int y2, BiConsumer<Integer, Integer> drawPixel) {
        int currentX = x1;
        int currentY = y1;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int error = (dx > dy ? dx : -dy) / 2;

        while (true) {
            drawPixel.accept(currentX, currentY);
            if (currentX == x2 && currentY == y2 ||
                    Math.abs(currentX - x1) > Math.abs(x2 - x1) ||
                    Math.abs(currentY - y1) > Math.abs(y2 - y1)) {
                break;
            }
            if (error * 2 > -dx) {
                error -= dy;
                currentX += x1 < x2 ? 1 : -1;
            }
            if (error * 2 < dy) {
                error += dx;
                currentY += y1 < y2 ? 1 : -1;
            }
        }
    }

    @SuppressWarnings("UnnecessaryLabelOnBreakStatement")
    public static void drawSphere(BlockPos radius,
                                  BlockPos center,
                                  boolean filled,
                                  Consumer<BlockPos> drawPixel) {

        double nextNx = 0;
        xLabel:
        for (int x = 0; x <= radius.getX(); x++) {
            double nx = nextNx;
            nextNx = (x + 1) * (1D / radius.getX());

            double nextNy = 0;
            yLabel:
            for (int y = 0; y <= radius.getY(); y++) {
                double ny = nextNy;
                nextNy = (y + 1) * (1D / radius.getY());

                double nextNz = 0;
                zLabel:
                for (int z = 0; z <= radius.getZ(); z++) {
                    double nz = nextNz;
                    nextNz = (z + 1) * (1D / radius.getZ());

                    if ((nx * nx) + (ny * ny) + (nz * nz) > 1) {
                        if (z != 0) {
                            break zLabel;
                        } else if (y != 0) {
                            break yLabel;
                        } else if (x != 0) {
                            break xLabel;
                        }
                    }

                    if (!filled) {
                        if ((nextNx * nextNx) + (ny * ny) + (nz * nz) <= 1 &&
                                (nx * nx) + (nextNy * nextNy) + (nz * nz) <= 1 &&
                                (nx * nx) + (ny * ny) + (nextNz * nextNz) <= 1) {
                            continue;
                        }
                    }

                    drawPixel.accept(center.offset(-x, -y, -z));
                    drawPixel.accept(center.offset(-x, -y, z));
                    drawPixel.accept(center.offset(-x, y, -z));
                    drawPixel.accept(center.offset(-x, y, z));
                    drawPixel.accept(center.offset(x, -y, -z));
                    drawPixel.accept(center.offset(x, -y, z));
                    drawPixel.accept(center.offset(x, y, -z));
                    drawPixel.accept(center.offset(x, y, z));
                }
            }
        }
    }


    public static void fill(boolean[][] data, int startX, int startY, int width, int height) {
        Queue<Pair<Integer, Integer>> queue = new ArrayDeque<>();
        queue.add(Pair.of(startX, startY));
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> point = queue.poll();
            if (point.getFirst() < 0 || point.getSecond() < 0 || point.getFirst() >= width || point.getSecond() >= height) {
                continue;
            }
            if (data[point.getFirst()][point.getSecond()]) {
                continue;
            }
            data[point.getFirst()][point.getSecond()] = true;
            queue.add(Pair.of(point.getFirst() - 1, point.getSecond()));
            queue.add(Pair.of(point.getFirst() + 1, point.getSecond()));
            queue.add(Pair.of(point.getFirst(), point.getSecond() - 1));
            queue.add(Pair.of(point.getFirst(), point.getSecond() + 1));
        }
    }
}
