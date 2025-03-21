package com.github.ecsoya.bear.framework.security;

import jakarta.validation.constraints.NotNull;

/**
 * 用户登录对象
 * 
 * @author angryred
 */
public class LoginBody {
	/**
	 * 用户名
	 */
	@NotNull
	private String username;

	/**
	 * 用户密码
	 */
	@NotNull
	private String password;

	/**
	 * 验证码
	 */
	private String code;

	/**
	 * 唯一标识
	 */
	private String uuid;

	private String unblock;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUnblock() {
		return unblock;
	}

	public void setUnblock(String unblock) {
		this.unblock = unblock;
	}
}
