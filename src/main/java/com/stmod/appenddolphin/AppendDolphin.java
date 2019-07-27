package com.stmod.appenddolphin;

import com.stmod.appenddolphin.entity.EntityDolphin;
import com.stmod.appenddolphin.item.ItemDolphinEgg;
import com.stmod.appenddolphin.register.SoundRegister;
import com.stmod.appenddolphin.renderer.RenderDolphin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = AppendDolphin.MODID, name = AppendDolphin.NAME, version = AppendDolphin.VERSION)
public class AppendDolphin {
	// TODO : 難読化部分はビルド時に切り替えておく
	//public static final String lookHelper = "lookHelper";
	//public static final String particleMaxAge = "particleMaxAge";
	public static final String lookHelper = "field_70749_g";
	public static final String particleMaxAge = "field_70547_e";

	public static final String MODID = "appenddolphin";
	public static final String NAME = "AppendDolphin";
	public static final String VERSION = "1.0";
	public static final Item dolphinEgg = new ItemDolphinEgg();;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		// 水中にスポーンさせるように設定
		EntitySpawnPlacementRegistry.setPlacementType(EntityDolphin.class, EntityLiving.SpawnPlacementType.IN_WATER);
		ForgeRegistries.ITEMS.register(dolphinEgg);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// MOBの登録
        EntityRegistry.registerModEntity(new ResourceLocation("minecraft", "dolphin"), EntityDolphin.class, "dolphin", 0, this, 80, 3, true);
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
        	RenderingRegistry.registerEntityRenderingHandler(EntityDolphin.class, new RenderDolphin(Minecraft.getMinecraft().getRenderManager()));
        }
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// スポーンの登録
		EntityRegistry.addSpawn(EntityDolphin.class, 2, 1, 2, EnumCreatureType.WATER_CREATURE, Biomes.OCEAN, Biomes.DEEP_OCEAN);
	}

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
    	// アイテムのモデルの登録
        ModelLoader.setCustomModelResourceLocation(dolphinEgg, 0, new ModelResourceLocation(new ResourceLocation(AppendDolphin.MODID, "dolphin_spawn_egg"), "inventory"));
    }

	@SubscribeEvent
	public void registerItemColormHandlers(ColorHandlerEvent.Item event) {
		// アイテム色の登録
		ItemColors itemcolors = event.getItemColors();
		itemcolors.registerItemColorHandler(new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				return tintIndex == 0 ? 2243405 : 16382457;
			}
		}, dolphinEgg);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
		// 効果音の登録
		event.getRegistry().registerAll(
				SoundRegister.ambient,
				SoundRegister.ambient_water,
				SoundRegister.attack,
				SoundRegister.death,
				SoundRegister.eat,
				SoundRegister.hurt,
				SoundRegister.jump,
				SoundRegister.play,
				SoundRegister.splash,
				SoundRegister.swim);
	}
}
