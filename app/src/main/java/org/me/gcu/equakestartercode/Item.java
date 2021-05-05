package org.me.gcu.equakestartercode;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Madani Napaul | S1903342
 */

public class Item implements Parcelable {
    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    private int id;
    private String title;
    private String description;
    private String location;
    private String link;
    private String pubDate;
    private String category;
    private double depth;
    private double magnitude;
    private double latitude;
    private double longitude;

    public Item() {
    }

    protected Item(Parcel in) {
        setId(in.readInt());
        setTitle(in.readString());
        setDescription(in.readString());
        setLocation(in.readString());
        setLink(in.readString());
        setPubDate(in.readString());
        setCategory(in.readString());
        setDepth(in.readDouble());
        setMagnitude(in.readDouble());
        setLatitude(in.readDouble());
        setLongitude(in.readDouble());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getTitle());
        dest.writeString(getDescription());
        dest.writeString(getLocation());
        dest.writeString(getLink());
        dest.writeString(getPubDate());
        dest.writeString(getCategory());
        dest.writeDouble(getDepth());
        dest.writeDouble(getMagnitude());
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", location='" + getLocation() + '\'' +
                ", link='" + getLink() + '\'' +
                ", pubDate='" + getPubDate() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", depth=" + getDepth() +
                ", magnitude=" + getMagnitude() +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                '}';
    }
}
