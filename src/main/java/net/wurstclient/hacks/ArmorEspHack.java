/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.RenderUtils;

@SearchTags({"armor esp"})
public final class ArmorEspHack extends Hack implements RenderListener
{
	public final CheckboxSetting showEnchants = new CheckboxSetting(
		"Show Enchantments", false);
	public final CheckboxSetting showRel = new CheckboxSetting(
		"Only Show Relevant Enchantments", true);
	
	public ArmorEspHack()
	{
		super("ArmorESP", "Allows you to see other players' armor above their nametag.");
		setCategory(Category.RENDER);
		addSetting(showEnchants);
		addSetting(showRel);
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
		for(AbstractClientPlayerEntity entity : MC.world.getPlayers())
			if(entity != MC.player)
			{
				for(int i = 0; i < 6; i++)
					if((!getArmorItem(i, entity).isEmpty()))
						renderArmor(entity, getArmorItem(i, entity),
							0.75, true, 75, i, showEnchants.isChecked(), showRel.isChecked(), partialTicks);
			}
	}
	
	private ItemStack getArmorItem(int id, AbstractClientPlayerEntity entity)
	{
		return id == 0 ? entity.inventory.getMainHandStack() : 
			id == 1 ? entity.inventory.offHand.get(0) : entity.inventory.getArmorStack(id - 2);
	}
	
	private void renderArmor(Entity entity, ItemStack item, 
		double height, boolean depth, int limit, int armorId, boolean showEnchants,
		boolean showRel, float partialTicks)
	{
		EntityRenderDispatcher manager = MC.getEntityRenderManager();
		double dist = manager.getSquaredDistanceToCamera(entity);
		if(dist <= (limit * limit))
		{
			GL11.glPushMatrix();
			RenderUtils.applyCameraRotationOnly();
			Vec3d camPos = RenderUtils.getCameraPos();
			GL11.glTranslated(
				-camPos.x + entity.prevX
				+ (entity.getX() - entity.prevX) * partialTicks,
				-camPos.y + entity.prevY
				+ (entity.getY() - entity.prevY) * partialTicks + entity.getHeight() + height,
				-camPos.z + entity.prevZ
				+ (entity.getZ() - entity.prevZ) * partialTicks);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			boolean nameTagsActive =
				WURST.getHax().nameTagsHack.isEnabled();
			float scale =
				(float)(0.016666668F
					* 1.6F
					* (entity.distanceTo(MC.getCameraEntity()) > 10
						&& nameTagsActive
						? entity.distanceTo(MC.getCameraEntity()) / 10
							: 1));
			scale *=
				(nameTagsActive
					? (entity.getBoundingBox().getAverageSideLength() > -50
						? 1
							: entity.distanceTo(MC.getCameraEntity()) / 300)
						: 1);
			GL11.glScaled(-scale, -scale, scale);
		    Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
			GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw()), 0, 1,
				0);
			GL11.glRotated(MathHelper.wrapDegrees(-camera.getPitch()), 1, 0,
				0);
		    GlStateManager.enableRescaleNormal();
		  	GlStateManager.enableBlend();
		  	GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, 
		  		GL11.GL_NONE);
		  	DiffuseLighting.enable();
			if(depth)
		  		GlStateManager.disableDepthTest();
			MC.getItemRenderer().zOffset = -147F;
			MC.getItemRenderer().renderGuiItem(item, -50 + armorId * 20, -20);
			if(item.isDamaged())
			{
			    int damage = (int)Math.round(13.0D - item.getDamage() * 13.0D / item.getMaxDamage());
 				int damageColor = (int)Math.round(255.0D - item.getDamage() * 255.0D / item.getMaxDamage());
 				if(depth)
 					GlStateManager.disableDepthTest();
 				GlStateManager.disableTexture();
 				GlStateManager.disableAlphaTest();
 				GlStateManager.disableBlend();
 				Tessellator tessellator = Tessellator.getInstance();
 				BufferBuilder bufferbuilder = tessellator.getBuffer();
 				renderQuad(bufferbuilder, -50 + armorId * 20, 0, 13, 2, 0, 0, 0, 255);
 				renderQuad(bufferbuilder, -50 + armorId * 20, 0, 12, 1, (255 - damageColor) / 4, 64, 0, 255);
 				renderQuad(bufferbuilder, -50 + armorId * 20, 0, damage, 1, 255 - damageColor
 					, damageColor, 0, 255);
 				GlStateManager.enableAlphaTest();
 				GlStateManager.enableTexture();
 				if(depth)
 					GlStateManager.enableDepthTest();
 				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		    }
		    int count = item.getCount();
		    if(count > 1)
		    {
		    	String counts = Integer.toString(count);
		    	if(depth)
		    		GlStateManager.disableDepthTest();
		    	GlStateManager.disableBlend();
		    	MC.textRenderer.drawWithShadow(counts, -50 + armorId * 20 + 17 - 
		    		MC.textRenderer.getStringWidth(counts), 
		    		0 - 4, 16777215);
		    	if(depth)
		    		GlStateManager.enableDepthTest();
		    }
		    if(showEnchants && item.hasEnchantments())
		    {
		    	if(depth)
		    		GlStateManager.disableDepthTest();
		    	GlStateManager.disableBlend();
		    	GL11.glScaled(0.5, 0.5, 0.5);
		    	int index = 0;
		    	for(Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(item).entrySet())
		    	{
	                if(showRel && !entry.getKey().type.isAcceptableItem(item.getItem()))
	                	continue;
	                index++;
	                MC.textRenderer.drawWithShadow(getShortName(entry.getKey())
	                	+ entry.getValue(), -95 + armorId * 40 - 
			    		MC.textRenderer.getStringWidth(getShortName(entry.getKey()) + entry.getValue()), 
			    		-60 + MC.textRenderer.fontHeight * index, 16777215);
	            }
		    	if(depth)
		    		GlStateManager.enableDepthTest();
		    }
		    GlStateManager.scaled(0.3, 0.3, 0.3);
		    DiffuseLighting.disable();
		    GlStateManager.disableRescaleNormal();
		    //Changing this caused issues in older versions
		    GlStateManager.disableBlend();
		    GlStateManager.enableAlphaTest();
			if(depth)
		  		GlStateManager.disableDepthTest();
			GL11.glPopMatrix();
			MC.getItemRenderer().zOffset = 0;
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
	
	private String getShortName(Enchantment enchantment)
	{
		if(enchantment == Enchantments.PROTECTION)
			return "PR";
		if(enchantment == Enchantments.FIRE_PROTECTION)
			return "FP";
		if(enchantment == Enchantments.FEATHER_FALLING)
			return "FF";
		if(enchantment == Enchantments.BLAST_PROTECTION)
			return "BP";
		if(enchantment == Enchantments.PROJECTILE_PROTECTION)
			return "PP";
		if(enchantment == Enchantments.RESPIRATION)
			return "RES";
		if(enchantment == Enchantments.AQUA_AFFINITY)
			return "AQ";
		if(enchantment == Enchantments.THORNS)
			return "TH";
		if(enchantment == Enchantments.DEPTH_STRIDER)
			return "DS";
		if(enchantment == Enchantments.FROST_WALKER)
			return "FW";
		if(enchantment == Enchantments.BINDING_CURSE)
			return "BI";
		if(enchantment == Enchantments.SHARPNESS)
			return "SH";
		if(enchantment == Enchantments.SMITE)
			return "SM";
		if(enchantment == Enchantments.BANE_OF_ARTHROPODS)
			return "BA";
		if(enchantment == Enchantments.KNOCKBACK)
			return "KN";
		if(enchantment == Enchantments.FIRE_ASPECT)
			return "FI";
		if(enchantment == Enchantments.LOOTING)
			return "LO";
		if(enchantment == Enchantments.SWEEPING)
			return "SW";
		if(enchantment == Enchantments.EFFICIENCY)
			return "EF";
		if(enchantment == Enchantments.SILK_TOUCH)
			return "SL";
		if(enchantment == Enchantments.UNBREAKING)
			return "UN";
		if(enchantment == Enchantments.FORTUNE)
			return "FO";
		if(enchantment == Enchantments.POWER)
			return "PO";
		if(enchantment == Enchantments.PUNCH)
			return "PU";
		if(enchantment == Enchantments.FLAME)
			return "FL";
		if(enchantment == Enchantments.INFINITY)
			return "IN";
		if(enchantment == Enchantments.LUCK_OF_THE_SEA)
			return "LS";
		if(enchantment == Enchantments.LURE)
			return "LU";
		if(enchantment == Enchantments.LOYALTY)
			return "LY";
		if(enchantment == Enchantments.IMPALING)
			return "IM";
		if(enchantment == Enchantments.RIPTIDE)
			return "RIP";
		if(enchantment == Enchantments.CHANNELING)
			return "CH";
		if(enchantment == Enchantments.MULTISHOT)
			return "MI";
		if(enchantment == Enchantments.QUICK_CHARGE)
			return "QC";
		if(enchantment == Enchantments.PIERCING)
			return "PI";
		if(enchantment == Enchantments.MENDING)
			return "ME";
		if(enchantment == Enchantments.VANISHING_CURSE)
			return "VA";
		return "??";
	}
}
