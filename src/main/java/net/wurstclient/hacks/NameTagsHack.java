/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.json.JsonUtils;

@SearchTags({"name tags"})
public final class NameTagsHack extends Hack implements RenderListener
{
	private final CheckboxSetting alwaysVisible = new CheckboxSetting(
		"Always See NameTags", false);	
	private final CheckboxSetting unlimited = new CheckboxSetting(
		"Unlimited Range Nametags", false);
	private final CheckboxSetting tamed = new CheckboxSetting(
		"See Owner for Tamed Entities", false);
	private Map<UUID, String> resolvedNames = new HashMap<>();
	
	public NameTagsHack()
	{
		super("NameTags", "Changes the scale of the nametags so you can\n"
			+ "always read them.\n" + "Also allows you to see the nametags of\n"
			+ "sneaking players.");
		
		setCategory(Category.RENDER);
		addSetting(alwaysVisible);	
		addSetting(unlimited);
		addSetting(tamed);
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
		if(!tamed.isChecked())
			return;
		for(Entity entity : MC.world.getEntities())
			if((entity instanceof TameableEntity && ((TameableEntity)entity).isTamed())
				|| (entity instanceof HorseBaseEntity && ((HorseBaseEntity)entity).isTame()))
			{
				UUID uuid = entity instanceof TameableEntity ? ((TameableEntity)entity).getOwnerUuid()
					: ((HorseBaseEntity)entity).getOwnerUuid();
				String name = resolveName(uuid);
				resolvedNames.put(uuid, name);
				double offset = WURST.getHax().healthTagsHack.isEnabled()
					&& WURST.getHax().healthTagsHack.mobs.isChecked() ? 0.5 : 0;
				if(!entity.hasCustomName())
					RenderUtils.renderTag("Owner: " + name, entity, 16777215, 0.5D + offset, 75, partialTicks);
				else 
					RenderUtils.renderTag("Owner: " + name, entity, 16777215, 1.0D + offset, 75, partialTicks);
			}
	}
	
	public String resolveName(UUID uuid)
	{
		//Check if name exists
		if(resolvedNames.containsKey(uuid))
			return resolvedNames.get(uuid);
		//Then try online UUID lookup
		try
		{
			String profileUrl = "https://sessionserver.mojang.com/session/minecraft/profile/";
			//Open connection
			HttpURLConnection connection = (HttpURLConnection)new URL(
				profileUrl + uuid.toString().replace("-", ""))
				.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			//Scan result
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null)
				result.append(line);
			reader.close();
			
			//Convert to JSON element
			JsonElement jsonElement = JsonUtils.GSON.fromJson(result.toString(), JsonElement.class);
			
			if(jsonElement == null || !jsonElement.isJsonObject())
				return uuid.toString();

			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			if(jsonObject.has("error") && jsonObject.has("errorMessage") 
				&& jsonObject.get("errorMessage").getAsString().length() > 0)
			{
				ChatUtils.message("Recieved error from Mojang API: " + 
				jsonObject.get("error").getAsString() + " - " + jsonObject.get("errorMessage").getAsString());
				return uuid.toString();
			}
			
			if(!jsonObject.has("name"))
				return uuid.toString();
			
			return jsonObject.get("name").getAsString();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return uuid.toString();
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
