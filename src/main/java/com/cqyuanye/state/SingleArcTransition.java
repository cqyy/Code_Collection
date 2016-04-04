package com.cqyuanye.state;

/**
 * Created by yuanye on 2016/4/4.
 */
public interface SingleArcTransition<OPERAND,EVENT> {
    void transition(OPERAND operand,EVENT event);
}
