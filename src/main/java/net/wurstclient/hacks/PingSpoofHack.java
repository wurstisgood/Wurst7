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
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class PingSpoofHack extends Hack
{
	public final SliderSetting delay = new SliderSetting("Delay", 
		"The delay before sending the ping packet in milliseconds.", 1000, 100, 2000, 100,
		ValueDisplay.INTEGER);
	public final CheckboxSetting random = new CheckboxSetting(
		"Randomly Decrease Delay",
		"Randomly decreases the delay to bring down the ping.", true);
	
	public PingSpoofHack()
	{
		super("PingSpoof",
			"Spoofs your ping.\n"
				+ "Note: If you are using the Null Ping mode,\n"
				+ "you must have this on before you join the server and not\n"
				+ "turn it off for it to work.\n");
		setCategory(Category.OTHER);
		addSetting(delay);
		addSetting(random);
	}
}
