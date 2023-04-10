package de.emilius123.noteclose.util;

public class Path {
    public static class Web {
        public static final String AUTH_PREFIX = "/auth/";
        public static final String OSM_LOGIN = AUTH_PREFIX + "login";
        public static final String OAUTH_CALLBACK = AUTH_PREFIX + "oauth_complete";
        public static final String OSM_LOGOUT = "/logout";

        private static final String NOTE_PREFIX = "/note/";
        public static final String NOTE_SCHEDULE = NOTE_PREFIX + "schedule";
        public static final String NOTE_CANCEL = NOTE_PREFIX + "cancel";
        public static final String NOTE_CLOSE = NOTE_PREFIX + "close";
    }
}
