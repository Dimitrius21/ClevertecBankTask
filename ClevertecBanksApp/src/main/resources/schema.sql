CREATE TABLE IF NOT EXISTS banks(
    id bigserial PRIMARY KEY,
    bank_code VARCHAR(20) UNIQUE NOT NULL,
    bank_name VARCHAR(200) NOT NULL,
    address VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS clients (
        id bigserial PRIMARY KEY,
        first_name VARCHAR(200) NOT NULL,
        second_name VARCHAR(200),
        surname VARCHAR(200) NOT NULL,
        passport_number VARCHAR(20) NOT NULL,
        create_date timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS currencies (
        currency_code VARCHAR(5) PRIMARY KEY,
        description VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
        id bigserial PRIMARY KEY,
        account_number VARCHAR(100) UNIQUE NOT NULL,
        value bigint CHECK( value >= 0),
        currency_code VARCHAR(5),
        create_date timestamp NOT NULL,
        bank_id bigint,
        client_id bigint,
		FOREIGN KEY (currency_code) REFERENCES currencies(currency_code),
	    FOREIGN KEY (bank_id) REFERENCES banks(id),
	 	FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE IF NOT EXISTS transactions (
        id bigserial PRIMARY KEY,
        sum bigint CHECK (sum > 0),
        currency_code VARCHAR(5) NOT NULL,
        account_id_from bigint NOT NULL,
        balance_from bigint CHECK (balance_from >= 0),
        account_id_to bigint NOT NULL,
        balance_to bigint CHECK (balance_to >= 0),
        carry_out_at timestamp NOT NULL,
        transaction_type VARCHAR(20) NOT NULL
);