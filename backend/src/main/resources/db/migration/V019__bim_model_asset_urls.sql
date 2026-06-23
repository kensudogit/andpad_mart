-- 壊れた外部 BIM アセット URL を自前 SVG / 安定 CDN に差し替え

UPDATE bim_models
SET viewer_url = 'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/DamagedHelmet/glTF-Binary/DamagedHelmet.glb',
    thumbnail_url = '/bim/thumbs/structure.svg'
WHERE title LIKE '%本館構造%'
   OR viewer_url LIKE '%modelviewer.dev%'
   OR thumbnail_url LIKE '%modelviewer.dev%';

UPDATE bim_models
SET viewer_url = 'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/Astronaut/glTF-Binary/Astronaut.glb',
    thumbnail_url = '/bim/thumbs/renovation.svg'
WHERE title LIKE '%改修計画%';

UPDATE bim_models
SET thumbnail_url = '/bim/thumbs/equipment.svg'
WHERE title LIKE '%設備%';

UPDATE bim_models
SET thumbnail_url = '/bim/thumbs/default.svg'
WHERE thumbnail_url = ''
   OR thumbnail_url LIKE '%unsplash.com%'
   OR thumbnail_url LIKE '%modelviewer.dev%';

UPDATE bim_models
SET viewer_url = 'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/DamagedHelmet/glTF-Binary/DamagedHelmet.glb'
WHERE viewer_url LIKE '%modelviewer.dev%';
