package com.StartupBBSR.competo.Models;

import java.util.List;

public class EventPalModel {
    private String Name;
    private String Bio;
    private String Photo;
    private List<String> Chips;
    private String UserID;

    public EventPalModel() {
    }

    public EventPalModel(String name, String bio, String photo, List<String> chips) {
        Name = name;
        Bio = bio;
        Photo = photo;
        Chips = chips;
    }

    public String getName() {
        return Name;
    }

    public String getBio() {
        return Bio;
    }

    public String getPhoto() {
        return Photo;
    }

    public List<String> getChips() {
        return Chips;
    }

    public String getUserID() {
        return UserID;
    }
}
