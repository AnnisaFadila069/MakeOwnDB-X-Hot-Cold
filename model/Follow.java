package model;

public class Follow {
    private int followerID;
    private int followeeID;

    public Follow(int followerID, int followeeID) {
        this.followerID = followerID;
        this.followeeID = followeeID;
    }

    public int getFollowerID() {
        return followerID;
    }

    public int getFolloweeID() {
        return followeeID;
    }

    // Konversi ke string (untuk ditulis ke file teks)
    public String toTextLine() {
        return followerID + "|" + followeeID;
    }

    // Baca objek Follow dari baris teks
    public static Follow fromTextLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format baris tidak valid: " + line);
        }

        int followerID = Integer.parseInt(parts[0]);
        int followeeID = Integer.parseInt(parts[1]);
        return new Follow(followerID, followeeID);
    }

    @Override
    public String toString() {
        return "Follow{followerID=" + followerID + ", followeeID=" + followeeID + "}";
    }
}
