Summary
Version and build number: @BUILD_AND_REV@

Release notes:
 - This is the RSA driver shim release as requested by UMB Bank.
 
Files included:

 - Latest supported TriVir RSA Driver 
 - Sample Policy XML file - See "RSA-vanilla-from-packages.xml"
	 - Note: this was the driver export created from the RSA driver Designer packages, also included as 'designer_packages'.
 - IdMUnit customer release (v2.1.43 or later)




===================================================================================================
Release Deploy Instructions:

Import the 


Assumptions:
 - The AUP war file is in a separate deliverable.

Schema:
 - Import the schema file into Designer and deploy any schema changes. File name: IDV-fcpsUserAuxClass-Schema.ldif

Prepare the Designer project on the jumpstation:
 - Update the previous release project with what is currently deployed. (Should be very similar if not identical).
 - Right click on the driver set, and do a 'Live Compare'
 - Import any differences into Designer.
 NOTE: If the FCPS Library has been updated, export the xml, sort it, and check it in.
 - Close all the open tabs in the main window of Designer.
 - This will bring you back to the 'Project' tab in the left window.
 - Make sure that the project name matches the currently deployed release. (If it doesn't right click it and choose 'Rename Project'). This allows us to go back to the previous Release in case of problems.
 - Right click on the Designer project, and select 'Copy Project'.
 - Name the new project to represent the release that is being deployed.
 - Delete any old projects from the workspace. (We only need the currently deployed Release project and the new Release project.)
 - Double click on the new project. This will bring up the "Outline" tab in the main window.
 - Designer is now ready to accept the new iteration.

Setup Designer to deploy the release:
 - Check that Designer is correctly configured to handle GCV updates in your release:
 - Click Window-> Preferences -> Novell -> Identity Manager -> Import/Depoy -> Then check 'Overwrite GCVs while importing from a configuration file'
 - Click Window-> Preferences -> Novell -> Identity Manager -> Configuration -> Then check 'Overwrite GCVs on the target server during copy' 
 - Delete any polcies that should be deleted as part of this iteration. Check the summary section of this document to see if anything should be deleted in this Release. Deploy the deletions.(This ensures that any deleted policies are not left behind. This can be done in Designer and/or eDirectory.)
 - If in Production, use the existing FCPS Library.xml. If you are deploying to DEV or TEST, import the FCPS Library.xml from Production.

Import the drivers into Designer:
 - In Windows Explorer: Extract the current Release in a location where you can easly navigate to it.
 - In Desginer: Right click on the Driver Set and choose "Import from a configuration file".
 - Select the driver on the filesystem, and rename from *****.xml to *****.xml.done. Doing this makes it not appear in your import screen any more. That way it is easy to track which drivers have been downloaded.
 - Put in the requested information for each driver.
 NOTE:
  - For Scoping Browse to Default Notificaiton Colleciton.Security. It will return it in DN format. Update it to be dot notation.
  - For Scoping on the Disabled Driver leave them blank, then pull them back in from eDirectory during the compare.
  - For passwords refer to the Environment file and the informaiton below.
  - PowerShell - No Authentication password and TriVir on the other passwords.
  - WorkOrder driver - No passwords. Leave them blank.
  - Remote Loader passwords and driver passwords are all trivir.
  - Disable Driver has no passwords.
  - Deploy each driver. Designer should report that each driver was successfully deployed. It should return a "Green" status. Investigate it if it is red or yellow.
 NOTE: If you get an SPIException and the deploy fails then re-deploy, and fix the jobs (see below).
=====================================================================================
Configuring User Application for Workflows
=====================================================================================
NOTE: UserApplication driver is named UserApplication in DEV and UserApplicationDriver in TEST

General Info:
Config info to execute workflows is sourced from iManager, Designer and User Application. The objective is to collect information from these 3 sources in the form 
of LDIFs and XML into a deployable zip file. Given below are some of the highlights on what was configured. Please refer to a full deployment documer for more
details and screenshots (to be ready by March 20th). Configurations items have been made into relevant categories below

Configuring User Application Navigation Permissions:
 - Login to User App using uaadmin account
 - Under Administration-->RBPM Provisioning Security-->Navigation Access Permission
 - Give navigation permissions to Manager Group for Work Dashboard-->Settings

Configure and Deploying Workflows:
 - Login to iManager
 - Search for individual PRDs (or workflows) under services-->idm-->DriverSet--->UserApplication-->App Config--->Request Def
 - Give ManagerGroup/ProxyGroup trustee rights on Create and Attest Non-employee workflows. Check dynamic on Attribute and Entry rights
 - Give Non-employee container trustee rights on Modify and Rename Non-employee workflows
 - ManagerGroup/ProxyGroup trustee rights to ProxyDefs container under App Configs
 - Deployment of the workflows and DAL artifact is done from Designer-->Provisioning Tab
Workflows and Related Artifacts
 - 4 Workflow PRD xml exports
 - DAL export of Entities, Queries, Global List
 Portal Pages
  - Each portlet page corresponds to a workflow and permission to access this page should mimic the trustee rights on PRDs shown above
 Administrator Assignments
  - ProxyGroup/ManagerGroup assigned Provisioning Manager roles
  - ManagerGroup given the "Congifure Proxy" privilege in addition to other PRD privileges for Provisioning. ProxyGroup does not get this privilege
Role Service Driver
   - Frequency of evaluation of Dynamic and Nested groups set to 1 min (found under Driver Properties --> Driver Config)
Email Templates
  - Templates for Create Non-employee Account confirmation included     - NonEmployee-CreateConfirmation and NonEmployee-CreateConfirmaiton_en (identical, needed by the workflow)

=====================END User App Configuration=========================================== 

Repair the jobs on each driver
 - Login to iManager.
 - Choose the "Roles and Resources" button.
 - Choose the "Driver Set Overview".
 - Click on the driver that needs to be fixed.
 - In the window that comes up on the driver select the "Jobs" tab.
 - Click on the box next to the job and click "Get Status".
 - Click on the driver name. Click OK once the dialog box pops up.
 
If the adamDriverSSHProcessor is updated: ADAM driver: installation of the SSH processor:
 - copy the file to: /root/adamDriverSSHProcessor
 - ensure the file is owned and executable by root:
 chmod 100 /root/adamDriverSSHProcessor
 - Create a new cronjob to execute the ssh processor:
*/5 * * * * /root/adamDriverSSHProcessor
 - Copy the certificates to the right location as mentioned in the processor.

Validate
 - Verify that each driver is up and running.
 - Run test 1.1 to validate things are syncing as expected.
 

====================================================================================================
How to roll back in case of a problem:
- If for any reason the most recent iteration needs to be reverted back to the previous iteration, follow these steps:
     - Close all open tabs.
     - Double click on the old project that contains the previous desired release.
     - Right click on the driver set, and execute a full deploy.
     - Restart the drivers.
    Note: when in this scenario, there may be unused policies in the directory; this is fine, as the assignments of policies are contained in the designer projects.
 
====================================================================================================
Supervisor notification: Instructions on populating supervisor, and triggering AUP notification emails to supervisor
 - Ensure the latest AUP manager notification email is installed (Import the EmailTemplates.ldif, or the raw AUP Expiration Notify Managers.xml file).
 - Ensure the Lawson tables have been updated as appropriate with the SUPERVISOR_WORKFORCEID column.
 - Ensure Lawson.IDView exposes the proper SUPERVISOR_WORKFORCEID column.
 - Ensure latest drivers are installed, restarting each after installing as described above
 - Using an insert statement, such as the "SUPERVISOR ID UPDATE SQL" shown below, populate the event log with updates to SUPERVISOR_WORKFORCEID.
     Note: the example given here will trigger an update to ALL NON-TERMINATED USERS THAT HAVE THE SUPERVISOR_WORKFORCEID POPULATED! It may be desired to modify the script to trigger a smaller amount of users while testing initially.  It may also be necessary to modify the example script shown here to work for the environment in which it is run.
 - This process will may take multiple hours to complete, as it will do 25,000 to 30,000 attribute updates.  
    - Confirm the driver trace shows the driver is idle before continuing.
 - Confirm a handful of users have their AUP acceptance date expired at least one year + 3 weeks prior to the date testing.  The manager notification code uses the following LDAP filter to determine on which users to notify:
    (&(objectClass=user)(fcpsAUPAcceptanceDate<=$oneYearThreeWeeksAgo$))
    - This filter essentially means to find all users who accepted the AUP at least one year and three weeks prior to the date the query executes.  It may be desired to run this filter in an external LDAP tool, to confirm some users will appear in the manager reports.
 - Note that users to be included in this report should have the following attributes populated:
    - manager,workforceID,sn,givenName,fcpsAUPAcceptanceDate,l,title
    - Also, the user specified by 'manager' above, must have an "Internet EMail Address" attribute value populated.
    - If any of the above attributes are missing for a user, results different than expected may occur.
 - Trigger the "AUP Expiration Notify Managers" in the Disable driver to cause the emails to be sent.
 - If a regular reminder email is desired, configure the job schedule as desired.

SUPERVISOR ID UPDATE SQL EXAMPLE:
INSERT INTO IDMLAWSON.EVENT_LOG
(
record_id,
table_key,
event_type,
event_time,
table_name,
column_name,
old_value,
new_value
)
SELECT
idmlawson.seq_log_record_id.NEXTVAL,
'pk_employee='||PK_EMPLOYEE,
2,
SYSDATE,
'lawson.idview',
'SUPERVISOR_WORKFORCEID',
'',
SUPERVISOR_WORKFORCEID
FROM lawson.idview
WHERE supervisor_workforceid IS NOT NULL 
AND EMPLOYEE_STATUS <> 'TX' AND EMPLOYEE_STATUS <> 'RX';

====================================================================================================
Known Issues:

Relase 45:
     - fcpsTACAcceptanceDate is not set for employees who were converted from a non Employee.
     

Lawson:
 - Two renames from Lawson in the same day cause multiple renames and emails to the user
 - Discussion needed: when converting from employee to non employee, renames are not executed (meaning a user named dduck will not be renamed to djduck if initials are provided).  I believe however, that this is desired functionality; though we should discuss it.
 
Disable driver:
 - Matches from Lawson to the tree will now cause rename notification emails to be sent to the target user, and create a rename work order
    Dev note: since this is in the disable driver, we are no longer detecting from-merge events; see if this is an issue.

PowerShell:
 - Sending new user emails to newly created AD users: delayed mailbox creation - may need to delay email to user
 - Non-Employee to employee situations: should send new user creation emails (on hold until release 34)
 
AD:
 - Avoid sending two 'user already exists' emails in the AD driver.
     - November 3, 2011: Currently considered too minor to resolve for now.
     - This occurs because teh modify password comes through as a seperat event, turns into an add, and sends the second email.
 - Cosmetic error - SC-UserMOdify - (if-op-attr 'L' not-equal "$correctValue$") = TRUE should be false when op-attr L and correctValue are both blank!
 It looks like we have had a few users that have had their USER_LEVEL_DESC attribute cleared in Lawson, which eventually hits a bug in some attribute reset code in the AD driver. When it tries to compare the value in AD with the value in eDirectory, it's failing. I've added it to the list of things to fix in release 32; however, it is only a cosmetic error and is not currently causing any issues.
     - November 3, 2011: This is just an optimization, too minor to resolve for now.
  - unnecessary code: processing on TX|RX in SC-UserMove isn't ever hit, remove code/retest - check carlicence on term
  
 Disable:
   - Cosmetic issue: Disable driver: password expiration job: 
    token-xpath("$current-node/attr[@attr-name='passwordExpirationTime']/value")
    - when no password expiration time is populated, don't tr to convert it!
     - November 3, 2011: This is just an optimization, too minor to resolve for now.

 PowerShell:
  - note for further review: re-enable of mailbox: review triggering off DirXML-AdContext, rather then move from non-disabled container to complete move.  Test 5_6

 Disable driver:
  - LdapSearch code returns a child tag of <stats level="error"... when there is an issue, and should be dealt with, rather then treating it as an <instance . . .
  - Not an issue, when the server is configured properly; review need for error reporting if at all.
 
UserApp:
     - When a user doesn't have fcpsUser added, an exception is thrown,  If desired, we could send out a useful error message that the end user to give to the service desk; however, assuming the user was created with normal channels, this shouldn't ever happen; however, it would reduce the chance of an end user seeing an 'internal server error 500', and a big stack trace.
        
Matching Scenarios:
     - Need to fully define and re-test, not currently in the AC (test 1_10* tests)
     
Testing:
     - Need to implement iMap testing + Greemail in local vm.
     
====================================================================================================
Test ID's: 

*IN PROGRESS*: to be completed in a future release.
WORKFORCEid: 920015

====================================================================================================
CHANGELOG:

Note: Historical logs have been removedprevious release change log items have been removed from this file for brevity. If the history is desired, please request it from TriVir.


Release 29
Issue: Non Employee AD driver was accepting renames from AD, and changing CN in IDV to the full AD cn (lastname, firstname)
Resolution: Updated Non-Employee driver to ignore these renames, and confirm that sAMACcountName was the only attribute that could trigger a rename.  Creations and renames from the NonEmployee driver that would result in a user name that is > 12 characters in IDV have now been blocked.  An email is sent in these cases.

Issue: Lawson driver was allowing user ID's up to 13 characters
Resolution: Updated Lawson driver to ensure that all CN's generated for IDV are 12 characters.

Issue: AD driver password caches were not being cleaned out properly.
Resolution: each of the AD drivers now properly discard password events if they're not ment to be processed on that driver, as both AD drivers receive all password changes.


Release 30
Issue: missing text in email for resolution of 'name too long' issue.
Resolution: Added text to email: " Please resolve this issue by using a shorter value for sAMAccountName for this user."

Issue: User creates/renames from Lawson should allow a generation of 12 character user names.
Resolution: Lawson driver code has been updated to generate CNs at the proper length.

Issue: password caches were not being properly cleaned up, as both AD drivers get all password changes.
Resolution: each driver now properly clears the password cache when the password change is for a user that it does not process.

Issue: The PowerShell driver was not creating/managing mailboxes for Non Employees, creating additional manual work, and causing issues with transfers between employees/nonemployees
Resolution: The PowerShell driver has been updated to manage mailboxes for Non Employees.

Issue: Lawson driver: DirXML-WorkToDo class was in Lawson filter, causing unnecessary processing.
Resolution: The class was removed.

Issue: Non employee driver wasn't allowing creation of users without an email address (needed for non employee mailbox creation)
Resolution: Driver has been updated to not require an email address, and create a default one to begin with.

Issue: The Non Employee had some unnecessary code using a temporary email address.
Resolution: this wasn't really an issue, it was simply some unnecessary code, but it has been removed either way.  The email address is already being properly generated, then populated after the PowerShell driver creates the mailbox.

Issue: Non Employee driver didn't officially have the log file roll over.
Resolution: Log file rollover has been included.

Issue: passwords were appearing in the trace at times.
Resolution: passwords have been hidden appropriately.

Issue: Non employees were incorrectly receiving the new employee email.
Resolution: a check for the employee type has been added; this email is sent only for Employees now.

Issue: Incorrect class name was used on user to group associations: just a cosmetic issue, but very confusing for trace readers.
Resolution: the class name has been fixed.

Issue: Non Employees were not getting their initial password set upon creation
Resolution: Initial password events are now detected properly, and synchronized as needed.

Issue: This was more of a question, but it is: does disabling a mailbox remove it from the address list?
Resolution: The answer is yes, the disable mailbox process causes the address to be removed from the address list.

Issue: Renames to sAMAccountName in the Non Employee driver didn't update mailNickName properly
Resolution: the mailNickname is now updated properly.

Issue: Non employee driver did not properly expire the password even though 'require user to change password at next login was selected.
Resolution: 'user must change password at next login flag' is now being checked properly, and causing the same behavior to happen in eDirectory.


Release 31:
Note: the UMCPANL flag is the 'User must change password at next login" flag.

Issue: UMCPANL flag: AD: Password was being synchronized in situations where it shouldn't have been (clearing the UMCPANL flag).
Resolution: the password is being sent only during proper situations now.

Issue: Users passwords were visible in trace during certain situations
Resolution: Password has been hidden from the trace in all known scenarios.

Issue: AD: The driver was trying to synchronize passwords when it was not populated on the user.
Resolution: The presence of the password is now checked prior to attempting to synchronize it.

Issue: Disable driver didn't include Non Employees in the AUP notification.
Resolution: Non Employees have been included in the AUP notification.

Issue: Service desk was receiving too many error messages, and have requested they be removed from the email list.
Resolution: Build has been updated to ensure the service desk is not included on the email messages.


Release 32:

Issue: UMCPANL flag was being set on any from-merge documents; this is incorrect, and shouldn't be set unless the password is expired in eDirectory.
Resolution: UMCPANL flag is now based off of password expiration time to set it, and changing of a password in either AD or eDir to clear it.

Issue: Missing updates for AD-UserMove, LW-1-Rename Notification, and Password Expiration Notice email templates.
Resolution: These email templates have been included in this release.

Issue: postalCode appeared to have issues, it was added to the 'modifyattrs' list
Resolution: assuming the data was correct in test, I've updated the main build drivers.

Issue: PowerShell driver was requesting a removal of object class when updating calculated mailstore; currently cosmetic, but relies to heavily on the engine.
Resolution: PowerShell driver now properly sets object class in Calculate mailstore

Issue: Bus drivers/attendants need to be included in the AUP reminder and Supervisor AUP report query email notifications.
Resolution: The exclusion for Bus drivers/attendants has been removed.

Issue: Missing updates for AD-UserMove, LW-1-Rename Notification, and Password Expiration Notice email templates.
Resolution: The modified email templates have been included in this release.

Issue: Mailstore search some times failed, and ended up blanking out the currently chosen mailstore.
Resolution: Driver properly detects mailstore issue condition, sends an email, and leaves the currently assigned value.

Issue: An issue USER_LEVEL_DESC/L caused a reset error in the AD driver, and sent an LDAP_INVALID_SYNTAX email notification.
Resolution: Code has been updated to only attempt to update the L attribute when it isn't blank; the notification will no longer be sent when it is blank.

Issue: The room number attribute loops in a specific situation.
Resolution: The code causing the loop has been removed.


Release 32: (post go live)

Issue: The ADAM driver was missing the writeFile function, meaning it essentially was discarding events.
Resolution: The writeFile function has been added back, the driver restarted. The 21 user creations, and 17 user deletions have been manually processed.


Release 33:
Issue: Move events aren't consistently reported (simply an artifact of how eDirectory 8.8.6 and 4.0.1 work).
Resolution: used different mechanisms to ensure the work was done at the right time, and in the proper order.

Issue: TP/RP isn't fully supported in the drivers.
Resolution: TP/RP now is processed properly by the drivers.

Issue: Email Templates were out of date
Resolution: updated the following email templates:
    Password Expiration Notice 
    AD-UserMove
    AD-1-SuccessfulAdd
    AUPExpiration
    AUPExpirationNonEmp
    AUP Expiration Manager Notification
    LW-1-Rename Notificatione

 
Release 34:
Issue: The manager related attributes manager/directReports, isManager, managerWorkforceID were either not being used, or were being used incorrectly, and were not correctly populated, or cleared.
Resolution: the Lawson and Disable driver have been updated to manage them properly, to set and clear manager/directReports as needed.

Issue: Workflow functionality needed: Managers were not correctly identified.
Resolution: Updated code now marks 'isManager' for all users that are listed in the SUPERVISOR_WORKFORCEID column in Lawson.

Issue: Had unused code.
Resolution: removed Move terminated users to disabled as it is no longer being used.

Release 35:
Issue: Disable driver was named poorly.
Resolution: Renamed to 'Service driver' as it manages many jobs, above and beyond disabling users.

Issue: Rename workorders are supported only for Employees
Resolution: Rename work order generation has been moved to the disable driver.

Issue: Transfers from Employees to Non Employees isn't supported
Resolution: Drivers have been updated to support the transfer, maintaining all attributes, including password.

Implemented: 
 - 2.1 Non-employee Active Account Attestation (Confirm Action)
 - 2.2 Non-employee Active Account Attestation (Decline Action)
 - 2.4 Attestation of Disabled Non-employee Account (Confirm Action)

Release 36:
Issue: New Staff container structure is needed to suport the separation of students and staff for UserApp, and other various security concerns.
Resolution: This release supports the new ou=Staff,o=FCPS container now that is the parent of the Staff Employees and Non-Employee containers.

Issue: GCV's were separated, and duplicated regarding containers, causing configuration issues.
Resolution: all container references have been conslidated, and moved to the Driver set GCVs as appropriate.

Issue: Static references to containers cause issues when container changes occur.
Resolution: all static references have been updated to use the appropriate driver set GCV.

Issue: Food servers, and bus workers were incorrectly being excluded still, due to a check in issue.
Resolution: Removed exclusion of the above workers.

Issue: Incorrect email template was referenced for Non employees.
Resolution: Updated email templates to properly reference non employee email template.

Release 36.5:
Issue: Collisions are still occuring with the County's AD system; This AD system needs to be included in the collision check for creations and renames.
Resolution: This release was built to include a collsion check with the County's AD system upon employee creates and renames.

Issue: Service driver was triggering rename events for student renames
Resolution: Service driver processes renames only for objects in the employee or non employee containers.
    (note: this was done after deploying release 36.5.134 in DEVL and TEST.)

Issue: PowerShell, ADAM and Service drivers were processing student events, and shouldn't.
Resolution: Student scoping has been added to these drivers.

Release 37:


Release notes for release 37
Note:
    - Added employeeType population; confirm population of the same attribute in AD for staff.
Release 37:
    - Rename the AD employees driver to 'STAFF'
    - Log clean up: Create a separate Studentsbeforerelease36, and Employeesbeforerelease36 log directories.
    - Delete the Non employee driver for release 37!
Deployment:

Release 37: - Ensure the AD NonEmployee driver is stopped
     - ensure fcpsFocusGroup attribute exists
     - Ensure fcpsFocusGroup is set to false for all users, except for those users in the cn=FOCUS_PROD_USERS,ou=SAP,o=FCPS, whose members should have the flag set to TRUE
     - Workflows have been included in this release; deployment notes will be added later to this file.


     
Issue: Workflow related files were not included in the release.
Resolution: They have been added; however, documentation is still pending.

Issue: AD employees driver processes employees and non employees now, so the name is incorrect.
Resolution: driver was renamed to 'Staff'

Issue: User type identification needed to be more obvious.
Resolution: an attribute employeeType containing either 'employee' or 'nonemployee' has been added to increase readability.

Issue: Username collision check is required in the county SAP environment
Resolution: A resolution check has been added to the system for employee and non employee creations and renames.

Issue: Username truncation driver was still in place; it used a policy that needs to be updated.  Due to lack of time to properly test, this driver needs to be deleted.
Resolution: The UsernameTruncation driver has been deleted.  If we need to make use of it again in the future, it will need to be tied to the new generation 

Issue: Staff Driver:  Position changes (such as AI to AZ, an 'enabled' to 'enabled' event) were being moved/enabled as needed, however, any data changed along in the same event was incorrectly vetoed in these cases.
Resolution: No longer vetoing needed data during a 'enabled' to 'enabled' event.

Issue: Roll over feature missing for state driver
Resolution: roll over added.

Issue: UserApp date of 2099 for proxies is an invalid date in eDirectory.
Resoluiton: workaround has been added to the service driver to process the future date as not expired.

Issue: Lawson driver was producing errors for users added first in eDirectory with an email address, and incorrectly trying to process them.
Resolution: The Lawson driver now properly avoids processing users created in eDirectory outside of Lawson.

Issue: Table and library updates were difficult to manage, we needed a way to deal with items owned by FCPS, and items owned by TriVir
Resolution: split the library into two exports: one with tables only, and the other with all policies.


Release 38:
Issue: Immediate and Pending termination were failing for various reasons due to race conditions
Resolution: added forced ordering of events, so terminations are now completely race condition free.

Items pending FCPS response:
 - AC 5_16: need email content for rehired user.

===============================================================================================================================================================
Release 39.121:
New Features:
-Implementation of employee ACs 2.2.x involving nonemployee to employee transfer
-All Non-employee ACs have now been implemented in this release. Please refer to the ACs in the document "Acceptance Criteria-FCPS-Non-Employee (Full System).docx. 
-User Application has been configured to allow Managers, Proxies and Non-employees to access workflows based on their trustee rights to those workflows
-There is associated preparatory work with this release 39 that has been documented based on discussion with FCPS. The document is in SVN "Release 39 Go Live Tasks.xlsx"
===============================================================================================================================================================
Release 39.122:
Issue: Upon a rename an employee/nonemployee cn,sAMAccountName were changed appropriately but givenName, surname and initials did not sync to AD
Resolution: No longer vetoing the sync events upon a rename. A rename of user now will sync renames of name components such as giveName, surname and/or initials to AD.

Issue: Upon a migration of a  non-employe his/her AD homeShare (scriptPath, homeDrive, homeDirectory) attrs were getting deleted
Resolution: siteLocation of a non-employee is no now being calculated based on his/her manager and is used in the placement of the non-employee in AD. 
In AD non-employee is located in the manager's container upon an assignment of a manager with a valid siteLocation attribute.
The homeShare AD attributes are NOT set upon creation of a non-employee. When a non-employee migrates his/her account
using the workflow, his pre-existing homeShare attrs will now be preserved and not updated. 
Here are the various permutations of homeShare attrs:

Event                   homeShare Attrs
--------                ----------------
1.Create Non-emp       NOT SET
2.Migrate Non-emp      NOT SET/NOT MODIFIED
3.Attest Non-emp       NOT SET/NOT MODIFIED
4.Nonemp to emp        SET/MODIFIED

Per Matt Watson (FCPS ITSD), Non-employee AD homeShare attrs need not be set or modified per events in IDV and will be manually updated by AD team.


Issue: Check for references to FCPS Table Library under FCPS Library.
Resolution: There originally were 2 FCPS Table Libraries, one under the FCPS Library and one by by itself. Now the one under the FCPS Library has been removed and unit testing done to
to make sure that none of the IDM Drivers or workflows reference it anymore

Issue: siteLocation for non-employees based on assigned manager
Resolution: siteLocation for non-employees that helps create and place a non-employee in AD is based on an assigned manager. 
In cases where there is no manager assigned the siteLocation defaults to the CENTRAL office (just like for employees)

Issue: Modify workflow not populating Company field of a non-employee even in cases where the user has a Company attr in IDV
Resolution: The workflow now displays a stored company field value for the non-employee

Issue: Modify workflow Company label needs to have capitalized C letter
Modify: Capitalized the C letter

Issue: extensionAttribute1 being populated for a non-employee when user modifies his/her account using Modify Workflow
Resolution: extensionAttribute1 will now only be populated in all cases for an employee user

Issue: extensionAttribute1 and extensionAttribute6 need to be cleared in cases where an ex-employee becomes a non-employee
Resolution: A step has been added to the ex-employee to non-employee processs that will clear these 2 extensionAttributes

Issue: Migrate Non-employee Workflow link to be removed from Identity Self-service tab
Resolution: This was 1st removed in PROD and has been now done in other environments as well. All non-employees have now migrated (by a batch process) on 3/15/2013
===============================================================================================================================================================
List of fixes in Release 39.122 (8/29/2013)
Ticket#166 and #173 - Fix for issue related to fcpsFocusGroup flag getting removed on some newly added users (June 27 2013)
Ticket#167 - Fix for issue related to Disable Termination Pending trigger does not move accounts in AD (May/July 2013)
Ticket#165 - Upgrade AD Driver shim and Password Filter to the latest version (July 2013)

===============================================================================================================================================================
Minor update to Release 39.122 (8/29/2013)
Ticket#164 - Per Non-employee AC 5.1 - CC non-employee (personal and FCPS email addresses) when attestation expiration email reminder sent to manager - 
Resolution: Change made to sub-etp-Trigger-Attestation Expire policy in Service Driver 
===============================================================================================================================================================
List of issues/fixes in Release 40.123 (9/9/2013) - TEST
Ticket#135: Enhancement-Non-employee AC 7.1 - Create custom workflows to allow Proxy assignment/update
Resolution: Enhancement-Custom UI updates and workflow have been done to allow proxy assignment, update and removal
Ticket#148: Frozen Management Trigger not setting fcpsDisabledDate upon termination
Resolution: The trigger is now setting the disabled date
Ticket#149: Employee AC 3.2 - Update the email template and policy (related to employee location change) to include old home directory
Resolution: There is now a homeDir param that is passed to the email template sent upon a work location change
Ticket#150: Update the non-employee create confirmation notification
Resolution: Fixed typos on the template
Ticket#151: Enhancement- Append "Disabled IDM mm/dd/yyyy, Final mm/dd/yyyy" on the display name only after the fcpsDisabledDate has been set (employeeStatus is TX/RX) on an account
Resolution: Display names now have Final mm/dd/yyyy or Disabled IDM mm/dd/yyyy appended appropriately upon a termination
Ticket#152:  AC 5.6 - When an employee account that is frozen is thawed and terminated, the disabled date on that account should show the date of move to Disabled container
Resolution: The disabled date is now set to the date the account is moved to the Disabled container
Ticket#153: Employee AC1.22 & 1.22 - Employee rehired that matches an active non-employee account needs additional attribute values
Resolution: Office code, description, title, address and proxyAddresses now exist in AD
Ticket#154: Non-employee AC 3.1 - Upon a workorder initiated rename of a non-employee account, displayname attr loses the middle initial
Resolution: Display name now includes the middle initial even after rename
Ticket#155: Enhancement-fcpsFocusGroup Flag should be set to FALSE when an ex-employee is converted to a non-employee
Resolution: The conversion to non-employee process sets the existing fcpsFocusGroup flag to FALSE
Ticket#156: Employee AC 1.1 to 1.9 - ITSD received 2 or more duplicate emails related to email address duplication upon rename
Resolution: This issue manifested during employee/non-employee creation and when the mailbox alias matched the firstname.lastname or nickname.lastname combination of another account mailbox.
            A check has been put in place to do the collision check only once after new hire process starts
Ticket#157: Enhancement - Emails sent out for these ACs will need to CC FOCUS support team/user/admin upon account renames
Resolution: Code change done so that the notification prior to rename workorder execution notifies (CCs) FOCUS Finance group and it is only done for employees who are in FOCUS group.
Ticket#158: AC 5.8 - The Delete 30 Day Disabled job on the Service driver is using 2 different formats when it compares dates
Resolution: The date format now is made to match
Ticket#159: Ehancement - Synchronize sAMAccount from AD into Lawson SAM_ACCOUNT field 
Resolution: Lawson driver filters and schema mappings have been appropriately modified to accomplish this.
Ticket#162: Enhancement - Non-employee AC 4.2 - Allow manager to update non-employee attributes similar to a non-employee
Resolution: A new workflow now exists that allows managers to modify non-employee's information
Ticket#163: Enhancement - Non-employee AC 2.x - Add attestation date to search result popup in Verify Non-employee Workflow
Resolution Attestation date has now been added to the popup
Ticket#164: Enhancement - Non-employee AC 5.1 - CC non-employee (personal and FCPS email addresses) when attestation expiration email reminder sent to manager
Resolution: Already in PROD, please see "Minor update to Release 39.122" section
Ticket#173: Enhancement - Lawson field FCPS_FOCUS_GRP field is authoritative source for fcpsFocusGroup flag in IDV
Resolution: Lawson driver filter has been appropriately modified to accomplish this
Ticket#174: Enhancement - Non-employee AC 1.x - Set fcpsFocusGroup flag to FALSE upon non-employee creation
Resolution: Create Non-employee Account has been modified to set this value upon creation of non-employee account
Ticket#175: Update Password expiration notification template
Resolution: Password expiration template was originally modified in TEST by FCPS and imported to all other environments VM, DEV
Ticket# 177: Memory leak caused by a bug in Disable Term Pending trigger
Resolution: An unnecessary clone operation being done in a loop of the trigger has been removed. 
Ticket#178: Update to Manager termination notify template
Resolution: The text in the template has been appropriately updated
Ticket#179: Enhancement - Set a substitute manager for non-employee direct report upon manager termination
Resolution: Manager Termination job now looks for accounts of managers that have been terminated and sets the mdmum account as a sub manager until a new one is assigned to the non-employees
Ticket#183: As an attempt to resolve Ticket#177, max JVM heap space was set to 2GB (default 512 MB)
Resolution: This max JVM heap space size will be reset to the original default value as a resolution has been found for ticket# 177
Ticket#185: Enhancement - Add a new non-employee position (FPAC Member) to dropdown list of Create Non-employee WF
Resolution: List has been modified
Ticket#188: Enhancement - Do not send notifications to County Focus Finance Group when non-employees get renamed
Resolution: Notifications will only be sent when employees who belong to FOCUS group are renamed
Ticket#189: Update non-employee rename template
Resolution: Previously all staff had the same rename template. Since non-employee's do not have a wfid and do not need to access employee benefits page links, the rename template has been
            changed appropriately
Ticket#190:  Enhancement - Blank the SAM_ACCOUNT and EMAIL_ADDRESS field in Lawson record when a terminated employee becomes non-employee
Resolution: Lawson driver's policy has been modified to resolve this
Ticket#191:  AD LDS record needs to be deleted when an ex-employee becomes non-employee
Resolution: AD LDS record is now deleted in cases where an employee is deleted or when he/she is converted to a non-employee
Ticket#194: Make CENTRAL the fallback siteLocation when no siteLocation is available in Mapping Table
Resolution: CENTRAL is now a fallback siteLocation for all cases such as new hire, rehire or non-emp to emp conversions
Ticket#198:  Account rename fails for users without a middle initial
Resolution: Check has been put in place so that a blank MI is not to be set to AD.
===============================================================================================================================================================
List of issues/fixes in Release 40.124 (10/8/2013)
===============================================================================================================================================================
Ticket #200: Homedrive, scriptpath and homeDirectory not added to user upon non-emp to emp conversion (go to mapping table)
Resolution: When a non-employee become employee, that user gets homedriver,scriptpath and homeDirectory based on siteLocation match in mapping table.
Ticket #201: AC 1.6 Dsabled employee account cannot be converted to a non-employee due to state issues
Resolution: Ex-employees even in a "hired" state will get converted to 
Ticket #208 Suppress AD-UserMove email when an ex-employee becomes non-employee (AC 1.6) 
Resolution: Added conditionality that suppresses notifications to non-employees when they get converted from an employee account.
Ticket #209 Display name shows two "Disabled IDM mm/dd/yyyy" suffixes after Thaw of Frozen TP with Past Term Account (AC 5.6)
Resolution: Added regular expression match replace logic to prevent duplication of displayName suffix
Ticket #210 Employee AC 4.11 & 4.12 - Rehired Employee matched to active non-employee not being removed from Non-employee AD Group
Resolution: Added a group removal statement action during rehire task on Staff Driver.
===============================================================================================================================================================
List of issues/fixes in Release 41
 - Fixed workforceID missing from duplicate email message.
 - Added ADLDS 7th optional object to delete (LAW*environment* object)
 - Staff driver: changed 'postalCode' to 'Postal Code' to ensure it is properly cleared before setting in AD (remove-all-values added)
 - Staff driver: added 'L' to the modAttrs list, as it needs to be cleared from AD as well.
 - Added EmployeesADGroup GCV to the build for the Staff driver; as it was missing; and found to be incorrect in production.
 - Preserved job settings as seen in production: found various settings, such as triggering events for AUP Expiration*, and a job in the Staff driver that have been preserved in this release.
 - Added fcpsProcessLevelContrl to come from Lawson, and into ADAM, as a standard sync.
41.132 - ADLDS: moved 'adam' file writing directory sibling to the /logs directory on eDirectory.
 - Add modAttrs GCV to ADLDS to resolve issue with adding values to attrs that already held values (same solution from Staff driver, for the same reason).
 - Fixed deletion DN for LawsonUnix account deletion.
 - Changed 'now' hours/minutes/seconds from 000000 to 235959 to include the endpoint days.
 - Updated the AUPAcceptanceDateMissing code to run at the 'Staff' level instead of the Employees only container.
 - Removed AUPAcceptanceDateMissingNonEmp code and email as it is no longer needed, per Susan's request (email isn't distinct in anyway.)
 - Removed AUPExpirationNotifyManagers, as we don't use it any more.  Users accounts are getting password expirred, so we don't need to involve the managers.
 - Note: deploying release 43 before 42: Service driver had some sercurity role email; not deploying this policy until 42 content is deployed.  Bottom line: for next deploy, deploy all drivers as usual.
===============================================================================================================================================================
List of issues resolved in release 42
 - Added 5 new Lawson security attributes into the Lawson and ADLDS drivers.
 - Updated adamDriverSSHProcessor script to include the new Lawson super user account file.
===============================================================================================================================================================
List of issues resolved in release 43
 - Added release 42 fixes: added Law[Environment] object to the Login:eWorkforceID object; see ticket 220.
===============================================================================================================================================================
List of issues resolved in release 44
 - No longer creating Unix DN and idxref dn.
 - Updated AC and IdMUnit tests per Susan's latest review.
 - Added Susan's "executeLineB" changes that copy the LawsonSecurity file to a second location.
===============================================================================================================================================================
List of issues resolved in Release 45
 - Note: email templates have been left ONLY for reference purposes; FCPS owns the latest copies of email templates 
 - TODO: EMAIL TEMPLATES:  ensure they are synchronized everywhere.
 - Normalized drivers with JDK 1.6_0_20 
 - Improved method of enabling mailbox creation in non production environments (Test-MapiConnectivity now only run in non-production version of scripts.)
 - Tricia; discuss non emp email template sub-etp-trigger-managerterminationifno policy in service driver (managerterminationinfotemplate)
 - Changed Exchange driver log file name prefix from PSDriver to ExchDriver.
 - Issues fixed: 
    TEST: 85 
 - 88 resolved: when managers are terminated, we now delete all proxy definitions that the manager had set up.
 - Parents scoping: Drivers now explicitly scope out Parents container, or avoid it by using states.
 - 85 resolved: If matching rule finds matching non employee as a result of a terminated user event it is now vetoed (isntead of incorrectly converting back).
 - 101 resolved: on emp to non emp, now removing ADLDS base user account and association on state change.
 - 123 resolved: code is now sensitive to the posibility of users being proxies for more than one manager.
 - 98 only steting fcpsisProxy to false if the LAST proxy to be assigned expires.
 - 108: included Employees\Disabled container for 'modAttrs' work in the ADLDS driver.
 - (NEW: TRICIA ADDED A FEW MORE)82: Added additional CC as requested.
 - 82: Resolved by adding requested CC to the email situations mentioned.
 - 91: Added old home directory populated in AD-UserMove email.
 - 107: removed Susan and Aaron email addresses; no longer needed.
 - 122: removed Susan and Aaron email addresses; no longer needed.
 - 89: Changed design: we are no longer disabling mailboxes on decline: instead; we leave them there. Users don't have access to them; but any re-attestation means they would have immediate access to their maibox again.
 - Disable mailbox: now properly removes association for account in eDirectory.
 - Removed sending of the "Termination Security Roles template" as it doesn't exist.
 - Fixed issue in testing framework that was making it difficult to test the new AccountNotification@fcps.edu CC value.
 - Cycle fix: emptononemp: fixed issue that caused attempt to connect to mailbox for non-emp would fail (Dev note: emptonomemp startPowershell blocked in SA-User)
 - TAC: implemented driver side functionality for the TAC utility.
Release 45 fix builds:
Build 45.4:
 - ADAM: added Given Name/Surname for a total of 7 attributes the driver synchronizes to mod attrs (all single valued attributes)
                <item xml:space="preserve">fcpsAccessLawson</item>
                <item xml:space="preserve">fcpsAdd-INS</item>
                <item xml:space="preserve">fcpsCheckLS</item>
                <item xml:space="preserve">fcpsLawsonJobQueue</item>
                <item xml:space="preserve">fcpsPortalRole</item>
                <item xml:space="preserve">Given Name</item>
                <item xml:space="preserve">Surname</item>
    Not adding these values as they are multivalued.  If we need to control out-of-sync issues with these attributes; code must be written to do a remove-all-values, then add ALL existing values in eDirectory; can't use mod attrs.
                <item xml:space="preserve">fcpsGroupName</item>
                <item xml:space="preserve">fcpsProcesslevelContr</item>
                <item xml:space="preserve">fcpsSecurityRoles</item>
 - Fixed issue with SAM_ACCOUNT/EMAIL_ADDRESS NOT CLEARING
 - Manager termination: now properly removes all proxies, but NOT if they are still a proxy for another manager.
Build 45.5:
 - Fixed fcpsTACAcceptanceDate on initial hire to be one year ago tonight at midnight - example: running on 20150722000000Z produces an TAC expiration date of 20140722000000Z, meaning they would have one day from being hired to accept their TAC.
 - Updated service driver to 1GB in size for all environments.
 - Confirmed instead of trigger, LAWSON.TRIG_UPDATE_EMAIL_SAMACCT, is clearing fcpsFocusGroup when EMAIL_ADDRESS and SAM_ACCOUNT are null.
 - Fixed issue with mailbox not being hidden from GAL due to build script issue.
 - No longer clearing SAM_ACCOUNT/EMAIL_ADDRESS in lawson on TX/RX/TP/RP
 - Added CloudAD driver to build.
 **ROLLING THIS AS A RELEASE JUST TO DEPLOY CLOUDAD AD DRIVER IN TEST**
     - not deploying other drivers from this release.

Build 45.6:
 - Fixed issue where bad comparison in streetAddress caused infinite loop for users.
 - Updated TAC Expiration and TAC Expiring Soon jobs to search only in the Employees container and sub-tree (was pointing to Staff).
 - Confirmed users get one grace day to accept the TAC.
 - Confirmed nonemp2emp conversions end up populating the user with a TAC acceptance date that gives the user one grace day to accept the TAC.
 - Setting fcpsTACAcceptance date to give rehired employees one grace day.
 - Red X support: enabled code that supports the direct deletions of proxy objects (the red X button in UserApp)
 - Added barepalli@fcps.edu, jchin@fcps.edu, pczicht@fcps.edu to the CN Collision Check failed email.
 - Issue 113: revisit: proxies now all receive email messages along with the manager when expiration dates come closer.

Build 45.7:
 - Resolved minor service driver issue where some jobs wouldn't fire for unassociated accounts.
 
Build 45.8:
 - Resolved minor issue with room number looping where resetting to the correct value in AD stripped the harddreturn between streetAdress and roomNumber.

Build 45.9:
 - Resolved issue where S, SA, Physical Delivery Office Name or Postal Code don't get updated on a non emp to emp match.
 
Build 45.10:
 - Changed name of Driver set GCV: EMAIL_CN_COLLISION_CHECK to FCPS_IDM_TEAM as it is used for more than one purpose.
 - Added the FCPS_IDM_TEAM gcv to the AD deletion email.
 
Build 45.11:
 - Fixed build to produce proper environment based FCPS_IDM_TEAM email addresses.

Build 45.12:
 - Disabled TAC jobs due to request to install TAC in the beginning of 2016.
 - Staff: deleted unused SE-TriggerCalculateTargetMailstore policy.
 - Added step to disable mailboxes for already disabled users on initial deployment.
 - Confirmed new user email does not include ITSD.
 - Confirmed that Password Expiration notice email does not include ITSD but does include AccountNotifications@fcpstrivir.dev.com.
 - Confirmed that AUP reminder email does not include ITSD but does include AccountNotifications@fcpstrivir.dev.com.
 - Confirmed all messages sent by template are from "ITServiceDesk@fcps.edu".
 - Confirmed AUP expiring soon email is sent to user and CC'ed AccountNotifications@fcps.edu 
 - Confirmed AUP expired email is sent to user and CC'ed AccountNotifications@fcps.edu - AUP exired: same as expiringsoon

Release 46.0
 - Service driver: deleted both TAC expiration related jobs.
 - Service driver: deleted TAC releated jobs.
 - Lawson: deleted initial setting of fcpsTACAcceptanceDate.
 - Deleted fcpsTACAcceptanceDate from all schemas.
 - Confirmed no other driver references fcpsTACAcceptanceDate.
 
Release 46.1
 - Staff: Ex1 and ex6 removed on proper state now rather then on a catch all policy.
 - Exchange: Process dispatcher called on unassociated adds right before create rule to deal with modify-add loosing dispatcher operation attributes.
 - Added CloudAD password checker.
 - Reroll:
     - Moved pre-mapping rule first on CloudAD driver.
     - Fixed SD_ACCOUNT email to be production email address in build.

Release 46.2
 - CloudAD driver: added password checker.
 
Release 46.3(MANUAL)
 - CloudAD Deployed latest password sync driver into production on idmprodidm2, and created manual version.
 - CloudAD driver: added password checker.
Release notes: 
See changelog for updates.  Please review and follow these notes to add new items form release 45.:
 - Be sure to install the new fcpsTACAcceptanceDate attribute; it can be installed from IDV-fcpsUserAuxClass-Schema.ldif; it needs to be a SYN_TIME single valued date attribute.
 - Be sure to install the new TAC* email templates.
 - After Deploying the Service driver, configure TAC expiring, and TAC expiring soon jobs to run at midnight.
 - AD-6 email template needs a fix in the body of the message: remove the 'd' from the second use of the variable: "$enableOrDisable$d"
 - Remember to update the latest PowerShell scripts.
 
Generic update notes for most versions:
 - Ensure Schema has been updated (and ndsd restarted on idm server)
 - Ensure latest email templates are deployed.
 - Deploy latest drivers, library first.
 
Release 47.9
 - Updated Work order trace to be "1000000K" (or 1 GB)in all non prod environments.
 - Staff: Added a custom publisher reset to AD's user must change password at next login when cleared in AD for a user that still has an expired password in eDirectory.
 - Staff: cleanup: removed calculate mailstore
 - Resolved "issue 111": if UMCPANL flag is cleared for a user that has an expired password in eDirectory, the value is reset back to being checked..
 - Removed check-password support from ADAM driver entirely.
 - CloudAD: Added fix to remove 9145 error in iManager: missing operation-data/src-dn causing fcpsUser object class update to fail.
 - (not deploying yet; not time sensitive) Lawson: disabled a Sentinel generate-event that was being raised as the result of a security role update.
 - Ldap ADAM driver: yes (check-password santization): check-password sanitizer has been checked in; verifying this made it into TEST/PROD
 - Resolved issue with rename happening at the same time an account is moved.
 - Exchange Online work completed:
    - Initial user email: removed TO address for end user; left mapping table email address, and email. 
        EX2007-PowerShell driver:
        - Passing Exchange server as GCV now (Updated PowerShell driver follows this pattern too.)
             - Removed mailbox disconnection code, and calculating mailstore code.
        - Removed businessCategory setting of guid as we are no longer disconnecting mailboxes.
             - Removed user email address as one of the 'To' addresses for the initial user email for employees.
    Staff driver: 
         - Added All Tombstone processing here: As event had to be able to be triggered by a delete from IDV (other deletes could happen, outside of the 30 day disable, scuh as the 'blown' away, and the occasional eEirectory manual deletion').
         - Added block to avoid processing any changes and echo from the tombstone container in AD.
    Recommendations for deploying:
    - Delete all objects from drivers before deploying:
        - Staff
        - Ex2007-PowerShell

Release 47.10 (Single driver focus release: Service driver fixes)
 - Service driver only release: Fixed tickets 323 and 325:
     - 323	IDM Drivers Looking at Old UserApp Driver (needs base dn for search)
     - 325	IDM Driver Not Excluding Expired Proxies from Attestation Expire Email

Release 48.1
 - CODE BASE: Reverted to release 46.1, plus bugfixes up to 47.10.
    Bugfixes include (but are not limted to:)
    - UMCPANL flag incorrectly cleared in AD writeback. 
    - X400 addresses removed (3543, July 20, 2016.)
 - Removed unnecessary adCollisionCheck ECMA script reference from WorkOrder driver.
 - Fixed ticket 272: local mailboxes aren't getting reconnected.
 - Populating carLicense for disabled accounts now. (for future collision checking, and for rehire email addresses.)
 - Removed AdministrativeHold/Tombstoning process: allowing mailboxes to stay in a disabled state now for 60 days.
 - Added Exchange Online code back, plus the updates as mentioned in ACs.

Release 48.2 - (Milestone: IDMStaff-48.2)
 - Service driver: removed clearing of company per ticket 369 (emp to non emp moves).
 - Added unique constraint on LAWSON.TEST_IDV to PK_EMPLOYEE to avoid duplicate rows (makes fixture and testing of managers much better), and note to the deployment instructions.
 - Tickets resolved:
356 - clearing SAM_ACCOUNT and EMAIL_ADDRESS on emp to non emp conversions; and attempting to keep them cleared: see race condition concerns above.
369 - company is now no longer cleared; however there are two dozen attributes that we clear when changed to a non emp; we should check to see if any other attrs should be preserved.
270 - 2 onmicrosoft addresses: need both.
277 - fn.ln email missing: wasn't populated, and should have been
369 - 'company' attribute needs to be maintained
378 - exclude expired process: attestation email
379 - exclude disabled process: attestation email

Release 48.3
Ticket 387: - Exchange driver fix: avoid disconnecting mailbox on terminate.

Release 48.4
 - Change to library only: 
    Fixes to name generator: 
        Fixed & escaping problem when collision containers have ampersands.
        Moved to indexOf from startsWith, as strings are downgraded to ECMA script strings in unexpected times; this confirms it will work period,
        Added LDAP connection pooling, 
        Changed ' to " in error xml.
 - Updated DriverSet GCVS for production in build.
 - Fix to unique name generation library to allow only lower and upper case letters.

Release 48.5
 - Staff: Added fix to SA/roomnumber: if roomnumber is being deleted to prepare for roomnumber cleanup. Also added check-if-user in two cases.
 - ADLDS driver included; ready to be deployed.

Release 48.6
 - Resolved Tickets 356, 409: SAM: Fixed an issue where SAM_ACCOUNT and EMAIL_ADDRESS were not set/cleared in Lawson due to a race condition bug. (test1_6ExEmployeeTPToNonemployee: added new state to explicitly clear/set values.)
 - Fixed an issue where clearing streetAddress would cause the event to fail if it was being removed. (fix to update made in 48.5). (fix to test2_3AttestConfirmDisabledAccountWithNewMgr: login Disabled wasn't being cleared due to <value/> tag in removal of street address.)
 - Resolved internal ticket (reported by Shridar): Ticket 210: non emp group not removed for a rehired non employee (ends up as employee).
 - Resolved issue with updating email addresses when primary address already exists as a secondary.
 - Added ojdbc7-Oracle12c.jar to build.
 - Updated Driverset GCVS' to have COUNTY_AD_PORT since the library is still using it.

Release 48.7
 - Lawson and state-machine drivers: Resolved additional issue on ticket 356: removing Lawson association in Lawson driver instead of Staff App now (Staff app version: StaffApp 9.18)

Release 48.8
 - Fixed various issues in adldsDriverSSHProcessor.

Release 49.0
 - Exchange driver: added rule to ignore all non Staff driver association changes to help speed up driver.
 - Updated CloudAD driver to sync intruder lockout to IDV and AD.  
 - UPdated CloudAD driver to recieve password expiration time
 *** PROD RELEASE INSTRUCTIONS ***
    -This driver will make a direct connection to Cloud AD server and NOT go through the remote loader.
    -This requires either 
        1. The CA cert from eDir to be imported into the Java Keystore of the IDM Cloud Machine.
    OR
        2. Have the firewall port of 389 open on the IDM Cloud Machine

Release 50.0
 - Fixed ticket 248: Veto user object named "CN=Enterprise,CN=Builtin,DC=fcps,DC=edu"
 - Internal: - Changed ADHost in build to IP address in TEST, as DNS doesn't resolve the DC.
 - Ticket 380: Updated drivers to avoid ever having multiple commas in the 'to' or 'cc' fields: Lawson, Service Driver, Staff.
 - *NEW*: Deleting three ADLDS Landmark objects before attempting to delete the parent:
    CN=OBJ_TYPE,CN=100015,OU=resources,O=lwsnrmdata,O=lwsn,DC=fcps,DC=edu
    CN=ProvSystemIds,CN=100015,OU=resources,O=lwsnrmdata,O=lwsn,DC=fcps,DC=edu
    CN=SystemTypes,CN=100015,OU=resources,O=lwsnrmdata,O=lwsn,DC=fcps,DC=edu
 
Release 50.3
 - Fixed ticket 119: States are now documented in Employee AC, Appendix G. Need FCPS's review before continuing/formatting/etc.
 - Internal: (Fixed change log for 50.00: added fixed ticket for 380.)
 - Fixed ticket 223: Job to delete proxy assignment objects.
 - Internal: ADLDS: removed unnecessary avktest operation property in the driver.
 - Updated the ADLDS driver to support migrating users that are in the disabled OU in eDir. This way the account can be fully cleaned up when it is deleted later.
 
Release (up to) 52
 - Fixed ADLDS processor to properly ignore splash screen and report errors when appropriate.
 - Fixed ignoring error on removing tmp error file, and changed 'Exiting' note to be debug only.
 - Confirmed ADLDS matches into ADLDS should only associate & match/merge main resource user. It doesn't need to attempt to recreate all remaining objects (Susan, 2017-06-16).
 - Ticket 438: first issue resolved: if a second rename happens before the first workorder completes, we now delete it properly.
 - Google Apps driver: limiting adds of Google entries from students/employees/non employee containers.
 - IdmUnit - Fixed test test5_2TPTermDatePast; no longer sending email.
 - IdMUnit - Fixed issue with 'Leaf' object deletion, as shown in tests test5_13TXEmpAcctDelete, and test5_14EmployeeDeletedFromIDV.
 - Staff driver: added fix to resolve issue with non emp not moving with manager when being converted (test1_6ExEmpToNonempWithManager, rows 56, 57).
 - Added workforceID to CN collision email.
 - Fixed issue in ticket 443: if GCV for unique name generator is missing, the proper error is thrown now.
 - Fixed issue in ticket 444: The proper ADLDS 'upn' version of the object is deleted, with a configurable ADLDS_LWSNSSOLOGINID_POSTFIX GCV value on the driver set (Note: this value is also used for adds and renames to properly reference this upn object).
 - Internal: removed jclient.jar from class path; as it is specific to the DXCMD connector.
 - Checked in the IDM 4.7 drivers.
 - Updated the Service Driver "Retry Google Password Sync" job to now set "syncpwd-removeassoc" when a user has an fcpsPasswordSyncStatusGoogle=".*Resource No Found.*" (This error is indicative of a broken association value).
 - See the GoogleApps readme for the associated updates on that driver.
 - Updated the IdMUnit termination tests for better performance and better deterministic testing. 
Release 53
 - Lawson driver update to fix the SYNTAX ERROR on bad siteLocations.
 - Lawson driver update to veto and send a notification if the last name starts with a space.
 - New GCV for the space in the name notification:
GCV Name: FAILED_LAWSON_ADD
GCV Description: Comma delimited failed Lawson add/mod email List (Space in last name notification)
 - Email template name: LW-7-LawsonSpaceBeforeLastName
 - Login Disabled modifications from AD now get reset.
 - Password changes in Computers and Students OUs on staff accounts will now sync back into eDir.
 - Removed old disabled code.
