package com.github.ecsoya.bear.framework.security.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.ecsoya.bear.common.constant.CacheConstants;
import com.github.ecsoya.bear.common.constant.Constants;
import com.github.ecsoya.bear.common.exception.ServiceException;
import com.github.ecsoya.bear.common.exception.user.BlackListException;
import com.github.ecsoya.bear.common.exception.user.CaptchaException;
import com.github.ecsoya.bear.common.exception.user.CaptchaExpireException;
import com.github.ecsoya.bear.common.exception.user.UserNotExistsException;
import com.github.ecsoya.bear.common.exception.user.UserPasswordNotMatchException;
import com.github.ecsoya.bear.common.utils.DateUtils;
import com.github.ecsoya.bear.common.utils.MessageUtils;
import com.github.ecsoya.bear.common.utils.StringUtils;
import com.github.ecsoya.bear.common.utils.ip.IpUtils;
import com.github.ecsoya.bear.framework.manager.AsyncManager;
import com.github.ecsoya.bear.framework.manager.factory.AsyncFactory;
import com.github.ecsoya.bear.framework.redis.RedisCache;
import com.github.ecsoya.bear.framework.security.LoginUser;
import com.github.ecsoya.bear.framework.security.context.AuthenticationContextHolder;
import com.github.ecsoya.bear.project.system.domain.SysUser;
import com.github.ecsoya.bear.project.system.service.ISysConfigService;
import com.github.ecsoya.bear.project.system.service.ISysUserService;

/**
 * 登录校验方法
 * 
 * @author angryred
 */
@Component
public class SysLoginService {
	@Autowired
	private TokenService tokenService;

	@Resource
	private AuthenticationManager authenticationManager;

	@Autowired
	private RedisCache redisCache;

	@Autowired
	private ISysUserService userService;

	@Autowired
	private ISysConfigService configService;

	@Autowired
	private SysPasswordService passwordService;

	public String loginByEmail(String email, String password, String code, String uuid, String unblock) {
		SysUser user = userService.selectUserByEmail(email);
		if (user == null) {
			throw new UserNotExistsException();
		}
		String oldToken = tokenService.getTokenByUserId(user.getUserId());
		String newToken = login(user.getUserName(), password, code, uuid, unblock);
		try {
			if (oldToken != null && !oldToken.equals(newToken)) {
				LoginUser loginUser = tokenService.getLoginUser(oldToken);
				if (loginUser != null) {
					tokenService.delLoginUser(loginUser.getToken());
				}
			}
		} catch (Exception e) {
			// Ignored
		}
		return newToken;
	}

	/**
	 * 登录验证
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public String login(String username, String password, String code, String uuid, String unblock) {
		if (StringUtils.isNotEmpty(unblock)) {
			passwordService.unblockUser(unblock);
		}
		// 验证码校验
		validateCaptcha(username, code, uuid);
		// 登录前置校验
		loginPreCheck(username, password);
		// 用户验证
		Authentication authentication = null;
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
					password);
			AuthenticationContextHolder.setContext(authenticationToken);
			// 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
			authentication = authenticationManager.authenticate(authenticationToken);
		} catch (Exception e) {
			if (e instanceof BadCredentialsException) {
				AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
						MessageUtils.message("user.password.not.match")));
				throw new UserPasswordNotMatchException();
			} else {
				AsyncManager.me()
						.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
				throw new ServiceException(e.getMessage());
			}
		} finally {
			AuthenticationContextHolder.clearContext();
		}
		AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS,
				MessageUtils.message("user.login.success")));
		LoginUser loginUser = (LoginUser) authentication.getPrincipal();
		recordLoginInfo(loginUser.getUserId());
		// 生成token
		String token = tokenService.createToken(loginUser);
		redisCache.setCacheObject(token, loginUser.getUserId());
		tokenService.bindToken(loginUser.getUserId(), token);
		return token;
	}

	/**
	 * 校验验证码
	 * 
	 * @param username 用户名
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public void validateCaptcha(String username, String code, String uuid) {
		boolean captchaEnabled = configService.selectCaptchaEnabled();
		if (captchaEnabled) {
			String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
			String captcha = redisCache.getCacheObject(verifyKey);
			redisCache.deleteObject(verifyKey);
			if (captcha == null) {
				AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
						MessageUtils.message("user.jcaptcha.expire")));
				throw new CaptchaExpireException();
			}
			if (!code.equalsIgnoreCase(captcha)) {
				AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
						MessageUtils.message("user.jcaptcha.error")));
				throw new CaptchaException();
			}
		}
	}

	/**
	 * 登录前置校验
	 * 
	 * @param username 用户名
	 * @param password 用户密码
	 */
	public void loginPreCheck(String username, String password) {
		// 用户名或密码为空 错误
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			AsyncManager.me().execute(
					AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
			throw new UserNotExistsException();
		}
		// 密码如果不在指定范围内 错误
//		if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
//				|| password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
//			AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
//					MessageUtils.message("user.password.not.match")));
//			throw new UserPasswordNotMatchException();
//		}
		// 用户名不在指定范围内 错误
//		if (username.length() < UserConstants.USERNAME_MIN_LENGTH
//				|| username.length() > UserConstants.USERNAME_MAX_LENGTH) {
//			AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
//					MessageUtils.message("user.password.not.match")));
//			throw new UserPasswordNotMatchException();
//		}
		// IP黑名单校验
		String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
		if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
			AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
					MessageUtils.message("login.blocked")));
			throw new BlackListException();
		}
	}

	/**
	 * 记录登录信息
	 *
	 * @param userId 用户ID
	 */
	public void recordLoginInfo(Long userId) {
		SysUser sysUser = new SysUser();
		sysUser.setUserId(userId);
		sysUser.setLoginIp(IpUtils.getIpAddr());
		sysUser.setLoginDate(DateUtils.getNowDate());
		userService.updateUserProfile(sysUser);
	}
}
