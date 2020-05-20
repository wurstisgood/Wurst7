/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.ChatUtil;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.ChatUtils;

public final class MassMsgCmd extends Command implements UpdateListener
{
	private final Random random = new Random();
	private final ArrayList<String> players = new ArrayList<>();
	
	private int index;
	private int timer;
	private String message;
	private String command;
	private boolean toggled;
	
	public MassMsgCmd()
	{
		super("massmsg",
			"Spams a command to all players on the server.",
				"<command> <message>", "<command>");	
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		toggled = !toggled;
		if(toggled)
		{
			if(args.length >= 1)
			{
				command = args[0];
				if(args.length >= 2)
				{
					for(int i = 2; i < args.length; i++)
						message += " " + args[i];
				}else if(args.length == 1)
					message = "";
				index = 0;
				timer = -1;
				players.clear();
				String playerName = MC.getSession().getProfile().getName();
				
				for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
				{
					String name = info.getProfile().getName();
					name = ChatUtil.stripTextFormat(name);
					
					if(name.equalsIgnoreCase(playerName))
						continue;
					
					players.add(name);
				}
				Collections.shuffle(players, random);
				
				if(players.isEmpty())
				{
					ChatUtils.error("Couldn't find any players.");
					return;
				}
				
				EVENTS.add(UpdateListener.class, this);
			}else
			{
				toggled = false;
				throw new CmdSyntaxError();
			}
		}else
		{
			EVENTS.remove(UpdateListener.class, this);
			message = null;
			ChatUtils.message("Disabling MassMessage.");
		}
	}

	@Override
	public void onUpdate()
	{
		if(message == null)
			return;
		
		if(timer > -1)
		{
			timer--;
			return;
		}
		
		if(index >= players.size())
		{
			EVENTS.remove(UpdateListener.class, this);
			ChatUtils.message("MassMessage finished.");
			message = null;
			toggled = false;
		}
		
		MC.player.sendChatMessage("/" + command + " " + players.get(index) + " " + message);
		index++;
		timer = 20;
	}	
}
