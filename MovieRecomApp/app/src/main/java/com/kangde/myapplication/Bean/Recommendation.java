package com.kangde.myapplication.Bean;

import java.io.Serializable;

public class Recommendation implements Serializable {
    private int Id;
    private String username;
    private String movieName;
    private String comment;
    private double rating;
    private  String pic;

   public Recommendation ()
   {

   }

    public Recommendation(int id, String username, String movieName, String comment, double rating, String pic) {
        Id = id;
        this.username = username;
        this.movieName = movieName;
        this.comment = comment;
        this.rating = rating;
        this.pic = pic;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }


}
