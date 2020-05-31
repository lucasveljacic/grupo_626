package org.activityrecognition.client.user;

import java.io.Serializable;

public class UserResponse implements Serializable {
    private String env;
    private String state;
    private UserDTO userDTO;
    private String msg;
    private String token;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "env='" + env + '\'' +
                ", state='" + state + '\'' +
                ", userDTO=" + userDTO +
                ", msg='" + msg + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
