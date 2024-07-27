package com.github.ecsoya.bear.framework.security.service;

public interface IUserUnlockService {

	int unlockUser(String username, String token);

}
