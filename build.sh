#!/bin/bash
if [ -L $0 ] ; then
    DIR=$(dirname $(readlink -f $0)) ;
else
    DIR=$(dirname $0) ;
fi;
cd $DIR;
VERSIONS=('1.2' '1.3' '1.4')
for v in "${VERSIONS[@]}"
do
    echo ''
    echo '*******************************************************'
    echo '* Building for Minecraft version: '$v'                 *'
    echo '*******************************************************'
    echo ''
    cd $v
    mvn
    cd ..
done