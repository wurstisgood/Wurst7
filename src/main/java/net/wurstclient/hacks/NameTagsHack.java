/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"name tags"})
public final class NameTagsHack extends Hack
{
	private final CheckboxSetting alwaysVisible = new CheckboxSetting(
		"Always See NameTags", false);	
	private final CheckboxSetting unlimited = new CheckboxSetting(
		"Unlimited Range Nametags", false);
	
	public NameTagsHack()
	{
		super("NameTags", "Changes the scale of the nametags so you can\n"
			+ "always read them.\n" + "Also allows you to see the nametags of\n"
			+ "sneaking players.");
		
		setCategory(Category.RENDER);
		addSetting(alwaysVisible);	
		addSetting(unlimited);
	}
	
	public boolean alwaysVisibleNametags()
	{
		return isEnabled() && alwaysVisible.isChecked();
	}
	
	public boolean isUnlimitedRange()
	{
		return isEnabled() && unlimited.isChecked();
	}
}
