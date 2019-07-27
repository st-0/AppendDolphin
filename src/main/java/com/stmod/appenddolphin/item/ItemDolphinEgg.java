package com.stmod.appenddolphin.item;

import java.util.List;

import javax.annotation.Nullable;

import com.stmod.appenddolphin.AppendDolphin;
import com.stmod.appenddolphin.entity.EntityDolphin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemDolphinEgg extends Item {
	public ItemDolphinEgg() {
		setCreativeTab(CreativeTabs.MISC);
		setUnlocalizedName("dolphin_spawn_egg");
		setRegistryName("dolphin_spawn_egg");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemstack = player.getHeldItem(hand);

		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		} else if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
			return EnumActionResult.FAIL;
		} else {
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if (block == Blocks.MOB_SPAWNER) {
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if (tileentity instanceof TileEntityMobSpawner) {
					MobSpawnerBaseLogic mobspawnerbaselogic = ((TileEntityMobSpawner) tileentity).getSpawnerBaseLogic();
					mobspawnerbaselogic.setEntityId(new ResourceLocation(AppendDolphin.MODID, "dolphin"));
					tileentity.markDirty();
					worldIn.notifyBlockUpdate(pos, iblockstate, iblockstate, 3);

					if (!player.capabilities.isCreativeMode) {
						itemstack.shrink(1);
					}

					return EnumActionResult.SUCCESS;
				}
			}

			BlockPos blockpos = pos.offset(facing);
			double d0 = this.getYOffset(worldIn, blockpos);
			Entity entity = spawnDolphin(worldIn, blockpos.getX() + 0.5D, blockpos.getY() + d0, blockpos.getZ() + 0.5D);

			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}

			return EnumActionResult.SUCCESS;
		}
	}

	protected double getYOffset(World worldIn, BlockPos posIn) {
		AxisAlignedBB axisalignedbb = (new AxisAlignedBB(posIn)).expand(0.0D, -1.0D, 0.0D);
		List<AxisAlignedBB> list = worldIn.getCollisionBoxes((Entity) null, axisalignedbb);

		if (list.isEmpty()) {
			return 0.0D;
		} else {
			double d0 = axisalignedbb.minY;

			for (AxisAlignedBB axisalignedbb1 : list) {
				d0 = Math.max(axisalignedbb1.maxY, d0);
			}

			return d0 - posIn.getY();
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (worldIn.isRemote) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
		} else {
			RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

			if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos blockpos = raytraceresult.getBlockPos();

				if (!(worldIn.getBlockState(blockpos).getBlock() instanceof BlockLiquid)) {
					return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
				} else if (worldIn.isBlockModifiable(playerIn, blockpos) && playerIn.canPlayerEdit(blockpos, raytraceresult.sideHit, itemstack)) {
					Entity entity = spawnDolphin(worldIn, blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D);

					if (!playerIn.capabilities.isCreativeMode) {
						itemstack.shrink(1);
					}

					playerIn.addStat(StatList.getObjectUseStats(this));
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);

				} else {
					return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
				}
			} else {
				return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
			}
		}
	}

	@Nullable
	public static Entity spawnDolphin(World worldIn, double x, double y, double z) {
		// イルカをスポーン
		EntityDolphin dolphin = new EntityDolphin(worldIn);
		dolphin.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F), 0.0F);
		dolphin.rotationYawHead = dolphin.rotationYaw;
		dolphin.renderYawOffset = dolphin.rotationYaw;
		dolphin.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(dolphin)), (IEntityLivingData) null);
		worldIn.spawnEntity(dolphin);
		dolphin.playLivingSound();
		return dolphin;
	}
}