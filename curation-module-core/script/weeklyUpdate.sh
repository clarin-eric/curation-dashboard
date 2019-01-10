# author: wowasa
# script deletes old profiles and reports, genrates new reports, recharges vlo

#!/bin/bash
SECONDS=0

NUMBER_OF_PROCESSES=2
MAX_FILES_PER_PROCESS=250000
WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
DATA_DIR=$WORK_DIR/data
CONF_DIR=$WORK_DIR/conf

CMDI_PATH=results/cmdi

LOG4J=-Dlog4j.configuration=file:$CONF_DIR/log4j.properties
VM_ARGS="-Xms2G -Xmx4G -XX:+UseG1GC -XX:-UseParallelGC -XX:+UseStringDeduplication"

XSD_CACHE=$WORK_DIR/xsd_cache
REPORTS_DIR=$WORK_DIR/reports/collections


#remove old profiles and reports
echo "remove old reports..."
find $REPORTS_DIR -name '*.xml' -exec rm {} \;

echo "generating new reports, downloading necessary profiles..."
ls -d $DATA_DIR/$CMDI_PATH/*|xargs -i -P $NUMBER_OF_PROCESSES java $VM_ARGS -Dprojectname=curate $LOG4J -jar $BIN_DIR/curation-module-core-2.0-jar-with-dependencies.jar -config $CONF_DIR/config.properties -c -path {}

echo "recharging curate vlo..."
#java -cp $BIN_DIR/vlo-importer-4.4.3-importer.jar $VM_ARGS -Dprojectname=vlo-importer $LOG4J -DconfigFile=$CONF_DIR/VloConfig.xml eu.clarin.cmdi.vlo.importer.MetadataImporterRunner

echo "Finished!"
duration=$SECONDS
#echo "For $MAX_FILES_PER_PROCESS records per collection, it took: $(($duration / 60)) minutes and $(($duration % 60)) seconds to generate all reports."
echo "please restart curate webapp with 'docker-manage -e clarin-curate -v'"
