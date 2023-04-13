package com.rs.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.player.actions.skillAction.invention.ItemDisassemblyData;

public class ItemsDisassemblyData {

	private final static String PACKED_PATH = Settings.DATA_PATH + "items/packedDisassemblyData.t";
	private static HashMap<Integer, ItemDisassemblyData> itemsDisassemblyData = new HashMap<Integer, ItemDisassemblyData>();

	public static final void main(String[] args) throws IOException {
		Cache.init();
		loadPackedItemsDisassemblyData();
	}

	public static final void init() {
		loadPackedItemsDisassemblyData();
	}

	public static ItemDisassemblyData getItemDisassemblyData(int itemId) {
		return itemsDisassemblyData.get(itemId);
	}

	public static void addItemDisassemblyData(int itemId, ItemDisassemblyData data) {
		itemsDisassemblyData.put(itemId, data);
	}

	private static void loadPackedItemsDisassemblyData() {
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int itemId = buffer.getInt();
				double disassembleXp = buffer.getDouble();
				int reqQuantity = buffer.getShort();
				int materialCount = buffer.getShort();
				double junkChance = buffer.getDouble();
				int[] specialMaterial = new int[2];
				for (int i = 0; i < specialMaterial.length; i++)
					specialMaterial[i] = buffer.getShort();
				int oftenMaterialslength = buffer.get();
				int[] oftenMaterials = new int[oftenMaterialslength];
				for (int i = 0; i < oftenMaterials.length; i++)
					oftenMaterials[i] = buffer.getShort();
				int sometimesMaterialslength = buffer.get();
				int[] sometimesMaterials = new int[sometimesMaterialslength];
				for (int i = 0; i < sometimesMaterials.length; i++)
					sometimesMaterials[i] = buffer.getShort();
				int rareMaterialslength = buffer.get();
				int[] rareMaterials = new int[rareMaterialslength];
				for (int i = 0; i < rareMaterials.length; i++)
					rareMaterials[i] = buffer.getShort();
				if (specialMaterial[0] == -1)
					specialMaterial = null;
				if (oftenMaterialslength == 0)
					oftenMaterials = null;
				if (sometimesMaterialslength == 0)
					sometimesMaterials = null;
				if (rareMaterialslength == 0)
					rareMaterials = null;
				ItemDisassemblyData data = new ItemDisassemblyData(disassembleXp, junkChance, reqQuantity,
						materialCount, specialMaterial, oftenMaterials, sometimesMaterials, rareMaterials);
				itemsDisassemblyData.put(itemId, data);
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

}
