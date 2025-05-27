package service;

import model.Post;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

public class ColdDataManager {
    private static final String COLD_FILE = "data/cold/all_users_cold_data.jsonl";
    private static final Gson gson = new GsonBuilder().create();

    // Simpan satu post ke cold data
    public static void saveToCold(Post post) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COLD_FILE, true))) {
            String jsonLine = gson.toJson(post);
            writer.write(jsonLine);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Gagal menyimpan post ke cold data: " + e.getMessage());
        }
    }

    // Ambil semua post dari cold data
    public static List<Post> getAllColdPosts() {
        List<Post> posts = new ArrayList<>();
        File file = new File(COLD_FILE);
        if (!file.exists()) return posts;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Post post = gson.fromJson(line, Post.class);
                posts.add(post);
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca cold data: " + e.getMessage());
        }

        return posts;
    }

    // Ambil post dari cold berdasarkan rentang tanggal (timestamp)
    public static List<Post> getPostsInRange(long start, long end) {
        List<Post> all = getAllColdPosts();
        List<Post> result = new ArrayList<>();
        for (Post p : all) {
            if (p.getTimestamp() >= start && p.getTimestamp() <= end) {
                result.add(p);
            }
        }
        return result;
    }

    // Migrasi dari hot ke cold jika post lebih lama dari hari ini
    public static void archiveOldHotPosts() {
        File dir = new File("data/hot/");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".msgpack"));
        if (files == null) return;

        long today = getStartOfToday();

        for (File f : files) {
            try {
                List<Post> allPosts = HotDataManager.readMsgpackPosts(f);
                List<Post> keep = new ArrayList<>();
                boolean migrated = false;

                for (Post p : allPosts) {
                    if (p.getTimestamp() < today) {
                        saveToCold(p);
                        migrated = true;
                    } else {
                        keep.add(p);
                    }
                }

                // Rewrite file jika ada perubahan
                if (migrated) {
                    if (keep.isEmpty()) {
                        f.delete();
                    } else {
                        HotDataManager.writeMsgpackPosts(f, keep);
                    }
                }

            } catch (IOException e) {
                System.out.println("❌ Gagal migrasi dari file: " + f.getName());
            }
        }

        System.out.println("✅ Migrasi dari hot ke cold selesai.");
    }

    // Ambil timestamp awal hari ini (jam 00:00)
    private static long getStartOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
