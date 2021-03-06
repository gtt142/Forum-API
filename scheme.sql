\connect forum_api

SET ROLE tp_subd;

CREATE TABLE public.users (
  user_id serial PRIMARY KEY NOT NULL,
  nickname VARCHAR(50) NOT NULL UNIQUE,
  fullname VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  about TEXT
);

CREATE TABLE public.forum (
  forum_id serial PRIMARY KEY NOT NULL,
  posts INTEGER default 0,
  threads INTEGER default 0,
  description TEXT,
  slug VARCHAR(256) NOT NULL UNIQUE,
  title VARCHAR(256) NOT NULL,
  username VARCHAR(64) NOT NULL
);

CREATE TABLE public.thread (
  thread_id serial PRIMARY KEY NOT NULL,
  votes INTEGER default 0,
  description TEXT,
  created TIMESTAMPTZ,
  forum VARCHAR(256),
  message TEXT,
  slug VARCHAR(256),
  title VARCHAR(256),
  author VARCHAR(64)
);

CREATE TABLE public.post (
  post_id serial PRIMARY KEY NOT NULL,
  votes INTEGER default 0,
  description TEXT,
  created TIMESTAMPTZ,
  forum VARCHAR(256),
  message TEXT,
  isEdited BOOLEAN default FALSE,
  parent INTEGER default 0,
  history INTEGER[],
  thread INTEGER,
  author VARCHAR(64)
);

CREATE TABLE public.votes (
  user_id INTEGER NOT NULL,
  thread_id INTEGER NOT NULL,
  vote INTEGER default 1
);

CREATE VIEW forum_users (forum, nickname)
  AS (
    SELECT t.forum, t.author
    FROM thread t
    UNION
    SELECT p.forum, p.author
    FROM post p
  );