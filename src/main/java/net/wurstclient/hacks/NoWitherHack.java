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

public class NoWitherHack extends Hack
{
	public NoWitherHack()
	{
		super("NoWither",
			"Uses the the poison effect when you are under\n"
			+ "the wither effect.");
		setCategory(Category.RENDER);
	}
}
