package com.cqyuanye.state;

import java.util.*;

/**
 * Created by YuanYe on 2016/4/4.
 *
 * A simple implementation of an immutable state machine factory.
 */
public class StateMachineFactory<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT> {

    private Map<STATE,Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>>> stateTransitionTable;
           // new EnumMap<STATE,Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>>>(STATE.class);

    TransitionNode<OPERAND,STATE,EVENTTYPE,EVENT> transitionNode;

    private  STATE defaultState;
    private boolean optimized;

    public StateMachineFactory(STATE initState){
        this.defaultState = initState;
        this.transitionNode = null;
        this.stateTransitionTable = null;
        this.optimized = false;
    }

    private StateMachineFactory(ApplicableMultiOrSingleTransition apt,
                                StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> that){
        this.defaultState = that.defaultState;
        transitionNode = new TransitionNode<>(that.transitionNode,apt);
        this.stateTransitionTable = null;
    }

    private StateMachineFactory(StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> that,boolean optimized){
        this.defaultState = that.defaultState;
        this.transitionNode = that.transitionNode;
        if (optimized){
            makeTransitionTable();
        }else {
            stateTransitionTable = null;
        }
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

    private interface ApplicableTransition<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT>{
        void apply(StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> factory);
    }

    private class ApplicableMultiOrSingleTransition
            implements ApplicableTransition<OPERAND,STATE,EVENTTYPE,EVENT>{

        private final Transition<OPERAND,STATE,EVENT> transition;
        private final EVENTTYPE eventtype;
        private final STATE preState;

        public ApplicableMultiOrSingleTransition(Transition<OPERAND,STATE,EVENT> transition,
                                                 EVENTTYPE eventtype,
                                                 STATE preState){
            this.transition = transition;
            this.eventtype = eventtype;
            this.preState = preState;
        }

        @Override
        public void apply(StateMachineFactory<OPERAND, STATE, EVENTTYPE, EVENT> factory) {
            Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>> map = factory.stateTransitionTable.get(preState);
            if (map == null){
                map = new HashMap<>();
                factory.stateTransitionTable.put(preState,map);
            }
            map.put(eventtype,transition);
        }
    }

    private class TransitionNode<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT>{
        TransitionNode<OPERAND,STATE ,EVENTTYPE,EVENT> next;
        ApplicableTransition<OPERAND,STATE,EVENTTYPE,EVENT> transition;

        public TransitionNode(TransitionNode<OPERAND,STATE ,EVENTTYPE,EVENT> next,
                              ApplicableTransition<OPERAND,STATE,EVENTTYPE,EVENT> transition){
            this.next = next;
            this.transition = transition;
        }
    }

    private class InternalStateMachine implements StateMachine<STATE,EVENTTYPE,EVENT>{

        private STATE currentState;
        private final OPERAND operand;

        public InternalStateMachine(OPERAND operand){
            this.currentState = defaultState;
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

    private void makeTransitionTable(){
        Stack<TransitionNode<OPERAND,STATE,EVENTTYPE,EVENT>> transitionNodeStack = new Stack<>();
        TransitionNode<OPERAND,STATE,EVENTTYPE,EVENT> node;
        for (node = transitionNode; node != null;node = node.next){
            transitionNodeStack.push(node);
        }

        Map<STATE,Map<EVENTTYPE,Transition<OPERAND,STATE,EVENT>>> protype = new HashMap<>();
        protype.put(defaultState,null);

        stateTransitionTable = new EnumMap<>(protype);

        while (!transitionNodeStack.isEmpty()){
            transitionNodeStack.pop().transition.apply(this);
        }
    }

    public StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> installTopology(){
        return new StateMachineFactory<>(this,true);
    }

    public StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> addSingleTransition(STATE preState,STATE postState,EVENTTYPE eventtype,
                                                   SingleArcTransition<OPERAND,EVENT> hook){

        SingleArcInternalTransition transition = new SingleArcInternalTransition(postState,hook);
        ApplicableMultiOrSingleTransition apt = new ApplicableMultiOrSingleTransition(transition,eventtype,preState);

        return new StateMachineFactory<>(apt,this);
    }

    public StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> addMultiTransition(STATE preState,Set<STATE> invalidState,EVENTTYPE eventtype,MultiArcTransition<OPERAND,EVENT,STATE> hook){
        MultiArcInternalTransition transition = new MultiArcInternalTransition(invalidState,hook);
        ApplicableMultiOrSingleTransition apt = new ApplicableMultiOrSingleTransition(transition,eventtype,preState);

        return new StateMachineFactory<>(apt,this);
    }

    public StateMachine<STATE,EVENTTYPE,EVENT> make(OPERAND operand){
        if (optimized == false){
            installTopology();
            optimized = true;
        }
        return new InternalStateMachine(operand);
    }
}
