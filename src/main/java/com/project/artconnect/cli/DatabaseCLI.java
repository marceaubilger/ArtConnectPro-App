package com.project.artconnect.cli;

import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.Scanner;

/**
 * Terminal CLI for ArtConnect Pro database objects.
 *
 * Run from the project root:
 *   mvn compile exec:java -Dexec.mainClass="com.project.artconnect.cli.DatabaseCLI"
 *
 * Or after packaging:
 *   java -cp target/classes:$(ls target/dependency/*.jar | tr '\n' ':') \
 *        com.project.artconnect.cli.DatabaseCLI
 */
public class DatabaseCLI {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String GREY   = "\u001B[90m";

    private static final Scanner sc = new Scanner(System.in);

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();
        testConnection();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Choice");
            switch (choice.trim()) {
                case "1"  -> menuViews();
                case "2"  -> menuFunctions();
                case "3"  -> menuProcedures();
                case "0"  -> running = false;
                default   -> warn("Unknown option, try again.");
            }
        }
        System.out.println(GREY + "\nGoodbye." + RESET);
    }

    // ── Menus ─────────────────────────────────────────────────────────────────
    private static void printMainMenu() {
        System.out.println();
        System.out.println(BOLD + CYAN + "╔══════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "║   ArtConnect Pro — DB Objects    ║" + RESET);
        System.out.println(BOLD + CYAN + "╠══════════════════════════════════╣" + RESET);
        System.out.println(CYAN + "║  1. Views                        ║" + RESET);
        System.out.println(CYAN + "║  2. Functions                    ║" + RESET);
        System.out.println(CYAN + "║  3. Stored Procedures            ║" + RESET);
        System.out.println(CYAN + "║  0. Exit                         ║" + RESET);
        System.out.println(BOLD + CYAN + "╚══════════════════════════════════╝" + RESET);
    }

    private static void menuViews() {
        while (true) {
            System.out.println();
            header("VIEWS");
            System.out.println("  1. PublicArtistProfile   — public artist info (hides email/phone)");
            System.out.println("  2. ExhibitionCatalogue   — full exhibition + artwork details");
            System.out.println("  3. ActiveExhibitions     — currently open exhibitions only");
            System.out.println("  0. Back");
            String choice = prompt("Choice");
            switch (choice.trim()) {
                case "1" -> queryView(
                    "PublicArtistProfile",
                    "SELECT * FROM PublicArtistProfile ORDER BY artistName",
                    new String[]{"artistName", "city", "website", "socialMedia", "isActive", "disciplines"});
                case "2" -> queryView(
                    "ExhibitionCatalogue",
                    "SELECT * FROM ExhibitionCatalogue ORDER BY exhibitionTitle, artworkTitle",
                    new String[]{"exhibitionTitle", "startDate", "endDate", "theme",
                                 "galleryName", "artworkTitle", "artworkType", "artworkPrice",
                                 "artistName", "tags"});
                case "3" -> queryView(
                    "ActiveExhibitions",
                    "SELECT * FROM ActiveExhibitions ORDER BY startDate",
                    new String[]{"exhibitionTitle", "startDate", "endDate", "galleryName",
                                 "openingHours", "artworkCount", "artistCount"});
                case "0" -> { return; }
                default  -> warn("Unknown option.");
            }
        }
    }

    private static void menuFunctions() {
        while (true) {
            System.out.println();
            header("FUNCTIONS");
            System.out.println("  1. fn_get_artwork_avg_rating(artwork_title)");
            System.out.println("  2. fn_get_participant_count(workshop_title)");
            System.out.println("  3. fn_is_member_booked(member_name, workshop_title)");
            System.out.println("  0. Back");
            String choice = prompt("Choice");
            switch (choice.trim()) {
                case "1" -> {
                    String title = prompt("Artwork title");
                    callFunction(
                        "SELECT fn_get_artwork_avg_rating(?) AS avg_rating",
                        new Object[]{title},
                        "Average rating for \"" + title + "\"");
                }
                case "2" -> {
                    String ws = prompt("Workshop title");
                    callFunction(
                        "SELECT fn_get_participant_count(?) AS paid_participants",
                        new Object[]{ws},
                        "Paid participant count for \"" + ws + "\"");
                }
                case "3" -> {
                    String member = prompt("Member name");
                    String ws     = prompt("Workshop title");
                    callFunction(
                        "SELECT fn_is_member_booked(?, ?) AS is_booked",
                        new Object[]{member, ws},
                        "Is \"" + member + "\" booked for \"" + ws + "\"?");
                }
                case "0" -> { return; }
                default  -> warn("Unknown option.");
            }
        }
    }

    private static void menuProcedures() {
        while (true) {
            System.out.println();
            header("STORED PROCEDURES");
            System.out.println("  1. sp_create_workshop_with_instructor");
            System.out.println("  2. sp_add_artist_with_discipline");
            System.out.println("  3. sp_workshop_report");
            System.out.println("  0. Back");
            String choice = prompt("Choice");
            switch (choice.trim()) {
                case "1" -> callCreateWorkshop();
                case "2" -> callAddArtist();
                case "3" -> {
                    String ws = prompt("Workshop title");
                    callProcedureMultiResult("CALL sp_workshop_report(?)", new Object[]{ws},
                        "Workshop Report: " + ws);
                }
                case "0" -> { return; }
                default  -> warn("Unknown option.");
            }
        }
    }

    private static void menuTransaction() {
        System.out.println();
        header("DEMO TRANSACTION — Register a member for two workshops atomically");
        System.out.println(GREY + "Both bookings are committed together or rolled back on any error." + RESET);
        System.out.println();

        String member = prompt("Member name");
        String ws1    = prompt("First workshop title");
        String ws2    = prompt("Second workshop title");

        String sql1 = "INSERT INTO booking (booking_date, workshop_title, member_name) VALUES (CURDATE(), ?, ?)";
        String sql2 = "INSERT INTO booking (booking_date, workshop_title, member_name) VALUES (CURDATE(), ?, ?)";

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                    ps1.setString(1, ws1);
                    ps1.setString(2, member);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                    ps2.setString(1, ws2);
                    ps2.setString(2, member);
                    ps2.executeUpdate();
                }
                conn.commit();
                success("Transaction committed — \"" + member + "\" booked for \"" + ws1 + "\" and \"" + ws2 + "\".");
            } catch (SQLException e) {
                conn.rollback();
                error("Transaction rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            error("Connection error: " + e.getMessage());
        }
    }

    // ── Procedure helpers ─────────────────────────────────────────────────────
    private static void callCreateWorkshop() {
        System.out.println();
        System.out.println(GREY + "Fill in workshop details:" + RESET);
        String title       = prompt("Title");
        String date        = prompt("Date & time (YYYY-MM-DD HH:MM:SS)");
        String duration    = prompt("Duration (minutes)");
        String maxPart     = prompt("Max participants");
        String price       = prompt("Price");
        String location    = prompt("Location");
        String description = prompt("Description");
        String level       = prompt("Level (Beginner/Intermediate/Advanced)");
        String instructor  = prompt("Instructor name (must be an active artist)");

        String sql = "CALL sp_create_workshop_with_instructor(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, title);
            cs.setString(2, date);
            cs.setInt   (3, Integer.parseInt(duration));
            cs.setInt   (4, Integer.parseInt(maxPart));
            cs.setDouble(5, Double.parseDouble(price));
            cs.setString(6, location);
            cs.setString(7, description);
            cs.setString(8, level);
            cs.setString(9, instructor);
            cs.execute();
            printResultSets(cs, "Create Workshop");
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private static void callAddArtist() {
        System.out.println();
        System.out.println(GREY + "Fill in artist details:" + RESET);
        String name       = prompt("Name");
        String birthYear  = prompt("Birth year");
        String email      = prompt("Email");
        String phone      = prompt("Phone");
        String city       = prompt("City");
        String website    = prompt("Website");
        String discipline = prompt("Discipline");

        String sql = "CALL sp_add_artist_with_discipline(?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, name);
            cs.setInt   (2, Integer.parseInt(birthYear));
            cs.setString(3, email);
            cs.setString(4, phone);
            cs.setString(5, city);
            cs.setString(6, website);
            cs.setString(7, discipline);
            cs.execute();
            printResultSets(cs, "Add Artist");
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    // ── Generic query helpers ─────────────────────────────────────────────────
    private static void queryView(String viewName, String sql, String[] cols) {
        header("VIEW: " + viewName);
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            printTable(rs, cols);
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private static void callFunction(String sql, Object[] params, String label) {
        header("FUNCTION: " + label);
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                printTable(rs, null);
            }
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private static void callProcedureMultiResult(String sql, Object[] params, String label) {
        header("PROCEDURE: " + label);
        try (Connection conn = ConnectionManager.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) cs.setObject(i + 1, params[i]);
            cs.execute();
            printResultSets(cs, label);
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    // ── Rendering helpers ─────────────────────────────────────────────────────
    private static void printResultSets(CallableStatement cs, String label) throws SQLException {
        int resultIndex = 1;
        ResultSet rs = cs.getResultSet();
        while (rs != null) {
            System.out.println(GREY + "── Result set " + resultIndex++ + " ──" + RESET);
            printTable(rs, null);
            if (!cs.getMoreResults()) break;
            rs = cs.getResultSet();
        }
    }

    private static void printTable(ResultSet rs, String[] cols) throws SQLException {
        ResultSetMetaData meta  = rs.getMetaData();
        int               count = meta.getColumnCount();

        // Use metadata column names if no explicit list provided
        String[] headers;
        if (cols != null && cols.length == count) {
            headers = cols;
        } else {
            headers = new String[count];
            for (int i = 1; i <= count; i++) headers[i - 1] = meta.getColumnLabel(i);
        }

        // Collect rows
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        while (rs.next()) {
            String[] row = new String[count];
            for (int i = 1; i <= count; i++) {
                Object v = rs.getObject(i);
                row[i - 1] = v == null ? "NULL" : v.toString();
            }
            rows.add(row);
        }

        if (rows.isEmpty()) {
            System.out.println(GREY + "  (no rows)" + RESET);
            return;
        }

        // Column widths
        int[] widths = new int[count];
        for (int i = 0; i < count; i++) widths[i] = headers[i].length();
        for (String[] row : rows)
            for (int i = 0; i < count; i++)
                widths[i] = Math.max(widths[i], row[i].length());

        // Print header
        printRow(headers, widths, BOLD + CYAN);
        printSeparator(widths);
        // Print data
        for (String[] row : rows) printRow(row, widths, RESET);
        System.out.println(GREY + "  " + rows.size() + " row(s)" + RESET);
    }

    private static void printRow(String[] cells, int[] widths, String color) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < cells.length; i++) {
            sb.append(color)
              .append(String.format("%-" + widths[i] + "s", cells[i]))
              .append(RESET)
              .append("  ");
        }
        System.out.println(sb);
    }

    private static void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("  ");
        for (int w : widths) sb.append(GREY).append("─".repeat(w)).append(RESET).append("  ");
        System.out.println(sb);
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println(BOLD + GREEN);
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║       ArtConnect Pro  —  Database CLI     ║");
        System.out.println("║   Views · Functions · Procedures · Txn    ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private static void testConnection() {
        try (Connection conn = ConnectionManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                success("Connected to: " + conn.getMetaData().getURL());
            }
        } catch (SQLException e) {
            error("Cannot connect to database: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void header(String text) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "── " + text + " " + "─".repeat(Math.max(0, 42 - text.length())) + RESET);
    }

    private static String prompt(String label) {
        System.out.print(CYAN + "  " + label + ": " + RESET);
        return sc.nextLine().trim();
    }

    private static void success(String msg) { System.out.println(GREEN  + "  ✓ " + msg + RESET); }
    private static void warn   (String msg) { System.out.println(YELLOW + "  ⚠ " + msg + RESET); }
    private static void error  (String msg) { System.out.println(RED    + "  ✗ " + msg + RESET); }
}
