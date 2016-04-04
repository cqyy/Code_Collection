package com.cqyuanye.state;

/**
 * Created by kali on 2016/4/4.
 */
public class WorkFlow {

    private final String wf_id;

    public WorkFlow(String id){
        this.wf_id = id;
    }

    @Override
    public String toString() {
        return "Workflow : " + wf_id;
    }
}
