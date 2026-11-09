-- 5 bản ghi mẫu cho bảng account (chạy sau postgres-schema.sql)
INSERT INTO account
    (acct_no, cif_no, id_no, acct_type, acct_title, curr_code, branch_code, ledger_balance, min_balance, acc_status)
VALUES
    ('100000000001', 'CIF001', '079123456789', 'CA01', 'NGUYEN VAN A', 'VND', 'HN0001', 15000000.50,  50000.00, '0'),  -- Active
    ('100000000002', 'CIF001', '079123456789', 'SA02', 'NGUYEN VAN A', 'USD', 'HN0001',     2500.00,     10.00, '0'),  -- Active
    ('100000000003', 'CIF002', '001987654321', 'CA01', 'TRAN THI B',   'VND', 'HCM001',        0.00,      0.00, '4'),  -- Closed
    ('100000000004', 'CIF003', '036555123456', 'SA01', 'LE VAN C',     'VND', 'DN0001', 82500000.00, 100000.00, '2'),  -- Dormant
    ('100000000005', 'CIF002', '001987654321', 'CA02', 'TRAN THI B',   'EUR', 'HCM001',     1200.75,      0.00, '1');  -- Inactive
