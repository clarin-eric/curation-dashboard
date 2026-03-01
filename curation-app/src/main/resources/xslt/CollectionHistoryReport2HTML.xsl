<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">
    <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes" />
    <xsl:template match="/collectionHistoryReport">
        <h1>Metadata Provider Report History</h1>
        <h2>List of Metadata Providers</h2>
        <ul>
            <xsl:for-each select="collection">
                <li>
                    <a href="#{generate-id()}">
                        <xsl:value-of select="replace(@name, '_', ' ')" />
                    </a>
                </li>
            </xsl:for-each>
        </ul>
        <xsl:for-each select="collection">
            <article class="shadow p-3 mb-5 bg-body-tertiary rounded border border-2">
            <h2 class="anchor" id="{generate-id()}">
                <xsl:value-of select="replace(@name, '_', ' ')" />
            </h2>
                <xsl:for-each-group select="report" group-by="substring(@isoDate, 1, 7)">
                    <xsl:choose>
                        <xsl:when test="current-grouping-key()=format-date(current-date(), '[Y,4]-[M,2]')">
                            <ul>
                                <li>
                                    <a href="#{generate-id()}" data-bs-toggle="collapse" aria-expanded="true" aria-controls="{generate-id()}"><xsl:value-of select="current-grouping-key()" /> (<xsl:value-of select="count(current-group())" />)</a>
                                    <ul class="collapse show" id="{generate-id()}">
                                    <xsl:for-each select="current-group()">
                                        <li>
                                            <a>
                                                <xsl:attribute name="href">
                                                    <xsl:text>/metadataprovider/</xsl:text><xsl:value-of select="@fileName" />
                                                </xsl:attribute>
                                                <xsl:value-of select="@isoDate" />
                                            </a>
                                        </li>
                                    </xsl:for-each>
                                    </ul>
                                </li>
                            </ul>
                        </xsl:when>
                        <xsl:otherwise>
                            <ul>
                                <li>
                                    <a href="#{generate-id()}" data-bs-toggle="collapse" aria-expanded="false" aria-controls="{generate-id()}"><xsl:value-of select="current-grouping-key()" /> (<xsl:value-of select="count(current-group())" />)</a>
                                    <ul class="collapse" id="{generate-id()}">
                                        <xsl:for-each select="current-group()">
                                            <li>
                                                <a>
                                                    <xsl:attribute name="href">
                                                        <xsl:text>/metadataprovider/</xsl:text><xsl:value-of select="@fileName" />
                                                    </xsl:attribute>
                                                    <xsl:value-of select="@isoDate" />
                                                </a>
                                            </li>
                                        </xsl:for-each>
                                    </ul>
                                </li>
                            </ul>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each-group>
            </article>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>