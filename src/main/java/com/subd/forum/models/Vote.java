package com.subd.forum.models;

public class Vote {
    String nickname;
    Integer voice;

    public Vote() {
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }
}
