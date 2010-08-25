java -jar ../../compiler.jar --compilation_level=WHITESPACE_ONLY --js=ui/LASUI.js --js_output_file=ui/LASUI_compiled.js
#cp ui/LASUI.js ui/LASUI_compiled.js
java -jar ../../compiler.jar --js=components/xmldom.js --js=components/LASRequest.js --js=components/DateWidget.js --js=components/LASGetGridResponse.js --js=components/LASGetCategoriesResponse.js --js=components/LASGetViewsResponse.js --js=components/LASGetOperationsResponse.js --js=components/LASGetOptionsResponse.js --js=components/LASGetDataConstraintsResponse.js --js_output_file=ui/LASUI_compiled_components.js
