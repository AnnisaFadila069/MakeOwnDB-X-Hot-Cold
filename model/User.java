package model;

// import java.io.*;

public class User {
    private int id;
    private String username;
    private String password;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Konversi ke baris teks (untuk disimpan)
    public String toTextLine() {
        return id + "|" + username + "|" + password;
    }

    // Membaca User dari baris teks
    public static User fromTextLine(String line) {
        String[] parts = line.split("\\|", -1); // -1 agar tidak abaikan trailing kosong
        if (parts.length != 3) {
            throw new IllegalArgumentException("Format baris tidak valid: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String username = parts[1];
        String password = parts[2];
        return new User(id, username, password);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}
