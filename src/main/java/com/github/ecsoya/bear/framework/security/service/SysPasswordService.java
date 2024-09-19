package com.github.ecsoya.bear.framework.security.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.ecsoya.bear.common.constant.CacheConstants;
import com.github.ecsoya.bear.common.constant.Constants;
import com.github.ecsoya.bear.common.exception.user.BlackListException;
import com.github.ecsoya.bear.common.exception.user.UserPasswordNotMatchException;
import com.github.ecsoya.bear.common.exception.user.UserPasswordRetryLimitExceedException;
import com.github.ecsoya.bear.common.utils.MessageUtils;
import com.github.ecsoya.bear.common.utils.SecurityUtils;
import com.github.ecsoya.bear.common.utils.StringUtils;
import com.github.ecsoya.bear.common.utils.sign.Base64;
import com.github.ecsoya.bear.framework.manager.AsyncManager;
import com.github.ecsoya.bear.framework.manager.factory.AsyncFactory;
import com.github.ecsoya.bear.framework.redis.RedisCache;
import com.github.ecsoya.bear.framework.security.context.AuthenticationContextHolder;
import com.github.ecsoya.bear.project.system.domain.SysUser;

/**
 * 登录密码方法
 * 
 * @author angryred
 */
@Component
public class SysPasswordService {
	@Autowired
	private RedisCache redisCache;

	@Value(value = "${user.password.maxRetryCount}")
	private int maxRetryCount;

	@Value(value = "${user.password.lockTime}")
	private int lockTime;

	@Value(value = "${user.password.blockTime}")
	private int blockTime;

	@Autowired(required = false)
	private IUserUnlockService userUnlockService;

	/**
	 * 登录账户密码错误次数缓存键名
	 * 
	 * @param username 用户名
	 * @return 缓存键key
	 */
	private String getCacheKey(String username) {
		return CacheConstants.PWD_ERR_CNT_KEY + username;
	}

	private String getBlockedKey(String username) {
		return CacheConstants.PWD_BLOCKED_CNT_KEY + username;
	}

	public boolean isLimited(String username) {
		Integer retryCount = redisCache.getCacheObject(getCacheKey(username));
		return retryCount != null && retryCount >= Integer.valueOf(maxRetryCount).intValue();
	}

	public boolean isBlocked(String username) {
		Integer blockCount = redisCache.getCacheObject(getBlockedKey(username));
		return blockCount != null && blockCount >= Integer.valueOf(blockTime).intValue();
	}

	public void validate(SysUser user) {
		Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
		String username = usernamePasswordAuthenticationToken.getName();
		String password = usernamePasswordAuthenticationToken.getCredentials().toString();

		Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

		if (retryCount == null) {
			retryCount = 0;
		}
		String blockedKey = getBlockedKey(username);
		Integer blockedCount = redisCache.getCacheObject(blockedKey);
		if (blockedCount != null && blockedCount >= Integer.valueOf(blockTime).intValue()) {
			// 发解锁邮件给用户
			String token = Base64.encode(blockedKey.getBytes());
			if (userUnlockService != null && blockedCount.equals(blockTime)) {
				AsyncManager.me().execute(() -> {
					userUnlockService.unlockUser(username, token);
				});
			}
			blockedCount = blockedCount + 1;
			redisCache.setCacheObject(blockedKey, blockedCount);
			throw new BlackListException();
		}

		if (retryCount >= Integer.valueOf(maxRetryCount).intValue()) {
			AsyncManager.me().schedule(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
					MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime)));

			throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
		}

		if (!matches(user, password)) {
			retryCount = retryCount + 1;
			AsyncManager.me().schedule(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
					MessageUtils.message("user.password.retry.limit.count", retryCount)));
			redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
			if (retryCount >= Integer.valueOf(maxRetryCount).intValue()) {
				if (blockedCount == null) {
					blockedCount = 0;
				}
				blockedCount = blockedCount + 1;
				redisCache.setCacheObject(blockedKey, blockedCount);
			}
			throw new UserPasswordNotMatchException();
		} else {
			clearLoginRecordCache(username);
		}
	}

	public boolean matches(SysUser user, String rawPassword) {
		return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
	}

	public void clearLoginRecordCache(String loginName) {
		if (redisCache.hasKey(getCacheKey(loginName))) {
			redisCache.deleteObject(getCacheKey(loginName));
		}
		if (redisCache.hasKey(getBlockedKey(loginName))) {
			redisCache.deleteObject(getBlockedKey(loginName));
		}
	}

	public void unblockUser(String unblock) {
		if (StringUtils.isEmpty(unblock)) {
			return;
		}
		String key = new String(Base64.decode(unblock));
		if (redisCache.hasKey(key)) {
			redisCache.deleteObject(key);
		}
	}
}
