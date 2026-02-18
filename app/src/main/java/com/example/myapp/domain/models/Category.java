package com.example.myapp.domain.models;

public class Category {

    private String id;
    private String userUid;
    private String name;
    private String color;

    public Category() {}

    public Category(String id, String userUid, String name, String color) {
        this.id = id;
        this.userUid = userUid;
        this.name = name;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}