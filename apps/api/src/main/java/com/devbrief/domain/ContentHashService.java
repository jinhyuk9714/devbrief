package com.devbrief.domain;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class ContentHashService {
    public String hash(String title, String url) {
        String input = normalizeTitle(title) + "|" + normalizeUrl(url);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash article identity", ex);
        }
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(url.trim());
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (Exception ignored) {
            return url.trim().replaceAll("([?&]utm_[^=&]+=[^&]+)", "");
        }
    }
}

