package com.cqyuanye.state;

import java.util.Map;
import java.util.Set;

/**
 * Created by yuanye on 2016/4/4.
 */
public class StateMachineFactory<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT> {

    private Map<STATE,Map<EVENTTYPE,ApplicableTransition<OPERAND,STATE,EVENT>>> stateTransitionTable;

    private final STATE initState;

    public StateMachineFactory(STATE initState){
        this.initState = initState;
    }

    private interface ApplicableTransition<OPERAND,STATE extends Enum<STATE>,EVENT>{
        STATE transition(OPERAND operand,EVENT event) throws InvalidStateException;
    }

    private class ApplicableSingleTransition implements ApplicableTransition<OPERAND,STATE,EVENT>{

        private final STATE postState;
        private final SingleArcTransition<OPERAND,EVENT> hook;

        public ApplicableSingleTransition(STATE postState,SingleArcTransition<OPERAND,EVENT> hook){
            this.postState = postState;
            this.hook = hook;
        }

        @Override
        public STATE transition(OPERAND operand, EVENT event) {
            hook.transition(operand,event);
            return postState;
        }
    }

    private class ApplicabelMultiTransition implements ApplicableTransition<OPERAND,STATE,EVENT>{

        private final Set<STATE> validStates;
        private final MultiArcTransition<OPERAND,EVENT,STATE> hook;

        public ApplicabelMultiTransition(Set<STATE> validStates,MultiArcTransition<OPERAND,EVENT,STATE> hook){
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
}
