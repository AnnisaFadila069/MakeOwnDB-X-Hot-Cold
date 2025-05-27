package service;

import model.Post;
import java.io.*;
import java.util.*;

public class TimelineService {
    private static final String FOLLOW_FILE = "data/Follow.txt";

    public static List<Post> getUserTimeline(int userId, int offset, int limit) {
        List<Post> timeline = new ArrayList<>();
        Set<Integer> followedUsers = getFollowedUsers(userId);

        // 1. Dari Cold Data
        for (Post post : ColdDataManager.getAllColdPosts()) {
            if (followedUsers.contains(post.getUserID()) || post.getUserID() == userId) {
                timeline.add(post);
            }
        }

        // 2. Dari Hot Data
        File dir = new File("data/hot");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".msgpack"));
        if (files != null) {
            for (File f : files) {
                try {
                    List<Post> posts = HotDataManager.readMsgpackPosts(f);
                    for (Post p : posts) {
                        if (followedUsers.contains(p.getUserID()) || p.getUserID() == userId) {
                            timeline.add(p);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("⚠️ Gagal baca hot data dari: " + f.getName());
                }
            }
        }

        // Urutkan berdasarkan timestamp terbaru
        timeline.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        // Pagination
        if (offset >= timeline.size()) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(offset + limit, timeline.size());
        return timeline.subList(offset, toIndex);
    }

    private static Set<Integer> getFollowedUsers(int userId) {
        Set<Integer> followedUsers = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FOLLOW_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//")) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    int followerId = Integer.parseInt(parts[0]);
                    int followeeId = Integer.parseInt(parts[1]);
                    if (followerId == userId) {
                        followedUsers.add(followeeId);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca data follow: " + e.getMessage());
        }

        return followedUsers;
    }
}
