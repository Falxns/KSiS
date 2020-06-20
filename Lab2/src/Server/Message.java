package Server;

import java.io.Serializable;

public class Message implements Serializable {
    public String msg;
    public int from;
    public int to;
    public int type;
    public String files;

    public Message(String msg, int from, int to, int type, String files){
        this.msg = msg;
        this.from = from;
        this.to = to;
        this.type = type;
        this.files = files;
    }
}