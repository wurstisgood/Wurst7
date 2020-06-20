/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.RenderUtils;

public final class BreadcrumbsHack extends Hack implements UpdateListener, RenderListener
{
	private List<double[]> points = new CopyOnWriteArrayList<>();
	private final CheckboxSetting opacity = new CheckboxSetting(
		"See Lines Through Walls", false);
	
	public BreadcrumbsHack()
	{
		super("Breadcrumbs",
			"Leaves a trail of breadcrumbs behind you.\n"
				+  "Type .breadcrumbs clear to remove breadcrumbs.");
		setCategory(Category.RENDER);
		addSetting(opacity);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		points.clear();
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!WURST.getHax().freecamHack.isEnabled())
			if(points.size() > 0) 
			{
				double x = Math.abs(points.get(points.size() - 1)[0] - MC.player.getX());
				double y = Math.abs(points.get(points.size() - 1)[1] - MC.player.getY());
				double z = Math.abs(points.get(points.size() - 1)[2] - MC.player.getZ());
				if(x > 0.25d || y > 0.25d || z > 0.25d)
					points.add(new double[]{MC.player.getX(), MC.player.getY(), MC.player.getZ()});
			}else 
				points.add(new double[]{MC.player.getX(), MC.player.getY(), MC.player.getZ()});
		if(WURST.getCmds().breadcrumbsCmd.clearCrumbs)
		{
			points.clear();
			WURST.getCmds().breadcrumbsCmd.clearCrumbs = false;
		}
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		for(int i = 1; i < points.size(); i++) 
		{
			double[] f = points.get(i-1);
			double[] t = points.get(i);
			Vec3d from = new Vec3d(f[0], f[1], f[2]);
			Vec3d to = new Vec3d(t[0], t[1], t[2]);
			line(from, to, 2F, 0, 0, 1, 0.75F, opacity.isChecked());
		}
	}
	
	private void line(Vec3d from, Vec3d to, float width, 
		float red, float green, float blue, float alpha, boolean opacity) 
	{
		Tessellator ts = Tessellator.getInstance();
		BufferBuilder bb = ts.getBuffer();
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		glLineWidth(width);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LINE_SMOOTH);
		if(opacity)
		{
			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
		}

		glColor4f(red, green, blue, alpha);

		double[] pf = renderPos(from);
		double[] pt = renderPos(to);
		
		GL11.glPushMatrix();
		RenderUtils.applyCameraRotationOnly();
		bb.begin(1, VertexFormats.POSITION);
		bb.vertex(pf[0], pf[1], pf[2]).next();
		bb.vertex(pt[0], pt[1], pt[2]).next();
		ts.draw();
		GL11.glPopMatrix();

		glEnable(GL_TEXTURE_2D);
		if(opacity)
		{
			glEnable(GL_DEPTH_TEST);
			glDepthMask(true);
		}
		glDisable(GL_LINE_SMOOTH);
		glDisable(GL_BLEND);
	}
	
	private double[] renderPos(Vec3d vec)
	{
		Vec3d camPos = BlockEntityRenderDispatcher.INSTANCE.camera.getPos();
        double x = vec.x - camPos.getX();
        double y = vec.y - camPos.getY();
        double z = vec.z - camPos.getZ();
        return new double[]{x, y, z};
	}
}
