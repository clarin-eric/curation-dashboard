<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">
    <xsl:output method="xhtml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:decimal-format NaN="N/A"/>
    <xsl:template match="/cmdInstanceReport">
        <div class="infoLine">
            <div class="floatLeft">
                created at
                <xsl:value-of select="./@creationTime"/>
            </div>
            <div class="floatRight">
                download as
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>/download/instance/</xsl:text>
                        <xsl:value-of
                                select="replace(//fileReport/location,'[/.:]','_')"/>
                    </xsl:attribute>
                    <xsl:text>xml</xsl:text>
                </a>
                <xsl:text> </xsl:text>
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>/download/instance/</xsl:text>
                        <xsl:value-of
                                select="replace(//fileReport/location,'[/.:]','_')"/>
                        <xsl:text>?format=json</xsl:text>
                    </xsl:attribute>
                    <xsl:text>json</xsl:text>
                </a>
            </div>
            <div class="clear"/>
        </div>
        <h1>CMD Record Report</h1>
        <p>
            Profile score:
            <xsl:value-of
                    select="format-number(./@profileScore,'0.00')"/>
            <br/>
            Score:
            <xsl:value-of
                    select="format-number(./@instanceScore,'0.00')"/>
            out of
            <xsl:value-of
                    select="format-number(./@maxScore,'0.00')"/>
        </p>

        <xsl:variable name="cmdRecord"
                      select="./fileReport/location"/>

        <p>
            CMD Record:
            <xsl:choose>
                <xsl:when
                        test="starts-with($cmdRecord, 'http://') or starts-with($cmdRecord, 'https://')">
                    <a href="{$cmdRecord}" target="_blank">
                        <xsl:copy-of select="$cmdRecord"/>
                    </a>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$cmdRecord"/>
                </xsl:otherwise>
            </xsl:choose>
        </p>
        <p>
            Schema location:
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="./profileHeaderReport/schemaLocation"/>
                </xsl:attribute>
                <xsl:value-of select="./profileHeaderReport/schemaLocation"/>
            </a>
        </p>
        <!-- <p>Status: -->
        <!-- <xsl:value-of select="./profile-section/status"/> -->
        <!-- </p> -->
        <p>
            File size (in CMDI 1.2):
            <xsl:value-of select="./fileReport/size"/>
            B
        </p>
        <hr/>
        <xsl:apply-templates select="./facetReport"/>
        <hr/>
        <xsl:apply-templates select="./resProxyReport"/>
        <hr/>
        <xsl:apply-templates
                select="./xmlPopulationReport"/>
        <hr/>
        <xsl:apply-templates
                select="./xmlValidityReport"/>

        <xsl:if test="./details/detail">
            <hr/>
            <details>
                <summary>
                    <h2 id="recordDetails">Record Details:</h2>
                </summary>
                <p>The record details section shows the particularities the record
                    as far as they're of importance for the data provider.
                </p>
            </details>
            <table class="reportTable">
                <thead>
                    <tr>
                        <th>Severity</th>
                        <th>Segment</th>
                        <th>Validate</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="./details/detail">
                        <xsl:choose>
                            <xsl:when test="not(position() > 100)">
                                <tr>
                                    <td>
                                        <xsl:value-of select="./severity"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./segment"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="./message"/>
                                    </td>
                                </tr>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr>
                                    <td colspan="3">[...] complete list in downloadable report</td>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>
    <!-- Facets -->
    <xsl:template match="facetReport">
        <details>
            <summary>
                <h2>Facets</h2>
            </summary>
            <p>The facets section shows the facet coverage in the uploaded file
            </p>
        </details>

        <h3>Facet Coverage</h3>

        <table class="reportTable">
            <thead>
                <tr>
                    <td>Name</td>
                    <td>Covered By Profile</td>
                    <td>Covered By Instance</td>
                </tr>
            </thead>
            <tfoot>
                <tr>
                    <td colspan="3">
                        <b>
                            Covered by profile
                            <xsl:value-of
                                    select="count(./coverages/facet[@coveredByProfile = 'true'])"/>
                            /
                            <xsl:value-of select="./@numOfFacets"/>
                            ;

                            Covered by Instance
                            <xsl:value-of
                                    select="./@numOfFacetsCoveredByInstance"/>
                            /
                            <xsl:value-of select="./@numOfFacets"/>
                            ;

                            Instance coverage:
                            <xsl:value-of
                                    select="format-number(./@percCoveragedByInstance,'0.0%')"/>
                        </b>
                    </td>
                </tr>
            </tfoot>
            <tbody>
                <xsl:for-each select="./coverages/facet">
                    <tr>
                        <td>
                            <xsl:value-of select="./@name"/>
                        </td>
                        <xsl:choose>
                            <xsl:when test="@coveredByProfile = 'true'">
                                <td class="facetCovered"
                                    style="background-color: lightgreen;">
                                    <xsl:value-of select="@coveredByProfile"/>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td class="facetNotCovered"
                                    style="background-color: lightcoral;">
                                    <xsl:value-of select="@coveredByProfile"/>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="@coveredByInstance = 'true'">
                                <td class="facetCovered"
                                    style="background-color: lightgreen;">
                                    <xsl:value-of select="@coveredByInstance"/>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td class="facetNotCovered"
                                    style="background-color: lightcoral;">
                                    <xsl:value-of select="@coveredByInstance"/>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>

                </xsl:for-each>

            </tbody>
        </table>

        <br/>
        <font color="#ffd100">&#9873;</font>
        - Derived facet
        <br/>
        <font color="#00aa00">&#9873;</font>
        - Value mapping
        <br/>

        <button class="btn btn-info" id="facetValuesButton" type="button">Show facet values</button>

        <div id="facetTable" hidden="true">
            <h3>Facet mapping</h3>
            <table class="reportTable">
                <thead>
                    <tr>
                        <th scope="col">XPath</th>
                        <th scope="col">Value</th>
                        <th scope="col">Concept</th>
                        <th scope="col">Facet</th>
                        <th scope="col">Normalised Value</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:apply-templates
                            select="./valueNodes/valueNode"/>
                </tbody>
            </table>
        </div>
    </xsl:template>
    <xsl:template match="valueNode">
        <xsl:choose>
            <xsl:when test="./facet">
                <xsl:for-each select="./facet">
                    <tr>
                        <xsl:if test="position() = 1">
                            <td rowspan="{last()}">
                                <xsl:value-of select="../xpath"/>
                            </td>
                            <td rowspan="{last()}">
                                <xsl:value-of select="../value"/>
                            </td>
                            <td rowspan="{last()}">
                                <a href="{../concept/@uri}"
                                   title="status: {../concept/@status}, uri: {../concept/@uri}"
                                   target="_blank">
                                    <xsl:value-of select="../concept/@prefLabel"/>
                                </a>
                            </td>
                        </xsl:if>
                        <xsl:choose>
                            <xsl:when test="@usesValueMapping">
                                <td>
                                    <font color="#00aa00">
                                        <xsl:value-of select="@name"/>
                                    </font>
                                </td>
                            </xsl:when>
                            <xsl:when test="@isDerived">
                                <td>
                                    <font color="#ffd100">
                                        <xsl:value-of select="@name"/>
                                    </font>
                                </td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td>
                                    <xsl:value-of select="@name"/>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                        <td>
                            <xsl:value-of select="@normalisedValue"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <tr class="noFacet">
                    <td>
                        <xsl:value-of select="xpath"/>
                    </td>
                    <td>
                        <xsl:value-of select="value"/>
                    </td>
                    <td>
                        <a href="{./concept/@uri}"
                           title="status: {./concept/@status}, uri: {./concept/@uri}"
                           target="_blank">
                            <xsl:value-of select="concept/@prefLabel"/>
                        </a>
                    </td>
                    <td></td>
                    <td></td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Resource proxy -->
    <xsl:template match="resProxyReport">
        <details>
            <summary>
                <h2>Resource Proxy</h2>
            </summary>
            <p>The resource proxy section shows information on the number of
                resource proxies on the kind (the mime type) of resources.
                A resource proxy is a link to an external resource, described by
                the CMD file.
            </p>
        </details>
        <p>
            Total number resource proxies:
            <xsl:value-of select="./numOfResources"/>
        </p>
        <p>
            Number of resource proxies having specified MIME type:
            <xsl:value-of select="./numOfResourcesWithMime"/>
        </p>
        <p>
            Percent of resource proxies having specified MIME type:
            <xsl:value-of
                    select="format-number(./percOfResourcesWithMime,'0.0%')"/>
        </p>
        <p>
            Number of resource proxies having reference:
            <xsl:value-of select="./numOfResourcesWithReference"/>
        </p>
        <p>
            Percent of resource proxies having reference:
            <xsl:value-of
                    select="format-number(./percOfResourcesWithReference,'0.0%')"/>
        </p>
        <table class="reportTable">
            <thead>
                <tr>
                    <th scope="col">Resource Type</th>
                    <th scope="col">Count</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="./resourceTypes/resourceType">
                    <tr>
                        <td>
                            <xsl:value-of select="./@type"/>
                        </td>
                        <td class="text-right">
                            <xsl:value-of select="./@count"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    <!-- XML population -->
    <xsl:template match="xmlPopulationReport">
        <details>
            <summary>
                <h2>XML Population</h2>
            </summary>
            <p>The XML population section shows information on the number of xml
                elements and the fact if these elements are containing data.
            </p>
        </details>
        <p>
            Number of XML elements:
            <xsl:value-of select="./numOfXMLElements"/>
        </p>
        <p>
            Number of simple XML elements:
            <xsl:value-of select="./numOfXMLSimpleElements"/>
        </p>
        <p>
            Number of empty XML elements:
            <xsl:value-of
                    select="./xmlPopulationReport/numOfXMLEmptyElements"/>
        </p>
        <p>
            Percentage of populated XML elements:
            <xsl:value-of
                    select="format-number(./percOfPopulatedElements,'0.0%')"/>
        </p>
    </xsl:template>
    <!-- XML validity -->
    <xsl:template match="xmlValidityReport">
        <details>
            <summary>
                <h2>XML Validation</h2>
            </summary>
            <p>The XML validation section shows the result of a simple
                validation of each CMD file against its profile.
            </p>
        </details>
        <p>
            Validity according to profile:
            <xsl:value-of select="boolean(./@score)"/>
        </p>
    </xsl:template>
</xsl:stylesheet> 