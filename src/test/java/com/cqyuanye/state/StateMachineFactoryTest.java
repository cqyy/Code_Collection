package com.cqyuanye.state;

import org.junit.After;
import org.powermock.reflect.Whitebox;

import java.util.Map;

/**
 * Created by YuanYe on 2016/4/7.
 *
 */
public class StateMachineFactoryTest {

    private SingleArcTransition<MockOperand,MockEvent> sat = new SingleArcTransition<MockOperand, MockEvent>() {
        @Override
        public void transition(MockOperand mockOperand, MockEvent mockEvent) {
            mockOperand.increaCount();
        }
    };


    @org.junit.Test
    public void testAddSingleTransition() throws Exception {
        StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> smFactory = createSMFactory();
       // Map<MockEvent,Map<MockEventType,Transition<MockOperand,MockState,MockEvent>>> stateTransitionTable; = Whitebox.getAllInstanceFields();
    }

    @org.junit.Test
    public void testInstallTopology() throws Exception {

    }


    @org.junit.Test
    public void testAddMultiTransition() throws Exception {

    }

    @org.junit.Test
    public void testMake() throws Exception {

    }

    private StateMachineFactory<MockOperand,MockState,MockEventType,MockEvent> createSMFactory() throws Exception {
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

    @After
    public void tearDown() throws Exception {

    }
}
