package com.cqyuanye.state;

/**
 * Created by kali on 2016/4/4.
 */
public enum  WorkflowCommand {
    START_WORKFLOW,
    STOP_WORKFLOW,
    WORKFLOW_WAIT,
    WORKFLOW_RESTART,
    RUNNING_COMPLETED,
    STOPPING_COMPLETED;
}
