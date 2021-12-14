#!/bin/bash
START_TIME=$SECONDS

WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
DATA_DIR=$WORK_DIR/data
CONF_DIR=$WORK_DIR/conf

HARVESTER_URL=https://vlo.clarin.eu/resultsets
#RESULTSETS="clarin.tar.bz2 others.tar.bz2"
RESULTSETS="clarin.tar.bz2 others.tar.bz2 europeana.tar.bz2"
#RESULTSETS="clarin.tar.bz2"
CMDI_PATH=results/cmdi

VM_ARGS="-Xms4G -Xmx8G -XX:+UseG1GC -XX:-UseParallelGC -XX:+UseStringDeduplication -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:GCTimeRatio=20"

XSD_CACHE=$WORK_DIR/xsd_cache

# terminate script if anything goes wrong
set -e

#delete old data in case not done before
#echo "delete old data in case not done before..."
#if [ -e $DATA_DIR ]; then
#	chmod -R a+w $DATA_DIR
#	rm -rf $DATA_DIR
#fi

# create new data directory
#mkdir -p $DATA_DIR/clarin
#mkdir $DATA_DIR/europeana

#get harvested collections
#for RESULTSET in $RESULTSETS; do
#	if [ "$RESULTSET" = "europeana.tar.bz2" ]; then
#		cd $DATA_DIR/europeana
#	else
#		cd $DATA_DIR/clarin
#	fi
#	#download tar
#	wget $HARVESTER_URL/$RESULTSET
#
#	echo "unpacking $RESULTSET..."
#	#unpack CMDI 1.2 files
#	tar -xjf $RESULTSET $CMDI_PATH
#
#	#delete tar
#	rm $RESULTSET
#done

echo "generating new reports, downloading necessary profiles..."
java $VM_ARGS -Dprojectname=curate -jar $BIN_DIR/curate.jar -config $CONF_DIR/config.properties -r -path $DATA_DIR/clarin/$CMDI_PATH $DATA_DIR/europeana/$CMDI_PATH
echo "report generation finished."

if [ -e "$BIN_DIR/vlo-mapping-creator.jar" ]; then
	echo "creating value maps..."
	# create value maps
	for name in resourceClass_tf-extended profileName2resourceClass_tf-extended_noResourceClassProfiles collection modality organisation; do
		curl -O https://raw.githubusercontent.com/acdh-oeaw/VLO-mapping/master/value-maps/$name.csv
		java -jar "$BIN_DIR/vlo-mapping-creator.jar" $name.csv > $WORK_DIR/value_maps/$name.xml
		rm $name.csv
	done
	echo "Finished!"
else
	echo "Skipping value map creation - mapping creator binary not found"
fi

ELAPSED_TIME=$(($SECONDS - $START_TIME))
echo "Elapsed time: $(($ELAPSED_TIME/60)) min"
echo "please restart curate webapp with 'docker-manage -e clarin-curate -v'"
#restart curate webapp
#docker-manage -e clarin-curate -v
