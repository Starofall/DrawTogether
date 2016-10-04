# Add Post

# --- !Ups

-- a collection of users
CREATE TABLE IF NOT EXISTS users (
  id              VARCHAR NOT NULL,
  username        VARCHAR NOT NULL,
  salted_password VARCHAR NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (username)
);

-- a collection of drawings
CREATE TABLE IF NOT EXISTS drawings (
  id                 VARCHAR NOT NULL,
  title              VARCHAR NOT NULL,
  total_rounds       INT     NOT NULL,
  seconds_per_round  INT     NOT NULL,
  base64_png_image   VARCHAR NOT NULL,
  creation_timestamp BIGINT  NOT NULL, -- time of its creation
  PRIMARY KEY (id),
);

-- a collection of users who gave a star to a given drawing
CREATE TABLE IF NOT EXISTS drawing_stars (
  drawing_id VARCHAR NOT NULL,
  user_id    VARCHAR NOT NULL,
  PRIMARY KEY (drawing_id, user_id)
);

-- a collection of users who contributed to a given drawing
CREATE TABLE IF NOT EXISTS drawing_contributes (
  drawing_id VARCHAR NOT NULL,
  user_id    VARCHAR NOT NULL,
  PRIMARY KEY (drawing_id, user_id)
);

-- a collection of commands from users to a given drawing
CREATE TABLE IF NOT EXISTS drawing_commands (
  id         VARCHAR NOT NULL,
  user_id    VARCHAR NOT NULL,
  drawing_id VARCHAR NOT NULL,
  fromX      INT     NOT NULL,
  fromY      INT     NOT NULL,
  toX        INT     NOT NULL,
  toY        INT     NOT NULL,
  size       INT     NOT NULL,
  color      INT     NOT NULL,
  PRIMARY KEY (id),
);

# --- !Downs