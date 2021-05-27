package org.phonen.fitguide.model;

public class User
{

    String name;
    String lastName;
    String userName;
    String phone;
    String height;
    String weight;
    String date;
    int level;
    int points;
    String rank;


    public User() {
    level=1;
    points=0;
    rank="HIERRO";
    }

    public void addPoints(double kcal){
        this.points += kcal;

        if (this.points > 1000){
            this.rank = Level.levels[4];
            this.level = 5;
        } else if (this.points > 700){
            this.rank = Level.levels[3];
            this.level = 4;
        } else if (this.points > 200){
            this.rank = Level.levels[2];
            this.level = 3;
        } else if (this.points > 50){
            this.rank = Level.levels[1];
            this.level = 2;
        } else if(this.points >= 0){
            this.rank = Level.levels[0];
            this.level = 1;
        }

    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
