package service;

import model.Post;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.*;
import java.util.*;

public class HotDataManager {
    private static final String HOT_DIR = "data/hot/";
    private static final int CHUNK_LIMIT = 10;

    private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());

    // Simpan post ke hot data dengan chunking otomatis
    public static void savePostToHot(Post post) {
        int userId = post.getUserID();
        try {
            File dir = new File(HOT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 1. Ambil semua chunk milik user
            File[] chunks = dir.listFiles((d, name) ->
                    name.startsWith(userId + "_chunk") && name.endsWith(".msgpack"));

            // 2. Cari chunk terakhir
            int maxChunk = 1;
            if (chunks != null && chunks.length > 0) {
                for (File f : chunks) {
                    String fname = f.getName().replace(userId + "_chunk", "").replace(".msgpack", "");
                    try {
                        int num = Integer.parseInt(fname);
                        maxChunk = Math.max(maxChunk, num);
                    } catch (NumberFormatException ignored) {}
                }
            }

            File lastChunkFile = new File(HOT_DIR + userId + "_chunk" + maxChunk + ".msgpack");
            List<Post> posts = new ArrayList<>();

            if (lastChunkFile.exists()) {
                posts = readMsgpackPosts(lastChunkFile);
            }

            // 3. Jika penuh, buat chunk baru
            if (posts.size() >= CHUNK_LIMIT) {
                maxChunk++;
                posts = new ArrayList<>();
                lastChunkFile = new File(HOT_DIR + userId + "_chunk" + maxChunk + ".msgpack");
            }

            posts.add(post);
            writeMsgpackPosts(lastChunkFile, posts);

            System.out.println("✅ Post disimpan ke hot data: " + lastChunkFile.getName());
        } catch (IOException e) {
            System.out.println("❌ Gagal menyimpan post ke hot data: " + e.getMessage());
        }
    }

    // Membaca file msgpack dan mengembalikan list post
    public static List<Post> readMsgpackPosts(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            Post[] postArray = mapper.readValue(in, Post[].class);
            return new ArrayList<>(Arrays.asList(postArray));
        }
    }

    // Menyimpan list post ke file msgpack
    public static void writeMsgpackPosts(File file, List<Post> posts) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            mapper.writeValue(out, posts);
        }
    }

    // Mendapatkan semua post hot milik user
    public static List<Post> getHotPostsByUser(int userId) {
        List<Post> allPosts = new ArrayList<>();
        File dir = new File(HOT_DIR);
        File[] chunks = dir.listFiles((d, name) ->
                name.startsWith(userId + "_chunk") && name.endsWith(".msgpack"));

        if (chunks != null) {
            Arrays.sort(chunks); // agar urut
            for (File f : chunks) {
                try {
                    allPosts.addAll(readMsgpackPosts(f));
                } catch (IOException e) {
                    System.out.println("⚠ Gagal membaca chunk: " + f.getName());
                }
            }
        }

        return allPosts;
    }
}
