/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat.forge_stubs;

/**
 * STUB(R.Chen): umbrella note for legacy {@code buildcraft.lib.net} files that
 * have NOT been migrated in place because of cascading dependencies. They remain
 * on disk but are excluded from the active sourceSet (libLeaf) and will be ported
 * incrementally as their dependents come online.
 *
 * Originals (path → status):
 *   common/buildcraft/lib/net/MessageManager.java        — replaced by {@link buildcraft.lib.net.BCNetworkManager}
 *   common/buildcraft/lib/net/MessageUpdateTile.java     — replaced by {@link buildcraft.lib.net.UpdateTilePayload}
 *   common/buildcraft/lib/net/MessageContainer.java      — needs full migration (FabricPacket, Container routing)
 *   common/buildcraft/lib/net/MessageMarker.java         — needs full migration (depends on MarkerCache)
 *   common/buildcraft/lib/net/MessageDebugRequest.java   — needs full migration (depends on TileBC_Neptune)
 *   common/buildcraft/lib/net/MessageDebugResponse.java  — needs full migration (depends on ClientDebuggables)
 *   common/buildcraft/lib/net/IPayloadReceiver.java      — uses Forge MessageContext; replace context with sender info
 *   common/buildcraft/lib/net/IPayloadWriter.java        — no Forge deps; can move to libLeaf when PacketBufferBC is ported
 *   common/buildcraft/lib/net/PacketBufferBC.java        — extends Forge's PacketByteBuf; on Yarn it would extend PacketByteBuf
 *   common/buildcraft/lib/delta/DeltaManager.java        — pure logic; depends on PacketBufferBC + IPayloadWriter
 *   common/buildcraft/lib/delta/DeltaInt.java            — uses Forge {@code Constants} for NBT tag IDs; trivial swap to {@link net.minecraft.nbt.NbtElement}
 *
 * No runtime members — this class exists solely as a porting tracker.
 */
public final class LegacyNetStubs {
    private LegacyNetStubs() {}
}
