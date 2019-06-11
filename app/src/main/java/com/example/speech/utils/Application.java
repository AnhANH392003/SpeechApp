package com.example.speech.utils;

public class Application {

    private String name;
    private String packageName;

    public Application(CharSequence name, String packageName) {
        this.name = String.valueOf(name);
        this.packageName = packageName;
    }

    public Application(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
