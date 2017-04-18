import java.io.*;

/**
 * Created by Карен on 18.04.2017.
 */
public class Message implements Serializable {

    protected static final long serialVersionUID = -4862926644813433707L;

    static final int AY = 0, MESSAGE = 1;
    private int type;
    private String message;

    Message(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
}
