package com.pentagon.feed;

public class PlayList {
    String name;
    String address;
    String date;
    String listSize;
    String watched;
    String displayed;

    public PlayList(String name, String address, String date, String listSize, String watched, String displayed) {
        this.name = name;
        this.address = address;
        this.date = date;
        this.listSize = listSize;
        this.watched = watched;
        this.displayed = displayed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getListSize() {
        return listSize;
    }

    public void setListSize(String listSize) {
        this.listSize = listSize;
    }

    public String getWatched() {
        return watched;
    }

    public void setWatched(String watched) {
        this.watched = watched;
    }

    public String getDisplayed() {
        return displayed;
    }

    public void setDisplayed(String displayed) {
        this.displayed = displayed;
    }
}
