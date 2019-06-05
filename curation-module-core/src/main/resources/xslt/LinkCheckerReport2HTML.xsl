<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"></xsl:output>


    <xsl:template match="/linkchecker-report">
        <html>
            <head></head>
            <body>
                <h3>Overall</h3>
                <table class="reportTable">
                    <thead>
                        <tr>
                            <th>Status</th>
                            <th>Category</th>
                            <th>Count</th>
                            <th>Average Response Duration(ms)</th>
                            <th>Max Response Duration(ms)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="overall/statistics">
                            <xsl:variable name="status" select="@statusCode"/>
                            <tr>
                                <xsl:attribute name="style">
                                    <xsl:choose>
                                        <xsl:when test="@category='Broken'">
                                            <xsl:text>background-color:#f2a6a6</xsl:text>
                                        </xsl:when>
                                        <xsl:when test="@category='Undetermined'">
                                            <xsl:text>background-color:#fff7b3</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text>background-color:#cbe7cc</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                </xsl:attribute>

                                <td class='text-right'>
                                    <a href="/statistics/Overall/{$status}">
                                        <xsl:value-of select="@statusCode"></xsl:value-of>
                                    </a>
                                </td>



                                <td>
                                    <xsl:value-of select="@category"></xsl:value-of>
                                </td>
                                <td class='text-right'>
                                    <xsl:value-of select="@count"></xsl:value-of>
                                </td>
                                <td class='text-right'>
                                    <xsl:value-of select="format-number(@avgRespTime, '##.#')"></xsl:value-of>
                                </td>
                                <td class='text-right'>
                                    <xsl:value-of select="format-number(@maxRespTime, '##.#')"></xsl:value-of>
                                </td>

                            </tr>
                        </xsl:for-each>

                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="5">Total Count: <xsl:value-of select="overall/@count"></xsl:value-of> &#0183;
                                Average Response Duration(ms):
                                <xsl:value-of select="format-number(overall/@avgRespTime,'##.#')"></xsl:value-of>
                            </td>
                        </tr>
                    </tfoot>

                </table>
                <br />
                <h3>Collections:</h3>

                <xsl:for-each select="collection">

                    <xsl:variable name="name" select="@name"/>


                    <h4>
                        <xsl:value-of select="@name"></xsl:value-of>
                    </h4>
                    <table class="reportTable">
                        <thead>
                            <tr>
                                <th>Status</th>
                                <th>Category</th>
                                <th>Count</th>
                                <th>Average Response Duration(ms)</th>
                                <th>Max Response Duration(ms)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <xsl:for-each select="statistics">

                                <xsl:variable name="status" select="@statusCode"/>

                                <tr>
                                    <xsl:attribute name="style">
                                        <xsl:choose>
                                            <xsl:when test="@category='Broken'">
                                                <xsl:text>background-color:#f2a6a6</xsl:text>
                                            </xsl:when>
                                            <xsl:when test="@category='Undetermined'">
                                                <xsl:text>background-color:#fff7b3</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:text>background-color:#cbe7cc</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>

                                    </xsl:attribute>


                                    <td class='text-right'>
                                        <a href="/statistics/{$name}/{$status}">
                                            <xsl:value-of select="@statusCode"></xsl:value-of>
                                        </a>
                                    </td>
                                    <td>
                                        <xsl:value-of select="@category"></xsl:value-of>
                                    </td>
                                    <td class='text-right'>
                                        <xsl:value-of select="@count"></xsl:value-of>
                                    </td>
                                    <td class='text-right'>
                                        <xsl:value-of select="format-number(@avgRespTime, '##.#')"></xsl:value-of>
                                    </td>
                                    <td class='text-right'>
                                        <xsl:value-of select="format-number(@maxRespTime, '##.#')"></xsl:value-of>
                                    </td>
                                </tr>

                            </xsl:for-each>

                        </tbody>

                    </table>
					<br />
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
