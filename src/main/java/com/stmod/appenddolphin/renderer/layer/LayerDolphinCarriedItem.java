package com.stmod.appenddolphin.renderer.layer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerDolphinCarriedItem implements LayerRenderer<EntityLivingBase> {
	protected final RenderLivingBase<?> renderer;
	private final ItemRenderer itemRenderer;

	public LayerDolphinCarriedItem(RenderLivingBase<?> renderIn) {
		this.renderer = renderIn;
		this.itemRenderer = Minecraft.getMinecraft().getItemRenderer();
	}

	@Override
	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		boolean flag = entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT;
		ItemStack itemstack = flag ? entitylivingbaseIn.getHeldItemOffhand() : entitylivingbaseIn.getHeldItemMainhand();
		ItemStack itemstack1 = flag ? entitylivingbaseIn.getHeldItemMainhand() : entitylivingbaseIn.getHeldItemOffhand();
		if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
			this.setItemMatrix(entitylivingbaseIn, itemstack1);
		}
	}

	private void setItemMatrix(EntityLivingBase livingIn, ItemStack stackIn) {
		// アイテムの表示設定
		if (!stackIn.isEmpty()) {
			if (!stackIn.isEmpty()) {
				Item item = stackIn.getItem();
				Block block = Block.getBlockFromItem(item);
				GlStateManager.pushMatrix();
				boolean flag = block.getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
				if (flag) {
					GlStateManager.depthMask(false);
				}

				float f = 1.0F;
				float f1 = -1.0F;
				float f2 = MathHelper.abs(livingIn.rotationPitch) / 60.0F;
				if (livingIn.rotationPitch < 0.0F) {
					GlStateManager.translate(0.0F, 1.0F - f2 * 0.5F, -1.0F + f2 * 0.5F);
				} else {
					GlStateManager.translate(0.0F, 1.0F + f2 * 0.8F, -1.0F + f2 * 0.2F);
				}

				this.itemRenderer.renderItem(livingIn, stackIn, ItemCameraTransforms.TransformType.GROUND);
				if (flag) {
					GlStateManager.depthMask(true);
				}

				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}