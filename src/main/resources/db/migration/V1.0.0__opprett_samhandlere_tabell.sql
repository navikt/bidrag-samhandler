CREATE TABLE IF NOT EXISTS samhandlere
(
    id                  integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (INCREMENT 1 START 1 MINVALUE 1),
    ident               text NOT NULL,
    navn                text NOT NULL,
    offentlig_id        text,
    offentlig_id_type   text,
    norskkontonr        text,
    iban                text,
    swift               text,
    banknavn            text,
    banklandkode        text,
    valutakode          text,
    bankcode            text,
    adresselinje1       text,
    adresselinje2       text,
    adresselinje3       text,
    postnr              text,
    poststed            text,
    land                text,
    endret_tidspunkt    timestamp,
    opprettet_tidspunkt timestamp DEFAULT current_timestamp
);

CREATE INDEX navn_index ON samhandlere (navn);
CREATE INDEX ident_index ON samhandlere (ident);