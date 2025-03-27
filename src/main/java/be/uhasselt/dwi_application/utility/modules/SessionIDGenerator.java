package be.uhasselt.dwi_application.utility.modules;

public class SessionIDGenerator {
    public static String generateSessionId() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd-HHmmss"));
    }
}

