/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FolderLoadable implements ILoadableResource {
    private final File base;

    public FolderLoadable(File base) {
        this.base = base;
    }

    @Override
    public InputStream getInputStreamFor(String location) throws IOException {
        File file = new File(base, location);
        if (!file.isFile()) {
            return null;
        }
        return new FileInputStream(file);
    }
}
