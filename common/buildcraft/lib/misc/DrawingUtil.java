/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.function.BiConsumer;

public class DrawingUtil {
    @SuppressWarnings("Duplicates")
    public static void drawEllipse(int cx,
                                   int cy,
                                   int width,
                                   int height,
                                   boolean filled,
                                   BiConsumer<Integer, Integer> drawPixel) {
        int a2 = width * width;
        int b2 = height * height;
        int fa2 = 4 * a2, fb2 = 4 * b2;
        int x, y, sigma;

        for (x = 0, y = height, sigma = 2 * b2 + a2 * (1 - 2 * height); b2 * x <= a2 * y; x++) {
            if (filled) {
                for (int i = -x; i <= x; i++) {
                    drawPixel.accept(cx + i, cy + y);
                    drawPixel.accept(cx + i, cy - y);
                }
            } else {
                drawPixel.accept(cx + x, cy + y);
                drawPixel.accept(cx - x, cy + y);
                drawPixel.accept(cx + x, cy - y);
                drawPixel.accept(cx - x, cy - y);
            }
            if (sigma >= 0) {
                sigma += fa2 * (1 - y);
                y--;
            }
            sigma += b2 * ((4 * x) + 6);
        }

        for (x = width, y = 0, sigma = 2 * a2 + b2 * (1 - 2 * width); a2 * y <= b2 * x; y++) {
            if (filled) {
                for (int i = -x; i <= x; i++) {
                    drawPixel.accept(cx + i, cy + y);
                    drawPixel.accept(cx + i, cy - y);
                }
            } else {
                drawPixel.accept(cx + x, cy + y);
                drawPixel.accept(cx - x, cy + y);
                drawPixel.accept(cx + x, cy - y);
                drawPixel.accept(cx - x, cy - y);
            }
            if (sigma >= 0) {
                sigma += fb2 * (1 - x);
                x--;
            }
            sigma += a2 * ((4 * y) + 6);
        }
    }
}
