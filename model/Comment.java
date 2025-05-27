package model;

public class Comment {
    private int commentID;
    private int postID;
    private int userID;
    private long timestamp;
    private String content;

    public Comment(int commentID, int postID, int userID, long timestamp, String content) {
        this.commentID = commentID;
        this.postID = postID;
        this.userID = userID;
        this.timestamp = timestamp;
        this.content = content;
    }

    // Konversi ke format teks (untuk disimpan ke file)
    public String toTextLine() {
        return commentID + "|" + postID + "|" + userID + "|" + timestamp + "|" + content.replace("\n", "\\n");
    }

    // Membuat objek Comment dari satu baris teks
    public static Comment fromTextLine(String line) {
        String[] parts = line.split("\\|", 5); // split max 5 bagian, agar konten bisa mengandung "|"
        if (parts.length != 5) {
            throw new IllegalArgumentException("Format baris tidak valid: " + line);
        }

        int commentID = Integer.parseInt(parts[0]);
        int postID = Integer.parseInt(parts[1]);
        int userID = Integer.parseInt(parts[2]);
        long timestamp = Long.parseLong(parts[3]);
        String content = parts[4].replace("\\n", "\n");

        return new Comment(commentID, postID, userID, timestamp, content);
    }

    public int getCommentID() { return commentID; }
    public int getPostID() { return postID; }
    public int getUserID() { return userID; }
    public long getTimestamp() { return timestamp; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "Comment{commentID=" + commentID +
               ", postID=" + postID +
               ", userID=" + userID +
               ", timestamp=" + timestamp +
               ", content='" + content + "'}";
    }
}
