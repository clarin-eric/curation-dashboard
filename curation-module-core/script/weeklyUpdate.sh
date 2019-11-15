#!/bin/bash
SECONDS=0

WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
DATA_DIR=$WORK_DIR/data
CONF_DIR=$WORK_DIR/conf

HARVESTER_URL=https://vlo.clarin.eu/resultsets
#RESULTSETS="clarin.tar.bz2 others.tar.bz2"
RESULTSETS="clarin.tar.bz2 others.tar.bz2 europeana.tar.bz2"
#RESULTSETS="clarin.tar.bz2"
CMDI_PATH=results/cmdi

LOG4J=-Dlog4j.configuration=file:$CONF_DIR/log4j.properties
VM_ARGS="-Xms4G -Xmx8G -XX:+UseG1GC -XX:-UseParallelGC -XX:+UseStringDeduplication"

XSD_CACHE=$WORK_DIR/xsd_cache
REPORTS_DIR=$WORK_DIR/reports/collections

# terminate script if anything goes wrong
set -e

#delete old data in case not done before
echo "delete old data in case not done before..."
if [ -e $DATA_DIR ]; then
	chmod -R a+w $DATA_DIR
	rm -rf $DATA_DIR
fi

# create new data directory
mkdir -p $DATA_DIR/clarin
mkdir $DATA_DIR/europeana

#get harvested collections
for RESULTSET in $RESULTSETS; do
	if [ "$RESULTSET" = "europeana.tar.bz2" ]; then
		cd $DATA_DIR/europeana
	else
		cd $DATA_DIR/clarin
	fi
	#download tar
	wget $HARVESTER_URL/$RESULTSET

	echo "unpacking $RESULTSET..."
	#unpack CMDI 1.2 files
	tar -xjf $RESULTSET $CMDI_PATH

	#delete tar
	rm $RESULTSET
done

#remove old profiles and reports
#echo "remove old profiles and reports..."
#find $XSD_CACHE -name '*.xsd' -exec rm {} \;
#find $REPORTS_DIR -name '*.xml' -exec rm {} \;

echo "generating new reports, downloading necessary profiles..."
java $VM_ARGS -Dprojectname=curate $LOG4J -jar $BIN_DIR/curation-module-core-3.1.2-jar-with-dependencies.jar -config $CONF_DIR/config.properties -r -path $DATA_DIR/clarin/$CMDI_PATH $DATA_DIR/europeana/$CMDI_PATH

# create value maps
for name in resourceClass_tf-extended profileName2resourceClass_tf-extended_noResourceClassProfiles collection modality organisation; do
	curl -O https://raw.githubusercontent.com/acdh-oeaw/VLO-mapping/master/value-maps/$name.csv
	java -jar $BIN_DIR/vlo-mapping-creator.jar $name.csv > $WORK_DIR/value_maps/$name.xml
	rm $name.csv
done

#echo "recharging curate vlo..."
#java -cp $BIN_DIR/vlo-importer-4.4.3-importer.jar $VM_ARGS -Dprojectname=vlo-importer $LOG4J -DconfigFile=$CONF_DIR/VloConfig-acdh.xml eu.clarin.cmdi.vlo.importer.MetadataImporterRunner

cd $WORK_DIR

if [ -e $DATA_DIR ]; then
        rm -rf $DATA_DIR
fi
echo "Finished!"
duration=$SECONDS
echo "please restart curate webapp with 'docker-manage -e clarin-curate -v'"
#restart curate webapp
#docker-manage -e clarin-curate -v
