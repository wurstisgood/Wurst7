/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

public final class NoPushHack extends Hack implements UpdateListener
{
	private float prevPush;
	
	public NoPushHack()
	{
		super("NoPush", "Prevents you from being pushed by entities.");
		setCategory(Category.OTHER);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		if(MC.player != null)
			prevPush = MC.player.pushSpeedReduction;
		else 
			prevPush = 0F;
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		MC.player.pushSpeedReduction = prevPush;
	}
	
	@Override
	public void onUpdate()
	{
		if(MC.player != null)
			MC.player.pushSpeedReduction = 1F;
	}
}
