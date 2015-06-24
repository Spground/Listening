package cn.edu.dlut.listening.event;

/**
 * Created by asus on 2015/6/20.
 */
public class ResumeEvent {
    private String msg;

    public ResumeEvent(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }
}
