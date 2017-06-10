/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.reload;

import java.util.Set;

public interface IReloadable {
    /** @param changed All of the objects that have been reloaded. This *may* just be an intersection of this reloadable
     *            objects parents (registered in {@link ReloadManager}) and all of the reloaded objects though.
     * @return True if this changed in a way that can be seen by listeners, false otherwise. */
    boolean reload(Set<ReloadSource> changed);
}
