-- ============================================================
--  SAMPLE DATA FOR ARTCONNECT PRO
-- ============================================================

USE artconnect_db;

-- ===== 1. DISCIPLINES =====
INSERT IGNORE INTO discipline (name) VALUES
  ('Painting'),
  ('Sculpture'),
  ('Photography'),
  ('Digital Art'),
  ('Ceramics'),
  ('Printmaking'),
  ('Installation Art');

-- ===== 2. ARTWORK TAGS =====
INSERT IGNORE INTO artwork_tag (name) VALUES
  ('abstract'),
  ('portrait'),
  ('landscape'),
  ('political'),
  ('minimalist'),
  ('surreal'),
  ('figurative'),
  ('experimental'),
  ('urban'),
  ('nature');

-- ===== 3. COMMUNITY MEMBERS =====
INSERT IGNORE INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
  ('Alice Moreau',    'alice.moreau@email.com',     1985, '0612345678', 'Paris',      'Premium'),
  ('Benoît Lefèvre',  'benoit.lefevre@email.com',   1990, '0623456789', 'Lyon',       'Standard'),
  ('Camille Fontaine','camille.fontaine@email.com',  1978, '0634567890', 'Bordeaux',   'Premium'),
  ('David Nguyen',    'david.nguyen@email.com',      2000, '0645678901', 'Paris',      'Student'),
  ('Émilie Rousseau', 'emilie.rousseau@email.com',   1995, '0656789012', 'Marseille',  'Standard'),
  ('François Bernard','francois.bernard@email.com',  1968, '0667890123', 'Toulouse',   'Premium'),
  ('Gina Kowalski',   'gina.kowalski@email.com',     2001, '0678901234', 'Paris',      'Student'),
  ('Henri Dubois',    'henri.dubois@email.com',      1975, '0689012345', 'Nantes',     'Standard');

-- ===== 4. FAVORITE DISCIPLINES (members with multiple interests) =====
INSERT IGNORE INTO member_discipline (member_name, discipline_name) VALUES
  ('Alice Moreau', 'Painting'),
  ('Alice Moreau', 'Photography'),
  ('Benoît Lefèvre', 'Sculpture'),
  ('Benoît Lefèvre', 'Installation Art'),
  ('Camille Fontaine', 'Photography'),
  ('Camille Fontaine', 'Digital Art'),
  ('David Nguyen', 'Digital Art'),
  ('David Nguyen', 'Printmaking'),
  ('Émilie Rousseau', 'Ceramics'),
  ('Émilie Rousseau', 'Painting'),
  ('François Bernard', 'Sculpture'),
  ('François Bernard', 'Painting'),
  ('Gina Kowalski', 'Photography'),
  ('Henri Dubois', 'Installation Art');

-- ===== 5. GALLERIES =====
INSERT IGNORE INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
  ('Galerie Lumière',     '12 Rue du Faubourg, Paris',     'Marie Clair',     'Tue-Sat 10:00-19:00', '0142345678', 4.7, 'www.galerie-lumiere.fr'),
  ('Espace Contemporain', '45 Quai des Arts, Lyon',        'Pierre Aubert',   'Wed-Sun 11:00-18:00', '0478901234', 4.2, 'www.espace-contemporain.fr'),
  ('Atelier du Sud',      '8 Boulevard du Mistral, Marseille', 'Sophie Blanc', 'Mon-Sat 09:00-17:00', '0491234567', 4.5, 'www.atelier-du-sud.fr');

-- ===== 6. ARTISTS =====
INSERT IGNORE INTO artist (name, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES
  ('Léa Tremblay',    1980, 'lea.tremblay@art.com',    '0611111111', 'Paris',      'www.leatremblay.com',   '@leatremblay',   TRUE),
  ('Marco Ferretti',  1975, 'marco.ferretti@art.com',  '0622222222', 'Marseille',  'www.marcoferretti.it',  '@marcoferretti', TRUE),
  ('Naomi Okoro',     1990, 'naomi.okoro@art.com',     '0633333333', 'Lyon',       'www.naomiokoro.com',    '@naomiokoro',    TRUE),
  ('Samuel Vidal',    1968, 'samuel.vidal@art.com',    '0644444444', 'Bordeaux',   'www.samuelvidal.fr',    '@samuelvidal',   FALSE),
  ('Yuki Hayashi',    1995, 'yuki.hayashi@art.com',    '0655555555', 'Paris',      'www.yukihayashi.jp',    '@yukihayashi',   TRUE),
  ('Clara Martínez',  1985, 'clara.martinez@art.com',  '0666666666', 'Toulouse',   'www.claramartinez.es',  '@claramartinez', TRUE);

-- ===== 7. ARTIST DISCIPLINES =====
INSERT IGNORE INTO artist_discipline (artist_name, discipline_name) VALUES
  ('Léa Tremblay', 'Painting'),
  ('Léa Tremblay', 'Photography'),
  ('Marco Ferretti', 'Sculpture'),
  ('Marco Ferretti', 'Installation Art'),
  ('Naomi Okoro', 'Photography'),
  ('Naomi Okoro', 'Digital Art'),
  ('Samuel Vidal', 'Painting'),
  ('Samuel Vidal', 'Printmaking'),
  ('Yuki Hayashi', 'Digital Art'),
  ('Yuki Hayashi', 'Installation Art'),
  ('Clara Martínez', 'Ceramics'),
  ('Clara Martínez', 'Sculpture');

-- ===== 8. ARTWORKS =====
INSERT IGNORE INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) VALUES
  ('Reflets Urbains',     2021, 'Painting',     'Oil on canvas',         '120x90cm',  3500.00, 'City reflections in rain puddles', 'FOR_SALE', 'Léa Tremblay'),
  ('Mémoire Floue',       2022, 'Photography',  'Fine art print',         '80x60cm',   1200.00, 'Blurred long-exposure urban portrait', 'FOR_SALE', 'Léa Tremblay'),
  ('Lumière d''Automne',  2023, 'Painting',     'Acrylic on canvas',     '100x80cm',  4200.00, 'Abstract autumn light study', 'FOR_SALE', 'Léa Tremblay'),
  ('Équilibre Précaire',  2019, 'Sculpture',    'Welded steel',          '45x30x30cm',8000.00, 'Balanced steel forms exploring tension', 'SOLD', 'Marco Ferretti'),
  ('Void IV',             2022, 'Installation', 'Mixed media, mirrors',  '300x200cm', 15000.00, 'Room-scale mirror installation', 'EXHIBITED', 'Marco Ferretti'),
  ('Data Drift',         2022, 'Digital Art',   'Generative, 3-screen',  '200x80cm', 9500.00, 'Generative data-driven landscape', 'EXHIBITED', 'Naomi Okoro'),
  ('Pixel Garden',       2022, 'Digital Art',   'Interactive screen',    '120x80cm',  5500.00, 'Interactive generative garden', 'EXHIBITED', 'Yuki Hayashi'),
  ('Floating Grid',      2023, 'Installation',  'Neon tubes, sensors',   '400x300cm', 22000.00, 'Sensor-reactive neon installation', 'EXHIBITED', 'Yuki Hayashi'),
  ('Terre et Feu I',     2021, 'Ceramics',      'Stoneware, raku-fired', '30x20x20cm',650.00,  'Raku-fired vessel exploring fire marks', 'FOR_SALE', 'Clara Martínez'),
  ('Corps Fragmentés',   2023, 'Sculpture',     'Terracotta',            '60x40x40cm',3200.00, 'Fragmented human form in terracotta', 'FOR_SALE', 'Clara Martínez'),
  ('Argile Vivante',     2023, 'Ceramics',      'Porcelain, hand-built', '25x15x15cm',480.00,  'Organic hand-built porcelain forms', 'FOR_SALE', 'Clara Martínez');

-- ===== 9. ARTWORK TAGS =====
INSERT IGNORE INTO artwork_artwork_tag (artwork_title, tag_name) VALUES
  ('Reflets Urbains', 'urban'),
  ('Reflets Urbains', 'abstract'),
  ('Mémoire Floue', 'portrait'),
  ('Mémoire Floue', 'urban'),
  ('Lumière d''Automne', 'abstract'),
  ('Lumière d''Automne', 'landscape'),
  ('Équilibre Précaire', 'minimalist'),
  ('Void IV', 'experimental'),
  ('Void IV', 'abstract'),
  ('Identités', 'portrait'),
  ('Identités', 'political'),
  ('Glitch Portraits', 'portrait'),
  ('Glitch Portraits', 'experimental'),
  ('Data Drift', 'abstract'),
  ('Data Drift', 'landscape'),
  ('Data Drift', 'experimental'),
  ('Géographies I', 'landscape'),
  ('Géographies I', 'abstract'),
  ('Empreintes', 'figurative'),
  ('Pixel Garden', 'abstract'),
  ('Pixel Garden', 'nature'),
  ('Pixel Garden', 'experimental'),
  ('Floating Grid', 'minimalist'),
  ('Floating Grid', 'experimental'),
  ('Terre et Feu I', 'abstract'),
  ('Terre et Feu I', 'nature'),
  ('Corps Fragmentés', 'figurative'),
  ('Argile Vivante', 'nature'),
  ('Argile Vivante', 'abstract');

-- ===== 10. EXHIBITIONS =====
INSERT IGNORE INTO exhibition (name, start_date, end_date, description, gallery_id) VALUES
  ('Fragments du Réel',      '2023-03-01', '2023-04-30', 'Exploring fragmented urban realities', 1),
  ('Matière et Mémoire',     '2023-06-15', '2023-08-31', 'Material memory across generations', 2),
  ('Nouvelles Géographies',  '2023-09-01', '2023-11-30', 'Digital and physical landscape exploration', 1),
  ('Corps & Territoire',     '2024-01-20', '2024-03-31', 'Body as landscape and political territory', 3),
  ('Biennale Jeune Création','2024-05-01', '2024-07-31', 'Emerging artists under 35', 2);

-- ===== 11. EXHIBITION ARTWORKS =====
INSERT IGNORE INTO exhibition_artwork (exhibition_name, artwork_title) VALUES
  ('Fragments du Réel', 'Reflets Urbains'),
  ('Fragments du Réel', 'Mémoire Floue'),
  ('Fragments du Réel', 'Identités'),
  ('Fragments du Réel', 'Équilibre Précaire'),
  ('Fragments du Réel', 'Corps Fragmentés'),
  ('Matière et Mémoire', 'Géographies I'),
  ('Matière et Mémoire', 'Empreintes'),
  ('Matière et Mémoire', 'Terre et Feu I'),
  ('Matière et Mémoire', 'Équilibre Précaire'),
  ('Matière et Mémoire', 'Reflets Urbains'),
  ('Nouvelles Géographies', 'Lumière d''Automne'),
  ('Nouvelles Géographies', 'Data Drift'),
  ('Nouvelles Géographies', 'Pixel Garden'),
  ('Nouvelles Géographies', 'Floating Grid'),
  ('Nouvelles Géographies', 'Géographies I'),
  ('Corps & Territoire', 'Identités'),
  ('Corps & Territoire', 'Glitch Portraits'),
  ('Corps & Territoire', 'Corps Fragmentés'),
  ('Corps & Territoire', 'Void IV'),
  ('Corps & Territoire', 'Mémoire Floue'),
  ('Biennale Jeune Création', 'Glitch Portraits'),
  ('Biennale Jeune Création', 'Pixel Garden'),
  ('Biennale Jeune Création', 'Floating Grid'),
  ('Biennale Jeune Création', 'Argile Vivante');

-- ===== 12. WORKSHOPS =====
INSERT IGNORE INTO workshop (title, date, duration_minutes, max_participants, price, location, description, level, instructor_name) VALUES
  ('Introduction à l''aquarelle',  '2023-10-07 10:00:00', 180, 12, 45.00,  'Galerie Lumière, Paris',      'Watercolour basics for beginners', 'Beginner', 'Léa Tremblay'),
  ('Photographie de rue',          '2023-10-14 10:00:00', 240, 8,  80.00,  'Paris — Marais district',     'Street photography guided walk', 'Intermediate', 'Naomi Okoro'),
  ('Initiation à la céramique',    '2023-11-04 10:00:00', 300, 10, 65.00,  'Atelier du Sud, Marseille',   'Hand-building and basic glazing', 'Beginner', 'Clara Martínez'),
  ('Art numérique et IA',          '2023-11-18 10:00:00', 240, 15, 90.00,  'Espace Contemporain, Lyon',   'Generative art with AI tools', 'Intermediate', 'Yuki Hayashi'),
  ('Sculpture sur métal',          '2024-01-13 10:00:00', 360, 6,  120.00, 'Atelier du Sud, Marseille',   'Steel welding and form exploration', 'Advanced', 'Marco Ferretti'),
  ('Installation in situ',         '2024-02-24 10:00:00', 480, 8,  150.00, 'Espace Contemporain, Lyon',   'Creating site-specific installation works', 'Advanced', 'Marco Ferretti'),
  ('Portrait photographique',      '2024-03-09 10:00:00', 180, 10, 70.00,  'Galerie Lumière, Paris',      'Studio portrait lighting and posing', 'Beginner', 'Léa Tremblay');

-- ===== 13. BOOKINGS =====
INSERT IGNORE INTO booking (workshop_title, member_name, booking_date) VALUES
  ('Introduction à l''aquarelle', 'Alice Moreau',    '2023-09-20'),
  ('Introduction à l''aquarelle', 'Émilie Rousseau', '2023-09-22'),
  ('Introduction à l''aquarelle', 'Gina Kowalski',   '2023-09-25'),
  ('Introduction à l''aquarelle', 'Henri Dubois',    '2023-10-01'),
  ('Introduction à l''aquarelle', 'Benoît Lefèvre',  '2023-10-03'),
  ('Photographie de rue', 'Alice Moreau',      '2023-09-28'),
  ('Photographie de rue', 'David Nguyen',      '2023-09-30'),
  ('Photographie de rue', 'Camille Fontaine',  '2023-10-02'),
  ('Initiation à la céramique', 'Émilie Rousseau', '2023-10-15'),
  ('Initiation à la céramique', 'François Bernard','2023-10-18'),
  ('Initiation à la céramique', 'Henri Dubois',    '2023-10-20'),
  ('Art numérique et IA', 'Camille Fontaine',  '2023-11-01'),
  ('Art numérique et IA', 'David Nguyen',      '2023-11-02'),
  ('Art numérique et IA', 'Gina Kowalski',     '2023-11-05'),
  ('Sculpture sur métal', 'Benoît Lefèvre',    '2024-01-03'),
  ('Sculpture sur métal', 'François Bernard',  '2024-01-05'),
  ('Installation in situ', 'Benoît Lefèvre',   '2024-02-01'),
  ('Installation in situ', 'Camille Fontaine', '2024-02-03'),
  ('Portrait photographique', 'Alice Moreau',  '2024-02-20'),
  ('Portrait photographique', 'Gina Kowalski', '2024-02-21'),
  ('Portrait photographique', 'David Nguyen',  '2024-02-22');

-- ===== 14. REVIEWS =====
INSERT IGNORE INTO review (member_name, artwork_title, rating, comment, review_date) VALUES
  ('Alice Moreau', 'Reflets Urbains', 5, 'Stunning play of light — the rain reflections feel alive', '2023-04-10'),
  ('Camille Fontaine', 'Mémoire Floue', 4, 'Emotionally resonant long exposure, beautifully printed', '2023-04-25'),
  ('Naomi Okoro', 'Lumière d''Automne', 5, 'Best piece in the show — colour palette is extraordinary', '2023-10-15'),
  ('Benoît Lefèvre', 'Équilibre Précaire', 4, 'Loved the tension in the sculpture, impressive balance', '2023-04-12'),
  ('Alice Moreau', 'Void IV', 5, 'Void IV is overwhelming in the best way — disorienting and beautiful', '2023-11-10'),
  ('Henri Dubois', 'Void IV', 3, 'Interesting concept but the mirrors need better maintenance', '2023-11-12'),
  ('Alice Moreau', 'Identités', 5, 'Identités is the most politically powerful series I have seen this year', '2023-04-20'),
  ('David Nguyen', 'Glitch Portraits', 4, 'Glitch Portraits: digital fragmentation mirrors real identity questions', '2024-04-15'),
  ('Émilie Rousseau', 'Data Drift', 5, 'Data Drift is hypnotic — I stood in front of it for 20 minutes', '2023-10-18'),
  ('François Bernard', 'Géographies I', 5, 'Géographies I remains a masterpiece — timeless geometric precision', '2023-08-10'),
  ('Camille Fontaine', 'Empreintes', 4, 'Empreintes: the etchings have a beautiful tactile quality even as prints', '2023-08-12'),
  ('Gina Kowalski', 'Pixel Garden', 5, 'Pixel Garden is addictive — every interaction changes the whole composition', '2023-10-20'),
  ('Benoît Lefèvre', 'Floating Grid', 4, 'Floating Grid: the sensors are a bit slow but the neon effect is superb', '2023-10-25'),
  ('David Nguyen', 'Floating Grid', 5, 'Completely immersive, the best installation of the biennale', '2024-06-10'),
  ('Émilie Rousseau', 'Terre et Feu I', 5, 'Terre et Feu I — the raku marks are unpredictable and gorgeous', '2023-08-15'),
  ('Alice Moreau', 'Corps Fragmentés', 4, 'Corps Fragmentés speaks volumes about body politics, powerful work', '2024-03-10'),
  ('Gina Kowalski', 'Argile Vivante', 5, 'Argile Vivante: organic forms that look grown, not made', '2024-07-05');

