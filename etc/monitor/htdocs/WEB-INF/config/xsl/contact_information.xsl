<?xml version="1.0" encoding="iso-8859-1" ?>

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0' xmlns:ci='http://www.ecmwf.int/schema/cos/contact_information/1.0' xmlns:datetime="http://exslt.org/dates-and-times" extension-element-prefixes="datetime"> 

<xsl:output method="html"/>


<xsl:template match="ci:contactInformation"> 
<table class="fieldsDetail" border="0">

  <xsl:apply-templates select="ci:info"/>


<tr><td colspan="5"> </td></tr>

  <xsl:apply-templates select="ci:comments"/>

<xsl:if test="ci:computingRepresentative">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Computing Representative</th></tr>
  <tr><td colspan="5">
<xsl:choose>
<xsl:when test="starts-with(ci:computingRepresentative,'http:')">
	<a href="{ci:computingRepresentative}"><xsl:value-of select="ci:computingRepresentative"/></a>
</xsl:when>
<xsl:otherwise>
	<xsl:value-of select="ci:computingRepresentative"/>
</xsl:otherwise>
</xsl:choose>
</td></tr>

</xsl:if>

<xsl:if test="ci:mainOperationalContact">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Main Operational Contact</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:mainOperationalContact"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:computerOperations">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Computer Operations</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:computerOperations"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:telecomOperators">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Telecom Operators</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:telecomOperators"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:meteorologists">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Meteorologists</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:meteorologists"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:ecpdsContact">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">OpenECPDS Contact</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:ecpdsContact"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:switchboard">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Switch Board</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:switchboard"/></xsl:call-template>

</xsl:if>


<xsl:if test="ci:mailGroup">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Mail Group</th></tr>
  <tr><th class="header2">Name</th><th colspan="5" class="header2">Email</th></tr>
 <tr><td><xsl:value-of select="ci:mailGroup/ci:name"/></td><td><a href="mailto:?bcc=operators@ecmwf.int&amp;bcc={ci:mailGroup/ci:email}"><xsl:value-of select="ci:mailGroup/ci:email"/></a></td></tr>

</xsl:if>


<xsl:if test="ci:other">

<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Other</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="ci:other"/></xsl:call-template>

</xsl:if>

<xsl:if test="ci:documentation">

<tr><td colspan="5"> </td></tr>

<xsl:apply-templates select="ci:documentation"/>

</xsl:if>

<xsl:if test="ci:dataServices">

<tr><td colspan="5"> </td></tr>
<tr><td colspan="5"> </td></tr>

  <tr><th colspan="5" class="header">Data Services</th></tr>
  <xsl:call-template name="blockDataServices"><xsl:with-param name="element" select="ci:dataServices"/></xsl:call-template>

</xsl:if>
  

</table>
</xsl:template> 


<xsl:template match="ci:info">
<tr><th>Organisation Web Page</th><td colspan="5"><a href="{./ci:organisationWebPage}"><xsl:value-of select="./ci:organisationWebPage"/></a></td></tr>
<tr><th>Sad Number</th><td colspan="5"><xsl:value-of select="./ci:SADNumber"/></td></tr>
</xsl:template>

<xsl:template match="ci:documentation">
<tr><th colspan="5" class="header">Documentations</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ci:comments">
<tr><th colspan="5" class="header">Comments</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ci:documentation/*">
<tr><th><xsl:value-of select="name(.)"/></th>
<td colspan="5">

<xsl:choose>
<xsl:when test="starts-with(.,'http:')">
	<a href="{.}"><xsl:value-of select="."/></a>
</xsl:when>
<xsl:otherwise>
	<xsl:value-of select="."/>
</xsl:otherwise>
</xsl:choose>

</td>
</tr>
</xsl:template>

<xsl:template match="ci:comments/*">
<tr><th><xsl:value-of select="name(.)"/></th><td colspan="3"><xsl:value-of select="."/></td></tr>
</xsl:template>



<xsl:template name="block">
<xsl:param name="elements"/>

<xsl:if test="$elements[1]">
  
<tr>
  <xsl:if test="$elements[1]/@type">
    <th class="header2">Type</th>
  </xsl:if>
<th class="header2">Name</th><th class="header2">Telephone</th><th class="header2">Fax</th><th class="header2">eMail</th>
  <xsl:if test="$elements[1]/ci:url">
    <th class="header2">URL</th>
  </xsl:if>
</tr>
</xsl:if>

<xsl:for-each select="$elements">
<tr>
  <xsl:if test="@type">
    <td><xsl:value-of select="@type"/></td>
  </xsl:if>
  <td><xsl:value-of select="./ci:name"/></td>
  <td><xsl:value-of select="./ci:telephoneNumber"/></td>
  <td><xsl:value-of select="./ci:faxNumber"/></td>
  <td><a href="mailto:{./ci:email}?bcc=operators@ecmwf.int"><xsl:value-of select="./ci:email"/></a></td>
  <xsl:if test="./ci:url">
    <td><a href="{./ci:url}"><xsl:value-of select="./ci:url"/></a></td>
  </xsl:if>
</tr>
</xsl:for-each>
<tr><td colspan="5"> </td></tr>
</xsl:template> 


<xsl:template name="blockDataServices">
<xsl:param name="element"/>

 <tr><th colspan="5" class="header">Contract ID</th></tr>
 <tr><td><xsl:value-of select="$element/ci:contractId"/></td></tr>

<xsl:if test="$element/ci:ecmwfContact">
 <tr><th colspan="5" class="header">ECMWF Contact</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="$element/ci:ecmwfContact"/></xsl:call-template>
</xsl:if>

<xsl:if test="$element/ci:meteorologicalContact">
  <tr><th colspan="5" class="header">Meteorological Contact</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="$element/ci:meteorologicalContact"/></xsl:call-template>
</xsl:if>

<xsl:if test="$element/ci:technicalContact">
  <tr><th colspan="5" class="header">Technical Contact</th></tr>
  <xsl:call-template name="block"><xsl:with-param name="elements" select="$element/ci:technicalContact"/></xsl:call-template>
</xsl:if>

</xsl:template> 


</xsl:stylesheet> 
