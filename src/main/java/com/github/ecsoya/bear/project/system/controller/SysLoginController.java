package com.github.ecsoya.bear.project.system.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ecsoya.bear.common.constant.Constants;
import com.github.ecsoya.bear.common.utils.SecurityUtils;
import com.github.ecsoya.bear.framework.security.LoginBody;
import com.github.ecsoya.bear.framework.security.service.SysLoginService;
import com.github.ecsoya.bear.framework.security.service.SysPermissionService;
import com.github.ecsoya.bear.framework.web.domain.AjaxResult;
import com.github.ecsoya.bear.project.system.domain.SysMenu;
import com.github.ecsoya.bear.project.system.domain.SysUser;
import com.github.ecsoya.bear.project.system.service.ISysMenuService;

/**
 * 登录验证
 * 
 * @author angryred
 */
@RestController
public class SysLoginController {
	@Autowired
	private SysLoginService loginService;

	@Autowired
	private ISysMenuService menuService;

	@Autowired
	private SysPermissionService permissionService;

	/**
	 * 登录方法
	 * 
	 * @param loginBody 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public AjaxResult login(@RequestBody LoginBody loginBody) {
		AjaxResult ajax = AjaxResult.success();
		// 生成令牌
		String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
				loginBody.getUuid(), loginBody.getUnblock());
		ajax.put(Constants.TOKEN, token);
		return ajax;
	}

	/**
	 * 获取用户信息
	 * 
	 * @return 用户信息
	 */
	@GetMapping("getInfo")
	public AjaxResult getInfo() {
		SysUser user = SecurityUtils.getLoginUser().getUser();
		// 角色集合
		Set<String> roles = permissionService.getRolePermission(user);
		// 权限集合
		Set<String> permissions = permissionService.getMenuPermission(user);
		AjaxResult ajax = AjaxResult.success();
		ajax.put("user", user);
		ajax.put("roles", roles);
		ajax.put("permissions", permissions);
		return ajax;
	}

	/**
	 * 获取路由信息
	 * 
	 * @return 路由信息
	 */
	@GetMapping("getRouters")
	public AjaxResult getRouters() {
		Long userId = SecurityUtils.getUserId();
		List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
		return AjaxResult.success(menuService.buildMenus(menus));
	}
}
