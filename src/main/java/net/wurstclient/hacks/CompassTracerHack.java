/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import org.lwjgl.opengl.GL11;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

public final class CompassTracerHack extends Hack implements RenderListener
{
	public CompassTracerHack()
	{
		super("CompassTracer", "Draws a tracer to where your compass points to.");
		setCategory(Category.RENDER);
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
		BlockPos spawn = MC.world.getSpawnPos();
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glColor4f(0.1F, 0.1F, 0.1F, 0.5F);
		RenderUtils.applyRenderOffset();
		
		// tracer line
		GL11.glBegin(GL11.GL_LINES);
		{
			// set start position
			Vec3d start = RotationUtils.getClientLookVec()
				.add(RenderUtils.getCameraPos());
					
			// set end position
			Vec3d end = new Vec3d(spawn).add(0.5, 0.5, 0.5);
			
			// draw line
			GL11.glVertex3d(start.x, start.y, start.z);
			GL11.glVertex3d(end.x, end.y, end.z);
		}
		GL11.glEnd();
				
		// block box
		{
			GL11.glPushMatrix();
			GL11.glTranslated(spawn.getX(), spawn.getY(),
				spawn.getZ());
					
			RenderUtils.drawOutlinedBox();
			
			GL11.glColor4f(0.1F, 0.1F, 0.1F, 0.25F);
			RenderUtils.drawSolidBox();
			
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
				
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}
