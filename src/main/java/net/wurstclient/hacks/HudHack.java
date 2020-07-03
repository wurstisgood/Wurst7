/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.Category;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.HUDElement;

public final class HudHack extends Hack implements GUIRenderListener, PacketInputListener
{
	private final CheckboxSetting armorHud = new CheckboxSetting(
		"ArmorHUD", true);
	private final CheckboxSetting potionHud = new CheckboxSetting(
		"PotionHUD", true);
	private final CheckboxSetting compassTime = new CheckboxSetting(
		"Show Compass and Time", true);
	private final CheckboxSetting showNameA = new CheckboxSetting(
		"Show Armor Name", true);
	private final CheckboxSetting showNameP = new CheckboxSetting(
		"Show Potion Name", true);
	private final CheckboxSetting showLag = new CheckboxSetting(
		"Detect Server-Side Lag", true)
	{
		@Override
		public void update()
		{
			if(isChecked())
				lastPacketMS = System.currentTimeMillis();
		}
	};
	private final SliderSetting lagOffset = new SliderSetting("Show Lag After (s)", 2, 0, 10,
		0.1, ValueDisplay.DECIMAL);
	private final SliderSetting armorX = new SliderSetting("ArmorHUD X", 0, 0, 600,
		6, ValueDisplay.INTEGER);
	private final SliderSetting armorY = new SliderSetting("ArmorHUD Y", 3, 0, 300,
		3, ValueDisplay.INTEGER);
	private final SliderSetting potionX = new SliderSetting("Potion X", 0, 0, 600,
		6, ValueDisplay.INTEGER);
	private final SliderSetting potionY = new SliderSetting("Potion Y", 0, 0, 300,
		6, ValueDisplay.INTEGER);
	
	private long lastPacketMS;
	
	public HudHack()
	{
		super("HUD", "Shows armor status, potion effects, time, and coords on screen.");
		setCategory(Category.RENDER);
		addSetting(armorHud);
		addSetting(potionHud);
		addSetting(compassTime);
		addSetting(showNameA);
		addSetting(showNameP);
		addSetting(showLag);
		addSetting(lagOffset);
		addSetting(armorX);
		addSetting(armorY);
		addSetting(potionX);
		addSetting(potionY);
	}

	@Override
	public void onEnable()
	{
		lastPacketMS = System.currentTimeMillis();
		EVENTS.add(GUIRenderListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(GUIRenderListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
	}
	
	@Override
	public void onRenderGUI(MatrixStack matrixStack, float partialTicks)
	{
		Window window = MC.getWindow();
		if(potionHud.isChecked())
		{
			Map<StatusEffect, StatusEffectInstance> active =
				MC.player.getActiveStatusEffects();
			if(!active.isEmpty())
			{
				int yOffset = showNameP.isChecked() ? 20 : 18;
				int yBase = getYPotion(active.size(), yOffset, window);
				for(Iterator<Entry<StatusEffect, StatusEffectInstance>> itr =
					active.entrySet().iterator(); itr
						.hasNext(); yBase += yOffset)
				{
					Entry<StatusEffect, StatusEffectInstance> entry = itr.next();
					StatusEffect effect = entry.getKey();
					StatusEffectInstance inst = entry.getValue();
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					MC.getTextureManager().bindTexture(new Identifier(
						"textures/gui/container/inventory.png"));
					int xBase =
						getXPotion(22 + MC.textRenderer.getWidth("0:00"),
							window);
					String potionName = "";
					potionName = I18n.translate(effect.getTranslationKey());
					String lvl = I18n.translate("enchantment.level." + (inst.getAmplifier() + 1));
					if(lvl.startsWith("enchantment.level."))
						lvl = String.valueOf(inst.getAmplifier() + 1);
					potionName += " " + lvl;

					xBase =
						getXPotion(22 + MC.textRenderer.getWidth(potionName),
							window);
					String effectDuration =
						StatusEffectUtil.durationToString(inst, 1);
					xBase = getXPotion(0, window);
					StatusEffectSpriteManager manager = MC.getStatusEffectSpriteManager();
					Sprite sprite = manager.getSprite(effect);
					if(shouldRender(effect, inst.getDuration(), 10))
					{
						MC.getTextureManager().bindTexture(sprite.getAtlas().getId());
						DrawableHelper.drawSprite(matrixStack, xBase - 18, yBase,
							-150, 18, 18, sprite);
					}
					int stringWidth =
						MC.textRenderer.getWidth(potionName);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					if(showNameP.isChecked())
						MC.textRenderer.drawWithShadow(matrixStack, potionName + "\u00a7r",
							xBase - 22 - stringWidth, yBase, 16777215);
					int stringWidth2 =
							MC.textRenderer.getWidth(effectDuration);
					if(shouldRender(effect, inst.getDuration(), 10))
						MC.textRenderer.drawWithShadow(matrixStack,
							effectDuration + "\u00a7r", xBase - 22 - stringWidth2,
							yBase + (showNameP.isChecked() ? 10 : 5), 16777215);
				}
			}
		}
		if(armorHud.isChecked())
		{
			List<HUDElement> elements = getHUDElements(matrixStack);
			int yBase;
			if(elements.size() > 0)
			{
				int yOffset = showNameA.isChecked() ? 18 : 16;
				yBase = (int)armorY.getValueF();
				for(HUDElement e : elements)
				{
					e.renderToHud(getXArmor(0, window), yBase);
					yBase += yOffset;
				}
			}
		}
		if(compassTime.isChecked())
		{
			long time = MC.world.getTime() % 24000;
			long hours = (time + 6000) / 1000 % 24;
			long seconds = (long)((time + 6000) % 1000 * (60.0D / 1000));
			String colorHour = "";
			if(time >= 0 && time < 12000)
				colorHour = "\u00a7e" + hours;
			else if(time >= 12000 && time < 13500)
				colorHour = "\u00a75" + hours;
			else if(time >= 13500 && time < 22500)
				colorHour = "\u00a79" + hours;
			else
				colorHour = "\u00a75" + hours;
			String worldTime = "";
			if(seconds < 10)
				worldTime = colorHour + ":0" + seconds;
			else
				worldTime = colorHour + ":" + seconds;
			int direction = MathHelper.floor(MC.player.yaw * 256.0F 
				/ 360.0F + 0.5D) & 255;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int xBase = window.getScaledWidth() / 2 - 45 / 2;
			int yBase = 2;
			MC.getTextureManager().bindTexture(new Identifier("wurst", "compass.png"));
			if(direction < 128)
				DrawableHelper.drawTexture(matrixStack, xBase, yBase, -100, direction, 0, 65, 12, 256, 256);
			else
				DrawableHelper.drawTexture(matrixStack, xBase, yBase, -100, direction - 128, 12, 65, 12, 256, 256);
			MC.textRenderer.draw(matrixStack, "\u00a7c|", xBase + 32, yBase + 1, 16777215);
			MC.textRenderer.draw(matrixStack, "\u00a7c|\u00a7r", xBase + 32, yBase + 5, 16777215);
			MC.textRenderer.drawWithShadow(matrixStack, worldTime,
				window.getScaledWidth() / 2 - 110 / 2,
				6, 16777215);
		}
		if(showLag.isChecked() && System.currentTimeMillis() > lastPacketMS + lagOffset.getValue() * 1000)
			MC.textRenderer.drawWithShadow(matrixStack, "Time Since Last Packet: " +
				((double)Math.round((System.currentTimeMillis() - lastPacketMS) / 500)) / 2 + " S",
				window.getScaledWidth() / 2 - 130 / 2,
				16, 16777215);
	}

	private int getXPotion(int width, Window window)
	{
		return window.getScaledWidth() - width - 2 - (int)potionX.getValueF();
	}

	private int getXArmor(int width, Window window)
	{
		return window.getScaledWidth() - width - 2 - (int)armorX.getValueF();
	}
	
	private int getYPotion(int rowCount, int height, Window window)
	{
		return window.getScaledHeight() - rowCount * height - 20 - (int)potionY.getValueF();
	}

	private boolean shouldRender(StatusEffect pe, int ticksLeft,
		int thresholdSeconds)
	{
		if(ticksLeft / 20 <= thresholdSeconds)
			return ticksLeft % 20 < 10;
		return true;
	}

	private List<HUDElement> getHUDElements(MatrixStack matrixStack)
	{
		List<HUDElement> elements = new ArrayList<>();
		for(int i = 3; i >= -2; i--)
		{
			ItemStack itemStack = null;
			if(i == -2)
				itemStack = MC.player.getMainHandStack();
			if(i == -1)
				itemStack = MC.player.getOffHandStack();
			else if(i != -1 && i != -2)
				itemStack = MC.player.inventory.armor.get(i);
			if(!itemStack.isEmpty())
				elements.add(new HUDElement(matrixStack, itemStack, 16, 16, 2, i > -1));
		}
		return elements;
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		lastPacketMS = System.currentTimeMillis();
	}
	
	public boolean shouldHidePotionHud()
	{
		return potionHud.isChecked();
	}
	
	public boolean showArmorNames()
	{
		return showNameA.isChecked();
	}
}
