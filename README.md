Maven (Nexus) Poller Plugin for Go
==================================

Introduction
------------
This is a [package material](http://www.thoughtworks.com/products/docs/go/13.3/help/package_material.html) plugin for [Go](http://www.thoughtworks.com/products/go-continuous-delivery). It is currently capable of polling [Nexus](http://www.sonatype.org/nexus/) repositories.

The behaviour and capabilities of the plugin is determined to a significant extent by that of the package material extension point in Go. Be sure to read the package material documentation before using this plugin.

Installation
------------
Just drop go-maven-poller.jar into plugins/external directory and restart Go. More details [here](http://www.thoughtworks.com/products/docs/go/13.3/help/plugin_user_guide.html)

Repository definition
---------------------
![Add a Maven (Nexus) repository][1]
Repo URL must be a valid http or https URL. Basic authentication (user:password@host/path) is supported.

Package definition
------------------
![Define a package as material for a pipeline][2]
Group Id and Artifact Id refer to the corresponding entries in [pom.xml](http://maven.apache.org/pom.html#Maven_Coordinates). Click check pacakge to make sure the plugin understands what you are looking for.

Package Metadata
----------------
The following additional POM info is accessed by the plugin:

[url](http://maven.apache.org/pom.html#More_Project_Information), if available, is used to display a TrackBack link. This is handy when the package is published outside of Go and we need a way to trace back to the piece of automation infrastructure (e.g. Jenkins job) that published it.

Published Environment Variables
-------------------------------
The following information is made available as environment variables for tasks:

    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LABEL
    GO_REPO_<REPO-NAME>_<PACKAGE-NAME>_REPO_URL
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_GROUP_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_ARTIFACT_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGING
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LOCATION

The LOCATION variable points to a downloadable url.

Downloading the Package
-----------------------
To download the package locally on the agent, we could write a curl (or wget) task like this:

                <exec command="/bin/bash" >
                <arg>-c</arg>
                <arg>curl -o /tmp/mypkg.jar $GO_PACKAGE_REPONAME_PKGNAME_LOCATION</arg>
                </exec>

When the task executes on the agent, the environment variables get subsituted and the package gets downloaded.

Or, to simply pass it as an argument to a deploy script on a remote server

                <exec command="/bin/bash">
                    <arg>-c</arg>
                    <arg>ssh server "cd /to/dest/dir;deploy.sh $GO_PACKAGE_REPONAME_PKGNAME_LOCATION"</arg>
                </exec>

Notes
-----
This plugin will detect at max one package revision per minute (the default interval at which Go materials poll). If multiple versions of a package get published to a repo in the time interval between two polls, Go will only register the latest version in that interval.
	
[1]: img/maven-pkg-repo.png  "Define Maven (Nexus) Package Repository"
[2]: img/maven-add-pkg.png  "Define package as material for a pipeline"