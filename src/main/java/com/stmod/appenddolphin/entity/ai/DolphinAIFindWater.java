package com.stmod.appenddolphin.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class DolphinAIFindWater extends EntityAIBase {
	private final EntityCreature creature;

	public DolphinAIFindWater(EntityCreature creatureIn) {
		this.creature = creatureIn;
	}

	@Override
	public boolean shouldExecute() {
		return this.creature.onGround && !(this.creature.world.getBlockState(new BlockPos(this.creature)).getMaterial() == Material.WATER);
	}

	@Override
	public void startExecuting() {
		BlockPos blockpos = null;

		for (BlockPos blockpos1 : BlockPos.getAllInBoxMutable(
				MathHelper.floor(this.creature.posX - 2.0D), MathHelper.floor(this.creature.posY - 2.0D),
				MathHelper.floor(this.creature.posZ - 2.0D), MathHelper.floor(this.creature.posX + 2.0D),
				MathHelper.floor(this.creature.posY), MathHelper.floor(this.creature.posZ + 2.0D))) {
			if (this.creature.world.getBlockState(blockpos1).getMaterial() == Material.WATER) {
				blockpos = blockpos1;
				break;
			}
		}

		if (blockpos != null) {
			this.creature.getMoveHelper().setMoveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1.0D);
		}

	}
}