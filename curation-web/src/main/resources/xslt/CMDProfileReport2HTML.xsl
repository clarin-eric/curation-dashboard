<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:decimal-format NaN="N/A" />
	<xsl:template match="/cmdProfileReport">
		<xsl:variable name="schemaLoc">
			<xsl:value-of select="./headerReport/schemaLocation" />
		</xsl:variable>
		<html>
			<head>
			</head>
			<body>
				<div class="infoLine">
					<div class="floatLeft">
						created at
						<xsl:value-of select="./@creationTime" />
					</div>
					<div class="floatRight">
						download as
						<a>
							<xsl:attribute name="href">
					   <xsl:text>/download/instance/</xsl:text>
					   <xsl:value-of
								select="translate(./headerReport/id,'.:','__')" />
					</xsl:attribute>
							<xsl:text>xml</xsl:text>
						</a>
						<xsl:text> </xsl:text>
						<a>
							<xsl:attribute name="href">
					   <xsl:text>/download/instance/</xsl:text>
					   <xsl:value-of
								select="translate(./headerReport/id,'.:','__')" />
							<xsl:text>?format=json</xsl:text>
					</xsl:attribute>
							<xsl:text>json</xsl:text>
						</a>
					</div>
					<div class="clear" />
				</div>
				<h1>CMD Profile Report</h1>
           <p>
               Score: 
               <xsl:value-of select="format-number(./@score,'0.00')" /> / <xsl:value-of select="format-number(./@maxScore,'0.00')" />
            </p>
				<h2>Header Section</h2>
           <b>
               Score: 
               <xsl:value-of select="format-number(./headerReport/@score,'0.00')" /> / <xsl:value-of select="format-number(./headerReport/@maxScore,'0.00')" />
            </b>
				<table class="reportTable">
					<tr>
						<th>Name</th>
						<td>
							<xsl:value-of select="./headerReport/name" />
						</td>
					</tr>
					<tr>
						<th>ID</th>
						<td>
							<xsl:value-of select="./headerReport/id" />
						</td>
					</tr>
					<tr>
						<th>Description</th>
						<td>
							<xsl:value-of select="./headerReport/description" />
						</td>
					</tr>
					<tr>
						<th>Schema Location</th>

						<td>
							<xsl:choose>
								<xsl:when
									test="starts-with($schemaLoc, 'http://') or starts-with($schemaLoc, 'https://')">
									<a href="{$schemaLoc}" target="_blank">
										<xsl:copy-of select="$schemaLoc" />
									</a>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select='$schemaLoc' />
								</xsl:otherwise>
							</xsl:choose>
						</td>

					</tr>
					<tr>
						<th>CMDI Version</th>
						<td>
							<xsl:value-of select="./headerReport/cmdiVersion" />
						</td>
					</tr>
					<xsl:if test="./headerReport/status">
						<tr>
							<th>Status</th>
							<td>
								<xsl:value-of select="./headerReport/status" />
							</td>
						</tr>
					</xsl:if>
				</table>
				<hr />
				<details>
					<summary>
						<h2>Facets Section</h2>
					</summary>
					<p>The facet section shows if a specific facet is covered by the
						profile.
						In other words, if the profile defines an element for the facet.
					</p>
				</details>
            <p>
               Score: 
               <xsl:value-of select="format-number(./facetReport/@score,'0.00')" /> / <xsl:value-of select="format-number(./facetReport/@maxScore,'0.00')" />
            </p>
				<p>Number of facets: <xsl:value-of select="./facetReport/@numOfFacets" /></p>
				<p>Number of facets covered by profile: <xsl:value-of select="./facetReport/@numOfFacetsCoveredByProfile" /></p>
				<p>Percentage of facets covered by profile: <xsl:value-of select="format-number(./facetReport/@percProfileCoverage,'0.0%')" /></p>
				<table class="reportTable">
					<thead>
						<tr>
							<th scope="col">Name</th>
							<th scope="col">Covered</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each
							select="./facetReport/coverage/facet">
							<xsl:sort select="./@coveredByProfile"
								order="descending" />
							<tr>
								<xsl:choose>
									<xsl:when test="./@coveredByProfile = 'false'">
										<td>
											<font color="#d33d3d">
												<xsl:value-of select="./@name" />
											</font>
										</td>
										<td>
											<font color="#d33d3d">
												<xsl:value-of select="./@coveredByProfile" />
											</font>
										</td>
									</xsl:when>
									<xsl:otherwise>
										<td>
											<xsl:value-of select="./@name" />
										</td>
										<td>
											<xsl:value-of select="./@coveredByProfile" />
										</td>
									</xsl:otherwise>
								</xsl:choose>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
				<hr />
<!-- we don't have any usage information for user uploads

				<details>
					<summary>
						<h2>Usage Section</h2>
					</summary>
					<p>The usage section shows in which collection the profile is used</p>
				</details>
				<table class="reportTable">
					<thead>
						<tr>
							<th scope="col">Collection</th>
							<th scope="col">Usage</th>
						</tr>
					</thead>
					<tbody>
						<xsl:if test="count(./collectionUsage/collection)=0">
							<tr>
								<td colspan="2">profile not used</td>
							</tr>
						</xsl:if>
						<xsl:for-each select="./collectionUsage/collection">
							<tr>
								<td>
									<xsl:value-of select="./@collectionName" />
								</td>
								<td>
									<xsl:value-of select="./@count" />
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
				<hr />
 -->				
				<details>
					<summary>
						<h2>Cmd Component Section</h2>
					</summary>
					<p>
						The components section shows information on the kind, id and the
						usage of concepts in the profile.
						<br />
						For more information on componets, have a look at the
						<a
							href="https://www.clarin.eu/content/component-registry-documentation">Component Registry Documentation</a>
						, please.
					</p>
				</details>
				<p>Total number of components: <xsl:value-of select="./componentReport/@total" /></p>
				<p>Number of unique components: <xsl:value-of select="./componentReport/@unique" /></p>
				<p>Number of required components: <xsl:value-of select="./componentReport/@required" /></p>

				<table class="reportTable">
					<thead>
						<tr>
							<th scope="col">Name</th>
							<th scope="col">Id</th>
							<th scope="col">Count</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each
							select="./componentReport/component">
							<tr>
								<td>
									<xsl:value-of select="./@name" />
								</td>
								<td>
									<a target="_blank">
									   <xsl:attribute name="href">
									      <xsl:text>http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/</xsl:text>
									      <xsl:value-of select="./@id" />
									   </xsl:attribute>
									   <xsl:value-of select="./@id" />
									</a>
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
						<h2>Cmd Concepts Section</h2>
					</summary>
					<p>
						The concepts section shows information on the kind, state and the
						number of concepts used in the profile.
						<br />
						For more information on concepts, have a look at the
						<a href="https://www.clarin.eu/content/clarin-concept-registry">CLARIN Concept Registry</a>
						, please.
					</p>
				</details>
				<p>
               Score: 
               <xsl:value-of select="format-number(./conceptReport/@score,'0.00')" /> / <xsl:value-of select="format-number(./conceptReport/@maxScore,'0.00')" />
            </p>
				<p>
					Total number of elements:
					<xsl:value-of select="./conceptReport/@total" />
				</p>
				<p>
					Number of required elements:
					<xsl:value-of
						select="./conceptReport/@required" />
				</p>
				<p>
					Number of elements with specified concept:
					<xsl:value-of
						select="./conceptReport/@withConcept" />
				</p>
				<p>
					Percentage of elements with specified concept:
					<xsl:value-of
						select="format-number(./conceptReport/@percWithConcept,'0.0%')" />
				</p>
				<table class="reportTable">
					<thead>
						<tr>
							<th scope="col">Concept</th>
							<th scope="col">Status</th>
							<th scope="col">Count</th>
						</tr>
					</thead>
					<tfoot>
                  <tr>
                     <td colspan="3">
                        <b>
                           Total:
                           <xsl:value-of
                              select="./conceptReport/@total" />
                           Unique:
                           <xsl:value-of
                              select="./conceptReport/@unique" />
                           Required:
                           <xsl:value-of
                              select="./conceptReport/@required" />
                        </b>
                     </td>
                  </tr>
               </tfoot>

					<tbody>
						<xsl:for-each
							select="./conceptReport/concepts/concept">
							<tr>
								<xsl:variable name="href">
									<xsl:value-of select="./@uri" />
								</xsl:variable>
								<td>
									<a href="{$href}" title="{$href}" target="_blank">
										<xsl:value-of select="./@prefLabel" />
									</a>
								</td>
								<td>
									<xsl:value-of select="./@status" />
								</td>
								<td class="text-right">
									<xsl:value-of select="./@count" />
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>


				<xsl:if test="./details//detail">

					<hr />
					<details>
						<summary>
							<h2>Details</h2>
						</summary>
						<p>The details section shows the number and the kind of details,
							which might have an impact on the processing of the profile.</p>
					</details>
					<table class="reportTable">
						<thead>
							<tr>
								<th scope="col">Segment</th>
								<th scope="col">Severity</th>
								<th scope="col">Message</th>
							</tr>
						</thead>
						<tbody>
							<xsl:for-each select="./details/detail">
								<xsl:variable name="seg">
									<xsl:value-of select="./segment" />
								</xsl:variable>
								<xsl:choose>
									<xsl:when test="./severity = 'ERROR'">
										<tr>
											<td>
												<font color="#d33d3d">
													<xsl:copy-of select="$seg" />
												</font>
											</td>
											<td>
												<font color="#d33d3d">
													<xsl:value-of select="./severity" />
												</font>
											</td>
											<td>
												<font color="#d33d3d">
													<xsl:value-of select="./message" />
												</font>
											</td>
										</tr>
									</xsl:when>
									<xsl:when test="./severity = 'WARNING'">
										<tr>
											<td>
												<font color="#dbd839">
													<xsl:copy-of select="$seg" />
												</font>
											</td>
											<td>
												<font color="#dbd839">
													<xsl:value-of select="./severity" />
												</font>
											</td>
											<td>
												<font color="#dbd839">
													<xsl:value-of select="./message" />
												</font>
											</td>
										</tr>
									</xsl:when>
									<xsl:otherwise>
										<tr>
											<td>
												<xsl:copy-of select="$seg" />
											</td>
											<td>
												<xsl:value-of select="./severity" />
											</td>
											<td>
												<xsl:value-of select="./message" />
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</tbody>
					</table>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet> 