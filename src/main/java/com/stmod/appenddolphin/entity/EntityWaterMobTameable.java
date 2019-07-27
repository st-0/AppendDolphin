package com.stmod.appenddolphin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityWaterMobTameable extends EntityTameable implements IAnimals {
	// 手懐け可能な水生MOBの標準設定
	private BlockPos homePosition = BlockPos.ORIGIN;
	private float maximumHomeDistance = -1.0F;

	public EntityWaterMobTameable(World worldIn) {
		super(worldIn);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean getCanSpawnHere() {
		return true;
	}

	@Override
	public boolean isNotColliding() {
		return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
	}

	@Override
	protected boolean canDespawn() {
		return true;
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		if (this.getAir() < this.getMaxAir()) {
			this.setAir(this.determineNextAir(this.getAir()));
		}
		int i = this.getAir();
		if (this.isEntityAlive() && !this.isInWater()) {
			--i;
			this.setAir(i);

			if (this.getAir() == -20) {
				this.setAir(0);
				this.attackEntityFrom(DamageSource.DROWN, 2.0F);
			}
		} else {
			this.setAir(300);
		}
	}

	protected int determineNextAir(int currentAir) {
		return this.getMaxAir();
	}

	public int getMaxAir() {
		return 4800;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		return false;
	}

	@Override
	public float getBlockPathWeight(BlockPos pos) {
		return 0.0F;
	}

	@Override
	protected void updateLeashedState() {
		super.updateLeashedState();

		if (this.getLeashed() && this.getLeashHolder() != null && this.getLeashHolder().world == this.world) {
			Entity entity = this.getLeashHolder();
			this.setHomePosAndDistance(new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ), 5);
			float f = this.getDistance(entity);

			this.onLeashDistance(f);

			if (f > 10.0F) {
				this.clearLeashed(true, true);
				this.tasks.disableControlFlag(1);
			} else if (f > 6.0F) {
				double d0 = (entity.posX - this.posX) / f;
				double d1 = (entity.posY - this.posY) / f;
				double d2 = (entity.posZ - this.posZ) / f;
				this.motionX += d0 * Math.abs(d0) * 0.4D;
				this.motionY += d1 * Math.abs(d1) * 0.4D;
				this.motionZ += d2 * Math.abs(d2) * 0.4D;
			} else {
				this.tasks.enableControlFlag(1);
				float f1 = 2.0F;
				Vec3d vec3d = (new Vec3d(entity.posX - this.posX, entity.posY - this.posY, entity.posZ - this.posZ))
						.normalize().scale(Math.max(f - 2.0F, 0.0F));
				this.getNavigator().tryMoveToXYZ(this.posX + vec3d.x, this.posY + vec3d.y, this.posZ + vec3d.z,
						this.followLeashSpeed());
			}
		}
	}
}