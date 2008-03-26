#!/bin/csh
#
if ( ! -e JavaSource ) then
echo "Must be run from LAS home."
exit
endif
#
echo -n '#set ($revision="' > WebContent/productserver/templates/revision.vm
svnversion -n >> WebContent/productserver/templates/revision.vm
echo '")' >> WebContent/productserver/templates/revision.vm

head -26 WebContent/productserver/templates/info.vm > WebContent/productserver/templates/info_tmp.vm

echo '<h3>Subversion Info:</h3>' >> WebContent/productserver/templates/info_tmp.vm
echo '<pre>' >> WebContent/productserver/templates/info_tmp.vm
svn info >> WebContent/productserver/templates/info_tmp.vm
echo '</body>' >> WebContent/productserver/templates/info_tmp.vm
echo '</html>' >> WebContent/productserver/templates/info_tmp.vm
echo '</pre>' >> WebContent/productserver/templates/info_tmp.vm
mv WebContent/productserver/templates/info_tmp.vm WebContent/productserver/templates/info.vm
