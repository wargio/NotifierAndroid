package deroad.phone.notifier;

import java.io.Serializable;

public class Data implements Serializable {

    private String who;
    private String message;
    private int access;

    public Data(String who, String message, int access) {
        this.who = who;
        this.message = message;
        this.access = access;
    }

    public String getWho() {
        return who;
    }

    public String getMessage() {
        return message;
    }

    public int getAccess() {
        return access;
    }
}
