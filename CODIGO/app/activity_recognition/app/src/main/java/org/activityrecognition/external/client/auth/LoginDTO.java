package org.activityrecognition.external.client.auth;

import java.io.Serializable;

public class LoginDTO implements Serializable {
    private String env;
    private String email;
    private String password;
    private final Integer group = 626;

    public LoginDTO(String env, String email, String password) {
        this.env = env;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "env='" + env + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", group=" + group +
                '}';
    }
}
