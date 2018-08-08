#!/bin/bash
SECONDS=0

NUMBER_OF_PROCESSES=3
MAX_FILES_PER_PROCESS=0
WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
DATA_DIR=$WORK_DIR/data
CONF_DIR=$WORK_DIR/conf

HARVESTER_URL=https://vlo.clarin.eu/resultsets
RESULTSETS="clarin.tar.bz2 others.tar.bz2"
#RESULTSETS="clarin.tar.bz2"
CMDI_PATH=results/cmdi

LOG4J="-Dprojectname=curate -Dlog4j.configuration=file:$CONF_DIR/log4j.properties"
VM_ARGS="-Xms1G -Xmx2G -XX:+UseG1GC -XX:-UseParallelGC -XX:+UseStringDeduplication"

XSD_CACHE=$WORK_DIR/xsd_cache
REPORTS_DIR=$WORK_DIR/reports/collections

# terminate script if anything goes wrong
set -e

#delete old data in case not done before
echo "delete old data in case not done before..."
if [ -e $DATA_DIR ]; then
	rm -rf $DATA_DIR
fi

# create new data directory
mkdir $DATA_DIR

cd $DATA_DIR

#get harvested collections
for RESULTSET in $RESULTSETS; do
	#download tar
	wget $HARVESTER_URL/$RESULTSET
	
	echo "unpacking $RESULTSET..."
	#unpack CMDI 1.2 files
	tar -xjf $RESULTSET $CMDI_PATH
	
	#delete tar
	rm $RESULTSET
done

# limit files to process per collection
if [ $MAX_FILES_PER_PROCESS -gt 0 ]; then
echo "limiting files per collection to $MAX_FILES_PER_PROCESS..."
        for COLLECTION in $(ls -d $DATA_DIR/$CMDI_PATH/*);do
		find $COLLECTION -type f |head -n -$MAX_FILES_PER_PROCESS|xargs -i rm {} 
        done
fi


#remove old profiles and reports
echo "remove old profiles and reports..."
find $XSD_CACHE -name '*.xsd' -exec rm {} \;
find $REPORTS_DIR -name '*.xml' -exec rm {} \;

echo "generating new reports, downloading necessary profiles..."
ls -d $DATA_DIR/$CMDI_PATH/*|xargs -i -P $NUMBER_OF_PROCESSES java $VM_ARGS $LOG4J -jar $BIN_DIR/curation-module-core-1.2.jar -config $CONF_DIR/config.properties -c -path {}

cd $WORK_DIR

if [ -e $DATA_DIR ]; then
        rm -rf $DATA_DIR
fi
echo "Finished!"
duration=$SECONDS
echo "For $MAX_FILES_PER_PROCESS records per collection, it took: $(($duration / 60)) minutes and $(($duration % 60)) seconds to generate all reports."
echo "please restart curate webapp with 'docker-manage -e vlo-curate -v'"
#restart curate webapp
#docker-manage -e vlo-curate -v
