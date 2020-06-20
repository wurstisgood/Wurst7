package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.hack.Hack;

public class NoLightningHack extends Hack
{
	public NoLightningHack()
	{
		super("NoLightning", "Disables lightning.");
		setCategory(Category.RENDER);
	}
}
