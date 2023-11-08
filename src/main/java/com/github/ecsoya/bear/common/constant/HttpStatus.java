package com.github.ecsoya.bear.common.constant;

/**
 * 返回状态码
 * 
 * @author angryred
 */
public class HttpStatus {
	/**
	 * 操作成功
	 */
	public static final int SUCCESS = org.springframework.http.HttpStatus.OK.value();

	/**
	 * 对象创建成功
	 */
	public static final int CREATED = org.springframework.http.HttpStatus.CREATED.value();

	/**
	 * 请求已经被接受
	 */
	public static final int ACCEPTED = org.springframework.http.HttpStatus.ACCEPTED.value();

	/**
	 * 操作已经执行成功，但是没有返回数据
	 */
	public static final int NO_CONTENT = org.springframework.http.HttpStatus.NO_CONTENT.value();

	/**
	 * 资源已被移除
	 */
	public static final int MOVED_PERM = org.springframework.http.HttpStatus.MOVED_PERMANENTLY.value();

	/**
	 * 重定向
	 */
	public static final int SEE_OTHER = org.springframework.http.HttpStatus.SEE_OTHER.value();

	/**
	 * 资源没有被修改
	 */
	public static final int NOT_MODIFIED = org.springframework.http.HttpStatus.NOT_MODIFIED.value();

	/**
	 * 参数列表错误（缺少，格式不匹配）
	 */
	public static final int BAD_REQUEST = org.springframework.http.HttpStatus.BAD_REQUEST.value();

	/**
	 * 未授权
	 */
	public static final int UNAUTHORIZED = org.springframework.http.HttpStatus.UNAUTHORIZED.value();

	/**
	 * 访问受限，授权过期
	 */
	public static final int FORBIDDEN = org.springframework.http.HttpStatus.FORBIDDEN.value();

	/**
	 * 资源，服务未找到
	 */
	public static final int NOT_FOUND = org.springframework.http.HttpStatus.NOT_FOUND.value();

	/**
	 * 不允许的http方法
	 */
	public static final int BAD_METHOD = org.springframework.http.HttpStatus.BAD_GATEWAY.value();

	/**
	 * 资源冲突，或者资源被锁
	 */
	public static final int CONFLICT = org.springframework.http.HttpStatus.CONFLICT.value();

	/**
	 * 不支持的数据，媒体类型
	 */
	public static final int UNSUPPORTED_TYPE = org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();

	/**
	 * 系统内部错误
	 */
	public static final int ERROR = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value();

	/**
	 * 接口未实现
	 */
	public static final int NOT_IMPLEMENTED = org.springframework.http.HttpStatus.NOT_IMPLEMENTED.value();

	/**
	 * 系统警告消息
	 */
	public static final int WARN = 601;
}
