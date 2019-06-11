package com.example.speech;

public class Contact {

    private String name;
    private String phone;
    private String nameNumber;

    public Contact(String name, String phone, String nameNumber) {
        this.name = name;
        this.phone = phone;
        this.nameNumber = nameNumber;
    }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNameNumber() {
        return nameNumber;
    }

    public void setNameNumber(String nameNumber) {
        this.nameNumber = nameNumber;
    }
}
