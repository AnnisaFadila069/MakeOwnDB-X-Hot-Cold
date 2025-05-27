package service;

import model.Post;
import model.User;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class PostService {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void createPost(int postId, int userId, String content) {
        long timestamp = System.currentTimeMillis();
        Post post = new Post(postId, userId, timestamp, content);

        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (timestamp < startOfToday) {
            ColdDataManager.saveToCold(post);
            System.out.println("❄ Post lama disimpan ke cold data.");
        } else {
            HotDataManager.savePostToHot(post);
        }
    }

    public static int generateUniquePostId() {
        int maxId = 0;

        // Cek cold
        for (Post p : ColdDataManager.getAllColdPosts()) {
            maxId = Math.max(maxId, p.getPostID());
        }

        // Cek hot
        File hotDir = new File("data/hot");
        File[] files = hotDir.listFiles((d, name) -> name.endsWith(".msgpack"));
        if (files != null) {
            for (File f : files) {
                try {
                    for (Post p : HotDataManager.readMsgpackPosts(f)) {
                        maxId = Math.max(maxId, p.getPostID());
                    }
                } catch (IOException ignored) {}
            }
        }

        return maxId + 1;
    }

    public static void listUserPosts(int userId) {
        boolean found = false;

        System.out.println("🧊 Post dari cold data:");
        for (Post post : ColdDataManager.getAllColdPosts()) {
            if (post.getUserID() == userId) {
                printPost(post);
                found = true;
            }
        }

        System.out.println("🔥 Post dari hot data:");
        for (Post post : HotDataManager.getHotPostsByUser(userId)) {
            printPost(post);
            found = true;
        }

        if (!found) {
            System.out.println("ℹ️ Belum ada post untuk user ini.");
        }
    }

    private static void printPost(Post post) {
        System.out.println("📝 Post ID: " + post.getPostID());
        System.out.println("📅 Timestamp: " + new Date(post.getTimestamp()));
        System.out.println("💬 Konten: " + post.getContent());
        System.out.println("---------------------------");
    }

    public static boolean deletePost(int postId) {
        boolean deleted = false;

        if (currentUser == null) {
            System.out.println("⚠️ Kamu belum login.");
            return false;
        }

        int userId = currentUser.getId();

        // Hapus dari hot
        File hotDir = new File("data/hot");
        File[] files = hotDir.listFiles((dir, name) -> name.startsWith(userId + "_") && name.endsWith(".msgpack"));
        if (files != null) {
            for (File f : files) {
                try {
                    List<Post> posts = HotDataManager.readMsgpackPosts(f);
                    List<Post> updated = new ArrayList<>();
                    boolean changed = false;

                    for (Post p : posts) {
                        if (p.getPostID() == postId && p.getUserID() == userId) {
                            changed = true;
                            deleted = true;
                        } else {
                            updated.add(p);
                        }
                    }

                    if (changed) {
                        if (updated.isEmpty()) f.delete();
                        else HotDataManager.writeMsgpackPosts(f, updated);
                    }
                } catch (IOException e) {
                    System.out.println("❌ Gagal menghapus dari hot: " + f.getName());
                }
            }
        }

        // Hapus dari cold
        File coldFile = new File("data/cold/all_users_cold_data.jsonl");
        if (coldFile.exists()) {
            List<Post> updated = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(coldFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Post p = new com.google.gson.Gson().fromJson(line, Post.class);
                    if (p.getPostID() == postId && p.getUserID() == userId) {
                        deleted = true;
                        continue;
                    }
                    updated.add(p);
                }
            } catch (IOException e) {
                System.out.println("❌ Gagal membaca cold data: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(coldFile))) {
                for (Post p : updated) {
                    writer.write(new com.google.gson.Gson().toJson(p));
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("❌ Gagal menulis ulang cold data: " + e.getMessage());
            }
        }

        // Hapus komentar
        File commentFile = new File("data/Comment.txt");
        if (commentFile.exists()) {
            List<String> updated = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(commentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("//") || line.trim().isEmpty()) {
                        updated.add(line);
                        continue;
                    }

                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        try {
                            int pId = Integer.parseInt(parts[1]);
                            if (pId != postId) updated.add(line);
                            else deleted = true;
                        } catch (NumberFormatException e) {
                            updated.add(line);
                        }
                    } else {
                        updated.add(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("❌ Gagal membaca komentar: " + e.getMessage());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(commentFile))) {
                for (String l : updated) {
                    writer.write(l);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("❌ Gagal menulis ulang komentar: " + e.getMessage());
            }
        }

        if (deleted) {
            System.out.println("✅ Post & komentar terkait berhasil dihapus.");
        } else {
            System.out.println("⚠️ Post tidak ditemukan atau bukan milikmu.");
        }

        return deleted;
    }
}
