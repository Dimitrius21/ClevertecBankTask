INSERT INTO banks (bank_code, bank_name, address) VALUES
('CLBKBY22', 'Clever-Bank', 'Gomel'),
('MTBKBY22', 'JSC «MTBank»', 'Minsk'),
('TECNBY22', 'JSC «Tecnabank»', 'Minsk'),
('ALFABY2X', 'Alfa-Bank', 'Minsk'),
('UNBSBY2X', 'BSB Bank', 'Minsk'),
('PJCBBY2X', '"Priorbank" JSC', 'Minsk')
;

INSERT INTO currencies (currency_code, description) VALUES ('BYN', 'Belarusian ruble'),
('USD', 'US Dollar'), ('EUR', 'EURO');

INSERT INTO clients (first_name, second_name, surname, passport_number, create_date)
VALUES ('Сергей', 'Сергеевич', 'Иванов', 'AA11111111', CURRENT_TIMESTAMP),
('Андрей', 'Петрович', 'Ковалев', 'AB22222222', CURRENT_TIMESTAMP),
('Иван', 'Иванович', 'Сидоров', 'AA33333333', CURRENT_TIMESTAMP),
('Алиса', 'Игоревна', 'Селезнева', 'BB11111111', CURRENT_TIMESTAMP),
('Тимофей', 'Владимирович', 'Макаров', 'BA44444444', CURRENT_TIMESTAMP),
('Ростислав', 'Сергеевич', 'Плюшкин', 'BH55555555', CURRENT_TIMESTAMP),
('Игорь', 'Владимирович', 'Петров', 'BM12345678', CURRENT_TIMESTAMP),
('Семен', 'Семенович', 'Горбунков', 'CH66666666', CURRENT_TIMESTAMP),
('Семен', 'Семенович', 'Горбунков', 'CB77777777', CURRENT_TIMESTAMP),
('Семен', 'Семенович', 'Горбунков', 'CM88888888', CURRENT_TIMESTAMP),
('Иван', 'Васильевич', 'Бунша', 'CP99999999', CURRENT_TIMESTAMP),
('Антон', 'Семенович', 'Шпак', 'HA98765432', CURRENT_TIMESTAMP),
('Карп', 'Савельевич', 'Якин', 'HB87654321', CURRENT_TIMESTAMP),
('Жорж', '', 'Милославский', 'MA32165487', CURRENT_TIMESTAMP),
('Геннадий', 'Петрович', 'Козодоев', 'MC12365478', CURRENT_TIMESTAMP),
('Надежда', 'Ивановна', 'Горбункова', 'MB78945612', CURRENT_TIMESTAMP),
('Варвара', 'Сергеевна', 'Плющ', 'MN96385274', CURRENT_TIMESTAMP),
('Евгений', 'Николаевич', 'Ладыженский', 'MB74185296', CURRENT_TIMESTAMP),
('Анатолий', 'Ефремович', 'Новосельцев', 'HA25836914', CURRENT_TIMESTAMP),
('Людмила', 'Прокофьевна', 'Калугина', 'HM74185296', CURRENT_TIMESTAMP),
('Петр', 'Иванович', 'Бубликов', 'AE95175396', CURRENT_TIMESTAMP)
;

INSERT INTO accounts (account_number, value, currency_code, create_date, bank_id, client_id) VALUES
('BY11CLBK181901001', 0, 'BYN', CURRENT_TIMESTAMP + interval '1 second', 1, 1),
('BY11CLBK181901002', 0, 'BYN', CURRENT_TIMESTAMP + interval '2 second', 1, 2),
('BY11CLBK181901003', 0, 'BYN', CURRENT_TIMESTAMP + interval '3 second', 1, 3),
('BY11CLBK181901004', 0, 'BYN', CURRENT_TIMESTAMP + interval '4 second', 1, 4),
('BY11CLBK181901005', 0, 'BYN', CURRENT_TIMESTAMP + interval '5 second', 1, 5),
('BY11CLBK181901006', 0, 'BYN', CURRENT_TIMESTAMP + interval '6 second', 1, 6),
('BY12CLBK181901007', 0, 'USD', CURRENT_TIMESTAMP + interval '7 second', 1, 6),
('BY11CLBK181901008', 0, 'BYN', CURRENT_TIMESTAMP + interval '8 second', 1, 7),
('BY11CLBK181901009', 0, 'BYN', CURRENT_TIMESTAMP + interval '9 second', 1, 8),
('BY11CLBK181901010', 0, 'BYN', CURRENT_TIMESTAMP + interval '10 second', 1, 9),
('BY11CLBK181901011', 0, 'BYN', CURRENT_TIMESTAMP + interval '11 second', 1, 10),
('BY11CLBK181901012', 0, 'BYN', CURRENT_TIMESTAMP + interval '12 second', 1, 11),
('BY13CLBK181901013', 0, 'EUR', CURRENT_TIMESTAMP + interval '13 second', 1, 11),
('BY21MTBK381102001', 0, 'BYN', CURRENT_TIMESTAMP + interval '14 second', 2, 12),
('BY21MTBK381102002', 0, 'BYN', CURRENT_TIMESTAMP + interval '15 second', 2, 13),
('BY21MTBK381102003', 0, 'BYN', CURRENT_TIMESTAMP + interval '16 second', 2, 14),
('BY21MTBK381102004', 0, 'BYN', CURRENT_TIMESTAMP + interval '17 second', 2, 15),
('BY22MTBK381102005', 0, 'USD', CURRENT_TIMESTAMP + interval '18 second', 2, 15),
('BY21MTBK381102006', 0, 'BYN', CURRENT_TIMESTAMP + interval '19 second', 2, 16),
('BY21MTBK381102007', 0, 'BYN', CURRENT_TIMESTAMP + interval '20 second', 2, 17),
('BY21MTBK381102008', 0, 'BYN', CURRENT_TIMESTAMP + interval '21 second', 2, 18),
('BY21MTBK381102009', 0, 'BYN', CURRENT_TIMESTAMP + interval '22 second', 2, 19),
('BY31TECN380103001', 0, 'BYN', CURRENT_TIMESTAMP + interval '23 second', 3, 20),
('BY31TECN380103002', 0, 'BYN', CURRENT_TIMESTAMP + interval '24 second', 3, 5),
('BY31TECN380103003', 0, 'BYN', CURRENT_TIMESTAMP + interval '25 second', 3, 10),
('BY31TECN380103004', 0, 'BYN', CURRENT_TIMESTAMP + interval '26 second', 3, 15),
('BY32TECN380203005', 0, 'USD', CURRENT_TIMESTAMP + interval '27 second', 3, 15),
('BY31TECN380103006', 0, 'BYN', CURRENT_TIMESTAMP + interval '28 second', 3, 1),
('BY41ALFA382104001', 0, 'BYN', CURRENT_TIMESTAMP + interval '29 second', 4, 2),
('BY41ALFA382104002', 0, 'BYN', CURRENT_TIMESTAMP + interval '30 second', 4, 7),
('BY41ALFA382104003', 0, 'BYN', CURRENT_TIMESTAMP + interval '31 second', 4, 12),
('BY42ALFA382104004', 0, 'USD', CURRENT_TIMESTAMP + interval '32 second', 4, 12),
('BY43ALFA382104005', 0, 'EUR', CURRENT_TIMESTAMP + interval '33 second', 4, 12),
('BY41ALFA382104006', 0, 'BYN', CURRENT_TIMESTAMP + interval '34 second', 4, 17),
('BY51UNBS382105001', 0, 'BYN', CURRENT_TIMESTAMP + interval '35 second', 5, 3),
('BY52UNBS382105001', 0, 'USD', CURRENT_TIMESTAMP + interval '36 second', 5, 3),
('BY51UNBS382105002', 0, 'BYN', CURRENT_TIMESTAMP + interval '37 second', 5, 8),
('BY51UNBS382105003', 0, 'BYN', CURRENT_TIMESTAMP + interval '38 second', 5, 13),
('BY53UNBS382105004', 0, 'EUR', CURRENT_TIMESTAMP + interval '39 second', 5, 13),
('BY51UNBS382105005', 0, 'BYN', CURRENT_TIMESTAMP + interval '40 second', 5, 18),
('BY61PJCB382106001', 0, 'BYN', CURRENT_TIMESTAMP + interval '41 second', 6, 4),
('BY61PJCB382106002', 0, 'BYN', CURRENT_TIMESTAMP + interval '42 second', 6, 9),
('BY61PJCB382106003', 0, 'BYN', CURRENT_TIMESTAMP + interval '43 second', 6, 15)
;

-- https://stackoverflow.com/questions/20952893/postgresql-encoding-problems-on-windows-when-using-psql-command-line-utility



