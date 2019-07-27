package com.stmod.appenddolphin.register;

import com.stmod.appenddolphin.AppendDolphin;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class SoundRegister {
	public static final SoundEvent ambient;
	public static final SoundEvent ambient_water;
	public static final SoundEvent attack;
	public static final SoundEvent death;
	public static final SoundEvent eat;
	public static final SoundEvent hurt;
	public static final SoundEvent jump;
	public static final SoundEvent play;
	public static final SoundEvent splash;
	public static final SoundEvent swim;

	static {
		ambient = addSoundsToRegistry("dolphin.ambient");
		ambient_water = addSoundsToRegistry("dolphin.ambient_water");
		attack = addSoundsToRegistry("dolphin.attack");
		death = addSoundsToRegistry("dolphin.death");
		eat = addSoundsToRegistry("dolphin.eat");
		hurt = addSoundsToRegistry("dolphin.hurt");
		jump = addSoundsToRegistry("dolphin.jump");
		play = addSoundsToRegistry("dolphin.play");
		splash = addSoundsToRegistry("dolphin.splash");
		swim = addSoundsToRegistry("dolphin.swim");
	}

	private static SoundEvent addSoundsToRegistry(String soundId) {
		ResourceLocation shotSoundLocation = new ResourceLocation(AppendDolphin.MODID, soundId);
		SoundEvent soundEvent = new SoundEvent(shotSoundLocation);
		soundEvent.setRegistryName(shotSoundLocation);
		return soundEvent;
	}
}
