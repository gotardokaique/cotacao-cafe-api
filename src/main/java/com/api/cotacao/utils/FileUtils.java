package com.api.cotacao.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FileUtils {

    private FileUtils() {}

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_BUFFER = 8192;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.systemDefault());

    // -------------------------
    // Basic file info / path
    // -------------------------
    public static boolean exists(Path p) {
        return Files.exists(p);
    }

    public static boolean exists(String path) {
        return exists(Paths.get(path));
    }

    public static long size(Path p) throws IOException {
        return Files.size(p);
    }

    public static String filename(Path p) {
        return p.getFileName().toString();
    }

    public static String filename(String path) {
        return filename(Paths.get(path));
    }

    public static String extension(Path p) {
        String n = filename(p);
        int i = n.lastIndexOf('.');
        return (i > 0) ? n.substring(i + 1) : "";
    }

    public static String basename(Path p) {
        String n = filename(p);
        int i = n.lastIndexOf('.');
        return (i > 0) ? n.substring(0, i) : n;
    }

    // -------------------------
    // Directories
    // -------------------------
    public static void ensureDir(Path dir) throws IOException {
        if (Files.notExists(dir)) Files.createDirectories(dir);
    }

    public static void ensureDir(String dir) throws IOException {
        ensureDir(Paths.get(dir));
    }

    // -------------------------
    // Read / Write (atomic)
    // -------------------------
    public static String readString(Path path, Charset charset) throws IOException {
        return Files.readString(path, charset);
    }

    public static String readString(Path path) throws IOException {
        return readString(path, UTF8);
    }

    public static void writeStringAtomic(Path path, String content, Charset charset) throws IOException {
        ensureDir(path.getParent());
        Path tmp = Files.createTempFile(path.getParent(), ".tmp-", ".tmp");
        Files.writeString(tmp, content, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void writeStringAtomic(Path path, String content) throws IOException {
        writeStringAtomic(path, content, UTF8);
    }

    public static byte[] readBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public static void writeBytesAtomic(Path path, byte[] data) throws IOException {
        ensureDir(path.getParent());
        Path tmp = Files.createTempFile(path.getParent(), ".tmp-", ".tmp");
        Files.write(tmp, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void appendString(Path path, String line) throws IOException {
        ensureDir(path.getParent());
        Files.writeString(path, line, UTF8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path, UTF8);
    }

    public static void writeLinesAtomic(Path path, List<String> lines) throws IOException {
        writeStringAtomic(path, String.join(System.lineSeparator(), lines), UTF8);
    }

    // -------------------------
    // Streaming large files
    // -------------------------
    public static InputStream openInputStream(Path path) throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    public static OutputStream openOutputStream(Path path) throws IOException {
        ensureDir(path.getParent());
        return Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Stream<String> streamLines(Path path) throws IOException {
        return Files.lines(path, UTF8);
    }

    public static long copyLarge(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER];
        long total = 0;
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
            total += r;
        }
        out.flush();
        return total;
    }

    // -------------------------
    // Safe move/copy/delete
    // -------------------------
    public static void move(Path source, Path target, boolean replace) throws IOException {
        ensureDir(target.getParent());
        if (replace) Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        else Files.move(source, target);
    }

    public static void copy(Path source, Path target, boolean replace) throws IOException {
        ensureDir(target.getParent());
        if (replace) Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        else Files.copy(source, target);
    }

    public static boolean deleteQuietly(Path p) {
        try {
            return Files.deleteIfExists(p);
        } catch (Exception e) {
            return false;
        }
    }

    public static void delete(Path p) throws IOException {
        Files.delete(p);
    }

    // -------------------------
    // Search / listing
    // -------------------------
    public static List<Path> listFilesRecursive(Path root, Predicate<Path> filter) throws IOException {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile).filter(filter).collect(Collectors.toList());
        }
    }

    public static List<Path> findFilesByExtension(Path root, String ext) throws IOException {
        String e = ext.startsWith(".") ? ext.substring(1) : ext;
        return listFilesRecursive(root, p -> extension(p).equalsIgnoreCase(e));
    }

    // -------------------------
    // Sanitize & security
    // -------------------------
    public static Path sanitize(Path base, Path target) {
        Path normalized = target.normalize();
        return base.resolve(normalized).normalize();
    }

    public static boolean ensureInside(Path base, Path candidate) throws IOException {
        Path b = base.toRealPath();
        Path c = candidate.toRealPath();
        return c.startsWith(b);
    }

    public static void ensureReadable(Path p) {
        if (!Files.isReadable(p)) throw new IllegalStateException("File not readable: " + p);
    }

    public static void ensureWritable(Path p) {
        Path parent = p.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                ensureDir(parent);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create parent dir: " + parent, e);
            }
        }
        if (Files.exists(p) && !Files.isWritable(p)) throw new IllegalStateException("File not writable: " + p);
    }

    // -------------------------
    // Zip / Unzip
    // -------------------------
    public static void zipDir(Path sourceDir, Path zipFile) throws IOException {
        ensureDir(zipFile.getParent());
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Path pp = sourceDir.toAbsolutePath();
            Files.walk(pp).filter(Files::isRegularFile).forEach(p -> {
                ZipEntry ze = new ZipEntry(pp.relativize(p).toString().replace('\\', '/'));
                try (InputStream is = Files.newInputStream(p)) {
                    zs.putNextEntry(ze);
                    copyLarge(is, zs);
                    zs.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    public static void unzip(Path zipFile, Path targetDir) throws IOException {
        ensureDir(targetDir);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                Path out = targetDir.resolve(ze.getName()).normalize();
                if (!out.startsWith(targetDir)) throw new IOException("Zip contains illegal entry: " + ze.getName());
                if (ze.isDirectory()) {
                    ensureDir(out);
                } else {
                    ensureDir(out.getParent());
                    try (OutputStream os = Files.newOutputStream(out)) {
                        copyLarge(zis, os);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public static void zipFiles(List<Path> files, Path zipFile) throws IOException {
        ensureDir(zipFile.getParent());
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (Path p : files) {
                if (!Files.isRegularFile(p)) continue;
                ZipEntry ze = new ZipEntry(p.getFileName().toString());
                zs.putNextEntry(ze);
                try (InputStream is = Files.newInputStream(p)) {
                    copyLarge(is, zs);
                }
                zs.closeEntry();
            }
        }
    }

    // -------------------------
    // Temporary files / backups
    // -------------------------
    public static Path temporaryFile(Path dir, String prefix, String suffix) throws IOException {
        ensureDir(dir);
        return Files.createTempFile(dir, prefix, suffix);
    }

    public static Path temporaryDir(Path parent, String prefix) throws IOException {
        ensureDir(parent);
        return Files.createTempDirectory(parent, prefix);
    }

    public static Path backupFile(Path source, Path backupDir) throws IOException {
        ensureDir(backupDir);
        String ts = TS_FMT.format(Instant.now());
        Path dest = backupDir.resolve(basename(source) + "_" + ts + "." + extension(source));
        copy(source, dest, true);
        return dest;
    }

    // -------------------------
    // Rotate / clean
    // -------------------------
    public static void cleanDir(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> s = Files.list(dir)) {
            s.forEach(p -> {
                try {
                    if (Files.isDirectory(p)) deleteRecursive(p);
                    else deleteQuietly(p);
                } catch (IOException ignored) {}
            });
        }
    }

    public static void deleteRecursive(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Path rotateFile(Path file, int keep) throws IOException {
        if (!Files.exists(file)) return file;
        Path dir = file.getParent();
        String base = basename(file);
        String ext = extension(file);
        for (int i = keep - 1; i >= 1; i--) {
            Path from = dir.resolve(base + "_" + i + (ext.isEmpty() ? "" : "." + ext));
            Path to = dir.resolve(base + "_" + (i + 1) + (ext.isEmpty() ? "" : "." + ext));
            if (Files.exists(from)) {
                copy(from, to, true);
            }
        }
        Path first = dir.resolve(base + "_1" + (ext.isEmpty() ? "" : "." + ext));
        copy(file, first, true);
        return first;
    }

    // -------------------------
    // Hash / compare
    // -------------------------
    public static String hashSha256(Path path) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] buf = new byte[DEFAULT_BUFFER];
            while (dis.read(buf) != -1) { /* consume */ }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static boolean compareFilesByHash(Path a, Path b) throws Exception {
        return Objects.equals(hashSha256(a), hashSha256(b));
    }

    public static boolean compareFilesByteByByte(Path a, Path b) throws IOException {
        if (size(a) != size(b)) return false;
        try (InputStream ia = Files.newInputStream(a);
             InputStream ib = Files.newInputStream(b)) {
            byte[] pa = new byte[DEFAULT_BUFFER];
            byte[] pb = new byte[DEFAULT_BUFFER];
            int ra, rb;
            while ((ra = ia.read(pa)) != -1) {
                rb = ib.read(pb);
                if (ra != rb) return false;
                if (!Arrays.equals(pa, pb)) return false;
            }
        }
        return true;
    }

    // -------------------------
    // JSON helpers (file based)
    // -------------------------
    public static <T> T readJson(Path path, Class<T> clazz) throws IOException {
        return MAPPER.readValue(path.toFile(), clazz);
    }

    public static <T> List<T> readJsonList(Path path, Class<T> clazz) throws IOException {
        JavaType jt = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
        return MAPPER.readValue(path.toFile(), jt);
    }

    public static void writeJson(Path path, Object data, boolean pretty) throws IOException {
        ensureDir(path.getParent());
        if (pretty) MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), data);
        else MAPPER.writeValue(path.toFile(), data);
    }
}
