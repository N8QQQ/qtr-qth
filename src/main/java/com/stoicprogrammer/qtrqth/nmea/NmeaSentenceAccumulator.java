package com.stoicprogrammer.qtrqth.nmea;

import java.util.Optional;

public class NmeaSentenceAccumulator {
    private StringBuilder buffer = new StringBuilder();

    /**
     * Accumulates bytes into a complete NMEA sentence.
     * @param b The byte to add.
     * @return An Optional containing the sentence if finished, empty otherwise.
     */
    public Optional<String> process(byte b) {
        char c = (char) b;
        if (c == '$') {
            buffer.setLength(0);
            buffer.append(c);
        } else if (c == '\n' || c == '\r') {
            if (buffer.length() > 0 && buffer.charAt(0) == '$') {
                String sentence = buffer.toString();
                buffer.setLength(0);
                return Optional.of(sentence);
            }
        } else if (buffer.length() > 0 && buffer.charAt(0) == '$') {
            buffer.append(c);
        }
        return Optional.empty();
    }
}

