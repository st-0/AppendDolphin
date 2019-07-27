package com.stmod.appenddolphin.entity.ai;

import com.stmod.appenddolphin.entity.EntityDolphin;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DolphinAIFollowOwner extends EntityAIBase {
	private final EntityTameable tameable;
	private EntityLivingBase owner;
	World world;
	private final double followSpeed;
	private final PathNavigate petPathfinder;
	private int timeToRecalcPath;
	float maxDist;
	float minDist;
	private float oldWaterCost;

	public DolphinAIFollowOwner(EntityTameable tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
		this.tameable = tameableIn;
		this.world = tameableIn.world;
		this.followSpeed = followSpeedIn;
		this.petPathfinder = tameableIn.getNavigator();
		this.minDist = minDistIn;
		this.maxDist = maxDistIn;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase entitylivingbase = this.tameable.getOwner();

		if (entitylivingbase == null || ((EntityDolphin)this.tameable).isFreedom()) {
			return false;
		} else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer) entitylivingbase).isSpectator()) {
			return false;
		} else if (this.tameable.isSitting()) {
			return false;
		} else if (this.tameable.getDistanceSq(entitylivingbase) < this.minDist * this.minDist) {
			return false;
		} else {
			this.owner = entitylivingbase;
			return true;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.petPathfinder.noPath() && this.tameable.getDistanceSq(this.owner) > this.maxDist * this.maxDist;
	}

	@Override
	public void startExecuting() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.tameable.getPathPriority(PathNodeType.WATER);
		this.tameable.setPathPriority(PathNodeType.WATER, 0.0F);
	}

	@Override
	public void resetTask() {
		this.owner = null;
		this.petPathfinder.clearPath();
		this.tameable.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
	}

	@Override
	public void updateTask() {
		this.tameable.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, this.tameable.getVerticalFaceSpeed());

		if (!this.tameable.isSitting()) {
			if (--this.timeToRecalcPath <= 0) {
				this.timeToRecalcPath = 10;

				if (!this.petPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed)) {
					if (!this.tameable.getLeashed() && !this.tameable.isRiding()) {
						if (this.tameable.getDistanceSq(this.owner) >= 144.0D) {
							int i = MathHelper.floor(this.owner.posX) - 2;
							int j = MathHelper.floor(this.owner.posZ) - 2;
							int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);

							for (int l = 0; l <= 4; ++l) {
								for (int i1 = 0; i1 <= 4; ++i1) {
									if ((l < 1 || i1 < 1 || l > 3 || i1 > 3)
											&& this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
										this.tameable.setLocationAndAngles(i + l + 0.5F, k, j + i1 + 0.5F,
												this.tameable.rotationYaw, this.tameable.rotationPitch);
										this.petPathfinder.clearPath();
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_) {
		BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		// 空気
		//  水  な場所ならテレポート
		//  水
		return iblockstate.getMaterial() == Material.WATER && iblockstate.canEntitySpawn(this.tameable)
				&& (this.world.getBlockState(blockpos.up()).getMaterial() == Material.WATER && this.world.isAirBlock(blockpos.up(2)));
	}
}