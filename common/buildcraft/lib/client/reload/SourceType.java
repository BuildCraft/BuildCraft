/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.reload;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Defines that source of the change. Listeners should check to see what actually changed to see if they also need to
 * reload.
 * <p>
 * A source is either a {@link #FILE} change, or it is an object derived from a file. The others are provided for better
 * resolution of overlapping types (for example "buildcraftlib:items/guide_book" may either refer to the item model or
 * the texture depending on what uses that identifier) */
public enum SourceType {
    /** The resource on-disk changed, and so the object that loaded from disk should change, but not that listeners of
     * that object. (unless the object itself has changed) */
    FILE,
    /** A configuration option has changed. As configs are handled by forge these won't be preceded by a {@link #FILE}
     * change. */
    CONFIG,
    /** A {@link TextureAtlasSprite} object has changed its data. */
    SPRITE,
    /** An {@link IBakedModel} or {@link IUnbakedModel} or other model storage object has changed. */
    MODEL;

    public static final SourceType[] VALUES = values();
}
