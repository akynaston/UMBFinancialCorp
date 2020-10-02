<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes" method="xml" standalone="yes"/>

    <!-- Strip out lib/test items, we'll add then back below.
    <xsl:template match="classpathentry[starts-with(@path,'lib/test')]"/>
-->

<!-- Ensure the xmlfile connector.jar file class path  is updated to point to the right lib directory-->
    <xsl:template match="classpathentry[contains(@path, 'xmlfile_conn')]">
        <classpathentry path="idmunit-external/lib/xmlfile_conn.jar">
            <xsl:apply-templates select="@*[name() != 'path']"/>
        </classpathentry>
    </xsl:template>

    <!-- All of our files form lib/test need to be pefixed with idmunit-external/: -->
    <xsl:template match="classpathentry[starts-with(@path, 'lib/')]">
        <classpathentry path="{concat('idmunit-external/', @path)}">
            <xsl:apply-templates select="@*[name() != 'path']"/>
        </classpathentry>
    </xsl:template>
    
 <!-- We don't need the XMLSplitter source code in the release: -->
 <xsl:template match="classpathentry[@path='src']">
    <xsl:message>****** NOTE: BLOCKING SRC DIRECTORY: <xsl:value-of select="@path"/></xsl:message>    
  </xsl:template>

  <!-- We don't need the DelimitedTextUtil.jar in the release: -->
  <xsl:template match="classpathentry[starts-with(@path, 'lib/build')]">
    <xsl:message>****** NOTE: BLOCKING lib/build entry: <xsl:value-of select="@path"/></xsl:message>
  </xsl:template>

	<!-- Don't need to do anything specific here for now; left in place for future . . -->
    <xsl:template match="@*|node()">
      <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
  </xsl:template>

</xsl:transform>