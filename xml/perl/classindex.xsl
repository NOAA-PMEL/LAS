<xsl:stylesheet version='1.0'
 xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="html" encoding=""/>
<xsl:template match="/">
<html><head><title>Package Index</title></head>
<body>
<h1>Package Index</h1>
<ul>
    <xsl:apply-templates/>
</ul>
</body>
</html>
</xsl:template> 

<xsl:template match="class">
<li><a>
    <xsl:attribute name="href"><xsl:text>classes.html#</xsl:text><xsl:value-of select="@name"/>
    </xsl:attribute>
<xsl:value-of select="@name"/>
</a></li>
</xsl:template>

<xsl:template match="file_name">
 <h3><xsl:value-of select="@name"/></h3>
</xsl:template>

<xsl:template match="text()"/>

</xsl:stylesheet>

