package com.ysf.chat.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nagrand
 * @title
 * @date 2020/4/25
 */
public class User {

    private String userName;

    private List<String> history;

    private boolean login;

    private String remoteAddress;

    public User() {
        login = false;
        history = new ArrayList<>();
    }

    public void addTalkingHistory(String content){
        history.add(content);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
