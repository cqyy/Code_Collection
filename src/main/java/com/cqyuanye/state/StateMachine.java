package com.cqyuanye.state;

/**
 * Created by yuanye on 2016/4/4.
 */
public interface StateMachine<STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT > {
     STATE  getCurrentState();
     STATE doTransition(EVENTTYPE eventtype,EVENT event) throws InvalidStateException;
}
