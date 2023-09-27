package com.github.ecsoya.bear.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

public class MathUtils {

	private MathUtils() {
	}

	public static boolean isPositive(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0;
	}

	public static boolean isValid(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
	}

	public static boolean isInvalid(BigDecimal value) {
		return value == null || value.doubleValue() < 0;
	}

	public static boolean isEmpty(BigDecimal value) {
		return !isPositive(value);
	}

	public static boolean gt(BigDecimal left, BigDecimal right) {
		return left != null && right != null && left.compareTo(right) > 0;
	}

	public static boolean gte(BigDecimal left, BigDecimal right) {
		return left != null && right != null && left.compareTo(right) >= 0;
	}

	public static boolean lt(BigDecimal left, BigDecimal right) {
		return left != null && right != null && left.compareTo(right) < 0;
	}

	public static boolean lte(BigDecimal left, BigDecimal right) {
		return left != null && right != null && left.compareTo(right) <= 0;
	}

	public static BigDecimal plus(BigDecimal... members) {
		BigDecimal sum = BigDecimal.ZERO;
		if (members != null) {
			for (BigDecimal bigDecimal : members) {
				if (bigDecimal != null) {
					sum = sum.add(bigDecimal);
				}
			}
		}
		return sum;
	}

	public static Long plus(Long... numbers) {
		BigDecimal sum = BigDecimal.ZERO;
		if (numbers != null) {
			for (Long number : numbers) {
				if (number != null) {
					sum = sum.add(BigDecimal.valueOf(number.longValue()));
				}
			}
		}
		return sum.longValue();
	}

	public static Long parseLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static BigDecimal nullToZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value.setScale(6, RoundingMode.HALF_UP);
	}

	public static BigDecimal nullToZero(Number value) {
		return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value.doubleValue());
	}

	public static String toPercent(BigDecimal value, int precision) {
		if (value == null) {
			return "0%";
		}
		return value.multiply(BigDecimal.valueOf(100)).setScale(precision).toPlainString() + "%";
	}

	public static BigDecimal divide(BigDecimal value, BigDecimal divisor) {
		if (value == null || divisor == null || BigDecimal.ZERO.equals(divisor)) {
			return BigDecimal.ZERO;
		}
		return value.divide(divisor, 6, RoundingMode.HALF_UP);
	}

	public static boolean isRate(BigDecimal value) {
		return value != null && value.doubleValue() >= 0 && value.doubleValue() <= 1;
	}

	public static boolean isDiscount(BigDecimal value) {
		return value != null && value.doubleValue() > 0 && value.doubleValue() <= 1;
	}

	public static BigDecimal parse(String value) {
		if (StringUtils.isEmpty(value)) {
			return BigDecimal.ZERO;
		}
		try {
			return new BigDecimal(value);
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	public static boolean equals(BigDecimal v1, BigDecimal v2) {
		if (v1 == null || v2 == null) {
			return false;
		}
		return v1.compareTo(v2) == 0;
	}

	public static BigDecimal toDecimal(Number number) {
		if (number == null) {
			return null;
		}
		return BigDecimal.valueOf(number.doubleValue());
	}

	public static boolean lt(Long version, Long latest) {
		if (version == null || latest == null) {
			return true;
		}
		return version.longValue() < latest.longValue();
	}

	public static void main(String[] args) {
		BigDecimal value = plus(BigDecimal.valueOf(2.5312), BigDecimal.valueOf(2.5312));
		System.out.println(value);
	}

	public static Integer parseInteger(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static Comparator<BigDecimal> asc() {
		return new Comparator<BigDecimal>() {

			@Override
			public int compare(BigDecimal b1, BigDecimal b2) {
				if (b1 == null || b2 == null) {
					return 0;
				}
				return b1.compareTo(b2);
			}
		};
	}

	public static Comparator<BigDecimal> desc() {
		return new Comparator<BigDecimal>() {

			@Override
			public int compare(BigDecimal b1, BigDecimal b2) {
				if (b1 == null || b2 == null) {
					return 0;
				}
				return -b1.compareTo(b2);
			}
		};
	}
}
