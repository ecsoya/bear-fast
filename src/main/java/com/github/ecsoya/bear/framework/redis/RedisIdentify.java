package com.github.ecsoya.bear.framework.redis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.github.ecsoya.bear.common.utils.DateUtils;

@Component
@SuppressWarnings("rawtypes")
public class RedisIdentify {
	private static final String KEY_ID = "redis_identify_id";
	private static final int DEFAULT_LENGTH = 6;
	private static RedisTemplate redisTemplate;

	@Autowired
	public void redisTemplate(RedisTemplate redisTemplate) {
		RedisIdentify.redisTemplate = redisTemplate;
	}

	@SuppressWarnings("unchecked")
	public static String generate(String prefix, int length) {
		StringBuffer buffer = new StringBuffer();
		if (prefix != null) {
			buffer.append(prefix);
		}

		String now = DateUtils.dateTimeNow("yyMMdd");
		buffer.append(now);

		String key = String.format("%s:%s", KEY_ID, Optional.ofNullable(prefix).orElse(""));

		Long increment = redisTemplate.opsForValue().increment(key, 1);

		Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
		if (expire == -1) {
			LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
			long secondsToMidnight = LocalDateTime.now().until(endOfDay, ChronoUnit.SECONDS);
			redisTemplate.expire(key, secondsToMidnight, TimeUnit.SECONDS);
		}
		String format = "%0" + length + "d";

		buffer.append(String.format(format, increment));

		return buffer.toString();
	}

	public static String generate() {
		return generate(null, DEFAULT_LENGTH);
	}
}
