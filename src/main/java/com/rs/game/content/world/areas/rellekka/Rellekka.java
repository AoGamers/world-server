// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.game.content.world.areas.rellekka;

import com.rs.engine.dialogue.Conversation;
import com.rs.engine.dialogue.Dialogue;
import com.rs.engine.dialogue.HeadE;
import com.rs.engine.dialogue.Options;
import com.rs.engine.quest.Quest;
import com.rs.game.content.PlayerLook;
import com.rs.game.content.achievements.AchievementDef;
import com.rs.game.content.achievements.AchievementDef.Area;
import com.rs.game.content.achievements.AchievementDef.Difficulty;
import com.rs.game.content.achievements.AchievementSystemD;
import com.rs.game.content.achievements.SetReward;
import com.rs.game.content.skills.magic.Magic;
import com.rs.game.content.skills.magic.TeleType;
import com.rs.game.content.world.AgilityShortcuts;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.Constants;
import com.rs.lib.game.Animation;
import com.rs.lib.game.Item;
import com.rs.lib.game.Tile;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.*;
import com.rs.utils.shop.ShopsHandler;

@PluginEventHandler
public class Rellekka {

	public static NPCClickHandler handleCouncilWorkman = new NPCClickHandler(new Object[] { 1287 }, e -> e.getPlayer().startConversation(new Conversation(e.getPlayer()) {
        {
            addNPC(e.getNPCId(), HeadE.CHEERFUL, "Hello, what can I do for you?");
            addOptions("What would you like to say?", new Options() {
                @Override
                public void create() {
                    option("About the Achievement System...", () -> new AchievementSystemD(player, e.getNPCId(), SetReward.FREMENNIK_BOOTS));
                }
            });
        }
    }));

	public static NPCClickHandler handleYrsa = new NPCClickHandler(new Object[] { 1301 }, e -> {
		switch(e.getOpNum()) {
		case 1:
			e.getPlayer().startConversation(new Conversation(e.getPlayer()) {
				{
					addNPC(e.getNPCId(), HeadE.CHEERFUL, "Welcome to my clothes shop. I can change your shoes, or I've got a fine selection of clothes for sale.");
					addOptions("What would you like to say?", new Options() {
						@Override
						public void create() {
							option("I'd like to buy some clothes.", () -> ShopsHandler.openShop(player, "yrsas_accoutrements"));
							option("I'd like to change my shoes.", () -> PlayerLook.openYrsaShop(player));
							option("About the Achievement System...", () -> new AchievementSystemD(player, e.getNPCId(), SetReward.FREMENNIK_BOOTS));
						}
					});
				}
			});
			break;
		case 3:
			ShopsHandler.openShop(e.getPlayer(), "yrsas_accoutrements");
			break;
		case 4:
			PlayerLook.openYrsaShop(e.getPlayer());
			break;
		}
	});

	public static ItemOnItemHandler handleStringLyre = new ItemOnItemHandler(3694, 3688, e -> {
		if (e.getPlayer().getSkills().getLevel(Constants.FLETCHING) < 25) {
			e.getPlayer().sendMessage("You need a Fletching level of 25 to string the lyre.");
			return;
		}
		Item branch = e.getUsedWith(3694);
		if (branch != null) {
			e.getPlayer().getInventory().deleteItem(3694, 1);
			branch.setId(3689);
			e.getPlayer().getInventory().refresh(branch.getSlot());
		}
	});

	public static ItemOnItemHandler handleCutLyre = new ItemOnItemHandler(946, new int[] { 3692 }, e -> {
		Item branch = e.getUsedWith(946);
		if (branch != null) {
			branch.setId(3688);
			e.getPlayer().getInventory().refresh(branch.getSlot());
			e.getPlayer().anim(6702);
		}
	});

	public static void rechargeLyre(Player player) {
		if (!player.isQuestComplete(Quest.FREMENNIK_TRIALS, "to charge a lyre."))
			return;
		if (!player.getInventory().containsItem(383, 1)) {
			player.sendMessage("The Fossegrimen is unresponsive.");
			return;
		}
		Item lyre = player.getItemWithPlayer(3689);
		if (lyre != null) {
			player.getInventory().deleteItem(383, 1);
			lyre.setId(3690);
			player.getInventory().refresh(lyre.getSlot());
			player.startConversation(new Conversation(player).addNPC(1273, HeadE.CHEERFUL, "I offer you this enchantment for your worthy offering."));
		} else {
			lyre = player.getItemWithPlayer(3690);
			if (lyre != null) {
				player.getInventory().deleteItem(383, 1);
				lyre.setId(6126);
				player.getInventory().refresh(lyre.getSlot());
				player.startConversation(new Conversation(player).addNPC(1273, HeadE.CHEERFUL, "I offer you this enchantment for your worthy offering."));
			} else
				player.sendMessage("You need a strung lyre with you to call upon the Fossegrimen.");
		}
	}

	public static ItemOnObjectHandler handleChargeLyre = new ItemOnObjectHandler(new Object[] { 4141 }, new Object[] { 383, 395, 363 }, e -> {
		rechargeLyre(e.getPlayer()); //TODO no way is this real in RS lmao
	});

	private static final int[] LYRE_IDS = { 3690, 3691, 6125, 6126, 6127, 14590, 14591 };

	private static int getLowerLyreId(int curr) {
		for (int i = 0;i < LYRE_IDS.length;i++)
			if (LYRE_IDS[i] == curr)
				return LYRE_IDS[i-1];
		return 3690;
	}

	private static void lyreTele(Player player, Tile loc, Item lyre, boolean reduceDaily) {
		Magic.sendTeleportSpell(player, 9600, -1, 1682, -1, 0, 0, loc, 5, true, TeleType.ITEM, () -> {
			if (reduceDaily) {
				player.setDailyB("freeLyreTele", true);
				return;
			}
			if (lyre != null) {
				lyre.setId(getLowerLyreId(lyre.getId()));
				player.getInventory().refresh();
			}
		});
	}
	
	public static Dialogue getLyreTeleOptions(Player player, Item item, boolean reduceDaily) {
		if (reduceDaily && player.getDailyB("freeLyreTele"))
			return new Dialogue().addNext(() -> player.sendMessage("You've already used your free teleport today."));
		return new Dialogue().addOptions("Where would you like to teleport?", ops -> {
			ops.add("Rellekka", () -> Rellekka.lyreTele(player, Tile.of(2643, 3676, 0), item, reduceDaily));
			if (AchievementDef.meetsRequirements(player, Area.FREMENNIK, Difficulty.HARD, false))
				ops.add("Waterbirth Island", () -> Rellekka.lyreTele(player, Tile.of(2547, 3757, 0), item, reduceDaily));
			if (AchievementDef.meetsRequirements(player, Area.FREMENNIK, Difficulty.ELITE, false)) {
				ops.add("Jatizso", () -> Rellekka.lyreTele(player, Tile.of(2407, 3803, 0), item, reduceDaily));
				ops.add("Neitiznot", () -> Rellekka.lyreTele(player, Tile.of(2336, 3803, 0), item, reduceDaily));
			}
		});
	}

	public static ItemClickHandler handleEnchantedLyre = new ItemClickHandler(new Object[] { 3690 }, new String[] { "Play" }, e -> e.getPlayer().sendMessage("The lyre is unresponsive. I should contact the Fossegrimen."));

	public static ItemClickHandler handleEnchantedLyreTeleports = new ItemClickHandler(new Object[] { 3691, 6125, 6126, 6127, 14590, 14591 }, new String[] { "Play" }, e -> e.getPlayer().startConversation(getLyreTeleOptions(e.getPlayer(), e.getItem(), false)));

	public static ObjectClickHandler handleLighthouseDoor = new ObjectClickHandler(new Object[] { 4577 }, e -> e.getPlayer().handleOneWayDoor(e.getObject(), e.getObject().getId()+1));

	public static ObjectClickHandler handleMountainCampWall = new ObjectClickHandler(new Object[] { 5847 }, e -> AgilityShortcuts.climbOver(e.getPlayer(), e.getPlayer().transform(e.getPlayer().getX() < e.getObject().getX() ? 2 : -2, 0, 0)));

	public static ObjectClickHandler handleKeldagrimEntrance = new ObjectClickHandler(new Object[] { 5008 }, e -> e.getPlayer().tele(Tile.of(2773, 10162, 0)));

	public static ObjectClickHandler handleKeldagrimExit = new ObjectClickHandler(new Object[] { 5014 }, e -> e.getPlayer().tele(Tile.of(2730, 3713, 0)));

	public static ObjectClickHandler handleLallisCave = new ObjectClickHandler(new Object[] { 4147 }, e -> e.getPlayer().startConversation(new Dialogue().addNPC(1270, HeadE.T_ANGRY, "Hey human! You not go in my house! It where me keep all my stuff!")));

	public static NPCClickHandler handleLallisConversation = new NPCClickHandler(new Object[] { 1270 }, e -> {
		//			e.getPlayer().startConversation(new Dialogue()
		//			.addPlayer(HeadE.CALM, "Hello there.")
		//			.addNPC(1270, HeadE.T_ANGRY, "Bah! Puny humans always try steal Lallis' golden apples! You go away now!")
		//			.addPlayer(HeadE.CONFUSED, "Uh.... Okay..."));
		e.getPlayer().startConversation(new Dialogue()
				.addPlayer(HeadE.CALM, "Hello there.")
				.addNPC(1270, HeadE.T_ANGRY, "Bah! Puny humans always try steal Lallis' golden apples! You go away now!")
				.addPlayer(HeadE.CONFUSED, "Oh no, I'm not here for golden apples, I was just wondering if I could have some golden fleece from the sheep over there.")
				.addNPC(1270, HeadE.T_CONFUSED, "Mmmm.. Here den, take some and leave Lalli alone!")
				.addItem(3693, "The troll hands you some golden fleece.", () -> e.getPlayer().getInventory().addItem(3693, 1)));
	});
}
