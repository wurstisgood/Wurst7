/*
 * Copyright © 2014 - 2018 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.hacks;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.network.packet.s2c.play.CloseContainerS2CPacket;
import net.wurstclient.Category;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

public final class AntiGuiCloseHack extends Hack implements PacketInputListener
{
	private final CheckboxSetting inventory = new CheckboxSetting(
		"Prevent Closing Inventory", true);
	
	public AntiGuiCloseHack()
	{
		super("AntiGuiClose", 
			"Prevents the server from closing non-container GUIs.");
		setCategory(Category.OTHER);
		addSetting(inventory);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(PacketInputListener.class, this);
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		if(!(event.getPacket() instanceof CloseContainerS2CPacket))
			return;
		
		if(MC.currentScreen == null)
			return;
		
		if(MC.currentScreen instanceof ContainerScreen && !(MC.currentScreen instanceof AbstractInventoryScreen))
			return;
		
		if(MC.currentScreen instanceof AbstractInventoryScreen && !inventory.isChecked())
			return;
		
		event.cancel();
	}
}
