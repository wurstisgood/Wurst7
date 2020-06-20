/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.hack.Hack;

public final class NoFireworksHack extends Hack
{
	public NoFireworksHack()
	{
		super("NoFireworks",
			"Disables the firework particles.\n"
				+ "Great for lagging other people out with\n"
				+ "fireworks but not yourself.");
		setCategory(Category.RENDER);
	}	
}
