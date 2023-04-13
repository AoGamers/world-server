package com.rs.game.player.actions.skillAction.invention;

public class InventionConstants {

	public static enum Perks {
		NONE(0, ""),

		BLUNTED(1, "Reduces the weapon's damage by 1% per rank."),

		INACCURATE(2, "Reduces the weapon's accuracy by 1% per rank."),

		// DONE
		BITING(3,
				"+2% chance per rank to critically hit opponents.<br><col=0x7592A0><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col></col>"),
		// DONE
		EQUILIBRIUM(4,
				"Increases minimum hit by 3% per rank and decreases maximum hit by 1% per rank. Does not stack with Equilibrium aura; aura takes precedence."),

		HONED(5, "Has a 2% per rank higher chance of successfully gathering items."),

		LUCKY(6, "0.5% chance per rank when hit that the damage dealt will be reduced to 1. Does not stack with the equivalent Warpriest effect.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		UNDEAD_SLAYER(7, "Deal 7% additional damage to undead."),

		DRAGON_SLAYER(8, "Deal 7% additional damage to dragons."),

		DEMON_SLAYER(9, "Deal 7% additional damage to demons."),

		UNDEAD_BAIT(10, "Deal 30% less damage to undead."),

		DRAGON_BAIT(11, "Deal 30% less damage to dragons."),

		DEMON_BAIT(12, "Deal 30% less damage to demons."),

		LOOTING(13, "Most enemies have a 25% chance to drop an additional high-level resource. (5 minute cooldown.)"),

		ENLIGHTENED(14, "+3% item XP per rank (for the item it's installed on)."),

		GLOW_WORM(15, "Provides light equivalent to a bullseye lantern."),

		ANTITHEISM(16, "Denies access to protect prayers/deflect curses."), // 36382

		HOARDING(17, "Protect Item protects two items instead of one. (Does not work in PvP areas.)"),

		TAUNTING(18,
				"Provoke affects enemies in a 5x5 square around its target, causing those that are already in combat to attack you."),

		COMMITTED(19, "Always skulled while this item is equipped."),

		// DONE
		MOBILE(20,
				"Reduces cooldown of Surge, Escape, Bladed Dive and Barge by 50%, but these abilities no longer generate adrenaline."),

		CAUTIOUS(21, "Cannot auto-retaliate while this item is equipped."),

		HALLUCINOGENIC(22, "Causes you to see strange things..."),

		TALKING(23, "Gives your gear more personality."),

		SCAVENGING(24,
				"1% chance per rank to get an uncommon Invention component as a drop from combat (with a 1% chance it will be a rare component instead).<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		TURTLING(25, "The Barricade ability's duration and cooldown are both increased by 10% per rank."),

		BRIEF_RESPITE(26,
				"Reduces cooldown for Guthix's Blessing and Rejuvenate by 5% per rank, and total healing by 1% of max lifepoints per rank."),

		WISE(27, "While equipped, +1% per rank additional experience, up to 50,000 XP per day."), // 35330

		// DONE
		EFFICIENT(28, "Charge drain rate for this item is reduced by 6% per rank."),

		ABSORBATIVE(29,
				"20% chance to reduce an attack by 5% per rank.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
		// DONE
		PRECISE(30, "Increases your minimum damage by 1.5% per rank of your maximum damage."),

		PROFANE(31, "Cannot consume prayer potions."),

		BRASSICAN(32, "Always sometimes cabbages."),

		FATIGUING(33, "Gain 2% less adrenaline per rank from all attacks."),

		GENOCIDAL(34,
				"Deal up to +7% extra damage to your current Slayer target proportional to progress through your current task."),

		// DONE
		CRACKLING(35,
				"Periodically zaps your combat target for 50% per rank of your weapon's damage (or 10% per rank in PvP). (1 minute cooldown)"), // 35321
		// DONE
		IMPATIENT(36,
				"9% chance per rank for basic abilities to generate 3 extra adrenaline.<br><col=0x7592A0><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col></col>"),

		INVIGORATING(37, "Boosts adrenaline gained from auto-attacks by 10% per rank."),

		VENOMBLOOD(38, "Regular poison damage is negated."),

		// DONE
		DEVOTED(39,
				"3% chance per rank on being hit that protection prayers will work at 100% (or 75% in PvP) for 3 seconds.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"), // 35319

		SPENDTHRIFT(40,
				"1% chance per rank to deal 1% extra damage per rank, at the cost of 1 gold coin per extra point of damage dealt.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		SHIELD_BASHING(41, "Debilitate's damage is increased by 15% per rank."),

		ULTIMATUMS(42,
				"Reduces the adrenaline cost of Overpower, Frenzy, Unload and Omnipower by 5% per rank. Does not stack with other ultimate adrenaline saving effects."),

		JUNK_FOOD(43, "Food gives 3% less health per rank."),

		ENERGISING(44,
				"Slice, Piercing Shot and Wrack deal 20% less damage, but generate 0.6 additional adrenaline per rank."),

		TROPHY_TAKERS(45,
				"3% chance per rank a slain creature will add zero kills to its Slayer assignment; 2% chance per rank it will add 2 kills.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		CLEAR_HEADED(46, "Anticipation lasts one additional second per rank, but no longer reduces damage taken."),

		REFLEXES(47, "Anticipation's duration and cooldown are halved."),

		BULWARK(48, "Debilitate deals no damage but gains up to 6% per rank extra duration from shield value."),

		PREPARATION(49, "Preparation's duration and cooldown are increased by 15% per rank."),

		MEDIOCRITY(50, "Reduces maximum hit by 3% per rank."),

		MYSTERIOUS(51, "Has a mysterious effect..."),

		FURNACE(52,
				"Has a 5% chance per rank of consuming a gathered resource for an extra 100% XP.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		POLISHING(53,
				"Has a 3% chance per rank of transmuting a gathered resource to a higher tier.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		CHEAPSKATE(54, "Has a 1% chance per rank of transmuting a gathered resource to a lower tier."),

		IMP_SOULED(55,
				"Has a 3% chance per rank on successful gathering to send the gathered resources to the bank at a cost of 30 prayer points.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		BUTTERFINGERS(56,
				"Has a 3% chance per rank of dropping the resource you have just gathered.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		REFINED(57,
				"Has a 5% chance per rank of preventing a resource depleting when gathering.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		CHARITABLE(58,
				"Has a 1% chance per rank of putting an extra item by other nearby players after successfully gathering a resource.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		CONFUSED(59, "Has a 1% chance per rank of teleporting you randomly after successfully gathering a resource."),

		CAROMING(60, "Chain and Ricochet hit <col=ffffff>1</col> extra target per rank."),
		// DONE
		AFTERSHOCK(61,
				"After dealing <col=ffffff>50,000</col> damage, create an explosion centered on your current target, dealing up to <col=ffffff>40%</col> per rank weapon damage to nearby enemies."), // 35318

		LUNGING(62,
				"The maximum damage of Combust, Dismember and Fragmentation shot is increased by <col=ffffff>20%</col> weapon damage per rank, but enemies that move will only take <col=ffffff>1.5X</col> increased damage."),

		PLANTED_FEET(63,
				"The duration of Sunshine and Death's Swiftness is increased by <col=ffffff>25%</col>, but they no longer deal periodic damage to your target."),
		// DONE
		ENHANCED_EFFICIENT(64,
				"Charge drain rate for this item is reduced by 9% per rank.  This does not stack with efficient."),
		// DONE
		FLANKING(65,
				"Backhand, Impact and Binding Shot no longer stun and deal <col=ffffff>40%</col> more damage per rank to targets that are not facing you. Forceful Backhand, Deep Impact and Tight Bindings no longer stun and deal <col=ffffff>15%</col> more damage per rank to targets that are not facing you."),

		// DONE
		ENHANCED_DEVOTED(66,
				"<col=ffffff>4.5%</col> chance per rank on being hit that protection prayers will work at <col=ffffff>100%</col> (or <col=ffffff>75%</col> in PvP) for 3 seconds. This does not stack with devoted.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"), // 35320

		CRYSTAL_SHIELD(67,
				"Has a 10% chance to activate on taking damage, lasting 10 seconds. 5% of damage taken per rank is totaled for this period, becoming temporary lifepoints afterwards. These last either 30 seconds or until depleted through further damage. (1 minute cooldown)<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),
		// crystal shield cooldown 36377
		RAPID(68,
				"Has a 5% chance per rank to carry out work at a faster pace.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		TINKER(69,
				"Has a 5% chance per rank to carry out higher quality work, awarding an extra 25% XP<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		PYROMANIAC(70,
				"Has a 0.1% chance per rank of burning all logs of the same type from the inventory.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		BREAKDOWN(71,
				"Has a 20% chance per rank of automatically disassembling items produced.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),

		PROSPER(72,
				"Allows you to find clue scrolls whilst skilling.<br><col=0x7592A0>This perk has an increased chance to activate on level 20 items.</col>"),;
		private int id;
		private String description;

		private Perks(int id, String description) {
			this.id = id;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public int getId() {
			return id;
		}

		public boolean hasIncreasedChance() {
			return description.toLowerCase().contains("increased chance to activate");
		}

	}

}
