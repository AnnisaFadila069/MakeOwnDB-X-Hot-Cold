package service;

import com.google.gson.*;

//import model.Post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.*;
import java.util.*;

public class DatabaseEvolutionService {
    private static final String HOT_DIR = "data/hot/";
    private static final String COLD_FILE = "data/cold/all_users_cold_data.jsonl";
    private static final ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Contoh: Tambah kolom likeCount ke semua data
    public static void addColumnToAll(String columnName, JsonElement defaultValue) {
        evolveColdData((obj) -> {
            if (!obj.has(columnName)) {
                obj.add(columnName, defaultValue.deepCopy());
            }
        });
        evolveHotData((obj) -> {
            if (!obj.has(columnName)) {
                obj.add(columnName, defaultValue.deepCopy());
            }
        });
        System.out.println("✅ Kolom '" + columnName + "' berhasil ditambahkan.");
    }    

    // Contoh: Rename kolom
    public static void renameColumn(String oldName, String newName) {
        evolveColdData((obj) -> {
            if (obj.has(oldName)) {
                JsonElement val = obj.remove(oldName);
                obj.add(newName, val);
            }
        });

        evolveHotData((obj) -> {
            if (obj.has(oldName)) {
                JsonElement val = obj.remove(oldName);
                obj.add(newName, val);
            }
        });

        System.out.println("✅ Kolom '" + oldName + "' berhasil diubah menjadi '" + newName + "'.");
    }

    // Contoh: Hapus kolom
    public static void removeColumn(String columnName) {
        evolveColdData((obj) -> obj.remove(columnName));
        evolveHotData((obj) -> obj.remove(columnName));
        System.out.println("✅ Kolom '" + columnName + "' berhasil dihapus.");
    }

    // Helper untuk file cold
    private static void evolveColdData(DataMutator mutator) {
        File cold = new File(COLD_FILE);
        if (!cold.exists()) return;

        List<JsonObject> mutated = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(cold))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                mutator.mutate(obj);
                mutated.add(obj);
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal membaca cold data: " + e.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cold))) {
            for (JsonObject obj : mutated) {
                writer.write(gson.toJson(obj));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Gagal menulis cold data: " + e.getMessage());
        }
    }

    // Helper untuk semua file hot
    private static void evolveHotData(DataMutator mutator) {
        File[] files = new File(HOT_DIR).listFiles((d, n) -> n.endsWith(".msgpack"));
        if (files == null) return;
    
        for (File f : files) {
            try (InputStream in = new FileInputStream(f)) {
                List<Map<String, Object>> rawList = msgpackMapper.readValue(
                    in, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
    
                List<Map<String, Object>> updatedList = new ArrayList<>();
                for (Map<String, Object> raw : rawList) {
                    JsonObject obj = gson.toJsonTree(raw).getAsJsonObject();
                    mutator.mutate(obj);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> updatedMap = gson.fromJson(obj, Map.class);
                    updatedList.add(updatedMap);
                }
    
                try (OutputStream out = new FileOutputStream(f)) {
                    msgpackMapper.writeValue(out, updatedList);
                }
    
            } catch (IOException e) {
                System.out.println("❌ Gagal evolusi file hot: " + f.getName() + " → " + e.getMessage());
            }
        }
    }    

    // Interface untuk modifikasi json
    private interface DataMutator {
        void mutate(JsonObject obj);
    }

    public static void printAvailableColumns() {
        Set<String> columns = new TreeSet<>();
    
        // Cek dari cold
        File cold = new File(COLD_FILE);
        if (cold.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cold))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                    columns.addAll(obj.keySet());
                    break; // cukup ambil satu baris representatif
                }
            } catch (IOException e) {
                System.out.println("❌ Gagal baca cold data: " + e.getMessage());
            }
        }
    
        // Cek dari hot
        File[] files = new File(HOT_DIR).listFiles((d, n) -> n.endsWith(".msgpack"));
        if (files != null) {
            for (File f : files) {
                try (InputStream in = new FileInputStream(f)) {
                    List<Map<String, Object>> list = msgpackMapper.readValue(
                        in,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                        );
                    if (!list.isEmpty()) {
                        JsonObject obj = gson.toJsonTree(list.get(0)).getAsJsonObject();
                        columns.addAll(obj.keySet());
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("⚠ Gagal baca hot data dari: " + f.getName() + " → " + e.getMessage());
                }
            }
        }
    
        if (columns.isEmpty()) {
            System.out.println("⚠ Tidak ada kolom yang terdeteksi.");
        } else {
            System.out.println("📌 Kolom yang tersedia:");
            for (String col : columns) {
                System.out.println("• " + col);
            }
        }
    }       

}