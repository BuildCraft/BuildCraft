/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class Technology {

	public static final HashMap<String, Technology> technologies = new HashMap<String, Technology>();
	private static final LinkedList[] registry = new LinkedList[Tier.values().length];

	protected ArrayList<Technology> followups = new ArrayList<Technology>();

	private Tier tier;
	private ItemStack[] requirements;
	private ArrayList<Technology> prerequisites = new ArrayList<Technology>();
	private String id;

	protected void initialize(String iId,
			Tier iTier,
			Object requirement1,
			Object requirement2,
			Object requirement3,
			Technology... iPrerequisites) {
		getTechnologies(iTier).add(this);
		technologies.put(iId, this);

		id = iId;

		tier = iTier;

		requirements = new ItemStack[]
		{toStack(requirement1),
				toStack(requirement2),
				toStack(requirement3)};

		prerequisites.add(iTier.getTechnology());

		for (Technology t : iPrerequisites) {
			prerequisites.add(t);
			t.followups.add(this);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon() {
		return null;
	}

	public ItemStack getStackToDisplay() {
		return null;
	}

	public String getLocalizedName() {
		return null;
	}

	public final ArrayList<Technology> getPrerequisites() {
		return prerequisites;
	}

	public final ArrayList<Technology> getFollowups() {
		return followups;
	}

	public final ItemStack[] getRequirements() {
		return requirements;
	}

	public final Tier getTier() {
		return tier;
	}

	public static Collection<Technology> getTechnologies(Tier tier) {
		if (registry[tier.ordinal()] == null) {
			registry[tier.ordinal()] = new LinkedList();
		}

		return registry[tier.ordinal()];
	}

	public static Technology getTechnology(String id) {
		return technologies.get(id);
	}

	public static ItemStack toStack(Object obj) {
		if (obj instanceof ItemStack) {
			return (ItemStack) obj;
		} else if (obj instanceof Item) {
			return new ItemStack((Item) obj);
		} else if (obj instanceof Block) {
			return new ItemStack((Block) obj);
		} else {
			return null;
		}
	}

	public final String getID() {
		return id;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {

	}

	public String getWikiLink() {
		return "http://www.mod-buildcraft.com/wiki/doku.php?id=techno:" + getID().replaceAll(":", "_");
	}
}
