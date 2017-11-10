/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.blueprints;

import buildcraft.api.core.Position;

public class Translation {

	public double x = 0;
	public double y = 0;
	public double z = 0;

	public Position translate (Position p) {
		Position p2 = new Position (p);

		p2.x = p.x + x;
		p2.y = p.y + y;
		p2.z = p.z + z;

		return p2;
	}

	@Override
	public String toString () {
		return "{" + x + ", " + y + ", " + z + "}";
	}

}
