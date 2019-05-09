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

                                <td>
                                    <xsl:value-of select="@statusCode"></xsl:value-of>
                                </td>

                                <td>
                                    <xsl:value-of select="@category"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="@count"></xsl:value-of>
                                </td>
                                <td>
                                    <xsl:value-of select="format-number(@avgRespTime, '##.#')"></xsl:value-of>
                                </td>
                                <td>
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
                <h2>Collections:</h2>

                <xsl:for-each select="collection">

                    <xsl:variable name="name" select="@name"/>

                    <h3>
                        <xsl:value-of select="@name"></xsl:value-of>
                    </h3>
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

                                <xsl:variable name="category" select="@category"/>

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


                                    <td>
                                        <a href="/statistics/{$name}/{$category}">
                                            <xsl:value-of select="@statusCode"></xsl:value-of>
                                        </a>
                                    </td>
                                    <td>
                                        <xsl:value-of select="@category"></xsl:value-of>
                                    </td>
                                    <td>
                                        <xsl:value-of select="@count"></xsl:value-of>
                                    </td>
                                    <td>
                                        <xsl:value-of select="format-number(@avgRespTime, '##.#')"></xsl:value-of>
                                    </td>
                                    <td>
                                        <xsl:value-of select="format-number(@maxRespTime, '##.#')"></xsl:value-of>
                                    </td>
                                </tr>

                            </xsl:for-each>

                        </tbody>

                    </table>

                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
