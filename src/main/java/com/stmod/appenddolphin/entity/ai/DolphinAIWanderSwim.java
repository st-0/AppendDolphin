package com.stmod.appenddolphin.entity.ai;

import javax.annotation.Nullable;

import com.stmod.appenddolphin.entity.EntityDolphin;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DolphinAIWanderSwim extends EntityAIWander {
	// 水中をさまようAI

	public DolphinAIWanderSwim(EntityCreature creatureIn, double speedIn, int chance) {
		super(creatureIn, speedIn, chance);
	}

	@Override
	@Nullable
	protected Vec3d getPosition() {
		if (((EntityDolphin)this.entity).getOwner() != null && ((EntityDolphin)this.entity).isFreedom() && this.entity.getDistanceSq(((EntityDolphin) this.entity).getFreePosBlock()) >= 5.0D) {
			// 自由行動時は起点の座標に
			return ((EntityDolphin)this.entity).getFreePos();
		} else {
			// ランダムな座標へ
			Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);

			for (int i = 0; vec3d != null
					&& !this.entity.world.getBlockState(new BlockPos(vec3d)).getBlock().isPassable(this.entity.world, new BlockPos(vec3d))
					&& i++ < 10; vec3d = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7)) {
				;
			}
			return vec3d;
		}
	}

	public void startExecuting() {
		if(!this.entity.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed) && ((EntityDolphin)this.entity).getOwner() != null && ((EntityDolphin)this.entity).isFreedom()) {
			// 自由行動時に移動できないならテレポート
			int i = MathHelper.floor(this.x) - 2;
			int j = MathHelper.floor(this.z) - 2;
			int k = MathHelper.floor(this.y) - 2;

			for (int l = 0; l <= 4; ++l) {
				for (int i1 = 0; i1 <= 4; ++i1) {
					for (int yAdd = 0; yAdd <= 4; ++yAdd) {
						if (this.isTeleportFriendlyBlock(i, j, k, l, i1, yAdd)) {
							this.entity.setLocationAndAngles(i + l + 0.5F, k + yAdd, j + i1 + 0.5F,
									this.entity.rotationYaw, this.entity.rotationPitch);
							return;
						}
					}
				}
			}
		}
	}

	protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xAdd, int zAdd, int yAdd) {
		BlockPos blockpos = new BlockPos(x + xAdd, y - 1 + yAdd, z + zAdd);
		IBlockState iblockstate = this.entity.world.getBlockState(blockpos);
		return iblockstate.getMaterial() == Material.WATER && this.entity.world.isAirBlock(blockpos.up(1)) && iblockstate.canEntitySpawn(this.entity);
	}
}