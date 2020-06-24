/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.io.IOException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"resource spoofer", "texture pack bypass"})
public final class ResourceSpooferHack extends Hack implements PacketOutputListener, PacketInputListener
{
	private final CheckboxSetting alwaysSuccess =
		new CheckboxSetting("Always Success", "Tell the server the pack was successfully downloaded\n"
			+ "when the download fails.", true);
	private boolean hasAccepted;
	
	public ResourceSpooferHack()
	{
		super("ResourceSpoofer",
			"Makes the server think you are using the\n"
				+ "texture pack even when you deny the texture pack.");
		setCategory(Category.OTHER);
		addSetting(alwaysSuccess);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(PacketOutputListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(PacketOutputListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
	}

	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		if(!(event.getPacket() instanceof ResourcePackStatusC2SPacket))
			return;
		
		ResourcePackStatusC2SPacket packet = (ResourcePackStatusC2SPacket)event.getPacket();
		PacketByteBuf buf = new PacketByteBuf(null);
		try
		{
			packet.write(buf);
		}catch(IOException e)
		{
			return;
		}
		Status status = buf.readEnumConstant(Status.class);
		if(status == ResourcePackStatusC2SPacket.Status.ACCEPTED)
			hasAccepted = true;
		if(status == ResourcePackStatusC2SPacket.Status.DECLINED)
		{
			event.cancel();
			MC.player.networkHandler.getConnection().send(
				new ResourcePackStatusC2SPacket(Status.ACCEPTED));
			MC.player.networkHandler.getConnection().send(
				new ResourcePackStatusC2SPacket(Status.SUCCESSFULLY_LOADED));
		}else if(status == ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD && alwaysSuccess.isChecked())
			if(hasAccepted)
			{
				event.cancel();
				MC.player.networkHandler.getConnection().send(
					new ResourcePackStatusC2SPacket(Status.SUCCESSFULLY_LOADED));
			}else
			{
				event.cancel();
				MC.player.networkHandler.getConnection().send(
					new ResourcePackStatusC2SPacket(Status.ACCEPTED));
				MC.player.networkHandler.getConnection().send(
					new ResourcePackStatusC2SPacket(Status.SUCCESSFULLY_LOADED));
			}
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		if(event.getPacket() instanceof ResourcePackSendS2CPacket)
			hasAccepted = false;
	}
}
