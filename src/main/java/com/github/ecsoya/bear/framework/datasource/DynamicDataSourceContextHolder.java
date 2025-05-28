package com.github.ecsoya.bear.framework.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 数据源切换处理
 * 
 * @author angryred
 */
public class DynamicDataSourceContextHolder {
	public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

	/**
	 * 使用TransmittableThreadLocal维护变量，TransmittableThreadLocal为每个使用该变量的线程提供独立的变量副本，
	 * 并且支持在线程池等场景下进行上下文传递。
	 */
	private static final TransmittableThreadLocal<String> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

	/**
	 * 设置数据源的变量
	 */
	public static void setDataSourceType(String dsType) {
		log.info("切换到{}数据源", dsType);
		CONTEXT_HOLDER.set(dsType);
	}

	/**
	 * 获得数据源的变量
	 */
	public static String getDataSourceType() {
		return CONTEXT_HOLDER.get();
	}

	/**
	 * 清空数据源变量
	 */
	public static void clearDataSourceType() {
		CONTEXT_HOLDER.remove();
	}
}
