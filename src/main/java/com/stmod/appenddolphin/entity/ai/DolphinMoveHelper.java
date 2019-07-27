package com.stmod.appenddolphin.entity.ai;

import com.stmod.appenddolphin.entity.EntityDolphin;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

public class DolphinMoveHelper extends EntityMoveHelper {
	private final EntityDolphin dolphin;

	public DolphinMoveHelper(EntityDolphin dolphinIn) {
		super(dolphinIn);
		this.dolphin = dolphinIn;
	}

	@Override
	public void onUpdateMoveHelper() {
		if (this.dolphin.isInWater()) {
			this.dolphin.motionY += 0.005D;
		}

		if (this.action == DolphinMoveHelper.Action.MOVE_TO && !this.dolphin.getNavigator().noPath()) {
			double d0 = this.posX - this.dolphin.posX;
			double d1 = this.posY - this.dolphin.posY;
			double d2 = this.posZ - this.dolphin.posZ;
			double d3 = d0 * d0 + d1 * d1 + d2 * d2;
			if (d3 < 2.5000003E-7F) {
				this.entity.setMoveForward(0.0F);
			} else {
				float f = (float) (MathHelper.atan2(d2, d0) * (180F / (float) Math.PI)) - 90.0F;
				this.dolphin.rotationYaw = this.limitAngle(this.dolphin.rotationYaw, f, 10.0F);
				this.dolphin.renderYawOffset = this.dolphin.rotationYaw;
				this.dolphin.rotationYawHead = this.dolphin.rotationYaw;
				float f1 = (float) (this.speed
						* this.dolphin.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue());
				if (this.dolphin.isInWater()) {
					this.dolphin.setAIMoveSpeed(f1 * 0.02F);
					float f2 = -((float) (MathHelper.atan2(d1, MathHelper.sqrt(d0 * d0 + d2 * d2))
							* (180F / (float) Math.PI)));
					f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), -85.0F, 85.0F);
					this.dolphin.rotationPitch = this.limitAngle(this.dolphin.rotationPitch, f2, 5.0F);
					float f3 = MathHelper.cos(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
					float f4 = MathHelper.sin(this.dolphin.rotationPitch * ((float) Math.PI / 180F));
					this.dolphin.moveForward = f3 * f1;
					this.dolphin.moveVertical = -f4 * f1;
				} else {
					this.dolphin.setAIMoveSpeed(f1 * 0.1F);
				}

			}
		} else {
			this.dolphin.setAIMoveSpeed(0.0F);
			this.dolphin.setMoveStrafing(0.0F);
			this.dolphin.setMoveVertical(0.0F);
			this.dolphin.setMoveForward(0.0F);
		}
	}

	@Override
	public void setMoveTo(double x, double y, double z, double speedIn) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.speed = speedIn;
		if (this.action != EntityMoveHelper.Action.JUMPING) {
			this.action = EntityMoveHelper.Action.MOVE_TO;
		}
	}
}
