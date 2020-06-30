/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.RenderUtils;

@SearchTags({"health tags"})
public final class HealthTagsHack extends Hack implements RenderListener
{
	public final CheckboxSetting mobs = new CheckboxSetting(
		"Health Tags for Mobs", false);
	
	public HealthTagsHack()
	{
		super("HealthTags", "Shows the health of players in their nametags.");
		setCategory(Category.RENDER);
		addSetting(mobs);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		if(!mobs.isChecked())
			return;
		for(Entity entity : MC.world.getEntities())
			if(entity instanceof MobEntity)
			{
				MobEntity en = (MobEntity)entity;
				int health = (int)en.getHealth();
				int maxHealth = (int)en.getMaximumHealth();
				if(maxHealth == 0)
					maxHealth = 1;
				String tag = getColor(health, maxHealth) + health + "/" + maxHealth;
				if(!en.hasCustomName())
					RenderUtils.renderTag(tag, en, 16777215, 0.5D, 75, partialTicks);
				else 
					RenderUtils.renderTag(tag, en, 16777215, 1.0D, 75, partialTicks);
			}
	}
	
	public String addHealth(LivingEntity entity, String nametag)
	{
		if(!isEnabled())
			return nametag;
		
		int health = (int)entity.getHealth();
		int maxHealth = (int)entity.getMaximumHealth();
		return nametag + " " + getColor(health, maxHealth) + health;
	}
	
	private String getColor(int health, int maxHealth)
	{
		if(health <= maxHealth * 0.25)
			return "\u00a74";
		
		if(health <= maxHealth * 0.5)
			return "\u00a76";
		
		if(health <= maxHealth * 0.75)
			return "\u00a7e";
		
		return "\u00a7a";
	}
}
