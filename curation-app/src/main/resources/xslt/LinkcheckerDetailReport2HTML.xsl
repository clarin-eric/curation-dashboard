<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:decimal-format NaN="N/A"/>
    <xsl:template match="/linkcheckerDetailReport">
        <div class="infoLine">
            <div class="floatLeft">
                created at
                <xsl:value-of select="./@creationTime"/>
            </div>
            <div class="clear"/>
        </div>
        <h1>Linkchecker Detail Report</h1>
        <h2>
            Collection name:
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/collection</xsl:text>
                    <xsl:if test="@provider!='Overall'">
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="@provider"/>
                        <xsl:text>.html</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:value-of select="replace(@provider,'_',' ')"/>
            </a>
        </h2>
        <xsl:apply-templates select="categoryReport"/>
    </xsl:template>
    <xsl:template match="categoryReport">
        <h3 class="anchor">
            <xsl:attribute name="id">
                <xsl:value-of
                        select="./@category"/>
            </xsl:attribute>
            Category:
            <xsl:value-of select="./@category"/>
        </h3>
        <div>
            Download full category
            <xsl:value-of select="./@category"/> list as
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of
                            select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                </xsl:attribute>
                <xsl:text>xml</xsl:text>
            </a>
            <xsl:text> </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                    <xsl:text>?format=json</xsl:text>
                </xsl:attribute>
                <xsl:text>json</xsl:text>
            </a>
            <xsl:text> </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                    <xsl:text>?format=tsv</xsl:text>
                </xsl:attribute>
                <xsl:text>tsv</xsl:text>
            </a>
            <xsl:text> (zipped </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of
                            select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                    <xsl:text>?zipped=true</xsl:text>
                </xsl:attribute>
                <xsl:text>xml</xsl:text>
            </a>
            <xsl:text> </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                    <xsl:text>?format=json&amp;zipped=true</xsl:text>
                </xsl:attribute>
                <xsl:text>json</xsl:text>
            </a>
            <xsl:text> </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>/download/linkchecker/</xsl:text>
                    <xsl:value-of select="/linkcheckerDetailReport/@provider"/>
                    <xsl:text>/</xsl:text>
                    <xsl:value-of select="@category"/>
                    <xsl:text>?format=tsv&amp;zipped=true</xsl:text>
                </xsl:attribute>
                <xsl:text>tsv</xsl:text>
            </a>
            <xsl:text>)</xsl:text>
        </div>
        <table class="reportTable">
            <thead>
                <tr>
                    <th>Url</th>
                    <th>Status</th>
                    <th>Info</th>
                </tr>
            </thead>

            <xsl:for-each select="status">
                <xsl:if test="not(position() > 30)">
                    <xsl:call-template name="status"/>
                </xsl:if>
            </xsl:for-each>
            <xsl:if
                    test="count(status) > 30">
                <tr>
                    <td colspan="3">[...] complete list in downloadable report</td>
                </tr>
            </xsl:if>
        </table>

    </xsl:template>
    <xsl:template name="status">
        <tr>
            <td>
                <xsl:attribute name="class">
                    <xsl:value-of select="../@name"/>
                </xsl:attribute>


                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="./@url"/>
                    </xsl:attribute>
                    <xsl:value-of select="./@url"/>
                </a>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="./@statusCode  &gt; 0">
                        <xsl:value-of select="./@statusCode"/>
                    </xsl:when>
                    <xsl:otherwise>
                        N/A
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td>
                <button class="showUrlInfo btn btn-info">Show</button>
            </td>
        </tr>
        <tr hidden="hidden">
            <td colspan="3">
                <b>Message:</b>
                <xsl:value-of select="./@message"/>
                <br/>
                <b>Content length:</b>
                <xsl:value-of select="./@contentLength"/>
                <br/>
                <b>Content type:</b>
                <xsl:value-of select="./@contentType"/>
                <br/>
                <b>Request duration(ms):</b>
                <xsl:value-of select="./@duration"/>
                <br/>
                <b>Method:</b>
                <xsl:value-of select="./@method"/>
                <br/>
                <b>Checking date:</b>
                <xsl:value-of select="./@checkingDate"/>
                <br/>
                context(s):
                <br/>
                <xsl:apply-templates select="*"/>
            </td>
        </tr>

    </xsl:template>
    <xsl:template match="context">
        <b>Origin:</b>
        <a>
            <xsl:attribute name="href">/record/<xsl:value-of
                    select="./@origin"/>
            </xsl:attribute>
            <xsl:value-of select="./@origin"/>
        </a>
        <br/>
        <b>Expected mime-type:</b>
        <xsl:value-of select="./@expectedMimeType"/>
        <br/>
    </xsl:template>
</xsl:stylesheet>