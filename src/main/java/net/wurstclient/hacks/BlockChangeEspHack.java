/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.Category;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;

public final class BlockChangeEspHack extends Hack implements RenderListener, PacketInputListener
{
	private ArrayList<BlockPos> matchingBlocks = new ArrayList<>();
	private int maxBlocks = 400;
	public boolean notify = true;
	private long time;
	private static final Box BOX = new Box(0, 0, 0, 1, 1, 1);
	
	private final CheckboxSetting clearAfterDelay = new CheckboxSetting(
		"Clear blocks every 15 seconds", false);
	
	public BlockChangeEspHack()
	{
		super("BlockChangeESP", 
			"Allows you to see blocks that have changed.\n"
				+ "If more than 400 blocks have changed, the blocks reset.\n"
				+ "Very good for tracking far players.\n"
				+ "Note: This will also show where players right clicked and left clicked.");
		setCategory(Category.RENDER);
		addSetting(clearAfterDelay);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(RenderListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		matchingBlocks.clear();
		EVENTS.remove(RenderListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		if(event.getPacket() instanceof BlockUpdateS2CPacket) 
		{
			BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket)event.getPacket();
			matchingBlocks.add(packet.getPos());
			if(matchingBlocks.size() >= maxBlocks && notify)
			{
				ChatUtils.warning(getName() + " found over " + maxBlocks + " blocks.");
				ChatUtils.message("To prevent lag, the blocks highlighted have been reset.");
				matchingBlocks.clear();
				notify = false;
			}else if(matchingBlocks.size() < maxBlocks)
				notify = true;
		}
		if(clearAfterDelay.isChecked() && System.currentTimeMillis() >= time + 15000)
		{
			matchingBlocks.clear();
			time = System.currentTimeMillis();
		}    	
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		float alpha =
			0.5F - MathHelper.abs(MathHelper
				.sin(System.currentTimeMillis() % 2000L / 1000.0F
					* (float)Math.PI * 1.0F) * 0.3F);
		RenderUtils.applyRenderOffset();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glLineWidth(1.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
			
		for(int i = 0; i < matchingBlocks.size(); i++)
		{
			BlockPos pos = matchingBlocks.get(i);
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glColor4d(0.25, 0.25, 1, 0.15F);
			RenderUtils.drawSolidBox(BOX);
			GL11.glColor4d(0, 0, 0, alpha);
			RenderUtils.drawOutlinedBox(BOX);
			GL11.glPopMatrix();
		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
