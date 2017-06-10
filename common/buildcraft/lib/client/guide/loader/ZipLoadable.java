/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import buildcraft.lib.misc.data.ZipFileHelper;

public class ZipLoadable implements ILoadableResource {
    private final ZipFileHelper zip;

    public ZipLoadable(File zip) throws IOException {
        try (FileInputStream fis = new FileInputStream(zip)) {
            ZipInputStream zis = new ZipInputStream(fis);
            this.zip = new ZipFileHelper(zis);
        }
    }

    @Override
    public ByteArrayInputStream getInputStreamFor(String location) {
        return zip.getInputStream(location);
    }
}
