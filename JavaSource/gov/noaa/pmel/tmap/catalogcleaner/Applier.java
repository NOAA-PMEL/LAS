package gov.noaa.pmel.tmap.catalogcleaner;

import gov.noaa.pmel.tmap.catalogcleaner.Datavalue;
import gov.noaa.pmel.tmap.catalogcleaner.data.*;

import java.util.List;

import org.jdom.*;

import thredds.catalog.InvCatalog;
// TODO: replace with a real rules engine
public class Applier {

	public static Catalog applyCatalogRules(Catalog cleanCatalog) throws Exception{


		return cleanCatalog;
	}
	public static Service applyCatalogServiceRules(int cleanCatalogId, Service cleanService) throws Exception{

		// begin rules
		if(!cleanService.getServiceType().equals("COMPOUND")){	// todo: this would be a rule applied
			cleanService = new Service(cleanService.getServiceId());
			cleanService.setName("ct2009");		// TODO: dynamically generate new name, determine other fields
			cleanService.setServiceType("COMPOUND");
			cleanService.setBase("");
			cleanService.setStatus("new");

			addCompoundServices(cleanService.getServiceId());
		}
		else{

		}
		// end rules
		return cleanService;
	}
	// this would be defined in a rule
	public static void addCompoundServices(int serviceId) throws Exception{
		Service newChild = new Service();
		newChild.setName("odap");
		newChild.setServiceType("OpenDAP");
		newChild.setBase("http://dunkel.pmel.noaa.gov:8780/thredds/dodsC/");
		newChild.setStatus("new");
		int childId = DataAccess.insertService(newChild);
		DataAccess.insertServiceService(serviceId, childId);

		Service newChild2 = new Service();
		newChild2.setName("http");
		newChild2.setServiceType("HTTPServer");
		newChild2.setBase("/thredds/fileServer/");	// TODO: dynamically determine which services are available on their server, use them
		newChild2.setStatus("new");
		int childId2 = DataAccess.insertService(newChild2);
		DataAccess.insertServiceService(serviceId, childId2);

		Service newChild3 = new Service();
		newChild3.setName("wms");
		newChild3.setServiceType("WMS");
		newChild3.setBase("/thredds/wms/");	// TODO: dynamically determine which services are available on their server, use them
		newChild3.setStatus("new");
		int childId3 = DataAccess.insertService(newChild2);
		DataAccess.insertServiceService(serviceId, childId3);
	}
	public static Service applyServiceServiceRules(int parentId, Service child) throws Exception{
		// apply rules

		return child;
	}
	public static Dataset applyCatalogDatasetRules(int parentId, Dataset child) throws Exception{

		return child;
	}
	public static Tmg applyDatasetTmgRules(int parentId, Tmg child) throws Exception{
		// nothing to check here, just insert new tmg for this dataset

		return child;
	}
	public static Metadata applyTmgMetadataRules(int parentId, Metadata child) throws Exception{

		return child;
	}
	public static TmgDocumentation applyTmgDocumentationRules(int parentId, TmgDocumentation child) throws Exception{

		return child;
	}
	public static TmgCreator applyTmgCreatorRules(int parentId, TmgCreator child) throws Exception{

		return child;
	}
	public static TmgCreatorName applyTmgCreatorNameRules(int parentId, TmgCreatorName child) throws Exception{

		return child;

	}
	public static TmgCreatorContact applyTmgCreatorContactRules(int parentId, TmgCreatorContact child) throws Exception{

		return child;
	}
	public static Tmg applyMetadataTmgRules(int parentId, Tmg child) throws Exception {

		return child;
	}
	public static TmgGeospatialcoverageUpdown applyTmgGeospatialcoverageUpdownRules(int parentId, TmgGeospatialcoverageUpdown child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgGeospatialcoverageNorthsouth applyTmgGeospatialcoverageNorthsouthRules(int parentId, TmgGeospatialcoverageNorthsouth child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgGeospatialcoverageName applyTmgGeospatialcoverageNameRules(int parentId, TmgGeospatialcoverageName child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgGeospatialcoverageEastwest applyTmgGeospatialcoverageEastwestRules(int parentId, TmgGeospatialcoverageEastwest child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgVariables applyTmgVariablesRules(int parentId, TmgVariables child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgGeospatialcoverage applyTmgGeospatialcoverageRules(int parentId, TmgGeospatialcoverage child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgKeyword applyTmgKeywordRules(int parentId, TmgKeyword child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgProject applyTmgProjectRules(int parentId, TmgProject child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgPublisher applyTmgPublisherRules(int parentId, TmgPublisher child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgTimecoverage applyTmgTimecoverageRules(int parentId, TmgTimecoverage child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgServicename applyTmgServicenameRules(int parentId, TmgServicename child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgProperty applyTmgPropertyRules(int parentId, TmgProperty child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDate applyTmgDateRules(int parentId, TmgDate child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDatatype applyTmgDatatypeRules(int parentId, TmgDatatype child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDatasize applyTmgDatasizeRules(int parentId, TmgDatasize child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDataformat applyTmgDataformatRules(int parentId, TmgDataformat child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgContributor applyTmgContributorRules(int parentId, TmgContributor child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgAuthority applyTmgAuthorityRules(int parentId, TmgAuthority child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDocumentationXlink applyTmgDocumentationXlinkRules(int parentId, TmgDocumentationXlink child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgDocumentationNamespace applyTmgDocumentationNamespaceRules(int parentId, TmgDocumentationNamespace child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static DatasetTmg applyDatasetTmgRules(int parentId, DatasetTmg child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static Service applyDatasetServiceRules(int parentId, Service child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static DatasetProperty applyDatasetPropertyRules(int parentId, DatasetProperty child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static DatasetNcml applyDatasetNcmlRules(int parentId, DatasetNcml child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static Dataset applyDatasetDatasetRules(int parentId, Dataset child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static DatasetAccess applyDatasetAccessRules(int parentId, DatasetAccess child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgPublisherName applyTmgPublisherNameRules(int parentId, TmgPublisherName child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgPublisherContact applyTmgPublisherContactRules(int parentId, TmgPublisherContact child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogrefDocumentationXlink applyCatalogrefDocumentationXlinkRules(int parentId, CatalogrefDocumentationXlink child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogrefDocumentationNamespace applyCatalogrefDocumentationNamespaceRules(int parentId, CatalogrefDocumentationNamespace child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogrefXlink applyCatalogrefXlinkRules(int parentId, CatalogrefXlink child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogrefDocumentation applyCatalogrefDocumentationRules(int parentId, CatalogrefDocumentation child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogXlink applyCatalogXlinkRules(int parentId, CatalogXlink child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static CatalogProperty applyCatalogPropertyRules(int parentId, CatalogProperty child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static DatasetAccessDatasize applyDatasetAccessDatasizeRules(int parentId, DatasetAccessDatasize child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static ServiceProperty applyServicePropertyRules(int parentId, ServiceProperty child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static ServiceDatasetroot applyServiceDatasetrootRules(int parentId, ServiceDatasetroot child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgVariablesVariablemap applyTmgVariablesVariablemapRules(int parentId, TmgVariablesVariablemap child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgVariablesVariable applyTmgVariablesVariableRules(int parentId, TmgVariablesVariable child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static MetadataXlink applyMetadataXlinkRules(int parentId, MetadataXlink child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static MetadataTmg applyMetadataTmgRules(int parentId, MetadataTmg child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static MetadataNamespace applyMetadataNamespaceRules(int parentId, MetadataNamespace child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgTimecoverageStart applyTmgTimecoverageStartRules(int parentId, TmgTimecoverageStart child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgTimecoverageResolution applyTmgTimecoverageResolutionRules(int parentId, TmgTimecoverageResolution child) {

		// TODO Auto-generated method stub
		return child;
	}
	public static TmgTimecoverageEnd applyTmgTimecoverageEndRules(int parentId, TmgTimecoverageEnd child) {
		// TODO Auto-generated method stub
		return child;
	}
	public static TmgTimecoverageDuration applyTmgTimecoverageDurationRules(int parentId, TmgTimecoverageDuration child) {
		// TODO Auto-generated method stub
		return child;
	}
}
