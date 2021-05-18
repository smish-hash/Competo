package com.StartupBBSR.competo.Models;

import java.util.List;

public class EventPalUserItemModel {
    private String image;
    private String name, about;
    private List<String> interestChips;

    public EventPalUserItemModel(String image, String name, String about, List<String> interestChips) {
        this.image = image;
        this.name = name;
        this.about = about;
        this.interestChips = interestChips;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<String> getInterestChips() {
        return interestChips;
    }

    public void setInterestChips(List<String> interestChips) {
        this.interestChips = interestChips;
    }
}
