package buildcraft.api.power;

/**
 * Use this class for IEnergyHandlers which wish to receive
 * Redstone Engine power.
 *
 * Please note, however, that BuildCraft's design does NOT
 * want you to allow this for any block. Redstone engines
 * generally emit 0.625 RF/t in pulses.
 *
 * Please consult us before having your tile entity
 * implement this. We're making the interface public as
 * an act of good will.
 */
public interface IRedstoneEnergyReceiver {
	boolean canReceiveRedstoneEnergy();
}
