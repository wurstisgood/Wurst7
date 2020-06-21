/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.wurstclient.Category;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class PotionEspHack extends Hack implements GUIRenderListener
{
	private final CheckboxSetting pot1 = new CheckboxSetting(
		"Show Potion Effect 1", true);
	private final EnumSetting<EnumPotion> potType1 = new EnumSetting<>(
		"Potion Type 1", EnumPotion.values(), EnumPotion.STRENGTH);
	private final SliderSetting potAmp1 = new SliderSetting("Minimum Amplifier for Potion 1", 1, 1, 5,
		1, ValueDisplay.INTEGER);
	private final CheckboxSetting pot2 = new CheckboxSetting(
		"Show Players with Regeneration", true);
	private final EnumSetting<EnumPotion> potType2 = new EnumSetting<>(
		"Potion Type 2", EnumPotion.values(), EnumPotion.REGENERATION);
	private final SliderSetting potAmp2 = new SliderSetting("Minimum Amplifier for Potion 2", 1, 1, 5,
		1, ValueDisplay.INTEGER);
	private final CheckboxSetting pot3 = new CheckboxSetting(
		"Show Players with Speed", false);
	private final EnumSetting<EnumPotion> potType3 = new EnumSetting<>(
		"Potion Type 3", EnumPotion.values(), EnumPotion.SPEED);
	private final SliderSetting potAmp3 = new SliderSetting("Minimum Amplifier for Potion 3", 1, 1, 5,
		1, ValueDisplay.INTEGER);
	private final SliderSetting x = new SliderSetting("Position X", 0, 0, 600,
		3, ValueDisplay.INTEGER);
	private final SliderSetting y = new SliderSetting("Position Y", 0, 0, 300,
		3, ValueDisplay.INTEGER);
	
	public PotionEspHack()
	{
		super("PotionESP", "Shows all players with a potion effect with your choice.");
		setCategory(Category.RENDER);
		addSetting(pot1);
		addSetting(potType1);
		addSetting(potAmp1);
		addSetting(pot2);
		addSetting(potType2);
		addSetting(potAmp2);
		addSetting(pot3);
		addSetting(potType3);
		addSetting(potAmp3);
		addSetting(x);
		addSetting(y);
	}

	@Override
	public void onEnable()
	{
		EVENTS.add(GUIRenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(GUIRenderListener.class, this);
	}
	
	@Override
	public void onRenderGUI(float partialTicks)
	{
		List<String> eff1 = new ArrayList<>();
		List<String> eff2 = new ArrayList<>();
		List<String> eff3 = new ArrayList<>();
		//Placeholder strings
		eff1.add("");
		eff2.add("");
		eff3.add("");
		
		for(AbstractClientPlayerEntity player : MC.world.getPlayers())
			if(player != MC.player && player.getActiveStatusEffects() != null && !player.getActiveStatusEffects().isEmpty())
				for(Entry<StatusEffect, StatusEffectInstance> entry : player.getActiveStatusEffects().entrySet())
				{
					if(pot1.isChecked() && entry.getKey() == potType1.getSelected().getStatusEffect())
					{
						if(entry.getValue().getAmplifier() + 1 >= potAmp1.getValueI())
							eff1.add(player.getDisplayName().asFormattedString() + " \u00a7r- \u00a74" + 
								toRomanNumeral(entry.getValue()) + " " + StatusEffectUtil.durationToString(entry.getValue(), 1));
					}else if(pot2.isChecked() && entry.getKey() == potType2.getSelected().getStatusEffect())
					{
						if(entry.getValue().getAmplifier() + 1 >= potAmp2.getValueI())
							eff2.add(player.getDisplayName().asFormattedString() + " \u00a7r- \u00a72" + 
								toRomanNumeral(entry.getValue()) + " " + StatusEffectUtil.durationToString(entry.getValue(), 1));
					}else if(pot3.isChecked() && entry.getKey() == potType3.getSelected().getStatusEffect())
					{
						if(entry.getValue().getAmplifier() + 1 >= potAmp3.getValueI())
							eff3.add(player.getDisplayName().asFormattedString() + " \u00a7r- \u00a7b" + 
								toRomanNumeral(entry.getValue()) + " " + StatusEffectUtil.durationToString(entry.getValue(), 1));
					}
				}
		eff1.set(0, "\u00a74" + potType1.getSelected().toString() + " " +
			potAmp1.getValueI() + " or higher (" + (eff1.size() - 1) + "):");
		eff2.set(0, "\u00a72" + potType1.getSelected().toString() + " " +
			potAmp2.getValueI() + " or higher (" + (eff2.size() - 1) + "):");
		eff3.set(0, "\u00a7b" + potType1.getSelected().toString() + " " +
			potAmp3.getValueI() + " or higher (" + (eff3.size() - 1) + "):");
		int i = y.getValueI();
		
		if(eff1.size() > 1)
			for(String str : eff1)
			{
				MC.textRenderer.drawWithShadow(str, MC.getWindow().getScaledWidth() -
					MC.textRenderer.getStringWidth(str) - x.getValueI(), i, 16777215);
				i += 10;
			}
		if(eff2.size() > 1)
			for(String rgn : eff2)
			{
				MC.textRenderer.drawWithShadow(rgn, MC.getWindow().getScaledWidth() -
					MC.textRenderer.getStringWidth(rgn) - x.getValueI(), i, 16777215);
				i += 10;
			}
		if(eff3.size() > 1)
			for(String spd : eff3)
			{
				MC.textRenderer.drawWithShadow(spd, MC.getWindow().getScaledWidth() -
					MC.textRenderer.getStringWidth(spd) - x.getValueI(), i, 16777215);
				i += 10;
			}
	}

	private String toRomanNumeral(StatusEffectInstance inst)
	{
		String res = I18n.translate("enchantment.level." + (inst.getAmplifier() + 1));
		if(res.startsWith("enchantment.level."))
			return String.valueOf(inst.getAmplifier() + 1);
		return res;
	}
	
	private enum EnumPotion
	{
		SPEED("Speed", StatusEffects.SPEED),
		SLOWNESS("Slowness", StatusEffects.SLOWNESS),
		HASTE("Haste", StatusEffects.HASTE),
		MINING_FATIGUE("Mining Fatigue", StatusEffects.MINING_FATIGUE),
		STRENGTH("Strength", StatusEffects.STRENGTH),
		INSTANT_HEALTH("Instant Health", StatusEffects.INSTANT_HEALTH),
		INSTANT_DAMAGE("Instant Damage", StatusEffects.INSTANT_DAMAGE),
		JUMP_BOOST("Jump Boost", StatusEffects.JUMP_BOOST),
		NAUSEA("Nausea", StatusEffects.NAUSEA),
		REGENERATION("Regeneration", StatusEffects.REGENERATION),
		RESISTANCE("Resistance", StatusEffects.RESISTANCE),
		FIRE_RESISTANCE("Fire Resistance", StatusEffects.FIRE_RESISTANCE),
		WATER_BREATHING("Water Breathing", StatusEffects.WATER_BREATHING),
		INVISIBILITY("Invisibility", StatusEffects.INVISIBILITY),
		BLINDNESS("Blindness", StatusEffects.BLINDNESS),
		NIGHT_VISION("Night Vision", StatusEffects.NIGHT_VISION),
		HUNGER("Hunger", StatusEffects.HUNGER),
		WEAKNESS("Weakness", StatusEffects.WEAKNESS),
		POISON("Poison", StatusEffects.POISON),
		WITHER("Wither", StatusEffects.WITHER),
		HEALTH_BOOST("Health Boost", StatusEffects.HEALTH_BOOST),
		ABSORPTION("Absorption", StatusEffects.ABSORPTION),
		SATURATION("Saturation", StatusEffects.SATURATION),
		GLOWING("Glowing", StatusEffects.GLOWING),
		LEVITATION("Levitation", StatusEffects.LEVITATION),
		LUCK("Luck", StatusEffects.LUCK),
		UNLUCK("Unluck", StatusEffects.UNLUCK),
		SLOW_FALLING("Slow Falling", StatusEffects.SLOW_FALLING),
		CONDUIT_POWER("Conduit Power", StatusEffects.CONDUIT_POWER),
		DOLPHINS_GRACE("Dolphins Grace", StatusEffects.DOLPHINS_GRACE),
		BAD_OMEN("Bad Omen", StatusEffects.BAD_OMEN),
		HERO_OF_THE_VILLAGE("Hero of the Village", StatusEffects.HERO_OF_THE_VILLAGE);
		
		private final String name;
		private final StatusEffect effect;
		
		private EnumPotion(String name, StatusEffect effect)
		{
			this.name = name;
			this.effect = effect;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public StatusEffect getStatusEffect()
		{
			return effect;
		}
	}
}
