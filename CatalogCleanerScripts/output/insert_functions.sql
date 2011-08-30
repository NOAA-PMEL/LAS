CREATE OR REPLACE FUNCTION insert_catalog_dataset(p_catalog_id int, p_dataset_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_dataset("catalog_id", "dataset_id") values (p_catalog_id, p_dataset_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalog_property(p_catalog_id int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_property("catalog_id", "name", "value") values (p_catalog_id, p_name, p_value);
		select currval('catalog_property_catalog_property_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalog_service(p_catalog_id int, p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_service("catalog_id", "service_id") values (p_catalog_id, p_service_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalog_xlink(p_catalog_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_xlink("catalog_id", "value") values (p_catalog_id, p_value);
		select currval('catalog_xlink_catalog_xlink_id_seq') into id;
		BEGIN
			update catalog_xlink set "xlink" = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set "xlink_nonstandard" = p_xlink where catalog_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalogref() RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref("not_empty") values ('true');
		select currval('catalogref_catalogref_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalogref_documentation(p_catalogref_id int, p_value text, p_documentationenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation("catalogref_id", "value") values (p_catalogref_id, p_value);
		select currval('catalogref_documentation_catalogref_documentation_id_seq') into id;
		BEGIN
			update catalogref_documentation set "documentationenum" = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set "documentationenum_nonstandard" = p_documentationenum where catalogref_documentation_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalogref_documentation_namespace(p_catalogref_documentation_id int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation_namespace("catalogref_documentation_id", "namespace") values (p_catalogref_documentation_id, p_namespace);
		select currval('catalogref_documentation_namespace_catalogref_documentation_namespace_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalogref_documentation_xlink(p_catalogref_documentation_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation_xlink("catalogref_documentation_id", "value") values (p_catalogref_documentation_id, p_value);
		select currval('catalogref_documentation_xlink_catalogref_documentation_xlink_id_seq') into id;
		BEGIN
			update catalogref_documentation_xlink set "xlink" = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set "xlink_nonstandard" = p_xlink where catalogref_documentation_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_catalogref_xlink(p_catalogref_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_xlink("catalogref_id", "value") values (p_catalogref_id, p_value);
		select currval('catalogref_xlink_catalogref_xlink_id_seq') into id;
		BEGIN
			update catalogref_xlink set "xlink" = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set "xlink_nonstandard" = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset(p_alias text, p_authority text, p_d_id text, p_harvest text, p_name text, p_resourcecontrol text, p_serviceName text, p_urlPath text, p_collectiontype text, p_datasize_unit text, p_dataType text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset("alias", "authority", "d_id", "harvest", "name", "resourcecontrol", "serviceName", "urlPath") values (p_alias, p_authority, p_d_id, p_harvest, p_name, p_resourcecontrol, p_serviceName, p_urlPath);
		select currval('dataset_dataset_id_seq') into id;
		BEGIN
			update dataset set "collectiontype" = cast(p_collectiontype as collectiontype) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set "collectiontype_nonstandard" = p_collectiontype where dataset_id=id;
		END;
		BEGIN
			update dataset set "datasize_unit" = cast(p_datasize_unit as datasize_unit) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set "datasize_unit_nonstandard" = p_datasize_unit where dataset_id=id;
		END;
		BEGIN
			update dataset set "dataType" = cast(p_dataType as dataType) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set "dataType_nonstandard" = p_dataType where dataset_id=id;
		END;
		update dataset set "status" = cast(p_status as status) where dataset_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_access(p_dataset_id int, p_servicename text, p_urlpath text, p_dataformat text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_access("dataset_id", "servicename", "urlpath") values (p_dataset_id, p_servicename, p_urlpath);
		select currval('dataset_access_dataset_access_id_seq') into id;
		BEGIN
			update dataset_access set "dataformat" = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set "dataformat_nonstandard" = p_dataformat where dataset_access_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_access_datasize(p_dataset_access_id int, p_value text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_access_datasize("dataset_access_id", "value") values (p_dataset_access_id, p_value);
		select currval('dataset_access_datasize_dataset_access_datasize_id_seq') into id;
		BEGIN
			update dataset_access_datasize set "units" = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set "units_nonstandard" = p_units where dataset_access_datasize_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_catalogref(p_dataset_id int, p_catalogref_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_catalogref("dataset_id", "catalogref_id") values (p_dataset_id, p_catalogref_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_dataset(p_parent_id int, p_child_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_dataset("parent_id", "child_id") values (p_parent_id, p_child_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_ncml(p_dataset_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_ncml("dataset_id") values (p_dataset_id);
		select currval('dataset_ncml_dataset_ncml_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_property(p_dataset_id int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_property("dataset_id", "name", "value") values (p_dataset_id, p_name, p_value);
		select currval('dataset_property_dataset_property_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_service(p_dataset_id int, p_service_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_service("dataset_id", "service_id") values (p_dataset_id, p_service_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_tmg(p_dataset_id int, p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_tmg("dataset_id", "tmg_id") values (p_dataset_id, p_tmg_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_metadata(p_inherited text, p_metadatatype text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into metadata("not_empty") values ('true');
		select currval('metadata_metadata_id_seq') into id;
		BEGIN
			update metadata set "inherited" = cast(p_inherited as inherited) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set "inherited_nonstandard" = p_inherited where metadata_id=id;
		END;
		BEGIN
			update metadata set "metadatatype" = cast(p_metadatatype as metadatatype) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set "metadatatype_nonstandard" = p_metadatatype where metadata_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_metadata_namespace(p_metadata_id int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_namespace("metadata_id", "namespace") values (p_metadata_id, p_namespace);
		select currval('metadata_namespace_metadata_namespace_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_metadata_tmg(p_metadata_id int, p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_tmg("metadata_id", "tmg_id") values (p_metadata_id, p_tmg_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_metadata_xlink(p_metadata_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_xlink("metadata_id", "value") values (p_metadata_id, p_value);
		select currval('metadata_xlink_metadata_xlink_id_seq') into id;
		BEGIN
			update metadata_xlink set "xlink" = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set "xlink_nonstandard" = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service(p_base text, p_desc text, p_name text, p_suffix text, p_serviceType text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service("base", "desc", "name", "suffix") values (p_base, p_desc, p_name, p_suffix);
		select currval('service_service_id_seq') into id;
		BEGIN
			update service set "serviceType" = cast(p_serviceType as serviceType) where service_id=id;
		EXCEPTION
			when others then
				update service set "serviceType_nonstandard" = p_serviceType where service_id=id;
		END;
		update service set "status" = cast(p_status as status) where service_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service_datasetroot(p_service_id int, p_location text, p_path text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service_datasetroot("service_id", "location", "path") values (p_service_id, p_location, p_path);
		select currval('service_datasetroot_service_datasetroot_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service_property(p_service_id int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service_property("service_id", "name", "value") values (p_service_id, p_name, p_value);
		select currval('service_property_service_property_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service_service(p_parent_id int, p_child_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service_service("parent_id", "child_id") values (p_parent_id, p_child_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg() RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg("not_empty") values ('true');
		select currval('tmg_tmg_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_authority(p_tmg_id int, p_authority text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_authority("tmg_id", "authority") values (p_tmg_id, p_authority);
		select currval('tmg_authority_tmg_authority_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_contributor(p_tmg_id int, p_name text, p_role text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_contributor("tmg_id", "name", "role") values (p_tmg_id, p_name, p_role);
		select currval('tmg_contributor_tmg_contributor_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_creator(p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator("tmg_id") values (p_tmg_id);
		select currval('tmg_creator_tmg_creator_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_creator_contact(p_tmg_creator_id int, p_email text, p_url text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_contact("tmg_creator_id", "email", "url") values (p_tmg_creator_id, p_email, p_url);
		select currval('tmg_creator_contact_tmg_creator_contact_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_creator_name(p_tmg_creator_id int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_name("tmg_creator_id", "value", "vocabulary") values (p_tmg_creator_id, p_value, p_vocabulary);
		select currval('tmg_creator_name_tmg_creator_name_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_dataformat(p_tmg_id int, p_dataformat text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_dataformat("tmg_id") values (p_tmg_id);
		select currval('tmg_dataformat_tmg_dataformat_id_seq') into id;
		BEGIN
			update tmg_dataformat set "dataformat" = cast(p_dataformat as dataformat) where tmg_dataformat_id=id;
		EXCEPTION
			when others then
				update tmg_dataformat set "dataformat_nonstandard" = p_dataformat where tmg_dataformat_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_datasize(p_tmg_id int, p_value text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_datasize("tmg_id", "value") values (p_tmg_id, p_value);
		select currval('tmg_datasize_tmg_datasize_id_seq') into id;
		BEGIN
			update tmg_datasize set "units" = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set "units_nonstandard" = p_units where tmg_datasize_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_datatype(p_tmg_id int, p_datatype text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_datatype("tmg_id") values (p_tmg_id);
		select currval('tmg_datatype_tmg_datatype_id_seq') into id;
		BEGIN
			update tmg_datatype set "datatype" = cast(p_datatype as datatype) where tmg_datatype_id=id;
		EXCEPTION
			when others then
				update tmg_datatype set "datatype_nonstandard" = p_datatype where tmg_datatype_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_date(p_tmg_id int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_date("tmg_id", "format", "value") values (p_tmg_id, p_format, p_value);
		select currval('tmg_date_tmg_date_id_seq') into id;
		BEGIN
			update tmg_date set "dateenum" = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set "dateenum_nonstandard" = p_dateenum where tmg_date_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_documentation(p_tmg_id int, p_value text, p_documentationenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation("tmg_id", "value") values (p_tmg_id, p_value);
		select currval('tmg_documentation_tmg_documentation_id_seq') into id;
		BEGIN
			update tmg_documentation set "documentationenum" = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set "documentationenum_nonstandard" = p_documentationenum where tmg_documentation_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_documentation_namespace(p_tmg_documentation_id int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation_namespace("tmg_documentation_id", "namespace") values (p_tmg_documentation_id, p_namespace);
		select currval('tmg_documentation_namespace_tmg_documentation_namespace_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_documentation_xlink(p_tmg_documentation_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation_xlink("tmg_documentation_id", "value") values (p_tmg_documentation_id, p_value);
		select currval('tmg_documentation_xlink_tmg_documentation_xlink_id_seq') into id;
		BEGIN
			update tmg_documentation_xlink set "xlink" = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set "xlink_nonstandard" = p_xlink where tmg_documentation_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage(p_tmg_id int, p_upordown text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage("tmg_id") values (p_tmg_id);
		select currval('tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq') into id;
		BEGIN
			update tmg_geospatialcoverage set "upordown" = cast(p_upordown as upordown) where tmg_geospatialcoverage_id=id;
		EXCEPTION
			when others then
				update tmg_geospatialcoverage set "upordown_nonstandard" = p_upordown where tmg_geospatialcoverage_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id int, p_resolution text, p_size text, p_start text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_eastwest("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_eastwest_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_name("tmg_geospatialcoverage_id", "value", "vocabulary") values (p_tmg_geospatialcoverage_id, p_value, p_vocabulary);
		select currval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id int, p_resolution text, p_size text, p_start text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_northsouth("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_northsouth_tmg_geospatialcoverage_northsouth_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id int, p_resolution text, p_size text, p_start text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_updown("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_keyword(p_tmg_id int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_keyword("tmg_id", "value", "vocabulary") values (p_tmg_id, p_value, p_vocabulary);
		select currval('tmg_keyword_tmg_keyword_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_metadata(p_tmg_id int, p_metadata_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_metadata("tmg_id", "metadata_id") values (p_tmg_id, p_metadata_id);

		return 1;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_project(p_tmg_id int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_project("tmg_id", "value", "vocabulary") values (p_tmg_id, p_value, p_vocabulary);
		select currval('tmg_project_tmg_project_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_property(p_tmg_id int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_property("tmg_id", "name", "value") values (p_tmg_id, p_name, p_value);
		select currval('tmg_property_tmg_property_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_publisher(p_tmg_id int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher("tmg_id") values (p_tmg_id);
		select currval('tmg_publisher_tmg_publisher_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_publisher_contact(p_tmg_publisher_id int, p_email text, p_url text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_contact("tmg_publisher_id", "email", "url") values (p_tmg_publisher_id, p_email, p_url);
		select currval('tmg_publisher_contact_tmg_publisher_contact_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_publisher_name(p_tmg_publisher_id int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_name("tmg_publisher_id", "value", "vocabulary") values (p_tmg_publisher_id, p_value, p_vocabulary);
		select currval('tmg_publisher_name_tmg_publisher_name_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_servicename(p_tmg_id int, p_servicename text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_servicename("tmg_id", "servicename") values (p_tmg_id, p_servicename);
		select currval('tmg_servicename_tmg_servicename_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_timecoverage(p_tmg_id int, p_resolution text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage("tmg_id", "resolution") values (p_tmg_id, p_resolution);
		select currval('tmg_timecoverage_tmg_timecoverage_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_timecoverage_duration(p_tmg_timecoverage_id int, p_duration text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_duration("tmg_timecoverage_id", "duration") values (p_tmg_timecoverage_id, p_duration);
		select currval('tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_timecoverage_end(p_tmg_timecoverage_id int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_end("tmg_timecoverage_id", "format", "value") values (p_tmg_timecoverage_id, p_format, p_value);
		select currval('tmg_timecoverage_end_tmg_timecoverage_end_id_seq') into id;
		BEGIN
			update tmg_timecoverage_end set "dateenum" = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set "dateenum_nonstandard" = p_dateenum where tmg_timecoverage_end_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_timecoverage_resolution(p_tmg_timecoverage_id int, p_duration text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_resolution("tmg_timecoverage_id", "duration") values (p_tmg_timecoverage_id, p_duration);
		select currval('tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_timecoverage_start(p_tmg_timecoverage_id int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_start("tmg_timecoverage_id", "format", "value") values (p_tmg_timecoverage_id, p_format, p_value);
		select currval('tmg_timecoverage_start_tmg_timecoverage_start_id_seq') into id;
		BEGIN
			update tmg_timecoverage_start set "dateenum" = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set "dateenum_nonstandard" = p_dateenum where tmg_timecoverage_start_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_variables(p_tmg_id int, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables("tmg_id") values (p_tmg_id);
		select currval('tmg_variables_tmg_variables_id_seq') into id;
		BEGIN
			update tmg_variables set "vocabulary" = cast(p_vocabulary as vocabulary) where tmg_variables_id=id;
		EXCEPTION
			when others then
				update tmg_variables set "vocabulary_nonstandard" = p_vocabulary where tmg_variables_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_variables_variable(p_tmg_variables_id int, p_name text, p_units text, p_vocabulary_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variable("tmg_variables_id", "name", "units", "vocabulary_name") values (p_tmg_variables_id, p_name, p_units, p_vocabulary_name);
		select currval('tmg_variables_variable_tmg_variables_variable_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_variables_variablemap(p_tmg_variables_id int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variablemap("tmg_variables_id", "value") values (p_tmg_variables_id, p_value);
		select currval('tmg_variables_variablemap_tmg_variables_variablemap_id_seq') into id;
		BEGIN
			update tmg_variables_variablemap set "xlink" = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set "xlink_nonstandard" = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

