<xsl:stylesheet version='1.0'
 xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="html" encoding=""/>
<xsl:template match="/">
<html>
<head>
<title>Packages</title>
<style type="text/css">
  p.method {text-indent: 1em}
  div.method {text-indent: 1em}
</style>
</head>
<body>
    <xsl:apply-templates/>

</body>
</html>
</xsl:template> 


<xsl:template match="class">
<hr></hr>
<a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute></a>
<a href="classindex.html">Package Index</a>
<h2>Package <xsl:value-of select="@name"/></h2>
    <xsl:value-of disable-output-escaping="yes" select="info/."/>
<hr></hr><h3>Methods</h3>
<dl>
    <xsl:apply-templates/>
</dl>
</xsl:template>


<xsl:template match="method">
<dd><p></p><b>
<xsl:value-of select="@name"/>(
    <xsl:for-each select="param">
    <xsl:value-of select="@name"/>
         <xsl:if test="not(position()=last())">, </xsl:if>
    </xsl:for-each>
)</b><br></br>
    <dl><dd>
    <xsl:value-of  disable-output-escaping="yes" select="info/."/>
    </dd></dl>

<dl><dd>
<xsl:for-each select="param">
    <xsl:if test="position() =1">
        <p></p><b>Parameters:</b><br></br>
    </xsl:if>
    <xsl:value-of select="@name"/> - <xsl:value-of select="."/><br></br>
</xsl:for-each>
</dd>
</dl>

<dl><dd>
<xsl:for-each select="return">
    <xsl:if test="position() =1">
        <p></p><b>Returns:</b><br></br>
    </xsl:if>
    <xsl:value-of select="."/><br></br>
</xsl:for-each>
</dd>
</dl>

</dd>
</xsl:template>

<xsl:template match="text()"/>

</xsl:stylesheet>

