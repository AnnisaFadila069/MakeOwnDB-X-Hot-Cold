package service;

import model.Follow;

import java.io.*;
import java.util.*;

public class FollowService {
    private static final String FOLLOW_FILE = "data/Follow.txt";

    public static void follow(int followerId, int followeeId) {
        // Pengecekan untuk memastikan tidak bisa follow diri sendiri
        if (followerId == followeeId) {
            System.out.println("❌ Kamu tidak bisa follow diri sendiri.");
            return;
        }

        if (isFollowing(followerId, followeeId)) {
            System.out.println("⚠️ Kamu sudah follow user ini.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FOLLOW_FILE, true))) {
            writer.write(followerId + "|" + followeeId);
            writer.newLine();
            System.out.println("✅ Follow berhasil.");
        } catch (IOException e) {
            System.out.println("❌ Gagal follow: " + e.getMessage());
        }
    }

    public static void unfollow(int followerId, int followeeId) {
        List<Follow> allFollows = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(FOLLOW_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Abaikan komentar yang dimulai dengan "//"
                if (line.trim().startsWith("//")) {
                    continue;  // Skip komentar
                }
    
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    try {
                        int fId = Integer.parseInt(parts[0]);
                        int feId = Integer.parseInt(parts[1]);
                        if (!(fId == followerId && feId == followeeId)) {
                            allFollows.add(new Follow(fId, feId));
                        }
                    } catch (NumberFormatException e) {
                        // Skip baris yang tidak valid
                        System.out.println("❌ Data tidak valid: " + line);
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca data follow: " + e.getMessage());
            return;
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FOLLOW_FILE))) {
            for (Follow f : allFollows) {
                writer.write(f.getFollowerID() + "|" + f.getFolloweeID());
                writer.newLine();
            }
            System.out.println("✅ Unfollow berhasil.");
        } catch (IOException e) {
            System.out.println("❌ Gagal menulis ulang follow: " + e.getMessage());
        }
    }    

    public static boolean isFollowing(int followerId, int followeeId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FOLLOW_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Mengabaikan baris komentar (dimulai dengan "//")
                if (line.trim().startsWith("//")) {
                    continue;
                }
    
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    try {
                        int fId = Integer.parseInt(parts[0]);
                        int feId = Integer.parseInt(parts[1]);
                        if (fId == followerId && feId == followeeId) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Format data tidak valid di baris: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca data follow: " + e.getMessage());
        }
        return false;
    }    

    public static List<Integer> getFollowingList(int userId) {
        List<Integer> following = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FOLLOW_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    int fId = Integer.parseInt(parts[0]);
                    int feId = Integer.parseInt(parts[1]);
                    if (fId == userId) {
                        following.add(feId);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal mengambil data follow: " + e.getMessage());
        }
        return following;
    }
}
