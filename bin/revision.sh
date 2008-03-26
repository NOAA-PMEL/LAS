#!/bin/sh
#
if [ ! -d JavaSource ]; then
echo "Must be run from LAS home."
exit 1
fi
REV=`svnversion -n`
if [ "$?" -ne "0" ]; then
       echo "Subversion not installed.  That is ok..."
       exit 1
fi
echo -n "#set (\$revision=\"$REV\")" > WebContent/productserver/templates/revision.vm

head -26 WebContent/productserver/templates/info.vm > WebContent/productserver/templates/info_tmp.vm

echo '<h3>Subversion Info:</h3>' >> WebContent/productserver/templates/info_tmp.vm
echo '<pre>' >> WebContent/productserver/templates/info_tmp.vm
svn info >> WebContent/productserver/templates/info_tmp.vm
if [ "$?" -ne "0" ]; then
       echo "Subversion not installed.  That is ok..."
       rm  WebContent/productserver/templates/info_tmp.vm
       exit 1
fi
echo '</pre>' >> WebContent/productserver/templates/info_tmp.vm
echo '</body>' >> WebContent/productserver/templates/info_tmp.vm
echo '</html>' >> WebContent/productserver/templates/info_tmp.vm
mv WebContent/productserver/templates/info_tmp.vm WebContent/productserver/templates/info.vm
