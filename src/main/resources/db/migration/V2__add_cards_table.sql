create table if not exists users.user_cards
(
    user_cards_id serial
        constraint user_cards_pk
            primary key,
    user_id       integer,
    card_number   integer,
    valid_thru    text,
    cvv           integer,
    add_date      timestamp default now() not null
);

create index user_cards_user_id_index
    on users.user_cards (user_id);
