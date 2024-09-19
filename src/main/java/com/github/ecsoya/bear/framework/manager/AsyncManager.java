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
	private final int OPERATE_DELAY_TIME = 10;

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
		log.info("async schedule...");
		if (executor.isShutdown() || executor.isTerminated()) {
			CompletableFuture.runAsync(task);
		} else {
			try {
				executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				CompletableFuture.runAsync(task);
			}
		}
	}

	public void execute(Runnable task) {
		long now = System.currentTimeMillis();
		log.info("async execute(" + now + ")");
		try {
			// 调用线程池操作，线程不足会抛出异常，然后让Java接手执行。
			Runnable wrapper = () -> {
				log.info("async execute(" + now + ") started");
				task.run();
				log.info("async execute(" + now + ") finished, used");
			};
			threadPoolExecutor.execute(wrapper);
		} catch (Exception e) {
			log.error("async execute(" + now + ") failed", e);
			CompletableFuture.runAsync(task);
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
