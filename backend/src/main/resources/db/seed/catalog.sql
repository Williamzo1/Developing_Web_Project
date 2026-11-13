INSERT INTO workshops (slug, name)
VALUES ('xuong-men-lam', 'Xưởng Men Lam'), ('xuong-dat-nung', 'Xưởng Đất Nung')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (slug, name)
VALUES ('coc-gom', 'Cốc gốm'), ('binh-gom', 'Bình gốm')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (workshop_id, category_id, slug, name, description, image_path)
SELECT w.id, c.id, s.slug, s.name, s.description, s.image_path
FROM (VALUES
    ('xuong-men-lam', 'coc-gom', 'coc-men-lam', 'Cốc men lam',
     'Cốc gốm dáng tròn với những nét men lam lấy cảm hứng từ sóng nước.', '/images/coc-men-lam.svg'),
    ('xuong-men-lam', 'binh-gom', 'binh-men-lam', 'Bình men lam',
     'Bình gốm cổ hẹp, thân tròn và họa tiết vẽ tay màu lam.', '/images/binh-men-lam.svg'),
    ('xuong-dat-nung', 'coc-gom', 'coc-dat-nung', 'Cốc đất nung',
     'Dáng cốc mộc với sắc đất nung ấm và bề mặt nhám nhẹ.', '/images/coc-dat-nung.svg')
) AS s(workshop_slug, category_slug, slug, name, description, image_path)
JOIN workshops w ON w.slug = s.workshop_slug
JOIN categories c ON c.slug = s.category_slug
ON CONFLICT (slug) DO NOTHING;
