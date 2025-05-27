package service;

import model.Post;

import java.io.File;
import java.util.*;

public class SearchService {

    public static List<Post> findPostsBetween(long startTimestamp, long endTimestamp) {
        List<Post> results = new ArrayList<>();

        // 1. Cari dari cold data
        List<Post> coldPosts = ColdDataManager.getPostsInRange(startTimestamp, endTimestamp);
        results.addAll(coldPosts);

        // 2. Cari dari hot data
        File hotDir = new File("data/hot");
        File[] files = hotDir.listFiles((dir, name) -> name.endsWith(".msgpack"));

        if (files != null) {
            for (File file : files) {
                try {
                    List<Post> posts = HotDataManager.readMsgpackPosts(file);
                    for (Post post : posts) {
                        long ts = post.getTimestamp();
                        if (ts >= startTimestamp && ts <= endTimestamp) {
                            results.add(post);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠ Gagal membaca file hot: " + file.getName());
                }
            }
        }

        // Urutkan hasil berdasarkan timestamp terbaru
        results.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        return results;
    }
}
