-- logs
CREATE SCHEMA logs;

create table if not exists logs.authorise_history
(
    id             serial
        constraint authorise_history_pk
            primary key,
    user_id        integer,
    is_success     integer,
    remote_address text,
    column_5       integer,
    column_6       integer,
    column_7       integer,
    column_8       integer,
    column_9       integer,
    add_data       timestamp with time zone default now()
);

comment on column logs.authorise_history.is_success is '0 - success
1 - not nuccess';

-- orders
CREATE SCHEMA orders;

create table if not exists orders.order_type
(
    service_type_id        serial
        constraint order_type_pk
            primary key,
    service_price_or_ratio text,
    description            text
);

create table if not exists orders.orders
(
    order_id                                 serial
        constraint orders_pk
            primary key,
    express                                  boolean,
    parcel_pickup_address_latitude           text,
    parcel_pickup_address_longitude          text,
    parcel_address_to_be_delivered_latitude  text,
    parcel_address_to_be_delivered_longitude text,
    service_price                            double precision,
    service_payment_type                     integer,
    client_name                              text,
    service_date                             timestamp,
    service_date_from                        timestamp,
    service_date_to                          timestamp,
    service_parcel_price                     double precision,
    service_parcel_identifiable              text,
    order_comment                            text,
    parcel_delivery_type                     integer,
    order_status                             integer,
    sender_user_id                           integer,
    job_id                                   integer,
    pickup_admin_area                        text,
    pickup_country_code                      text,
    to_be_delivered_admin_area               text,
    to_be_delivered_country_code             text,
    currency                                 text,
    total_distance                           text,
    viewer_phone                             text,
    handed_over_courier_user_id              integer,
    arrival_in_progress                      boolean   default false,
    parcel_type                              integer,
    courier_has_parcel_money                 boolean   default false,
    column_31                                integer,
    column_32                                integer,
    column_33                                integer,
    column_34                                integer,
    column_35                                integer,
    column_36                                integer,
    column_37                                integer,
    column_38                                integer,
    column_39                                integer,
    column_40                                integer,
    add_date                                 timestamp default now(),
    change_date                              timestamp default now()
);

comment on column orders.orders.service_payment_type is '0-card; 1-delivery; 2-taking';

comment on column orders.orders.parcel_delivery_type is '0-IN_REGION;
1-OUT_OF_REGION;
2-OUT_OF_COUNTRY';

comment on column orders.orders.order_status is '0-ACTIVE; 1-DONE';

comment on column orders.orders.parcel_type is '0 - SMALL, 1 - BIG';

create index sender_user_id_index
    on orders.orders (sender_user_id);

create index inactive_parcels_index
    on orders.orders (sender_user_id)
    where ((arrival_in_progress = false) AND (order_status = 0) AND (job_id IS NOT NULL));

create index made_parcels
    on orders.orders (sender_user_id)
    where ((arrival_in_progress = true) AND (order_status = 1) AND (job_id IS NOT NULL));

create index active_parcels
    on orders.orders (sender_user_id)
    where ((arrival_in_progress = true) AND (order_status = 0) AND (job_id IS NOT NULL));

create index parcels_with_out_job
    on orders.orders (sender_user_id)
    where ((order_status = 0) AND (job_id IS NULL));

create index today_parcels_index
    on orders.orders (sender_user_id, add_date)
    where ((order_status = 0) AND (job_id IS NOT NULL));

create index parcels_as_courier_index
    on orders.orders (handed_over_courier_user_id, change_date, order_status)
    where (order_status = 1);

create index parcels_as_sender_index
    on orders.orders (sender_user_id, change_date, order_status)
    where (order_status = 1);

create table if not exists orders.order_jobs
(
    job_id                                serial
        constraint order_jobs_pk
            primary key,
    order_job_status                      integer,
    sender_user_id                        integer,
    sender_phone                          text,
    courier_user_id                       integer,
    courier_phone                         text,
    order_count                           integer,
    contains_delivery_type_out_of_country boolean,
    contains_delivery_type_out_of_region  boolean,
    contains_express_order                boolean,
    job_name                              text,
    rewriter_user_id                      integer,
    column_16                             integer,
    column_17                             integer,
    column_18                             integer,
    column_19                             integer,
    column_20                             integer,
    column_21                             integer,
    column_22                             integer,
    column_23                             integer,
    column_24                             integer,
    column_25                             integer,
    add_date                              timestamp default now(),
    change_date                           timestamp default now()
);

comment on column orders.order_jobs.order_job_status is '0-ACTIVE; 1-DONE';

create index today_jobs
    on orders.order_jobs (sender_user_id, add_date)
    where (order_job_status = 0);

create index active_jobs
    on orders.order_jobs (sender_user_id)
    where (order_job_status = 0);

create index on_hold_jobs
    on orders.order_jobs (sender_user_id)
    where (order_job_status = 2);

create index done_jobs
    on orders.order_jobs (sender_user_id)
    where (order_job_status = 1);

create index all_jobs
    on orders.order_jobs (sender_user_id);

insert into orders.order_type (service_type_id, service_price_or_ratio, description)
values  (6, '5', 'standard'),
        (5, '8', 'express'),
        (4, '0.05', 'another_region'),
        (3, '0.1', 'express_another_region'),
        (2, '1.0', 'another_country'),
        (1, '2.0', 'express_another_country');


-- users
CREATE SCHEMA users;

create table if not exists users.users
(
    user_id                      serial
        constraint users_pk
            primary key,
    phone_number                 text,
    first_name                   text,
    last_name                    text,
    status_id                    integer   default 0,
    email                        text,
    rating                       double precision,
    nickname                     text,
    favourite_courier_company_id integer,
    deposit                      double precision,
    payment_type                 integer,
    is_vip                       boolean,
    vip_expiration_date          integer,
    column_15                    integer,
    column_16                    integer,
    column_17                    integer,
    column_18                    integer,
    column_19                    integer,
    add_date                     timestamp default now() not null,
    last_auth_date               timestamp
);

create unique index users_phone_number_uindex
    on users.users (phone_number);

create table if not exists users.user_roles
(
    user_role_id serial
        constraint user_roles_pkey
            primary key,
    user_id      integer                 not null,
    target_id    integer                 not null,
    add_date     timestamp default now() not null,
    status_id    integer   default 0
);

create unique index user_roles_users_uniq
    on users.user_roles (user_id, target_id);

create table if not exists users.targets
(
    target_id          serial
        constraint targets_pkey
            primary key,
    target_name        text              not null,
    target_description text,
    app_id             integer default 0 not null,
    order_by           integer default 0 not null,
    group_by           text    default 0 not null
);

create table if not exists users.apps
(
    id             serial
        constraint apps_pkey
            primary key,
    app_name       text,
    role_target_id text,
    order_id       integer,
    browser_hash   text,
    parent_id      integer
);


create table if not exists users.temporary_codes
(
    temporary_code_id serial
        constraint temporary_codes_pk
            primary key,
    user_name         text,
    code              text
);


create index user_name_index
    on users.temporary_codes (user_name);

create table if not exists users.users_favorite_users
(
    users_favorite_users_id serial
        constraint users_favorite_users_pk
            primary key,
    user_id                 integer,
    favorite_user_id        integer,
    favorite_nickname       text
);

create index favorite_users_user_id_index
    on users.users_favorite_users (user_id);

create or replace view users.active_users(user_id, user_name, first_name, last_name, nickname, email, rating) as
SELECT users.user_id,
       users.phone_number AS user_name,
       users.first_name,
       users.last_name,
       users.nickname,
       users.email,
       users.rating
FROM users.users
WHERE users.status_id = 0;

create or replace view users.user_rolesv(user_name, role) as
SELECT u.user_name,
       t.target_name AS role
FROM users.user_roles r,
     users.active_users u,
     users.targets t
WHERE r.user_id = u.user_id
  AND r.target_id = t.target_id;

create or replace function users.insert_basic_roles() returns trigger
    language plpgsql
as
$$
BEGIN
    INSERT INTO users.user_roles(user_id, target_id)
    VALUES(NEW.user_id, 1);
    INSERT INTO users.user_roles(user_id, target_id)
    VALUES(NEW.user_id, 2);
    INSERT INTO users.user_roles(user_id, target_id)
    VALUES(NEW.user_id, 3);
    INSERT INTO users.user_roles(user_id, target_id)
    VALUES(NEW.user_id, 4);

    RETURN NEW;
END;
$$;

create trigger insert_basic_roles_on_register
    before insert
    on users.users
    for each row
execute procedure users.insert_basic_roles();

insert into users.apps (id, app_name, role_target_id, order_id, browser_hash, parent_id)
values  (1, 'აპლიკაცია_კურიერები', 'ROLE_COURIERS_APP', 1, 'couriers_app', null);

insert into users.targets (target_id, target_name, target_description, app_id, order_by, group_by)
values  (1, 'ROLE_COURIERS_APP', 'აპლიკაცია კურიერები', 1, 1, 'კურიერი'),
        (2, 'ROLE_COURIER', 'კურიერი', 1, 2, 'კურიერი'),
        (3, 'ROLE_SENDER', 'ამანათების გამგზავნი', 1, 3, 'კურიერი'),
        (4, 'ROLE_BUYER', 'მესამე პირი (მყიდველი)', 1, 4, 'კურიერი'),
        (5, 'ROLE_COURIERS_ADMIN', 'ადმინისტრატორი', 1, 5, 'ადმინისტრატორი');


-- accounting
CREATE SCHEMA accounting;

create table if not exists accounting.courier_fees
(
    courier_fee_id serial
        constraint courier_fees_pk
            primary key,
    user_id        integer,
    order_id       integer,
    courier_fee    double precision,
    column_5       integer,
    column_6       integer,
    column_7       integer,
    column_8       integer,
    column_9       integer,
    column_10      integer,
    add_date       timestamp default now()
);

create index courier_fees_user_id_add_date_index
    on accounting.courier_fees (user_id, add_date);

create table if not exists accounting.debts
(
    debt_id      serial
        constraint debts_pk
            primary key,
    user_id      integer not null,
    job_id       integer not null,
    card_debt    double precision,
    deposit_debt double precision,
    paid         boolean,
    parcel_id    integer,
    column_8     integer,
    column_9     integer,
    column_10    integer,
    column_11    integer,
    column_12    integer,
    column_13    integer,
    column_14    integer,
    column_15    integer,
    column_16    integer,
    column_17    integer,
    column_18    integer,
    column_19    integer,
    column_20    integer,
    add_date     timestamp default now(),
    change_date  timestamp default now()
);

comment on table accounting.debts is 'ვალები';

create index debts_user_id_paid_false_index
    on accounting.debts (user_id, paid)
    where (paid = false);

create index debts_user_id_paid_true_index
    on accounting.debts (user_id, paid)
    where (paid = true);

create table if not exists accounting.deposit_cash_flow
(
    deposit_cash_flow_id    serial
        constraint deposit_cash_flow_pk
            primary key,
    user_id                 integer,
    flow_in                 double precision,
    flow_out                double precision,
    current_deposit_balance double precision,
    column_1                integer,
    column_2                integer,
    column_3                integer,
    column_4                integer,
    column_5                integer,
    column_7                integer,
    column_8                integer,
    column_9                integer,
    column_10               integer,
    add_date                timestamp default now()
);

create index deposit_cash_flow_user_index
    on accounting.deposit_cash_flow (user_id);

