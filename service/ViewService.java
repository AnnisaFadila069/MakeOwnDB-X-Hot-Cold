package service;

import java.util.concurrent.TimeUnit;
import model.Post;
import model.Comment;

import java.io.*;
import java.util.*;

public class ViewService {

    private static final String COMMENT_FILE = "data/Comment.txt";
    private static final String USER_FILE = "data/User.txt";
    private static final Scanner scanner = new Scanner(System.in);

    public static String timeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) return "baru saja";
        else if (minutes == 1) return "1 menit yang lalu";
        else if (minutes < 60) return minutes + " menit yang lalu";

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours == 1) return "1 jam yang lalu";
        else if (hours < 24) return hours + " jam yang lalu";

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days == 1) return "kemarin";
        return days + " hari yang lalu";
    }

    public static void viewPostWithComments(int postId) {
        Set<Integer> shownCommentIds = new HashSet<>();

        Post post = findPostById(postId);
        if (post == null) {
            System.out.println("❌ Post tidak ditemukan.");
            return;
        }

        displayPostDetail(post);
        displayComments(postId, shownCommentIds);
    }

    private static Post findPostById(int postId) {
        // 1. Dari cold data
        for (Post p : ColdDataManager.getAllColdPosts()) {
            if (p.getPostID() == postId) return p;
        }

        // 2. Dari hot data
        File hotDir = new File("data/hot");
        File[] files = hotDir.listFiles((dir, name) -> name.endsWith(".msgpack"));
        if (files != null) {
            for (File f : files) {
                try {
                    List<Post> posts = HotDataManager.readMsgpackPosts(f);
                    for (Post p : posts) {
                        if (p.getPostID() == postId) return p;
                    }
                } catch (IOException e) {
                    System.out.println("⚠ Gagal membaca hot data dari " + f.getName());
                }
            }
        }

        return null;
    }

    private static void displayPostDetail(Post post) {
        System.out.println("📝 Post ID: " + post.getPostID());
        System.out.println("🕒 " + timeAgo(post.getTimestamp()));
        System.out.println("🙋‍♂️ Oleh: @" + getUsernameById(post.getUserID()));
        System.out.println("📅 Timestamp: " + new Date(post.getTimestamp()));
        System.out.println("💬 Konten: " + post.getContent());
        System.out.println("---------------------------");
        System.out.println("💬 Komentar:");
    }

    private static void displayComments(int postId, Set<Integer> shownCommentIds) {
        List<Comment> allComments = loadCommentsForPost(postId, shownCommentIds);
        if (allComments.isEmpty()) {
            System.out.println("ℹ️ Tidak ada komentar untuk post ini.");
            return;
        }

        allComments.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
        Iterator<Comment> iterator = allComments.iterator();
        int displayed = 0;

        while (iterator.hasNext()) {
            while (iterator.hasNext() && displayed < 5) {
                Comment comment = iterator.next();
                shownCommentIds.add(comment.getCommentID());
                displayComment(comment);
                displayed++;
            }

            if (iterator.hasNext()) {
                System.out.print("🔄 Muat lebih banyak komentar? (y/n): ");
                String input = scanner.nextLine();
                if (!input.equalsIgnoreCase("y")) break;
                displayed = 0;
            } else {
                System.out.println("✅ Semua komentar telah ditampilkan.");
            }
        }
    }

    private static List<Comment> loadCommentsForPost(int postId, Set<Integer> excludeIds) {
        List<Comment> comments = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(COMMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//") || line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", 5);
                if (parts.length == 5) {
                    int commentId = Integer.parseInt(parts[0]);
                    int commentPostId = Integer.parseInt(parts[1]);
                    int userId = Integer.parseInt(parts[2]);
                    long timestamp = Long.parseLong(parts[3]);
                    String content = parts[4];

                    if (commentPostId == postId && !excludeIds.contains(commentId)) {
                        comments.add(new Comment(commentId, commentPostId, userId, timestamp, content));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca Comment.txt: " + e.getMessage());
        }

        return comments;
    }

    private static void displayComment(Comment comment) {
        System.out.println("🗨️ Comment by @" + getUsernameById(comment.getUserID()));
        System.out.println("🕒 " + timeAgo(comment.getTimestamp()));
        System.out.println("📅 Timestamp: " + new Date(comment.getTimestamp()));
        System.out.println("💬 " + comment.getContent());
        System.out.println("---------------------------");
    }

    private static String getUsernameById(int userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//")) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    if (Integer.parseInt(parts[0]) == userId) {
                        return parts[1];
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca User.txt: " + e.getMessage());
        }

        return "unknown_user";
    }
}
