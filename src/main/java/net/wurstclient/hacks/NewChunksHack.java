/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;

public final class NewChunksHack extends Hack implements PacketInputListener, RenderListener
{
	private Map<String, Set<ChunkPos>> chunkMap = new HashMap<>();
	private int buffer;
	private int capacity;
	private boolean setup = false;

	public NewChunksHack()
	{
		super("NewChunks", "Highlights new chunks on Spigot servers.");
		setCategory(Category.RENDER);
	}

	@Override
	public void onEnable()
	{
		if(!setup)
		{
			buffer = GL15.glGenBuffers();
			getCapacity(0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0);
			setup = true;
		}
		EVENTS.add(PacketInputListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}

	@Override
	public void onDisable()
	{
		EVENTS.remove(PacketInputListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		Set<ChunkPos> set = getSet(false);
		if(set != null)
		{
			GlStateManager.enableBlend();
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GlStateManager.disableDepthTest();
			GlStateManager.disableTexture();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GlStateManager.depthMask(false);
			GlStateManager.pushMatrix();
			GL11.glLineWidth(1);
			GlStateManager.color4f(1, 0, 0, 1);
			try
			{
				Set<ChunkPos> copy = new HashSet<>(set);
				copy.forEach(cpos -> {
					GlStateManager.pushMatrix();
					RenderUtils.applyCameraRotationOnly();
					Vec3d camPos = BlockEntityRenderDispatcher.INSTANCE.camera.getPos();
					GlStateManager.translated(
						-camPos.getX() + cpos.getStartX(),
						-camPos.getY(),
						-camPos.getZ() + cpos.getStartZ());
					GlStateManager.scalef(16, 0, 16);
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
					GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glDrawArrays(2, 0, capacity);
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
					GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
					GlStateManager.popMatrix();
				});
			}catch(Exception e)
			{
			}
			GlStateManager.popMatrix();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GlStateManager.enableDepthTest();
			GlStateManager.depthMask(true);
		}
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		if(event.getPacket() instanceof ChunkDataS2CPacket
			&& !((ChunkDataS2CPacket)event.getPacket()).isFullChunk())
		{
			ChunkDataS2CPacket packet = (ChunkDataS2CPacket)event.getPacket();
			getSet(true).add(MC.world.getChunk(packet.getX(), packet.getZ())
				.getPos());
		}
	}
	
	private Set<ChunkPos> getSet(boolean createNew)
	{
		return chunkMap.computeIfAbsent(getUniqueWorldString(),
			(s) -> createNew ? new HashSet<>() : null);
	}
	
	private String getUniqueWorldString()
	{
		ServerInfo data = MC.getCurrentServerEntry();
		StringBuilder sb = new StringBuilder();
		if(MC.isIntegratedServerRunning())
			sb.append("sp:");
		else if(MC.isConnectedToRealms())
			sb.append("realms:");
		else
			sb.append("mp:");
		if(data != null)
			sb.append(data.address);
		else
			sb.append("Disconnected");
		sb.append(":").append(MC.player.dimension.getRawId());
		return sb.toString();
	}
	
	public void getCapacity(float... fList)
	{
		FloatBuffer buf = ByteBuffer.allocateDirect(fList.length * 4)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
		buf.put(fList).flip();
		capacity = buf.capacity();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
}
