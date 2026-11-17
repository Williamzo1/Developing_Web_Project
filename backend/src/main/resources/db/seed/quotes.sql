-- Provisional Q-38 tags. No real contact information.
INSERT INTO tags(slug,name) VALUES ('gom-thu-cong','Gốm thủ công'),('men-lam','Men lam'),('dat-nung','Đất nung') ON CONFLICT(slug) DO NOTHING;
INSERT INTO product_tags(product_id,tag_id)
 SELECT p.id,t.id FROM products p JOIN workshops w ON w.id=p.workshop_id
 JOIN tags t ON t.slug='gom-thu-cong' OR (t.slug='men-lam' AND w.slug='xuong-men-lam') OR (t.slug='dat-nung' AND w.slug='xuong-dat-nung')
 WHERE w.slug IN ('xuong-men-lam','xuong-dat-nung') ON CONFLICT DO NOTHING;
UPDATE workshops SET contact_email=COALESCE(contact_email,slug||'@example.test'),phone=COALESCE(phone,'0900000000')
 WHERE slug IN ('xuong-men-lam','xuong-dat-nung');
