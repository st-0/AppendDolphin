package com.stmod.appenddolphin.particle;

import java.lang.reflect.Field;

import com.stmod.appenddolphin.AppendDolphin;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSuspendedTown;
import net.minecraft.world.World;

public class ParticleDolphin extends ParticleSuspendedTown {
	// 泳いでるときのパーティクル

	protected ParticleDolphin(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double speedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, speedIn);
	}

	public static class DolphinSpeedFactory implements IParticleFactory {
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			Particle particle = new ParticleDolphin(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
			particle.setRBGColorF(0.3F, 0.5F, 1.0F);
			particle.setAlphaF(1.0F - worldIn.rand.nextFloat() * 0.7F);
			try {
				particle.setMaxAge(getMaxAge(particle) / 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return particle;
		}
	}

	private static int getMaxAge(Particle particle) throws Exception {
		// 強引にアクセス
		Field field = Particle.class.getDeclaredField(AppendDolphin.particleMaxAge);
		field.setAccessible(true);
		return field.getInt(particle);
	}
}
