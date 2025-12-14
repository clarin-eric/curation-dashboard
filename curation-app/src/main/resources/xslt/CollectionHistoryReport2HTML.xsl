<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">
    <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="/collectionHistoryReport">
        <h1>Metadata Provider Report History</h1>
        <h2>List of Metadata Providers</h2>
        <ul>
            <xsl:for-each select="collection">
                <li>
                    <a href="#{generate-id(.)}">
                        <xsl:value-of select="replace(@name, '_', ' ')" />
                    </a>
                </li>
            </xsl:for-each>
        </ul>
        <xsl:for-each select="collection">
            <article class="shadow p-3 mb-5 bg-body-tertiary rounded border border-2">
            <h2 class="anchor" id="{generate-id(.)}">
                <xsl:value-of select="replace(@name, '_', ' ')" />
            </h2>
            <xsl:for-each select="report">
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>/metadataprovider/</xsl:text><xsl:value-of select="@fileName" />
                    </xsl:attribute>
                    <xsl:value-of select="@isoDate" />
                </a>
                <xsl:text> </xsl:text>
            </xsl:for-each>
            </article>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>