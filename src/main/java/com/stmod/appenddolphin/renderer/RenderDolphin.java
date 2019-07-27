package com.stmod.appenddolphin.renderer;

import com.stmod.appenddolphin.AppendDolphin;
import com.stmod.appenddolphin.entity.EntityDolphin;
import com.stmod.appenddolphin.renderer.layer.LayerDolphinCarriedItem;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDolphin extends RenderLiving<EntityDolphin> {
	private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation(AppendDolphin.MODID + ":textures/entity/dolphin.png");

	public RenderDolphin(RenderManager renderManagerIn) {
		// 所持アイテムのレイヤーを追加
		super(renderManagerIn, new DolphinModel(), 0.7F);
		this.addLayer(new LayerDolphinCarriedItem(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDolphin entity) {
		return DOLPHIN_LOCATION;
	}

	@Override
	protected void preRenderCallback(EntityDolphin entitylivingbaseIn, float partialTickTime) {
		float f = 1.0F;
		GlStateManager.scale(f, f, f);
	}
}