package com.cqyuanye.state;

/**
 * Created by yuanye on 2016/4/4.
 */
public interface MultiArcTransition<OPERAND,EVENT,STATE extends Enum<STATE>> {
    STATE transition(OPERAND operand,EVENT event);
}
