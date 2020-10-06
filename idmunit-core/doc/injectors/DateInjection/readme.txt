DateInjection Injector
---------------
The DateInjection Injector provides a way to easily create references of time
in your IdMUnit tests by give them a key or handle inwhich when the test are
run will be replace with the proper time as calculated by the Injector.

See the Special Considerations section below for additional notes.

OPERATIONS
----------


SPECIAL CONSIDERATIONS
----------------------
Keep the following in mind when using the DateInjection:
- The mutator takes an integer number and will change today's date to reflect that. The Number may also be a negative integer, and today's date is created by your system time.

CONFIGURATION
-------------
To configure this connector you need to specify a type, key, and format.

<data-injections>
	<data-injection>
		<type>org.idmunit.injector.DateInjection</type>
		<key>%TODAY%</key>
		<format>yyyyMMdd</format>
	</data-injection>
	<data-injection>
		<type>org.idmunit.injector.DateInjection</type>
		<key>%TODAY+30%</key>
		<format>yyyyMMdd</format>
		<mutator>30</mutator>
	</data-injection>
</data-injections>


* type: The class path to the injection class you will be using
* key: The variable name, or handle that will be replaced throughout your test
   with data-injection, in this case a date
* format: Specified format for the date to appear. More information can be
   found here: http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
* mutator : the integer value that will alter the current date in the units of
   days.

The following parameters are optional:
* mutator: The numerical value in which the date can be incremented or
   decremented, so 30 will increment Todays date with 30 days.
