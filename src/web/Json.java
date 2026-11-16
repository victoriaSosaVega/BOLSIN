package web;

import java.util.*;
import java.util.stream.Collectors;

/** Codifica únicamente los tipos simples de los DTO de la boundary. */
final class Json {
    private Json() { }
    static String escribir(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Boolean || valor instanceof Number) return valor.toString();
        if (valor instanceof Map<?, ?> mapa) return mapa.entrySet().stream()
                .map(e -> escribir(e.getKey().toString()) + ":" + escribir(e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
        if (valor instanceof Collection<?> lista) return lista.stream().map(Json::escribir).collect(Collectors.joining(",", "[", "]"));
        StringBuilder result = new StringBuilder("\"");
        for (char c : valor.toString().toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 32) result.append(String.format("\\u%04x", (int)c));
                    else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }
}
