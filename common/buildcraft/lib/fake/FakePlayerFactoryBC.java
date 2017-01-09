/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package buildcraft.lib.fake;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.WorldServer;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

public class FakePlayerFactoryBC
{
    private static GameProfile MINECRAFT = new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]");
    private static Map<GameProfile, FakePlayerBC> fakePlayers = Maps.newHashMap();
    private static WeakReference<FakePlayerBC> MINECRAFT_PLAYER = null;

    public static FakePlayerBC getMinecraft(WorldServer world)
    {
        FakePlayerBC ret = MINECRAFT_PLAYER != null ? MINECRAFT_PLAYER.get() : null;
        if (ret == null)
        {
            ret = FakePlayerFactoryBC.get(world,  MINECRAFT);
            MINECRAFT_PLAYER = new WeakReference<>(ret);
        }
        return ret;
    }

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    public static FakePlayerBC get(WorldServer world, GameProfile username)
    {
        if (!fakePlayers.containsKey(username))
        {
            FakePlayerBC fakePlayer = new FakePlayerBC(world, username);
            fakePlayers.put(username, fakePlayer);
        }

        return fakePlayers.get(username);
    }

    public static void unloadWorld(WorldServer world)
    {
        fakePlayers.entrySet().removeIf(entry -> entry.getValue().world == world);
    }
}