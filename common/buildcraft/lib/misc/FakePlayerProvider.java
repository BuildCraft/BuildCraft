/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

// STUB(R.Chen): the Forge FakePlayer machinery (IFakePlayerProvider / FakePlayerBC / WorldServer) is
// deferred until a Fabric fake-player implementation lands. Only NULL_PROFILE — the default owner
// GameProfile used by TileBC_Neptune — is retained here.
public enum FakePlayerProvider {
    INSTANCE;

    /** The default {@link GameProfile} to use if a tile entity cannot determine its real owner. */
    @Deprecated
    public static final GameProfile NULL_PROFILE;

    static {
        UUID id = UUID.nameUUIDFromBytes("buildcraft.core".getBytes(StandardCharsets.UTF_8));
        NULL_PROFILE = new GameProfile(id, "[BuildCraft]");
    }
}
