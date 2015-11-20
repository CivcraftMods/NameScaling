package com.biggestnerd.namescaling;

import java.awt.Color;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid="namescaling", name="Name Scaling", version="v1.0")
public class NameScaling {

	private Minecraft mc;
	private KeyBinding toggle;
	private boolean enabled = true;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		mc = Minecraft.getMinecraft();
		ClientRegistry.registerKeyBinding(toggle = new KeyBinding("Toggle Scaling", Keyboard.KEY_N, "Name Scaling"));
	}
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		for(Object o : mc.theWorld.loadedEntityList) {
			if(o instanceof EntityOtherPlayerMP) {
				EntityOtherPlayerMP player = (EntityOtherPlayerMP) o;
				renderNametag(player, event.partialTicks);
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if(toggle.isKeyDown()) {
			enabled = !enabled;
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_AQUA + "[NameScaling]" + EnumChatFormatting.GRAY + "Scaling " + (enabled ? "enabled" : "disabled")));
		}
	}
	
	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
		if(event.entity instanceof EntityOtherPlayerMP) {
			event.setCanceled(true);
		}
	}
	
	private void renderNametag(EntityOtherPlayerMP player, float partialTickTime) {
		String name = player.getName();
		int dist = (int) player.getDistanceToEntity(mc.thePlayer);
		name += " (" + dist + ")";
		
		RenderManager rm = mc.getRenderManager();
		FontRenderer fr = rm.getFontRenderer();
		
		float renderPosX = (float) (mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX)
				* partialTickTime);
		float renderPosY = (float) (mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY)
				* partialTickTime);
		float renderPosZ = (float) (mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ)
				* partialTickTime);

		float dx = (float)player.posX - renderPosX;
		float dy = (float)(player.posY + (player.isSneaking() ? 1.9 : 2.2)) - renderPosY;
		float dz = (float)player.posZ - renderPosZ;
		float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		float scale = (float) (-0.09+1/((1+Math.pow(1.3, 10-distance/20))*.6));
		
		GL11.glPushMatrix();
		GL11.glTranslatef(dx, dy, dz);
		GL11.glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scale, -scale, scale);
		GL11.glDisable(2896);
		GL11.glDepthMask(false);
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		
		int textWidth = fr.getStringWidth(name);
		int lineHeight = fr.FONT_HEIGHT;
		Tessellator t = Tessellator.getInstance();
		WorldRenderer wr = t.getWorldRenderer();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		wr.startDrawingQuads();
		int stringMiddle = textWidth / 2;
		wr.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
		wr.addVertex(-stringMiddle - 1, -1.0D, 0.0D);
		wr.addVertex(-stringMiddle - 1, lineHeight, 0.0D);
		wr.addVertex(stringMiddle + 1, lineHeight, 0.0D);
		wr.addVertex(stringMiddle + 1, -1, 0.0D);
		t.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int color = player.isSneaking() ? Color.GREEN.getRGB() : Color.WHITE.getRGB();
		fr.drawString(name, -textWidth / 2, 0, color);
		GL11.glDepthMask(true);
		GL11.glEnable(2929);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
