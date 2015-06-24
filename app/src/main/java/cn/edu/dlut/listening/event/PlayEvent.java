package cn.edu.dlut.listening.event;

/**
 * Created by asus on 2015/6/20.
 */
public class PlayEvent {

   private String msg;

    public PlayEvent(String msg){
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }
}
