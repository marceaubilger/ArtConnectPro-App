-- Create the database
CREATE DATABASE IF NOT EXISTS artconnect_db;
USE artconnect_db;

-- Table: discipline
CREATE TABLE IF NOT EXISTS discipline (
    name VARCHAR(100) PRIMARY KEY
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: artist
CREATE TABLE IF NOT EXISTS artist (
    name VARCHAR(100) PRIMARY KEY,
    bio LONGTEXT,
    birth_year INT NULL,
    contact_email VARCHAR(150),
    phone VARCHAR(20),
    city VARCHAR(100),
    website VARCHAR(200),
    social_media VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: artist_discipline (Many-to-Many)
CREATE TABLE IF NOT EXISTS artist_discipline (
    artist_name VARCHAR(100) NOT NULL,
    discipline_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (artist_name, discipline_name),
    FOREIGN KEY (artist_name) REFERENCES artist(name) ON DELETE CASCADE,
    FOREIGN KEY (discipline_name) REFERENCES discipline(name) ON DELETE CASCADE
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: artwork_tag
CREATE TABLE IF NOT EXISTS artwork_tag (
    name VARCHAR(100) PRIMARY KEY
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: artwork
CREATE TABLE IF NOT EXISTS artwork (
    title VARCHAR(255) PRIMARY KEY,
    creation_year INT NULL,
    type VARCHAR(100),
    medium VARCHAR(100),
    dimensions VARCHAR(100),
    description LONGTEXT,
    price DOUBLE,
    status VARCHAR(50),
    artist_name VARCHAR(100),
    FOREIGN KEY (artist_name) REFERENCES artist(name) ON DELETE SET NULL
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: artwork_artwork_tag (Many-to-Many)
CREATE TABLE IF NOT EXISTS artwork_artwork_tag (
    artwork_title VARCHAR(255) NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (artwork_title, tag_name),
    FOREIGN KEY (artwork_title) REFERENCES artwork(title) ON DELETE CASCADE,
    FOREIGN KEY (tag_name) REFERENCES artwork_tag(name) ON DELETE CASCADE
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: gallery
CREATE TABLE IF NOT EXISTS gallery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255),
    owner_name VARCHAR(100),
    opening_hours VARCHAR(200),
    contact_phone VARCHAR(20),
    rating DOUBLE DEFAULT 0.0,
    website VARCHAR(200)
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: exhibition
CREATE TABLE IF NOT EXISTS exhibition (
    name VARCHAR(255) PRIMARY KEY,
    start_date DATETIME,
    end_date DATETIME,
    gallery_id BIGINT,
    description LONGTEXT,
    FOREIGN KEY (gallery_id) REFERENCES gallery(id) ON DELETE SET NULL
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: exhibition_artwork (Many-to-Many)
CREATE TABLE IF NOT EXISTS exhibition_artwork (
    exhibition_name VARCHAR(255) NOT NULL,
    artwork_title VARCHAR(255) NOT NULL,
    PRIMARY KEY (exhibition_name, artwork_title),
    FOREIGN KEY (exhibition_name) REFERENCES exhibition(name) ON DELETE CASCADE,
    FOREIGN KEY (artwork_title) REFERENCES artwork(title) ON DELETE CASCADE
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: workshop
CREATE TABLE IF NOT EXISTS workshop (
    title VARCHAR(255) PRIMARY KEY,
    date DATETIME,
    duration_minutes INT,
    max_participants INT,
    price DOUBLE,
    instructor_name VARCHAR(100),
    location VARCHAR(255),
    description LONGTEXT,
    level VARCHAR(50),
    FOREIGN KEY (instructor_name) REFERENCES artist(name) ON DELETE SET NULL
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: community_member
CREATE TABLE IF NOT EXISTS community_member (
    name VARCHAR(100) PRIMARY KEY,
    email VARCHAR(150),
    birth_year INT NULL,
    phone VARCHAR(20),
    city VARCHAR(100),
    membership_type VARCHAR(50)
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: member_discipline (Many-to-Many)
CREATE TABLE IF NOT EXISTS member_discipline (
    member_name VARCHAR(100) NOT NULL,
    discipline_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (member_name, discipline_name),
    FOREIGN KEY (member_name) REFERENCES community_member(name) ON DELETE CASCADE,
    FOREIGN KEY (discipline_name) REFERENCES discipline(name) ON DELETE CASCADE
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: booking
CREATE TABLE IF NOT EXISTS booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workshop_title VARCHAR(255),
    member_name VARCHAR(100),
    booking_date DATETIME,
    FOREIGN KEY (workshop_title) REFERENCES workshop(title) ON DELETE SET NULL,
    FOREIGN KEY (member_name) REFERENCES community_member(name) ON DELETE SET NULL
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: review
CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_name VARCHAR(100),
    artwork_title VARCHAR(255),
    rating INT,
    comment LONGTEXT,
    review_date DATETIME,
    FOREIGN KEY (member_name) REFERENCES community_member(name) ON DELETE SET NULL,
    FOREIGN KEY (artwork_title) REFERENCES artwork(title) ON DELETE SET NULL
) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Indices for performance
CREATE INDEX idx_artist_city ON artist(city);
CREATE INDEX idx_artwork_artist ON artwork(artist_name);
CREATE INDEX idx_workshop_instructor ON workshop(instructor_name);
CREATE INDEX idx_community_city ON community_member(city);
CREATE INDEX idx_exhibition_gallery ON exhibition(gallery_id);
CREATE INDEX idx_booking_member ON booking(member_name);
CREATE INDEX idx_booking_workshop ON booking(workshop_title);


