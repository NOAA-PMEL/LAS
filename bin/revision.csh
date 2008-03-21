#!/bin/csh
#
if ( ! -e JavaSource ) then
echo "Must be run from LAS home."
exit
endif

echo -n '#set ($revision="' > WebContent/productserver/templates/revision.vm
svnversion -n >> WebContent/productserver/templates/revision.vm
echo '")' >> WebContent/productserver/templates/revision.vm
