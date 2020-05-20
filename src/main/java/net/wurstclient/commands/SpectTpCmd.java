/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.util.ChatUtil;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class SpectTpCmd extends Command
{	
	public SpectTpCmd()
	{
		super("specttp", "Teleports you to a player. Requires spectator mode.",
			"[<player>]");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(!MC.player.isSpectator())
			throw new CmdError("Spectator mode only.");
		if(args.length == 1)
		{
			PlayerListEntry name = null;
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
			{
				if(ChatUtil.stripTextFormat(info.getProfile().getName()).equals(args[0]))
					name = info;
			}
			if(name == null)
				throw new CmdError("Player \"" + args[0] + "\" could not be found.");
			MC.getNetworkHandler().sendPacket(new SpectatorTeleportC2SPacket(name.getProfile().getId()));
		}else
			throw new CmdSyntaxError();
	}
}
