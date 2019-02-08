# Guacamole Docker Startup Extension
This repository contains a Guacamole extension that starts up a Docker
container specified by an image for a particular User, User Group, or all users
of the system.  This extension decorates other extensions, relying on another
extension to store attributes for Users and/or User Groups.  Currently the
JDBC module within Guacamole is the only module to support this.
