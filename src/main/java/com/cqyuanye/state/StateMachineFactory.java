package com.cqyuanye.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuanye on 2016/4/4.
 */
public class StateMachineFactory<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT> {

    private Map<STATE,Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>>> stateTransitionTable = new HashMap<>();

    private final STATE initState;

    public StateMachineFactory(STATE initState){
        this.initState = initState;
        stateTransitionTable.put(initState,null);
    }

    private interface Transition<OPERAND,STATE extends Enum<STATE>,EVENT>{
        STATE transition(OPERAND operand,EVENT event) throws InvalidStateException;
    }

    private class SingleArcInternalTransition implements Transition<OPERAND,STATE,EVENT>{

        private final STATE postState;
        private final SingleArcTransition<OPERAND,EVENT> hook;

        public SingleArcInternalTransition(STATE postState,SingleArcTransition<OPERAND,EVENT> hook){
            this.postState = postState;
            this.hook = hook;
        }

        @Override
        public STATE transition(OPERAND operand, EVENT event) {
            hook.transition(operand,event);
            return postState;
        }
    }

    private class MultiArcInternalTransition implements Transition<OPERAND,STATE,EVENT>{

        private final Set<STATE> validStates;
        private final MultiArcTransition<OPERAND,EVENT,STATE> hook;

        public MultiArcInternalTransition(Set<STATE> validStates,MultiArcTransition<OPERAND,EVENT,STATE> hook){
            this.validStates = validStates;
            this.hook = hook;
        }


        @Override
        public STATE transition(OPERAND operand, EVENT event) throws InvalidStateException {
            STATE postState = hook.transition(operand,event);
             if (!validStates.contains(postState)){
                 throw new InvalidStateException();
             }
            return postState;
        }
    }

    private class InternalStateMachine implements StateMachine<STATE,EVENTTYPE,EVENT>{

        private STATE currentState;
        private final OPERAND operand;

        public InternalStateMachine(OPERAND operand,STATE state){
            this.currentState = state;
            this.operand = operand;
        }

        @Override
        public STATE getCurrentState() {
            return currentState;
        }

        @Override
        public STATE doTransition(EVENTTYPE eventtype, EVENT event) throws InvalidStateException {
            currentState =  StateMachineFactory.this.doTransition(currentState,operand,eventtype,event);
            return currentState;
        }
    }

    private STATE doTransition(STATE preState,OPERAND operand,EVENTTYPE eventtype,EVENT event)
            throws InvalidStateException {
        Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>> transitionMap = stateTransitionTable.get(preState);
        if(transitionMap != null){
            Transition<OPERAND,STATE,EVENT> transition = transitionMap.get(eventtype);
            if (transition != null){
                return transition.transition(operand,event);
            }
        }
        throw new InvalidStateException();
    }

    public StateMachineFactory addSingleTransition(STATE preState,STATE postState,EVENTTYPE eventtype,SingleArcTransition<OPERAND,EVENT> hook){
        SingleArcInternalTransition transition = new SingleArcInternalTransition(postState,hook);
        Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>> transitionMap = stateTransitionTable.get(preState);
        if (transitionMap == null){
            transitionMap = new HashMap<>();
            stateTransitionTable.put(preState,transitionMap);
        }
        transitionMap.put(eventtype,transition);
        return this;
    }

    public StateMachineFactory addMultiTransition(STATE preState,Set<STATE> invalidState,EVENTTYPE eventtype,MultiArcTransition<OPERAND,EVENT,STATE> hook){
        MultiArcInternalTransition transition = new MultiArcInternalTransition(invalidState,hook);
        Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>> transitionMap = stateTransitionTable.get(preState);
        if (transitionMap == null){
            transitionMap = new HashMap<>();
            stateTransitionTable.put(preState,transitionMap);
        }
        transitionMap.put(eventtype,transition);
        return this;
    }

    public StateMachine<STATE,EVENTTYPE,EVENT> getStateMachine(OPERAND operand,STATE initState){
        return new InternalStateMachine(operand,initState);
    }
}
