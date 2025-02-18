<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com"
                exclude-result-prefixes="#all">
    <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:function name="functx:capitalize-first" as="xs:string?">
        <xsl:param name="arg" as="xs:string?"/>
        <xsl:sequence select="concat(upper-case(substring($arg,1,1)), substring($arg,2))"/>
    </xsl:function>
    <xsl:decimal-format NaN="N/A"/>
    <xsl:template match="/allCollectionReport">
        <div class="infoLine">
            <div class="floatLeft">
                created at
                <xsl:value-of select="./@creationTime"/>
                <xsl:text> </xsl:text>
                (<a href="/metadataprovider/CollectionHistoryReport.html">report history</a>)
            </div>
            <div class="floatRight">
                download as
                <a href="/download/metadataprovider/AllCollectionReport">xml</a>
                <xsl:text>
                       </xsl:text>
                <a href="/download/metadataprovider/AllCollectionReport?format=json">json</a>
                <xsl:text> </xsl:text>
                <a href="/download/metadataprovider/AllCollectionReport?format=tsv">tsv</a>
            </div>
            <div class="clear" />
        </div>
        <table id="collections" class="display text-nowrap" width="100%">
            <thead>
                <tr>
                    <th data-sortable="true">Name</th>
                    <th data-sortable="true">Score</th>
                    <th data-sortable="true">Num Of Records</th>
                    <th data-sortable="true">Num Of Profiles</th>
                    <th data-sortable="true">Num Of Unique Links</th>
                    <th data-sortable="true">No Of Checked Links</th>
                    <th data-sortable="true">Ratio Of Valid Links</th>
                    <th data-sortable="true">Num Of Res Proxies</th>
                    <th data-sortable="true">Avg Num Of Res Proxies</th>
                    <th data-sortable="true">Ratio Of Valid Records</th>
                    <th data-sortable="true">Avg Num Of Empty XML Elements</th>
                    <th data-sortable="true">Avg Facet Coverage</th>

                    <xsl:for-each select="./collection[1]/facets/facet">
                        <th data-sortable="true">
                            <xsl:value-of select="functx:capitalize-first(@name)"/>
                        </th>
                    </xsl:for-each>
                </tr>
            </thead>

            <tbody>
                <xsl:for-each select="collection">
                    <tr>
                        <td>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:text>/metadataprovider/</xsl:text>
                                    <xsl:value-of select="translate(reportName,'.:','__')"/>
                                    <xsl:text>.html</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="@name"/>
                            </a>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(scorePercentage,'0.00%')"/>
                        </td>
                        <td>
                            <xsl:value-of select="numOfFiles"/>
                        </td>
                        <td>
                            <xsl:value-of select="numOfProfiles"/>
                        </td>
                        <td>
                            <xsl:value-of select="numOfUniqueLinks"/>
                        </td>
                        <td>
                            <xsl:value-of select="numOfCheckedLinks"/>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(ratioOfValidLinks,'0.00%')"/>
                        </td>
                        <td>
                            <xsl:value-of select="numOfResources"/>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(avgNumOfResources,'0.0')"/>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(ratioOfValidRecords,'0.00%')"/>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(avgNumOfEmptyXMLElements,'0.0')"/>
                        </td>
                        <td>
                            <xsl:value-of select="format-number(avgFacetCoverage,'0.0%')"/>
                        </td>

                        <xsl:for-each select="facets/facet">
                            <td>
                                <xsl:value-of select="format-number(./@avgCoverage,'0.00%')"/>
                            </td>
                        </xsl:for-each>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
</xsl:stylesheet>