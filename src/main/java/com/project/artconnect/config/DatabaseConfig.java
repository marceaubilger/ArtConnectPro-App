package com.project.artconnect.config;


public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    // Local default target database.
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "password.";

    public static final String URL = System.getProperty(
            "artconnect.db.url",
            System.getenv().getOrDefault("ARTCONNECT_DB_URL", DEFAULT_URL));

    public static final String USER = System.getProperty(
            "artconnect.db.user",
            System.getenv().getOrDefault("ARTCONNECT_DB_USER", DEFAULT_USER));

    public static final String PASSWORD = System.getProperty(
            "artconnect.db.password",
            System.getenv().getOrDefault("ARTCONNECT_DB_PASSWORD", DEFAULT_PASSWORD));
}
