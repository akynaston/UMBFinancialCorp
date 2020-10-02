<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes" method="xml" standalone="yes"/>

    <xsl:param name="live-profile"/>
    <xsl:template match="idmunit/@live-profile">	
		<xsl:attribute name="live-profile">
			<xsl:value-of select="$live-profile"/>
		</xsl:attribute>
    </xsl:template>
    
    <xsl:template match="@*|node()">
      <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
  </xsl:template>

</xsl:transform>