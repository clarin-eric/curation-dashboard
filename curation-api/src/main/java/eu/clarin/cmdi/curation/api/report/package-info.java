/**
 * This package contains all the JAXB annotated instances of Report to store the results of entity processing. Usually
 * alle the instance variables of the reports are public and directly accessible except in the few cases where the report
 * wraps another object.
 * The general structure of this package is first a sub-packaging by CurationEntityType (profile, instance, collection,
 * linkckecker). There we have the entity specific instance of NamedReport and a sub-package "sec", which contains all the section reports
 * of the entity specific report.
 * Further on we have an instance of AggregatedReport (except for instance), which, you might not guess it, aggregates
 * all instances of NamedReport of on entity in one report (rendered as an overview table in HTML)
 *
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 */

package eu.clarin.cmdi.curation.api.report;