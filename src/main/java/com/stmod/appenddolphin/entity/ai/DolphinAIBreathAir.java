package com.stmod.appenddolphin.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DolphinAIBreathAir extends EntityAIBase {
	private final EntityCreature creature;

	public DolphinAIBreathAir(EntityCreature creatureIn) {
		this.creature = creatureIn;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		return this.creature.getAir() < 140;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public boolean isInterruptible() {
		return false;
	}

	@Override
	public void startExecuting() {
		this.moveToAir();
	}

	private void moveToAir() {
		Iterable<BlockPos.MutableBlockPos> iterable = BlockPos.getAllInBoxMutable(
				MathHelper.floor(this.creature.posX - 1.0D), MathHelper.floor(this.creature.posY),
				MathHelper.floor(this.creature.posZ - 1.0D), MathHelper.floor(this.creature.posX + 1.0D),
				MathHelper.floor(this.creature.posY + 8.0D), MathHelper.floor(this.creature.posZ + 1.0D));
		BlockPos blockpos = null;

		for (BlockPos blockpos1 : iterable) {
			if (this.canBreath(this.creature.world, blockpos1)) {
				blockpos = blockpos1;
				break;
			}
		}

		if (blockpos == null) {
			blockpos = new BlockPos(this.creature.posX, this.creature.posY + 8.0D, this.creature.posZ);
		}

		this.creature.getNavigator().tryMoveToXYZ(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ(), 1.0D);
	}

	@Override
	public void updateTask() {
		this.moveToAir();
		this.creature.moveRelative(this.creature.moveStrafing, this.creature.moveVertical, this.creature.moveForward, 0.02F);
		this.creature.move(MoverType.SELF, this.creature.motionX, this.creature.motionY, this.creature.motionZ);
	}

	private boolean canBreath(World worldIn, BlockPos posIn) {
		IBlockState iblockstate = worldIn.getBlockState(posIn);
		return (worldIn.getBlockState(posIn).getMaterial() == Material.AIR) || iblockstate.getBlock().isPassable(worldIn, posIn);
	}
}