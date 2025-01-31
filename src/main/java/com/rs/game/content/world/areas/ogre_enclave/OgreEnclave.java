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
package com.rs.game.content.world.areas.ogre_enclave;


import com.rs.engine.quest.Quest;
import com.rs.lib.game.Tile;
import com.rs.plugin.annotations.PluginEventHandler;
import com.rs.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class OgreEnclave {

	public static ObjectClickHandler handleogreenclaveentrance = new ObjectClickHandler(new Object[] { 2804 }, e -> {
		if (e.getPlayer().isQuestComplete(Quest.WATCHTOWER)) {
			e.getPlayer().tele(Tile.of(2589, 9409, 0));
		}else
			e.getPlayer().sendMessage("You do not meet the requirements for Watchtower.");
	});

	public static ObjectClickHandler handleogreenclaveexit= new ObjectClickHandler(new Object[] { 32494 }, e -> e.getPlayer().tele(Tile.of(2541, 3054, 0)));


}
