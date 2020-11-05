Summary
Version and build number: @BUILD_AND_REV@

Release notes:
 - This is the RSA driver shim release as requested by UMB Bank.
 
Files included:

doc\readme.txt: RSA readme, delivered previously.
doc\rsa.pdf: RSA documentation delivered previously.
export\RSA-vanilla-from-packages: - Latest TriVir RSA Driver XML created from the designer packages included in the RSA delivery zip.
README.txt: this file
RSA-ACE-driver-2.1.62.zip: latest ACE driver shim, and libraries.
UMBBank-IdMUnitTests.zip: Latest Designer importable project 'archive' of the IdMUnit customer release, version 2.1.54-b31
	 - This file can be directly imported as an 'archived' java project into Designer.
	 
	 
Contents of UMBBank-IdMUnitTests.zip:

While this file should be imported as a java archive project file, it also includes some additional items:

doc: Documentation for IdMUnit
lib: Libraries needed to run IdMUnit.
test: Test folder including sample runner. Review documentation in the doc directory for more information.
	Note: this folder also contains required idmunit-config.xml, and idmunit-defaults.properties along with a sample test file.
.classpath: classpath setup for IdMUnit.
.project: Designer importable project file.
License.txt: Licence for IdMUnit.


===================================================================================================
Release Deploy notes:

 - Import the ACE shim.jar file to the standard location.


====================================================================================================
CHANGELOG:

Release 1.0 (Release 1, build 0)
 - Delivery of the requested files from the SOW, as listed above.

Release (NEXT)
 - Fixed some documentation issues.
