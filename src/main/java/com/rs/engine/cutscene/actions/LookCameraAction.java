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
package com.rs.engine.cutscene.actions;

import com.rs.engine.cutscene.Cutscene;
import com.rs.game.model.entity.player.Player;
import com.rs.lib.game.Tile;

import java.util.Map;

public class LookCameraAction extends CutsceneAction {

	private final int viewLocalX;
	private final int viewLocalY;
	private final int viewZ;
	private final int speedToExactDestination;
	private final int speedOnRoutePath;

	public LookCameraAction(int viewLocalX, int viewLocalY, int viewZ, int speedToExactDestination, int speedOnRoutePath, int actionDelay) {
		super(null, actionDelay);
		this.viewLocalX = viewLocalX;
		this.viewLocalY = viewLocalY;
		this.viewZ = viewZ;
		this.speedToExactDestination = speedToExactDestination;
		this.speedOnRoutePath = speedOnRoutePath;
	}

	public LookCameraAction(int viewLocalX, int viewLocalY, int viewZ, int actionDelay) {
		this(viewLocalX, viewLocalY, viewZ, -1, -1, actionDelay);
	}

	@Override
	public void process(Player player, Map<String, Object> objects) {
		Cutscene scene = (Cutscene) objects.get("cutscene");
		player.getPackets().sendCameraLook(Tile.of(scene.getX(viewLocalX), scene.getY(viewLocalY), 0), viewZ, speedToExactDestination, speedOnRoutePath);
	}

}
