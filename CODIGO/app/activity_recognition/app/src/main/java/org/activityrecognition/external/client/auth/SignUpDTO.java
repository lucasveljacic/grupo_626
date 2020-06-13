package org.activityrecognition.external.client.auth;

import java.io.Serializable;

public class SignUpDTO implements Serializable {
    private String env;
    private String name;
    private String lastname;
    private Integer dni;
    private String email;
    private String password;
    private final Integer commission = 2900;
    private final Integer group = 626;

    public SignUpDTO(String env, String name, String lastname, Integer dni, String email, String password) {
        this.env = env;
        this.name = name;
        this.lastname = lastname;
        this.dni = dni;
        this.email = email;
        this.password = password;
    }

    public SignUpDTO(String env, String email, String password) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCommission() {
        return commission;
    }

    public Integer getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "env='" + env + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", dni=" + dni +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", commission=" + commission +
                ", group=" + group +
                '}';
    }
}
