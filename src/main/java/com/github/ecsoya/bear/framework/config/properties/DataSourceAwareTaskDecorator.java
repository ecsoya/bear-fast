package com.github.ecsoya.bear.framework.config.properties;

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import com.github.ecsoya.bear.framework.datasource.DynamicDataSourceContextHolder;

public class DataSourceAwareTaskDecorator implements TaskDecorator {
	@Override
	public @NonNull Runnable decorate(@NonNull Runnable runnable) {
		// 获取父线程的数据源标识
		String dataSourceKey = DynamicDataSourceContextHolder.getDataSourceType();
		return () -> {
			try {
				// 在子线程中设置数据源标识
				DynamicDataSourceContextHolder.setDataSourceType(dataSourceKey);
				runnable.run();
			} finally {
				DynamicDataSourceContextHolder.clearDataSourceType();
			}
		};
	}
}