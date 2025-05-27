package service;

import model.Comment;

import java.io.*;
import java.util.*;

public class CommentService {
    private static final String COMMENT_FILE = "data/Comment.txt";

    public static void addComment(int postId, int userId, String content) {
        long timestamp = System.currentTimeMillis();
        int commentId = generateUniqueCommentId();
        Comment comment = new Comment(commentId, postId, userId, timestamp, content);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COMMENT_FILE, true))) {
            writer.write(comment.toTextLine());  // Simpan dengan format standar
            writer.newLine();
            System.out.println("💬 Komentar berhasil ditambahkan.");
        } catch (IOException e) {
            System.out.println("❌ Gagal menulis komentar: " + e.getMessage());
        }
    }

    public static int generateUniqueCommentId() {
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(COMMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//") || line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 1) {
                    try {
                        int currentId = Integer.parseInt(parts[0]);
                        maxId = Math.max(maxId, currentId);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        return maxId + 1;
    }

    public static List<Comment> getCommentsByPost(int postId) {
        List<Comment> comments = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(COMMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//") || line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", 5);
                if (parts.length == 5) {
                    try {
                        int cmtId = Integer.parseInt(parts[0]);
                        int pId = Integer.parseInt(parts[1]);
                        int userId = Integer.parseInt(parts[2]);
                        long timestamp = Long.parseLong(parts[3]);
                        String content = parts[4];

                        if (pId == postId) {
                            comments.add(new Comment(cmtId, pId, userId, timestamp, content));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca komentar: " + e.getMessage());
        }

        return comments;
    }
}
