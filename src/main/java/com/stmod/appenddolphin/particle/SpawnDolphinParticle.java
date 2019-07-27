package com.stmod.appenddolphin.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

public class SpawnDolphinParticle {

	private static Minecraft mc = Minecraft.getMinecraft();

	public static Particle spawnParticle(double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
		// パーティクル表示用のメソッド
		if (mc != null && mc.getRenderViewEntity() != null && mc.effectRenderer != null) {
			int pAmount = mc.gameSettings.particleSetting;

			if (pAmount == 1 && mc.world.rand.nextInt(3) == 0) {
				pAmount = 2;
			}

			double vX = mc.getRenderViewEntity().posX - xCoordIn;
			double vY = mc.getRenderViewEntity().posY - yCoordIn;
			double vZ = mc.getRenderViewEntity().posZ - zCoordIn;
			double mx = 16.0D;

			if (vX * vX + vY * vY + vZ * vZ > mx * mx || pAmount > 1) {
				return null;
			} else {
				Particle pOut = new ParticleDolphin.DolphinSpeedFactory().createParticle(0, mc.world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
				mc.effectRenderer.addEffect(pOut);
				return pOut;
			}
		}
		return null;
	}
}