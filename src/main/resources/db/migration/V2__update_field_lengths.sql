ALTER TABLE users
ALTER
COLUMN username TYPE VARCHAR(50);

ALTER TABLE tasks
ALTER
COLUMN description TYPE VARCHAR(1000);

ALTER TABLE users
    RENAME CONSTRAINT users_username_key TO uk_users_username;

