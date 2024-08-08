<?xml version="1.0" encoding="iso-8859-1" ?>

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0' xmlns:ca='http://www.ecmwf.int/schema/cos/contact_acquisition/1.0' xmlns:datetime="http://exslt.org/dates-and-times" extension-element-prefixes="datetime"> 

<xsl:output method="html"/>

<xsl:template match="ca:dataSourceInformation"> 

<table class="fieldsDetail" border="0">
  <xsl:apply-templates select="ca:DataSourceInformations" />
  <xsl:apply-templates select="ca:ContactInformations"/>
  <xsl:apply-templates select="ca:DataSourceAccessInformations"/>
  <xsl:apply-templates select="ca:DataTypeInformations"/>
  <xsl:apply-templates select="ca:OpsProcedure"/>
  <xsl:apply-templates select="ca:AnalystProcedure"/>
  <xsl:apply-templates select="ca:Documentation"/>
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Other</th></tr>
  <xsl:apply-templates select="ca:metappsSystemChange"/>
  <xsl:apply-templates select="ca:comments"/>
</table>
</xsl:template> 

<xsl:template match="ca:DataSourceInformations">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Data Source</th></tr>
<xsl:apply-templates />
</xsl:template>

<xsl:template match="ca:ContactInformations">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Contact Information</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ca:DataSourceAccessInformations">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Data Source Access Informations</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ca:DataTypeInformations">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Data Type Informations</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ca:OpsProcedure">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Ops Procedure</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ca:Documentation">
<tr><td colspan="2"> </td></tr>
<tr><th colspan="2" class="header">Documentation</th></tr>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*">
<tr>
<th width="250px">
<xsl:choose>
<xsl:when test="name(.)='AgencyOrOrganizationOfOriginWebPage'">Agency Or Organization Of Origin Web Page</xsl:when>
<xsl:when test="name(.)='AgencyOrOrganizationOfOrigin'">Agency Or Organization Of Origin</xsl:when>
<xsl:when test="name(.)='CentreOfOrigin'">Centre Of Origin</xsl:when>
<xsl:when test="name(.)='PhoneNumber'">Phone Number</xsl:when>
<xsl:when test="name(.)='SADNumber'">SAD Number</xsl:when>
<xsl:when test="name(.)='telephoneNumber'">Telephone Number</xsl:when>
<xsl:when test="name(.)='faxNumber'">Fax Number</xsl:when>
<xsl:when test="name(.)='RemoteServer'">Remote Server</xsl:when>
<xsl:when test="name(.)='UserId'">User Id</xsl:when>
<xsl:when test="name(.)='FilePattern'">File Pattern</xsl:when>
<xsl:when test="name(.)='RemotePath'">Remote Path</xsl:when>
<xsl:when test="name(.)='DataDescription'">Data Description</xsl:when>
<xsl:when test="name(.)='TypeOfObservation'">Type Of Observation</xsl:when>
<xsl:when test="name(.)='ImportanceOfDataTypeForAssimilation'">Importance Of Data Type For Assimilation</xsl:when>
<xsl:when test="name(.)='InstrumentChannels'">Instrument Channels</xsl:when>
<xsl:when test="name(.)='DataFormat'">Data Format</xsl:when>
<xsl:when test="name(.)='WarningInfo'">Warning Info</xsl:when>
<xsl:when test="name(.)='ECFSPath'">ECFS Path</xsl:when>
<xsl:when test="name(.)='OnLineBackup'">Online Backup</xsl:when>
<xsl:when test="name(.)='OpsProcedureWebPage'">Ops Procedure Webpage</xsl:when>
<xsl:when test="name(.)='ShiftProcedure'">Shift Procedure</xsl:when>
<xsl:when test="name(.)='AnalystProcedure'">Analyst Procedure</xsl:when>
<xsl:when test="name(.)='TechDoc'">Tech Doc</xsl:when>
<xsl:when test="name(.)='metappsSystemChange'">METAPPS Systems Change</xsl:when>
<xsl:when test="name(.)='opsProcedure'">Ops Procedure</xsl:when>
<xsl:when test="name(.)='rmdcnTechnicalPage'">RMDCN Technical Page</xsl:when>
<xsl:when test="name(.)='comments'">Comments</xsl:when>
<xsl:when test="name(.)='name'">Name</xsl:when>
<xsl:when test="name(.)='email'">Email</xsl:when>
<xsl:otherwise> <xsl:value-of select="name(.)"/> </xsl:otherwise>
</xsl:choose>
</th>
<td>
<xsl:choose>
<xsl:when test="starts-with(.,'http:')"> <a href="{.}"><xsl:value-of select="."/></a> </xsl:when>
<xsl:when test="starts-with(.,'https:')"> <a href="{.}"><xsl:value-of select="."/></a> </xsl:when>
<xsl:when test="contains(name(.),'email')"> <a href="mailto:{.}?bcc=operators@ecmwf.int"><xsl:value-of select="."/></a> </xsl:when>
<xsl:otherwise> <xsl:value-of select="."/> </xsl:otherwise>
</xsl:choose>
</td>

</tr>
</xsl:template>

</xsl:stylesheet> 
