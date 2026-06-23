-- BIM モデルサムネイル画像

ALTER TABLE bim_models ADD COLUMN IF NOT EXISTS thumbnail_url TEXT NOT NULL DEFAULT '';

UPDATE bim_models
SET thumbnail_url = 'https://modelviewer.dev/shared-assets/models/Astronaut.webp'
WHERE thumbnail_url = '' AND title LIKE '%本館構造%';

UPDATE bim_models
SET thumbnail_url = 'https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=320&h=180&fit=crop'
WHERE thumbnail_url = '' AND title LIKE '%改修計画%';

UPDATE bim_models
SET thumbnail_url = 'https://modelviewer.dev/shared-assets/models/Astronaut.webp'
WHERE thumbnail_url = '';
