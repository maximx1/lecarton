Le Carton
=========

[![Build Status](https://travis-ci.org/maximx1/lecarton.svg?branch=master)](https://travis-ci.org/maximx1/lecarton)

### Description
This is another pastebin clone that I wish to make with added functionality like:

* Timed delete
* Profiles
* Social constructs - commenting, following, and markdown
* Plugin support
* rss feeds

This amounts to a standalone gist.

### Deploy
Obviously, clone the git repo 

#### Heroku
1. Set up a heroku instance with the following either through heroku website or using heroku create:
    * At least 1 dyno
    * Heroku postgres (Or any other postgres host)
2. You need to set up some env variables in the system:
    * PORT =>
    * DATABASE_URL => url/username/password for the database
3. Set up the code with heroku (using [git](http://stackoverflow.com/a/5129733/3183419) or [heroku's signin](https://devcenter.heroku.com/articles/git#creating-a-heroku-remote))
4. Push via `git push heroku master`

#### Standalone (linux only)
This will run the app in the background. You can uncomment out the credentials in in the [conf file](https://github.com/maximx1/lecarton/blob/master/conf/application.conf#L41-42)

1. Run
    * `./jecommande run <portnumber>`
2. Stop
    * `./jecommande kill`

##### Notes
* You can modify jecommande to change the database storage location and name. By default it'll save in the root of the cloned project. Under .data/
* You can also copy the command line overrides from Procfile and past them in jecommande if you wished to use some other database system.

You can check jecommande usage by running `./jecommande`.

### Data location
Default data location for running with jecommand is in the root of the project .data/

### For Developers
##### debug locally
1. Run `./activator -jvm-debug` to start the debugger and `run` once the debugger is ready
2. In intellij
    1. Open run->Edit Configurations...
    2. Then hit the + (select remote)
    3. Once there enter 9999 in the port (everything else should be good)
    4. Select the new run config in the dropdown next to the debug icon and click debug
