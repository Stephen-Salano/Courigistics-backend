-- Seed Default Depot (NBO-MAIN)
INSERT INTO depot (id, name, code, depot_type, address, city, country, latitude, longitude, coverage_radius_km, status, created_at)
SELECT 'd104b2bd-0fb3-4885-afc9-e069dc3abeb4', 'Nairobi Main Depot', 'NBO-MAIN', 'MAIN', 'Mombasa Road', 'Nairobi', 'Kenya', -1.2921, 36.8219, 50.0, 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM depot WHERE code = 'NBO-MAIN');