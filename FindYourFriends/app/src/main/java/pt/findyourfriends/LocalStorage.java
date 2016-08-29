package pt.findyourfriends;

import android.app.Application;

public class LocalStorage extends Application {
    private Integer userID;

    public LocalStorage() {
        userID = -1;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer id) {
        userID = id;
    }
}
