package com.stmod.appenddolphin.entity.ai;

import com.stmod.appenddolphin.entity.EntityDolphin;
import com.stmod.appenddolphin.init.DolphinSoundEvents;

import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DolphinAIJump extends EntityAIBase {
	private static final int[] searchSpace = new int[] { 0, 1, 4, 5, 6, 7 };
	private final EntityDolphin dolphin;
	private final int randomRange;
	private boolean isInWater;

	public DolphinAIJump(EntityDolphin dolphinIn, int randIn) {
		this.dolphin = dolphinIn;
		this.randomRange = randIn;
		this.setMutexBits(5);
	}

	@Override
	public boolean shouldExecute() {
		if (this.dolphin.getRNG().nextInt(this.randomRange) != 0) {
			return false;
		} else {
			EnumFacing enumfacing = this.dolphin.getAdjustedHorizontalFacing();
			int i = enumfacing.getFrontOffsetX();
			int j = enumfacing.getFrontOffsetZ();
			BlockPos blockpos = new BlockPos(this.dolphin);

			for (int k : searchSpace) {
				if (!this.isWater(blockpos, i, j, k) || !this.isAirUp(blockpos, i, j, k)) {
					return false;
				}
			}

			return true;
		}
	}

	private boolean isWater(BlockPos posIn, int x, int z, int scaleIn) {
		BlockPos blockpos = posIn.add(x * scaleIn, 0, z * scaleIn);
		return (this.dolphin.world.getBlockState(blockpos).getMaterial() == Material.WATER)
				&& !this.dolphin.world.getBlockState(blockpos).getMaterial().blocksMovement();
	}

	private boolean isAirUp(BlockPos posIn, int x, int z, int scaleIn) {
		return (this.dolphin.world.getBlockState(posIn.add(x * scaleIn, 1, z * scaleIn)).getMaterial() == Material.AIR)
				&& (this.dolphin.world.getBlockState(posIn.add(x * scaleIn, 2, z * scaleIn)).getMaterial() == Material.AIR);
	}

	@Override
	public boolean shouldContinueExecuting() {
		return (!(this.dolphin.motionY * this.dolphin.motionY < 0.03F) || this.dolphin.rotationPitch == 0.0F
				|| !(Math.abs(this.dolphin.rotationPitch) < 10.0F) || !this.dolphin.isInWater())
				&& !this.dolphin.onGround;
	}

	@Override
	public boolean isInterruptible() {
		return false;
	}

	@Override
	public void startExecuting() {
		EnumFacing enumfacing = this.dolphin.getAdjustedHorizontalFacing();
		this.dolphin.motionX += enumfacing.getFrontOffsetX() * 0.6D;
		this.dolphin.motionY += 0.7D;
		this.dolphin.motionZ += enumfacing.getFrontOffsetZ() * 0.6D;
		this.dolphin.getNavigator().clearPath();
	}

	@Override
	public void resetTask() {
		this.dolphin.rotationPitch = 0.0F;
	}

	@Override
	public void updateTask() {
		boolean flag = this.isInWater;
		if (!flag) {
			Material mat = this.dolphin.world.getBlockState(new BlockPos(this.dolphin)).getMaterial();
			this.isInWater = (mat == Material.WATER);
		}

		if (this.isInWater && !flag) {
			this.dolphin.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_JUMP, 1.0F, 1.0F);
		}

		if (this.dolphin.motionY * this.dolphin.motionY < 0.03F && this.dolphin.rotationPitch != 0.0F) {
			this.dolphin.rotationPitch = this.newRotPitch(this.dolphin.rotationPitch, 0.0F, 0.2F);
		} else {
			double d2 = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionY * this.dolphin.motionY + this.dolphin.motionZ * this.dolphin.motionZ);
			double d0 = Math.sqrt(this.dolphin.motionX * this.dolphin.motionX + this.dolphin.motionZ * this.dolphin.motionZ);
			double d1 = Math.signum(-this.dolphin.motionY) * Math.acos(d0 / d2) * (180F / (float) Math.PI);
			this.dolphin.rotationPitch = (float) d1;
		}

	}

	protected float newRotPitch(float rot, float offsetIn, float scaleIn) {
		float f;
		// fを-180～180におさめる
		for (f = offsetIn - rot; f < -180.0F; f += 360.0F) {
			;
		}

		while (f >= 180.0F) {
			f -= 360.0F;
		}

		return rot + scaleIn * f;
	}
}