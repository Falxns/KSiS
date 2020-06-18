package Server;

import java.io.Serializable;

public class Message implements Serializable {
    public String msg;
    public int from;
    public int to;
    public int type;

    public Message(String msg, int from, int to, int type){
        this.msg = msg;
        this.from = from;
        this.to = to;
        this.type = type;
    }
}