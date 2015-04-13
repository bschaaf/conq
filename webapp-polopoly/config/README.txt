Each file under the common directory here will be included under WEB-INF/classes.

The files present under each remaining subfolder will be selectively included in
the war file based on the chosen profile (actually, based on the targetEnv property).
The files will also be included under WEB-INF/classes, overwriting any files with
the same name from the common directory.

E.g. if the current profile is dev, all files under config/common will be included
under WEB-INF/classes in the resulting war file, and all files under config/dev/
will also be included.
