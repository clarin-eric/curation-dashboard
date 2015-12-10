<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <ns uri="http://www.clarin.eu/cmd/" prefix="cmd"/>
    <ns uri="http://www.w3.org/2001/XMLSchema-instance" prefix="xsi"/>

    <pattern>
        <rule role="warning" context="cmd:Header">
            <assert test="string-length(cmd:MdProfile/text()) &gt; 0">
                [CMDI Best Practices] A CMDI instance should contain a non-empty &lt;cmd:MdProfile&gt; element in &lt;cmd:Header&gt;.
            </assert>
        </rule>   
    </pattern>

    <pattern>
        <rule  role="warning" context="cmd:Header">
            <assert test="string-length(cmd:MdSelfLink/text()) &gt; 0">
                [CMDI Best Practices] A CMDI instance should contain a non-empty &lt;cmd:MdSelfLink&gt; element in &lt;cmd:Header&gt;.
            </assert>
        </rule>   
    </pattern>

    <!--
    <pattern>
        <rule context="cmd:Header" role="information">
            <report test="cmd:MdSelfLink">MdSelfLink "<value-of select="cmd:MdSelfLink"/>".</report>
        </rule>
    </pattern>
    -->
    
    <!--
        Rules contributed by Menzo Windhouwer <Menzo.Windhouwer@mpi.nl>
        Reformatted and assert messages slightly reworded
    -->
    <!-- Does the schema reside in the Component Registry? -->
    <pattern>
        <title>Test xsi:schemaLocation</title>
        <rule role="warning" context="/cmd:CMD">
            <assert test="contains(@xsi:schemaLocation,'http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/')">
                [CMDI Best Practice] /cmd:CMD/@xsi:schemaLocation doesn't refer to a schema from the Component Registry! [Actual value was [<value-of select="@xsi:schemaLocation"/>]
            </assert>
        </rule>
    </pattern>
    
    <!-- Is there at least one ResourceProxy? -->
    <pattern>
        <title>Test for ResourceProxies</title>
        <rule role="warning" context="/cmd:CMD/cmd:Resources/cmd:ResourceProxyList">
            <assert test="count(cmd:ResourceProxy) ge 1">
                [CMDI Best Practices] There should be at least one ResourceProxy! Otherwise this is the metadata of what? Itself?
            </assert>
        </rule>
    </pattern>
    
    <!-- Can we determine the profile used? -->
    <pattern>
        <title>Test for known profile</title>
        <rule role="warning" context="/cmd:CMD">
            <assert test="matches(@xsi:schemaLocation,'clarin.eu:cr[0-9]+:p_[0-9]+.+') or matches(cmd:Header/cmd:MdProfile,'clarin.eu:cr[0-9]+:p_[0-9]+.+')">
                [CMDI Best Practice] the CMD profile of this record can't be found in the /cmd:CMD/@xsi:schemaLocation or /cmd:CMD/cmd:Header/cmd:MdProfile. The profile should be known for the record to be processed properly in the CLARIN joint metadata domain!
            </assert>
        </rule>
    </pattern>
    
    <!-- Is the CMD namespace bound to a schema? -->
    <pattern>
        <title>Test for CMD namespace schema binding</title>
        <rule role="warning" context="/cmd:CMD">
            <assert test="matches(@xsi:schemaLocation,'http://www.clarin.eu/cmd/ ')">
                [possible CMDI Best Practice] is the CMD namespace properly bound to a profile schema?
            </assert>
        </rule>
    </pattern>
    
    <!-- Is the cmd:CMD root there? -->
    <pattern>
        <title>Test for cmd:CMD root</title>
        <rule role="warning" context="/">
            <assert test="exists(cmd:CMD)">
                [CMDI violation] is this really a CMD record? Is the namespace properly declared, e.g., including ending slash?
            </assert>
        </rule>
    </pattern>
    
</schema>
