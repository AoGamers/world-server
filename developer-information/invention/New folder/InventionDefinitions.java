package com.rs.cache.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.game.player.actions.Skills;
import com.rs.network.buffer.InputBuffer;
import com.rs.utils.Utils;

public class InventionDefinitions {

	private static final ConcurrentHashMap<Integer, InventionDefinitions> InventionDefs = new ConcurrentHashMap<Integer, InventionDefinitions>();

	private Object[][] data;

	public static final InventionDefinitions getData(int dataId) {
		InventionDefinitions def = InventionDefs.get(dataId);
		if (def != null && def.data != null)
			return def;
		def = new InventionDefinitions();
		byte[] data = Cache.STORE.getIndexes()[2].getFile(41, dataId);
		if (data != null)
			def.readValueLoop(new InputBuffer(data));
		return def;
	}

	private void readValueLoop(InputBuffer stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			readValues(stream, opcode);
		}
	}

	private void readValues(InputBuffer stream, int opcode) {
		if (opcode == 3) {
			int i_1_ = stream.readUnsignedByte();
			if (null == data) {
				data = new Object[i_1_][];
			}
			boolean[] bools = new boolean[i_1_];
			int[] numbers = new int[i_1_];
			for (int i_2_ = stream.readUnsignedByte(); 255 != i_2_; i_2_ = stream.getRemaining() > 0 ? stream.readUnsignedByte() : 255) {
				int i_3_ = stream.readUnsignedByte();

				for (int i_4_ = 0; i_4_ < i_3_; i_4_++) {
					int smart = stream.readUnsignedSmart();
					bools[i_2_] = smart != 36;
					numbers[i_2_] = smart;
				}
				data[i_2_] = method8709(stream, i_3_, bools[i_2_], i_2_);
			}
		}
	}

	public static Object[] method8709(InputBuffer stream, int length, boolean bool, int lol) {
		int i_96_ = stream.readUnsignedSmart();
		Object[] objects = new Object[i_96_ * length];
		// System.out.println(i_96_ * length);
		// if (lol == 8)
		// return method59(stream);
		for (int i_97_ = 0; i_97_ < i_96_; i_97_++) {
			for (int i_98_ = 0; i_98_ < length; i_98_++) {
				int i_99_ = i_98_ + i_97_ * length;

				objects[i_99_] = bool ? Integer.valueOf(stream.readInt()) : stream.readString();
			}
		}
		return objects;
	}

	public static Object[] method13775(InputBuffer stream) {
		int i_3_ = stream.readUnsignedByte();
		if (i_3_ == 0)
			return null;
		i_3_--;
		stream.readByte();
		int i_4_ = stream.readInt();
		System.out.println("i_4_=" + i_4_);
		Object[] objects = new Object[i_3_];
		for (int i_5_ = 0; i_5_ < i_3_; i_5_++) {
			int i_6_ = stream.readUnsignedByte();
			if (i_6_ == 0)
				objects[i_5_] = stream.readInt();
			else if (i_6_ == 1)
				objects[i_5_] = stream.readString();
			else
				throw new IllegalStateException(new StringBuilder().append("Unrecognised type ID in deserialise: ").append(i_6_).toString());
		}
		return objects;
	}

	public static Object[] method59(InputBuffer stream) {
		Object[] objects = new Object[4];
		objects[0] = stream.readUnsignedByte();
		objects[1] = stream.readInt();
		objects[2] = stream.readInt();
		objects[3] = stream.readInt();
		return objects;
	}

	public Object[][] getData() {
		return data;
	}

	public Object getDataInIndex(int index) {
		if (data == null || data.length == 0 || data[index] == null || data[index].length == 0)
			return null;
		return data[index][0];
	}

	public static String getDataName(int dataId) {
		InventionDefinitions def = getData(dataId);
		return (String) def.getDataInIndex(1);
	}

	public static int getMaterialIndex(int materialId) {
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		for (int i = 0; i < map.getSize(); i++) {
			InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
			if (((int) def.getDataInIndex(0)) == materialId)
				return i;
		}
		return -1;
	}

	public static String getMaterialName(int materialIndex) {
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(materialIndex));
		return (String) def.getDataInIndex(1);
	}

	public static int getBluePrintIndex(int bluePrintId) {
		ClientScriptMap map = ClientScriptMap.getMap(10743);
		for (int i = 0; i < map.getSize(); i++) {
			InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
			if (((int) def.getDataInIndex(0)) == bluePrintId)
				return i;
		}
		return -1;
	}

	public static int getConfigIndex(int value) {
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		for (int i = 0; i < map.getSize(); i++) {
			InventionDefinitions def = InventionDefinitions.getData(map.getIntValue(i));
			int v = (int) def.getDataInIndex(0);
			if (v == value)
				return i;
		}
		return -1;
	}

	public static Object getDBField(int fieldId, int row, int index) {
		InventionDefinitions def = InventionDefinitions.getData(fieldId);
		if (def.data == null)
			return null;
		int dataIndex = row & 0xff;
		if (dataIndex >= def.data.length || def.data[dataIndex] == null)
			return null;
		return def.data[dataIndex][index];
	}

	public static int getPerkIdByName(String name) {
		for (int i = 0; i < 2000; i++) {
			if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
				continue;
			try {
				InventionDefinitions def = InventionDefinitions.getData(i);
				Object[] data = new Object[def.data.length];
				for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
															// null &&
					// def.data[j].length > 1) // System.out.println("lollll");
					data[j] = Arrays.toString(def.data[j]);
				}
				if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
					continue;
				if (((String) def.getDataInIndex(1)).equalsIgnoreCase(name))
					return (int) def.getDataInIndex(0);
			} catch (Exception e) {

			}
		}
		return -1;
	}

	public static int getRandomMaterial(int rarity) {
		List<Integer> materials = new ArrayList<Integer>();
		ClientScriptMap map = ClientScriptMap.getMap(10742);
		for (int i = 0; i < map.getSize(); i++) {
			InventionDefinitions defs = InventionDefinitions.getData(map.getIntValue(i));
			if (defs == null || defs.data == null)
				continue;
			if (((int) defs.getDataInIndex(7)) == rarity)
				materials.add(i);
		}
		if (materials.isEmpty())
			return -1;
		return materials.get(Utils.random(materials.size()));
	}

	public static String getPerkNameById(int perkId) {
		for (int i = 0; i < 2000; i++) {
			if (!Cache.STORE.getIndexes()[2].fileExists(41, i))
				continue;
			try {
				InventionDefinitions def = InventionDefinitions.getData(i);
				Object[] data = new Object[def.data.length];
				for (int j = 0; j < def.data.length; j++) { // if(def.data[j] !=
															// null &&
					// def.data[j].length > 1) // System.out.println("lollll");
					data[j] = Arrays.toString(def.data[j]);
				}
				if ((!(def.data[2][0] instanceof Integer)) || def.data[2].length > 1 || def.data[1][0] instanceof Integer)
					continue;
				if (((int) def.getDataInIndex(0)) == perkId)
					return ((String) def.getDataInIndex(1));
			} catch (Exception e) {

			}
		}
		return "null";
	}
}
