package com.stoicprogrammer.qtrqth.nmea;

public class NmeaSentenceAccumulator {
    private StringBuilder buffer = new StringBuilder();

    /**
     * Accumulates bytes into a complete NMEA sentence.
     * @param b The byte to add.
     * @return The complete NMEA sentence string (without CRLF) if a sentence is finished, null otherwise.
     */
    public String addByte(byte b) {
        char c = (char) b;
        if (c == '$') {
            buffer.setLength(0);
            buffer.append(c);
        } else if (c == '\n' || c == '\r') {
            if (buffer.length() > 0 && buffer.charAt(0) == '$') {
                String sentence = buffer.toString();
                buffer.setLength(0); // Reset after returning
                return sentence;
            }
        } else if (buffer.length() > 0 && buffer.charAt(0) == '$') {
            buffer.append(c);
        }
        return null;
    }
}
