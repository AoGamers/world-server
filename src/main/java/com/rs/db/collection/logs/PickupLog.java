package com.rs.db.collection.logs;

import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.GroundItem;
import com.rs.lib.game.Item;
import com.rs.lib.game.Tile;

import java.util.Objects;
import java.util.UUID;

public class PickupLog {
	private final String uuid;
	private final String player;
	private final String itemName;
	private final Item item;
	private final Tile tile;
	private final String owner;

	public PickupLog(Player player, GroundItem item) {
		this.player = player.getUsername();
		this.tile = item.getTile();
		this.item = new Item(item);
		this.itemName = item.getName();
		this.owner = item.getCreatorUsername();
		this.uuid = UUID.randomUUID().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PickupLog pickupLog = (PickupLog) o;
		return Objects.equals(uuid, pickupLog.uuid) && Objects.equals(player, pickupLog.player) && Objects.equals(itemName, pickupLog.itemName) && Objects.equals(item, pickupLog.item) && Objects.equals(tile, pickupLog.tile) && Objects.equals(owner, pickupLog.owner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, player, itemName, item, tile, owner);
	}
}
