package com.cqyuanye.state;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by kali on 2016/4/4.
 */
public class Test {
    public static void main(String[] args) throws InvalidStateException {

        StateMachineFactory<WorkFlow,WorkflowState,WorkflowCommand,WorkflowEvent> factory =
                new StateMachineFactory<WorkFlow,WorkflowState,WorkflowCommand,WorkflowEvent>(WorkflowState.NOT_START)
                        .addSingleTransition(WorkflowState.NOT_START, WorkflowState.RUNNING,
                        WorkflowCommand.START_WORKFLOW, (workFlow, workflowEvent) ->
                                System.out.println(workFlow + " -- " + workflowEvent))
                        .addSingleTransition(WorkflowState.RUNNING, WorkflowState.WAITING,
                                WorkflowCommand.WORKFLOW_WAIT, (workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .addSingleTransition(WorkflowState.RUNNING,WorkflowState.STOPPING,
                                WorkflowCommand.STOP_WORKFLOW,(workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .addSingleTransition(WorkflowState.WAITING, WorkflowState.RUNNING,
                                WorkflowCommand.WORKFLOW_RESTART, (workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .addSingleTransition(WorkflowState.RUNNING, WorkflowState.STOPPING,
                                WorkflowCommand.STOP_WORKFLOW, (workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .addMultiTransition(WorkflowState.RUNNING, EnumSet.of(WorkflowState.SUCCESS, WorkflowState.FAILURE),
                                WorkflowCommand.RUNNING_COMPLETED, (workFlow, workflowEvent) -> {
                                    System.out.println(workFlow + " -- " + workflowEvent);
                                    return WorkflowState.SUCCESS;
                                })
                        .addSingleTransition(WorkflowState.WAITING, WorkflowState.STOPPED,
                                WorkflowCommand.STOP_WORKFLOW, (workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .addSingleTransition(WorkflowState.STOPPING, WorkflowState.STOPPED,
                                WorkflowCommand.STOPPING_COMPLETED, (workFlow, workflowEvent) ->
                                        System.out.println(workFlow + " -- " + workflowEvent))
                        .installTopology();

        StateMachine<WorkflowState,WorkflowCommand,WorkflowEvent> stateMachine =
                factory.make(new WorkFlow("11"));
        List<WorkflowCommand> commandList = new ArrayList<>();
        commandList.add(WorkflowCommand.START_WORKFLOW);
        commandList.add(WorkflowCommand.RUNNING_COMPLETED);

        for (WorkflowCommand command : commandList){
            System.out.println(stateMachine.getCurrentState());
            stateMachine.doTransition(command,new WorkflowEvent());
        }
        System.out.println(stateMachine.getCurrentState());
    }
}
