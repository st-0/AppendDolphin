package com.stmod.appenddolphin.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateDolphin extends PathNavigateSwimmer {
	public PathNavigateDolphin(EntityLiving entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
	}

	@Override
	protected PathFinder getPathFinder() {
		this.nodeProcessor = new DolphinNodeProcessor();
		return new PathFinder(this.nodeProcessor);
	}

	@Override
	protected boolean canNavigate() {
		// 水中でも陸上でも
		return true;
	}

	@Override
	public void onUpdateNavigation() {
		++this.totalTicks;
		if (this.tryUpdatePath) {
			this.updatePath();
		}

		if (!this.noPath()) {
			if (this.canNavigate()) {
				this.pathFollow();
			} else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
				Vec3d vec3d = this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex());
				if (MathHelper.floor(this.entity.posX) == MathHelper.floor(vec3d.x)
						&& MathHelper.floor(this.entity.posY) == MathHelper.floor(vec3d.y)
						&& MathHelper.floor(this.entity.posZ) == MathHelper.floor(vec3d.z)) {
					this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
				}
			}

			this.debugPathFinding();
			if (!this.noPath()) {
				Vec3d vec3d1 = this.currentPath.getPosition(this.entity);
				this.entity.getMoveHelper().setMoveTo(vec3d1.x, vec3d1.y, vec3d1.z, this.speed);
			}
		}
	}

	@Override
	protected void pathFollow() {
		if (this.currentPath != null) {
			Vec3d vec3d = this.getEntityPosition();
			float f = this.entity.width > 0.75F ? this.entity.width / 2.0F : 0.75F - this.entity.width / 2.0F;
			if (MathHelper.abs((float) this.entity.motionX) > 0.2D || MathHelper.abs((float) this.entity.motionZ) > 0.2D) {
				f *= MathHelper.sqrt(this.entity.motionX * this.entity.motionX + this.entity.motionY * this.entity.motionY + this.entity.motionZ * this.entity.motionZ) * 6.0F;
			}

			int i = 6;
			Vec3d vec3d1 = this.currentPath.getCurrentPos();
			if (MathHelper.abs((float) (this.entity.posX - (vec3d1.x + 0.5D))) < f
					&& MathHelper.abs((float) (this.entity.posZ - (vec3d1.z + 0.5D))) < f
					&& Math.abs(this.entity.posY - vec3d1.y) < f * 2.0F) {
				this.currentPath.incrementPathIndex();
			}

			for (int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); j > this.currentPath.getCurrentPathIndex(); --j) {
				vec3d1 = this.currentPath.getVectorFromIndex(this.entity, j);
				if (!(vec3d1.squareDistanceTo(vec3d) > 36.0D) && this.isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0)) {
					this.currentPath.setCurrentPathIndex(j);
					break;
				}
			}

			this.checkForStuck(vec3d);
		}
	}
}
