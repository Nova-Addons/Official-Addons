package xyz.xenondevs.nova.addon.simpleupgrades

import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.enumMap
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.combinedProvider
import xyz.xenondevs.commons.provider.immutable.map
import xyz.xenondevs.nova.addon.simpleupgrades.registry.UpgradeTypes
import xyz.xenondevs.nova.tileentity.NetworkedTileEntity
import xyz.xenondevs.nova.tileentity.TileEntity
import xyz.xenondevs.nova.tileentity.network.type.NetworkConnectionType
import xyz.xenondevs.nova.tileentity.network.type.energy.holder.DefaultEnergyHolder
import xyz.xenondevs.nova.tileentity.network.type.fluid.FluidType
import xyz.xenondevs.nova.tileentity.network.type.fluid.container.DynamicFluidContainer
import xyz.xenondevs.nova.util.CUBE_FACES
import xyz.xenondevs.nova.world.region.DynamicRegion
import xyz.xenondevs.nova.world.region.Region
import kotlin.math.roundToLong

/**
 * Retrieves an [UpgradeHolder] with the given [allowedTypes] that is stored in this [TileEntity]
 * or creates a new one if none is present.
 *
 * The upgrade holder will also be registered as a drop provider, i.e. the upgrade items will be dropped
 * when the [TileEntity] is destroyed.
 */
fun TileEntity.storedUpgradeHolder(vararg allowedTypes: UpgradeType<*>): UpgradeHolder =
    storedUpgradeHolder(false, *allowedTypes)

/**
 * Retrieves an [UpgradeHolder] with the given [allowedTypes] that is stored in this [TileEntity]
 * or creates a new one if none is present.
 *
 * If [persistent] is false, the upgrade holder will also be registered as a drop provider, i.e. the upgrade
 * items will be dropped when the [TileEntity] is destroyed. Otherwise, the upgrade items will be stored in the
 * [TileEntity's][TileEntity] [ItemStack].
 */
fun TileEntity.storedUpgradeHolder(persistent: Boolean, vararg allowedTypes: UpgradeType<*>): UpgradeHolder {
    val holder = UpgradeHolder(this, allowedTypes.toHashSet(), storedValue("upgrades", persistent, ::HashMap))
    dropProvider(holder::getUpgradeItems)
    return holder
}

/**
 * Creates a [DefaultEnergyHolder] whose [maxEnergy] will be automatically affected by
 * the [UpgradeTypes.ENERGY] value of [upgradeHolder].
 *
 * @see NetworkedTileEntity.storedEnergyHolder
 */
fun NetworkedTileEntity.storedEnergyHolder(
    maxEnergy: Provider<Long>,
    upgradeHolder: UpgradeHolder,
    allowedConnectionType: NetworkConnectionType,
    defaultConnectionConfig: () -> Map<BlockFace, NetworkConnectionType> = { CUBE_FACES.associateWithTo(enumMap()) { allowedConnectionType } }
): DefaultEnergyHolder =
    storedEnergyHolder(
        combinedProvider(
            maxEnergy,
            upgradeHolder.getValueProvider(UpgradeTypes.ENERGY)
        ).map { (maxEnergy, energy) -> (maxEnergy * energy).roundToLong() },
        allowedConnectionType,
        defaultConnectionConfig
    )


/**
 * Creates a [DynamicFluidContainer] whose [capacity] will be automatically affected by
 * the [UpgradeTypes.FLUID] value of [upgradeHolder].
 *
 * @see TileEntity.storedFluidContainer
 */
fun TileEntity.storedFluidContainer(
    name: String,
    allowedTypes: Set<FluidType>,
    capacity: Provider<Long>,
    upgradeHolder: UpgradeHolder,
    persistent: Boolean = false,
    updateHandler: (() -> Unit)? = null,
): DynamicFluidContainer =
    storedFluidContainer(
        name,
        allowedTypes,
        combinedProvider(
            capacity,
            upgradeHolder.getValueProvider(UpgradeTypes.FLUID)
        ).map { (capacity, fluid) -> (capacity * fluid).roundToLong() },
        persistent,
        updateHandler
    )


/**
 * Creates a [DynamicRegion] whose size will be automatically affected by
 * the [UpgradeTypes.RANGE] value of [upgradeHolder].
 *
 * @see TileEntity.storedRegion
 */
fun TileEntity.storedRegion(
    name: String,
    minSize: Provider<Int>,
    maxSize: Provider<Int>,
    defaultSize: Int,
    upgradeHolder: UpgradeHolder,
    createRegion: (Int) -> Region
): DynamicRegion =
    storedRegion(
        name,
        minSize,
        combinedProvider(
            maxSize,
            upgradeHolder.getValueProvider(UpgradeTypes.RANGE)
        ).map { (min, range) -> min + range },
        defaultSize,
        createRegion
    )