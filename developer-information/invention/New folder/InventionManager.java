package com.rs.game.player.managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.InventionDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.entity.Hit;
import com.rs.game.item.Item;
import com.rs.game.item.ItemConstants;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Skills;
import com.rs.game.player.actions.skillAction.invention.Disassemble;
import com.rs.game.player.actions.skillAction.invention.InventionData;
import com.rs.game.player.actions.skillAction.invention.ItemDisassemblyData;
import com.rs.game.player.actions.skillAction.invention.MaterialData;
import com.rs.game.player.actions.skillAction.invention.InventionConstants.Perks;
import com.rs.game.player.actions.skillAction.invention.InventionData.Gizmo;
import com.rs.game.player.actions.skillAction.invention.InventionData.Perk;
import com.rs.game.player.actions.skillAction.invention.MaterialData.PossiblePerk;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.player.playerOptions.Equipment;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.ItemsDisassemblyData;
import com.rs.utils.MaterialsData;
import com.rs.utils.Utils;

public class InventionManager implements Serializable {
	
	private transient int[] addedMaterials;
	private int afterShockDamage;
	private int containerChargesFilled;
	private boolean convertMemories;
	private long cracklingCooldown;
	private transient BluePrint currentBluePrint;
	public boolean[] discoveredBluePrints;
	private int divineCharges;
	private int divineChargesStored;
	private long drainCoolDown;
	private int emptyDivineChargesStored;
	private transient int gizmoType;
	private boolean hasChargePack;
	private int inspiration;
	private transient int interactionIndex;
	private int[] materials;
	private transient Player player;
	private transient Object[][] possiblePerks;
	private boolean resetCharges;
	
	public class BluePrint {

		private boolean[] blockedModules;
		private int bluePrintId;
		private int[] correctModules;
		private int selectedIndex;
		private int[][] selectedModules;
		private int stage;

		public BluePrint(int bluePrintId) {
			this.bluePrintId = bluePrintId;
			this.selectedModules = new int[2][5];
			this.blockedModules = new boolean[10];
			this.selectedIndex = -1;
		}

		public void addModul(int modulNumber) {
			if (blockedModules[modulNumber - 1])
				return;
			for (int i = 0; i < selectedModules[stage - 1].length; i++) {
				if (selectedModules[stage - 1][i] == 0) {
					selectedModules[stage - 1][i] = modulNumber;
					break;
				}
			}
			refresh();
		}

		private void blockIncorrectModul(int modulIndex) {
			blockedModules[modulIndex] = true;
			refresh();
		}

		private boolean containsModul(int modulNumber) {
			for (int i = 0; i < correctModules.length; i++)
				if (correctModules[i] == modulNumber)
					return true;
			return false;
		}

		public void createPrototype() {
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					boolean failed = hasIncorrectModuls();
					if (!failed) {
						blockedModules = new boolean[10];
						stage = 2;
						player.getVarsManager().forceSendVarBit(30242, 0);
						player.getPackets().sendExecuteScript(12146, 1, 1);
						player.getPackets().sendHideIComponent(1708, 57, false);
						player.getPackets().sendHideIComponent(1708, 58, true);
						for (int i = 0; i < 10; i++) {
							player.getPackets().sendHideIComponent(1708, 43 + i, true);
							player.getPackets().sendHideIComponent(1708, (i == 0 ? 53 : 83 + i), true);
						}
						player.getPackets().sendIComponentText(1708, 4, "Place all the modules on the track.");
					} else
						player.getPackets().sendExecuteScript(12142, 0);
					player.getPackets().sendHideIComponent(1708, 98, false);
					refresh();
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							refresh();
						}
					}, 1);
					stop();
				}

			}, 1);
		}

		private void generateRandomCorrectModules() {
			int[] modules = new int[5];
			int randomModule = Utils.random(1, 11);
			int index = 0;
			while (index < modules.length) {
				boolean contains = false;
				for (int i = 0; i < modules.length; i++) {
					if (modules[i] == randomModule) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					modules[index] = randomModule;
					index++;
				}
				randomModule = Utils.random(1, 11);
			}
			correctModules = modules;
		}

		private void generateRandomModules() {
			ClientScriptMap modulesMap = ClientScriptMap.getMap(10744);
			int[] modules = new int[10];
			int randomModule = modulesMap.getIntValue(Utils.random(modulesMap.getSize()));
			int index = 0;
			while (index < modules.length) {
				boolean contains = false;
				for (int i = 0; i < modules.length; i++) {
					if (modules[i] == randomModule) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					modules[index] = randomModule;
					index++;
				}
				randomModule = modulesMap.getIntValue(Utils.random(modulesMap.getSize()));
			}
			for (int i = 0; i < modules.length; i++)
				player.getPackets().sendIComponentSprite(1708, 43 + i, modules[i]);
		}

		public int getBluePrintId() {
			return bluePrintId;
		}

		public int[] getCorrectModules() {
			return correctModules;
		}

		private int getIncorrectIndexCount() {
			int count = 0;
			for (int i = 0; i < correctModules.length; i++) {
				if (selectedModules[1][i] == 0)
					return -1;
				int index = selectedModules[1][i] - 1;
				if (correctModules[i] != selectedModules[0][index])
					count++;
			}
			return count;
		}

		public int getSelectedIndex() {
			return selectedIndex;
		}

		public int getStage() {
			return stage;
		}

		private boolean hasIncorrectModuls() {
			boolean hasIncorrectModuls = false;
			for (int i = 0; i < selectedModules[0].length; i++) {
				if (!containsModul(selectedModules[0][i])) {
					blockIncorrectModul(selectedModules[0][i] - 1);
					setModul(i, 0);
					hasIncorrectModuls = true;
				}
			}
			return hasIncorrectModuls;
		}

		public void invent() {
			if (discoveredBluePrints[bluePrintId])
				return;
			ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
			int dataId = bluePrintsMap.getIntValue(bluePrintId);
			InventionDefinitions def = InventionDefinitions.getData(dataId);
			String name = (String) def.getDataInIndex(1);
			int level = (int) def.getDataInIndex(8);
			int spirit = (int) def.getDataInIndex(5);
			double xp = InventionManager.getBluePrintXpForLevel(level);
			int n = getIncorrectIndexCount();
			double poorXP = xp * 0.2;
			double extraXp = (n == 5 ? 0 : n == 4 ? (xp * 0.4) : n == 3 ? (xp * 0.6) : n == 2 ? (xp * 0.8) : xp) - (n != 5 ? poorXP : 0);
			xp = poorXP + extraXp;
			discoveredBluePrints[bluePrintId] = true;
			double xpAdded = player.getSkills().addXp(26, xp * 0.05);
			player.getInterfaceManager().setInterface(false, 1708, 71, 1530);
			String message = "You earned " + Utils.getFormattedNumber((int) xpAdded) + " Invention XP, and discovered a new invention: <col=ffffff>" + name + "</col>.";
			player.getPackets().sendExecuteScript(2734, "Success!", message, "Ok", "", "", spirit);
			player.getPackets().sendGameMessage(message);
			player.getPackets().sendHideIComponent(1708, 71, false);
			player.getPackets().sendExecuteScript(12060, ((int) def.getDataInIndex(0)));
			stage = 3;
		}

		private boolean isSelected(int modulNumber) {
			for (int i = 0; i < selectedModules[1].length; i++)
				if (selectedModules[1][i] == modulNumber)
					return true;
			return false;
		}

		private void refresh() {
			for (int i = 0; i < 5; i++) {
				boolean b = isSelected(i + 1);
				player.getVarsManager().forceSendVarBit(30237 + i, b ? 0 : selectedModules[0][i]);
				player.getVarsManager().forceSendCSVarBit(30259 + i, b ? 0 : selectedModules[0][i]);
				player.getVarsManager().forceSendVarBit(30244 + i, selectedModules[1][i]);
				player.getVarsManager().forceSendCSVarBit(30264 + i, selectedModules[1][i]);
			}
			int value = 0;
			for (int i = 0; i < blockedModules.length; i++)
				if (blockedModules[i])
					value |= 1 << i;
			player.getVarsManager().forceSendVarBit(30242, value);
			player.getVarsManager().forceSendVarBit(30250, stage);
			player.getPackets().sendExecuteScript(stage == 1 ? 12131 : 12148);
			player.getPackets().sendExecuteScript(12128);
			if (stage == 2) {
				int n = getIncorrectIndexCount();
				ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
				int dataId = bluePrintsMap.getIntValue(bluePrintId);
				InventionDefinitions def = InventionDefinitions.getData(dataId);
				int level = (int) def.getDataInIndex(8);
				double xp = InventionManager.getBluePrintXpForLevel(level);
				double poorXP = xp * 0.2;
				double extraXp = (n == 5 ? 0 : n == 4 ? (xp * 0.4) : n == 3 ? (xp * 0.6) : n == 2 ? (xp * 0.8) : xp) - (n != 5 ? poorXP : 0);
				String col = "<col=" + (n == 0 ? "008000" : n == 2 ? "ffffff" : n == 3 ? "ffff00" : n == 4 ? "ffa500" : "ff0000") + ">";
				String text = col + "Optimisation: " + (n == 0 ? "Perfect" : n == 2 ? "Excellent" : n == 3 ? "Good" : n == 4 ? "Satisfactory" : "Poor") + "</col><br>" + "You will gain : <col=ffffff>" + Utils.formatNumber((int) poorXP) + " </col>+ " + col + Utils.formatNumber((int) extraXp) + "</col> extra XP.";
				player.getPackets().sendIComponentText(1708, 4, n == -1 ? "Place all the modules on the track." : text);
				player.getPackets().sendHideIComponent(1708, 104, /* inspiration >= inspirationRequired && */ getIncorrectIndexCount() != -1);
			}
		}

		public void removeModul(int index) {
			if (index >= selectedModules[0].length)
				return;
			selectedModules[0][index] = 0;
			refresh();
		}

		public void setModul(int index, int modulNumber) {
			if (index >= selectedModules[stage - 1].length)
				return;
			if (modulNumber != 0 && blockedModules[modulNumber - 1])
				return;
			selectedModules[stage - 1][index] = modulNumber;
			if (stage == 1)
				player.getPackets().sendExecuteScript(12138, index, modulNumber);
			refresh();
		}

		public void setSelectedIndex(int selectedIndex) {
			this.selectedIndex = selectedIndex;
		}

		public void setStage(int stage) {
			this.stage = stage;
		}

		public BluePrint start() {
			ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
			int dataId = bluePrintsMap.getIntValue(bluePrintId);
			InventionDefinitions def = InventionDefinitions.getData(dataId);
			player.getPackets().sendExecuteScript(12130, dataId, 0, (def.getDataInIndex(3)), 0, 0);
			generateRandomModules();
			generateRandomCorrectModules();
			stage = 1;
			player.getPackets().sendExecuteScript(12131);
			player.getVarsManager().forceSendVarBit(30250, stage);
			player.getVarsManager().forceSendVarBit(30242, 0);
			return this;
		}

		public void switchModul(int fromIndex, int toIndex) {
			if (fromIndex >= selectedModules[stage - 1].length || toIndex >= selectedModules[stage - 1].length)
				return;
			int temp = selectedModules[stage - 1][fromIndex];
			selectedModules[stage - 1][fromIndex] = selectedModules[stage - 1][toIndex];
			selectedModules[stage - 1][toIndex] = temp;
			refresh();
		}

	}

	public static final int[] MATERIALS_VARS = { 5998, 5999, 6000, 6001, 6002, 6003, 6004, 6005, 6006, 6007, 6008, 6009, 6010, 6011, 6012, 6013, 6014, 6015, 6016, 6017, 6018, 6019, 6020, 6021, 6022, 6023, 6025, 6064, 6065, 6026, 6027, 6028, 6029, 6030, 6031, 6032, 6033, 6034, 6035, 6036, 6037, 6038, 6039, 6024, 6040, 6041, 6042, 6043, 6044, 6045, 6046, 6047, 6062, 6048, 6049, 6050, 6051, 6052, 6053, 6054, 6055, 6056, 6057, 6058, 6059, 6060, 6061, 6063, 6066, 6215, 6216, 6217, 6218, 6508, 6509, 5997 };
	private static final long serialVersionUID = 3329963111343241716L;
	public static final Perks[] TWO_SLOTS_PERKS = { Perks.ENHANCED_DEVOTED, Perks.ENHANCED_EFFICIENT };
	public static double getBluePrintXpForLevel(int level) {
		if (level == 1)
			return 200.00;
		if (level == 2)
			return 250.00;
		if (level == 3)
			return 400.00;
		if (level == 4)
			return 107.00;
		if (level == 8)
			return 161.00;
		if (level == 16)
			return 628.00;
		if (level == 20)
			return 1198.10;
		if (level == 22)
			return 1541.00;
		if (level == 24)
			return 1541.00;
		if (level == 27)
			return 1541.00;
		if (level == 34)
			return 5446.00;
		if (level == 40)
			return 9184.00;
		if (level == 43)
			return 11221.00;
		if (level == 45)
			return 12726.00;
		if (level == 49)
			return 16114.00;
		if (level == 50)
			return 18257.00;
		if (level == 54)
			return 22600.00;
		if (level == 55)
			return 23779.00;
		if (level == 60)
			return 32284.50;
		if (level == 64)
			return 38610.00;
		if (level == 69)
			return 47562.00;
		if (level == 70)
			return 52591.00;
		if (level == 72)
			return 56862.00;
		if (level == 74)
			return 61347.00;
		if (level == 75)
			return 63672.00;
		if (level == 77)
			return 68489.00;
		if (level == 78)
			return 70981.00;
		if (level == 80)
			return 80618.00;
		if (level == 81)
			return 83441.00;
		if (level == 83)
			return 89273.00;
		if (level == 87)
			return 101703.00;
		if (level == 89)
			return 108310.00;
		if (level == 90)
			return 117921.00;
		if (level == 91)
			return 121584.00;
		if (level == 92)
			return 125320.00;
		if (level == 93)
			return 129127.50;
		if (level == 94)
			return 133008.50;
		if (level == 95)
			return 136962.60;
		if (level == 96)
			return 140990.50;
		if (level == 97)
			return 145093.00;
		if (level == 98)
			return 149272.00;
		if (level == 99)
			return 153526.00;
		if (level == 100)
			return 166167.00;
		if (level == 101)
			return 170806.70;
		if (level == 102)
			return 175528.00;
		if (level == 104)
			return 185218.50;
		if (level == 105)
			return 190188.80;
		if (level == 107)
			return 200382.30;
		if (level == 108)
			return 205606.90;
		if (level == 109)
			return 210917.40;
		if (level == 110)
			return 216314.60;
		if (level == 111)
			return 221798.80;
		if (level == 112)
			return 227370.00;
		if (level == 113)
			return 233031.00;
		if (level == 114)
			return 238780.00;
		if (level == 115)
			return 244619.00;
		if (level == 117)
			return 256568.00;
		if (level == 118)
			return 262680.00;
		return 0;
	}

	public InventionManager() {
		this.discoveredBluePrints = new boolean[ClientScriptMap.getMap(10743).getSize()];
		this.materials = new int[ClientScriptMap.getMap(10742).getSize()];
		materials[0] = 100;// simple parts
		materials[10] = 100;
		divineCharges = 300000000;
	}

	public void addDivineCharges(int quantity) {
		int amount = player.getInventory().getAmountOf(36390);
		if (quantity > amount)
			quantity = amount;
		if (quantity == 0)
			return;
		int increase = quantity * 3000 * 3000;
		double totalAmount = quantity + (double) divineCharges / (3000 * 3000);
		int maxDivineCharges = getMaxDivineCharges() / (3000 * 3000);
		if (totalAmount > maxDivineCharges) {
			quantity = (getMaxDivineCharges() - divineCharges) / (3000 * 3000);
			increase = quantity * 3000 * 3000;
			player.getPackets().sendGameMessage("You can't store more than " + Utils.getFormattedNumber((getMaxDivineCharges() / 3000)) + " Charges in your pack.");
			if (quantity == 0 || increase == 0)
				return;
		}
		player.getInventory().deleteItem(36390, quantity);
		divineCharges += increase;
		refreshDivineCharges();
		player.getPackets().sendGameMessage("You add " + quantity + " divine Charges containing " + Utils.getFormattedNumber((quantity * 3000)) + " Charge to your pack. You now have " + Utils.getFormattedNumber((divineCharges / 3000)) + " Charge stored.");
	}

	public void addEmptyDivineCharges(int amount) {
		if (amount == 0)
			return;
		if (amount > player.getInventory().getAmountOf(41073))
			amount = player.getInventory().getAmountOf(41073);
		if (amount + (emptyDivineChargesStored + divineChargesStored) > 100) {
			amount = 100 - emptyDivineChargesStored - divineChargesStored;
			player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " can't hold more than 100 empty divine charge containers.");
			if (amount == 0)
				return;
		}
		player.getInventory().deleteItem(41073, amount);
		emptyDivineChargesStored += amount;
		player.getPackets().sendGameMessage("You store " + amount + " empty divine charge containers in the " + ItemDefinitions.getItemDefinitions(41083).getName() + ".");
		player.getInventory().refresh();
		player.getEquipment().refresh();
	}

	public void addMaterial(int materialId) {
		addMaterial(-1, materialId, false);
	}

	public void addMaterial(int index, int materialId, boolean forceAdd) {
		if (index == -1) {
			for (int i = 0; i < addedMaterials.length; i++) {
				if (addedMaterials[i] == -1) {
					index = i;
					break;
				}
			}
		}
		interactionIndex++;
		if (index == -1 || (!forceAdd && addedMaterials[index] != -1)) {
			refreshAddMaterialsInterface();
			return;
		}
		addedMaterials[index] = materialId;
		refreshAddMaterialsInterface();
	}

	public boolean augmentItem(Item used, Item usedWith) {
		final Item augmentor = used.getId() == 36725 ? used : usedWith.getId() == 36725 ? usedWith : null;
		final Item originalItem = augmentor == null ? null : augmentor == used ? usedWith : used;
		if (augmentor == null || originalItem == null)
			return false;
		ItemDefinitions defs = originalItem.getDefinitions();
		if (defs.getAugmentedItemId() == 0) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You can't seem to work out how to augment that item.", 0, -120, 0);
			return true;
		}
		if (originalItem.getInventionData() != null) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You cannot augment an already augmented item.", 0, -120, 0);
			return true;
		}
		int category = defs.itemCategory;
		ClientScriptMap map = ClientScriptMap.getMap(10743);
		int dataId = map.getIntValue(8);
		if (!hasDiscoveredBluePrint(8) && (category == 7 || category == 18)) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".", 0, -120, 0);
			return true;
		}
		dataId = map.getIntValue(9);
		if (!hasDiscoveredBluePrint(9) && (category == 8 || category == 10)) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".", 0, -120, 0);
			return true;
		}
		dataId = map.getIntValue(10);
		if (!hasDiscoveredBluePrint(10) && (category == 67)) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".", 0, -120, 0);
			return true;
		}
		dataId = map.getIntValue(12);
		if (!hasDiscoveredBluePrint(12) && (category == 35)) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You need to discover " + InventionDefinitions.getDataName(dataId) + " before you can augment " + originalItem.getName() + ".", 0, -120, 0);
			return true;
		}
		boolean noted = defs.isNoted();
		if (noted) {
			player.getPackets().sendGameMessage("You can't augment a noted item.");
			return true;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				if (componentId == 26) {
					int augmentedId = defs.getAugmentedItemId();
					originalItem.setId(augmentedId);
					originalItem.setInventionData(new InventionData(0));
					player.getInventory().deleteItem(36725, 1);
					player.getInventory().refresh();
					player.getPackets().sendExecuteScript(1211, "You attach the augmentor, upgrading the item to an augmented version: " + originalItem.getName(), 0, -120, 0);

				}
				end();
			}

			@Override
			public void start() {

				ItemDefinitions defs = originalItem.getDefinitions();
				player.getInterfaceManager().sendCentralInterface(1048);
				player.getPackets().sendExecuteScript(9727, "Augment", "This will convert " + defs.getName() + " into an augmented version which requires divine charges to use.<br><br>This process cannot be undone without using an augmentation dissolver.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
			}

		});
		return true;
	}

	public boolean canUseCrackling() {
		return cracklingCooldown == 0 || Utils.currentTimeMillis() >= cracklingCooldown;
	}

	public boolean checkAugmentedItem(int slot, Item item, boolean equipment) {
		if (item.getInventionData() == null)
			return false;
		player.getInterfaceManager().sendCentralInterface(1711);
		player.getPackets().sendExecuteScript(12199, equipment ? 94 : 93, slot, (int) (ItemConstants.getAugmentedItemDrainRate(player, item) * 1800), -1);
		return true;
	}

	public void checkVaccumCharges() {
		player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " " + (divineChargesStored == 0 ? "is empty" : "has " + divineChargesStored + " divine charges stored") + ".");
	}

	public void clearMaterials() {
		interactionIndex++;
		Arrays.fill(addedMaterials, -1);
		refreshAddMaterialsInterface();
	}

	public void configureVaccum() {
		player.stopAll();
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {
			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				switch (stage) {
				case -1:
					stage = 0;
					sendOptionsDialogue("WHAT WOULD YOU LIKE TO DO?", "Siphon energy (default)", "Siphon energy and memories");
					break;
				case 0:
					convertMemories = componentId == OPTION_2;
					player.getPackets().sendGameMessage("<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " will now siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".");
					player.getPackets().sendExecuteScript(1211, "<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " will now siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".", 0, -120, 0);
					end();
					break;
				}
			}

			@Override
			public void start() {
				sendDialogue("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " is currently set to siphon " + (convertMemories ? "all energy and memories" : "all energy") + ".");
			}
		});
	}

	private void createGizmo() {
		if (player.getInventory().getFreeSlots() == 0) {
			player.getPackets().sendGameMessage("You don't have enough inventory space.");
			player.getInterfaceManager().closeFullScreenInterface();
			return;
		}
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				Perk[] perks = new Perk[2];
				if (possiblePerks.length > 0) {
					int random = Utils.random(possiblePerks.length);
					Object[] possiblePerk = possiblePerks[random];
					int perkId = (int) possiblePerk[0];
					int[] possibleRanks = (int[]) possiblePerk[1];
					perks[0] = new Perk(perkId, possibleRanks.length == 1 ? possibleRanks[0] : Utils.random(possibleRanks[0], possibleRanks[1] + 1));
					boolean twoSlotsPerk = false;
					for (Perks perk : TWO_SLOTS_PERKS) {
						if (perk.getId() == perkId) {
							twoSlotsPerk = true;
							break;
						}
					}
					if (!twoSlotsPerk && possiblePerks.length >= 2 && Utils.randomDouble() <= 0.7) {
						int secondPerk = Utils.random(possiblePerks.length);
						while (secondPerk == random) {
							secondPerk = Utils.random(possiblePerks.length);
						}
						twoSlotsPerk = false;
						for (Perks perk : TWO_SLOTS_PERKS) {
							if (perk.getId() == secondPerk) {
								twoSlotsPerk = true;
								break;
							}
						}
						if (!twoSlotsPerk) {
							possiblePerk = possiblePerks[secondPerk];
							perkId = (int) possiblePerk[0];
							possibleRanks = (int[]) possiblePerk[1];
							perks[1] = new Perk(perkId, possibleRanks.length == 1 ? possibleRanks[0] : Utils.random(possibleRanks[0], possibleRanks[1] + 1));
						}
					}
				}
				ClientScriptMap map = ClientScriptMap.getMap(10742);
				int[] materialsFieldIds = new int[5];

				for (int i = 0; i < materialsFieldIds.length; i++) {
					materialsFieldIds[i] = addedMaterials[i] == -1 ? -1 : map.getIntValue(addedMaterials[i]);
				}
				double chance = 0.00;
				for (int i = 0; i < addedMaterials.length; i++)
					chance += addedMaterials[i] != -1 ? 20.00 : 0.00;
				double roll = Utils.randomDouble();
				double xp = 0;
				int[][] materialsUsed = new int[addedMaterials.length][2];
				Map<Integer, Integer> totalMaterialsUsed = new HashMap<Integer, Integer>();
				if (roll <= chance) {
					for (int i = 0; i < addedMaterials.length; i++) {
						if (addedMaterials[i] == -1) {
							materialsUsed[i] = new int[] { -1, -1 };
							continue;
						}
						MaterialData data = MaterialsData.getMaterialData(addedMaterials[i]);
						if (data != null) {
							xp += data.getXp();
						}
						InventionDefinitions defs = InventionDefinitions.getData(map.getIntValue(addedMaterials[i]));
						int requiredAmount = ((int) defs.getDataInIndex(7)) == 2 ? 5 : 1;
						materials[addedMaterials[i]] -= requiredAmount;
						materialsUsed[i] = new int[] { addedMaterials[i], requiredAmount };
						if (totalMaterialsUsed.containsKey(addedMaterials[i])) {
							totalMaterialsUsed.put(addedMaterials[i], totalMaterialsUsed.get(addedMaterials[i]) + requiredAmount);
						} else
							totalMaterialsUsed.put(addedMaterials[i], requiredAmount);
					}
				}
				int amountRepeats = Integer.MAX_VALUE;
				for (int i = 0; i < materials.length; i++) {
					if (!totalMaterialsUsed.containsKey(i))
						continue;
					if ((materials[i] / totalMaterialsUsed.get(i)) < amountRepeats)
						amountRepeats = materials[i] / totalMaterialsUsed.get(i);
				}

				if (possiblePerks.length == 0 || roll > chance) {
					player.getPackets().sendExecuteScript(12192, 0, 0, 0, 0, 0, amountRepeats, player.getInventory().getAmountOf(36719 + (gizmoType * 2)));
				} else {
					Item gizmo = new Item(36720 + (gizmoType * 2), 1);
					Gizmo gizmos = new Gizmo(perks, materialsUsed);
					gizmo.setInventionData(new InventionData(gizmos));
					player.getInventory().deleteItem(36719 + (gizmoType * 2), 1);
					player.getInventory().addItem(gizmo);
					player.getSkills().addXp(Skills.INVENTION, xp);
					player.getPackets().sendExecuteScript(12192, perks[0].getId(), perks[0].getRank(), perks[1] == null ? 0 : perks[1].getId(), perks[1] == null ? 0 : perks[1].getRank(), (int) (xp * 10), amountRepeats, player.getInventory().getAmountOf(36719 + (gizmoType * 2)));
				}
				refreshMaterials();
			}
		});
	}

	public boolean disolveGizmo(Item used, Item usedWith) {
		final Item disolver = used.getId() == 36726 ? used : usedWith.getId() == 36726 ? usedWith : null;
		final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
		if (disolver == null || augmentedItem == null)
			return false;
		if (augmentedItem.getInventionData() == null)
			return false;
		if (augmentedItem.getDefinitions().getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
			return false;
		int gizmoCount = augmentedItem.getInventionData().getGizmosCount();
		if (gizmoCount == 0) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>This item does not have any gizmos installed.", 0, -120, 0);
			return true;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {
			private int selectedIndex;

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				switch (stage) {
				case -1:
					switch (componentId) {
					case OPTION_1:
					case OPTION_2:
						sendConfirmGizmo(componentId == OPTION_2 ? 1 : 0);
						break;
					case OPTION_3:
						end();
						break;
					}
					break;
				case 0:
					if (componentId == 26) {
						augmentedItem.getInventionData().getGizmos()[selectedIndex] = null;
						player.getInventory().deleteItem(36726, 1);
						player.getInventory().refresh();
						player.getPackets().sendExecuteScript(1211, "You successfully remove the gizmo", 0, -120, 0);

					}
					end();
					break;
				}
			}

			public void sendConfirmGizmo(int index) {
				stage = 0;
				ItemDefinitions defs = augmentedItem.getDefinitions();
				selectedIndex = index;
				player.getInterfaceManager().removeDialogueInterface();
				player.getInterfaceManager().sendCentralInterface(1048);
				player.getPackets().sendExecuteScript(9727, "Remove Gizmo #" + (index + 1), "You are about to remove Gizmo #" + (index + 1) + " from: " + defs.getName() + "<br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
			}

			@Override
			public void start() {
				Gizmo[] gizmos = augmentedItem.getInventionData().getGizmos();
				if (gizmoCount == 1) {
					sendConfirmGizmo(gizmos[0] == null ? 1 : 0);
					return;
				}
				sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Dissolve Gizmo #1", "Dissolve Gizmo #2", "Nevermind.");
			}
		});
		return true;
	}
	public boolean disolveItem(Item used, Item usedWith) {
		final Item disolver = used.getId() == 36961 ? used : usedWith.getId() == 36961 ? usedWith : null;
		final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
		if (disolver == null || augmentedItem == null)
			return false;
		if (augmentedItem.getInventionData() == null)
			return false;
		ItemDefinitions defs = augmentedItem.getDefinitions();
		if (defs.getUnAugmentedItemId() == 0)
			return false;
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				if (componentId == 26) {
					ItemDefinitions defs = augmentedItem.getDefinitions();
					int originalId = defs.getName().toLowerCase().contains("augmented") ? defs.getUnAugmentedItemId() : augmentedItem.getId();
					augmentedItem.setId(originalId);
					augmentedItem.setInventionData(null);
					player.getInventory().deleteItem(36961, 1);
					player.getInventory().refresh();
					player.getPackets().sendExecuteScript(1211, "You remove the augmentation from: " + defs.getName() + ", reverting the item back to: " + ItemDefinitions.getItemDefinitions(originalId).getName(), 0, -120, 0);

				}
				end();
			}

			@Override
			public void start() {
				ItemDefinitions defs = augmentedItem.getDefinitions();
				int originalId = defs.getUnAugmentedItemId();
				player.getInterfaceManager().sendCentralInterface(1048);
				player.getPackets().sendExecuteScript(9727, "Remove Augmentation", "You are about to remove an augmentation from: " + defs.getName() + "<br><br>This will change into: " + ItemDefinitions.getItemDefinitions(originalId).getName() + "<br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
			}

		});
		return true;
	}
	public boolean dissolveEquipment(Item used, Item usedWith) {
		final Item disolver = used.getId() == 36728 ? used : usedWith.getId() == 36728 ? usedWith : null;
		final Item augmentedItem = disolver == null ? null : disolver == used ? usedWith : used;
		if (disolver == null || augmentedItem == null)
			return false;
		if (augmentedItem.getInventionData() == null)
			return false;
		if (augmentedItem.getDefinitions().getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
			return false;
		int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
		if (gizmosCount == 0) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>This item does not have any gizmos installed.", 0, -120, 0);
			return true;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				if (componentId == 26) {
					ItemDefinitions defs = augmentedItem.getDefinitions();
					int augmentedItemSlot = player.getInventory().getItemSlot(augmentedItem);
					if (augmentedItemSlot == -1) {
						end();
						return;
					}
					int itemCategory = defs.itemCategory;
					int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
					int gizmoId = 36720 + 2 * augmentedItemType;
					player.getInventory().getItems().set(augmentedItemSlot, null);
					player.getInventory().deleteItem(36728, 1);
					for (int i = 0; i < augmentedItem.getInventionData().getGizmos().length; i++) {
						if (augmentedItem.getInventionData().getGizmos()[i] != null) {
							Item gizmo = new Item(gizmoId, 1);
							gizmo.setInventionData(new InventionData(augmentedItem.getInventionData().getGizmos()[i]));
							player.getInventory().addItem(gizmo);
						}
					}
					player.getInventory().refresh();
					player.getPackets().sendExecuteScript(1211, "You successfully extract the gizmos.", 0, -120, 0);
				}
				end();
			}

			@Override
			public void start() {
				ItemDefinitions defs = augmentedItem.getDefinitions();
				player.getInterfaceManager().sendCentralInterface(1048);
				player.getPackets().sendExecuteScript(9727, "Dissolve Equipment", "You are about to destroy: " + defs.getName() + "<br><br>This will Destroy the item and return all installed gizmos. <br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
			}

		});
		return true;
	}
	public int getAfterShockDamage() {
		return afterShockDamage;
	}

	public int getContainerChargesFilled() {
		return containerChargesFilled;
	}

	public long getCracklingCooldown() {
		return cracklingCooldown;
	}

	public int getDivineCharges() {
		return divineCharges;
	}

	public int getDivineChargesStored() {
		return divineChargesStored;
	}

	public double getDrainReductionModifier() {
		if (hasDiscoveredBluePrint(53))
			return 0.80;
		if (hasDiscoveredBluePrint(52))
			return 0.83;
		if (hasDiscoveredBluePrint(51))
			return 0.86;
		if (hasDiscoveredBluePrint(50))
			return 0.88;
		if (hasDiscoveredBluePrint(49))
			return 0.91;
		if (hasDiscoveredBluePrint(48))
			return 0.93;
		if (hasDiscoveredBluePrint(47))
			return 0.95;
		if (hasDiscoveredBluePrint(46))
			return 0.97;
		if (hasDiscoveredBluePrint(45))
			return 0.99;
		return 1;
	}

	public int getEmptyDivineChargesStored() {
		return emptyDivineChargesStored;
	}

	public int getMaterialCount(int materialId) {
		int count = 0;
		for (int i = 0; i < addedMaterials.length; i++) {
			if (addedMaterials[i] == materialId)
				count++;
		}
		return count;
	}

	public int[] getMaterials() {
		return materials;
	}

	public int getMaxDivineCharges() {
		if (!hasDiscoveredBluePrint(54))
			return 600000000;
		if (!hasDiscoveredBluePrint(55))
			return 750000000;
		if (!hasDiscoveredBluePrint(56))
			return 900000000;
		if (!hasDiscoveredBluePrint(57))
			return 1050000000;
		if (!hasDiscoveredBluePrint(58))
			return 1200000000;
		return 1500000000;
	}

	private Object[][] getPossiblePerks() {
		Map<Integer, Object[]> possiblePerks = new HashMap<Integer, Object[]>();
		List<Integer> checkedMaterialIds = new ArrayList<Integer>();
		for (int i = 0; i < addedMaterials.length; i++) {
			int materialId = addedMaterials[i];
			if (materialId == -1 || materialId == 75 || checkedMaterialIds.contains(materialId))
				continue;
			MaterialData data = MaterialsData.getMaterialData(materialId);
			int materialCount = getMaterialCount(materialId);
			for (PossiblePerk perk : data.getPossiblePerks()) {
				if (!perk.getAllowedGizmoTypes()[gizmoType] || perk.getPossibleRanks()[materialCount - 1][0] <= 0)
					continue;
				if (possiblePerks.containsKey(perk.getPerkId())) {
					int[] OldPossibleRanks = (int[]) possiblePerks.get(perk.getPerkId())[1];
					int[] newPossibleRanks = perk.getPossibleRanks()[materialCount - 1];
					int[] rank = new int[OldPossibleRanks.length == 1 && newPossibleRanks.length == 1 ? 1 : 2];
					rank[0] = Math.max(OldPossibleRanks[0], newPossibleRanks[0]);
					if (rank.length > 1)
						rank[1] = Math.max(OldPossibleRanks.length == 1 ? 0 : OldPossibleRanks[1], newPossibleRanks.length == 1 ? 0 : newPossibleRanks[1]);
					possiblePerks.put(perk.getPerkId(), new Object[] { perk.getPerkId(), rank });
				} else
					possiblePerks.put(perk.getPerkId(), new Object[] { perk.getPerkId(), perk.getPossibleRanks()[materialCount - 1] });
			}
			checkedMaterialIds.add(materialId);
		}
		Map<Integer, Object[]> treeMap = new TreeMap<Integer, Object[]>(possiblePerks);
		Object[][] objects = new Object[treeMap.size()][];
		for (int i = 0; i < objects.length; i++) {
			objects[i] = treeMap.values().toArray(new Object[treeMap.values().size()][])[i];
		}
		return objects;
	}

	public double getTotalEquipedItemsDrainRate() {
		double drainRate = 0.00;
		for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
			Item item = player.getEquipment().getItems().get(i);
			if (item != null && item.getInventionData() != null && !item.hasGizmo())
				drainRate += ItemConstants.getAugmentedItemDrainRate(player, item);
		}
		return drainRate;
	}

	public Item getUnaugmentedItem(Item item) {
		if (item.getInventionData() == null)
			return item;
		if (item.getId() == 36720 || item.getId() == 36722 || item.getId() == 36724) {
			item.setId(item.getId() - 1);
			item.setInventionData(null);
			return item;
		}
		ItemDefinitions defs = item.getDefinitions();
		if (defs.getUnAugmentedItemId() == 0)
			return item;
		int originalId = defs.getUnAugmentedItemId();
		item.setId(originalId);
		item.setInventionData(null);
		return item;
	}

	public void handleInterface(int interfaceId, int componentId, int slotId, int slotId2) {
		if (interfaceId == 1708) {
			if (componentId >= 43 && componentId <= 52) {
				if (currentBluePrint == null)
					return;
				currentBluePrint.addModul(componentId - 42);
			} else if (componentId >= 23 && componentId <= 27) {
				if (currentBluePrint == null)
					return;
				if (currentBluePrint.getStage() == 2)
					currentBluePrint.addModul(componentId - 22);
				else
					currentBluePrint.removeModul(componentId - 23);
			} else if (componentId >= 14 && componentId <= 18) {
				int index = componentId - 14;
				int selectedIndex = currentBluePrint.getSelectedIndex();
				if (selectedIndex != -1) {
					currentBluePrint.switchModul(index, selectedIndex);
					currentBluePrint.setSelectedIndex(-1);
					return;
				}
				currentBluePrint.setSelectedIndex(index);
			} else if (componentId == 33) {
				if (currentBluePrint != null && currentBluePrint.getBluePrintId() == slotId)
					return;
				currentBluePrint = new BluePrint(slotId).start();
			} else if (componentId == 80) {
				currentBluePrint.createPrototype();
			} else if (componentId == 103) {
				currentBluePrint.invent();
			}
		} else if (interfaceId == 1530) {
			if (currentBluePrint == null)
				return;
			if (currentBluePrint.getStage() == 3) {
				player.getPackets().closeInterface(1530);
				player.getInterfaceManager().setInterface(false, 1708, 71, 0);
				player.getPackets().sendHideIComponent(1708, 71, true);
				player.getPackets().sendExecuteScript(12130, -1, 0, "", 0, 0);
				player.getPackets().sendExecuteScript(12116);
				currentBluePrint = null;
				return;
			}
			if (componentId == 34 || componentId == 18) {
				player.getPackets().closeInterface(1530);
				player.getInterfaceManager().setInterface(false, 1708, 71, 0);
				player.getPackets().sendHideIComponent(1708, 71, true);
				currentBluePrint.setStage(2);
			} else if (componentId == 28) {
				currentBluePrint.setStage(3);
				currentBluePrint.invent();
			}
		} else if (interfaceId == 1712) {
			if (componentId == 2) {
				clearMaterials();
			} else if (componentId == 3) {
				int index = slotId == 4 ? 0 : slotId == 1 ? 1 : slotId == 3 ? 2 : slotId == 7 ? 4 : 3;
				removeMaterial(index);
			} else if (componentId == 6) {
				addMaterial(slotId);
			} else if (componentId == 24 || componentId == 41) {
				createGizmo();
			} else if (componentId == 58) {
				openAddMaterialsInterface(36719 + (gizmoType * 2));
			}
		}
	}

	public void handleSwitchComponents(int fromInterfaceId, int fromComponentId, int toInterfaceId, int toComponentId, int fromSlot, int toSlot) {
		if (fromInterfaceId == 1708 && toInterfaceId == 1708) {
			if (currentBluePrint == null)
				return;
			if (fromComponentId >= 43 && fromComponentId <= 52 && toComponentId >= 23 && toComponentId <= 27) {
				currentBluePrint.setModul(toComponentId - 23, fromComponentId - 42);
			} else if (fromComponentId == 29 || fromComponentId >= 116 && fromComponentId <= 122) {
				int fromIndex = fromComponentId == 29 ? 0 : fromComponentId == 116 ? 1 : fromComponentId == 118 ? 2 : fromComponentId == 120 ? 3 : 4;
				if (toComponentId >= 23 && toComponentId <= 27 && currentBluePrint.getStage() == 1) {
					currentBluePrint.switchModul(fromIndex, toComponentId - 23);
				} else if (toComponentId == 42 && currentBluePrint.getStage() == 1) {
					currentBluePrint.removeModul(fromIndex);
				} else if (toComponentId >= 14 && toComponentId <= 18) {
					currentBluePrint.setModul(toComponentId - 14, fromIndex + 1);
				}
			} else if (fromComponentId >= 14 && fromComponentId <= 18 && toComponentId >= 14 && toComponentId <= 18) {
				currentBluePrint.switchModul(toComponentId - 14, fromComponentId - 14);
			}

		} else if (fromInterfaceId == 1712 && toInterfaceId == 1712) {
			if (fromComponentId == 6 && toComponentId == 3) {
				int index = toSlot == 4 ? 0 : toSlot == 1 ? 1 : toSlot == 3 ? 2 : toSlot == 7 ? 4 : toSlot == 5 ? 3 : -1;
				if (index == -1) {
					interactionIndex++;
					refreshAddMaterialsInterface();
					return;
				}
				addMaterial(index, fromSlot, true);
			} else if (fromComponentId == 3 && toComponentId == 3) {
				int fromIndex = fromSlot == 4 ? 0 : fromSlot == 1 ? 1 : fromSlot == 3 ? 2 : fromSlot == 7 ? 4 : 3;
				int toIndex = toSlot == 4 ? 0 : toSlot == 1 ? 1 : toSlot == 3 ? 2 : toSlot == 7 ? 4 : toSlot == 5 ? 3 : -1;
				if (toIndex == -1) {
					removeMaterial(fromIndex);
					return;
				}
				switchMaterials(fromIndex, toIndex);
			}
		}
	}

	public boolean hasChargePack() {
		return hasChargePack;
	}

	public boolean hasDiscoveredBluePrint(int index) {
		return discoveredBluePrints[index];
	}

	public Perk hasPerk(Perks perks) {
		if (divineCharges == 0)
			return null;
		int id = perks.getId();
		int rank = -1;
		boolean hasIncreasedChance = false;
		for (int i = 0; i < player.getEquipment().getItems().getItems().length; i++) {
			Item item = player.getEquipment().getItems().get(i);
			if (item == null || item.getInventionData() == null)
				continue;
			for (Gizmo gizmo : item.getInventionData().getGizmos()) {
				if (gizmo == null)
					continue;
				for (Perk perk : gizmo.getPerks()) {
					if (perk == null || perk.getId() != id)
						continue;
					if (perk.getRank() > rank) {
						rank = perk.getRank();
						hasIncreasedChance = perks.hasIncreasedChance() && InventionData.getItemLevel(player, item) == 20;
					}
				}
			}
		}
		Perk perk = rank == -1 ? null : new Perk(id, rank);
		if (perk != null)
			perk.setIncreasedChance(hasIncreasedChance);
		return perk;
	}

	public void increaseChargesFilled(int ordinal, int amount) {
		if (emptyDivineChargesStored == 0)
			return;
		int increase = (10 + (ordinal * 27)) * amount;
		int before = containerChargesFilled / 300;
		this.containerChargesFilled = (containerChargesFilled + increase) / 300 >= 100 ? 30000 : (containerChargesFilled + increase);
		int after = containerChargesFilled / 300;
		player.getEquipment().refresh();
		if (after == 100) {
			containerChargesFilled = 0;
			emptyDivineChargesStored--;
			divineChargesStored++;
			player.getPackets().sendGameMessage("<col=00ff00>Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " has successfully collected enough divine charge to fill a container.");
		} else {
			if (before < 25 && after >= 25) {
				player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 25% full.");
			}
			if (before < 50 && after >= 50) {
				player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 50% full.");
			}
			if (before < 75 && after >= 75) {
				player.getPackets().sendGameMessage("<col=FF8C00>An empty divine charge container is 75% full.");
			}
		}
	}

	public void init() {
		player.getVarsManager().sendVarBit(30223, 105);// unlocks invention
		// tutorial
		ClientScriptMap map = ClientScriptMap.getMap(10743);
		if (!resetCharges) {
			divineCharges = 300000000;// 100,000 divine charge
			resetCharges = true;
		}
		if (discoveredBluePrints == null)
			discoveredBluePrints = new boolean[map.getSize()];
		if (materials == null)
			materials = new int[ClientScriptMap.getMap(10742).getSize()];
		if (discoveredBluePrints.length != map.getSize()) {
			boolean[] newBluePrints = new boolean[map.getSize()];
			for (int i = 0; i < discoveredBluePrints.length; i++)
				newBluePrints[i] = discoveredBluePrints[i];
			this.discoveredBluePrints = newBluePrints;
		}
		int[] autoDiscovered = { 81, 80, 82, 30, 79, 21 };
		for (int auto : autoDiscovered)
			discoveredBluePrints[auto] = true;
		for (int i = 0; i < discoveredBluePrints.length; i++) {
			if (discoveredBluePrints[i]) {
				ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
				int dataId = bluePrintsMap.getIntValue(i);
				InventionDefinitions def = InventionDefinitions.getData(dataId);
				player.getPackets().sendExecuteScript(12060, ((int) def.getDataInIndex(0)));
			}
		} 
		player.getVarsManager().sendVarBit(30225, 1);// unlocks charge pack
		player.getVarsManager().sendVarBit(30224, 1); // unlocks inventor tool
														// and bag of materials
		refreshDivineCharges();// charges in charge pack
		refreshEquipedItemsDrainRate();
		refreshMaterials();
		resetAugmentedItems();
	}

	public boolean installGizmo(Item used, Item usedWith) {
		final Item gizmo = (used.getId() == 36720 || used.getId() == 36722 || used.getId() == 36724) ? used : (usedWith.getId() == 36720 || usedWith.getId() == 36722 || usedWith.getId() == 36724) ? usedWith : null;
		final Item augmentedItem = gizmo == null ? null : gizmo == used ? usedWith : used;
		if (gizmo == null || augmentedItem == null)
			return false;
		if (gizmo.getInventionData() == null || augmentedItem.getInventionData() == null)
			return false;

		if (augmentedItem.getDefinitions().getUnAugmentedItemId() == 0 && augmentedItem.getDefinitions().getAugmentedItemId() == 0)
			return false;
		ItemDefinitions defs = augmentedItem.getDefinitions();
		int itemCategory = defs.itemCategory;
		int gizmoType = (gizmo.getId() - 36720) / 2;
		int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
		if (gizmoType != augmentedItemType) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You can only use that gizmo on augmented " + (gizmoType == 2 ? "tools" : gizmoType == 1 ? "armour" : "weapons") + ".", 0, -120, 0);
			return true;
		}
		int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
		boolean twoHanded = Equipment.isTwoHandedWeapon(augmentedItem);
		int equipSlot = defs.getEquipSlot();
		int maxGizmos = ((!twoHanded && (equipSlot == Equipment.SLOT_WEAPON || equipSlot == Equipment.SLOT_SHIELD))) ? 1 : 2;
		Gizmo[] gizmos = augmentedItem.getInventionData().getGizmos();
		if (gizmosCount >= maxGizmos) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>You can't install more than " + (maxGizmos == 2 ? "two gizmos" : "one gizmo") + " to this item.", 0, -120, 0);
			return true;
		}
		int gizmoSlot = player.getInventory().getItemSlot(gizmo);
		if (gizmoSlot == -1)
			return false;
		player.getInventory().getItems().set(gizmoSlot, null);
		for (int i = 0; i < gizmos.length; i++) {
			if (gizmos[i] == null) {
				gizmos[i] = gizmo.getInventionData().getGizmos()[0];
				break;
			}
		}
		player.getInventory().refresh();
		player.getPackets().sendExecuteScript(1211, "You successfully install the gizmo", 0, -120, 0);
		return true;
	}

	public boolean isConvertMemories() {
		return convertMemories;
	}

	public void onEquipmentChange() {
		if (hasPerk(Perks.AFTERSHOCK) == null)
			afterShockDamage = 0;
	}

	public void openAddMaterialsInterface(int itemId) {
		if (itemId != 36719 && itemId != 36721 && itemId != 36723) {
			return;
		}
		gizmoType = (itemId - 36719) / 2;
		addedMaterials = new int[5];
		possiblePerks = new Object[8][2];
		Arrays.fill(addedMaterials, -1);
		interactionIndex = 0;
		refreshMaterials();
		player.getInterfaceManager().sendFullScreenInterface(1712);
		player.getPackets().sendExecuteScript(12171, itemId);
		player.getPackets().sendIComponentSettings(1712, 3, 0, 8, 2621470);
		player.getPackets().sendIComponentSettings(1712, 6, 0, 75, 786462);
		refreshAddMaterialsInterface();
	}

	public void openAnalysisInterface(int itemId) {
		if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsBankInterface()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
			return;
		}
		int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
		for (int skillId : skillIds) {
			if (player.getSkills().getLevelForXp(skillId) < 80) {
				player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
				return;
			}
		}
		ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
		ItemDisassemblyData data = ItemsDisassemblyData.getItemDisassemblyData(itemId);
		if (data == null) {
			data = ItemsDisassemblyData.getItemDisassemblyData(defs.certId);
			if (data == null) {
				player.getPackets().sendGameMessage("You can't disassemble this item.");
				return;
			}
		}
		player.stopAll();
		player.getInterfaceManager().sendCentralInterface(1048);
		String materials = "";
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		int count = 0;
		if (data.getSpecialMaterial() != null)
			materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(data.getSpecialMaterial()[0])) + "</col> (Always)<br>";
		if (data.getRareMaterials() != null) {
			for (int i : data.getRareMaterials()) {
				if (count >= 10)
					continue;
				materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(i)) + "</col> (Rarely)<br>";
				count++;
			}
		}
		if (data.getSometimesMaterials() != null) {
			for (int i : data.getSometimesMaterials()) {
				if (count >= 10)
					continue;
				materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(i)) + "</col> (Sometimes)<br>";
				count++;
			}
		}
		if (data.getOftenMaterials() != null) {
			for (int i : data.getOftenMaterials()) {
				if (count >= 10)
					continue;
				materials += "* <col=ffffff>" + InventionDefinitions.getDataName(map.getIntValue(i)) + "</col> (Often)<br>";
				count++;
			}
		}
		if (count >= 10) {
			materials += "* ... and other materials.";
		}
		player.getPackets().sendExecuteScript(9727, "Material analysis", "<col=ffff00>" + defs.getName() + "</col><br><br>Junk chance: <col=ffffff>" + data.getJunkChance() + "%</col><br>Chances for materials: <col=ffffff>" + data.getMaterialCount() + "</col>" + (data.getSpecialMaterial() != null ? " (excludes special)" : "") + "<br><br>This may disassemble into:<br>" + materials, "Ok", "", "", -1);
	}

	public void openBagOfMaterialsInterface() {
		if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsBankInterface()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
			return;
		}
		int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
		for (int skillId : skillIds) {
			if (player.getSkills().getLevelForXp(skillId) < 80) {
				player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
				return;
			}
		}
		refreshMaterials();
		player.stopAll();
		player.getInterfaceManager().sendCentralInterface(1709);
	}

	public void openCheckPerksInterface(Item item) {
		int itemId = item.getId();
		if (itemId != 36720 && itemId != 36722 && itemId != 36724) {
			return;
		}
		if (item.getInventionData() == null || item.getInventionData().getGizmos() == null) {
			player.getPackets().sendGameMessage("This item doesn't have perks!");
			return;
		}
		player.getInterfaceManager().sendCentralInterface(1713);
		Perk[] perks = item.getInventionData().getGizmos()[0].getPerks();
		if (perks == null) {
			player.getPackets().sendGameMessage("This item doesn't have perks!");
			return;
		}
		player.getPackets().sendExecuteScript(12170, item.getId(), perks[0].getId(), perks[0].getRank(), perks[1] == null ? 0 : perks[1].getId(), perks[1] == null ? 0 : perks[1].getRank());
	}

	public void openDiscoveryInterface() {
		if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsBankInterface()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
			return;
		}
		int[] skillIds = { Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING };
		for (int skillId : skillIds) {
			if (player.getSkills().getLevelForXp(skillId) < 80) {
				player.getPackets().sendGameMessage("You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
				return;
			}
		}
		player.stopAll();
		player.getInterfaceManager().sendFullScreenInterface(1708);
		player.getPackets().sendUnlockIComponentOptionSlots(1708, 33, 0, ClientScriptMap.getMap(10743).getSize() - 1, 0, 1);
		player.getPackets().sendExecuteScript(12121);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				currentBluePrint = null;
				player.getInterfaceManager().setInterface(false, 1708, 71, 0);
				player.getPackets().sendHideIComponent(1708, 71, true);
				player.getInterfaceManager().closeFullScreenInterface();
			}
		});
	}

	public void processCombatDrain() {
		double drainRate = getTotalEquipedItemsDrainRate();
		if (drainRate > 0) {
			if ((drainCoolDown == 0 || Utils.currentTimeMillis() >= drainCoolDown) && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
				drainCoolDown = Utils.currentTimeMillis() + 6000;
				double drain = (6.00 * drainRate * 3000.00);
				divineCharges = (int) (divineCharges - drain <= 0 ? 0 : divineCharges - drain);
				refreshDivineCharges();
			}
		}
	}

	public void processCombatXp(Hit hit, NPC npc) {
		if (npc == null || npc.getXp() == 0 || hit == null || hit.getDamage() == 0)
			return;
		if (divineCharges == 0)
			return;
		int damage = hit.getDamage() > npc.getHitpoints() ? npc.getHitpoints() : hit.getDamage();
		if (damage == 0)
			return;
		double damageRatio = damage > npc.getMaxHitpoints() ? 1.00 : ((double) damage / (double) npc.getMaxHitpoints());
		double totalXP = npc.getXp() * (damageRatio);
		if (totalXP == 0)
			return;
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null || item.getInventionData() == null)
				continue;
			ItemDefinitions defs = item.getDefinitions();
			if (defs == null)
				continue;
			String name = defs.getName().toLowerCase();
			boolean tool = name.contains("augmented dragon hatchet") || name.contains("augmented dragon pickaxe") || name.contains("augmented crystal pickaxe") || name.contains("augmented crystal hatchet") || name.contains("augmented crystal fishing rod") || name.contains("augmented crystal tinderbox") || name.contains("augmented crystal hammer") || name.contains("Augmented Tavia's fishing rod") || name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron");
			if (tool)
				continue;
			boolean twoHanded = Equipment.isTwoHandedWeapon(item);
			double xp = (twoHanded ? 0.06 : defs.getEquipSlot() == Equipment.SLOT_SHIELD ? 0.02 : 0.04) * totalXP;
			int levelBefore = InventionData.getItemLevel(player, item);
			item.getInventionData().setXp((item.getInventionData().getXp() + xp));
			int levelafter = InventionData.getItemLevel(player, item);
			if (levelafter > levelBefore)
				player.getPackets().sendGameMessage("<col=FFFF00>Congratulations! Your " + item.getName() + " has gained a level! It is now level " + levelafter);
		}
		player.getEquipment().refresh();
	}

	private void refreshAddedMaterials() {
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		int[] materialsFieldIds = new int[5];
		for (int i = 0; i < materialsFieldIds.length; i++) {
			materialsFieldIds[i] = addedMaterials[i] == -1 ? -1 : map.getIntValue(addedMaterials[i]);
		}
		for (int i = 0; i < 9; i++) {
			player.getVarsManager().sendVar(6079 + i, i >= materialsFieldIds.length ? -1 : materialsFieldIds[i]);
		}
		player.getPackets().sendExecuteScript(12173, interactionIndex, -1, materialsFieldIds[1], -1, materialsFieldIds[2], materialsFieldIds[0], materialsFieldIds[3], -1, materialsFieldIds[4], -1);
	}

	private void refreshAddMaterialsInterface() {
		refreshAddedMaterials();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				refreshPossiblePerks();
			}
		});
	}

	private void refreshDivineCharges() {
		player.getVarsManager().sendVar(5984, divineCharges);
		player.getCombatDefinitions().refreshBonuses();
	}

	public void refreshEquipedItemsDrainRate() {
		double drainRate = getTotalEquipedItemsDrainRate();
		player.getVarsManager().sendVar(5991, (int) (drainRate * 1800));
		onEquipmentChange();
	}
	public void refreshMaterials() {
		for (int i = 0; i < materials.length; i++) {
			player.getVarsManager().sendVar(MATERIALS_VARS[i], materials[i]);
		}
	}
	private void refreshPossiblePerks() {
		possiblePerks = getPossiblePerks();
		int[] values = new int[8];
		for (int i = 0; i < 8; i++) {
			if (i >= possiblePerks.length) {
				values[i] = 0;
				continue;
			}
			Object[] perk = possiblePerks[i];
			int perkId = (int) perk[0];
			int[] possibleRanks = (int[]) perk[1];
			int firstRank = possibleRanks[0];
			int secondRank = possibleRanks.length == 1 ? 0 : possibleRanks[1];
			values[i] = ((firstRank + 256 * secondRank) << 16 | perkId);
		}
		player.getPackets().sendExecuteScript(12188, interactionIndex, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
	}
	public void removeMaterial(int index) {
		interactionIndex++;
		addedMaterials[index] = -1;
		refreshAddMaterialsInterface();
	}

	private void resetAugmentedItems() {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item == null)
				continue;
			ItemDefinitions defs = item.getDefinitions();
			if (defs == null)
				continue;
			if (item.getId() == 33429) {
				item.setId(33430);
			}
			if (!item.getDefinitions().isNoted() && item.getDefinitions().getName().toLowerCase().contains("augmented") && item.getInventionData() == null) {
				item.setInventionData(new InventionData(0));
			}
		}
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue;
			ItemDefinitions defs = item.getDefinitions();
			if (defs == null)
				continue;
			if (item.getId() == 33429) {
				item.setId(33430);
			}
			if (item.getDefinitions().getName().toLowerCase().contains("augmented") && item.getInventionData() == null) {
				item.setInventionData(new InventionData(0));
			}
		}
	}

	public void resetCombatDrain() {
		drainCoolDown = 0;
	}

	public boolean seperateEquipment(Item used, Item usedWith) {
		final Item seperator = used.getId() == 41079 ? used : usedWith.getId() == 41079 ? usedWith : null;
		final Item augmentedItem = seperator == null ? null : seperator == used ? usedWith : used;
		if (seperator == null || augmentedItem == null)
			return false;
		if (augmentedItem.getInventionData() == null)
			return false;
		if (augmentedItem.hasGizmo())
			return false;
		int gizmosCount = augmentedItem.getInventionData().getGizmosCount();
		if (gizmosCount == 0) {
			player.getPackets().sendExecuteScript(1211, "<col=ff0000>This item does not have any gizmos installed.", 0, -120, 0);
			return true;
		}
		int itemLevel = InventionData.getItemLevel(player, augmentedItem);
		if (itemLevel < 15) {
			player.getPackets().sendGameMessage("Items below level 15 cannot be seperated.");
			return true;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				if (componentId == 26) {
					ItemDefinitions defs = augmentedItem.getDefinitions();
					int itemCategory = defs.itemCategory;
					int augmentedItemType = (itemCategory == 35 || itemCategory == 67) ? 2 : (itemCategory == 7 || itemCategory == 8 || itemCategory == 18 || itemCategory == 10) ? 1 : 0;
					int gizmoId = 36720 + 2 * augmentedItemType;
					boolean consumeSeperator = itemLevel >= 19 ? (Math.random() <= 0.50) : itemLevel >= 17 ? (Math.random() <= 0.75) : true;
					if (consumeSeperator)
						player.getInventory().deleteItem(41079, 1);
					for (int i = 0; i < augmentedItem.getInventionData().getGizmos().length; i++) {
						if (augmentedItem.getInventionData().getGizmos()[i] != null) {
							Item gizmo = new Item(gizmoId, 1);
							gizmo.setInventionData(new InventionData(augmentedItem.getInventionData().getGizmos()[i]));
							player.getBankManager().addItem(gizmo, true);
							augmentedItem.getInventionData().getGizmos()[i] = null;
						}
					}
					player.getInventory().refresh();
					player.getPackets().sendExecuteScript(1211, "You successfully seperate the gizmos from your item.", 0, -120, 0);
					player.getPackets().sendGameMessage("Your gizmos have been added to your bank.");
					if (!consumeSeperator)
						player.getPackets().sendGameMessage("Your seperator wasn't consumed in the process.", true);
				}
				end();
			}

			@Override
			public void start() {
				ItemDefinitions defs = augmentedItem.getDefinitions();
				player.getInterfaceManager().sendCentralInterface(1048);
				player.getPackets().sendExecuteScript(9727, "Seperate Equipment", "You are about to seperate the gizmos from: " + defs.getName() + "<br><br>This will return all installed gizmos. <br><br>This cannot be undone.<br><br>Do you wish to continue?", "OK", "Cancel", "", -1, 48);
			}

		});
		return true;
	}

	public void setAfterShockDamage(int afterShockDamage) {
		this.afterShockDamage = afterShockDamage;
	}

	public void setContainerChargesFilled(int containerChargesFilled) {
		this.containerChargesFilled = containerChargesFilled;
	}

	public void setConvertMemories(boolean convertMemories) {
		this.convertMemories = convertMemories;
	}

	public void setCracklingCooldown(long cracklingCooldown) {
		this.cracklingCooldown = cracklingCooldown;
	}

	public void setDivineCharges(int divineCharges) {
		this.divineCharges = divineCharges;
		refreshDivineCharges();
	}

	public void setDivineChargesStored(int divineChargesStored) {
		this.divineChargesStored = divineChargesStored;
	}

	public void setEmptyDivineChargesStored(int emptyDivineChargesStored) {
		this.emptyDivineChargesStored = emptyDivineChargesStored;
	}

	public void setHasChargePack(boolean hasChargePack) {
		this.hasChargePack = hasChargePack;
		player.getVarsManager().sendVarBit(30225, hasChargePack ? 1 : 0);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean siphonEquipment(Item used, Item usedWith) {
		final Item siphon = used.getId() == 36730 || used.getId() == 38872 ? used : usedWith.getId() == 36730 || usedWith.getId() == 38872 ? usedWith : null;
		final Item augmentedItem = siphon == null ? null : siphon == used ? usedWith : used;
		if (siphon == null || augmentedItem == null)
			return false;
		if (augmentedItem.getInventionData() == null)
			return false;
		if (augmentedItem.hasGizmo())
			return false;
		boolean crystalSiphon = siphon.getId() == 38872;
		if (crystalSiphon && !augmentedItem.getDefinitions().getName().toLowerCase().contains("crystal")) {
			player.getPackets().sendGameMessage("You can only use this to siphon crystal equipment.");
			return true;
		}
		int itemLevel = InventionData.getItemLevel(player, augmentedItem);
		if (itemLevel <= 3) {
			player.getPackets().sendGameMessage("Items below level 4 cannot be siphoned.");
			return true;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void finish() {

			}

			@Override
			public void run(int interfaceId, int componentId, int slotId) throws ClassNotFoundException {
				if (componentId == 26) {
					int baseXp = (int) Disassemble.getDisassembleXP(itemLevel - 2);
					int tier = crystalSiphon ? 90 : augmentedItem.getDefinitions().getCSOpcode(750);
					baseXp *= (1.00 + (1.50 * ((tier - 80) / 100.00)));
					player.getSkills().addXp(Skills.INVENTION, baseXp);
					boolean consumeSiphon = itemLevel >= 16 ? false : itemLevel >= 13 ? (Math.random() <= 0.5) : true;
					if (consumeSiphon)
						player.getInventory().deleteItem(siphon.getId(), 1);
					augmentedItem.getInventionData().setXp(0);
					player.getInventory().refresh();
					player.getPackets().sendExecuteScript(1211, "You successfully siphon the equipment.", 0, -120, 0);
					if (!consumeSiphon)
						player.getPackets().sendGameMessage("Your siphon wasn't consumed in the process.", true);
				}
				end();
			}

			@Override
			public void start() {
				player.getInterfaceManager().sendCentralInterface(1048);
				int baseXp = (int) Disassemble.getDisassembleXP(itemLevel - 2);
				int tier = crystalSiphon ? 90 : augmentedItem.getDefinitions().getCSOpcode(750);
				baseXp *= (1.00 + (1.50 * ((tier - 80) / 100.00)));
				player.getPackets().sendExecuteScript(9727, "SIPHON EQUIPMENT", "Are you sure you want to siphon your item?<br><br>It is currently level <col=ffffff>" + itemLevel + "</col>.<br><br><img=6>You will gain <col=ffffff>" + Utils.getFormattedNumber(baseXp) + "</col> Invention XP.<br><br><col=ff0000>if you siphon this, it's xp will be completly reset.", "SIPHON", "Cancel", "", -1, 48);
			}
		});
		return true;
	}

	public void switchMaterials(int fromIndex, int toIndex) {
		int temp = addedMaterials[fromIndex];
		interactionIndex++;
		addedMaterials[fromIndex] = addedMaterials[toIndex];
		addedMaterials[toIndex] = temp;
		refreshAddMaterialsInterface();
	}

	public void withdrawDivineCharges() {
		if (divineChargesStored == 0) {
			player.getPackets().sendGameMessage("Your " + ItemDefinitions.getItemDefinitions(41083).getName() + " " + (divineChargesStored == 0 ? "is empty" : "has " + divineChargesStored + " divine charges stored") + ".");
			return;
		}
		if (player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(36390, 1)) {
			player.getPackets().sendGameMessage("You don't have enough space in your inventory.");
			return;
		}
		player.getInventory().addItem(36390, divineChargesStored);
		divineChargesStored = 0;
		player.getPackets().sendGameMessage("Your divine charges are placed in your inventory.");
		player.getInventory().refresh();
		player.getEquipment().refresh();
	}

}
