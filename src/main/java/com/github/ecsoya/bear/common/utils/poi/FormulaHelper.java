package com.github.ecsoya.bear.common.utils.poi;

import java.util.ArrayList;
import java.util.List;

public class FormulaHelper {
	private static final String[] words = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "G", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", };

	private FormulaHelper() {
	}

	public static final List<String> genExcelColumns(int length) {
		List<String> array = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			array.add(getColumnName(i));
		}
		return array;
	}

	public static final String getColumnName(int column) {
		if (column < words.length) {
			return words[column];
		}
		int i = (int) (Math.floor(column / words.length) - 1);
		int j = column % words.length;
		return getColumnName(i) + getColumnName(j);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println(i + "=>" + getColumnName(i));
		}
	}
}
