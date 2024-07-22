package xyz.xenondevs.nova.addon.machines.tileentity.energy

import org.joml.Quaternionf
import xyz.xenondevs.cbf.Compound
import xyz.xenondevs.commons.provider.immutable.map
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.nova.addon.machines.registry.Blocks
import xyz.xenondevs.nova.addon.machines.registry.Models
import xyz.xenondevs.nova.addon.machines.util.efficiencyDividedValue
import xyz.xenondevs.nova.addon.simpleupgrades.gui.OpenUpgradesItem
import xyz.xenondevs.nova.addon.simpleupgrades.registry.UpgradeTypes
import xyz.xenondevs.nova.addon.simpleupgrades.storedEnergyHolder
import xyz.xenondevs.nova.addon.simpleupgrades.storedUpgradeHolder
import xyz.xenondevs.nova.tileentity.NetworkedTileEntity
import xyz.xenondevs.nova.tileentity.menu.TileEntityMenuClass
import xyz.xenondevs.nova.tileentity.network.type.NetworkConnectionType.EXTRACT
import xyz.xenondevs.nova.ui.menu.EnergyBar
import xyz.xenondevs.nova.util.yaw
import xyz.xenondevs.nova.world.BlockPos
import xyz.xenondevs.nova.world.block.state.NovaBlockState
import xyz.xenondevs.nova.world.block.state.property.DefaultBlockStateProperties
import xyz.xenondevs.nova.world.model.Model
import xyz.xenondevs.nova.world.model.MovableMultiModel
import kotlin.math.roundToLong

private val MAX_ENERGY = Blocks.WIND_TURBINE.config.entry<Long>("capacity")
private val ENERGY_PER_TICK = Blocks.WIND_TURBINE.config.entry<Long>("energy_per_tick")
private val PLAY_ANIMATION by Blocks.WIND_TURBINE.config.entry<Boolean>("animation")

class WindTurbine(pos: BlockPos, blockState: NovaBlockState, data: Compound) : NetworkedTileEntity(pos, blockState, data) {
    
    private val upgradeHolder = storedUpgradeHolder(UpgradeTypes.EFFICIENCY, UpgradeTypes.ENERGY)
    private val energyHolder = storedEnergyHolder(MAX_ENERGY, upgradeHolder, EXTRACT)
    
    private val turbineModel = MovableMultiModel()
    private val altitude = (pos.y - pos.world.minHeight) / (pos.world.maxHeight - pos.world.minHeight - 1).toDouble()
    private val rotationPerTick = altitude * 15.0
    private val energyPerTick by efficiencyDividedValue(ENERGY_PER_TICK, upgradeHolder).map { (it * altitude).roundToLong() }
    
    override fun handleEnable() {
        super.handleEnable()
        spawnModels()
    }
    
    override fun handleDisable() {
        super.handleDisable()
        turbineModel.close()
    }
    
    private fun spawnModels() {
        val location = pos.location.add(0.5, 3.5, 0.5)
        location.yaw = blockState.getOrThrow(DefaultBlockStateProperties.FACING).yaw
        
        turbineModel.add(Model(Models.WIND_TURBINE_ROTOR_MIDDLE, location))
        for (blade in 0..2) {
            turbineModel.add(Model(
                Models.WIND_TURBINE_ROTOR_BLADE,
                location,
                rightRotation = Quaternionf().setAngleAxis(
                    (Math.PI * 2 / 3 * blade).toFloat(),
                    0f, 0f, 1f
                )
            ))
        }
    }
    
    override fun handleTick() {
        energyHolder.energy += energyPerTick
    }
    
    override suspend fun handleAsyncTick() {
        if (!PLAY_ANIMATION)
            return
        
        turbineModel.useMetadata {
            it.leftRotation = it.leftRotation.rotateAxis(
                Math.toRadians(rotationPerTick).toFloat(),
                0f, 0f, 1f,
                Quaternionf()
            )
        }
    }
    
    @TileEntityMenuClass
    inner class WindTurbineMenu : GlobalTileEntityMenu() {
        
        override val gui = Gui.normal()
            .setStructure(
                "1 - - - - - - - 2",
                "| u # # e # # # |",
                "| # # # e # # # |",
                "| # # # e # # # |",
                "3 - - - - - - - 4")
            .addIngredient('u', OpenUpgradesItem(upgradeHolder))
            .addIngredient('e', EnergyBar(3, energyHolder))
            .build()
        
    }
    
}