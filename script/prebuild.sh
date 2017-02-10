# !/bin/bash

wget --no-check-certificate https://files.zyuiop.net/spigot.jar -O spigot.jar
mvn -q org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=spigot.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.10.2-R0.1-SNAPSHOT -Dpackaging=jar
