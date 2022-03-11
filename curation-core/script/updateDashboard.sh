#!/bin/bash
START_TIME=$SECONDS

WORK_DIR=/usr/local/curation-module
BIN_DIR=$WORK_DIR/bin
LIB_DIR=$WORK_DIR/lib
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

# set data paths
if [ -z "$PROVIDER_SETS" ]; then
   export PROVIDER_SETS="clarin europeana"
fi

for providerSet in $PROVIDER_SETS; do
   DATA_PATHS="$DATA_PATHS ${DATA_DIR}/${providerSet}/${CMDI_PATH}"
done 

echo "generating new reports, downloading necessary profiles..."
java $VM_ARGS -Dprojectname=curation -cp "${BIN_DIR}/curation.jar:${LIB_DIR}/*" eu.clarin.cmdi.curation.main.Main -config ${CONF_DIR}/config.properties -r -path ${DATA_PATHS}
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
