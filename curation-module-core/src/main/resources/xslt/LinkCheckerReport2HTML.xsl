<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"></xsl:output>


	<xsl:template match="/linkchecker-report">
		<html>
			<head></head>
			<body>
			   <div class="creation-time">created at <xsl:value-of select="./@creation-time" /></div>
			   <div class="download">download as <a href="/download/xml/statistics/LinkCheckerReport">xml</a><xsl:text> </xsl:text><a href="/download/json/statistics/LinkCheckerReport">json</a></div>
				<div class="clear" />
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
								<xsl:value-of select="./@category" />
							</xsl:variable>
							<xsl:variable name="color">
								<xsl:value-of select="./@colorCode" />
							</xsl:variable>
							<tr style="background-color:{$color}">

								<td>
									<a href="/statistics/Overall/{$category}">
										<xsl:value-of select="@category"></xsl:value-of>
									</a>
								</td>
								<td class='text-right'>
									<xsl:value-of select="@count"></xsl:value-of>
								</td>
								<td class='text-right'>
									<xsl:choose>
										<xsl:when test="@avgRespTime &gt; 0">
											<xsl:value-of
												select="format-number(@avgRespTime, '0.0')"></xsl:value-of>
										</xsl:when>
										<xsl:otherwise>N/A</xsl:otherwise>
									</xsl:choose>
								</td>
								<td class='text-right'>
									<xsl:choose>
										<xsl:when test="@maxRespTime &gt; 0">
											<xsl:value-of
												select="format-number(@maxRespTime, '0.0')"></xsl:value-of>
										</xsl:when>
										<xsl:otherwise>N/A</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</xsl:for-each>

					</tbody>
					<tfoot>
						<tr>
							<td colspan="5">
								Total Count:
								<xsl:value-of select="overall/@count"></xsl:value-of>
								&#0183;
								Average Response Duration(ms):
								<xsl:choose>
									<xsl:when test="overall/@avgRespTime &gt; 0">
										<xsl:value-of
											select="format-number(overall/@avgRespTime,'0.0')"></xsl:value-of>
										&#0183;
									</xsl:when>
									<xsl:otherwise>N/A &#0183;</xsl:otherwise>
								</xsl:choose>
								Max Response Duration(ms):
								<xsl:choose>
									<xsl:when test="overall/@maxRespTime &gt; 0">
										<xsl:value-of
											select="format-number(overall/@maxRespTime,'0.0')"></xsl:value-of>
									</xsl:when>
									<xsl:otherwise>N/A</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</tfoot>
				</table>
				<br />
				<h3>Collections:</h3>

				<xsl:for-each select="collection">

					<xsl:variable name="name" select="@name" />

					<h4>
						<xsl:value-of select="replace($name,'_',' ')" />
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
									<xsl:value-of select="./@category" />
								</xsl:variable>
								<xsl:variable name="color">
									<xsl:value-of select="./@colorCode" />
								</xsl:variable>
								<tr style="background-color:{$color}">
									<td>
										<a href="/statistics/{$name}/{$category}">
											<xsl:value-of select="@category"></xsl:value-of>
										</a>
									</td>
									<td class='text-right'>
										<xsl:value-of select="@count"></xsl:value-of>
									</td>
									<td class='text-right'>
										<xsl:choose>
											<xsl:when test="@avgRespTime &gt; 0">
												<xsl:value-of
													select="format-number(@avgRespTime, '0.0')"></xsl:value-of>
											</xsl:when>
											<xsl:otherwise>
												N/A
											</xsl:otherwise>
										</xsl:choose>
									</td>
									<td class='text-right'>
										<xsl:choose>
											<xsl:when test="@maxRespTime &gt; 0">
												<xsl:value-of
													select="format-number(@maxRespTime, '0.0')"></xsl:value-of>
											</xsl:when>
											<xsl:otherwise>
												N/A
											</xsl:otherwise>
										</xsl:choose>
									</td>
								</tr>
							</xsl:for-each>
						</tbody>
						<tfoot>
							<tr>
								<td colspan="5">
									Total Count:
									<xsl:value-of select="@count"></xsl:value-of>
									&#0183;
									Average Response Duration(ms):
									<xsl:choose>
										<xsl:when test="@avgRespTime &gt; 0">
											<xsl:value-of
												select="format-number(@avgRespTime,'0.0')"></xsl:value-of>
											&#0183;
										</xsl:when>
										<xsl:otherwise>
											N/A &#0183;
										</xsl:otherwise>
									</xsl:choose>
									Max Response Duration(ms):
									<xsl:choose>
										<xsl:when test="@maxRespTime &gt; 0">
											<xsl:value-of
												select="format-number(@maxRespTime,'0.0')"></xsl:value-of>
										</xsl:when>
										<xsl:otherwise>
											N/A
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</tfoot>
					</table>
					<br />
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
