package com.biggestnerd.namescaling;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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

@Mod(modid="namescaling", name="Name Scaling", version="1.0")
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
				renderNametag(player, event.getPartialTicks());
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if(toggle.isKeyDown()) {
			enabled = !enabled;
			mc.thePlayer.addChatMessage(new TextComponentString(TextFormatting.DARK_AQUA + "[NameScaling]" + TextFormatting.GRAY + "Name scaling " + (enabled ? "enabled" : "disabled")));
		}
	}
	
	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
		if(event.getEntity() instanceof EntityOtherPlayerMP) {
			event.setCanceled(true);
		}
	}
	
	private void renderNametag(EntityOtherPlayerMP player, float partialTickTime) {
		String name = player.getName();
		int dist = (int) player.getDistanceToEntity(mc.thePlayer);
		name += " (" + dist + "m)";
		
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
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(dx, dy, dz);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-scale, -scale, scale);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		int textWidth = fr.getStringWidth(name);
		int lineHeight = fr.FONT_HEIGHT;
		Tessellator t = Tessellator.getInstance();
		VertexBuffer vb = t.getBuffer();
		GlStateManager.disableTexture2D();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		int stringMiddle = textWidth / 2;
		vb.pos(-stringMiddle - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.3F).endVertex();
		vb.pos(-stringMiddle - 1, lineHeight, 0.0D).color(0.0F, 0.0F, 0.0F, 0.3F).endVertex();
		vb.pos(stringMiddle + 1, lineHeight, 0.0D).color(0.0F, 0.0F, 0.0F, 0.3F).endVertex();
		vb.pos(stringMiddle + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.3F).endVertex();
		t.draw();
		GlStateManager.enableTexture2D();
		int color = player.isSneaking() ? -16711936 : 2852543;
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		fr.drawString(name, -textWidth / 2, 0, color);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}
}
