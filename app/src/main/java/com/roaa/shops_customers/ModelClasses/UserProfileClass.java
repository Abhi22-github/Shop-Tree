package com.roaa.shops_customers.ModelClasses;

import java.io.Serializable;

public class UserProfileClass implements Serializable {
    private String firstName;
    private String lastName;
    private String email;
    private String profileImage;
    private String phoneNumber;
    private String userID;

    public UserProfileClass(){

    }

    public UserProfileClass(String firstName, String lastName, String email,
                            String profileImage, String phoneNumber, String userID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImage = profileImage;
        this.phoneNumber = phoneNumber;
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
