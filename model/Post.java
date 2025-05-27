package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {
    @JsonProperty("postID")
    private int postID;

    @JsonProperty("userID")
    private int userID;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("content")
    private String content;

    // Konstruktor kosong → dibutuhkan untuk deserialisasi Jackson & Gson
    public Post() {}

    public Post(int postID, int userID, long timestamp, String content) {
        this.postID = postID;
        this.userID = userID;
        this.timestamp = timestamp;
        this.content = content;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Post{postID=" + postID +
               ", userID=" + userID +
               ", timestamp=" + timestamp +
               ", content='" + content + "'}";
    }
}
