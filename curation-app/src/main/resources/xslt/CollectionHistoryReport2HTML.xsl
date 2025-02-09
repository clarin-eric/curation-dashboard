<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:template match="/collectionHistoryReport">
        <h1>Report History</h1>
        <xsl:for-each select="collection">
            <p>
            <h2><xsl:value-of select="@name" /> </h2>

            <xsl:for-each select="report">
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>/collection/</xsl:text><xsl:value-of select="@fileName" />
                    </xsl:attribute>
                    <xsl:value-of select="@isoDate" />
                </a>
                <xsl:text> </xsl:text>
            </xsl:for-each>
            </p>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>