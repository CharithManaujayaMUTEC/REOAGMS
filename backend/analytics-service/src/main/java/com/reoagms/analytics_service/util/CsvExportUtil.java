package com.reoagms.analytics_service.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static byte[] singleRecord(List<String> headers, List<?> values) {
        StringBuilder csv = new StringBuilder();
        csv.append(headers.stream().map(CsvExportUtil::escape).reduce((left, right) -> left + "," + right).orElse(""));
        csv.append(System.lineSeparator());
        csv.append(values.stream()
                .map(value -> escape(value == null ? "" : value.toString()))
                .reduce((left, right) -> left + "," + right)
                .orElse(""));
        csv.append(System.lineSeparator());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        String safe = value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
