package com.stmod.appenddolphin.entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.stmod.appenddolphin.AppendDolphin;
import com.stmod.appenddolphin.entity.ai.DolphinAIBreathAir;
import com.stmod.appenddolphin.entity.ai.DolphinAIFindWater;
import com.stmod.appenddolphin.entity.ai.DolphinAIFollowBoat;
import com.stmod.appenddolphin.entity.ai.DolphinAIFollowOwner;
import com.stmod.appenddolphin.entity.ai.DolphinAIJump;
import com.stmod.appenddolphin.entity.ai.DolphinAITempt;
import com.stmod.appenddolphin.entity.ai.DolphinAIWanderSwim;
import com.stmod.appenddolphin.entity.ai.DolphinLookHelper;
import com.stmod.appenddolphin.entity.ai.DolphinMoveHelper;
import com.stmod.appenddolphin.init.DolphinSoundEvents;
import com.stmod.appenddolphin.particle.SpawnDolphinParticle;
import com.stmod.appenddolphin.pathfinding.PathNavigateDolphin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityDolphin extends EntityWaterMobTameable {
	private static final DataParameter<Float> DATA_HEALTH_ID = EntityDataManager.<Float> createKey(EntityDolphin.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> MOISTNESS = EntityDataManager.<Integer> createKey(EntityDolphin.class, DataSerializers.VARINT);
	public static final Predicate<EntityItem> ITEM_SELECTOR = (t) -> {
		return !t.cannotPickup() && t.isEntityAlive() && t.isInWater();
	};
	private static boolean isFreedom;
	private static Vec3d freePos;

	public EntityDolphin(World worldIn) {
		super(worldIn);
		this.setSize(0.9F, 0.6F);
		this.moveHelper = new DolphinMoveHelper(this);
		lookHelperUpdate();
		this.setCanPickUpLoot(true);
        this.setTamed(false);
	}

	public void lookHelperUpdate(){
		// 強引に書き換え
		try {
			Class clazz = EntityLiving.class;
			Field field = clazz.getDeclaredField(AppendDolphin.lookHelper);
			field.setAccessible(true);
			field.set(this, new DolphinLookHelper(this, 10));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		// 初期化
		this.setAir(this.getMaxAir());
		this.rotationPitch = 0.0F;
		return super.onInitialSpawn(difficulty, livingdata);
	}

	@Override
	public boolean canBreatheUnderwater() {
		// 哺乳類やぞ
		return false;
	}

	@Override
	protected void updateAITasks() {
		// 体力を更新
		this.dataManager.set(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
		this.dataManager.register(MOISTNESS, 2400);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		// 水分量、自由行動、自由行動の起点を書き込み
		super.writeEntityToNBT(compound);
		compound.setInteger("Moistness", this.getMoistness());
        compound.setBoolean("Freedom", this.isFreedom());
        compound.setTag("Freepos", this.newDoubleNBTList(this.getFreePos().x, this.getFreePos().y, this.getFreePos().z));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		// 水分量、自由行動、自由行動の起点を読み込み
		super.readEntityFromNBT(compound);
		this.setMoistness(compound.getInteger("Moistness"));
		this.setFreedom(compound.getBoolean("Freedom"));
        NBTTagList nbttaglist = compound.getTagList("Freepos", 6);
        this.setFreePos(nbttaglist.getDoubleAt(0), nbttaglist.getDoubleAt(1), nbttaglist.getDoubleAt(2));
	}

	@Override
	protected void initEntityAI() {
		// AIの設定
		this.tasks.addTask(0, new DolphinAIBreathAir(this));
		this.tasks.addTask(0, new DolphinAIFindWater(this));
		this.tasks.addTask(1, new DolphinAITempt(this, 1.0D, Items.FISH, false));
		this.tasks.addTask(2, new DolphinAIWanderSwim(this, 1.0D, 10));
		this.tasks.addTask(3, new DolphinAIJump(this, 10));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.2F, true));
        this.tasks.addTask(5, new DolphinAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
		this.tasks.addTask(6, new EntityDolphin.AIPlayWithItems());
		this.tasks.addTask(6, new DolphinAIFollowBoat(this));
		this.tasks.addTask(7, new EntityAIAvoidEntity<>(this, EntityGuardian.class, 8.0F, 1.0D, 1.0D));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
	}

	@Override
	protected boolean canDespawn() {
		// 手懐けたイルカはデスポーンしない
		return !this.isTamed();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		if (this.isTamed()) {
			// 手懐けたイルカの体力はオオカミと同じ20に設定
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		} else {
			// 野良イルカは体力10
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		}
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.2F);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	@Override
	public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
		// 敵味方判別
		if (isEnemy(entitylivingbaseIn)) {
			super.setAttackTarget(entitylivingbaseIn);
		}
	}

	@Override
	protected PathNavigate createNavigator(World worldIn) {
		// イルカ用の設定
		return new PathNavigateDolphin(this, worldIn);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		// 攻撃食らったときの処理
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
		if (flag) {
			this.applyEnchantments(this, entityIn);
			this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_ATTACK, 1.0F, 1.0F);
		}

		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else {
			Entity entity = source.getTrueSource();

			if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
				amount = (amount + 1.0F) / 2.0F;
			}

			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public void setTamed(boolean tamed) {
		// 手懐けられたときのステータス
		super.setTamed(tamed);
		if (tamed) {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		} else {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		}
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public float getEyeHeight() {
		// 視点の高さ
		return 0.3F;
	}

	@Override
	public int getVerticalFaceSpeed() {
		// 垂直方向の回転速度
		return 1;
	}

	@Override
	public int getHorizontalFaceSpeed() {
		// 水平方向の回転速度
		return 1;
	}

	@Override
	protected boolean canBeRidden(Entity entityIn) {
		// 乗り物に乗れないように設定
		return false;
	}

	@Override
	protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
		// アイテム回収用
		if (this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()) {
			ItemStack itemstack = itemEntity.getItem();
			if (this.canEquipItem(itemstack)) {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack);
				this.inventoryHandsDropChances[EntityEquipmentSlot.MAINHAND.getIndex()] = 2.0F;
				this.onItemPickup(itemEntity, itemstack.getCount());
				itemEntity.setDead();
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.isAIDisabled()) {
			if (this.isWet()) {
				this.setMoistness(2400);
			} else {
				this.setMoistness(this.getMoistness() - 1);
				if (this.getMoistness() <= 0) {
					this.attackEntityFrom(new DamageSource("dryout"), 1.0F);
				}

				if (this.onGround) {
					this.motionY += 0.5D;
					this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
					this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
					this.rotationYaw = this.rand.nextFloat() * 360.0F;
					this.onGround = false;
					this.isAirBorne = true;
				}
			}

			if (this.world.isRemote && this.isInWater() && this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ > 0.03D) {
				Vec3d vec3d = this.getLook(0.0F);
				float f = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * 0.3F;
				float f1 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)) * 0.3F;
				float f2 = 1.2F - this.rand.nextFloat() * 0.7F;

				for (int i = 0; i < 2; ++i) {
					SpawnDolphinParticle.spawnParticle(this.posX - vec3d.x * f2 + f, this.posY - vec3d.y, this.posZ - vec3d.z * f2 + f1, 0.0D, 0.0D, 0.0D);
					SpawnDolphinParticle.spawnParticle(this.posX - vec3d.x * f2 - f, this.posY - vec3d.y, this.posZ - vec3d.z * f2 - f1, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (getOwner() != null) {
			// 飼育個体
			if(!itemstack.isEmpty() && itemstack.getItem() == Items.FISH) {
				// 持ち物が魚以外なら魚をあげる
				if (!this.world.isRemote) {
					this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_EAT, 1.0F, 1.0F);
				}
				if (!player.capabilities.isCreativeMode) {
					itemstack.shrink(1);
				}
				if (this.dataManager.get(DATA_HEALTH_ID).floatValue() < 20.0F) {
					// 体力が満タンでないなら回復
					this.heal(2);
				} else {
					// 体力が満タンならハートを出す
					if (!this.world.isRemote) {
						this.world.setEntityState(this, (byte) 7);
					}
				}
				return true;
			} else {
				// 持ち物が魚以外なら自由行動のオンオフ
				if (this.isOwner(player)) {
	                if (!this.world.isRemote) {
	                	this.setFreedom(!this.isFreedom());
		                this.setFreePos(this.getPositionVector());
	                	this.isJumping = false;
	                	this.navigator.clearPath();
	                	this.setAttackTarget((EntityLivingBase)null);
	                	this.world.setEntityState(this, this.isFreedom() ? (byte)6 : (byte)7);
	                }
				}
				return true;
			}
		} else if(!itemstack.isEmpty() && itemstack.getItem() == Items.FISH) {
			// 野良イルカに魚をあげる
			if (!this.world.isRemote) {
				if (this.rand.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
					// 手懐け成功
					this.setTamedBy(player);
					this.navigator.clearPath();
					this.setAttackTarget((EntityLivingBase) null);
					this.setHealth(20.0F);
					this.setFreedom(false);
					this.world.setEntityState(this, (byte)7);
				} else {
					// 手懐け失敗
					this.world.setEntityState(this, (byte)6);
				}
			}
			return true;
		}
		return super.processInteract(player, hand);
	}

	@Nullable
	public EntityItem throwItem(ItemStack stack) {
		if (stack.isEmpty()) {
			// アイテム未所持なら何もしない
			return null;
		} else {
			// アイテムを投げる
			double d0 = this.posY - 0.3F + this.getEyeHeight();
			EntityItem entityitem = new EntityItem(this.world, this.posX, d0, this.posZ, stack);
			entityitem.setPickupDelay(40);
			entityitem.setThrower(this.getUniqueID().toString());
			float f = 0.3F;
			entityitem.motionX = -MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)) * MathHelper.cos(this.rotationPitch * ((float) Math.PI / 180F)) * f;
			entityitem.motionY = MathHelper.sin(this.rotationPitch * ((float) Math.PI / 180F)) * f * 1.5F;
			entityitem.motionZ = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F)) * MathHelper.cos(this.rotationPitch * ((float) Math.PI / 180F)) * f;
			float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
			f = 0.02F * this.rand.nextFloat();
			entityitem.motionX += MathHelper.cos(f1) * f;
			entityitem.motionZ += MathHelper.sin(f1) * f;
			this.world.spawnEntity(entityitem);
			return entityitem;
		}
	}

	@Override
	public boolean getCanSpawnHere() {
		// あまり深くない海にスポーン
		return this.posY > 45.0D && this.posY < this.world.getSeaLevel()
				&& this.world.getBiome(new BlockPos(this)) != Biomes.OCEAN
				|| this.world.getBiome(new BlockPos(this)) != Biomes.DEEP_OCEAN
				&& super.getCanSpawnHere();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		// 攻撃されたときの声
		return DolphinSoundEvents.ENTITY_DOLPHIN_HURT;
	}

	@Override
	@Nullable
	protected SoundEvent getDeathSound() {
		// やられたときの声
		return DolphinSoundEvents.ENTITY_DOLPHIN_DEATH;
	}

	@Override
	@Nullable
	protected SoundEvent getAmbientSound() {
		// 通常時の声
		return this.isInWater() ? DolphinSoundEvents.ENTITY_DOLPHIN_AMBIENT_WATER : DolphinSoundEvents.ENTITY_DOLPHIN_AMBIENT;
	}

	@Override
	protected SoundEvent getSplashSound() {
		// ブリーチングの音
		return DolphinSoundEvents.ENTITY_DOLPHIN_SPLASH;
	}

	@Override
	protected SoundEvent getSwimSound() {
		// 泳いでいるときの音
		return DolphinSoundEvents.ENTITY_DOLPHIN_SWIM;
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		if (this.isServerWorld() && this.isInWater()) {
			this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.9F;
			this.motionY *= 0.9F;
			this.motionZ *= 0.9F;
			if (this.getAttackTarget() == null) {
				this.motionY -= 0.005D;
			}
		} else {
			super.travel(strafe, vertical, forward);
		}
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		// リードを付けられる
		return true;
	}

	@Override
	public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
		// ターゲットが敵であるなら攻撃
		return isEnemy(target);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		// 繁殖用の設定（機能してない）
		EntityDolphin entitydolphin = new EntityDolphin(this.world);
		UUID uuid = this.getOwnerId();

		if (uuid != null) {
			entitydolphin.setOwnerId(uuid);
			entitydolphin.setTamed(true);
		}

		return entitydolphin;
	}

	public boolean isEnemy(Entity pEntity) {
		if (pEntity == null || pEntity == this.getOwner() || !pEntity.isInWater()) {
			// 主人またはMOBが陸上または敵なし
			return false;
		}
		if (pEntity instanceof EntityCreeper && ((EntityCreature)pEntity).getAttackTarget() != this.getOwner()) {
			// クリーパーとその拡張MOBはヤバイから主人をターゲットにしてから攻撃
			return false;
		}
		if (pEntity instanceof EntityMob) {
			// モンスター
			return true;
		}
		if (pEntity instanceof EntityCreature) {
			// 敵の攻撃対象が味方なら敵とみなす
			Entity et = ((EntityCreature) pEntity).getAttackTarget();
			if (et == this.getOwner()) {
				// 主人
				return true;
			}
			if (et == this) {
				// 自分
				return true;
			}
			if (et instanceof EntityTameable) {
				// 主人の手懐けたMOB
				if (((EntityTameable) et).getOwner() == this.getOwner()) {
					return true;
				}
			}
		}
		return false;
	}

	// セッターゲッター
	public int getMoistness() {
		return this.dataManager.get(MOISTNESS);
	}

	public void setMoistness(int moist) {
		this.dataManager.set(MOISTNESS, moist);
	}

	public boolean isFreedom() {
		return isFreedom;
	}

	public void setFreedom(boolean boolIn) {
		isFreedom = boolIn;
	}

	public BlockPos getFreePosBlock() {
		return new BlockPos(freePos);
	}

	public Vec3d getFreePos() {
		return freePos;
	}

	public void setFreePos(Vec3d vecIn) {
		freePos = vecIn;
	}

	public void setFreePos(BlockPos posIn) {
		freePos = new Vec3d(posIn);
	}

	public void setFreePos(Double x, Double y, Double z) {
		freePos = new Vec3d(x, y, z);
	}

	// アイテムで遊ぶ用のAI
	class AIPlayWithItems extends EntityAIBase {
		private int playTick;

		private AIPlayWithItems() {
		}

		@Override
		public boolean shouldExecute() {
			if (this.playTick > EntityDolphin.this.ticksExisted) {
				return false;
			} else {
				List<EntityItem> list = EntityDolphin.this.world.getEntitiesWithinAABB(EntityItem.class, EntityDolphin.this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.ITEM_SELECTOR);
				return !list.isEmpty()
						|| !EntityDolphin.this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty();
			}
		}

		@Override
		public void startExecuting() {
			List<EntityItem> list = EntityDolphin.this.world.getEntitiesWithinAABB(EntityItem.class, EntityDolphin.this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.ITEM_SELECTOR);
			if (!list.isEmpty()) {
				EntityDolphin.this.getNavigator().tryMoveToEntityLiving(list.get(0), 1.2F);
				EntityDolphin.this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_PLAY, 1.0F, 1.0F);
			}
			this.playTick = 0;
		}

		@Override
		public void resetTask() {
			ItemStack itemstack = EntityDolphin.this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
			if (!itemstack.isEmpty()) {
				if (itemstack.getItem() == Items.FISH) {
					// 魚の場合はつまみ食い
					EntityDolphin.this.world.setEntityState(EntityDolphin.this, (byte) 7);
					EntityDolphin.this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_EAT, 1.0F, 1.0F);
					EntityDolphin.this.heal(2);
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				} else  if (EntityDolphin.this.getOwner() != null && EntityDolphin.this.getOwner().isAddedToWorld() && EntityDolphin.this.getDistanceSq(getOwner()) >= 1.0D && !EntityDolphin.this.isFreedom()) {
					// 主人のところにアイテムを持ってくる
					EntityDolphin.this.getNavigator().tryMoveToEntityLiving(EntityDolphin.this.getOwner(), 1.2F);
				} else if (EntityDolphin.this.getOwner() != null && !EntityDolphin.this.isFreedom() && ((EntityPlayer)EntityDolphin.this.getOwner()).addItemStackToInventory(itemstack)) {
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					EntityDolphin.this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_PLAY, 1.0F, 1.0F);
				} else {
					// 遊ぶ
					EntityDolphin.this.throwItem(itemstack);
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					this.playTick = EntityDolphin.this.ticksExisted + EntityDolphin.this.rand.nextInt(100);
				}
			}
		}

		@Override
		public void updateTask() {
			List<EntityItem> list = EntityDolphin.this.world.getEntitiesWithinAABB(EntityItem.class,
					EntityDolphin.this.getEntityBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.ITEM_SELECTOR);
			ItemStack itemstack = EntityDolphin.this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
			if (!itemstack.isEmpty()) {
				if (itemstack.getItem() == Items.FISH) {
					// 魚の場合はつまみ食い
					EntityDolphin.this.world.setEntityState(EntityDolphin.this, (byte) 7);
					EntityDolphin.this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_EAT, 1.0F, 1.0F);
					EntityDolphin.this.heal(2);
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				} else  if (EntityDolphin.this.getOwner() != null && EntityDolphin.this.getOwner().isAddedToWorld() && EntityDolphin.this.getDistanceSq(getOwner()) >= 1.0D && !EntityDolphin.this.isFreedom()) {
					// 主人のところにアイテムを持ってくる
					EntityDolphin.this.getNavigator().tryMoveToEntityLiving(EntityDolphin.this.getOwner(), 1.2F);
				} else if (EntityDolphin.this.getOwner() != null && !EntityDolphin.this.isFreedom() && ((EntityPlayer)EntityDolphin.this.getOwner()).addItemStackToInventory(itemstack)) {
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					EntityDolphin.this.playSound(DolphinSoundEvents.ENTITY_DOLPHIN_PLAY, 1.0F, 1.0F);
				} else {
					// 遊ぶ
					EntityDolphin.this.throwItem(itemstack);
					EntityDolphin.this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				}
			} else if (!list.isEmpty()) {
				// アイテムめがけて移動
				EntityDolphin.this.getNavigator().tryMoveToEntityLiving(list.get(0), 1.2F);
			}
		}
	}
}