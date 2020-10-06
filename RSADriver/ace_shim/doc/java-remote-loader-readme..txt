To use the java remote loader with the RSA driver, a few JVM settings must be set as described in the readme. However, since the java remote loader uses it's own JVM, the settings need to be set where java is executed.

Follow these steps:

 - Set up your java remote loader by extracting the tar files from the java-remote loader directory on the IDM install media.  I am using IDM 4.0.2 on Linux, so I'll pull the tar files from:
/media/IDM4.0.2_Lin/products/IDM/java_remoteloader.

	Note: there are three tarballs, only two of the three are required:
		- dirxml_jremote_dev.tar.gz: needed; untar . .
		- dirxml_jremote.tar.gz: needed: untar . .
		- dirxml_jremote_mvs.tar: not needed: this is for HP-UX AS/400 and z/OS operating systems; see IDM documentation for more details.

-Copy the jace.jar and ACEShim.jar to the following directory on your Remote Loader server:
-- Windows or Linux: To the \lib directory under your java remote loader untarring that you did in the first step.
---For Linux, Set privileges on the jar files to 755

Update the dirxml_jremote with the following:
 - Start by backing up dirxml_jremote.
 - Update the call to java from this:
 
 java -classpath $CLASSPATH com.novell.nds.dirxml.remote.loader.RemoteLoader $*
 
 To this:
 
 /opt/novell/eDirectory/lib64/nds-modules/jre1.6.0_31/bin/java -classpath $CLASSPATH -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl -Dsun.lang.ClassLoader.allowArraySyntax=true com.novell.nds.dirxml.remote.loader.RemoteLoader $*

 Of course, you may set up your path to your java installation if you prefer not having a static path in your script. Be sure that all of the data on this call ends up on the same line.
 
To start the java remote loader on boot on SLES, you need a start up script file, and then register the script. The best script I found so far has been added to this document.  Save all of the data between the BEGIN and END markers, save it as 'novell-javaremoteloader' and register as follows:

chkconfig --add novell-javaremoteloader.

The "### BEGIN INIT INFO" section is what decides what other services are required, and what run levels the RL will be running on.  3 and 5 should be sufficient in most cases; though modify as needed for your install.

Remove the "java-remote-loader" portion from teh beginning of the "java-remote-loader-novell-javaremoteloader.txt", and delete the .txt extension, and you can use this as a start up script if you need the java remote loader to star ton boot.  Be sure to set the paths and the runlevel configuration as needed. Also note: the start up script file as checked in, is checked in with Unix line endings.  Be sure these are preserved to avoid any goofiness on Unix type systems.