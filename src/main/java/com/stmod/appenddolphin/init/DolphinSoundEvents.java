package com.stmod.appenddolphin.init;

import com.stmod.appenddolphin.AppendDolphin;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class DolphinSoundEvents {
	public static final SoundEvent ENTITY_DOLPHIN_AMBIENT;
	public static final SoundEvent ENTITY_DOLPHIN_AMBIENT_WATER;
	public static final SoundEvent ENTITY_DOLPHIN_ATTACK;
	public static final SoundEvent ENTITY_DOLPHIN_DEATH;
	public static final SoundEvent ENTITY_DOLPHIN_EAT;
	public static final SoundEvent ENTITY_DOLPHIN_HURT;
	public static final SoundEvent ENTITY_DOLPHIN_JUMP;
	public static final SoundEvent ENTITY_DOLPHIN_PLAY;
	public static final SoundEvent ENTITY_DOLPHIN_SPLASH;
	public static final SoundEvent ENTITY_DOLPHIN_SWIM;

	private static SoundEvent getRegisteredSoundEvent(String id) {
		SoundEvent soundevent = SoundEvent.REGISTRY.getObject(new ResourceLocation(AppendDolphin.MODID, id));
		if (soundevent == null) {
			throw new IllegalStateException("Invalid Sound requested: " + id);
		} else {
			return soundevent;
		}
	}

	static {
		ENTITY_DOLPHIN_AMBIENT = getRegisteredSoundEvent("dolphin.ambient");
		ENTITY_DOLPHIN_AMBIENT_WATER = getRegisteredSoundEvent("dolphin.ambient_water");
		ENTITY_DOLPHIN_ATTACK = getRegisteredSoundEvent("dolphin.attack");
		ENTITY_DOLPHIN_DEATH = getRegisteredSoundEvent("dolphin.death");
		ENTITY_DOLPHIN_EAT = getRegisteredSoundEvent("dolphin.eat");
		ENTITY_DOLPHIN_HURT = getRegisteredSoundEvent("dolphin.hurt");
		ENTITY_DOLPHIN_JUMP = getRegisteredSoundEvent("dolphin.jump");
		ENTITY_DOLPHIN_PLAY = getRegisteredSoundEvent("dolphin.play");
		ENTITY_DOLPHIN_SPLASH = getRegisteredSoundEvent("dolphin.splash");
		ENTITY_DOLPHIN_SWIM = getRegisteredSoundEvent("dolphin.swim");
	}
}
