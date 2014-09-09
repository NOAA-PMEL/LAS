#

.PHONY: configure
configure:
	configure

.PHONY: clean-ant
clean-ant:
	ant clean

.PHONY: clean-noant
clean-noant:
	rm -rf velocity.log
	rm -rf config.results
	rm -rf config7.results
	rm -rf bin/las_ui_check.sh
	rm -rf bin/initialize_check.sh
	rm -rf conf/example/LAS_Config.pl
	rm -rf conf/example/sample_las.xml
	rm -rf conf/example/sample_ui.xml
	rm -rf conf/example/sample_insitu_las.xml
	rm -rf conf/example/sample_insitu_ui.xml
	rm -rf conf/example/productserver.xml
	rm -rf conf/example/LAS_config.pl
	rm -rf startserver.sh rebootserver.sh stopserver.sh
	rm -rf JavaSource/resources/ferret/FerretBackendConfig.xml
	rm -rf JavaSource/resources/database/DatabaseBackendConfig.xml
	rm -rf JavaSource/resources/ferret/FerretBackendConfig.xml.base
	rm -rf JavaSource/resources/kml/KMLBackendConfig.xml
	rm -rf WebContent/TestLinks.html
	rm -rf WebContent/classes
	rm -rf WebContent/docs
	rm -rf WebContent/WEB-INF/classes
	rm -rf WebContent/WEB-INF/struts-config.xml
	rm -rf WebContent/WEB-INF/web.xml
	rm -rf xml/perl/genLas.pl
	rm -rf build.xml
	rm -rf test/LASTest/las_test_config.xml

.PHONY: clean-more
clean-more:
	rm -f  bin/addXML.sh
	rm -f  bin/addDiscrete.sh
	rm -f  bin/lasTest.sh
	rm -fr conf/server
	rm -fr WebContent/JavaScript/gwt-unitCache
	rm -fr WebContent/JavaScript/components/WEB-INF
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.InteractiveDownloadData
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.PPT
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.InteractiveDownloadDataJUnit
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.DateWidgetTest
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.Correlation
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.SPPV
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.ColumnEditor
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.UI
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.HelpMenu
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.ClimateAnalysis
	rm -fr WebContent/JavaScript/components/gov.noaa.pmel.tmap.las.NativeMapWidget
	rm -fr build

.PHONY: clean 
clean: clean-ant clean-noant

.PHONY: ultra-clean
ultra-clean: clean-ant clean-noant clean-more

