package com.github.eros.server.common;

/**
 * @author fankongqiumu
 * @description
 * @date 2022/6/28 19:19
 */
public interface Constants {

    interface ExecutorConstants {
        String MODIFIED_SYNC_DISPATCHER_SERVICE = "modifiedSyncDispatcherService";
        String MODIFIED_SYNC_SCHEDULED_SERVICE = "modifiedSyncScheduledService";

        String ASYNC_EVENT_TASK_EXECUTOR = "asyncEventTaskExecutor";
    }

    interface CacheConstants {
        String CONFIG_MODIFY_SYNC_EVENT_CACHE = "configModifySyncEventCache";
    }
}
