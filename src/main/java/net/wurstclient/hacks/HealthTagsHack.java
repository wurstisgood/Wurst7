/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
				String tag = "";
				float percent = health * 100 / maxHealth;
				if(percent <= 25)
					tag += "\u00a74";
				else if(percent <= 50)
					tag += "\u00a76";
				else if(percent <= 75)
					tag += "\u00a7e";
				else if(percent <= 100)
					tag += "\u00a7a";
				tag += health + "/" + maxHealth;
				if(!en.hasCustomName())
					renderTag(tag, en, 16777215, 0.5D, 75, partialTicks);
				else 
					renderTag(tag, en, 16777215, 1.0D, 75, partialTicks);
			}
	}
	
	public static void renderTag(String tag, Entity entity, int color, 
		double height, int limit, float partialTicks)
	{
		EntityRenderDispatcher manager = MC.getEntityRenderManager();
		double dist = manager.getSquaredDistanceToCamera(entity);
		if(dist <= (limit * limit))
		{
			TextRenderer font = MC.textRenderer;
			int width = font.getStringWidth(tag);
			GL11.glPushMatrix();
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);   
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glLineWidth(1.0F);
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
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.3F);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3d(-width / 2 - 2, -2.0D, 0.0D);
			GL11.glVertex3d(-width / 2 - 2, 9.0D, 0.0D);
			GL11.glVertex3d(width / 2 + 2, 9.0D, 0.0D);
			GL11.glVertex3d(width / 2 + 2, -2.0D, 0.0D);
			GL11.glEnd();
			GL11.glColor3f(1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			font.draw(tag, -width / 2, 0, color, false, Rotation3.identity().getMatrix(), immediate, true, 0, 15728880);
			immediate.draw();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
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
