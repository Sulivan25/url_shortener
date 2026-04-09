package org.example.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62UtilTest {

    @Test
    void encode_zero_returns_zero_char() {
        assertEquals("0", Base62Util.encode(0));
    }

    @Test
    void encode_positive_value() {
        assertEquals("3d7", Base62Util.encode(12345));
    }

    @Test
    void encode_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> Base62Util.encode(-1));
    }

    @Test
    void decode_valid_string() {
        assertEquals(12345L, Base62Util.decode("3d7"));
    }

    @Test
    void decode_invalid_char_throws() {
        assertThrows(IllegalArgumentException.class, () -> Base62Util.decode("!@#"));
    }

    @Test
    void encode_decode_roundtrip() {
        long[] values = {0, 1, 62, 1000, 999999, Long.MAX_VALUE / 2};
        for (long v : values) {
            assertEquals(v, Base62Util.decode(Base62Util.encode(v)));
        }
    }
}
