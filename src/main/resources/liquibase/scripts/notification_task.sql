-- liquibase formatted sql

-- changeset akuznetsov:1
CREATE TABLE notification_task (
    id SERIAL,
    chat_id INTEGER,
    task TEXT,
    send_Date_Time timestamptz
)