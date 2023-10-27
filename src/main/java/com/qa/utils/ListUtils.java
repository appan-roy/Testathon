package com.qa.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ListUtils {

	public static <T> T getRandomValueFromList(List<T> list) {
		return list.get(new Random().nextInt(list.size()));
	}

	public static <T> List<T> sortListInAscendingOrder(List<T> list) {
		Collections.sort(list, null);
		return list;
	}

	public static <T> List<T> sortListInDescendingOrder(List<T> list) {
		Collections.reverse(list);
		return list;
	}

}
