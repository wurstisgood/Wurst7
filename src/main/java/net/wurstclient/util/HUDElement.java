/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.wurstclient.WurstClient;

public class HUDElement
{
	private MatrixStack matrixStack;
	private ItemStack itemStack;
	private int iconW;
	private int iconH;
	private int padW;
	private int elementW;
	private int elementH;
	private String itemName = "";
  	private int itemNameW;
  	private String itemDamage = "";
  	private int itemDamageW;
  	private boolean isArmor;
  
  	public HUDElement(MatrixStack matrixStack, ItemStack itemStack, int iconW, int iconH, int padW, boolean isArmor)
  	{
  		this.matrixStack = matrixStack;
  		this.itemStack = itemStack;
  		this.iconW = iconW;
  		this.iconH = iconH;
  		this.padW = padW;
  		this.isArmor = isArmor;
  		
  		initSize();
  	}
  
  	public int width()
  	{
  		return elementW;
  	}
  
 	public int height()
 	{
 		return elementH;
 	}

 	private void initSize()
 	{
 		elementH = Math.max(WurstClient.MC.textRenderer.fontHeight * 
 			(WurstClient.INSTANCE.getHax().hudHack.showArmorNames() ? 2 : 1), iconH);
 		if(itemStack != null)
 		{
 			int damage = 1;
    		int maxDamage = 1;
    		int percentdamage = 1;
    		if(isArmor || (!isArmor && itemStack.isDamaged()))
    		{
    			maxDamage = itemStack.getMaxDamage() + 1;
    			damage = maxDamage - itemStack.getDamage();
    			percentdamage = damage * 100 / maxDamage;
    			itemDamage = "\u00a7" + getColorCode(percentdamage) + damage + "/" + maxDamage;
    		}
    		itemDamageW = WurstClient.MC.textRenderer.getWidth
    			(stripCtrl(itemDamage));
    		elementW = padW + iconW + padW + itemDamageW;
    		itemName = I18n.translate(itemStack.getTranslationKey());
    		elementW = padW + iconW + padW + Math.max(WurstClient.MC.textRenderer.getWidth(
    				(stripCtrl(itemName))), itemDamageW);
    		itemNameW = WurstClient.MC.textRenderer.getWidth(stripCtrl(itemName));
 		}
 	}
  
 	public void renderToHud(int x, int y)
 	{
 		ItemRenderer itemRenderer = WurstClient.MC.getItemRenderer();
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		GlStateManager.enableRescaleNormal();
 		GlStateManager.enableBlend();
 		GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, 
 			GL11.GL_NONE);
 		DiffuseLighting.enable();
 		itemRenderer.zOffset = 200.0F;
 		itemRenderer.renderGuiItemIcon(itemStack, x - (iconW + padW), y);
 		renderItemOverlayIntoGUI(WurstClient.MC.textRenderer, itemStack, x - 
 			(iconW + padW), y, true, true);
      
 		DiffuseLighting.disable();
 		GlStateManager.disableRescaleNormal();
 		GlStateManager.disableBlend();
      
 		if(WurstClient.INSTANCE.getHax().hudHack.showArmorNames())
 			WurstClient.MC.textRenderer.draw(matrixStack, itemName + "\u00a7r", x - (padW + 
 				iconW + padW) - itemNameW, y, 16777215);
 		WurstClient.MC.textRenderer.drawWithShadow(matrixStack, itemDamage + "\u00a7r", x - (padW + 
 			iconW + padW) - itemDamageW, y + (elementH / 
 				(WurstClient.INSTANCE.getHax().hudHack.showArmorNames() ? 2 : 4)), 16777215);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		itemRenderer.zOffset = 0;
 	}
  
 	private String stripCtrl(String string)
 	{
 		return string.replaceAll("(?i)\u00a7[0-9a-fklmnor]", "");
 	}
  
 	private void renderItemOverlayIntoGUI(TextRenderer textRenderer, ItemStack itemStack, 
 		int x, int y, boolean showDamageBar, boolean showCount)
 	{
 		if(itemStack != null && (showDamageBar || showCount))
 		{
 			if(itemStack.isDamaged() && showDamageBar)
 			{
 				int j = (int)Math.round(13.0D - itemStack.getDamage() * 13.0D / itemStack.getMaxDamage());
 				int i = (int)Math.round(255.0D - itemStack.getDamage() * 255.0D / itemStack.getMaxDamage());
 				GlStateManager.disableLighting();
 				GlStateManager.disableDepthTest();
 				GlStateManager.disableTexture();
 				GlStateManager.disableAlphaTest();
 				GlStateManager.disableBlend();
 				Tessellator tessellator = Tessellator.getInstance();
 				BufferBuilder bufferbuilder = tessellator.getBuffer();
 				renderQuad(bufferbuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
 				renderQuad(bufferbuilder, x + 2, y + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
 				renderQuad(bufferbuilder, x + 2, y + 13, j, 1, 255 - i, i, 0, 255);
 				GlStateManager.enableAlphaTest();
 				GlStateManager.enableTexture();
 				GlStateManager.enableLighting();
 				GlStateManager.enableDepthTest();
 				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 			}
 			if(showCount)
 			{
 				int count = 0;
 				if(itemStack.getMaxCount() > 1)
 					count = itemStack.getCount();
 				else if(itemStack.getItem().equals(Items.BOW))
 					count = countInInventory(WurstClient.MC.player, Items.ARROW);
 				if(count > 1)
 				{
 					String counts = "" + count;
 					GlStateManager.disableLighting();
 					GlStateManager.disableDepthTest();
 					GlStateManager.disableBlend();
 					VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
 					WurstClient.MC.textRenderer.draw(counts, x + 19 - 2 - textRenderer.getWidth(counts), 
 						y + 6 + 3, 16777215, true, matrixStack.peek().getModel(), immediate, true, 0, 15728880);
 					immediate.draw();
 					GlStateManager.enableLighting();
 					GlStateManager.enableDepthTest();
 				}
 			}
 		}
 	}
 	
 	private void renderQuad(BufferBuilder renderer, int x, int y, int width, 
		int height, int red, int green, int blue, int alpha)
	{
		renderer.begin(7, VertexFormats.POSITION_COLOR);
        renderer.vertex(x + 0, y + 0, 0.0D).color(red, green, blue, alpha).next();
        renderer.vertex(x + 0, y + height, 0.0D).color(red, green, blue, alpha).next();
        renderer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).next();
        renderer.vertex(x + width, y + 0, 0.0D).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
	}
  
 	private int countInInventory(PlayerEntity player, Item item)
 	{
 		int count = 0;
 		for(int i = 0; i < player.inventory.main.size(); i++)
 			if(!player.inventory.main.get(i).isEmpty()
 				&& item.equals(player.inventory.main.get(i).getItem()))
 				count += player.inventory.main.get(i).getCount();
 		return count;
 	}
  
 	private String getColorCode(int value)
 	{
 		if(value >= 0 && value < 10)
 			return "4";
 		else if(value >= 10 && value < 25)
 			return "c";
 		else if(value >= 25 && value < 40)
 			return "6";
 		else if(value >= 40 && value < 60)
 			return "e";
 		else if(value >= 60 && value < 80)
 			return "7";
 		else
 			return "f";
 	}
}
