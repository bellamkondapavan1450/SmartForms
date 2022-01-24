package com.example.smartforms;

public class User {
    private String fName;
    private String lName;
    private String email;
    private String image;

    public User() {
    }

    public User(String fName, String lName, String email, String image) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.image = image;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
