<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com">
    <xsl:function name="functx:capitalize-first" as="xs:string?"
                  xmlns:functx="http://www.functx.com">
        <xsl:param name="arg" as="xs:string?"/>

        <xsl:sequence select="
   concat(upper-case(substring($arg,1,1)),
             substring($arg,2))
 "/>

    </xsl:function>

    <xsl:template match="/collections-report">
        <html>
            <body>
                <table data-toggle="table" class="text-nowrap tableFixHead fullscreenTable table table-responsive table table-striped table-bordered table-sm">

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
                                    <xsl:value-of select="functx:capitalize-first(@name)"></xsl:value-of>
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
                                            <xsl:text>/collection/</xsl:text>
                                            <xsl:value-of select="translate(reportName,'.:','__')"></xsl:value-of>
                                            <xsl:text>.html</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="@name"></xsl:value-of>
                                    </a>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(scorePercentage,'##.##%')"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="numOfFiles"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="numOfProfiles"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="numOfUniqueLinks"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="numOfCheckedLinks"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(ratioOfValidLinks,'##.##%')"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="numOfResProxies"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(avgNumOfResProxies,'##.#')"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(ratioOfValidRecords,'##.##%')"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of
                                            select="format-number(avgNumOfEmptyXMLElements,'##.#')"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(avgFacetCoverage,'##.##%')"></xsl:value-of>
                                </td>

                                <xsl:for-each select="facets/facet">
                                    <td>
                                        <xsl:value-of select="format-number(.,'##.##%')"></xsl:value-of>
                                    </td>
                                </xsl:for-each>
                            </tr>
                        </xsl:for-each>
                    </tbody>


                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>