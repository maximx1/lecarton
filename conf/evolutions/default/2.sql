# --- !Ups
ALTER TABLE profiles ADD COLUMN isAdmin bool NOT NULL DEFAULT false;
UPDATE profiles SET isAdmin = true WHERE username ='anon'

# --- !Downs
ALTER TABLE profiles DROP COLUMN isAdmin;