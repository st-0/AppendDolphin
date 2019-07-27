package com.stmod.appenddolphin.entity.ai;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class DolphinAIFollowBoat extends EntityAIBase {
	private int timeToRecalcPath;
	private final EntityCreature creature;
	private EntityLivingBase living;
	private BoatGoals goal;

	public DolphinAIFollowBoat(EntityCreature creatureIn) {
		this.creature = creatureIn;
	}

	@Override
	public boolean shouldExecute() {
		List<EntityBoat> list = this.creature.world.getEntitiesWithinAABB(EntityBoat.class, this.creature.getEntityBoundingBox().grow(5.0D));
		boolean flag = false;

		for (EntityBoat entityboat : list) {
			if (entityboat.getControllingPassenger() != null && (MathHelper.abs(((EntityLivingBase)entityboat.getControllingPassenger()).moveStrafing) > 0.0F
					|| MathHelper.abs(((EntityLivingBase) entityboat.getControllingPassenger()).moveForward) > 0.0F)) {
				flag = true;
				break;
			}
		}

		return this.living != null && (MathHelper.abs(this.living.moveStrafing) > 0.0F
				|| MathHelper.abs(this.living.moveForward) > 0.0F) || flag;
	}

	@Override
	public boolean isInterruptible() {
		return true;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.living != null && this.living.isPassenger(living)
				&& (MathHelper.abs(this.living.moveStrafing) > 0.0F || MathHelper.abs(this.living.moveForward) > 0.0F);
	}

	@Override
	public void startExecuting() {
		for (EntityBoat entityboat : this.creature.world.getEntitiesWithinAABB(EntityBoat.class, this.creature.getEntityBoundingBox().grow(5.0D))) {
			if (entityboat.getControllingPassenger() != null && entityboat.getControllingPassenger() instanceof EntityLivingBase) {
				this.living = (EntityLivingBase) entityboat.getControllingPassenger();
				break;
			}
		}

		this.timeToRecalcPath = 0;
		this.goal = BoatGoals.GO_TO_BOAT;
	}

	@Override
	public void resetTask() {
		this.living = null;
	}

	@Override
	public void updateTask() {
		boolean flag = MathHelper.abs(this.living.moveStrafing) > 0.0F
				|| MathHelper.abs(this.living.moveForward) > 0.0F;
		float f = this.goal == BoatGoals.GO_IN_BOAT_DIRECTION ? (flag ? 0.17999999F : 0.0F) : 0.135F;
		this.creature.moveRelative(this.creature.moveStrafing, this.creature.moveVertical, this.creature.moveForward, f);
		this.creature.move(MoverType.SELF, this.creature.motionX, this.creature.motionY, this.creature.motionZ);
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
			if (this.goal == BoatGoals.GO_TO_BOAT) {
				BlockPos blockpos = (new BlockPos(this.living)).offset(this.living.getHorizontalFacing().getOpposite());
				blockpos = blockpos.add(0, -1, 0);
				this.creature.getNavigator().tryMoveToXYZ(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1.0D);
				if (this.creature.getDistance(this.living) < 4.0F) {
					this.timeToRecalcPath = 0;
					this.goal = BoatGoals.GO_IN_BOAT_DIRECTION;
				}
			} else if (this.goal == BoatGoals.GO_IN_BOAT_DIRECTION) {
				EnumFacing enumfacing = this.living.getAdjustedHorizontalFacing();
				BlockPos blockpos1 = (new BlockPos(this.living)).offset(enumfacing, 10);
				this.creature.getNavigator().tryMoveToXYZ(blockpos1.getX(), blockpos1.getY() - 1, blockpos1.getZ(), 1.0D);
				if (this.creature.getDistance(this.living) > 12.0F) {
					this.timeToRecalcPath = 0;
					this.goal = BoatGoals.GO_TO_BOAT;
				}
			}
		}
	}
}