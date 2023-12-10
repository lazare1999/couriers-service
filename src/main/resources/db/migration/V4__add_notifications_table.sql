create table if not exists users.notifications
(
    notification_id serial
        constraint notifications_pk
            primary key,
    user_id       integer,
    status_id       integer,
    body            text,
    add_date      timestamp default now() not null
);

create index notifications_user_id_index
    on users.notifications (user_id);
