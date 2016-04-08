package com.cqyuanye.state;

import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by YuanYe on 2016/4/7.
 *
 */
public class StateMachineFactoryTest {

    private SingleArcTransition<MockOperand,MockEvent> sat = (mockOperand, mockEvent) -> mockOperand.increaCount();

    @org.junit.Test
    public void testAddSingleTransition() throws Exception {
        StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> smFactory = createSMFactory();
        Field tnField = Whitebox.getField(StateMachineFactory.class,"transitionNode");
        Object node = tnField.get(smFactory);               ///TransitionNode of FSM factory

        Class nodeClazz = Whitebox.getInnerClassType(StateMachineFactory.class,"TransitionNode");
        Field  next = Whitebox.getField(nodeClazz,"next");

        int count = 0;
        while (node !=null){
            count ++;
            node = next.get(node);
        }

        assert  count == 11;
    }

    @org.junit.Test
    public void testInstallTopology() throws Exception {
        StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> smFactory = createSMFactory();

        Field transitionTableField = Whitebox.getField(StateMachineFactory.class,"stateTransitionTable");
        Field optimizedField  = Whitebox.getField(StateMachineFactory.class,"optimized");
        Object table = transitionTableField.get(smFactory);
        boolean optimized = optimizedField.getBoolean(smFactory);

        assert table == null;
        assert optimized == false;

        smFactory = smFactory.installTopology();
        table = transitionTableField.get(smFactory);
        optimized = optimizedField.getBoolean(smFactory);

        assert  table != null;
        assert optimized == true;

        Map map = (Map)table;
        assert map.size() == 9;
    }


    @org.junit.Test
    public void testMake() throws Exception {
        StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> smFactory = createSMFactory().installTopology();
        MockOperand operand = new MockOperand();
        StateMachine<MockState,MockEventType,MockEvent> sm = smFactory.make(operand);

        Field transitionTableField = Whitebox.getField(StateMachineFactory.class,"stateTransitionTable");
        Field optimizedField  = Whitebox.getField(StateMachineFactory.class,"optimized");

        Object table = transitionTableField.get(smFactory);
        boolean optimized = optimizedField.getBoolean(smFactory);
        assert table != null;
        assert optimized == true;

        Queue<MockEventType> eventTypes = new LinkedList<>();
        eventTypes.offer(MockEventType.t_1to2);
        eventTypes.offer(MockEventType.t_2to3);
        eventTypes.offer(MockEventType.t_3to6);
        eventTypes.offer(MockEventType.t_6yo9);
        eventTypes.offer(MockEventType.t_9to10);

        Queue<MockState> states = new LinkedList<>();
        states.offer(MockState.state_1);
        states.offer(MockState.state_2);
        states.offer(MockState.state_3);
        states.offer(MockState.state_6);
        states.offer(MockState.state_9);
        states.offer(MockState.state_10);

        assert sm.getCurrentState() == states.poll();
        while (!eventTypes.isEmpty()){
           assert  sm.doTransition(eventTypes.poll(),new MockEvent()) == states.poll();
        }

        assert operand.getCount() == 5;

    }

    @org.junit.Test(expected = InvalidStateException.class)
    public void testInvalidException() throws InvalidStateException {
        StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> smFactory = createSMFactory().installTopology();
        StateMachine<MockState,MockEventType,MockEvent> sm = smFactory.make(new MockOperand());
        sm.doTransition(MockEventType.t_2to3,new MockEvent());
    }

    private StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> createSMFactory() {
        return new StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent>(MockState.state_1)
                .addSingleTransition(MockState.state_1, MockState.state_2, MockEventType.t_1to2, sat)
                .addSingleTransition(MockState.state_2, MockState.state_3, MockEventType.t_2to3, sat)
                .addSingleTransition(MockState.state_2,MockState.state_4,MockEventType.t_2to4,sat)
                .addSingleTransition(MockState.state_2,MockState.state_5,MockEventType.t_2to5,sat)
                .addSingleTransition(MockState.state_3,MockState.state_6,MockEventType.t_3to6,sat)
                .addSingleTransition(MockState.state_4,MockState.state_7,MockEventType.t_4to7,sat)
                .addSingleTransition(MockState.state_5,MockState.state_8,MockEventType.t_5to8,sat)
                .addSingleTransition(MockState.state_6,MockState.state_9,MockEventType.t_6yo9,sat)
                .addSingleTransition(MockState.state_7,MockState.state_9,MockEventType.t_7to9,sat)
                .addSingleTransition(MockState.state_8,MockState.state_10,MockEventType.t_8to10,sat)
                .addSingleTransition(MockState.state_9, MockState.state_10, MockEventType.t_9to10, sat);
    }
}
