﻿<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">
    <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:decimal-format NaN="N/A"/>
    <xsl:template match="/allLinkcheckerReport">
        <div class="infoLine">
            <div class="floatLeft">
                created at
                <xsl:value-of select="./@creationTime"/>
            </div>
            <div class="floatLeft">
                <a href="/linkchecker/latestChecks">latest checks</a>
            </div>
            <div class="floatRight">
                download as
                <a href="/download/linkchecker/AllLinkcheckerReport">xml</a>
                <xsl:text> </xsl:text>
                <a href="/download/linkchecker/AllLinkcheckerReport?format=json">json</a>
            </div>
            <div class="clear"/>
        </div>
        <h3>Overall</h3>
        <table class="reportTable">
            <thead>
                <tr>
                    <th>Category</th>
                    <th>Count</th>
                    <th>Average Response Duration(ms)</th>
                    <th>Max Response Duration(ms)</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="overall/statistics">
                    <xsl:variable name="category">
                        <xsl:value-of select="./@category"/>
                    </xsl:variable>
                    <tr class="{$category}">

                        <td>
                            <a href="/linkchecker/Overall#{$category}">
                                <xsl:value-of select="@category"/>
                            </a>
                        </td>
                        <td class='text-right'>
                            <xsl:value-of select="format-number(@count, '###,##0')"/>
                        </td>
                        <td class='text-right'>
                            <xsl:value-of select="format-number(@avgRespTime, '###,##0.0')"/>
                        </td>
                        <td class='text-right'>
                            <xsl:value-of select="format-number(@maxRespTime, '###,##0.0')"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="5">
                        Total number with response:
                        <xsl:value-of select="format-number(overall/@totNumOfLinksWithDuration, '###,##0')"/>
                        &#0183;
                        Average Response Duration(ms):
                        <xsl:value-of select="format-number(overall/@avgRespTime,'###,##0.0')"/>
                        &#0183;
                        Max Response Duration(ms):
                        <xsl:value-of select="format-number(overall/@maxRespTime,'###,##0.0')"/>
                    </td>
                </tr>
            </tfoot>
        </table>
        <br />
        <img src="/img/linkchecker/Overall.png" alt="timeline of link checking results" width="800" higth="200" />
        <br/>
        <h3>Metadata Providers:</h3>

        <xsl:for-each select="collection">

            <xsl:variable name="name" select="@name"/>

            <h4>
                <xsl:value-of select="replace($name,'_',' ')"/>
            </h4>
            <table class="reportTable">
                <thead>
                    <tr>

                        <th>Category</th>
                        <th>Count</th>
                        <th>Average Response Duration(ms)</th>
                        <th>Max Response Duration(ms)</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="statistics">
                        <xsl:variable name="category">
                            <xsl:value-of select="./@category"/>
                        </xsl:variable>
                        <tr class="{$category}">
                            <td>
                                <a href="/linkchecker/{$name}#{$category}">
                                    <xsl:value-of select="@category"/>
                                </a>
                            </td>
                            <td class='text-right'>
                                <xsl:value-of select="format-number(@count, '###,##0')"/>
                            </td>
                            <td class='text-right'>
                                <xsl:value-of select="format-number(@avgRespTime, '###,##0.0')"/>
                            </td>
                            <td class='text-right'>
                                <xsl:value-of select="format-number(@maxRespTime, '###,##0.0')"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="5">
                            Total Number With Response:
                            <xsl:value-of select="format-number(@totNumOfLinksWithDuration, '###,##0')"/>
                            &#0183;
                            Average Response Duration(ms):
                            <xsl:value-of select="format-number(@avgRespTime,'###,##0.0')"/>
                            &#0183;
                            Max Response Duration(ms):
                            <xsl:value-of select="format-number(@maxRespTime,'###,##0.0')"/>
                        </td>
                    </tr>
                </tfoot>
            </table>
            <br/>
            <img alt="timeline of link checking results" width="800" higth="200">
                <xsl:attribute name="src">
                    <xsl:text>/img/linkchecker/</xsl:text>
                    <xsl:value-of select="$name" />
                    <xsl:text>.png</xsl:text>
                </xsl:attribute>
            </img>
            <br />
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
