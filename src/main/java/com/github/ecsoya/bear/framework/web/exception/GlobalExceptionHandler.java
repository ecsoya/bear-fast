package com.github.ecsoya.bear.framework.web.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.alibaba.fastjson2.JSON;
import com.github.ecsoya.bear.common.exception.DemoModeException;
import com.github.ecsoya.bear.common.exception.ServiceException;
import com.github.ecsoya.bear.common.utils.StringUtils;
import com.github.ecsoya.bear.framework.web.domain.AjaxResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 
 * @author angryred
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * 权限校验异常
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
		return AjaxResult.error(HttpStatus.FORBIDDEN.value(),
				org.springframework.http.HttpStatus.FORBIDDEN.getReasonPhrase());
	}

	/**
	 * 请求方式不支持
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
			HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
		return AjaxResult.error(e.getMessage());
	}

	/**
	 * 业务异常
	 */
	@ExceptionHandler(ServiceException.class)
	public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
		log.warn(e.getMessage(), e);
		Integer code = e.getCode();
		return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
	}

	/**
	 * 请求路径中缺少必需的路径变量
	 */
	@ExceptionHandler(MissingPathVariableException.class)
	public AjaxResult handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
		return AjaxResult.error(String.format("Missing Path[%s]", e.getVariableName()));
	}

	/**
	 * 请求参数类型不匹配
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public AjaxResult handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
			HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
		return AjaxResult.error(String.format("Type Not Matched, [%s] needs: '%s', but: '%s'", e.getName(),
				e.getRequiredType().getName(), e.getValue()));
	}

	/**
	 * 拦截未知的运行时异常
	 */
	@ExceptionHandler(RuntimeException.class)
	public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求地址'{}',发生未知异常.", requestURI, e);
		return AjaxResult.error(e.getMessage());
	}

	/**
	 * 系统异常
	 */
	@ExceptionHandler(Exception.class)
	public AjaxResult handleException(Exception e, HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		log.warn("请求地址'{}',发生系统异常.", requestURI, e);
		return AjaxResult.error(e.getMessage());
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(BindException.class)
	public AjaxResult handleBindException(BindException e) {
		log.warn(e.getMessage(), e);
		String message = e.getAllErrors().get(0).getDefaultMessage();
		return AjaxResult.error(message);
	}

	/**
	 * 自定义验证异常
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn(e.getMessage(), e);
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		return AjaxResult.error(JSON.toJSONString(errors));
	}

	/**
	 * 演示模式异常
	 */
	@ExceptionHandler(DemoModeException.class)
	public AjaxResult handleDemoModeException(DemoModeException e) {
		return AjaxResult.error("Not allowed in demo mode");
	}
}
