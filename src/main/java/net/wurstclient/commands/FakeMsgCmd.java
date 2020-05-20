/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.text.LiteralText;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class FakeMsgCmd extends Command
{
	public FakeMsgCmd()
	{
		super("fakemsg",
			"Generates a fake message into the chat screen. Use $ for colors, use $$ for $.",
			"[<message>]");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0)
			throw new CmdSyntaxError();
		String message = args[0];
		for(int i = 1; i < args.length; i++)
			message += " " + args[i];
		message = message.replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
		MC.inGameHud.getChatHud().addMessage(new LiteralText(message));
	}
}
