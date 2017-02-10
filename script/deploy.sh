#!/bin/bash

cp target/rpmachine.jar rpmachine.jar

# Then we deploy it using SSH because I still don't want to learn how to use rsync o/

targetName=rpmachine-$TRAVIS_BUILD_NUMBER-${TRAVIS_COMMIT:0:8}.jar
scp -oStrictHostKeyChecking=no rpmachine.jar deploy@ks.zyuiop.net:/srv/archive.zyuiop.net/RPMachine/$targetName
ssh -oStrictHostKeyChecking=no deploy@ks.zyuiop.net cp /srv/archive.zyuiop.net/RPMachine/$targetName /srv/archive.zyuiop.net/RPMachine/rpmachine-LATEST.jar
