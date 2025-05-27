package service;

import model.User;
import java.io.*;
// import java.util.*;

public class AuthService {
    private static final String USER_FILE = "data/User.txt";
    private static User loggedInUser = null;

    public static boolean register(int id, String username, String password) {
        if (isUsernameTaken(username)) {
            System.out.println("❌ Username sudah digunakan.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(id + "|" + username + "|" + password);
            writer.newLine();
            System.out.println("✅ Registrasi berhasil!");
            return true;
        } catch (IOException e) {
            System.out.println("❌ Gagal saat menyimpan user: " + e.getMessage());
            return false;
        }
    }

    public static String getLoggedInUsername() {
        return loggedInUser != null ? loggedInUser.getUsername() : null;
    }

    public static int getLoggedInUserId() {
        return loggedInUser != null ? loggedInUser.getId() : -1;
    }    

    public static User login(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    String uname = parts[1];
                    String pass = parts[2];
    
                    if (uname.equals(username) && pass.equals(password)) {
                        loggedInUser = new User(id, uname, pass); // simpan user
                        System.out.println("✅ Login berhasil!");
                        return loggedInUser;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal saat membaca user: " + e.getMessage());
        }
        System.out.println("❌ Username atau password salah.");
        return null;
    }    

    public static String getUsernameById(int userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/User.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Abaikan komentar
                if (line.trim().startsWith("//")) {
                    continue;
                }
    
                String[] parts = line.split("\\|");
                if (parts.length >= 2) { // Ubah dari == 2 jadi >= 2
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        if (id == userId) {
                            return parts[1].trim(); // Ambil username
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Format ID tidak valid di baris: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca file User.txt: " + e.getMessage());
        }
        return "Unknown User";
    }            
    
    private static boolean isUsernameTaken(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2 && parts[1].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Bisa di-log jika perlu
        }
        return false;
    }
}
