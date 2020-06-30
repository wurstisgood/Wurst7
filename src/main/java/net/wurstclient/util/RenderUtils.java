/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.Rotation3;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;

public enum RenderUtils
{
	;
	
	private static final Box DEFAULT_AABB = new Box(0, 0, 0, 1, 1, 1);
	
	public static void scissorBox(int startX, int startY, int endX, int endY)
	{
		int width = endX - startX;
		int height = endY - startY;
		int bottomY = WurstClient.MC.currentScreen.height - endY;
		double factor = WurstClient.MC.getWindow().getScaleFactor();
		
		int scissorX = (int)(startX * factor);
		int scissorY = (int)(bottomY * factor);
		int scissorWidth = (int)(width * factor);
		int scissorHeight = (int)(height * factor);
		GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
	}
	
	public static void applyRenderOffset()
	{
		applyCameraRotationOnly();
		Vec3d camPos = getCameraPos();
		GL11.glTranslated(-camPos.x, -camPos.y, -camPos.z);
	}
	
	public static void applyCameraRotationOnly()
	{
		Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
		GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1, 0, 0);
		GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0), 0, 1,
			0);
	}
	
	public static Vec3d getCameraPos()
	{
		return BlockEntityRenderDispatcher.INSTANCE.camera.getPos();
	}
	
	public static void drawSolidBox()
	{
		drawSolidBox(DEFAULT_AABB);
	}
	
	public static void drawSolidBox(Box bb)
	{
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glEnd();
	}
	
	public static void drawOutlinedBox()
	{
		drawOutlinedBox(DEFAULT_AABB);
	}
	
	public static void drawOutlinedBox(Box bb)
	{
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glEnd();
	}
	
	public static void drawCrossBox()
	{
		drawCrossBox(DEFAULT_AABB);
	}
	
	public static void drawCrossBox(Box bb)
	{
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x1, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x2, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y2, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y2, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z1);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z2);
		
		GL11.glVertex3d(bb.x2, bb.y1, bb.z2);
		GL11.glVertex3d(bb.x1, bb.y1, bb.z1);
		GL11.glEnd();
	}
	
	public static void drawNode(Box bb)
	{
		double midX = (bb.x1 + bb.x2) / 2;
		double midY = (bb.y1 + bb.y2) / 2;
		double midZ = (bb.z1 + bb.z2) / 2;
		
		GL11.glVertex3d(midX, midY, bb.z2);
		GL11.glVertex3d(bb.x1, midY, midZ);
		
		GL11.glVertex3d(bb.x1, midY, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);
		
		GL11.glVertex3d(midX, midY, bb.z1);
		GL11.glVertex3d(bb.x2, midY, midZ);
		
		GL11.glVertex3d(bb.x2, midY, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);
		
		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(bb.x2, midY, midZ);
		
		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(bb.x1, midY, midZ);
		
		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);
		
		GL11.glVertex3d(midX, bb.y2, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);
		
		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(bb.x2, midY, midZ);
		
		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(bb.x1, midY, midZ);
		
		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(midX, midY, bb.z1);
		
		GL11.glVertex3d(midX, bb.y1, midZ);
		GL11.glVertex3d(midX, midY, bb.z2);
	}
	
	public static void drawArrow(Vec3d from, Vec3d to)
	{
		double startX = from.x;
		double startY = from.y;
		double startZ = from.z;
		
		double endX = to.x;
		double endY = to.y;
		double endZ = to.z;
		
		GL11.glPushMatrix();
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(startX, startY, startZ);
		GL11.glVertex3d(endX, endY, endZ);
		GL11.glEnd();
		
		GL11.glTranslated(endX, endY, endZ);
		GL11.glScaled(0.1, 0.1, 0.1);
		
		double angleX = Math.atan2(endY - startY, startZ - endZ);
		GL11.glRotated(Math.toDegrees(angleX) + 90, 1, 0, 0);
		
		double angleZ = Math.atan2(endX - startX,
			Math.sqrt(Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2)));
		GL11.glRotated(Math.toDegrees(angleZ), 0, 0, 1);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(0, 2, 1);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(-1, 2, 0);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 2, -1);
		GL11.glVertex3d(1, 2, 0);
		
		GL11.glVertex3d(1, 2, 0);
		GL11.glVertex3d(0, 2, 1);
		
		GL11.glVertex3d(1, 2, 0);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(0, 2, 1);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(1, 2, 0);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(-1, 2, 0);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 2, -1);
		
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 2, 1);
		GL11.glEnd();
		
		GL11.glPopMatrix();
	}
	
	public static void renderTag(String tag, Entity entity, int color, 
		double height, int limit, float partialTicks)
	{
		EntityRenderDispatcher manager = WurstClient.MC.getEntityRenderManager();
		double dist = manager.getSquaredDistanceToCamera(entity);
		if(dist <= (limit * limit))
		{
			TextRenderer font = WurstClient.MC.textRenderer;
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
				WurstClient.INSTANCE.getHax().nameTagsHack.isEnabled();
			float scale =
				(float)(0.016666668F
					* 1.6F
					* (entity.distanceTo(WurstClient.MC.getCameraEntity()) > 10
						&& nameTagsActive
						? entity.distanceTo(WurstClient.MC.getCameraEntity()) / 10
							: 1));
			scale *=
				(nameTagsActive
					? (entity.getBoundingBox().getAverageSideLength() > -50
						? 1
							: entity.distanceTo(WurstClient.MC.getCameraEntity()) / 300)
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
}
