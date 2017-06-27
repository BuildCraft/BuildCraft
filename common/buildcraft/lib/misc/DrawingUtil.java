/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

import javax.vecmath.Point2i;

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
            if (currentX == x2 && currentY == y2) {
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

    public static void fill(boolean[][] data, int startX, int startY, int width, int height) {
        Queue<Point2i> queue = new ArrayDeque<>();
        queue.add(new Point2i(startX, startY));
        while (!queue.isEmpty()) {
            Point2i point = queue.poll();
            if (point.x < 0 || point.y < 0 || point.x >= width || point.y >= height) {
                continue;
            }
            if (data[point.x][point.y]) {
                continue;
            }
            data[point.x][point.y] = true;
            queue.add(new Point2i(point.x - 1, point.y));
            queue.add(new Point2i(point.x + 1, point.y));
            queue.add(new Point2i(point.x, point.y - 1));
            queue.add(new Point2i(point.x, point.y + 1));
        }
    }
}
