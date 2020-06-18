/*
 * Copyright © 2014 - 2018 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.GuiCloseC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.hack.Hack;

public final class MoreInventoryHack extends Hack implements PacketOutputListener
{
	public MoreInventoryHack()
	{
		super("MoreInventory", "Prevents items from dropping in crafting slots.");
		setCategory(Category.OTHER);
	}

	@Override
	public void onEnable()
	{
		EVENTS.add(PacketOutputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(PacketOutputListener.class, this);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		 if(event.getPacket() instanceof GuiCloseC2SPacket) 
			 event.cancel();
	}
}
