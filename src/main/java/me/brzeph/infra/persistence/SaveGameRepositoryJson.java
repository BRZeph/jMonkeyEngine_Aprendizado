package me.brzeph.infra.persistence;

import me.brzeph.app.ports.SaveGamePort;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SaveGameRepositoryJson implements SaveGamePort {

    // ===== Config padrão =====
    private static final String DEFAULT_DIR_NAME = ".myrpg";
    private static final String DEFAULT_FILE_NAME = "savegame.json";
    private static final int    SCHEMA_VERSION = 1;

    private final Path saveDir;
    private final Path saveFile;
    private final Path backupFile;
    private final ObjectMapper mapper;
    private final ReentrantLock lock = new ReentrantLock(true);

    // ====== Construtores ======
    public SaveGameRepositoryJson() {
        this(getDefaultDir(), DEFAULT_FILE_NAME);
    }

    public SaveGameRepositoryJson(Path directory) {
        this(directory, DEFAULT_FILE_NAME);
    }

    public SaveGameRepositoryJson(Path directory, String fileName) {
        this.saveDir = directory;
        this.saveFile = directory.resolve(fileName);
        this.backupFile = directory.resolve(fileName + ".bak");
        this.mapper = defaultMapper();
        ensureDir();
    }

    // ===== SaveGamePort =====
    @Override
    public void save(Object snapshot) {
        lock.lock();
        try {
            Envelope env = Envelope.of(SCHEMA_VERSION, "1.0.0", snapshot, mapper);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(env);
            atomicWrite(json);
        } catch (IOException e) {
            throw new SaveGameException("Falha ao salvar o jogo", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T load(Class<T> type) {
        lock.lock();
        try {
            if (!Files.exists(saveFile)) {
                if (Files.exists(backupFile)) {
                    return loadFrom(backupFile, type);
                }
                return null;
            }
            return loadFrom(saveFile, type);
        } catch (IOException e) {
            try {
                if (Files.exists(backupFile)) {
                    return loadFrom(backupFile, type);
                }
            } catch (IOException ignored) {}
            throw new SaveGameException("Falha ao carregar o jogo", e);
        } finally {
            lock.unlock();
        }
    }

    // ===== Helpers =====
    private <T> T loadFrom(Path file, Class<T> type) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        Envelope env = mapper.readValue(json, Envelope.class);

        if (env.schemaVersion != SCHEMA_VERSION) {
            throw new SaveGameException("Versão de schema incompatível: " + env.schemaVersion + " (esperado " + SCHEMA_VERSION + ")");
        }

        if (!env.isChecksumValid(mapper)) {
            throw new SaveGameException("Arquivo de save corrompido (checksum inválido).");
        }

        return mapper.convertValue(env.body, type);
    }

    private void atomicWrite(String json) throws IOException {
        ensureDir();

        if (Files.exists(saveFile)) {
            Files.move(saveFile, backupFile, REPLACE_EXISTING);
        }

        Path tmp = Files.createTempFile(saveDir, "save-", ".tmp");
        Files.writeString(tmp, json, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        try {
            Files.move(tmp, saveFile, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, saveFile, REPLACE_EXISTING);
        }
    }

    private void ensureDir() {
        try {
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }
        } catch (IOException e) {
            throw new SaveGameException("Não foi possível criar a pasta de saves: " + saveDir, e);
        }
    }

    private static Path getDefaultDir() {
        String home = System.getProperty("user.home");
        return Paths.get(home, DEFAULT_DIR_NAME);
    }

    private static ObjectMapper defaultMapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.registerModule(new Jdk8Module());
        m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    // ===== Envelope (metadados + body) =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Envelope {
        public int schemaVersion;
        public String gameVersion;
        public long savedAtEpochMillis;
        public JsonNode body;
        public String checksumHex;

        public Envelope() {}

        public static Envelope of(int schemaVersion, String gameVersion, Object bodyObj, ObjectMapper mapper) throws JsonProcessingException {
            Envelope e = new Envelope();
            e.schemaVersion = schemaVersion;
            e.gameVersion = gameVersion;
            e.savedAtEpochMillis = Instant.now().toEpochMilli();
            e.body = mapper.valueToTree(bodyObj);
            e.checksumHex = checksumOf(e.body, mapper);
            return e;
        }

        public boolean isChecksumValid(ObjectMapper mapper) {
            try {
                return checksumHex != null && checksumHex.equals(checksumOf(body, mapper));
            } catch (Exception e) {
                return false;
            }
        }

        private static String checksumOf(JsonNode body, ObjectMapper mapper) throws JsonProcessingException {
            String compact = mapper.writeValueAsString(body);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(compact.getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(digest);
            } catch (Exception ex) {
                throw new RuntimeException("Erro ao calcular checksum", ex);
            }
        }
    }

    // ===== Exceção específica =====
    public static class SaveGameException extends RuntimeException {
        public SaveGameException(String message) { super(message); }
        public SaveGameException(String message, Throwable cause) { super(message, cause); }
    }
}
