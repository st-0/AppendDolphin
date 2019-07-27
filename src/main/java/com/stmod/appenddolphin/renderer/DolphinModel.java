package com.stmod.appenddolphin.renderer;

import com.stmod.appenddolphin.entity.EntityDolphin;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DolphinModel extends ModelBase {
	private final ModelRenderer body;
	private final ModelRenderer face;
	private final ModelRenderer finbase;
	private final ModelRenderer talefin;

	public DolphinModel() {
		this.textureWidth = 64;
		this.textureHeight = 64;
		float f = 18.0F;
		float f1 = -8.0F;
		this.body = new ModelRenderer(this, 22, 0);
		this.body.addBox(-4.0F, -7.0F, 0.0F, 8, 7, 13);
		this.body.setRotationPoint(0.0F, 22.0F, -5.0F);
		ModelRenderer modelrenderer = new ModelRenderer(this, 51, 0);
		modelrenderer.addBox(-0.5F, 0.0F, 8.0F, 1, 4, 5);
		modelrenderer.rotateAngleX = ((float) Math.PI / 3F);
		this.body.addChild(modelrenderer);
		ModelRenderer modelrenderer1 = new ModelRenderer(this, 48, 20);
		modelrenderer1.mirror = true;
		modelrenderer1.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
		modelrenderer1.setRotationPoint(2.0F, -2.0F, 4.0F);
		modelrenderer1.rotateAngleX = ((float) Math.PI / 3F);
		modelrenderer1.rotateAngleZ = 2.0943952F;
		this.body.addChild(modelrenderer1);
		ModelRenderer modelrenderer2 = new ModelRenderer(this, 48, 20);
		modelrenderer2.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
		modelrenderer2.setRotationPoint(-2.0F, -2.0F, 4.0F);
		modelrenderer2.rotateAngleX = ((float) Math.PI / 3F);
		modelrenderer2.rotateAngleZ = -2.0943952F;
		this.body.addChild(modelrenderer2);
		this.finbase = new ModelRenderer(this, 0, 19);
		this.finbase.addBox(-2.0F, -2.5F, 0.0F, 4, 5, 11);
		this.finbase.setRotationPoint(0.0F, -2.5F, 11.0F);
		this.finbase.rotateAngleX = -0.10471976F;
		this.body.addChild(this.finbase);
		this.talefin = new ModelRenderer(this, 19, 20);
		this.talefin.addBox(-5.0F, -0.5F, 0.0F, 10, 1, 6);
		this.talefin.setRotationPoint(0.0F, 0.0F, 9.0F);
		this.talefin.rotateAngleX = 0.0F;
		this.finbase.addChild(this.talefin);
		this.face = new ModelRenderer(this, 0, 0);
		this.face.addBox(-4.0F, -3.0F, -3.0F, 8, 7, 6);
		this.face.setRotationPoint(0.0F, -4.0F, -3.0F);
		ModelRenderer modelrenderer3 = new ModelRenderer(this, 0, 13);
		modelrenderer3.addBox(-1.0F, 2.0F, -7.0F, 2, 2, 4);
		this.face.addChild(modelrenderer3);
		this.body.addChild(this.face);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float bodyPitch, float scale) {
		// スケールの適用
		this.body.render(scale);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float bodyPitch, float scaleFactor, Entity entityIn) {
		// 動かす
		this.body.rotateAngleX = bodyPitch * ((float) Math.PI / 180F);
		this.body.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
		if (entityIn instanceof EntityDolphin) {
			EntityDolphin entitydolphin = (EntityDolphin) entityIn;
			if (entitydolphin.motionX != 0.0D || entitydolphin.motionZ != 0.0D) {
				this.body.rotateAngleX += -0.05F + -0.05F * MathHelper.cos(ageInTicks * 0.3F);
				this.finbase.rotateAngleX = -0.1F * MathHelper.cos(ageInTicks * 0.3F);
				this.talefin.rotateAngleX = -0.2F * MathHelper.cos(ageInTicks * 0.3F);
			}
		}
	}
}