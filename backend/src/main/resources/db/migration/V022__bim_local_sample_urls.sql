-- 外部 CDN (jsDelivr 403 等) を自前サンプル GLB に差し替え

UPDATE bim_models
SET viewer_url = '/bim/samples/andpad-sample.glb'
WHERE viewer_url LIKE '%cdn.jsdelivr.net%'
   OR viewer_url LIKE '%modelviewer.dev%'
   OR viewer_url LIKE '%KhronosGroup%';

UPDATE bim_models
SET viewer_url = '/bim/samples/andpad-sample.glb'
WHERE viewer_url = ''
  AND format ILIKE '%gltf%';
