package buildcraft.core.triggers;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class ActionTriggerIconProvider implements IIconProvider {
	
	public static ActionTriggerIconProvider INSTANCE = new ActionTriggerIconProvider();

	public static final int Action_MachineControl_On 			=  0;
	public static final int Action_MachineControl_Off 			=  1;
	public static final int Action_MachineControl_Loop 			=  2;

	public static final int Trigger_EngineHeat_Blue 			=  3;
	public static final int Trigger_EngineHeat_Green 			=  4;
	public static final int Trigger_EngineHeat_Yellow 			=  5;
	public static final int Trigger_EngineHeat_Red 				=  6;
	public static final int Trigger_Inventory_Empty 			=  7;
	public static final int Trigger_Inventory_Contains 			=  8;
	public static final int Trigger_Inventory_Space 			=  9;
	public static final int Trigger_Inventory_Full 				= 10;
	public static final int Trigger_FluidContainer_Empty 		= 11;
	public static final int Trigger_FluidContainer_Contains	= 12;
	public static final int Trigger_FluidContainer_Space 		= 13;
	public static final int Trigger_FluidContainer_Full 		= 14;
	public static final int Trigger_Machine_Active 				= 15;
	public static final int Trigger_Machine_Inactive 			= 16;
	public static final int Trigger_PipeContents_Empty 			= 17;
	public static final int Trigger_PipeContents_ContainsItems 	= 18;
	public static final int Trigger_PipeContents_ContainsFluid = 19;
	public static final int Trigger_PipeContents_ContainsEnergy = 20;
	public static final int Trigger_PipeSignal_Red_Active 		= 21;
	public static final int Trigger_PipeSignal_Blue_Active 		= 22;
	public static final int Trigger_PipeSignal_Green_Active 	= 23;
	public static final int Trigger_PipeSignal_Yellow_Active 	= 24;
	public static final int Trigger_PipeSignal_Red_Inactive 	= 25;
	public static final int Trigger_PipeSignal_Blue_Inactive 	= 26;
	public static final int Trigger_PipeSignal_Green_Inactive 	= 27;
	public static final int Trigger_PipeSignal_Yellow_Inactive 	= 28;
	public static final int Trigger_RedstoneInput_Active 		= 29;
	public static final int Trigger_RedstoneInput_Inactive 		= 30;
	public static final int Trigger_PipeContents_RequestsEnergy	= 31;
	public static final int Trigger_PipeContents_TooMuchEnergy	= 32;
	public static final int Trigger_Inventory_Below25 			= 33;
	public static final int Trigger_Inventory_Below50 			= 34;
	public static final int Trigger_Inventory_Below75 			= 35;

	public static final int MAX 								= 36;


	@SideOnly(Side.CLIENT)
	private final Icon[] icons = new Icon[MAX];
	
	private ActionTriggerIconProvider(){}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		icons[ActionTriggerIconProvider.Action_MachineControl_On] = iconRegister.registerIcon("buildcraft:triggers/action_machinecontrol_on");
		icons[ActionTriggerIconProvider.Action_MachineControl_Off] = iconRegister.registerIcon("buildcraft:triggers/action_machinecontrol_off");
		icons[ActionTriggerIconProvider.Action_MachineControl_Loop] = iconRegister.registerIcon("buildcraft:triggers/action_machinecontrol_loop");

//		icons[ActionTriggerIconProvider.Trigger_EngineHeat_Blue] = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_blue");
//		icons[ActionTriggerIconProvider.Trigger_EngineHeat_Green] = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_green");
//		icons[ActionTriggerIconProvider.Trigger_EngineHeat_Yellow] = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_yellow");
//		icons[ActionTriggerIconProvider.Trigger_EngineHeat_Red] = iconRegister.registerIcon("buildcraft:triggers/trigger_engineheat_red");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Empty] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_empty");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Contains] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_contains");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Space] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_space");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Full] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_full");
		icons[ActionTriggerIconProvider.Trigger_FluidContainer_Empty] = iconRegister.registerIcon("buildcraft:triggers/trigger_liquidcontainer_empty");
		icons[ActionTriggerIconProvider.Trigger_FluidContainer_Contains] = iconRegister.registerIcon("buildcraft:triggers/trigger_liquidcontainer_contains");
		icons[ActionTriggerIconProvider.Trigger_FluidContainer_Space] = iconRegister.registerIcon("buildcraft:triggers/trigger_liquidcontainer_space");
		icons[ActionTriggerIconProvider.Trigger_FluidContainer_Full] = iconRegister.registerIcon("buildcraft:triggers/trigger_liquidcontainer_full");
		icons[ActionTriggerIconProvider.Trigger_Machine_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_active");
		icons[ActionTriggerIconProvider.Trigger_Machine_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_inactive");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_Empty] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_empty");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_ContainsItems] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsitems");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_ContainsFluid] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsliquid");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_ContainsEnergy] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_containsenergy");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_RequestsEnergy] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_requestsenergy");
//		icons[ActionTriggerIconProvider.Trigger_PipeContents_TooMuchEnergy] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipecontents_toomuchenergy");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_red_active");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Red_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_red_inactive");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_blue_active");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_blue_inactive");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_green_active");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Green_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_green_inactive");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_yellow_active");
		icons[ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_pipesignal_yellow_inactive");
		icons[ActionTriggerIconProvider.Trigger_RedstoneInput_Active] = iconRegister.registerIcon("buildcraft:triggers/trigger_redstoneinput_active");
		icons[ActionTriggerIconProvider.Trigger_RedstoneInput_Inactive] = iconRegister.registerIcon("buildcraft:triggers/trigger_redstoneinput_inactive");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Below25] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_below25");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Below50] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_below50");
		icons[ActionTriggerIconProvider.Trigger_Inventory_Below75] = iconRegister.registerIcon("buildcraft:triggers/trigger_inventory_below75");
	}

}
