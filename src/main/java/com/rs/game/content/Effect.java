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
package com.rs.game.content;

import com.rs.game.model.entity.Entity;
import com.rs.game.model.entity.Hit;
import com.rs.game.model.entity.npc.NPC;
import com.rs.game.model.entity.player.Player;
import com.rs.game.model.entity.player.Skills;
import com.rs.lib.Constants;
import com.rs.lib.game.SpotAnim;
import com.rs.utils.Ticks;

import java.util.List;


public enum Effect {
	SKULL {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player)
				player.getAppearance().generateAppearanceData();
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player)
				player.getAppearance().generateAppearanceData();
		}
	},
	ANTIPOISON("poison immunity") {
		@Override
		public void apply(Entity player) {
			player.getPoison().reset();
		}
	},
	STUN,
	FREEZE,
	FREEZE_BLOCK,
	MIASMIC_SLOWDOWN,
	MIASMIC_BLOCK,
	TELEBLOCK("teleblock"),
	DOUBLE_XP("double xp", false),
	ANTIFIRE("antifire"),
	SUPER_ANTIFIRE("super-antifire"),
	PRAYER_RENEWAL("prayer renewal") {
		@Override
		public void tick(Entity entity, long tickNumber) {
			if (entity instanceof Player player)
				if (!player.getPrayer().hasFullPoints()) {
					player.getPrayer().restorePrayer(((player.getSkills().getLevelForXp(Constants.PRAYER) * 4.3 / 600.0)) + 0.2);
					if (tickNumber % 25 == 0)
						player.setNextSpotAnim(new SpotAnim(1295));
				}
		}
	},
	BARON_SHARK() {
		@Override
		public void tick(Entity entity, long tick) {
			if (tick % 2 == 0) {
				entity.heal(10);
			}
		}
	},
	STAFF_OF_LIGHT_SPEC("staff of light protection", true),
	JUJU_MINING("juju mining potion", false),
	JUJU_MINE_BANK,
	JUJU_WOODCUTTING("juju woodcutting potion", false),
	JUJU_WC_BANK,
	JUJU_FARMING("juju farming potion", false),
	JUJU_FISHING("juju fishing potion", false),
	SCENTLESS("scentless potion", false),
	JUJU_HUNTER("juju hunter potion", false),
	SARA_BLESSING("Saradomin's blessing", false),
	GUTHIX_GIFT("Guthix's gift", false),
	ZAMMY_FAVOR("Zamorak's favour", false),

	REV_IMMUNE("immunity to revenants", false),
	REV_AGGRO_IMMUNE("revenant aggression immunity", false),

	CHARGED("god charge", false),

	DUNG_HS_SCROLL_BOOST("hoardstalker boost", true),

	AGGRESSION_POTION("aggression potion", false),

	OVERLOAD_PVP_REDUCTION(true),
	MELEE_IMMUNE(true),

	BONFIRE("bonfire boost", false) {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player)
				player.getEquipment().refreshConfigs(false);
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player)
				player.getEquipment().refreshConfigs(false);
		}
	},

	BLOOD_NECKLACE("blood necklace") {
		@Override
		public boolean sendWarnings() {
			return false;
		}

		@Override
		public void tick(Entity entity, long tick) {
			if(entity instanceof Player player && player.getDungManager().isInsideDungeon()) {
				List<NPC> npcs = player.queryNearbyNPCsByTileRange(1, npc -> !npc.isDead() && npc.withinDistance(player, 1)
						&& npc.getDefinitions().hasAttackOption() && player.getControllerManager().canHit(npc) && npc.getTarget() instanceof Player);
				for (NPC npc : npcs)
					if (tick % Ticks.fromSeconds(10) == 0) {
						int dmg = 40 * player.getSkills().getCombatLevelWithSummoning() / 138;
						npc.applyHit(new Hit(player, dmg, Hit.HitLook.TRUE_DAMAGE));
						player.heal(dmg);
					}
			}
		}
	},

	EXCALIBUR_HEAL() {
		@Override
		public void tick(Entity entity, long tick) {
			if (tick % 7 == 0) {
				entity.heal(40);
				entity.spotAnim(1507);
			}
		}
	},

	OVERLOAD("overload") {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player)
				Potions.applyOverLoadEffect(player);
		}

		@Override
		public void tick(Entity entity, long tick) {
			if (tick % 25 == 0 && entity instanceof Player player)
				Potions.applyOverLoadEffect(player);
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player) {
				if (!player.isDead()) {
					int actualLevel = player.getSkills().getLevel(Constants.ATTACK);
					int realLevel = player.getSkills().getLevelForXp(Constants.ATTACK);
					if (actualLevel > realLevel)
						player.getSkills().set(Constants.ATTACK, realLevel);
					actualLevel = player.getSkills().getLevel(Constants.STRENGTH);
					realLevel = player.getSkills().getLevelForXp(Constants.STRENGTH);
					if (actualLevel > realLevel)
						player.getSkills().set(Constants.STRENGTH, realLevel);
					actualLevel = player.getSkills().getLevel(Constants.DEFENSE);
					realLevel = player.getSkills().getLevelForXp(Constants.DEFENSE);
					if (actualLevel > realLevel)
						player.getSkills().set(Constants.DEFENSE, realLevel);
					actualLevel = player.getSkills().getLevel(Constants.MAGIC);
					realLevel = player.getSkills().getLevelForXp(Constants.MAGIC);
					if (actualLevel > realLevel)
						player.getSkills().set(Constants.MAGIC, realLevel);
					actualLevel = player.getSkills().getLevel(Constants.RANGE);
					realLevel = player.getSkills().getLevelForXp(Constants.RANGE);
					if (actualLevel > realLevel)
						player.getSkills().set(Constants.RANGE, realLevel);
					player.heal(500);
				}
				player.soundEffect(2607, false);
				player.sendMessage("<col=480000>The effects of overload have worn off and you feel normal again.");
			}
		}
	},
	BANDOSPOOL("Bandos pool") {

	},
	SULPHUR("Sulphur spring") {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player) {
				int realLevel = player.getSkills().getLevelForXp(Constants.PRAYER);
				player.getSkills().set(Constants.PRAYER, (int) Math.floor(realLevel * 0.1) + realLevel);
				player.getPrayer().restorePrayer(((player.getSkills().getLevel(Constants.PRAYER))));
			}
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player) {
				int actualLevel = player.getSkills().getLevel(Constants.PRAYER);
				int realLevel = player.getSkills().getLevelForXp(Constants.PRAYER);
				if (actualLevel > realLevel)
					player.getSkills().set(Constants.PRAYER, realLevel);
			}
		}
	},
	SALTWATER("Salt-Water Spring") {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player) {
				player.restoreRunEnergy(player.getSkills().getLevel(Skills.AGILITY));
			}
		}
		@Override
		public void tick(Entity entity, long tick) {
			if (tick % Ticks.fromSeconds(3) == 0 && entity instanceof Player player)
				player.restoreRunEnergy(player.getSkills().getLevel(Skills.AGILITY));
		}
	},
	THERMAL("Thermal bath") {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player) {
				player.getSkills().adjustStat(Skills.HITPOINTS,player.getSkills().getLevelForXp(Skills.HITPOINTS*3), Skills.HITPOINTS);
				player.heal(1000);
				player.refreshHitPoints();
				player.addEffect(Effect.ANTIPOISON, Ticks.fromHours(1));
			}
		}

		@Override
		public void tick(Entity entity, long tick) {
			if (tick % Ticks.fromSeconds(3) == 0 && entity instanceof Player player){
				player.restoreRunEnergy(player.getSkills().getLevel(Skills.AGILITY));
			}
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player) {
				int actualLevel = player.getSkills().getLevel(Constants.HITPOINTS);
				int realLevel = player.getSkills().getLevelForXp(Constants.HITPOINTS);
				if (actualLevel > realLevel) {
					player.getSkills().set(Skills.HITPOINTS,realLevel);
					player.refreshHitPoints();
				}
				player.removeEffect(Effect.ANTIPOISON);
			}
		}
	},

	MUD("Mud bath") {
		@Override
		public void apply(Entity entity) {
			if (entity instanceof Player player) {
				player.getTempAttribs().setI("mudChances", 10);
				int realLevel = player.getSkills().getLevelForXp(Constants.HUNTER);
				player.getSkills().set(Constants.HUNTER, (int) Math.floor(realLevel * 0.1) + realLevel);
			}
		}

		@Override
		public void tick(Entity entity, long tick) {
			if (tick % Ticks.fromMinutes(1) == 0 && entity instanceof Player player) {
				if (player.getTempAttribs().getI("mudChances") > 0) {
					int actualLevel = player.getSkills().getLevel(Constants.HUNTER);
					int realLevel = player.getSkills().getLevelForXp(Constants.HUNTER);
					if (actualLevel > realLevel + 1)
						player.getSkills().set(Constants.HUNTER, actualLevel - 1);
					player.getTempAttribs().decI("mudChances");
				}
				else
					expire(player);
			}
		}

		@Override
		public void expire(Entity entity) {
			if (entity instanceof Player player) {
				int actualLevel = player.getSkills().getLevel(Constants.HUNTER);
				int realLevel = player.getSkills().getLevelForXp(Constants.HUNTER);
				if (actualLevel > realLevel)
					player.getSkills().set(Constants.HUNTER, realLevel);
			}
		}
	},


	FARMERS_AFFINITY("Farmer's affinity"),

	SHOOTING_STAR_MINING_BUFF("star sprite's power", false);

    private final String name;

	Effect(String name, boolean removeOnDeath) {
		this.name = name;
	}

	Effect(String name) {
		this(name, true);
	}

	Effect(boolean removeOnDeath) {
		this(null, removeOnDeath);
	}

	Effect() {
		this(null, true);
	}

	public boolean isRemoveOnDeath() {
        boolean removeOnDeath = true;
        return removeOnDeath;
	}

	public void apply(Entity player) {

	}

	public void tick(Entity player, long tickNumber) {

	}

	public void expire(Entity player) {

	}

	public boolean sendWarnings() {
		return name != null;
	}

	public String get30SecWarning() {
		return "<col=FF0000>Your " + name + " is going to run out in 30 seconds!";
	}

	public String getExpiryMessage() {
		return "<col=FF0000>Your " + name + " has run out!";
	}

}
