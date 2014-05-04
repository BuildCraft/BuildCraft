/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;


/**
 * This class allow to specify specific behavior for blocks stored in
 * blueprints:
 *
 * - what items needs to be used to create that block - how the block has to be
 * built on the world - how to rotate the block - what extra data to store /
 * load in the blueprint
 *
 * Default implementations of this can be seen in the package
 * buildcraft.api.schematics. The class SchematicUtils provide some additional
 * utilities.
 *
 * Blueprints perform "id translation" in case the block ids between a blueprint
 * and the world installation are different. Mapping is done through the
 * builder context.
 *
 * At blueprint load time, BuildCraft will check that each block id of the
 * blueprint corresponds to the block id in the installation. If not, it will
 * perform a search through the block list, and upon matching signature, it will
 * translate all blocks ids of the blueprint to the installation ones. If no
 * such block id is found, BuildCraft will assume that the block is not
 * installed and will not load the blueprint.
 */
public abstract class SchematicBlockBase extends Schematic {

}
