<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/cmdInstanceReport">
		<html>
			<head>
			</head>
			<body>
            <div class="creationTime">
               created at
               <xsl:value-of select="./@creationTime" />
            </div>
            <div class="download">
               download as
               <a>
                  <xsl:attribute name="href">
                   <xsl:text>/download/instance/</xsl:text>
                   <xsl:value-of select="replace(//fileReport/location,'[/.:]','_')" />
                </xsl:attribute>
                  <xsl:text>xml</xsl:text>
               </a>
               <xsl:text> </xsl:text>
               <a>
                  <xsl:attribute name="href">
                   <xsl:text>/download/instance/</xsl:text>
                   <xsl:value-of select="replace(//fileReport/location,'[/.:]','_')" />
                   <xsl:text>?format=json</xsl:text>
                </xsl:attribute>
                  <xsl:text>json</xsl:text>
               </a>
            </div>
            <div class="clear" />
				<h1>CMD Record Report</h1>
           <p>
               Profile Score: 
               <xsl:value-of select="format-number(./@profileScore,'0.00')" />
               Score:
               <xsl:value-of select="format-number(./@instanceScore,'0.00')" />
               out of 
               <xsl:value-of select="format-number(./@maxScore,'0.00')" />
            </p>

				<xsl:variable name="cmdRecord"
					select="./fileReport/location" />

				<p>
					CMD Record:
					<xsl:choose>
						<xsl:when
							test="starts-with($cmdRecord, 'http://') or starts-with($cmdRecord, 'https://')">
							<a href="{$cmdRecord}" target="_blank">
								<xsl:copy-of select="$cmdRecord" />
							</a>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="$cmdRecord" />
						</xsl:otherwise>
					</xsl:choose>
				</p>

				<p>Url: /download/xml/instance/</p>

				<p>
					ProfileID:
					<a>
						<xsl:attribute name="href">
                            <xsl:value-of
							select="./profileHeaderReport/schemaLocation"></xsl:value-of>
                        </xsl:attribute>
						<xsl:value-of select="./profileHeaderReport/id"></xsl:value-of>
					</a>
				</p>
				<!-- <p>Status: -->
				<!-- <xsl:value-of select="./profile-section/status"/> -->
				<!-- </p> -->
				<p>
					File Size:
					<xsl:value-of select="./fileReport/size" />
					B
				</p>

				<hr />

				<details>
					<summary>
						<h2>Facet Section</h2>
					</summary>
					<p>The facet section shows the facet coverage within the
						collection. It's quite evident that the facet coverage of a
						certain CMD file can't be higher than those of the profile it is
						based on.
					</p>
				</details>

				<font color="#ffd100">&#9873;</font>
				- Derived Facet
				<br />
				<font color="#00aa00">&#9873;</font>
				- Value Mapping
				<br />


				<button class="btn btn-info" id="facetValuesButton"
					type="button" onClick="toggleFacets()">Show Facet Values</button>

				<div id="facetTable" hidden="true">
					<table class="reportTable">
						<thead>
							<tr>
								<th scope="col">Value</th>
								<th scope="col">Facet</th>
								<th scope="col">Normalised Value</th>
								<th scope="col">Concept</th>
								<th scope="col">XPath</th>
							</tr>
						</thead>
						<tfoot>
							<tr>
								<td colspan="5">
									<b>
										covered by Instance
										<xsl:value-of
											select="count(./facetReport/coverages/facet[@coveredByInstance = 'true'])" />
										/
										<xsl:value-of
											select="./facetReport/@numOfFacets" />
										;

										covered by profile
										<xsl:value-of
											select="count(./facetReport/coverages/facet[@coveredByProfile = 'true'])" />
										/
										<xsl:value-of
											select="./facetReport/@numOfFacets" />
										;

										instance coverage:
										<xsl:value-of
											select="format-number(./facetReport/@percCoveragedByInstance,'0.0%')" />
									</b>
								</td>
							</tr>
						</tfoot>
						<tbody>

							<xsl:for-each
								select="./facetReport/valueNodes/valueNode">
								<xsl:choose>
									<xsl:when test="./facet">
										<xsl:for-each select="./facet">
											<tr>
												<xsl:if test="position() = 1">
													<td rowspan="{last()}">
														<xsl:value-of select="../value" />
													</td>
												</xsl:if>
												<xsl:choose>
													<xsl:when test="@usesValueMapping">
														<td>
															<font color="#00aa00">
																<xsl:value-of select="@name" />
															</font>
														</td>
													</xsl:when>
													<xsl:when test="@isDerived">
														<td>
															<font color="#ffd100">
																<xsl:value-of select="@name" />
															</font>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td>
															<xsl:value-of select="@name" />
														</td>
													</xsl:otherwise>
												</xsl:choose>
												<td>
													<xsl:value-of select="@normalisedValue" />
												</td>
												<xsl:if test="position() = 1">
													<td rowspan="{last()}">
														<a href="{../concept/@uri}"
															title="status: {../concept/@status}, uri: {../concept/@uri}"
															target="_blank">
															<xsl:value-of select="../concept/@prefLabel" />
														</a>
													</td>
												</xsl:if>
												<xsl:if test="position() = 1">
													<td rowspan="{last()}">
														<xsl:value-of select="../xpath" />
													</td>
												</xsl:if>
											</tr>
										</xsl:for-each>
									</xsl:when>
									<xsl:otherwise>
										<tr class="noFacet">
											<td>
												<xsl:value-of select="value" />
											</td>
											<td></td>
											<td></td>
											<td>
												<a href="{concept/@uri}"
													title="status: {concept/@status}, uri: {concept/@uri}"
													target="_blank">
													<xsl:value-of select="concept/@prefLabel" />
												</a>
											</td>
											<td>
												<xsl:value-of select="xpath" />
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>

							<xsl:for-each
								select="./facetReport/coverage/facet[@coveredByProfile = 'false']">
								<tr>
									<td>
										<font color="#d33d3d">not covered by profile</font>
									</td>
									<td>
										<font color="#d33d3d">
											<xsl:value-of select="@name" />
										</font>
									</td>
									<td></td>
									<td></td>
									<td></td>
								</tr>
							</xsl:for-each>

							<xsl:for-each
								select="./facetReport/coverage/facet[@coveredByInstance = 'false'][@coveredByProfile = 'true']">
								<tr>
									<td>
										<font color="#d33d3d">not covered by instance</font>
									</td>
									<td>
										<font color="#d33d3d">
											<xsl:value-of select="@name" />
										</font>
									</td>
									<td></td>
									<td></td>
									<td></td>
								</tr>
							</xsl:for-each>
						</tbody>
					</table>
				</div>

				<hr />

				<details>
					<summary>
						<h2>ResourceProxy Section</h2>
					</summary>
					<p>The resource proxy section shows information on the number of
						resource proxies on the kind (the mime type) of resources.
						A resource proxy is a link to an external resource, described by
						the CMD file.
					</p>
				</details>
				<p>
					Total number ResourceProxies:
					<xsl:value-of
						select="./resProxyReport/numOfResProxies" />
				</p>
				<p>
					Number of ResourceProxies having specified MIME type:
					<xsl:value-of
						select="./resProxyReport/numOfResourcesWithMime" />
				</p>
				<p>
					Percent of ResourceProxies having specified MIME type:
					<xsl:value-of
						select="format-number(./resProxyReport/percOfResourcesWithMime,'0.0%')" />
				</p>
				<p>
					Number of ResourceProxies having reference:
					<xsl:value-of
						select="./resProxyReport/numOfResProxiesWithReference" />
				</p>
				<p>
					Percent of ResourceProxies having reference:
					<xsl:value-of
						select="format-number(./resProxyReport/percOfResProxiesWithReference,'0.0%')" />
				</p>
				<table class="reportTable">
					<thead>
						<tr>
							<th scope="col">Resource Type</th>
							<th scope="col">Count</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each
							select="./resProxyReport/resourceTypes/resourceType">
							<tr>
								<td>
									<xsl:value-of select="./@type" />
								</td>
								<td class="text-right">
									<xsl:value-of select="./@count" />
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>

				<hr />

				<details>
					<summary>
						<h2>XML Validation Section</h2>
					</summary>
					<p>The XML validation section shows the result of a simple
						validation of each CMD file against its profile.
					</p>
				</details>
				<p>
					Validity according to profile:
					<xsl:value-of select="boolean(./xmlValidityReport/@score)" />
				</p>

				<hr />

				<details>
					<summary>
						<h2>XML Populated Section</h2>
					</summary>
					<p>The XML populated section shows information on the number of xml
						elements and the fact if these elements are conatining data.
					</p>
				</details>
				<p>
					Number of XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/numOfXMLElements" />
				</p>
				<p>
					Number of simple XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/numOfXMLSimpleElements" />
				</p>
				<p>
					Number of empty XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/numOfXMLEmptyElements" />
				</p>
				<p>
					Percentage of populated XML elements:
					<xsl:value-of
						select="format-number(./xmlPopulationReport/percOfPopulatedElements,'0.0%')" />
				</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet> 