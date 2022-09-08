package com.rs.game.content.clans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.game.World;
import com.rs.game.content.skills.magic.Magic;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.npc.OwnedNPC;
import com.rs.game.model.entity.pathing.Direction;
import com.rs.game.model.entity.player.Player;
import com.rs.game.tasks.WorldTasks;
import com.rs.lib.game.Rights;
import com.rs.lib.game.WorldTile;
import com.rs.lib.model.clan.Clan;
import com.rs.lib.util.RSColor;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.events.ItemClickEvent;
import com.rs.plugin.events.NPCClickEvent;
import com.rs.plugin.events.ObjectClickEvent;
import com.rs.plugin.handlers.ItemClickHandler;
import com.rs.plugin.handlers.NPCClickHandler;
import com.rs.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class ClanVexillum extends OwnedNPC {
	
	private static Map<String, ClanVexillum> CLAN_VEXES = new ConcurrentHashMap<>();
	
	private Clan clan;
	
	/*
	 * anim skeleton 3606 = clan teleports
	 */

	private ClanVexillum(Player owner, WorldTile tile, Clan clan) {
		super(owner, 13634, tile, false);
		this.clan = clan;
		setAutoDespawnAtDistance(false);
		setIgnoreNPCClipping(true);
		setHidden(true);
		Direction faceDir = Direction.getDirectionTo(this, tile);
		if (faceDir == null)
			faceDir = Direction.fromAngle(owner.getFaceAngle());
		if (faceDir != null)
			setFaceAngle(faceDir.getAngle());
		CLAN_VEXES.put(clan.getName(), this);
	}
	
	public static NPCClickHandler interact = new NPCClickHandler(new Object[] { 13634 }) {
		@Override
		public void handle(NPCClickEvent e) {
			if (!(e.getNPC() instanceof ClanVexillum vex)) {
				e.getNPC().finish();
				return;
			}
			e.getNPC().resetDirection();
			if (e.getOption().equals("Remove")) {
				if (vex.getOwner() != e.getPlayer()) {
					e.getPlayer().sendMessage("This isn't your vexillum to remove.");
					return;
				}
				e.getPlayer().sendOptionDialogue("Would you like to pick up the vexillum?", ops -> {
					ops.add("Yes, pick it up.", () -> vex.finish());
					ops.add("No, leave it.");
				});
			} else {
				ClansManager.openClanDetails(e.getPlayer(), vex.getOwner(), vex.clan);
			}
		}
	};
	
	public static ItemClickHandler vexOps = new ItemClickHandler(new Object[] { 20709 }, new String[] { "Teleport", "Place", "Recall", "Find" }) {
		@Override
		public void handle(ItemClickEvent e) {
			if (e.getOption().equals("Teleport"))
				Magic.sendTeleportSpell(e.getPlayer(), 7389, 7312, 537, 538, 0, 0, new WorldTile(2960, 3285, 0), 4, true, Magic.MAGIC_TELEPORT, null);
			else if (e.getOption().equals("Place"))
				create(e.getPlayer(), true);
			else if (e.getOption().equals("Recall")) {
				if (e.getPlayer().getClan() == null) {
					e.getPlayer().sendMessage("Could not find a vexillum to recall.");
					return;
				}
				ClanVexillum vex = CLAN_VEXES.get(e.getPlayer().getClan().getName());
				if (vex == null) {
					e.getPlayer().sendMessage("Could not find a vexillum to recall.");
					return;
				}
				if (vex.getOwner() != e.getPlayer()) {
					e.getPlayer().sendMessage("This isn't your vexillum to remove.");
					return;
				}
				e.getPlayer().sendOptionDialogue("Would you like to pick up the vexillum?", ops -> {
					ops.add("Yes, pick it up.", () -> vex.finish());
					ops.add("No, leave it.");
				});
			} else if (e.getOption().equals("Find")) {
				if (e.getPlayer().getClan() == null) {
					e.getPlayer().sendMessage("Could not find your vexillum.");
					return;
				}
				ClanVexillum vex = CLAN_VEXES.get(e.getPlayer().getClan().getName());
				if (vex == null) {
					e.getPlayer().sendMessage("Could not find your vexillum.");
					return;
				}
				e.getPlayer().getHintIconsManager().addHintIcon(vex, 0, -1, false);
			}
		}
	};
	
	public static ObjectClickHandler plantStand = new ObjectClickHandler(new Object[] { "Vexillum stand" }) {
		@Override
		public void handle(ObjectClickEvent e) {
			create(e.getPlayer(), false);
		}
	};
	
	public static void create(Player player, boolean checkClip) {
		Clan clan = player.getClan();
		if (clan == null) {
			player.sendMessage("You must be in a clan to place a vexillum.");
			return;
		}
		if (CLAN_VEXES.get(clan.getName()) != null) {
			player.sendMessage(CLAN_VEXES.get(clan.getName()).getOwner().getDisplayName() + " has already placed a vexillum for your clan.");
			return;
		}
		if (player.getControllerManager().getController() != null) {
			player.sendMessage("You can't place a vexillum here.");
			return;
		}
		final WorldTile tile = player.transform(player.getDirection().getDx(), player.getDirection().getDy());
		if (checkClip && !World.canLightFire(tile.getPlane(), tile.getX(), tile.getY())) {
			player.sendMessage("You can't place a vexillum here.");
			return;
		}
		for (NPC npc : World.getNPCsInRegionRange(player.getRegionId())) {
			if (npc == null || npc.hasFinished())
				continue;
			if (npc.withinDistance(tile, npc instanceof ClanVexillum ? 6 : 1)) {
				player.sendMessage("You can't place a vexillum this close to "+(npc instanceof ClanVexillum ? "another vexillum" : "another NPC") + ".");
				return;
			}
		}
		player.anim(8178);
		player.lock();
		ClanVexillum vex = new ClanVexillum(player, tile, clan);
		WorldTasks.delay(0, () -> {
			player.unlock();
			vex.display();
		});
	}
	
	public void display() {
		setHidden(false);
		setFaceAngle(getOwner().getDirection().getAngle());
		anim(3495);
		RSColor primary = RSColor.fromHSL(clan.getMotifColors()[2]);
		RSColor secondary = RSColor.fromHSL(clan.getMotifColors()[3]);
		modifyMesh()
			.setModel(0, getOwner().hasRights(Rights.DEVELOPER) ? 64928 : -1) //t5 citadel
			.addColors(clan.getMotifColors()[0], clan.getMotifColors()[1], primary.adjustLuminance(20).getValue(), primary.adjustLuminance(-4).getValue(), primary.adjustLuminance(-4).getValue(), primary.adjustLuminance(-4).getValue(), primary.adjustLuminance(-4).getValue(), primary.adjustLuminance(-4).getValue(), secondary.adjustLuminance(20).getValue(), secondary.adjustLuminance(-4).getValue(), secondary.adjustLuminance(-4).getValue(), secondary.adjustLuminance(-4).getValue(), secondary.adjustLuminance(-4).getValue(), secondary.adjustLuminance(-4).getValue())
			.addTextures(clan.getMotifTextures()[0], clan.getMotifTextures()[1]);
	}

	@Override
	public void finish() {
		super.finish();
		CLAN_VEXES.remove(clan.getName());
	}
}
