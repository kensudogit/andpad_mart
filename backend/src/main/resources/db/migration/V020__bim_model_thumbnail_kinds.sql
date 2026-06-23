-- タイトルベースでサムネイル種別を補正（空・壊れた URL の修復）

UPDATE bim_models
SET thumbnail_url = '/bim/thumbs/equipment.svg'
WHERE title LIKE '%設備%';

UPDATE bim_models
SET thumbnail_url = '/bim/thumbs/structure.svg'
WHERE title LIKE '%本館%' OR title LIKE '%構造%';

UPDATE bim_models
SET thumbnail_url = '/bim/thumbs/renovation.svg'
WHERE title LIKE '%改修%';

UPDATE bim_models
SET viewer_url = 'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/DamagedHelmet/glTF-Binary/DamagedHelmet.glb'
WHERE (title LIKE '%本館%' OR title LIKE '%構造%')
  AND (viewer_url = '' OR viewer_url LIKE '%modelviewer.dev%');

UPDATE bim_models
SET viewer_url = 'https://cdn.jsdelivr.net/gh/KhronosGroup/glTF-Sample-Assets@main/Models/Astronaut/glTF-Binary/Astronaut.glb'
WHERE title LIKE '%改修%'
  AND (viewer_url = '' OR viewer_url LIKE '%modelviewer.dev%');
