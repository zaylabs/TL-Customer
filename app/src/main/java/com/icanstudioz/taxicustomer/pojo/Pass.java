package com.icanstudioz.taxicustomer.pojo;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;

/**
 * Created by android on 13/10/17.
 */

public class Pass implements Serializable,Parcelable {
    private Place fromPlace;
    private Place toPlace;
    private String driverId;
    private String driverName;
    private String fare;
    private String vehicleName;

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public Pass() {
    }

    protected Pass(Parcel in) {
        driverId = in.readString();
        driverName = in.readString();
        fare = in.readString();
    }

    public static final Creator<Pass> CREATOR = new Creator<Pass>() {
        @Override
        public Pass createFromParcel(Parcel in) {
            return new Pass(in);
        }

        @Override
        public Pass[] newArray(int size) {
            return new Pass[size];
        }
    };

    public Place getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(Place fromPlace) {
        this.fromPlace = fromPlace;
    }

    public Place getToPlace() {
        return toPlace;
    }

    public void setToPlace(Place toPlace) {
        this.toPlace = toPlace;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(driverId);
        parcel.writeString(driverName);
        parcel.writeString(fare);
    }
}
