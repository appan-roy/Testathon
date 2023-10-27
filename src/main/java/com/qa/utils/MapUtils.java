package com.qa.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class MapUtils {

	public static <K, V> V getRandomValueFromHashMap(HashMap<K, V> hashmap) {
		List<K> keyList = new ArrayList<K>(hashmap.keySet());
		return hashmap.get(keyList.get(new Random().nextInt(keyList.size())));
	}

	public static <K, V> K getRandomKeyFromHashMap(HashMap<K, V> hashmap) {
		V randomValue = getRandomValueFromHashMap(hashmap);
		K randomKey = null;
		for (K key : hashmap.keySet()) {
			if (hashmap.get(key) == randomValue | hashmap.get(key).equals(randomValue)) {
				randomKey = key;
				break;
			}
		}
		return randomKey;
	}

	public static HashMap<String, Double> sortHashMapByValue(HashMap<String, Double> hashmap) {
		// create a list from the elements of Hashmap
		List<Map.Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(hashmap.entrySet());

		// sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// put data from sorted list to Hashmap
		HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list) {
			temp.put(entry.getKey(), entry.getValue());
		}

		return temp;
	}

	public static TreeMap<String, Double> sortHashMapByKey(HashMap<String, Double> hashmap) {
		// TreeMap to store values of HashMap
		TreeMap<String, Double> sorted = new TreeMap<String, Double>();

		// Copy all data from hashMap into TreeMap
		sorted.putAll(hashmap);

		return sorted;
	}

}
