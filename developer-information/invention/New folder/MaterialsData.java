package com.rs.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.game.player.actions.skillAction.invention.MaterialData;
import com.rs.game.player.actions.skillAction.invention.MaterialData.PossiblePerk;

public class MaterialsData {

	private final static String PACKED_PATH = Settings.DATA_PATH + "items/packedMaterialsData.t";
	private static HashMap<Integer, MaterialData> materialsData = new HashMap<Integer, MaterialData>();

	public static final void main(String[] args) throws IOException {
		Cache.init();
		loadPackedMaterialsData();
	}

	public static final void init() {
		loadPackedMaterialsData();
	}

	public static MaterialData getMaterialData(int materialId) {
		return materialsData.get(materialId);
	}

	public static void addItemDisassemblyData(int materialId, MaterialData data) {
		materialsData.put(materialId, data);
	}

	private static void loadPackedMaterialsData() {
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int materialId = buffer.getInt();
				double xp = buffer.getDouble();
				int possiblePerksCount = buffer.get();
				PossiblePerk[] possiblePerks = new PossiblePerk[possiblePerksCount];
				for (int i = 0; i < possiblePerks.length; i++) {
					int perkId = buffer.getShort();
					int possibleGizmoTypesLength = buffer.get();
					boolean[] possibleGizmoTypes = new boolean[possibleGizmoTypesLength];
					for (int j = 0; j < possibleGizmoTypes.length; j++) {
						possibleGizmoTypes[j] = buffer.get() == 1;
					}
					int possiblePerksRanksLength = buffer.get();
					int[][] possibleRanks = new int[possiblePerksRanksLength][];
					for (int j = 0; j < possibleRanks.length; j++) {
						int length = buffer.get();
						possibleRanks[j] = new int[length];
						for (int z = 0; z < possibleRanks[j].length; z++) {
							possibleRanks[j][z] = buffer.getShort();
						}
					}
					possiblePerks[i] = new PossiblePerk(perkId, possibleGizmoTypes, possibleRanks);
				}
				MaterialData data = new MaterialData(xp, possiblePerks);
				materialsData.put(materialId, data);
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}

}
