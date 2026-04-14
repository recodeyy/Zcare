-- Step 1.1 schema expansion for medicines
-- Note: kept existing price column for backward compatibility.

ALTER TABLE medicines ADD COLUMN IF NOT EXISTS barcode VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS sku VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS batch_number VARCHAR(100);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS manufacture_date DATE;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS dosage_form VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS strength VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS generic_name VARCHAR(200);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS brand_name VARCHAR(200);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS prescription_required BOOLEAN DEFAULT FALSE;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS min_stock_level INTEGER DEFAULT 10;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS max_stock_level INTEGER DEFAULT 1000;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS unit_of_measure VARCHAR(50);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS rack_location VARCHAR(200);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS cost_price DOUBLE PRECISION;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS selling_price DOUBLE PRECISION;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS tax_rate DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS active_ingredient VARCHAR(500);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS storage_conditions VARCHAR(500);
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_medicine_barcode ON medicines(barcode);
CREATE INDEX IF NOT EXISTS idx_medicine_name ON medicines(name);
CREATE INDEX IF NOT EXISTS idx_medicine_category ON medicines(category);
CREATE INDEX IF NOT EXISTS idx_medicine_expiry_date ON medicines(expiry_date);
CREATE INDEX IF NOT EXISTS idx_medicine_batch_number ON medicines(batch_number);
