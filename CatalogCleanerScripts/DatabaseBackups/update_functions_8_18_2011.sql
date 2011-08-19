DROP FUNCTION IF EXISTS insert_catalog(text, text, text, text, text, text);
DROP FUNCTION IF EXISTS update_catalog(int, text, text, text, text, text, text);
DROP FUNCTION IF EXISTS delete_catalog(int);
DROP FUNCTION IF EXISTS insert_catalog_dataset(int, int);
DROP FUNCTION IF EXISTS delete_catalog_dataset(int, int);
DROP FUNCTION IF EXISTS insert_catalog_property(text, text, int);
DROP FUNCTION IF EXISTS update_catalog_property(int, text, text, int);
DROP FUNCTION IF EXISTS delete_catalog_property(int);
DROP FUNCTION IF EXISTS insert_catalog_service(int, int);
DROP FUNCTION IF EXISTS delete_catalog_service(int, int);
DROP FUNCTION IF EXISTS insert_catalog_xlink(text, text, int);
DROP FUNCTION IF EXISTS update_catalog_xlink(int, text, text, int);
DROP FUNCTION IF EXISTS delete_catalog_xlink(int);
DROP FUNCTION IF EXISTS insert_catalogref(int, int);
DROP FUNCTION IF EXISTS update_catalogref(int, int, int);
DROP FUNCTION IF EXISTS delete_catalogref(int);
DROP FUNCTION IF EXISTS insert_catalogref_documentation(text, text, int);
DROP FUNCTION IF EXISTS update_catalogref_documentation(int, text, text, int);
DROP FUNCTION IF EXISTS delete_catalogref_documentation(int);
DROP FUNCTION IF EXISTS insert_catalogref_documentation_namespace(text, int);
DROP FUNCTION IF EXISTS update_catalogref_documentation_namespace(int, text, int);
DROP FUNCTION IF EXISTS delete_catalogref_documentation_namespace(int);
DROP FUNCTION IF EXISTS insert_catalogref_documentation_xlink(text, text, int);
DROP FUNCTION IF EXISTS update_catalogref_documentation_xlink(int, text, text, int);
DROP FUNCTION IF EXISTS delete_catalogref_documentation_xlink(int);
DROP FUNCTION IF EXISTS insert_catalogref_xlink(text, text, int);
DROP FUNCTION IF EXISTS update_catalogref_xlink(int, text, text, int);
DROP FUNCTION IF EXISTS delete_catalogref_xlink(int);
DROP FUNCTION IF EXISTS insert_dataset(text, text, text, text, text, text, text, text, text, text, text, text);
DROP FUNCTION IF EXISTS update_dataset(int, text, text, text, text, text, text, text, text, text, text, text, text);
DROP FUNCTION IF EXISTS delete_dataset(int);
DROP FUNCTION IF EXISTS insert_dataset_access(text, text, text, int);
DROP FUNCTION IF EXISTS update_dataset_access(int, text, text, text, int);
DROP FUNCTION IF EXISTS delete_dataset_access(int);
DROP FUNCTION IF EXISTS insert_dataset_access_datasize(text, text, int);
DROP FUNCTION IF EXISTS update_dataset_access_datasize(int, text, text, int);
DROP FUNCTION IF EXISTS delete_dataset_access_datasize(int);
DROP FUNCTION IF EXISTS insert_dataset_catalogref(int, int);
DROP FUNCTION IF EXISTS delete_dataset_catalogref(int, int);
DROP FUNCTION IF EXISTS insert_dataset_dataset(int, int);
DROP FUNCTION IF EXISTS delete_dataset_dataset(int, int);
DROP FUNCTION IF EXISTS insert_dataset_ncml(int);
DROP FUNCTION IF EXISTS update_dataset_ncml(int, int);
DROP FUNCTION IF EXISTS delete_dataset_ncml(int);
DROP FUNCTION IF EXISTS insert_dataset_property(text, text, int);
DROP FUNCTION IF EXISTS update_dataset_property(int, text, text, int);
DROP FUNCTION IF EXISTS delete_dataset_property(int);
DROP FUNCTION IF EXISTS insert_dataset_service(int, int);
DROP FUNCTION IF EXISTS delete_dataset_service(int, int);
DROP FUNCTION IF EXISTS insert_dataset_tmg(int, int);
DROP FUNCTION IF EXISTS delete_dataset_tmg(int, int);
DROP FUNCTION IF EXISTS insert_metadata(text, text);
DROP FUNCTION IF EXISTS update_metadata(int, text, text);
DROP FUNCTION IF EXISTS delete_metadata(int);
DROP FUNCTION IF EXISTS insert_metadata_namespace(text, int);
DROP FUNCTION IF EXISTS update_metadata_namespace(int, text, int);
DROP FUNCTION IF EXISTS delete_metadata_namespace(int);
DROP FUNCTION IF EXISTS insert_metadata_tmg(int, int);
DROP FUNCTION IF EXISTS delete_metadata_tmg(int, int);
DROP FUNCTION IF EXISTS insert_metadata_xlink(text, text, int);
DROP FUNCTION IF EXISTS update_metadata_xlink(int, text, text, int);
DROP FUNCTION IF EXISTS delete_metadata_xlink(int);
DROP FUNCTION IF EXISTS insert_service(text, text, text, text, text, text);
DROP FUNCTION IF EXISTS update_service(int, text, text, text, text, text, text);
DROP FUNCTION IF EXISTS delete_service(int);
DROP FUNCTION IF EXISTS insert_service_datasetroot(text, text, int);
DROP FUNCTION IF EXISTS update_service_datasetroot(int, text, text, int);
DROP FUNCTION IF EXISTS delete_service_datasetroot(int);
DROP FUNCTION IF EXISTS insert_service_property(text, text, int);
DROP FUNCTION IF EXISTS update_service_property(int, text, text, int);
DROP FUNCTION IF EXISTS delete_service_property(int);
DROP FUNCTION IF EXISTS insert_service_service(int, int);
DROP FUNCTION IF EXISTS delete_service_service(int, int);
DROP FUNCTION IF EXISTS insert_tmg();
DROP FUNCTION IF EXISTS update_tmg(int);
DROP FUNCTION IF EXISTS delete_tmg(int);
DROP FUNCTION IF EXISTS insert_tmg_authority(text, int);
DROP FUNCTION IF EXISTS update_tmg_authority(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_authority(int);
DROP FUNCTION IF EXISTS insert_tmg_contributor(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_contributor(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_contributor(int);
DROP FUNCTION IF EXISTS insert_tmg_creator(int);
DROP FUNCTION IF EXISTS update_tmg_creator(int, int);
DROP FUNCTION IF EXISTS delete_tmg_creator(int);
DROP FUNCTION IF EXISTS insert_tmg_creator_contact(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_creator_contact(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_creator_contact(int);
DROP FUNCTION IF EXISTS insert_tmg_creator_name(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_creator_name(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_creator_name(int);
DROP FUNCTION IF EXISTS insert_tmg_dataformat(text, int);
DROP FUNCTION IF EXISTS update_tmg_dataformat(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_dataformat(int);
DROP FUNCTION IF EXISTS insert_tmg_datasize(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_datasize(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_datasize(int);
DROP FUNCTION IF EXISTS insert_tmg_datatype(text, int);
DROP FUNCTION IF EXISTS update_tmg_datatype(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_datatype(int);
DROP FUNCTION IF EXISTS insert_tmg_date(text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_date(int, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_date(int);
DROP FUNCTION IF EXISTS insert_tmg_documentation(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_documentation(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_documentation(int);
DROP FUNCTION IF EXISTS insert_tmg_documentation_namespace(text, int);
DROP FUNCTION IF EXISTS update_tmg_documentation_namespace(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_documentation_namespace(int);
DROP FUNCTION IF EXISTS insert_tmg_documentation_xlink(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_documentation_xlink(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_documentation_xlink(int);
DROP FUNCTION IF EXISTS insert_tmg_geospatialcoverage(text, int);
DROP FUNCTION IF EXISTS update_tmg_geospatialcoverage(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_geospatialcoverage(int);
DROP FUNCTION IF EXISTS insert_tmg_geospatialcoverage_eastwest(text, text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_geospatialcoverage_eastwest(int, text, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_geospatialcoverage_eastwest(int);
DROP FUNCTION IF EXISTS insert_tmg_geospatialcoverage_name(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_geospatialcoverage_name(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_geospatialcoverage_name(int);
DROP FUNCTION IF EXISTS insert_tmg_geospatialcoverage_northsouth(text, text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_geospatialcoverage_northsouth(int, text, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_geospatialcoverage_northsouth(int);
DROP FUNCTION IF EXISTS insert_tmg_geospatialcoverage_updown(text, text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_geospatialcoverage_updown(int, text, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_geospatialcoverage_updown(int);
DROP FUNCTION IF EXISTS insert_tmg_keyword(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_keyword(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_keyword(int);
DROP FUNCTION IF EXISTS insert_tmg_metadata(int, int);
DROP FUNCTION IF EXISTS delete_tmg_metadata(int, int);
DROP FUNCTION IF EXISTS insert_tmg_project(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_project(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_project(int);
DROP FUNCTION IF EXISTS insert_tmg_property(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_property(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_property(int);
DROP FUNCTION IF EXISTS insert_tmg_publisher(int);
DROP FUNCTION IF EXISTS update_tmg_publisher(int, int);
DROP FUNCTION IF EXISTS delete_tmg_publisher(int);
DROP FUNCTION IF EXISTS insert_tmg_publisher_contact(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_publisher_contact(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_publisher_contact(int);
DROP FUNCTION IF EXISTS insert_tmg_publisher_name(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_publisher_name(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_publisher_name(int);
DROP FUNCTION IF EXISTS insert_tmg_servicename(text, int);
DROP FUNCTION IF EXISTS update_tmg_servicename(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_servicename(int);
DROP FUNCTION IF EXISTS insert_tmg_timecoverage(text, int);
DROP FUNCTION IF EXISTS update_tmg_timecoverage(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_timecoverage(int);
DROP FUNCTION IF EXISTS insert_tmg_timecoverage_duration(text, int);
DROP FUNCTION IF EXISTS update_tmg_timecoverage_duration(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_timecoverage_duration(int);
DROP FUNCTION IF EXISTS insert_tmg_timecoverage_end(text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_timecoverage_end(int, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_timecoverage_end(int);
DROP FUNCTION IF EXISTS insert_tmg_timecoverage_resolution(text, int);
DROP FUNCTION IF EXISTS update_tmg_timecoverage_resolution(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_timecoverage_resolution(int);
DROP FUNCTION IF EXISTS insert_tmg_timecoverage_start(text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_timecoverage_start(int, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_timecoverage_start(int);
DROP FUNCTION IF EXISTS insert_tmg_variables(text, int);
DROP FUNCTION IF EXISTS update_tmg_variables(int, text, int);
DROP FUNCTION IF EXISTS delete_tmg_variables(int);
DROP FUNCTION IF EXISTS insert_tmg_variables_variable(text, text, text, int);
DROP FUNCTION IF EXISTS update_tmg_variables_variable(int, text, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_variables_variable(int);
DROP FUNCTION IF EXISTS insert_tmg_variables_variablemap(text, text, int);
DROP FUNCTION IF EXISTS update_tmg_variables_variablemap(int, text, text, int);
DROP FUNCTION IF EXISTS delete_tmg_variables_variablemap(int);

ALTER TABLE catalogref
   ADD COLUMN not_empty text DEFAULT 'true';



CREATE OR REPLACE FUNCTION insert_catalog(p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into catalog("name", "expires", "version", "base", "xmlns") values (p_name, p_expires, p_version, p_base, p_xmlns);
		select currval('catalog_catalog_id_seq') into id;
		update catalog set status = cast(p_status as status) where catalog_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

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
			update catalog_xlink set xlink = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set xlink_nonstandard = p_xlink where catalog_xlink_id=id;
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
			update catalogref_documentation set documentationenum = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set documentationenum_nonstandard = p_documentationenum where catalogref_documentation_id=id;
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
			update catalogref_documentation_xlink set xlink = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set xlink_nonstandard = p_xlink where catalogref_documentation_xlink_id=id;
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
			update catalogref_xlink set xlink = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set xlink_nonstandard = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset(p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset("harvest", "name", "alias", "authority", "d_id", "servicename", "urlpath", "resourcecontrol") values (p_harvest, p_name, p_alias, p_authority, p_d_id, p_servicename, p_urlpath, p_resourcecontrol);
		select currval('dataset_dataset_id_seq') into id;
		BEGIN
			update dataset set collectiontype = cast(p_collectiontype as collectiontype) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set collectiontype_nonstandard = p_collectiontype where dataset_id=id;
		END;
		update dataset set status = cast(p_status as status) where dataset_id=id;
		BEGIN
			update dataset set datatype = cast(p_datatype as datatype) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set datatype_nonstandard = p_datatype where dataset_id=id;
		END;
		BEGIN
			update dataset set datasize_unit = cast(p_datasize_unit as datasize_unit) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set datasize_unit_nonstandard = p_datasize_unit where dataset_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_dataset_access(p_dataset_id int, p_urlpath text, p_servicename text, p_dataformat text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_access("dataset_id", "urlpath", "servicename") values (p_dataset_id, p_urlpath, p_servicename);
		select currval('dataset_access_dataset_access_id_seq') into id;
		BEGIN
			update dataset_access set dataformat = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set dataformat_nonstandard = p_dataformat where dataset_access_id=id;
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
			update dataset_access_datasize set units = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set units_nonstandard = p_units where dataset_access_datasize_id=id;
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

CREATE OR REPLACE FUNCTION insert_metadata(p_metadatatype text, p_inherited text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into metadata("not_empty") values ('true');
		select currval('metadata_metadata_id_seq') into id;
		BEGIN
			update metadata set metadatatype = cast(p_metadatatype as metadatatype) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set metadatatype_nonstandard = p_metadatatype where metadata_id=id;
		END;
		BEGIN
			update metadata set inherited = cast(p_inherited as inherited) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set inherited_nonstandard = p_inherited where metadata_id=id;
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
			update metadata_xlink set xlink = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set xlink_nonstandard = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service(p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service("suffix", "name", "base", "desc") values (p_suffix, p_name, p_base, p_desc);
		select currval('service_service_id_seq') into id;
		BEGIN
			update service set servicetype = cast(p_servicetype as servicetype) where service_id=id;
		EXCEPTION
			when others then
				update service set servicetype_nonstandard = p_servicetype where service_id=id;
		END;
		update service set status = cast(p_status as status) where service_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service_datasetroot(p_service_id int, p_path text, p_location text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service_datasetroot("service_id", "path", "location") values (p_service_id, p_path, p_location);
		select currval('service_datasetroot_service_datasetroot_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_service_property(p_service_id int, p_value text, p_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into service_property("service_id", "value", "name") values (p_service_id, p_value, p_name);
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

CREATE OR REPLACE FUNCTION insert_tmg_contributor(p_tmg_id int, p_role text, p_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_contributor("tmg_id", "role", "name") values (p_tmg_id, p_role, p_name);
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
			update tmg_dataformat set dataformat = cast(p_dataformat as dataformat) where tmg_dataformat_id=id;
		EXCEPTION
			when others then
				update tmg_dataformat set dataformat_nonstandard = p_dataformat where tmg_dataformat_id=id;
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
			update tmg_datasize set units = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set units_nonstandard = p_units where tmg_datasize_id=id;
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
			update tmg_datatype set datatype = cast(p_datatype as datatype) where tmg_datatype_id=id;
		EXCEPTION
			when others then
				update tmg_datatype set datatype_nonstandard = p_datatype where tmg_datatype_id=id;
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
			update tmg_date set dateenum = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set dateenum_nonstandard = p_dateenum where tmg_date_id=id;
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
			update tmg_documentation set documentationenum = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set documentationenum_nonstandard = p_documentationenum where tmg_documentation_id=id;
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
			update tmg_documentation_xlink set xlink = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set xlink_nonstandard = p_xlink where tmg_documentation_xlink_id=id;
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
			update tmg_geospatialcoverage set upordown = cast(p_upordown as upordown) where tmg_geospatialcoverage_id=id;
		EXCEPTION
			when others then
				update tmg_geospatialcoverage set upordown_nonstandard = p_upordown where tmg_geospatialcoverage_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id int, p_size text, p_units text, p_start text, p_resolution text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_eastwest("tmg_geospatialcoverage_id", "size", "units", "start", "resolution") values (p_tmg_geospatialcoverage_id, p_size, p_units, p_start, p_resolution);
		select currval('tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_eastwest_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id int, p_vocabulary text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_name("tmg_geospatialcoverage_id", "vocabulary", "value") values (p_tmg_geospatialcoverage_id, p_vocabulary, p_value);
		select currval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id int, p_size text, p_resolution text, p_start text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_northsouth("tmg_geospatialcoverage_id", "size", "resolution", "start", "units") values (p_tmg_geospatialcoverage_id, p_size, p_resolution, p_start, p_units);
		select currval('tmg_geospatialcoverage_northsouth_tmg_geospatialcoverage_northsouth_id_seq') into id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id int, p_start text, p_resolution text, p_size text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_updown("tmg_geospatialcoverage_id", "start", "resolution", "size", "units") values (p_tmg_geospatialcoverage_id, p_start, p_resolution, p_size, p_units);
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

CREATE OR REPLACE FUNCTION insert_tmg_publisher_contact(p_tmg_publisher_id int, p_url text, p_email text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_contact("tmg_publisher_id", "url", "email") values (p_tmg_publisher_id, p_url, p_email);
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
			update tmg_timecoverage_end set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set dateenum_nonstandard = p_dateenum where tmg_timecoverage_end_id=id;
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
			update tmg_timecoverage_start set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set dateenum_nonstandard = p_dateenum where tmg_timecoverage_start_id=id;
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
			update tmg_variables set vocabulary = cast(p_vocabulary as vocabulary) where tmg_variables_id=id;
		EXCEPTION
			when others then
				update tmg_variables set vocabulary_nonstandard = p_vocabulary where tmg_variables_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_tmg_variables_variable(p_tmg_variables_id int, p_units text, p_name text, p_vocabulary_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variable("tmg_variables_id", "units", "name", "vocabulary_name") values (p_tmg_variables_id, p_units, p_name, p_vocabulary_name);
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
			update tmg_variables_variablemap set xlink = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set xlink_nonstandard = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION update_catalog(p_catalog int, p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalog_id into id from catalog where catalog_id=p_catalog_id;
		if(id is null) then
			return -1;
		end if;
		update catalog set "name"=p_name, "expires"=p_expires, "version"=p_version, "base"=p_base, "xmlns"=p_xmlns where catalog_id=id;
		update catalog set status = cast(p_status as status) where catalog_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalog_property(p_catalog_id int, p_catalog_property int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalog_property_id into id from catalog_property where catalog_property_id=p_catalog_property_id;
		if(id is null) then
			return -1;
		end if;
		update catalog_property set "catalog_id"=p_catalog_id, "name"=p_name, "value"=p_value where catalog_property_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalog_xlink(p_catalog_id int, p_catalog_xlink int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalog_xlink_id into id from catalog_xlink where catalog_xlink_id=p_catalog_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalog_xlink set "catalog_id"=p_catalog_id, "value"=p_value where catalog_xlink_id=id;
		BEGIN
			update catalog_xlink set xlink = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set xlink_nonstandard = p_xlink where catalog_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalogref(p_catalogref int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalogref_id into id from catalogref where catalogref_id=p_catalogref_id;
		if(id is null) then
			return -1;
		end if;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalogref_documentation(p_catalogref_id int, p_catalogref_documentation int, p_value text, p_documentationenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_id into id from catalogref_documentation where catalogref_documentation_id=p_catalogref_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation set "catalogref_id"=p_catalogref_id, "value"=p_value where catalogref_documentation_id=id;
		BEGIN
			update catalogref_documentation set documentationenum = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set documentationenum_nonstandard = p_documentationenum where catalogref_documentation_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalogref_documentation_namespace(p_catalogref_documentation_id int, p_catalogref_documentation_namespace int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_namespace_id into id from catalogref_documentation_namespace where catalogref_documentation_namespace_id=p_catalogref_documentation_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation_namespace set "catalogref_documentation_id"=p_catalogref_documentation_id, "namespace"=p_namespace where catalogref_documentation_namespace_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalogref_documentation_xlink(p_catalogref_documentation_id int, p_catalogref_documentation_xlink int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_xlink_id into id from catalogref_documentation_xlink where catalogref_documentation_xlink_id=p_catalogref_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation_xlink set "catalogref_documentation_id"=p_catalogref_documentation_id, "value"=p_value where catalogref_documentation_xlink_id=id;
		BEGIN
			update catalogref_documentation_xlink set xlink = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set xlink_nonstandard = p_xlink where catalogref_documentation_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_catalogref_xlink(p_catalogref_id int, p_catalogref_xlink int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select catalogref_xlink_id into id from catalogref_xlink where catalogref_xlink_id=p_catalogref_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_xlink set "catalogref_id"=p_catalogref_id, "value"=p_value where catalogref_xlink_id=id;
		BEGIN
			update catalogref_xlink set xlink = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set xlink_nonstandard = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_dataset(p_dataset int, p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select dataset_id into id from dataset where dataset_id=p_dataset_id;
		if(id is null) then
			return -1;
		end if;
		update dataset set "harvest"=p_harvest, "name"=p_name, "alias"=p_alias, "authority"=p_authority, "d_id"=p_d_id, "servicename"=p_servicename, "urlpath"=p_urlpath, "resourcecontrol"=p_resourcecontrol where dataset_id=id;
		BEGIN
			update dataset set collectiontype = cast(p_collectiontype as collectiontype) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set collectiontype_nonstandard = p_collectiontype where dataset_id=id;
		END;
		update dataset set status = cast(p_status as status) where dataset_id=id;
		BEGIN
			update dataset set datatype = cast(p_datatype as datatype) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set datatype_nonstandard = p_datatype where dataset_id=id;
		END;
		BEGIN
			update dataset set datasize_unit = cast(p_datasize_unit as datasize_unit) where dataset_id=id;
		EXCEPTION
			when others then
				update dataset set datasize_unit_nonstandard = p_datasize_unit where dataset_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_dataset_access(p_dataset_id int, p_dataset_access int, p_urlpath text, p_servicename text, p_dataformat text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_id into id from dataset_access where dataset_access_id=p_dataset_access_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access set "dataset_id"=p_dataset_id, "urlpath"=p_urlpath, "servicename"=p_servicename where dataset_access_id=id;
		BEGIN
			update dataset_access set dataformat = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set dataformat_nonstandard = p_dataformat where dataset_access_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_dataset_access_datasize(p_dataset_access_id int, p_dataset_access_datasize int, p_value text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_datasize_id into id from dataset_access_datasize where dataset_access_datasize_id=p_dataset_access_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access_datasize set "dataset_access_id"=p_dataset_access_id, "value"=p_value where dataset_access_datasize_id=id;
		BEGIN
			update dataset_access_datasize set units = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set units_nonstandard = p_units where dataset_access_datasize_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_dataset_ncml(p_dataset_id int, p_dataset_ncml int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select dataset_ncml_id into id from dataset_ncml where dataset_ncml_id=p_dataset_ncml_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_ncml set "dataset_id"=p_dataset_id where dataset_ncml_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_dataset_property(p_dataset_id int, p_dataset_property int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select dataset_property_id into id from dataset_property where dataset_property_id=p_dataset_property_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_property set "dataset_id"=p_dataset_id, "name"=p_name, "value"=p_value where dataset_property_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_metadata(p_metadata int, p_metadatatype text, p_inherited text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select metadata_id into id from metadata where metadata_id=p_metadata_id;
		if(id is null) then
			return -1;
		end if;
		BEGIN
			update metadata set metadatatype = cast(p_metadatatype as metadatatype) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set metadatatype_nonstandard = p_metadatatype where metadata_id=id;
		END;
		BEGIN
			update metadata set inherited = cast(p_inherited as inherited) where metadata_id=id;
		EXCEPTION
			when others then
				update metadata set inherited_nonstandard = p_inherited where metadata_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_metadata_namespace(p_metadata_id int, p_metadata_namespace int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select metadata_namespace_id into id from metadata_namespace where metadata_namespace_id=p_metadata_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update metadata_namespace set "metadata_id"=p_metadata_id, "namespace"=p_namespace where metadata_namespace_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_metadata_xlink(p_metadata_id int, p_metadata_xlink int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select metadata_xlink_id into id from metadata_xlink where metadata_xlink_id=p_metadata_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update metadata_xlink set "metadata_id"=p_metadata_id, "value"=p_value where metadata_xlink_id=id;
		BEGIN
			update metadata_xlink set xlink = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set xlink_nonstandard = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_service(p_service int, p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select service_id into id from service where service_id=p_service_id;
		if(id is null) then
			return -1;
		end if;
		update service set "suffix"=p_suffix, "name"=p_name, "base"=p_base, "desc"=p_desc where service_id=id;
		BEGIN
			update service set servicetype = cast(p_servicetype as servicetype) where service_id=id;
		EXCEPTION
			when others then
				update service set servicetype_nonstandard = p_servicetype where service_id=id;
		END;
		update service set status = cast(p_status as status) where service_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_service_datasetroot(p_service_id int, p_service_datasetroot int, p_path text, p_location text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select service_datasetroot_id into id from service_datasetroot where service_datasetroot_id=p_service_datasetroot_id;
		if(id is null) then
			return -1;
		end if;
		update service_datasetroot set "service_id"=p_service_id, "path"=p_path, "location"=p_location where service_datasetroot_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_service_property(p_service_id int, p_service_property int, p_value text, p_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select service_property_id into id from service_property where service_property_id=p_service_property_id;
		if(id is null) then
			return -1;
		end if;
		update service_property set "service_id"=p_service_id, "value"=p_value, "name"=p_name where service_property_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg(p_tmg int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_id into id from tmg where tmg_id=p_tmg_id;
		if(id is null) then
			return -1;
		end if;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_authority(p_tmg_id int, p_tmg_authority int, p_authority text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_authority_id into id from tmg_authority where tmg_authority_id=p_tmg_authority_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_authority set "tmg_id"=p_tmg_id, "authority"=p_authority where tmg_authority_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_contributor(p_tmg_id int, p_tmg_contributor int, p_role text, p_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_contributor_id into id from tmg_contributor where tmg_contributor_id=p_tmg_contributor_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_contributor set "tmg_id"=p_tmg_id, "role"=p_role, "name"=p_name where tmg_contributor_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_creator(p_tmg_id int, p_tmg_creator int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_creator_id into id from tmg_creator where tmg_creator_id=p_tmg_creator_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_creator set "tmg_id"=p_tmg_id where tmg_creator_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_creator_contact(p_tmg_creator_id int, p_tmg_creator_contact int, p_email text, p_url text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_creator_contact_id into id from tmg_creator_contact where tmg_creator_contact_id=p_tmg_creator_contact_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_creator_contact set "tmg_creator_id"=p_tmg_creator_id, "email"=p_email, "url"=p_url where tmg_creator_contact_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_creator_name(p_tmg_creator_id int, p_tmg_creator_name int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_creator_name_id into id from tmg_creator_name where tmg_creator_name_id=p_tmg_creator_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_creator_name set "tmg_creator_id"=p_tmg_creator_id, "value"=p_value, "vocabulary"=p_vocabulary where tmg_creator_name_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_dataformat(p_tmg_id int, p_tmg_dataformat int, p_dataformat text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_dataformat_id into id from tmg_dataformat where tmg_dataformat_id=p_tmg_dataformat_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_dataformat set "tmg_id"=p_tmg_id where tmg_dataformat_id=id;
		BEGIN
			update tmg_dataformat set dataformat = cast(p_dataformat as dataformat) where tmg_dataformat_id=id;
		EXCEPTION
			when others then
				update tmg_dataformat set dataformat_nonstandard = p_dataformat where tmg_dataformat_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_datasize(p_tmg_id int, p_tmg_datasize int, p_value text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_datasize_id into id from tmg_datasize where tmg_datasize_id=p_tmg_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_datasize set "tmg_id"=p_tmg_id, "value"=p_value where tmg_datasize_id=id;
		BEGIN
			update tmg_datasize set units = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set units_nonstandard = p_units where tmg_datasize_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_datatype(p_tmg_id int, p_tmg_datatype int, p_datatype text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_datatype_id into id from tmg_datatype where tmg_datatype_id=p_tmg_datatype_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_datatype set "tmg_id"=p_tmg_id where tmg_datatype_id=id;
		BEGIN
			update tmg_datatype set datatype = cast(p_datatype as datatype) where tmg_datatype_id=id;
		EXCEPTION
			when others then
				update tmg_datatype set datatype_nonstandard = p_datatype where tmg_datatype_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_date(p_tmg_id int, p_tmg_date int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_date_id into id from tmg_date where tmg_date_id=p_tmg_date_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_date set "tmg_id"=p_tmg_id, "format"=p_format, "value"=p_value where tmg_date_id=id;
		BEGIN
			update tmg_date set dateenum = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set dateenum_nonstandard = p_dateenum where tmg_date_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_documentation(p_tmg_id int, p_tmg_documentation int, p_value text, p_documentationenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_id into id from tmg_documentation where tmg_documentation_id=p_tmg_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation set "tmg_id"=p_tmg_id, "value"=p_value where tmg_documentation_id=id;
		BEGIN
			update tmg_documentation set documentationenum = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set documentationenum_nonstandard = p_documentationenum where tmg_documentation_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_documentation_namespace(p_tmg_documentation_id int, p_tmg_documentation_namespace int, p_namespace text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_namespace_id into id from tmg_documentation_namespace where tmg_documentation_namespace_id=p_tmg_documentation_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation_namespace set "tmg_documentation_id"=p_tmg_documentation_id, "namespace"=p_namespace where tmg_documentation_namespace_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_documentation_xlink(p_tmg_documentation_id int, p_tmg_documentation_xlink int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_xlink_id into id from tmg_documentation_xlink where tmg_documentation_xlink_id=p_tmg_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation_xlink set "tmg_documentation_id"=p_tmg_documentation_id, "value"=p_value where tmg_documentation_xlink_id=id;
		BEGIN
			update tmg_documentation_xlink set xlink = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set xlink_nonstandard = p_xlink where tmg_documentation_xlink_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_geospatialcoverage(p_tmg_id int, p_tmg_geospatialcoverage int, p_upordown text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_id into id from tmg_geospatialcoverage where tmg_geospatialcoverage_id=p_tmg_geospatialcoverage_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage set "tmg_id"=p_tmg_id where tmg_geospatialcoverage_id=id;
		BEGIN
			update tmg_geospatialcoverage set upordown = cast(p_upordown as upordown) where tmg_geospatialcoverage_id=id;
		EXCEPTION
			when others then
				update tmg_geospatialcoverage set upordown_nonstandard = p_upordown where tmg_geospatialcoverage_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id int, p_tmg_geospatialcoverage_eastwest int, p_size text, p_units text, p_start text, p_resolution text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_eastwest_id into id from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=p_tmg_geospatialcoverage_eastwest_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_eastwest set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "size"=p_size, "units"=p_units, "start"=p_start, "resolution"=p_resolution where tmg_geospatialcoverage_eastwest_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id int, p_tmg_geospatialcoverage_name int, p_vocabulary text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_name_id into id from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=p_tmg_geospatialcoverage_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_name set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "vocabulary"=p_vocabulary, "value"=p_value where tmg_geospatialcoverage_name_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id int, p_tmg_geospatialcoverage_northsouth int, p_size text, p_resolution text, p_start text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_northsouth_id into id from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=p_tmg_geospatialcoverage_northsouth_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_northsouth set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "size"=p_size, "resolution"=p_resolution, "start"=p_start, "units"=p_units where tmg_geospatialcoverage_northsouth_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id int, p_tmg_geospatialcoverage_updown int, p_start text, p_resolution text, p_size text, p_units text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_updown_id into id from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=p_tmg_geospatialcoverage_updown_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_updown set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "start"=p_start, "resolution"=p_resolution, "size"=p_size, "units"=p_units where tmg_geospatialcoverage_updown_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_keyword(p_tmg_id int, p_tmg_keyword int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_keyword_id into id from tmg_keyword where tmg_keyword_id=p_tmg_keyword_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_keyword set "tmg_id"=p_tmg_id, "value"=p_value, "vocabulary"=p_vocabulary where tmg_keyword_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_project(p_tmg_id int, p_tmg_project int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_project_id into id from tmg_project where tmg_project_id=p_tmg_project_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_project set "tmg_id"=p_tmg_id, "value"=p_value, "vocabulary"=p_vocabulary where tmg_project_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_property(p_tmg_id int, p_tmg_property int, p_name text, p_value text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_property_id into id from tmg_property where tmg_property_id=p_tmg_property_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_property set "tmg_id"=p_tmg_id, "name"=p_name, "value"=p_value where tmg_property_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_publisher(p_tmg_id int, p_tmg_publisher int) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_id into id from tmg_publisher where tmg_publisher_id=p_tmg_publisher_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher set "tmg_id"=p_tmg_id where tmg_publisher_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_publisher_contact(p_tmg_publisher_id int, p_tmg_publisher_contact int, p_url text, p_email text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_contact_id into id from tmg_publisher_contact where tmg_publisher_contact_id=p_tmg_publisher_contact_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher_contact set "tmg_publisher_id"=p_tmg_publisher_id, "url"=p_url, "email"=p_email where tmg_publisher_contact_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_publisher_name(p_tmg_publisher_id int, p_tmg_publisher_name int, p_value text, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_name_id into id from tmg_publisher_name where tmg_publisher_name_id=p_tmg_publisher_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher_name set "tmg_publisher_id"=p_tmg_publisher_id, "value"=p_value, "vocabulary"=p_vocabulary where tmg_publisher_name_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_servicename(p_tmg_id int, p_tmg_servicename int, p_servicename text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_servicename_id into id from tmg_servicename where tmg_servicename_id=p_tmg_servicename_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_servicename set "tmg_id"=p_tmg_id, "servicename"=p_servicename where tmg_servicename_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_timecoverage(p_tmg_id int, p_tmg_timecoverage int, p_resolution text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_id into id from tmg_timecoverage where tmg_timecoverage_id=p_tmg_timecoverage_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage set "tmg_id"=p_tmg_id, "resolution"=p_resolution where tmg_timecoverage_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_timecoverage_duration(p_tmg_timecoverage_id int, p_tmg_timecoverage_duration int, p_duration text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_duration_id into id from tmg_timecoverage_duration where tmg_timecoverage_duration_id=p_tmg_timecoverage_duration_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_duration set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "duration"=p_duration where tmg_timecoverage_duration_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_timecoverage_end(p_tmg_timecoverage_id int, p_tmg_timecoverage_end int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_end_id into id from tmg_timecoverage_end where tmg_timecoverage_end_id=p_tmg_timecoverage_end_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_end set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "format"=p_format, "value"=p_value where tmg_timecoverage_end_id=id;
		BEGIN
			update tmg_timecoverage_end set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set dateenum_nonstandard = p_dateenum where tmg_timecoverage_end_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_timecoverage_resolution(p_tmg_timecoverage_id int, p_tmg_timecoverage_resolution int, p_duration text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_resolution_id into id from tmg_timecoverage_resolution where tmg_timecoverage_resolution_id=p_tmg_timecoverage_resolution_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_resolution set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "duration"=p_duration where tmg_timecoverage_resolution_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_timecoverage_start(p_tmg_timecoverage_id int, p_tmg_timecoverage_start int, p_format text, p_value text, p_dateenum text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_start_id into id from tmg_timecoverage_start where tmg_timecoverage_start_id=p_tmg_timecoverage_start_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_start set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "format"=p_format, "value"=p_value where tmg_timecoverage_start_id=id;
		BEGIN
			update tmg_timecoverage_start set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set dateenum_nonstandard = p_dateenum where tmg_timecoverage_start_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_variables(p_tmg_id int, p_tmg_variables int, p_vocabulary text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_id into id from tmg_variables where tmg_variables_id=p_tmg_variables_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables set "tmg_id"=p_tmg_id where tmg_variables_id=id;
		BEGIN
			update tmg_variables set vocabulary = cast(p_vocabulary as vocabulary) where tmg_variables_id=id;
		EXCEPTION
			when others then
				update tmg_variables set vocabulary_nonstandard = p_vocabulary where tmg_variables_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_variables_variable(p_tmg_variables_id int, p_tmg_variables_variable int, p_units text, p_name text, p_vocabulary_name text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variable_id into id from tmg_variables_variable where tmg_variables_variable_id=p_tmg_variables_variable_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variable set "tmg_variables_id"=p_tmg_variables_id, "units"=p_units, "name"=p_name, "vocabulary_name"=p_vocabulary_name where tmg_variables_variable_id=id;

		return id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_tmg_variables_variablemap(p_tmg_variables_id int, p_tmg_variables_variablemap int, p_value text, p_xlink text) RETURNS int AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variablemap_id into id from tmg_variables_variablemap where tmg_variables_variablemap_id=p_tmg_variables_variablemap_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variablemap set "tmg_variables_id"=p_tmg_variables_id, "value"=p_value where tmg_variables_variablemap_id=id;
		BEGIN
			update tmg_variables_variablemap set xlink = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set xlink_nonstandard = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$ LANGUAGE plpgsql;



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


