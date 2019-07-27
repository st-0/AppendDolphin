package com.stmod.appenddolphin.pathfinding;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class DolphinNodeProcessor extends SwimNodeProcessor {

	@Override
	public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
		return this.getPathNodeType(blockaccessIn, x, y, z);
	}

	@Override
	public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z) {
		BlockPos blockpos = new BlockPos(x, y, z);
		IBlockState iblockstate = blockaccessIn.getBlockState(blockpos);
		return iblockstate.getMaterial() == Material.WATER && iblockstate.getBlock().isPassable(blockaccessIn, blockpos)
				? PathNodeType.WATER
				: PathNodeType.BLOCKED;
	}

	@Override
	protected PathPoint openPoint(int x, int y, int z) {
		PathPoint pathpoint = super.openPoint(x, y, z);
		PathNodeType pathnodetype = this.getPathNodeType(this.entity.world, x, y, z);
		float f = this.entity.getPathPriority(pathnodetype);
		if (f >= 0.0F) {
			pathpoint.nodeType = pathnodetype;
			pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
			if (this.blockaccess.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.AIR) {
				pathpoint.costMalus += 8.0F;
			}
		}

		return pathnodetype == PathNodeType.OPEN ? pathpoint : pathpoint;
	}

	@Override
	public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
		int i = 0;

		for (EnumFacing enumfacing : EnumFacing.values()) {
			PathPoint pathpoint = this.getWaterNode(currentPoint.x + enumfacing.getFrontOffsetX(),currentPoint.y + enumfacing.getFrontOffsetY(), currentPoint.z + enumfacing.getFrontOffsetZ());
			if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance) {
				pathOptions[i++] = pathpoint;
			}
		}

		return i;
	}

	@Nullable
	private PathPoint getWaterNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
		PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
		return pathnodetype == PathNodeType.WATER ? this.openPoint(p_186328_1_, p_186328_2_, p_186328_3_) : null;
	}

	private PathNodeType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
		// これがオーバーライドできないからちょっとコード長くなってる
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int i = p_186327_1_; i < p_186327_1_ + this.entitySizeX; ++i) {
			for (int j = p_186327_2_; j < p_186327_2_ + this.entitySizeY; ++j) {
				for (int k = p_186327_3_; k < p_186327_3_ + this.entitySizeZ; ++k) {
					IBlockState iblockstate = this.blockaccess.getBlockState(blockpos$mutableblockpos.setPos(i, j, k));
					if (iblockstate.getMaterial() != Material.WATER) {
						return PathNodeType.BLOCKED;
					}
				}
			}
		}

		IBlockState iblockstate1 = this.blockaccess.getBlockState(blockpos$mutableblockpos);
		if (iblockstate1.getBlock().isPassable(this.blockaccess, blockpos$mutableblockpos)) {
			return PathNodeType.WATER;
		} else {
			return PathNodeType.BLOCKED;
		}
	}
}
