/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableVertex;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class JsonVertex {
    public Point3f pos;
    public Vector3f normal;
    public Point2f uv;

    public JsonVertex(MutableVertex vertex) {
        pos = vertex.positionvf();
        normal = vertex.normal();
        uv = vertex.tex();
    }

    public void loadInto(MutableVertex vertex) {
        vertex.positionv(pos);
        vertex.normalv(normal);
        vertex.texv(uv);
    }
}
