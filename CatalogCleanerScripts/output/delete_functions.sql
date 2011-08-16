CREATE OR REPLACE FUNCTION delete_catalog(p_catalog_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalog_id into id from catalog where catalog_id=p_catalog_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalog where catalog_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalog_dataset(p_catalog_id int, p_dataset_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from catalog_dataset where catalog_id=p_catalog_id and dataset_id=p_dataset_id;
	if(id=0) then
		return -1;
	end if;
	delete from catalog_dataset where catalog_id=p_catalog_id and dataset_id=p_dataset_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalog_property(p_catalog_property_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalog_property_id into id from catalog_property where catalog_property_id=p_catalog_property_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalog_property where catalog_property_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalog_service(p_catalog_id int, p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from catalog_service where catalog_id=p_catalog_id and service_id=p_service_id;
	if(id=0) then
		return -1;
	end if;
	delete from catalog_service where catalog_id=p_catalog_id and service_id=p_service_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalog_xlink(p_catalog_xlink_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalog_xlink_id into id from catalog_xlink where catalog_xlink_id=p_catalog_xlink_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalog_xlink where catalog_xlink_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalogref(p_catalogref_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalogref_id into id from catalogref where catalogref_id=p_catalogref_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalogref where catalogref_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalogref_documentation(p_catalogref_documentation_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalogref_documentation_id into id from catalogref_documentation where catalogref_documentation_id=p_catalogref_documentation_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalogref_documentation where catalogref_documentation_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalogref_documentation_namespace(p_catalogref_documentation_namespace_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalogref_documentation_namespace_id into id from catalogref_documentation_namespace where catalogref_documentation_namespace_id=p_catalogref_documentation_namespace_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalogref_documentation_namespace where catalogref_documentation_namespace_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalogref_documentation_xlink(p_catalogref_documentation_xlink_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalogref_documentation_xlink_id into id from catalogref_documentation_xlink where catalogref_documentation_xlink_id=p_catalogref_documentation_xlink_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalogref_documentation_xlink where catalogref_documentation_xlink_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_catalogref_xlink(p_catalogref_xlink_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select catalogref_xlink_id into id from catalogref_xlink where catalogref_xlink_id=p_catalogref_xlink_id;
	if(id is null) then
		return -1;
	end if;
	delete from catalogref_xlink where catalogref_xlink_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset(p_dataset_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select dataset_id into id from dataset where dataset_id=p_dataset_id;
	if(id is null) then
		return -1;
	end if;
	delete from dataset where dataset_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_access(p_dataset_access_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select dataset_access_id into id from dataset_access where dataset_access_id=p_dataset_access_id;
	if(id is null) then
		return -1;
	end if;
	delete from dataset_access where dataset_access_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_access_datasize(p_dataset_access_datasize_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select dataset_access_datasize_id into id from dataset_access_datasize where dataset_access_datasize_id=p_dataset_access_datasize_id;
	if(id is null) then
		return -1;
	end if;
	delete from dataset_access_datasize where dataset_access_datasize_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_catalogref(p_dataset_id int, p_catalogref_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from dataset_catalogref where dataset_id=p_dataset_id and catalogref_id=p_catalogref_id;
	if(id=0) then
		return -1;
	end if;
	delete from dataset_catalogref where dataset_id=p_dataset_id and catalogref_id=p_catalogref_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_dataset(p_parent_id int, p_child_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from dataset_dataset where parent_id=p_parent_id and child_id=p_child_id;
	if(id=0) then
		return -1;
	end if;
	delete from dataset_dataset where parent_id=p_parent_id and child_id=p_child_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_ncml(p_dataset_ncml_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select dataset_ncml_id into id from dataset_ncml where dataset_ncml_id=p_dataset_ncml_id;
	if(id is null) then
		return -1;
	end if;
	delete from dataset_ncml where dataset_ncml_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_property(p_dataset_property_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select dataset_property_id into id from dataset_property where dataset_property_id=p_dataset_property_id;
	if(id is null) then
		return -1;
	end if;
	delete from dataset_property where dataset_property_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_service(p_dataset_id int, p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from dataset_service where dataset_id=p_dataset_id and service_id=p_service_id;
	if(id=0) then
		return -1;
	end if;
	delete from dataset_service where dataset_id=p_dataset_id and service_id=p_service_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_dataset_tmg(p_dataset_id int, p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from dataset_tmg where dataset_id=p_dataset_id and tmg_id=p_tmg_id;
	if(id=0) then
		return -1;
	end if;
	delete from dataset_tmg where dataset_id=p_dataset_id and tmg_id=p_tmg_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_metadata(p_metadata_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select metadata_id into id from metadata where metadata_id=p_metadata_id;
	if(id is null) then
		return -1;
	end if;
	delete from metadata where metadata_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_metadata_namespace(p_metadata_namespace_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select metadata_namespace_id into id from metadata_namespace where metadata_namespace_id=p_metadata_namespace_id;
	if(id is null) then
		return -1;
	end if;
	delete from metadata_namespace where metadata_namespace_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_metadata_tmg(p_metadata_id int, p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from metadata_tmg where metadata_id=p_metadata_id and tmg_id=p_tmg_id;
	if(id=0) then
		return -1;
	end if;
	delete from metadata_tmg where metadata_id=p_metadata_id and tmg_id=p_tmg_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_metadata_xlink(p_metadata_xlink_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select metadata_xlink_id into id from metadata_xlink where metadata_xlink_id=p_metadata_xlink_id;
	if(id is null) then
		return -1;
	end if;
	delete from metadata_xlink where metadata_xlink_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_service(p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select service_id into id from service where service_id=p_service_id;
	if(id is null) then
		return -1;
	end if;
	delete from service where service_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_service_datasetroot(p_service_datasetroot_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select service_datasetroot_id into id from service_datasetroot where service_datasetroot_id=p_service_datasetroot_id;
	if(id is null) then
		return -1;
	end if;
	delete from service_datasetroot where service_datasetroot_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_service_property(p_service_property_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select service_property_id into id from service_property where service_property_id=p_service_property_id;
	if(id is null) then
		return -1;
	end if;
	delete from service_property where service_property_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_service_service(p_parent_id int, p_child_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from service_service where parent_id=p_parent_id and child_id=p_child_id;
	if(id=0) then
		return -1;
	end if;
	delete from service_service where parent_id=p_parent_id and child_id=p_child_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg(p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_id into id from tmg where tmg_id=p_tmg_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg where tmg_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_authority(p_tmg_authority_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_authority_id into id from tmg_authority where tmg_authority_id=p_tmg_authority_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_authority where tmg_authority_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_contributor(p_tmg_contributor_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_contributor_id into id from tmg_contributor where tmg_contributor_id=p_tmg_contributor_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_contributor where tmg_contributor_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_creator(p_tmg_creator_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_creator_id into id from tmg_creator where tmg_creator_id=p_tmg_creator_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_creator where tmg_creator_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_creator_contact(p_tmg_creator_contact_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_creator_contact_id into id from tmg_creator_contact where tmg_creator_contact_id=p_tmg_creator_contact_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_creator_contact where tmg_creator_contact_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_creator_name(p_tmg_creator_name_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_creator_name_id into id from tmg_creator_name where tmg_creator_name_id=p_tmg_creator_name_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_creator_name where tmg_creator_name_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_dataformat(p_tmg_dataformat_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_dataformat_id into id from tmg_dataformat where tmg_dataformat_id=p_tmg_dataformat_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_dataformat where tmg_dataformat_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_datasize(p_tmg_datasize_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_datasize_id into id from tmg_datasize where tmg_datasize_id=p_tmg_datasize_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_datasize where tmg_datasize_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_datatype(p_tmg_datatype_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_datatype_id into id from tmg_datatype where tmg_datatype_id=p_tmg_datatype_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_datatype where tmg_datatype_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_date(p_tmg_date_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_date_id into id from tmg_date where tmg_date_id=p_tmg_date_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_date where tmg_date_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_documentation(p_tmg_documentation_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_documentation_id into id from tmg_documentation where tmg_documentation_id=p_tmg_documentation_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_documentation where tmg_documentation_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_documentation_namespace(p_tmg_documentation_namespace_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_documentation_namespace_id into id from tmg_documentation_namespace where tmg_documentation_namespace_id=p_tmg_documentation_namespace_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_documentation_namespace where tmg_documentation_namespace_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_documentation_xlink(p_tmg_documentation_xlink_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_documentation_xlink_id into id from tmg_documentation_xlink where tmg_documentation_xlink_id=p_tmg_documentation_xlink_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_documentation_xlink where tmg_documentation_xlink_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_geospatialcoverage(p_tmg_geospatialcoverage_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_geospatialcoverage_id into id from tmg_geospatialcoverage where tmg_geospatialcoverage_id=p_tmg_geospatialcoverage_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_geospatialcoverage where tmg_geospatialcoverage_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_eastwest_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_geospatialcoverage_eastwest_id into id from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=p_tmg_geospatialcoverage_eastwest_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_name_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_geospatialcoverage_name_id into id from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=p_tmg_geospatialcoverage_name_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_northsouth_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_geospatialcoverage_northsouth_id into id from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=p_tmg_geospatialcoverage_northsouth_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_updown_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_geospatialcoverage_updown_id into id from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=p_tmg_geospatialcoverage_updown_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_keyword(p_tmg_keyword_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_keyword_id into id from tmg_keyword where tmg_keyword_id=p_tmg_keyword_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_keyword where tmg_keyword_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_metadata(p_tmg_id int, p_metadata_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select count(*) into id from tmg_metadata where tmg_id=p_tmg_id and metadata_id=p_metadata_id;
	if(id=0) then
		return -1;
	end if;
	delete from tmg_metadata where tmg_id=p_tmg_id and metadata_id=p_metadata_id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_project(p_tmg_project_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_project_id into id from tmg_project where tmg_project_id=p_tmg_project_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_project where tmg_project_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_property(p_tmg_property_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_property_id into id from tmg_property where tmg_property_id=p_tmg_property_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_property where tmg_property_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_publisher(p_tmg_publisher_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_publisher_id into id from tmg_publisher where tmg_publisher_id=p_tmg_publisher_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_publisher where tmg_publisher_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_publisher_contact(p_tmg_publisher_contact_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_publisher_contact_id into id from tmg_publisher_contact where tmg_publisher_contact_id=p_tmg_publisher_contact_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_publisher_contact where tmg_publisher_contact_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_publisher_name(p_tmg_publisher_name_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_publisher_name_id into id from tmg_publisher_name where tmg_publisher_name_id=p_tmg_publisher_name_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_publisher_name where tmg_publisher_name_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_servicename(p_tmg_servicename_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_servicename_id into id from tmg_servicename where tmg_servicename_id=p_tmg_servicename_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_servicename where tmg_servicename_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_timecoverage(p_tmg_timecoverage_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_timecoverage_id into id from tmg_timecoverage where tmg_timecoverage_id=p_tmg_timecoverage_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_timecoverage where tmg_timecoverage_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_timecoverage_duration(p_tmg_timecoverage_duration_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_timecoverage_duration_id into id from tmg_timecoverage_duration where tmg_timecoverage_duration_id=p_tmg_timecoverage_duration_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_timecoverage_duration where tmg_timecoverage_duration_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_timecoverage_end(p_tmg_timecoverage_end_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_timecoverage_end_id into id from tmg_timecoverage_end where tmg_timecoverage_end_id=p_tmg_timecoverage_end_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_timecoverage_end where tmg_timecoverage_end_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_timecoverage_resolution(p_tmg_timecoverage_resolution_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_timecoverage_resolution_id into id from tmg_timecoverage_resolution where tmg_timecoverage_resolution_id=p_tmg_timecoverage_resolution_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_timecoverage_resolution where tmg_timecoverage_resolution_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_timecoverage_start(p_tmg_timecoverage_start_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_timecoverage_start_id into id from tmg_timecoverage_start where tmg_timecoverage_start_id=p_tmg_timecoverage_start_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_timecoverage_start where tmg_timecoverage_start_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_variables(p_tmg_variables_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_variables_id into id from tmg_variables where tmg_variables_id=p_tmg_variables_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_variables where tmg_variables_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_variables_variable(p_tmg_variables_variable_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_variables_variable_id into id from tmg_variables_variable where tmg_variables_variable_id=p_tmg_variables_variable_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_variables_variable where tmg_variables_variable_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_tmg_variables_variablemap(p_tmg_variables_variablemap_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
	select tmg_variables_variablemap_id into id from tmg_variables_variablemap where tmg_variables_variablemap_id=p_tmg_variables_variablemap_id;
	if(id is null) then
		return -1;
	end if;
	delete from tmg_variables_variablemap where tmg_variables_variablemap_id=id;

	return id;
END;
$$ LANGUAGE plpgsql;

