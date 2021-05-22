package org.phonen.fitguide.model;

public class UserModel {
    String name;
    String lastName;
    String userName;
    String phone;
    String idU;

    public UserModel(String name, String lastName, String userName, String idU) {
        this.name = name;
        this.lastName = lastName;
        this.userName = userName;
        this.idU = idU;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdU() {
        return idU;
    }

    public void setIdU(String idU) {
        this.idU = idU;
    }
}
