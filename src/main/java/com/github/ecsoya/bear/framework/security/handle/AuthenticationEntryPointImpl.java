package com.github.ecsoya.bear.framework.security.handle;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.github.ecsoya.bear.common.utils.ServletUtils;
import com.github.ecsoya.bear.common.utils.StringUtils;
import com.github.ecsoya.bear.framework.web.domain.AjaxResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证失败处理类 返回未授权
 * 
 * @author angryred
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
	private static final long serialVersionUID = -8970718410437077606L;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		String msg = StringUtils.format("{}: {}", status.getReasonPhrase(), request.getRequestURI());
		ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(status.value(), msg)));
	}
}
