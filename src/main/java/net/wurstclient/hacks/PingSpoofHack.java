/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class PingSpoofHack extends Hack implements PacketOutputListener
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.values(), Mode.NULLPING);
	public final SliderSetting delay = new SliderSetting("Delay for High Ping", 
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
		addSetting(mode);
		addSetting(delay);
		addSetting(random);
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
		if(mode.getSelected() == Mode.NULLPING && event.getPacket() instanceof KeepAliveC2SPacket)
		{
			event.cancel();
			MC.player.networkHandler.getConnection().send(new KeepAliveC2SPacket(Integer.MAX_VALUE));
		}
	}
	
	public boolean shouldDelayPingPacket()
	{
		return isEnabled() && mode.getSelected() == Mode.HIGHPING;
	}
	
	public enum Mode
	{
		NULLPING("Null Ping"),
		HIGHPING("High Ping");

		private final String name;

		private Mode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
