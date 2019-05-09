<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/profile-report">
<html>
	<head>
	</head>
	<body>
	
		<xsl:variable name="schemaLoc"><xsl:value-of select="./header-section/schemaLocation" /></xsl:variable>
		
		<h1>CMD Profile Report</h1>
		
		 <table class="reportTable">
			  <tr>
			    <th>Name</th>
			    <th><xsl:value-of select="./header-section/name"/></th>
			  </tr>
			  <tr>
			    <th>ID</th>
			    <th><xsl:value-of select="./header-section/id"/></th>
			  </tr>
			  <tr>
			    <th>Description</th>
			    <th><xsl:value-of select="./header-section/description"/></th>
			  </tr>
			  <tr>
			    <th>Schema Location</th>
			    <th><a href="{$schemaLoc}" target="_blank"><xsl:copy-of select="$schemaLoc" /></a></th>
			  </tr>
			  <tr>
			    <th>CMDI Version</th>
			    <th><xsl:value-of select="./header-section/cmdiVersion"/></th>
			  </tr>
			  <xsl:if test="./header-section/status">
				  <tr>
				    <th>Status</th>
				    <th><xsl:value-of select="./header-section/status"/></th>
				  </tr>
			  </xsl:if>
			 <tr>
				 <xsl:variable name="url"><xsl:value-of select="./@url"/></xsl:variable>
				 <th>URL:</th>
				 <th><a href="{$url}"><xsl:copy-of select="$url"/></a></th>
			 </tr>
			</table>	
		<hr/>
		
		<h2>score-section</h2>
		<table class="reportTable">
			<thead>
				<tr>
					<th scope="col">segment</th>
					<th scope="col">score</th>
					<th scope="col">max</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="3"><b>
					total: <xsl:value-of select="format-number(./@score,'##.##')"/>
					max: <xsl:value-of select="format-number(./@max-score,'##.##')"/>
				</b></td></tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./score-section/score">
			<tr>
				<td><xsl:value-of select="./@segment"/></td>
				<td class="v-align-right"><xsl:value-of select="format-number(./@score,'##.##')"/></td>
				<td class="v-align-right"><xsl:value-of select="format-number(./@maxScore,'##.##')"/></td>
		    </tr>
			</xsl:for-each>
			</tbody>
		</table>
		
		<hr/>
		
		<h2>facets-section</h2>	
		<table class="reportTable">
			<thead>
				<tr>
					<th scope="col">name</th>
					<th scope="col">covered</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="2"><b>
					covered:
					<xsl:value-of select="count(./facets-section/coverage/facet[@coveredByProfile = 'true'])" /> / 
					<xsl:value-of select="./facets-section/@numOfFacets"/>
					coverage: <xsl:value-of select="format-number(./facets-section/@profileCoverage,'##.#%')"/>
				</b></td></tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./facets-section/coverage/facet">
				<xsl:sort select="./@coveredByProfile" order="descending"/>
				<tr>				
					<xsl:choose>
		  				<xsl:when test="./@coveredByProfile = 'false'">								
							<td><font color="#d33d3d"><xsl:value-of select="./@name"/></font></td>
							<td><font color="#d33d3d"><xsl:value-of select="./@coveredByProfile"/></font></td>								
						</xsl:when>
		  				<xsl:otherwise>
		  					<td><xsl:value-of select="./@name"/></td>
							<td><xsl:value-of select="./@coveredByProfile"/></td>	
		  				</xsl:otherwise>
		  			</xsl:choose>
			    </tr>
			</xsl:for-each>
			</tbody>
		</table>
		<hr/>
		<h2>cmd-component-section</h2>	
		<table class="reportTable">
			<thead>
				<tr>
					<th scope="col">name</th>
					<th scope="col">id</th>
					<th scope="col">count</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="3"><b>
					total: <xsl:value-of select="./cmd-components-section/@total"/>
					unique: <xsl:value-of select="./cmd-components-section/@unique"/>
					required: <xsl:value-of select="./cmd-components-section/@required"/>
				</b></td></tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./cmd-components-section/component">
			<tr>
				<xsl:variable name="href">http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/<xsl:value-of select="./@id" /></xsl:variable>
				<td><xsl:value-of select="./@name"/></td>
				<td><a href="{$href}" target="_blank"><xsl:value-of select="./@id" /></a></td>
				<td class="v-align-right"><xsl:value-of select="./@count"/></td>
		    </tr>
			</xsl:for-each>
			</tbody>
		</table>
		<hr/>
		<h2>cmd-concepts-section</h2>
		<p>total number of elements: <xsl:value-of select="./cmd-concepts-section/@total"/></p>
		<p>number of required elements: <xsl:value-of select="./cmd-concepts-section/@required"/></p>
		<p>number of elements with specified concept: <xsl:value-of select="./cmd-concepts-section/@withConcept"/></p>
		<p>percentage of elements with specified concept: <xsl:value-of select="format-number(./cmd-concepts-section/@percWithConcept,'##.#%')"/></p>
		<table class="reportTable">
			<thead>
				<tr>
					<th scope="col">concept</th>
					<th scope="col">status</th>
					<th scope="col">count</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="3"><b>
					total: <xsl:value-of select="./cmd-concepts-section/concepts/@total"/>
					unique: <xsl:value-of select="./cmd-concepts-section/concepts/@unique"/>
					required: <xsl:value-of select="./cmd-concepts-section/concepts/@required"/>
				</b></td></tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./cmd-concepts-section/concepts/concept">
			<tr>
				<xsl:variable name="href"><xsl:value-of select="./@uri" /></xsl:variable>
				<td><a href="{$href}" title="{$href}" target="_blank"><xsl:value-of select="./@prefLabel" /></a></td>
				<td><xsl:value-of select="./@status"/></td>
				<td class="v-align-right"><xsl:value-of select="./@count"/></td>
		    </tr>
			</xsl:for-each>
			</tbody>
		</table>
		
		
		<xsl:if test="./score-section//issue">
		
			<hr/>	
			<h2>Issues</h2>
			<table class="reportTable">
				<thead>
					<tr>
						<th scope="col">segment</th>
						<th scope="col">severity</th>
						<th scope="col">message</th>
					</tr>
				</thead>
				<tbody>
				<xsl:for-each select="./score-section/score">
				<xsl:variable name="seg"><xsl:value-of select="./@segment" /></xsl:variable>
					<xsl:for-each select="./issue">
						<xsl:choose>
			  				<xsl:when test="@lvl = 'ERROR'">	
			  				  	<tr>							
									<td><font color="#d33d3d"><xsl:copy-of select="$seg" /></font></td>
									<td><font color="#d33d3d"><xsl:value-of select="./@lvl"/></font></td>
									<td><font color="#d33d3d"><xsl:value-of select="./@message"/></font></td>
							   	</tr>							  					
							</xsl:when>
							<xsl:when test="@lvl = 'WARNING'">
								<tr>							
									<td><font color="#dbd839"><xsl:copy-of select="$seg" /></font></td>
									<td><font color="#dbd839"><xsl:value-of select="./@lvl"/></font></td>
									<td><font color="#dbd839"><xsl:value-of select="./@message"/></font></td>
							  	</tr>							  					
							</xsl:when>
			  				<xsl:otherwise>
								<tr>
									<td><xsl:copy-of select="$seg" /></td>
									<td><xsl:value-of select="./@lvl"/></td>
									<td><xsl:value-of select="./@message"/></td>
							    </tr>	
			  				</xsl:otherwise>
			  			</xsl:choose>
			    	</xsl:for-each>			    	
				</xsl:for-each>
				</tbody>
			</table>
		</xsl:if>
	</body>
</html>
</xsl:template>
</xsl:stylesheet> 