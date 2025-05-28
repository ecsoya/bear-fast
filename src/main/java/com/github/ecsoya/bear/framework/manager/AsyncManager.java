package com.github.ecsoya.bear.framework.manager;

import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.ecsoya.bear.common.utils.Threads;
import com.github.ecsoya.bear.common.utils.spring.SpringUtils;
import com.github.ecsoya.bear.framework.datasource.DynamicDataSourceContextHolder;

/**
 * 异步任务管理器
 * 
 * @author angryred
 */
public class AsyncManager {
	private static final Logger log = LoggerFactory.getLogger("async");
	/**
	 * 操作延迟10毫秒
	 */
	private static final int OPERATE_DELAY_TIME = 10;

	/**
	 * 异步操作任务调度线程池
	 */
	private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");
	private ThreadPoolTaskExecutor threadPoolExecutor = SpringUtils.getBean("threadPoolTaskExecutor");

	/**
	 * 单例模式
	 */
	private AsyncManager() {
	}

	private static AsyncManager me = new AsyncManager();

	public static AsyncManager me() {
		return me;
	}

	/**
	 * 执行任务
	 * 
	 * @param task 任务
	 */
	public void schedule(TimerTask task) {
		long now = System.currentTimeMillis();
		log.info("async schedule...");
		// 获取当前线程的数据源类型
		String dataSourceType = DynamicDataSourceContextHolder.getDataSourceType();
		// 包装任务，确保数据源上下文传递
		TimerTask wrappedTask = new TimerTask() {
			@Override
			public void run() {
				try {
					if (dataSourceType != null) {
						DynamicDataSourceContextHolder.setDataSourceType(dataSourceType);
					}
					task.run();
				} finally {
					DynamicDataSourceContextHolder.clearDataSourceType();
				}
			}
		};

		if (executor.isShutdown() || executor.isTerminated()) {
			CompletableFuture.runAsync(wrappedTask);
			return;
		}

		try {
			executor.schedule(wrappedTask, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			CompletableFuture.runAsync(wrappedTask);
		}
	}

	/**
	 * 执行任务
	 *
	 * @param task 任务
	 */
	public void execute(Runnable task) {
		long now = System.currentTimeMillis();
		log.info("async execute(" + now + ")");
		// 获取当前线程的数据源类型
		String dataSourceType = DynamicDataSourceContextHolder.getDataSourceType();
		// 包装任务，确保数据源上下文传递
		Runnable wrapper = () -> {
			try {
				log.info("async execute(" + now + ") started");
				if (dataSourceType != null) {
					DynamicDataSourceContextHolder.setDataSourceType(dataSourceType);
				}
				task.run();
				log.info("async execute(" + now + ") finished, used");
			} finally {
				DynamicDataSourceContextHolder.clearDataSourceType();
			}
		};

		try {
			threadPoolExecutor.execute(wrapper);
		} catch (Exception e) {
			log.error("async execute(" + now + ") failed", e);
			CompletableFuture.runAsync(wrapper);
		}
	}

	/**
	 * 停止任务线程池
	 */
	public void shutdown() {
		Threads.shutdownAndAwaitTermination(executor);
		threadPoolExecutor.shutdown();
	}
}
