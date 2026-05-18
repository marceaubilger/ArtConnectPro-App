-- ============================================================
-- ArtConnect Pro — Database Objects
-- Views · Indexes · Triggers · Functions · Procedures
--
-- Run once against artconnect_db to install every object:
--   mysql -u root -p artconnect_db < db_objects.sql
-- ============================================================

-- ─── Optional support tables (booking & review) ──────────────
-- These are not yet wired into the Java app but are required
-- by the triggers and functions below. Safe to run multiple times.

CREATE TABLE IF NOT EXISTS booking (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    booking_date    DATE         NOT NULL,
    workshop_title  VARCHAR(255) NOT NULL,
    member_name     VARCHAR(255) NOT NULL,
    FOREIGN KEY (workshop_title) REFERENCES workshop(title),
    FOREIGN KEY (member_name)    REFERENCES community_member(name)
);

CREATE TABLE IF NOT EXISTS review (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    rating       INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment      VARCHAR(500),
    review_date  DATE,
    artwork_title VARCHAR(255) NOT NULL,
    member_name  VARCHAR(255) NOT NULL,
    FOREIGN KEY (artwork_title) REFERENCES artwork(title),
    FOREIGN KEY (member_name)   REFERENCES community_member(name)
);

-- ============================================================
-- VIEWS
-- ============================================================

-- ─── View 1 · PublicArtistProfile ────────────────────────────
-- Objective: SECURITY — hides contact_email and phone.
-- Exposes only public-safe fields for front-end / API use.
CREATE OR REPLACE VIEW PublicArtistProfile AS
SELECT
    a.name                                                                  AS artistName,
    a.city,
    a.website,
    a.social_media                                                          AS socialMedia,
    a.is_active                                                             AS isActive,
    GROUP_CONCAT(ad.discipline_name ORDER BY ad.discipline_name SEPARATOR ', ') AS disciplines
FROM artist a
LEFT JOIN artist_discipline ad ON a.name = ad.artist_name
GROUP BY
    a.name, a.city, a.website, a.social_media, a.is_active;

-- ─── View 2 · ExhibitionCatalogue ────────────────────────────
-- Objective: QUERY SIMPLIFICATION — pre-joins 6 tables.
-- Simple SELECT replaces complex join for catalogue screens.
CREATE OR REPLACE VIEW ExhibitionCatalogue AS
SELECT
    ex.name                                                                  AS exhibitionTitle,
    DATE(ex.start_date)                                                      AS startDate,
    DATE(ex.end_date)                                                        AS endDate,
    ex.description                                                           AS theme,
    g.name                                                                   AS galleryName,
    g.address                                                                AS galleryAddress,
    a.title                                                                  AS artworkTitle,
    a.type                                                                   AS artworkType,
    a.medium,
    a.dimensions,
    a.price                                                                  AS artworkPrice,
    ar.name                                                                  AS artistName,
    ar.city                                                                  AS artistCity,
    GROUP_CONCAT(t.tag_name ORDER BY t.tag_name SEPARATOR ', ')             AS tags
FROM exhibition ex
JOIN gallery            g   ON ex.gallery_id      = g.id
JOIN exhibition_artwork ea  ON ex.name             = ea.exhibition_name
JOIN artwork            a   ON ea.artwork_title    = a.title
JOIN artist             ar  ON a.artist_name       = ar.name
LEFT JOIN artwork_artwork_tag t ON a.title         = t.artwork_title
GROUP BY
    ex.name, ex.start_date, ex.end_date, ex.description,
    g.name, g.address,
    a.title, a.type, a.medium, a.dimensions, a.price,
    ar.name, ar.city;

-- ─── View 3 · ActiveExhibitions ──────────────────────────────
-- Objective: SECURITY + SIMPLIFICATION — temporal filtering.
-- Encapsulates "currently open" logic; safe for read-only roles.
CREATE OR REPLACE VIEW ActiveExhibitions AS
SELECT
    ex.name                                     AS exhibitionTitle,
    DATE(ex.start_date)                         AS startDate,
    DATE(ex.end_date)                           AS endDate,
    ex.description                              AS theme,
    g.name                                      AS galleryName,
    g.address                                   AS galleryAddress,
    g.opening_hours                             AS openingHours,
    g.contact_phone                             AS contactPhone,
    g.website                                   AS galleryWebsite,
    COUNT(DISTINCT ea.artwork_title)            AS artworkCount,
    COUNT(DISTINCT a.artist_name)               AS artistCount
FROM exhibition ex
JOIN gallery            g  ON ex.gallery_id   = g.id
JOIN exhibition_artwork ea ON ex.name          = ea.exhibition_name
JOIN artwork            a  ON ea.artwork_title = a.title
WHERE DATE(ex.start_date) <= CURRENT_DATE
  AND DATE(ex.end_date)   >= CURRENT_DATE
GROUP BY
    ex.name, ex.start_date, ex.end_date, ex.description,
    g.name, g.address, g.opening_hours, g.contact_phone, g.website;

-- ============================================================
-- INDEXES
-- ============================================================

DROP PROCEDURE IF EXISTS sp_create_indexes;
DELIMITER $$
CREATE PROCEDURE sp_create_indexes()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE table_schema = DATABASE() AND table_name = 'artwork'
                   AND index_name = 'idx_artwork_artist_name') THEN
        CREATE INDEX idx_artwork_artist_name ON artwork(artist_name);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE table_schema = DATABASE() AND table_name = 'booking'
                   AND index_name = 'idx_booking_workshop_title') THEN
        CREATE INDEX idx_booking_workshop_title ON booking(workshop_title);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE table_schema = DATABASE() AND table_name = 'booking'
                   AND index_name = 'idx_booking_member_name') THEN
        CREATE INDEX idx_booking_member_name ON booking(member_name);
    END IF;
END$$
DELIMITER ;
CALL sp_create_indexes();
DROP PROCEDURE sp_create_indexes;

-- ============================================================
-- TRIGGERS
-- ============================================================

DROP TRIGGER IF EXISTS trg_check_workshop_capacity;
DROP TRIGGER IF EXISTS trg_check_exhibition_dates_insert;
DROP TRIGGER IF EXISTS trg_check_exhibition_dates_update;
DROP TRIGGER IF EXISTS trg_check_booking_date;

DELIMITER $$

-- ─── Trigger 1 · Workshop capacity check ─────────────────────
CREATE TRIGGER trg_check_workshop_capacity
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE current_count INT;
    DECLARE max_cap       INT;

    SELECT COUNT(*)        INTO current_count FROM booking  WHERE workshop_title  = NEW.workshop_title;
    SELECT max_participants INTO max_cap       FROM workshop WHERE title           = NEW.workshop_title;

    IF current_count >= max_cap THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Workshop is fully booked. No more participants can be added.';
    END IF;
END$$

-- ─── Trigger 2 · Exhibition date validity (INSERT) ───────────
CREATE TRIGGER trg_check_exhibition_dates_insert
BEFORE INSERT ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Exhibition end date cannot be before the start date.';
    END IF;
END$$

-- ─── Trigger 2 · Exhibition date validity (UPDATE) ───────────
CREATE TRIGGER trg_check_exhibition_dates_update
BEFORE UPDATE ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Exhibition end date cannot be before the start date.';
    END IF;
END$$

-- ─── Trigger 3 · Prevent booking a past workshop ─────────────
CREATE TRIGGER trg_check_booking_date
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE workshop_date DATE;
    SELECT DATE(date) INTO workshop_date FROM workshop WHERE title = NEW.workshop_title;

    IF workshop_date < CURDATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot book a workshop that has already taken place.';
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- FUNCTIONS
-- ============================================================

DROP FUNCTION IF EXISTS fn_get_artwork_avg_rating;
DROP FUNCTION IF EXISTS fn_get_participant_count;
DROP FUNCTION IF EXISTS fn_is_member_booked;

DELIMITER $$

-- ─── Function 1 · Average artwork rating ─────────────────────
CREATE FUNCTION fn_get_artwork_avg_rating(p_artwork_title VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci)
RETURNS DECIMAL(4,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE avg_rating DECIMAL(4,2);
    SELECT ROUND(AVG(rating), 2) INTO avg_rating
    FROM review
    WHERE artwork_title = p_artwork_title;
    RETURN COALESCE(avg_rating, 0.00);
END$$

-- ─── Function 2 · Confirmed participant count ────────────────
CREATE FUNCTION fn_get_participant_count(p_workshop_title VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE participant_count INT;
    SELECT COUNT(*) INTO participant_count
    FROM booking
    WHERE workshop_title = p_workshop_title;
    RETURN participant_count;
END$$

-- ─── Function 3 · Check member already booked ────────────────
CREATE FUNCTION fn_is_member_booked(p_member_name VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci, p_workshop_title VARCHAR(255) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE booking_exists INT;
    SELECT COUNT(*) INTO booking_exists
    FROM booking
    WHERE member_name    = p_member_name
      AND workshop_title = p_workshop_title;
    RETURN booking_exists > 0;
END$$

DELIMITER ;

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DROP PROCEDURE IF EXISTS sp_create_workshop_with_instructor;
DROP PROCEDURE IF EXISTS sp_add_artist_with_discipline;
DROP PROCEDURE IF EXISTS sp_workshop_report;

DELIMITER $$

-- ─── Procedure 1 · Create workshop + assign instructor ───────
CREATE PROCEDURE sp_create_workshop_with_instructor(
    IN p_title            VARCHAR(255),
    IN p_date             DATETIME,
    IN p_duration_minutes INT,
    IN p_max_participants INT,
    IN p_price            DOUBLE,
    IN p_location         VARCHAR(255),
    IN p_description      VARCHAR(500),
    IN p_level            VARCHAR(50),
    IN p_instructor_name  VARCHAR(255)
)
BEGIN
    DECLARE artist_exists INT;
    SELECT COUNT(*) INTO artist_exists
    FROM artist
    WHERE name = p_instructor_name AND is_active = TRUE;

    IF artist_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Artist does not exist or is not active.';
    END IF;

    START TRANSACTION;
        INSERT INTO workshop (title, date, duration_minutes, max_participants, price,
                              instructor_name, location, description, level)
        VALUES (p_title, p_date, p_duration_minutes, p_max_participants, p_price,
                p_instructor_name, p_location, p_description, p_level);
    COMMIT;

    SELECT CONCAT('Workshop "', p_title, '" created with instructor: ', p_instructor_name) AS result;
END$$

-- ─── Procedure 2 · Add artist with discipline ────────────────
CREATE PROCEDURE sp_add_artist_with_discipline(
    IN p_name       VARCHAR(255),
    IN p_birth_year INT,
    IN p_email      VARCHAR(255),
    IN p_phone      VARCHAR(50),
    IN p_city       VARCHAR(100),
    IN p_website    VARCHAR(255),
    IN p_discipline VARCHAR(100)
)
BEGIN
    INSERT INTO artist (name, birth_year, contact_email, phone, city, website, social_media, is_active)
    VALUES (p_name, p_birth_year, p_email, p_phone, p_city, p_website, '', TRUE);

    INSERT IGNORE INTO discipline (name) VALUES (p_discipline);

    INSERT INTO artist_discipline (artist_name, discipline_name)
    VALUES (p_name, p_discipline);

    SELECT CONCAT('Artist "', p_name, '" added with discipline "', p_discipline, '"') AS result;
END$$

-- ─── Procedure 3 · Full workshop report ──────────────────────
CREATE PROCEDURE sp_workshop_report(IN p_workshop_title VARCHAR(255))
BEGIN
    -- Workshop summary
    SELECT
        w.title,
        DATE(w.date)                                    AS workDate,
        w.level,
        w.price,
        w.max_participants                              AS maxParticipants,
        fn_get_participant_count(w.title)               AS paidParticipants,
        (w.max_participants - fn_get_participant_count(w.title)) AS spotsLeft
    FROM workshop w
    WHERE w.title = p_workshop_title;

    -- Instructor
    SELECT a.name AS instructorName, a.city
    FROM workshop  w
    JOIN artist    a ON w.instructor_name = a.name
    WHERE w.title = p_workshop_title;

    -- Registered members
    SELECT
        b.member_name       AS memberName,
        cm.membership_type,
        b.booking_date
    FROM booking        b
    JOIN community_member cm ON b.member_name = cm.name
    WHERE b.workshop_title = p_workshop_title
    ORDER BY b.booking_date;
END$$

DELIMITER ;

-- ============================================================
-- DEMO TRANSACTION (not auto-executed — see DatabaseCLI)
-- Register one member into two workshops atomically.
--
-- START TRANSACTION;
-- INSERT INTO booking (booking_date, payment_status, workshop_title, member_name)
--     VALUES (CURDATE(), 'Paid', '<workshop1>', '<member>');
-- INSERT INTO booking (booking_date, payment_status, workshop_title, member_name)
--     VALUES (CURDATE(), 'Paid', '<workshop2>', '<member>');
-- COMMIT;
-- ============================================================

SELECT 'db_objects.sql installed successfully.' AS status;