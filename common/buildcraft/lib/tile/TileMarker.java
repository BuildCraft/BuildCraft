/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public abstract class TileMarker<C extends MarkerConnection<C>> extends TileBC_Neptune implements IDebuggable {
    public TileMarker(TileEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

    public MarkerSubCache<C> getLocalCache() {
        return getCache().getSubCache(level);
    }

    /** @return True if this has lasers being emitted, or any other reason you want. Activates the surrounding "glow"
     *         parts for the block model. */
    public abstract boolean isActiveForRender();

    public C getCurrentConnection() {
        return getLocalCache().getConnection(getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getLocalCache().loadMarker(getBlockPos(), this);
    }

    @Override
//    public void onChunkUnload()
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        getLocalCache().unloadMarker(getBlockPos());
    }

    @Override
//    public void invalidate()
    public void setRemoved() {
        super.setRemoved();
        // getLocalCache().removeMarker(getPos());
    }

    @Override
    public void onRemove() {
        super.onRemove();
        getLocalCache().removeMarker(getBlockPos());
    }

    protected void disconnectFromOthers() {
        C currentConnection = getCurrentConnection();
        if (currentConnection != null) {
            currentConnection.removeMarker(getBlockPos());
        }
    }

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<ITextComponent> left, List<ITextComponent> right, Direction side) {
        C current = getCurrentConnection();
        MarkerSubCache<C> cache = getLocalCache();
//        left.add("Exists = " + (cache.getMarker(getBlockPos()) == this));
        left.add(new StringTextComponent("Exists = " + (cache.getMarker(getBlockPos()) == this)));
        if (current == null) {
//            left.add("NetworkManager = null");
            left.add(new StringTextComponent("NetworkManager = null"));
        } else {
//            left.add("NetworkManager:");
            left.add(new StringTextComponent("NetworkManager:"));
            current.getDebugInfo(getBlockPos(), left);
        }
    }
}
