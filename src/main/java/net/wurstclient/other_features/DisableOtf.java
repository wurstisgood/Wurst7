/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"turn off", "hide wurst logo", "ghost mode", "stealth mode",
	"vanilla Minecraft"})
@DontBlock
public final class DisableOtf extends OtherFeature
{
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.BUTTON);
	
	public DisableOtf()
	{
		super("Disable Wurst",
			"To disable Wurst, go to the Statistics screen and press the \"Disable Wurst\" button.\n"
				+ "There are 3 modes for re-enabling:\n"
				+ "Button mode adds an Enable Wurst button in the Statistics screen.\n"
				+ "Code mode generates a code that you have to type to re-enable.\n"
				+ "Restart mode forces you to restart to re-enable the client.");
		addSetting(mode);
	}
	
	public Mode getMode()
	{
		return mode.getSelected();
	}
	
	public enum Mode
	{
		BUTTON("Button"),
		CODE("Code"),
		RESTART("Restart");
		
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
