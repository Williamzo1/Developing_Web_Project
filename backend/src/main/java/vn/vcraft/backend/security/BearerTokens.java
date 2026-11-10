package vn.vcraft.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

public final class BearerTokens {
    private static final Pattern HEADER = Pattern.compile("Bearer [a-f0-9-]{72}");

    private BearerTokens() {}

    public static Optional<String> extract(String header) {
        return Optional.ofNullable(header).filter(value -> HEADER.matcher(value).matches())
                .map(value -> value.substring(7));
    }

    public static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
