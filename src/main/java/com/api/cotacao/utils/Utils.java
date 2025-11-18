package com.api.cotacao.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Utils {
	
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {}
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final DateTimeFormatter MES_ANO_FORMATTER =
            DateTimeFormatter.ofPattern("MM/yyyy", Locale.US);
    
    private static final DateTimeFormatter ISO_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    
    private static final DateTimeFormatter ANO_MES_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM", Locale.US);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static void validar(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Parâmetro obrigatório não informado.");
        }
        if (valor.isBlank()) {
            throw new IllegalArgumentException("Parâmetro obrigatório está em branco.");
        }
    }

    private static String limpar(String valor) {
            validar(valor);

            String limpo = valor.replaceAll("[^0-9/]", "");
            validar(limpo);

            if (limpo.length() != 7) {
                log.error("Data inválida. Formato esperado mm/yyyy ou yyyy/MM, formato recebido = " + valor);
            }

            int posBarra = limpo.indexOf('/');
            // aceitando "MM/yyyy" (pos=2) ou "yyyy/MM" (pos=4)
            if (posBarra != 2 && posBarra != 4) {
                log.error("Data inválida, formato ja limpo ficou  = " + limpo);
            }

            return limpo;
    }

    // ==========
    // DATAS
    // ==========
    public static LocalDate parseMesAno(String mesAno) {
    	YearMonth ym = YearMonth.parse(limpar(mesAno), MES_ANO_FORMATTER);
    	return ym.atDay(1);
    }
    
    public static LocalDate parseAnoMes(String anoMes) {
        YearMonth ym = YearMonth.parse(limpar(anoMes), ANO_MES_FORMATTER);
        // retorna o primeiro dia daquele ano/mês
        return ym.atDay(1);
    }
    

    public static LocalDate parseIsoDate(String isoDate) {
        return LocalDate.parse(limpar(isoDate), ISO_DATE_FORMATTER);
    }

    public static String formatIsoDate(LocalDate date) {
        Objects.requireNonNull(date, "date não pode ser nulo");
        return date.format(ISO_DATE_FORMATTER);
    }

    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }

    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate lastDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static String formatLocalDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }


    // ========================================================================
    // NÚMEROS
    // ========================================================================
    public static BigDecimal toBigDecimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String norm = s.trim().replace(".", "").replace(",", ".");
        return new BigDecimal(norm);
    }

    public static BigDecimal round(BigDecimal value, int scale) {
        if (value == null) {
            return null;
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }


    // ========================================================================
    // ARQUIVOS
    // ========================================================================
    public static String readFileUtf8(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static void writeFileUtf8(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static void ensureDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
    }


    // ========================================================================
    // JSON
    // ========================================================================
    public static <T> T readJson(Path path, Class<T> clazz) throws IOException {
        String content = readFileUtf8(path);
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static <T> List<T> readJsonList(Path path, Class<T> elementClass) throws IOException {
        String content = readFileUtf8(path);
        return OBJECT_MAPPER.readValue(content,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
    }


    // === JSON genérico para arquivos ===
    public static <T> T parseJson(String path, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(path), clazz);
    }

    public static <T> T parseJson(String path, JavaType type) throws IOException {
        return mapper.readValue(new File(path), type);
    }

    public static JavaType getListType(Class<?> clazz) {
        return mapper.getTypeFactory().constructCollectionType(List.class, clazz);
    }

    public static void saveJson(String path, Object data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), data);
    }


    // ========================================================================
    // STRINGS
    // ========================================================================
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    public static String safeString(String s) {
        return s == null ? "" : s.trim();
    }

    public static int safeInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double safeDouble(String s) {
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String repeat(String s, int times) {
        if (s == null || times <= 0) {
            return "";
        }
        return s.repeat(times);
    }

    public static <T> T notNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException("Campo obrigatório: " + name);
        }
        return obj;
    }
    // ========================================================================
    // OUTROS
    // ========================================================================
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static <T> List<T> fromIterable(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }

    public static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    public static String slugify(String input) {
        String noAccent = normalize(input);
        return noAccent.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

}
