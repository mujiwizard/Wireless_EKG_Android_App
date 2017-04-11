package com.g13.mano.g13_wireless_ekg.Information;

/**
 * Created by Mano on 2/12/2017.
 */

public class UserInfo {
    private String Name, DOB,Height,Weight,Gender;


    public UserInfo(){

    }

    public String getName() {
        return "Name: "+Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getDOB() {
        return "DOB: "+DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getHeight() {
        return "Height: "+Height +" cm";
    }

    public void setHeight(String height) {
        Height = height;
    }

    public String getWeight() {
        return "Weight: "+Weight + " Kg";
    }

    public void setWeight(String weight) {
        Weight = weight;
    }

    public String getGender() {
        return "Gender: "+Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }
}
