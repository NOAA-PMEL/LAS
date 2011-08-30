--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0.4
-- Dumped by pg_dump version 9.0.4
-- Started on 2011-08-29 17:05:09

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 791 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- TOC entry 479 (class 1247 OID 39994)
-- Dependencies: 6
-- Name: booltype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE booltype AS ENUM (
    'true',
    'false'
);


ALTER TYPE public.booltype OWNER TO postgres;

--
-- TOC entry 481 (class 1247 OID 39998)
-- Dependencies: 6
-- Name: collectiontype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE collectiontype AS ENUM (
    'TimeSeries',
    'Stations'
);


ALTER TYPE public.collectiontype OWNER TO postgres;

--
-- TOC entry 483 (class 1247 OID 40002)
-- Dependencies: 6
-- Name: dataformat; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE dataformat AS ENUM (
    'BUFR',
    'ESML',
    'Gempak',
    'GRIB-1',
    'GRIB-2',
    'HDF4',
    'HDF5',
    'NcML',
    'NetCDF',
    'image/gif',
    'image/jpeg',
    'image/tiff',
    'text/plain',
    'text/tab-separated-values',
    'text/xml',
    'video/mpeg',
    'video/quicktime',
    'video/realtime',
    'other valid MIME type'
);


ALTER TYPE public.dataformat OWNER TO postgres;

--
-- TOC entry 485 (class 1247 OID 40023)
-- Dependencies: 6
-- Name: datasize_unit; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE datasize_unit AS ENUM (
    'bytes',
    'Kbytes',
    'Mbytes',
    'Gbytes',
    'Tbytes'
);


ALTER TYPE public.datasize_unit OWNER TO postgres;

--
-- TOC entry 487 (class 1247 OID 40030)
-- Dependencies: 6
-- Name: datatype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE datatype AS ENUM (
    'Grid',
    'Image',
    'Station',
    'Swath',
    'Trajectory'
);


ALTER TYPE public.datatype OWNER TO postgres;

--
-- TOC entry 489 (class 1247 OID 40037)
-- Dependencies: 6
-- Name: dateenum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE dateenum AS ENUM (
    'created',
    'modified',
    'valid',
    'issued',
    'available'
);


ALTER TYPE public.dateenum OWNER TO postgres;

--
-- TOC entry 491 (class 1247 OID 40044)
-- Dependencies: 6
-- Name: documentationenum; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE documentationenum AS ENUM (
    'funding',
    'history',
    'processing_level',
    'rights',
    'summary'
);


ALTER TYPE public.documentationenum OWNER TO postgres;

--
-- TOC entry 493 (class 1247 OID 40051)
-- Dependencies: 6
-- Name: metadatatype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE metadatatype AS ENUM (
    'THREDDS',
    'ADN',
    'Aggregation',
    'CatalogGenConfig',
    'DublinCore',
    'DIF',
    'FGDC',
    'LAS',
    'NetCDF',
    'ESG',
    'Other'
);


ALTER TYPE public.metadatatype OWNER TO postgres;

--
-- TOC entry 495 (class 1247 OID 40064)
-- Dependencies: 6
-- Name: servicetype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE servicetype AS ENUM (
    'ADDE',
    'DODS',
    'OpenDAP',
    'OpenDAP-G',
    'HTTPServer',
    'FTP',
    'GridFTP',
    'File',
    'LAS',
    'WMS',
    'WFS',
    'WCS',
    'WSDL',
    'WebForm',
    'Catalog',
    'QueryCapability',
    'Resolver',
    'Compound',
    'OPENDAP'
);


ALTER TYPE public.servicetype OWNER TO postgres;

--
-- TOC entry 497 (class 1247 OID 40085)
-- Dependencies: 6
-- Name: status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE status AS ENUM (
    'new',
    'unchanged',
    'removed',
    'altered',
    'invalid',
    'valid'
);


ALTER TYPE public.status OWNER TO postgres;

--
-- TOC entry 499 (class 1247 OID 40093)
-- Dependencies: 6
-- Name: units; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE units AS ENUM (
    'bytes',
    'Kbytes',
    'Mbytes',
    'Gbytes',
    'Tbytes',
    'BYTES',
    'KBYTES',
    'MBYTES',
    'GBYTES',
    'TBYTES'
);


ALTER TYPE public.units OWNER TO postgres;

--
-- TOC entry 501 (class 1247 OID 40105)
-- Dependencies: 6
-- Name: upordown; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE upordown AS ENUM (
    'up',
    'down'
);


ALTER TYPE public.upordown OWNER TO postgres;

--
-- TOC entry 503 (class 1247 OID 40109)
-- Dependencies: 6
-- Name: vocabulary; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE vocabulary AS ENUM (
    'CF',
    'DIF',
    'GRIB-1'
);


ALTER TYPE public.vocabulary OWNER TO postgres;

--
-- TOC entry 505 (class 1247 OID 40114)
-- Dependencies: 6
-- Name: xlink; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE xlink AS ENUM (
    'xlink:href',
    'xlink:title',
    'xlink:show',
    'xlink:type'
);


ALTER TYPE public.xlink OWNER TO postgres;

--
-- TOC entry 131 (class 1255 OID 44411)
-- Dependencies: 6 791
-- Name: delete_catalog(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalog(p_catalog_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalog(p_catalog_id integer) OWNER TO postgres;

--
-- TOC entry 132 (class 1255 OID 44412)
-- Dependencies: 6 791
-- Name: delete_catalog_dataset(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalog_dataset(p_catalog_id integer, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalog_dataset(p_catalog_id integer, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 133 (class 1255 OID 44413)
-- Dependencies: 6 791
-- Name: delete_catalog_property(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalog_property(p_catalog_property_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalog_property(p_catalog_property_id integer) OWNER TO postgres;

--
-- TOC entry 134 (class 1255 OID 44414)
-- Dependencies: 6 791
-- Name: delete_catalog_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalog_service(p_catalog_id integer, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalog_service(p_catalog_id integer, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 135 (class 1255 OID 44415)
-- Dependencies: 791 6
-- Name: delete_catalog_xlink(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalog_xlink(p_catalog_xlink_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalog_xlink(p_catalog_xlink_id integer) OWNER TO postgres;

--
-- TOC entry 136 (class 1255 OID 44416)
-- Dependencies: 791 6
-- Name: delete_catalogref(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalogref(p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalogref(p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 137 (class 1255 OID 44417)
-- Dependencies: 6 791
-- Name: delete_catalogref_documentation(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalogref_documentation(p_catalogref_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalogref_documentation(p_catalogref_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 138 (class 1255 OID 44418)
-- Dependencies: 6 791
-- Name: delete_catalogref_documentation_namespace(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalogref_documentation_namespace(p_catalogref_documentation_namespace_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalogref_documentation_namespace(p_catalogref_documentation_namespace_id integer) OWNER TO postgres;

--
-- TOC entry 139 (class 1255 OID 44419)
-- Dependencies: 6 791
-- Name: delete_catalogref_documentation_xlink(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalogref_documentation_xlink(p_catalogref_documentation_xlink_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalogref_documentation_xlink(p_catalogref_documentation_xlink_id integer) OWNER TO postgres;

--
-- TOC entry 140 (class 1255 OID 44420)
-- Dependencies: 791 6
-- Name: delete_catalogref_xlink(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_catalogref_xlink(p_catalogref_xlink_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_catalogref_xlink(p_catalogref_xlink_id integer) OWNER TO postgres;

--
-- TOC entry 141 (class 1255 OID 44421)
-- Dependencies: 6 791
-- Name: delete_dataset(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset(p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset(p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 142 (class 1255 OID 44422)
-- Dependencies: 791 6
-- Name: delete_dataset_access(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_access(p_dataset_access_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_access(p_dataset_access_id integer) OWNER TO postgres;

--
-- TOC entry 143 (class 1255 OID 44423)
-- Dependencies: 6 791
-- Name: delete_dataset_access_datasize(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_access_datasize(p_dataset_access_datasize_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_access_datasize(p_dataset_access_datasize_id integer) OWNER TO postgres;

--
-- TOC entry 72 (class 1255 OID 44424)
-- Dependencies: 6 791
-- Name: delete_dataset_catalogref(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_catalogref(p_dataset_id integer, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_catalogref(p_dataset_id integer, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 106 (class 1255 OID 44425)
-- Dependencies: 791 6
-- Name: delete_dataset_dataset(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_dataset(p_parent_id integer, p_child_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_dataset(p_parent_id integer, p_child_id integer) OWNER TO postgres;

--
-- TOC entry 144 (class 1255 OID 44426)
-- Dependencies: 791 6
-- Name: delete_dataset_ncml(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_ncml(p_dataset_ncml_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_ncml(p_dataset_ncml_id integer) OWNER TO postgres;

--
-- TOC entry 145 (class 1255 OID 44427)
-- Dependencies: 791 6
-- Name: delete_dataset_property(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_property(p_dataset_property_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_property(p_dataset_property_id integer) OWNER TO postgres;

--
-- TOC entry 146 (class 1255 OID 44428)
-- Dependencies: 6 791
-- Name: delete_dataset_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_service(p_dataset_id integer, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_service(p_dataset_id integer, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 147 (class 1255 OID 44429)
-- Dependencies: 6 791
-- Name: delete_dataset_tmg(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_dataset_tmg(p_dataset_id integer, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_dataset_tmg(p_dataset_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 148 (class 1255 OID 44430)
-- Dependencies: 6 791
-- Name: delete_metadata(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_metadata(p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_metadata(p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 149 (class 1255 OID 44431)
-- Dependencies: 6 791
-- Name: delete_metadata_namespace(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_metadata_namespace(p_metadata_namespace_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_metadata_namespace(p_metadata_namespace_id integer) OWNER TO postgres;

--
-- TOC entry 150 (class 1255 OID 44432)
-- Dependencies: 791 6
-- Name: delete_metadata_tmg(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_metadata_tmg(p_metadata_id integer, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_metadata_tmg(p_metadata_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 151 (class 1255 OID 44433)
-- Dependencies: 791 6
-- Name: delete_metadata_xlink(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_metadata_xlink(p_metadata_xlink_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_metadata_xlink(p_metadata_xlink_id integer) OWNER TO postgres;

--
-- TOC entry 152 (class 1255 OID 44434)
-- Dependencies: 791 6
-- Name: delete_service(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_service(p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_service(p_service_id integer) OWNER TO postgres;

--
-- TOC entry 153 (class 1255 OID 44435)
-- Dependencies: 6 791
-- Name: delete_service_datasetroot(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_service_datasetroot(p_service_datasetroot_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_service_datasetroot(p_service_datasetroot_id integer) OWNER TO postgres;

--
-- TOC entry 154 (class 1255 OID 44436)
-- Dependencies: 791 6
-- Name: delete_service_property(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_service_property(p_service_property_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_service_property(p_service_property_id integer) OWNER TO postgres;

--
-- TOC entry 155 (class 1255 OID 44437)
-- Dependencies: 791 6
-- Name: delete_service_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_service_service(p_parent_id integer, p_child_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_service_service(p_parent_id integer, p_child_id integer) OWNER TO postgres;

--
-- TOC entry 156 (class 1255 OID 44438)
-- Dependencies: 6 791
-- Name: delete_tmg(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg(p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg(p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 157 (class 1255 OID 44439)
-- Dependencies: 791 6
-- Name: delete_tmg_authority(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_authority(p_tmg_authority_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_authority(p_tmg_authority_id integer) OWNER TO postgres;

--
-- TOC entry 158 (class 1255 OID 44440)
-- Dependencies: 6 791
-- Name: delete_tmg_contributor(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_contributor(p_tmg_contributor_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_contributor(p_tmg_contributor_id integer) OWNER TO postgres;

--
-- TOC entry 159 (class 1255 OID 44441)
-- Dependencies: 6 791
-- Name: delete_tmg_creator(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_creator(p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_creator(p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 160 (class 1255 OID 44442)
-- Dependencies: 791 6
-- Name: delete_tmg_creator_contact(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_creator_contact(p_tmg_creator_contact_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_creator_contact(p_tmg_creator_contact_id integer) OWNER TO postgres;

--
-- TOC entry 161 (class 1255 OID 44443)
-- Dependencies: 6 791
-- Name: delete_tmg_creator_name(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_creator_name(p_tmg_creator_name_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_creator_name(p_tmg_creator_name_id integer) OWNER TO postgres;

--
-- TOC entry 162 (class 1255 OID 44444)
-- Dependencies: 6 791
-- Name: delete_tmg_dataformat(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_dataformat(p_tmg_dataformat_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_dataformat(p_tmg_dataformat_id integer) OWNER TO postgres;

--
-- TOC entry 163 (class 1255 OID 44445)
-- Dependencies: 791 6
-- Name: delete_tmg_datasize(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_datasize(p_tmg_datasize_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_datasize(p_tmg_datasize_id integer) OWNER TO postgres;

--
-- TOC entry 164 (class 1255 OID 44446)
-- Dependencies: 6 791
-- Name: delete_tmg_datatype(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_datatype(p_tmg_datatype_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_datatype(p_tmg_datatype_id integer) OWNER TO postgres;

--
-- TOC entry 165 (class 1255 OID 44447)
-- Dependencies: 6 791
-- Name: delete_tmg_date(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_date(p_tmg_date_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_date(p_tmg_date_id integer) OWNER TO postgres;

--
-- TOC entry 166 (class 1255 OID 44448)
-- Dependencies: 6 791
-- Name: delete_tmg_documentation(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_documentation(p_tmg_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_documentation(p_tmg_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 167 (class 1255 OID 44449)
-- Dependencies: 791 6
-- Name: delete_tmg_documentation_namespace(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_documentation_namespace(p_tmg_documentation_namespace_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_documentation_namespace(p_tmg_documentation_namespace_id integer) OWNER TO postgres;

--
-- TOC entry 168 (class 1255 OID 44450)
-- Dependencies: 6 791
-- Name: delete_tmg_documentation_xlink(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_documentation_xlink(p_tmg_documentation_xlink_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_documentation_xlink(p_tmg_documentation_xlink_id integer) OWNER TO postgres;

--
-- TOC entry 169 (class 1255 OID 44451)
-- Dependencies: 6 791
-- Name: delete_tmg_geospatialcoverage(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_geospatialcoverage(p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_geospatialcoverage(p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 170 (class 1255 OID 44452)
-- Dependencies: 791 6
-- Name: delete_tmg_geospatialcoverage_eastwest(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_eastwest_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_eastwest_id integer) OWNER TO postgres;

--
-- TOC entry 171 (class 1255 OID 44453)
-- Dependencies: 791 6
-- Name: delete_tmg_geospatialcoverage_name(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_name_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_name_id integer) OWNER TO postgres;

--
-- TOC entry 172 (class 1255 OID 44454)
-- Dependencies: 791 6
-- Name: delete_tmg_geospatialcoverage_northsouth(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_northsouth_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_northsouth_id integer) OWNER TO postgres;

--
-- TOC entry 173 (class 1255 OID 44455)
-- Dependencies: 791 6
-- Name: delete_tmg_geospatialcoverage_updown(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_updown_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_updown_id integer) OWNER TO postgres;

--
-- TOC entry 174 (class 1255 OID 44456)
-- Dependencies: 6 791
-- Name: delete_tmg_keyword(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_keyword(p_tmg_keyword_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_keyword(p_tmg_keyword_id integer) OWNER TO postgres;

--
-- TOC entry 175 (class 1255 OID 44457)
-- Dependencies: 6 791
-- Name: delete_tmg_metadata(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_metadata(p_tmg_id integer, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_metadata(p_tmg_id integer, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 176 (class 1255 OID 44458)
-- Dependencies: 791 6
-- Name: delete_tmg_project(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_project(p_tmg_project_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_project(p_tmg_project_id integer) OWNER TO postgres;

--
-- TOC entry 177 (class 1255 OID 44459)
-- Dependencies: 791 6
-- Name: delete_tmg_property(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_property(p_tmg_property_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_property(p_tmg_property_id integer) OWNER TO postgres;

--
-- TOC entry 178 (class 1255 OID 44460)
-- Dependencies: 791 6
-- Name: delete_tmg_publisher(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_publisher(p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_publisher(p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 179 (class 1255 OID 44461)
-- Dependencies: 6 791
-- Name: delete_tmg_publisher_contact(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_publisher_contact(p_tmg_publisher_contact_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_publisher_contact(p_tmg_publisher_contact_id integer) OWNER TO postgres;

--
-- TOC entry 180 (class 1255 OID 44462)
-- Dependencies: 791 6
-- Name: delete_tmg_publisher_name(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_publisher_name(p_tmg_publisher_name_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_publisher_name(p_tmg_publisher_name_id integer) OWNER TO postgres;

--
-- TOC entry 181 (class 1255 OID 44463)
-- Dependencies: 6 791
-- Name: delete_tmg_servicename(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_servicename(p_tmg_servicename_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_servicename(p_tmg_servicename_id integer) OWNER TO postgres;

--
-- TOC entry 182 (class 1255 OID 44464)
-- Dependencies: 6 791
-- Name: delete_tmg_timecoverage(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_timecoverage(p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_timecoverage(p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 183 (class 1255 OID 44465)
-- Dependencies: 6 791
-- Name: delete_tmg_timecoverage_duration(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_timecoverage_duration(p_tmg_timecoverage_duration_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_timecoverage_duration(p_tmg_timecoverage_duration_id integer) OWNER TO postgres;

--
-- TOC entry 184 (class 1255 OID 44466)
-- Dependencies: 791 6
-- Name: delete_tmg_timecoverage_end(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_timecoverage_end(p_tmg_timecoverage_end_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_timecoverage_end(p_tmg_timecoverage_end_id integer) OWNER TO postgres;

--
-- TOC entry 185 (class 1255 OID 44467)
-- Dependencies: 6 791
-- Name: delete_tmg_timecoverage_resolution(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_timecoverage_resolution(p_tmg_timecoverage_resolution_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_timecoverage_resolution(p_tmg_timecoverage_resolution_id integer) OWNER TO postgres;

--
-- TOC entry 186 (class 1255 OID 44468)
-- Dependencies: 6 791
-- Name: delete_tmg_timecoverage_start(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_timecoverage_start(p_tmg_timecoverage_start_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_timecoverage_start(p_tmg_timecoverage_start_id integer) OWNER TO postgres;

--
-- TOC entry 187 (class 1255 OID 44469)
-- Dependencies: 791 6
-- Name: delete_tmg_variables(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_variables(p_tmg_variables_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_variables(p_tmg_variables_id integer) OWNER TO postgres;

--
-- TOC entry 188 (class 1255 OID 44470)
-- Dependencies: 6 791
-- Name: delete_tmg_variables_variable(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_variables_variable(p_tmg_variables_variable_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_variables_variable(p_tmg_variables_variable_id integer) OWNER TO postgres;

--
-- TOC entry 189 (class 1255 OID 44471)
-- Dependencies: 791 6
-- Name: delete_tmg_variables_variablemap(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_tmg_variables_variablemap(p_tmg_variables_variablemap_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.delete_tmg_variables_variablemap(p_tmg_variables_variablemap_id integer) OWNER TO postgres;

--
-- TOC entry 190 (class 1255 OID 44477)
-- Dependencies: 6 791
-- Name: insert_catalog(text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog(p_clean_catalog_id text, p_base text, p_expires text, p_name text, p_version text, p_xmlns text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog("name", "expires", "version", "base", "xmlns") values (p_name, p_expires, p_version, p_base, p_xmlns);
		select currval('catalog_catalog_id_seq') into id;
		update catalog set status = cast(p_status as status) where catalog_id=id;
		if(length(p_clean_catalog_id) > 0 and p_clean_catalog_id <> '-1') then
			update catalog set clean_catalog_id=cast(p_clean_catalog_id as int) where catalog_id=id;
		end if;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalog(p_clean_catalog_id text, p_base text, p_expires text, p_name text, p_version text, p_xmlns text, p_status text) OWNER TO postgres;

--
-- TOC entry 18 (class 1255 OID 44300)
-- Dependencies: 6 791
-- Name: insert_catalog_dataset(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_dataset(p_catalog_id integer, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_dataset("catalog_id", "dataset_id") values (p_catalog_id, p_dataset_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_catalog_dataset(p_catalog_id integer, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 19 (class 1255 OID 44301)
-- Dependencies: 6 791
-- Name: insert_catalog_property(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_property(p_catalog_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_property("catalog_id", "name", "value") values (p_catalog_id, p_name, p_value);
		select currval('catalog_property_catalog_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalog_property(p_catalog_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 20 (class 1255 OID 44302)
-- Dependencies: 6 791
-- Name: insert_catalog_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_service(p_catalog_id integer, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_service("catalog_id", "service_id") values (p_catalog_id, p_service_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_catalog_service(p_catalog_id integer, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 21 (class 1255 OID 44303)
-- Dependencies: 6 791
-- Name: insert_catalog_xlink(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_xlink(p_catalog_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_catalog_xlink(p_catalog_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 22 (class 1255 OID 44304)
-- Dependencies: 791 6
-- Name: insert_catalogref(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref("not_empty") values ('true');
		select currval('catalogref_catalogref_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref() OWNER TO postgres;

--
-- TOC entry 23 (class 1255 OID 44305)
-- Dependencies: 791 6
-- Name: insert_catalogref_documentation(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation(p_catalogref_id integer, p_value text, p_documentationenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_catalogref_documentation(p_catalogref_id integer, p_value text, p_documentationenum text) OWNER TO postgres;

--
-- TOC entry 24 (class 1255 OID 44306)
-- Dependencies: 6 791
-- Name: insert_catalogref_documentation_namespace(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation_namespace(p_catalogref_documentation_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation_namespace("catalogref_documentation_id", "namespace") values (p_catalogref_documentation_id, p_namespace);
		select currval('catalogref_documentation_namespace_catalogref_documentation_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref_documentation_namespace(p_catalogref_documentation_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 25 (class 1255 OID 44307)
-- Dependencies: 791 6
-- Name: insert_catalogref_documentation_xlink(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation_xlink(p_catalogref_documentation_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_catalogref_documentation_xlink(p_catalogref_documentation_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 26 (class 1255 OID 44308)
-- Dependencies: 791 6
-- Name: insert_catalogref_xlink(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_xlink(p_catalogref_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_catalogref_xlink(p_catalogref_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 28 (class 1255 OID 44309)
-- Dependencies: 6 791
-- Name: insert_dataset(text, text, text, text, text, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset(p_alias text, p_authority text, p_d_id text, p_harvest text, p_name text, p_resourcecontrol text, p_servicename text, p_urlpath text, p_collectiontype text, p_datasize_unit text, p_datatype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_dataset(p_alias text, p_authority text, p_d_id text, p_harvest text, p_name text, p_resourcecontrol text, p_servicename text, p_urlpath text, p_collectiontype text, p_datasize_unit text, p_datatype text, p_status text) OWNER TO postgres;

--
-- TOC entry 29 (class 1255 OID 44310)
-- Dependencies: 6 791
-- Name: insert_dataset_access(integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_access(p_dataset_id integer, p_servicename text, p_urlpath text, p_dataformat text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_dataset_access(p_dataset_id integer, p_servicename text, p_urlpath text, p_dataformat text) OWNER TO postgres;

--
-- TOC entry 30 (class 1255 OID 44311)
-- Dependencies: 791 6
-- Name: insert_dataset_access_datasize(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_access_datasize(p_dataset_access_id integer, p_value text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_dataset_access_datasize(p_dataset_access_id integer, p_value text, p_units text) OWNER TO postgres;

--
-- TOC entry 31 (class 1255 OID 44312)
-- Dependencies: 791 6
-- Name: insert_dataset_catalogref(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_catalogref(p_dataset_id integer, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_catalogref("dataset_id", "catalogref_id") values (p_dataset_id, p_catalogref_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_dataset_catalogref(p_dataset_id integer, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 32 (class 1255 OID 44313)
-- Dependencies: 6 791
-- Name: insert_dataset_dataset(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_dataset(p_parent_id integer, p_child_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_dataset("parent_id", "child_id") values (p_parent_id, p_child_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_dataset_dataset(p_parent_id integer, p_child_id integer) OWNER TO postgres;

--
-- TOC entry 33 (class 1255 OID 44314)
-- Dependencies: 6 791
-- Name: insert_dataset_ncml(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_ncml(p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_ncml("dataset_id") values (p_dataset_id);
		select currval('dataset_ncml_dataset_ncml_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_dataset_ncml(p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 34 (class 1255 OID 44315)
-- Dependencies: 6 791
-- Name: insert_dataset_property(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_property(p_dataset_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_property("dataset_id", "name", "value") values (p_dataset_id, p_name, p_value);
		select currval('dataset_property_dataset_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_dataset_property(p_dataset_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 35 (class 1255 OID 44316)
-- Dependencies: 6 791
-- Name: insert_dataset_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_service(p_dataset_id integer, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_service("dataset_id", "service_id") values (p_dataset_id, p_service_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_dataset_service(p_dataset_id integer, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 36 (class 1255 OID 44317)
-- Dependencies: 6 791
-- Name: insert_dataset_tmg(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_tmg(p_dataset_id integer, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_tmg("dataset_id", "tmg_id") values (p_dataset_id, p_tmg_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_dataset_tmg(p_dataset_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 37 (class 1255 OID 44318)
-- Dependencies: 791 6
-- Name: insert_metadata(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata(p_inherited text, p_metadatatype text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_metadata(p_inherited text, p_metadatatype text) OWNER TO postgres;

--
-- TOC entry 38 (class 1255 OID 44319)
-- Dependencies: 791 6
-- Name: insert_metadata_namespace(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_namespace(p_metadata_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_namespace("metadata_id", "namespace") values (p_metadata_id, p_namespace);
		select currval('metadata_namespace_metadata_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_metadata_namespace(p_metadata_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 39 (class 1255 OID 44320)
-- Dependencies: 791 6
-- Name: insert_metadata_tmg(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_tmg(p_metadata_id integer, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_tmg("metadata_id", "tmg_id") values (p_metadata_id, p_tmg_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_metadata_tmg(p_metadata_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 40 (class 1255 OID 44321)
-- Dependencies: 791 6
-- Name: insert_metadata_xlink(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_xlink(p_metadata_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_metadata_xlink(p_metadata_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 27 (class 1255 OID 44322)
-- Dependencies: 791 6
-- Name: insert_service(text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service(p_base text, p_desc text, p_name text, p_suffix text, p_servicetype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_service(p_base text, p_desc text, p_name text, p_suffix text, p_servicetype text, p_status text) OWNER TO postgres;

--
-- TOC entry 41 (class 1255 OID 44323)
-- Dependencies: 6 791
-- Name: insert_service_datasetroot(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_datasetroot(p_service_id integer, p_location text, p_path text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_datasetroot("service_id", "location", "path") values (p_service_id, p_location, p_path);
		select currval('service_datasetroot_service_datasetroot_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_service_datasetroot(p_service_id integer, p_location text, p_path text) OWNER TO postgres;

--
-- TOC entry 42 (class 1255 OID 44324)
-- Dependencies: 791 6
-- Name: insert_service_property(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_property(p_service_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_property("service_id", "name", "value") values (p_service_id, p_name, p_value);
		select currval('service_property_service_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_service_property(p_service_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 43 (class 1255 OID 44325)
-- Dependencies: 6 791
-- Name: insert_service_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_service(p_parent_id integer, p_child_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_service("parent_id", "child_id") values (p_parent_id, p_child_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_service_service(p_parent_id integer, p_child_id integer) OWNER TO postgres;

--
-- TOC entry 44 (class 1255 OID 44326)
-- Dependencies: 6 791
-- Name: insert_tmg(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg("not_empty") values ('true');
		select currval('tmg_tmg_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg() OWNER TO postgres;

--
-- TOC entry 45 (class 1255 OID 44327)
-- Dependencies: 6 791
-- Name: insert_tmg_authority(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_authority(p_tmg_id integer, p_authority text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_authority("tmg_id", "authority") values (p_tmg_id, p_authority);
		select currval('tmg_authority_tmg_authority_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_authority(p_tmg_id integer, p_authority text) OWNER TO postgres;

--
-- TOC entry 46 (class 1255 OID 44328)
-- Dependencies: 791 6
-- Name: insert_tmg_contributor(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_contributor(p_tmg_id integer, p_name text, p_role text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_contributor("tmg_id", "name", "role") values (p_tmg_id, p_name, p_role);
		select currval('tmg_contributor_tmg_contributor_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_contributor(p_tmg_id integer, p_name text, p_role text) OWNER TO postgres;

--
-- TOC entry 47 (class 1255 OID 44329)
-- Dependencies: 791 6
-- Name: insert_tmg_creator(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_creator(p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator("tmg_id") values (p_tmg_id);
		select currval('tmg_creator_tmg_creator_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_creator(p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 48 (class 1255 OID 44330)
-- Dependencies: 6 791
-- Name: insert_tmg_creator_contact(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_creator_contact(p_tmg_creator_id integer, p_email text, p_url text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_contact("tmg_creator_id", "email", "url") values (p_tmg_creator_id, p_email, p_url);
		select currval('tmg_creator_contact_tmg_creator_contact_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_creator_contact(p_tmg_creator_id integer, p_email text, p_url text) OWNER TO postgres;

--
-- TOC entry 49 (class 1255 OID 44331)
-- Dependencies: 791 6
-- Name: insert_tmg_creator_name(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_creator_name(p_tmg_creator_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_name("tmg_creator_id", "value", "vocabulary") values (p_tmg_creator_id, p_value, p_vocabulary);
		select currval('tmg_creator_name_tmg_creator_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_creator_name(p_tmg_creator_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 50 (class 1255 OID 44332)
-- Dependencies: 6 791
-- Name: insert_tmg_dataformat(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_dataformat(p_tmg_id integer, p_dataformat text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_dataformat(p_tmg_id integer, p_dataformat text) OWNER TO postgres;

--
-- TOC entry 51 (class 1255 OID 44333)
-- Dependencies: 791 6
-- Name: insert_tmg_datasize(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_datasize(p_tmg_id integer, p_value text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_datasize(p_tmg_id integer, p_value text, p_units text) OWNER TO postgres;

--
-- TOC entry 52 (class 1255 OID 44334)
-- Dependencies: 791 6
-- Name: insert_tmg_datatype(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_datatype(p_tmg_id integer, p_datatype text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_datatype(p_tmg_id integer, p_datatype text) OWNER TO postgres;

--
-- TOC entry 53 (class 1255 OID 44335)
-- Dependencies: 6 791
-- Name: insert_tmg_date(integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_date(p_tmg_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_date(p_tmg_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 54 (class 1255 OID 44336)
-- Dependencies: 6 791
-- Name: insert_tmg_documentation(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation(p_tmg_id integer, p_value text, p_documentationenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_documentation(p_tmg_id integer, p_value text, p_documentationenum text) OWNER TO postgres;

--
-- TOC entry 55 (class 1255 OID 44337)
-- Dependencies: 791 6
-- Name: insert_tmg_documentation_namespace(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation_namespace(p_tmg_documentation_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation_namespace("tmg_documentation_id", "namespace") values (p_tmg_documentation_id, p_namespace);
		select currval('tmg_documentation_namespace_tmg_documentation_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_documentation_namespace(p_tmg_documentation_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 56 (class 1255 OID 44338)
-- Dependencies: 6 791
-- Name: insert_tmg_documentation_xlink(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation_xlink(p_tmg_documentation_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_documentation_xlink(p_tmg_documentation_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 57 (class 1255 OID 44339)
-- Dependencies: 6 791
-- Name: insert_tmg_geospatialcoverage(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage(p_tmg_id integer, p_upordown text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage(p_tmg_id integer, p_upordown text) OWNER TO postgres;

--
-- TOC entry 58 (class 1255 OID 44340)
-- Dependencies: 791 6
-- Name: insert_tmg_geospatialcoverage_eastwest(integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_eastwest("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_eastwest_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 59 (class 1255 OID 44341)
-- Dependencies: 6 791
-- Name: insert_tmg_geospatialcoverage_name(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_name("tmg_geospatialcoverage_id", "value", "vocabulary") values (p_tmg_geospatialcoverage_id, p_value, p_vocabulary);
		select currval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 60 (class 1255 OID 44342)
-- Dependencies: 6 791
-- Name: insert_tmg_geospatialcoverage_northsouth(integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_northsouth("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_northsouth_tmg_geospatialcoverage_northsouth_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 61 (class 1255 OID 44343)
-- Dependencies: 791 6
-- Name: insert_tmg_geospatialcoverage_updown(integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_updown("tmg_geospatialcoverage_id", "resolution", "size", "start", "units") values (p_tmg_geospatialcoverage_id, p_resolution, p_size, p_start, p_units);
		select currval('tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 62 (class 1255 OID 44344)
-- Dependencies: 6 791
-- Name: insert_tmg_keyword(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_keyword(p_tmg_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_keyword("tmg_id", "value", "vocabulary") values (p_tmg_id, p_value, p_vocabulary);
		select currval('tmg_keyword_tmg_keyword_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_keyword(p_tmg_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 63 (class 1255 OID 44345)
-- Dependencies: 6 791
-- Name: insert_tmg_metadata(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_metadata(p_tmg_id integer, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_metadata("tmg_id", "metadata_id") values (p_tmg_id, p_metadata_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_tmg_metadata(p_tmg_id integer, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 64 (class 1255 OID 44346)
-- Dependencies: 6 791
-- Name: insert_tmg_project(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_project(p_tmg_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_project("tmg_id", "value", "vocabulary") values (p_tmg_id, p_value, p_vocabulary);
		select currval('tmg_project_tmg_project_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_project(p_tmg_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 65 (class 1255 OID 44347)
-- Dependencies: 6 791
-- Name: insert_tmg_property(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_property(p_tmg_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_property("tmg_id", "name", "value") values (p_tmg_id, p_name, p_value);
		select currval('tmg_property_tmg_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_property(p_tmg_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 66 (class 1255 OID 44348)
-- Dependencies: 6 791
-- Name: insert_tmg_publisher(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_publisher(p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher("tmg_id") values (p_tmg_id);
		select currval('tmg_publisher_tmg_publisher_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_publisher(p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 67 (class 1255 OID 44349)
-- Dependencies: 6 791
-- Name: insert_tmg_publisher_contact(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_publisher_contact(p_tmg_publisher_id integer, p_email text, p_url text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_contact("tmg_publisher_id", "email", "url") values (p_tmg_publisher_id, p_email, p_url);
		select currval('tmg_publisher_contact_tmg_publisher_contact_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_publisher_contact(p_tmg_publisher_id integer, p_email text, p_url text) OWNER TO postgres;

--
-- TOC entry 68 (class 1255 OID 44350)
-- Dependencies: 6 791
-- Name: insert_tmg_publisher_name(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_publisher_name(p_tmg_publisher_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_name("tmg_publisher_id", "value", "vocabulary") values (p_tmg_publisher_id, p_value, p_vocabulary);
		select currval('tmg_publisher_name_tmg_publisher_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_publisher_name(p_tmg_publisher_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 69 (class 1255 OID 44351)
-- Dependencies: 791 6
-- Name: insert_tmg_servicename(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_servicename(p_tmg_id integer, p_servicename text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_servicename("tmg_id", "servicename") values (p_tmg_id, p_servicename);
		select currval('tmg_servicename_tmg_servicename_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_servicename(p_tmg_id integer, p_servicename text) OWNER TO postgres;

--
-- TOC entry 70 (class 1255 OID 44352)
-- Dependencies: 6 791
-- Name: insert_tmg_timecoverage(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage(p_tmg_id integer, p_resolution text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage("tmg_id", "resolution") values (p_tmg_id, p_resolution);
		select currval('tmg_timecoverage_tmg_timecoverage_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage(p_tmg_id integer, p_resolution text) OWNER TO postgres;

--
-- TOC entry 71 (class 1255 OID 44353)
-- Dependencies: 6 791
-- Name: insert_tmg_timecoverage_duration(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_duration(p_tmg_timecoverage_id integer, p_duration text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_duration("tmg_timecoverage_id", "duration") values (p_tmg_timecoverage_id, p_duration);
		select currval('tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_duration(p_tmg_timecoverage_id integer, p_duration text) OWNER TO postgres;

--
-- TOC entry 73 (class 1255 OID 44354)
-- Dependencies: 6 791
-- Name: insert_tmg_timecoverage_end(integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_end(p_tmg_timecoverage_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_end(p_tmg_timecoverage_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 74 (class 1255 OID 44355)
-- Dependencies: 791 6
-- Name: insert_tmg_timecoverage_resolution(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_resolution(p_tmg_timecoverage_id integer, p_duration text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_resolution("tmg_timecoverage_id", "duration") values (p_tmg_timecoverage_id, p_duration);
		select currval('tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_resolution(p_tmg_timecoverage_id integer, p_duration text) OWNER TO postgres;

--
-- TOC entry 75 (class 1255 OID 44356)
-- Dependencies: 6 791
-- Name: insert_tmg_timecoverage_start(integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_start(p_tmg_timecoverage_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_start(p_tmg_timecoverage_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 76 (class 1255 OID 44357)
-- Dependencies: 6 791
-- Name: insert_tmg_variables(integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables(p_tmg_id integer, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_variables(p_tmg_id integer, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 77 (class 1255 OID 44358)
-- Dependencies: 6 791
-- Name: insert_tmg_variables_variable(integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables_variable(p_tmg_variables_id integer, p_name text, p_units text, p_vocabulary_name text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variable("tmg_variables_id", "name", "units", "vocabulary_name") values (p_tmg_variables_id, p_name, p_units, p_vocabulary_name);
		select currval('tmg_variables_variable_tmg_variables_variable_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_variables_variable(p_tmg_variables_id integer, p_name text, p_units text, p_vocabulary_name text) OWNER TO postgres;

--
-- TOC entry 78 (class 1255 OID 44359)
-- Dependencies: 6 791
-- Name: insert_tmg_variables_variablemap(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables_variablemap(p_tmg_variables_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_variables_variablemap(p_tmg_variables_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 191 (class 1255 OID 44478)
-- Dependencies: 791 6
-- Name: update_catalog(integer, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog(p_catalog_id integer, p_clean_catalog_id text, p_base text, p_expires text, p_name text, p_version text, p_xmlns text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalog_id into id from catalog where catalog_id=p_catalog_id;
		if(id is null) then
			return -1;
		end if;
		update catalog set "name"=p_name, "expires"=p_expires, "version"=p_version, "base"=p_base, "xmlns"=p_xmlns where catalog_id=id;
		update catalog set status = cast(p_status as status) where catalog_id=id;
		if(length(p_clean_catalog_id) > 0 and p_clean_catalog_id <> '-1') then
			update catalog set clean_catalog_id=cast(p_clean_catalog_id as int) where catalog_id=id;
		end if;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalog(p_catalog_id integer, p_clean_catalog_id text, p_base text, p_expires text, p_name text, p_version text, p_xmlns text, p_status text) OWNER TO postgres;

--
-- TOC entry 79 (class 1255 OID 44360)
-- Dependencies: 6 791
-- Name: update_catalog_property(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog_property(p_catalog_id integer, p_catalog_property_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_catalog_property(p_catalog_id integer, p_catalog_property_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 80 (class 1255 OID 44361)
-- Dependencies: 6 791
-- Name: update_catalog_xlink(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog_xlink(p_catalog_id integer, p_catalog_xlink_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalog_xlink_id into id from catalog_xlink where catalog_xlink_id=p_catalog_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalog_xlink set "catalog_id"=p_catalog_id, "value"=p_value where catalog_xlink_id=id;
		BEGIN
			update catalog_xlink set "xlink" = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set "xlink_nonstandard" = p_xlink where catalog_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalog_xlink(p_catalog_id integer, p_catalog_xlink_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 81 (class 1255 OID 44362)
-- Dependencies: 6 791
-- Name: update_catalogref(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref(p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_id into id from catalogref where catalogref_id=p_catalogref_id;
		if(id is null) then
			return -1;
		end if;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref(p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 82 (class 1255 OID 44363)
-- Dependencies: 6 791
-- Name: update_catalogref_documentation(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation(p_catalogref_id integer, p_catalogref_documentation_id integer, p_value text, p_documentationenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_id into id from catalogref_documentation where catalogref_documentation_id=p_catalogref_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation set "catalogref_id"=p_catalogref_id, "value"=p_value where catalogref_documentation_id=id;
		BEGIN
			update catalogref_documentation set "documentationenum" = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set "documentationenum_nonstandard" = p_documentationenum where catalogref_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_documentation(p_catalogref_id integer, p_catalogref_documentation_id integer, p_value text, p_documentationenum text) OWNER TO postgres;

--
-- TOC entry 83 (class 1255 OID 44364)
-- Dependencies: 6 791
-- Name: update_catalogref_documentation_namespace(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation_namespace(p_catalogref_documentation_id integer, p_catalogref_documentation_namespace_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_catalogref_documentation_namespace(p_catalogref_documentation_id integer, p_catalogref_documentation_namespace_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 85 (class 1255 OID 44365)
-- Dependencies: 6 791
-- Name: update_catalogref_documentation_xlink(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation_xlink(p_catalogref_documentation_id integer, p_catalogref_documentation_xlink_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_xlink_id into id from catalogref_documentation_xlink where catalogref_documentation_xlink_id=p_catalogref_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation_xlink set "catalogref_documentation_id"=p_catalogref_documentation_id, "value"=p_value where catalogref_documentation_xlink_id=id;
		BEGIN
			update catalogref_documentation_xlink set "xlink" = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set "xlink_nonstandard" = p_xlink where catalogref_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_documentation_xlink(p_catalogref_documentation_id integer, p_catalogref_documentation_xlink_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 86 (class 1255 OID 44366)
-- Dependencies: 6 791
-- Name: update_catalogref_xlink(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_xlink(p_catalogref_id integer, p_catalogref_xlink_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_xlink_id into id from catalogref_xlink where catalogref_xlink_id=p_catalogref_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_xlink set "catalogref_id"=p_catalogref_id, "value"=p_value where catalogref_xlink_id=id;
		BEGIN
			update catalogref_xlink set "xlink" = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set "xlink_nonstandard" = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_xlink(p_catalogref_id integer, p_catalogref_xlink_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 87 (class 1255 OID 44367)
-- Dependencies: 791 6
-- Name: update_dataset(integer, text, text, text, text, text, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset(p_dataset_id integer, p_alias text, p_authority text, p_d_id text, p_harvest text, p_name text, p_resourcecontrol text, p_servicename text, p_urlpath text, p_collectiontype text, p_datasize_unit text, p_datatype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_id into id from dataset where dataset_id=p_dataset_id;
		if(id is null) then
			return -1;
		end if;
		update dataset set "alias"=p_alias, "authority"=p_authority, "d_id"=p_d_id, "harvest"=p_harvest, "name"=p_name, "resourcecontrol"=p_resourcecontrol, "serviceName"=p_serviceName, "urlPath"=p_urlPath where dataset_id=id;
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
$$;


ALTER FUNCTION public.update_dataset(p_dataset_id integer, p_alias text, p_authority text, p_d_id text, p_harvest text, p_name text, p_resourcecontrol text, p_servicename text, p_urlpath text, p_collectiontype text, p_datasize_unit text, p_datatype text, p_status text) OWNER TO postgres;

--
-- TOC entry 88 (class 1255 OID 44368)
-- Dependencies: 6 791
-- Name: update_dataset_access(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_access(p_dataset_id integer, p_dataset_access_id integer, p_servicename text, p_urlpath text, p_dataformat text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_id into id from dataset_access where dataset_access_id=p_dataset_access_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access set "dataset_id"=p_dataset_id, "servicename"=p_servicename, "urlpath"=p_urlpath where dataset_access_id=id;
		BEGIN
			update dataset_access set "dataformat" = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set "dataformat_nonstandard" = p_dataformat where dataset_access_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_dataset_access(p_dataset_id integer, p_dataset_access_id integer, p_servicename text, p_urlpath text, p_dataformat text) OWNER TO postgres;

--
-- TOC entry 89 (class 1255 OID 44369)
-- Dependencies: 6 791
-- Name: update_dataset_access_datasize(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_access_datasize(p_dataset_access_id integer, p_dataset_access_datasize_id integer, p_value text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_datasize_id into id from dataset_access_datasize where dataset_access_datasize_id=p_dataset_access_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access_datasize set "dataset_access_id"=p_dataset_access_id, "value"=p_value where dataset_access_datasize_id=id;
		BEGIN
			update dataset_access_datasize set "units" = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set "units_nonstandard" = p_units where dataset_access_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_dataset_access_datasize(p_dataset_access_id integer, p_dataset_access_datasize_id integer, p_value text, p_units text) OWNER TO postgres;

--
-- TOC entry 90 (class 1255 OID 44370)
-- Dependencies: 6 791
-- Name: update_dataset_ncml(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_ncml(p_dataset_id integer, p_dataset_ncml_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_dataset_ncml(p_dataset_id integer, p_dataset_ncml_id integer) OWNER TO postgres;

--
-- TOC entry 91 (class 1255 OID 44371)
-- Dependencies: 6 791
-- Name: update_dataset_property(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_property(p_dataset_id integer, p_dataset_property_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_dataset_property(p_dataset_id integer, p_dataset_property_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 92 (class 1255 OID 44372)
-- Dependencies: 6 791
-- Name: update_metadata(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata(p_metadata_id integer, p_inherited text, p_metadatatype text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select metadata_id into id from metadata where metadata_id=p_metadata_id;
		if(id is null) then
			return -1;
		end if;
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
$$;


ALTER FUNCTION public.update_metadata(p_metadata_id integer, p_inherited text, p_metadatatype text) OWNER TO postgres;

--
-- TOC entry 93 (class 1255 OID 44373)
-- Dependencies: 6 791
-- Name: update_metadata_namespace(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata_namespace(p_metadata_id integer, p_metadata_namespace_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_metadata_namespace(p_metadata_id integer, p_metadata_namespace_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 94 (class 1255 OID 44374)
-- Dependencies: 6 791
-- Name: update_metadata_xlink(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata_xlink(p_metadata_id integer, p_metadata_xlink_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select metadata_xlink_id into id from metadata_xlink where metadata_xlink_id=p_metadata_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update metadata_xlink set "metadata_id"=p_metadata_id, "value"=p_value where metadata_xlink_id=id;
		BEGIN
			update metadata_xlink set "xlink" = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set "xlink_nonstandard" = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_metadata_xlink(p_metadata_id integer, p_metadata_xlink_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 95 (class 1255 OID 44375)
-- Dependencies: 6 791
-- Name: update_service(integer, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service(p_service_id integer, p_base text, p_desc text, p_name text, p_suffix text, p_servicetype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select service_id into id from service where service_id=p_service_id;
		if(id is null) then
			return -1;
		end if;
		update service set "base"=p_base, "desc"=p_desc, "name"=p_name, "suffix"=p_suffix where service_id=id;
		BEGIN
			update service set "serviceType" = cast(p_serviceType as serviceType) where service_id=id;
		EXCEPTION
			when others then
				update service set "serviceType_nonstandard" = p_serviceType where service_id=id;
		END;
		update service set "status" = cast(p_status as status) where service_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_service(p_service_id integer, p_base text, p_desc text, p_name text, p_suffix text, p_servicetype text, p_status text) OWNER TO postgres;

--
-- TOC entry 96 (class 1255 OID 44376)
-- Dependencies: 6 791
-- Name: update_service_datasetroot(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service_datasetroot(p_service_id integer, p_service_datasetroot_id integer, p_location text, p_path text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select service_datasetroot_id into id from service_datasetroot where service_datasetroot_id=p_service_datasetroot_id;
		if(id is null) then
			return -1;
		end if;
		update service_datasetroot set "service_id"=p_service_id, "location"=p_location, "path"=p_path where service_datasetroot_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_service_datasetroot(p_service_id integer, p_service_datasetroot_id integer, p_location text, p_path text) OWNER TO postgres;

--
-- TOC entry 97 (class 1255 OID 44377)
-- Dependencies: 6 791
-- Name: update_service_property(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service_property(p_service_id integer, p_service_property_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select service_property_id into id from service_property where service_property_id=p_service_property_id;
		if(id is null) then
			return -1;
		end if;
		update service_property set "service_id"=p_service_id, "name"=p_name, "value"=p_value where service_property_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_service_property(p_service_id integer, p_service_property_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 98 (class 1255 OID 44378)
-- Dependencies: 6 791
-- Name: update_tmg(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg(p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_id into id from tmg where tmg_id=p_tmg_id;
		if(id is null) then
			return -1;
		end if;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg(p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 99 (class 1255 OID 44379)
-- Dependencies: 791 6
-- Name: update_tmg_authority(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_authority(p_tmg_id integer, p_tmg_authority_id integer, p_authority text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_authority(p_tmg_id integer, p_tmg_authority_id integer, p_authority text) OWNER TO postgres;

--
-- TOC entry 100 (class 1255 OID 44380)
-- Dependencies: 6 791
-- Name: update_tmg_contributor(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_contributor(p_tmg_id integer, p_tmg_contributor_id integer, p_name text, p_role text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_contributor_id into id from tmg_contributor where tmg_contributor_id=p_tmg_contributor_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_contributor set "tmg_id"=p_tmg_id, "name"=p_name, "role"=p_role where tmg_contributor_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_contributor(p_tmg_id integer, p_tmg_contributor_id integer, p_name text, p_role text) OWNER TO postgres;

--
-- TOC entry 101 (class 1255 OID 44381)
-- Dependencies: 6 791
-- Name: update_tmg_creator(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator(p_tmg_id integer, p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_creator(p_tmg_id integer, p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 102 (class 1255 OID 44382)
-- Dependencies: 6 791
-- Name: update_tmg_creator_contact(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator_contact(p_tmg_creator_id integer, p_tmg_creator_contact_id integer, p_email text, p_url text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_creator_contact(p_tmg_creator_id integer, p_tmg_creator_contact_id integer, p_email text, p_url text) OWNER TO postgres;

--
-- TOC entry 103 (class 1255 OID 44383)
-- Dependencies: 6 791
-- Name: update_tmg_creator_name(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator_name(p_tmg_creator_id integer, p_tmg_creator_name_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_creator_name(p_tmg_creator_id integer, p_tmg_creator_name_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 104 (class 1255 OID 44384)
-- Dependencies: 6 791
-- Name: update_tmg_dataformat(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_dataformat(p_tmg_id integer, p_tmg_dataformat_id integer, p_dataformat text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_dataformat_id into id from tmg_dataformat where tmg_dataformat_id=p_tmg_dataformat_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_dataformat set "tmg_id"=p_tmg_id where tmg_dataformat_id=id;
		BEGIN
			update tmg_dataformat set "dataformat" = cast(p_dataformat as dataformat) where tmg_dataformat_id=id;
		EXCEPTION
			when others then
				update tmg_dataformat set "dataformat_nonstandard" = p_dataformat where tmg_dataformat_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_dataformat(p_tmg_id integer, p_tmg_dataformat_id integer, p_dataformat text) OWNER TO postgres;

--
-- TOC entry 105 (class 1255 OID 44385)
-- Dependencies: 6 791
-- Name: update_tmg_datasize(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_datasize(p_tmg_id integer, p_tmg_datasize_id integer, p_value text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_datasize_id into id from tmg_datasize where tmg_datasize_id=p_tmg_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_datasize set "tmg_id"=p_tmg_id, "value"=p_value where tmg_datasize_id=id;
		BEGIN
			update tmg_datasize set "units" = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set "units_nonstandard" = p_units where tmg_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_datasize(p_tmg_id integer, p_tmg_datasize_id integer, p_value text, p_units text) OWNER TO postgres;

--
-- TOC entry 107 (class 1255 OID 44386)
-- Dependencies: 6 791
-- Name: update_tmg_datatype(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_datatype(p_tmg_id integer, p_tmg_datatype_id integer, p_datatype text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_datatype_id into id from tmg_datatype where tmg_datatype_id=p_tmg_datatype_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_datatype set "tmg_id"=p_tmg_id where tmg_datatype_id=id;
		BEGIN
			update tmg_datatype set "datatype" = cast(p_datatype as datatype) where tmg_datatype_id=id;
		EXCEPTION
			when others then
				update tmg_datatype set "datatype_nonstandard" = p_datatype where tmg_datatype_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_datatype(p_tmg_id integer, p_tmg_datatype_id integer, p_datatype text) OWNER TO postgres;

--
-- TOC entry 108 (class 1255 OID 44387)
-- Dependencies: 791 6
-- Name: update_tmg_date(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_date(p_tmg_id integer, p_tmg_date_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_date_id into id from tmg_date where tmg_date_id=p_tmg_date_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_date set "tmg_id"=p_tmg_id, "format"=p_format, "value"=p_value where tmg_date_id=id;
		BEGIN
			update tmg_date set "dateenum" = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set "dateenum_nonstandard" = p_dateenum where tmg_date_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_date(p_tmg_id integer, p_tmg_date_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 109 (class 1255 OID 44388)
-- Dependencies: 6 791
-- Name: update_tmg_documentation(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation(p_tmg_id integer, p_tmg_documentation_id integer, p_value text, p_documentationenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_id into id from tmg_documentation where tmg_documentation_id=p_tmg_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation set "tmg_id"=p_tmg_id, "value"=p_value where tmg_documentation_id=id;
		BEGIN
			update tmg_documentation set "documentationenum" = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set "documentationenum_nonstandard" = p_documentationenum where tmg_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_documentation(p_tmg_id integer, p_tmg_documentation_id integer, p_value text, p_documentationenum text) OWNER TO postgres;

--
-- TOC entry 110 (class 1255 OID 44389)
-- Dependencies: 6 791
-- Name: update_tmg_documentation_namespace(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation_namespace(p_tmg_documentation_id integer, p_tmg_documentation_namespace_id integer, p_namespace text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_documentation_namespace(p_tmg_documentation_id integer, p_tmg_documentation_namespace_id integer, p_namespace text) OWNER TO postgres;

--
-- TOC entry 111 (class 1255 OID 44390)
-- Dependencies: 791 6
-- Name: update_tmg_documentation_xlink(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation_xlink(p_tmg_documentation_id integer, p_tmg_documentation_xlink_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_xlink_id into id from tmg_documentation_xlink where tmg_documentation_xlink_id=p_tmg_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation_xlink set "tmg_documentation_id"=p_tmg_documentation_id, "value"=p_value where tmg_documentation_xlink_id=id;
		BEGIN
			update tmg_documentation_xlink set "xlink" = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set "xlink_nonstandard" = p_xlink where tmg_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_documentation_xlink(p_tmg_documentation_id integer, p_tmg_documentation_xlink_id integer, p_value text, p_xlink text) OWNER TO postgres;

--
-- TOC entry 112 (class 1255 OID 44391)
-- Dependencies: 791 6
-- Name: update_tmg_geospatialcoverage(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage(p_tmg_id integer, p_tmg_geospatialcoverage_id integer, p_upordown text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_id into id from tmg_geospatialcoverage where tmg_geospatialcoverage_id=p_tmg_geospatialcoverage_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage set "tmg_id"=p_tmg_id where tmg_geospatialcoverage_id=id;
		BEGIN
			update tmg_geospatialcoverage set "upordown" = cast(p_upordown as upordown) where tmg_geospatialcoverage_id=id;
		EXCEPTION
			when others then
				update tmg_geospatialcoverage set "upordown_nonstandard" = p_upordown where tmg_geospatialcoverage_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage(p_tmg_id integer, p_tmg_geospatialcoverage_id integer, p_upordown text) OWNER TO postgres;

--
-- TOC entry 113 (class 1255 OID 44392)
-- Dependencies: 791 6
-- Name: update_tmg_geospatialcoverage_eastwest(integer, integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_eastwest_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_eastwest_id into id from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=p_tmg_geospatialcoverage_eastwest_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_eastwest set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "resolution"=p_resolution, "size"=p_size, "start"=p_start, "units"=p_units where tmg_geospatialcoverage_eastwest_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_eastwest_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 114 (class 1255 OID 44393)
-- Dependencies: 791 6
-- Name: update_tmg_geospatialcoverage_name(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_name_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_name_id into id from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=p_tmg_geospatialcoverage_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_name set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "value"=p_value, "vocabulary"=p_vocabulary where tmg_geospatialcoverage_name_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_name_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 115 (class 1255 OID 44394)
-- Dependencies: 791 6
-- Name: update_tmg_geospatialcoverage_northsouth(integer, integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_northsouth_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_northsouth_id into id from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=p_tmg_geospatialcoverage_northsouth_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_northsouth set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "resolution"=p_resolution, "size"=p_size, "start"=p_start, "units"=p_units where tmg_geospatialcoverage_northsouth_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_northsouth_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 116 (class 1255 OID 44395)
-- Dependencies: 791 6
-- Name: update_tmg_geospatialcoverage_updown(integer, integer, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_updown_id integer, p_resolution text, p_size text, p_start text, p_units text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_updown_id into id from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=p_tmg_geospatialcoverage_updown_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_updown set "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id, "resolution"=p_resolution, "size"=p_size, "start"=p_start, "units"=p_units where tmg_geospatialcoverage_updown_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_id integer, p_tmg_geospatialcoverage_updown_id integer, p_resolution text, p_size text, p_start text, p_units text) OWNER TO postgres;

--
-- TOC entry 84 (class 1255 OID 44396)
-- Dependencies: 6 791
-- Name: update_tmg_keyword(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_keyword(p_tmg_id integer, p_tmg_keyword_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_keyword(p_tmg_id integer, p_tmg_keyword_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 117 (class 1255 OID 44397)
-- Dependencies: 791 6
-- Name: update_tmg_project(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_project(p_tmg_id integer, p_tmg_project_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_project(p_tmg_id integer, p_tmg_project_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 118 (class 1255 OID 44398)
-- Dependencies: 791 6
-- Name: update_tmg_property(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_property(p_tmg_id integer, p_tmg_property_id integer, p_name text, p_value text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_property(p_tmg_id integer, p_tmg_property_id integer, p_name text, p_value text) OWNER TO postgres;

--
-- TOC entry 119 (class 1255 OID 44399)
-- Dependencies: 6 791
-- Name: update_tmg_publisher(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher(p_tmg_id integer, p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_publisher(p_tmg_id integer, p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 120 (class 1255 OID 44400)
-- Dependencies: 6 791
-- Name: update_tmg_publisher_contact(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher_contact(p_tmg_publisher_id integer, p_tmg_publisher_contact_id integer, p_email text, p_url text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_contact_id into id from tmg_publisher_contact where tmg_publisher_contact_id=p_tmg_publisher_contact_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher_contact set "tmg_publisher_id"=p_tmg_publisher_id, "email"=p_email, "url"=p_url where tmg_publisher_contact_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_publisher_contact(p_tmg_publisher_id integer, p_tmg_publisher_contact_id integer, p_email text, p_url text) OWNER TO postgres;

--
-- TOC entry 121 (class 1255 OID 44401)
-- Dependencies: 791 6
-- Name: update_tmg_publisher_name(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher_name(p_tmg_publisher_id integer, p_tmg_publisher_name_id integer, p_value text, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_publisher_name(p_tmg_publisher_id integer, p_tmg_publisher_name_id integer, p_value text, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 122 (class 1255 OID 44402)
-- Dependencies: 6 791
-- Name: update_tmg_servicename(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_servicename(p_tmg_id integer, p_tmg_servicename_id integer, p_servicename text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_servicename(p_tmg_id integer, p_tmg_servicename_id integer, p_servicename text) OWNER TO postgres;

--
-- TOC entry 123 (class 1255 OID 44403)
-- Dependencies: 791 6
-- Name: update_tmg_timecoverage(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage(p_tmg_id integer, p_tmg_timecoverage_id integer, p_resolution text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_timecoverage(p_tmg_id integer, p_tmg_timecoverage_id integer, p_resolution text) OWNER TO postgres;

--
-- TOC entry 124 (class 1255 OID 44404)
-- Dependencies: 6 791
-- Name: update_tmg_timecoverage_duration(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_duration(p_tmg_timecoverage_id integer, p_tmg_timecoverage_duration_id integer, p_duration text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_timecoverage_duration(p_tmg_timecoverage_id integer, p_tmg_timecoverage_duration_id integer, p_duration text) OWNER TO postgres;

--
-- TOC entry 125 (class 1255 OID 44405)
-- Dependencies: 791 6
-- Name: update_tmg_timecoverage_end(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_end(p_tmg_timecoverage_id integer, p_tmg_timecoverage_end_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_end_id into id from tmg_timecoverage_end where tmg_timecoverage_end_id=p_tmg_timecoverage_end_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_end set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "format"=p_format, "value"=p_value where tmg_timecoverage_end_id=id;
		BEGIN
			update tmg_timecoverage_end set "dateenum" = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set "dateenum_nonstandard" = p_dateenum where tmg_timecoverage_end_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_end(p_tmg_timecoverage_id integer, p_tmg_timecoverage_end_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 126 (class 1255 OID 44406)
-- Dependencies: 791 6
-- Name: update_tmg_timecoverage_resolution(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_resolution(p_tmg_timecoverage_id integer, p_tmg_timecoverage_resolution_id integer, p_duration text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_tmg_timecoverage_resolution(p_tmg_timecoverage_id integer, p_tmg_timecoverage_resolution_id integer, p_duration text) OWNER TO postgres;

--
-- TOC entry 127 (class 1255 OID 44407)
-- Dependencies: 6 791
-- Name: update_tmg_timecoverage_start(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_start(p_tmg_timecoverage_id integer, p_tmg_timecoverage_start_id integer, p_format text, p_value text, p_dateenum text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_start_id into id from tmg_timecoverage_start where tmg_timecoverage_start_id=p_tmg_timecoverage_start_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_start set "tmg_timecoverage_id"=p_tmg_timecoverage_id, "format"=p_format, "value"=p_value where tmg_timecoverage_start_id=id;
		BEGIN
			update tmg_timecoverage_start set "dateenum" = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set "dateenum_nonstandard" = p_dateenum where tmg_timecoverage_start_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_start(p_tmg_timecoverage_id integer, p_tmg_timecoverage_start_id integer, p_format text, p_value text, p_dateenum text) OWNER TO postgres;

--
-- TOC entry 128 (class 1255 OID 44408)
-- Dependencies: 791 6
-- Name: update_tmg_variables(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables(p_tmg_id integer, p_tmg_variables_id integer, p_vocabulary text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_id into id from tmg_variables where tmg_variables_id=p_tmg_variables_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables set "tmg_id"=p_tmg_id where tmg_variables_id=id;
		BEGIN
			update tmg_variables set "vocabulary" = cast(p_vocabulary as vocabulary) where tmg_variables_id=id;
		EXCEPTION
			when others then
				update tmg_variables set "vocabulary_nonstandard" = p_vocabulary where tmg_variables_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables(p_tmg_id integer, p_tmg_variables_id integer, p_vocabulary text) OWNER TO postgres;

--
-- TOC entry 129 (class 1255 OID 44409)
-- Dependencies: 791 6
-- Name: update_tmg_variables_variable(integer, integer, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables_variable(p_tmg_variables_id integer, p_tmg_variables_variable_id integer, p_name text, p_units text, p_vocabulary_name text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variable_id into id from tmg_variables_variable where tmg_variables_variable_id=p_tmg_variables_variable_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variable set "tmg_variables_id"=p_tmg_variables_id, "name"=p_name, "units"=p_units, "vocabulary_name"=p_vocabulary_name where tmg_variables_variable_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables_variable(p_tmg_variables_id integer, p_tmg_variables_variable_id integer, p_name text, p_units text, p_vocabulary_name text) OWNER TO postgres;

--
-- TOC entry 130 (class 1255 OID 44410)
-- Dependencies: 6 791
-- Name: update_tmg_variables_variablemap(integer, integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables_variablemap(p_tmg_variables_id integer, p_tmg_variables_variablemap_id integer, p_value text, p_xlink text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variablemap_id into id from tmg_variables_variablemap where tmg_variables_variablemap_id=p_tmg_variables_variablemap_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variablemap set "tmg_variables_id"=p_tmg_variables_id, "value"=p_value where tmg_variables_variablemap_id=id;
		BEGIN
			update tmg_variables_variablemap set "xlink" = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set "xlink_nonstandard" = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables_variablemap(p_tmg_variables_id integer, p_tmg_variables_variablemap_id integer, p_value text, p_xlink text) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1983 (class 1259 OID 40293)
-- Dependencies: 497 6
-- Name: catalog; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog (
    catalog_id integer NOT NULL,
    xmlns text,
    name text,
    status status,
    base text,
    version text,
    expires text,
    clean_catalog_id integer
);


ALTER TABLE public.catalog OWNER TO cleaner;

--
-- TOC entry 2632 (class 0 OID 0)
-- Dependencies: 1983
-- Name: COLUMN catalog.base; Type: COMMENT; Schema: public; Owner: cleaner
--

COMMENT ON COLUMN catalog.base IS 'deprecated: not working, don''t use (as of 1.0.2)';


--
-- TOC entry 1984 (class 1259 OID 40299)
-- Dependencies: 6 1983
-- Name: catalog_catalog_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalog_catalog_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalog_catalog_id_seq OWNER TO cleaner;

--
-- TOC entry 2633 (class 0 OID 0)
-- Dependencies: 1984
-- Name: catalog_catalog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_catalog_id_seq OWNED BY catalog.catalog_id;


--
-- TOC entry 1985 (class 1259 OID 40301)
-- Dependencies: 6
-- Name: catalog_dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_dataset (
    catalog_id integer NOT NULL,
    dataset_id integer NOT NULL
);


ALTER TABLE public.catalog_dataset OWNER TO cleaner;

--
-- TOC entry 1986 (class 1259 OID 40304)
-- Dependencies: 6
-- Name: template_property; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_property (
    name text,
    value text
);


ALTER TABLE public.template_property OWNER TO postgres;

--
-- TOC entry 2634 (class 0 OID 0)
-- Dependencies: 1986
-- Name: TABLE template_property; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_property IS 'abstract template only for service_property, dataset_property, or catalog_property';


--
-- TOC entry 1987 (class 1259 OID 40310)
-- Dependencies: 1986 6
-- Name: catalog_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_property (
    catalog_id integer,
    catalog_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.catalog_property OWNER TO cleaner;

--
-- TOC entry 1988 (class 1259 OID 40316)
-- Dependencies: 1987 6
-- Name: catalog_property_catalog_property_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalog_property_catalog_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalog_property_catalog_property_id_seq OWNER TO cleaner;

--
-- TOC entry 2635 (class 0 OID 0)
-- Dependencies: 1988
-- Name: catalog_property_catalog_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_property_catalog_property_id_seq OWNED BY catalog_property.catalog_property_id;


--
-- TOC entry 1989 (class 1259 OID 40318)
-- Dependencies: 6
-- Name: catalog_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_service (
    catalog_id integer NOT NULL,
    service_id integer NOT NULL
);


ALTER TABLE public.catalog_service OWNER TO cleaner;

--
-- TOC entry 1990 (class 1259 OID 40321)
-- Dependencies: 505 6
-- Name: template_xlink; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_xlink (
    value text,
    xlink xlink,
    xlink_nonstandard text
);


ALTER TABLE public.template_xlink OWNER TO postgres;

--
-- TOC entry 1991 (class 1259 OID 40327)
-- Dependencies: 1990 6 505
-- Name: catalog_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_xlink (
    catalog_xlink_id integer NOT NULL,
    catalog_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalog_xlink OWNER TO cleaner;

--
-- TOC entry 1992 (class 1259 OID 40333)
-- Dependencies: 6 1991
-- Name: catalog_xlink_catalog_xlink_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalog_xlink_catalog_xlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalog_xlink_catalog_xlink_id_seq OWNER TO cleaner;

--
-- TOC entry 2636 (class 0 OID 0)
-- Dependencies: 1992
-- Name: catalog_xlink_catalog_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_xlink_catalog_xlink_id_seq OWNED BY catalog_xlink.catalog_xlink_id;


--
-- TOC entry 1993 (class 1259 OID 40335)
-- Dependencies: 2399 6
-- Name: catalogref; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref (
    parent_id integer NOT NULL,
    child_id integer NOT NULL,
    catalogref_id integer NOT NULL,
    not_empty text DEFAULT 'true'::text
);


ALTER TABLE public.catalogref OWNER TO cleaner;

--
-- TOC entry 1994 (class 1259 OID 40338)
-- Dependencies: 6 1993
-- Name: catalogref_catalogref_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalogref_catalogref_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_catalogref_id_seq OWNER TO cleaner;

--
-- TOC entry 2637 (class 0 OID 0)
-- Dependencies: 1994
-- Name: catalogref_catalogref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_catalogref_id_seq OWNED BY catalogref.catalogref_id;


--
-- TOC entry 1995 (class 1259 OID 40340)
-- Dependencies: 491 6
-- Name: template_documentation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_documentation (
    documentationenum documentationenum,
    documentationenum_nonstandard text,
    value text
);


ALTER TABLE public.template_documentation OWNER TO postgres;

--
-- TOC entry 2638 (class 0 OID 0)
-- Dependencies: 1995
-- Name: TABLE template_documentation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_documentation IS 'Be sure to use xx_documentation_namespace and -xlink when using this table.';


--
-- TOC entry 1996 (class 1259 OID 40346)
-- Dependencies: 491 1995 6
-- Name: catalogref_documentation; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation (
    catalogref_documentation_id integer NOT NULL,
    catalogref_id integer
)
INHERITS (template_documentation);


ALTER TABLE public.catalogref_documentation OWNER TO cleaner;

--
-- TOC entry 1997 (class 1259 OID 40352)
-- Dependencies: 6 1996
-- Name: catalogref_documentation_catalogref_documentation_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalogref_documentation_catalogref_documentation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_documentation_catalogref_documentation_id_seq OWNER TO cleaner;

--
-- TOC entry 2639 (class 0 OID 0)
-- Dependencies: 1997
-- Name: catalogref_documentation_catalogref_documentation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_catalogref_documentation_id_seq OWNED BY catalogref_documentation.catalogref_documentation_id;


--
-- TOC entry 1998 (class 1259 OID 40354)
-- Dependencies: 6
-- Name: template_namespace; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_namespace (
    namespace text
);


ALTER TABLE public.template_namespace OWNER TO postgres;

--
-- TOC entry 1999 (class 1259 OID 40360)
-- Dependencies: 1998 6
-- Name: catalogref_documentation_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation_namespace (
    catalogref_documentation_namespace_id integer NOT NULL,
    catalogref_documentation_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.catalogref_documentation_namespace OWNER TO cleaner;

--
-- TOC entry 2000 (class 1259 OID 40366)
-- Dependencies: 1999 6
-- Name: catalogref_documentation_name_catalogref_documentation_name_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalogref_documentation_name_catalogref_documentation_name_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_documentation_name_catalogref_documentation_name_seq OWNER TO cleaner;

--
-- TOC entry 2640 (class 0 OID 0)
-- Dependencies: 2000
-- Name: catalogref_documentation_name_catalogref_documentation_name_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_name_catalogref_documentation_name_seq OWNED BY catalogref_documentation_namespace.catalogref_documentation_namespace_id;


--
-- TOC entry 2001 (class 1259 OID 40368)
-- Dependencies: 6
-- Name: catalogref_documentation_namespace_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE catalogref_documentation_namespace_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_documentation_namespace_seq OWNER TO postgres;

--
-- TOC entry 2002 (class 1259 OID 40370)
-- Dependencies: 6 1990 505
-- Name: catalogref_documentation_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation_xlink (
    catalogref_documentation_xlink_id integer NOT NULL,
    catalogref_documentation_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalogref_documentation_xlink OWNER TO cleaner;

--
-- TOC entry 2003 (class 1259 OID 40376)
-- Dependencies: 2002 6
-- Name: catalogref_documentation_xlink_catalogref_documentation_xlink_s; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalogref_documentation_xlink_catalogref_documentation_xlink_s
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_documentation_xlink_catalogref_documentation_xlink_s OWNER TO cleaner;

--
-- TOC entry 2641 (class 0 OID 0)
-- Dependencies: 2003
-- Name: catalogref_documentation_xlink_catalogref_documentation_xlink_s; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_xlink_catalogref_documentation_xlink_s OWNED BY catalogref_documentation_xlink.catalogref_documentation_xlink_id;


--
-- TOC entry 2004 (class 1259 OID 40378)
-- Dependencies: 1990 6 505
-- Name: catalogref_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_xlink (
    catalogref_xlink_id integer NOT NULL,
    catalogref_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalogref_xlink OWNER TO cleaner;

--
-- TOC entry 2005 (class 1259 OID 40384)
-- Dependencies: 6 2004
-- Name: catalogref_xlink_catalogref_xlink_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE catalogref_xlink_catalogref_xlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.catalogref_xlink_catalogref_xlink_id_seq OWNER TO cleaner;

--
-- TOC entry 2642 (class 0 OID 0)
-- Dependencies: 2005
-- Name: catalogref_xlink_catalogref_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_xlink_catalogref_xlink_id_seq OWNED BY catalogref_xlink.catalogref_xlink_id;


--
-- TOC entry 2006 (class 1259 OID 40386)
-- Dependencies: 487 497 481 485 6
-- Name: dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset (
    dataset_id integer NOT NULL,
    name text,
    alias text,
    authority text,
    d_id text,
    "serviceName" text,
    "urlPath" text,
    status status,
    collectiontype_nonstandard text,
    "dataType_nonstandard" text,
    collectiontype collectiontype,
    datasize_unit datasize_unit,
    "dataType" datatype,
    resourcecontrol text,
    harvest text,
    datasize_unit_nonstandard text
);


ALTER TABLE public.dataset OWNER TO cleaner;

--
-- TOC entry 2007 (class 1259 OID 40392)
-- Dependencies: 483 6
-- Name: template_access; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_access (
    urlpath text,
    servicename text,
    dataformat dataformat,
    dataformat_nonstandard text
);


ALTER TABLE public.template_access OWNER TO postgres;

--
-- TOC entry 2643 (class 0 OID 0)
-- Dependencies: 2007
-- Name: TABLE template_access; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_access IS 'Be sure to use xx_access_datasize as well.';


--
-- TOC entry 2008 (class 1259 OID 40398)
-- Dependencies: 483 2007 6
-- Name: dataset_access; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_access (
    dataset_access_id integer NOT NULL,
    dataset_id integer
)
INHERITS (template_access);


ALTER TABLE public.dataset_access OWNER TO cleaner;

--
-- TOC entry 2009 (class 1259 OID 40404)
-- Dependencies: 6 2008
-- Name: dataset_access_dataset_access_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE dataset_access_dataset_access_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_access_dataset_access_id_seq OWNER TO cleaner;

--
-- TOC entry 2644 (class 0 OID 0)
-- Dependencies: 2009
-- Name: dataset_access_dataset_access_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_access_dataset_access_id_seq OWNED BY dataset_access.dataset_access_id;


--
-- TOC entry 2010 (class 1259 OID 40406)
-- Dependencies: 499 6
-- Name: template_datasize; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_datasize (
    units units,
    units_nonstandard text,
    value text
);


ALTER TABLE public.template_datasize OWNER TO postgres;

--
-- TOC entry 2011 (class 1259 OID 40412)
-- Dependencies: 499 6 2010
-- Name: dataset_access_datasize; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_access_datasize (
    dataset_access_datasize_id integer NOT NULL,
    dataset_access_id integer
)
INHERITS (template_datasize);


ALTER TABLE public.dataset_access_datasize OWNER TO cleaner;

--
-- TOC entry 2012 (class 1259 OID 40418)
-- Dependencies: 6 2011
-- Name: dataset_access_datasize_dataset_access_datasize_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE dataset_access_datasize_dataset_access_datasize_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_access_datasize_dataset_access_datasize_id_seq OWNER TO cleaner;

--
-- TOC entry 2645 (class 0 OID 0)
-- Dependencies: 2012
-- Name: dataset_access_datasize_dataset_access_datasize_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_access_datasize_dataset_access_datasize_id_seq OWNED BY dataset_access_datasize.dataset_access_datasize_id;


--
-- TOC entry 2013 (class 1259 OID 40420)
-- Dependencies: 6
-- Name: dataset_catalogref; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_catalogref (
    dataset_id integer NOT NULL,
    catalogref_id integer NOT NULL
);


ALTER TABLE public.dataset_catalogref OWNER TO cleaner;

--
-- TOC entry 2014 (class 1259 OID 40423)
-- Dependencies: 6
-- Name: dataset_dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_dataset (
    parent_id integer NOT NULL,
    child_id integer NOT NULL
);


ALTER TABLE public.dataset_dataset OWNER TO cleaner;

--
-- TOC entry 2015 (class 1259 OID 40426)
-- Dependencies: 2006 6
-- Name: dataset_dataset_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE dataset_dataset_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_dataset_id_seq OWNER TO cleaner;

--
-- TOC entry 2646 (class 0 OID 0)
-- Dependencies: 2015
-- Name: dataset_dataset_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_dataset_id_seq OWNED BY dataset.dataset_id;


--
-- TOC entry 2016 (class 1259 OID 40428)
-- Dependencies: 6
-- Name: dataset_ncml; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_ncml (
    dataset_ncml_id integer NOT NULL,
    dataset_id integer
);


ALTER TABLE public.dataset_ncml OWNER TO cleaner;

--
-- TOC entry 2017 (class 1259 OID 40431)
-- Dependencies: 6 2016
-- Name: dataset_ncml_dataset_ncml_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE dataset_ncml_dataset_ncml_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_ncml_dataset_ncml_id_seq OWNER TO cleaner;

--
-- TOC entry 2647 (class 0 OID 0)
-- Dependencies: 2017
-- Name: dataset_ncml_dataset_ncml_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_ncml_dataset_ncml_id_seq OWNED BY dataset_ncml.dataset_ncml_id;


--
-- TOC entry 2018 (class 1259 OID 40433)
-- Dependencies: 6 1986
-- Name: dataset_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_property (
    dataset_id integer,
    dataset_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.dataset_property OWNER TO cleaner;

--
-- TOC entry 2019 (class 1259 OID 40439)
-- Dependencies: 2018 6
-- Name: dataset_property_dataset_property_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE dataset_property_dataset_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_property_dataset_property_id_seq OWNER TO cleaner;

--
-- TOC entry 2648 (class 0 OID 0)
-- Dependencies: 2019
-- Name: dataset_property_dataset_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_property_dataset_property_id_seq OWNED BY dataset_property.dataset_property_id;


--
-- TOC entry 2020 (class 1259 OID 40441)
-- Dependencies: 6
-- Name: dataset_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_service (
    dataset_id integer NOT NULL,
    service_id integer NOT NULL
);


ALTER TABLE public.dataset_service OWNER TO cleaner;

--
-- TOC entry 2649 (class 0 OID 0)
-- Dependencies: 2020
-- Name: TABLE dataset_service; Type: COMMENT; Schema: public; Owner: cleaner
--

COMMENT ON TABLE dataset_service IS 'Depreciated in 1.0.';


--
-- TOC entry 2021 (class 1259 OID 40444)
-- Dependencies: 6
-- Name: dataset_tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_tmg (
    dataset_id integer NOT NULL,
    tmg_id integer NOT NULL
);


ALTER TABLE public.dataset_tmg OWNER TO cleaner;

--
-- TOC entry 2022 (class 1259 OID 40447)
-- Dependencies: 2409 6 493 479
-- Name: metadata; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata (
    metadata_id integer NOT NULL,
    metadatatype metadatatype,
    metadatatype_nonstandard text,
    inherited_nonstandard text,
    not_empty text DEFAULT 'true'::text,
    inherited booltype
);


ALTER TABLE public.metadata OWNER TO cleaner;

--
-- TOC entry 2023 (class 1259 OID 40454)
-- Dependencies: 6 2022
-- Name: metadata_metadata_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE metadata_metadata_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metadata_metadata_id_seq OWNER TO cleaner;

--
-- TOC entry 2650 (class 0 OID 0)
-- Dependencies: 2023
-- Name: metadata_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_metadata_id_seq OWNED BY metadata.metadata_id;


--
-- TOC entry 2024 (class 1259 OID 40456)
-- Dependencies: 6 1998
-- Name: metadata_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_namespace (
    metadata_namespace_id integer NOT NULL,
    metadata_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.metadata_namespace OWNER TO cleaner;

--
-- TOC entry 2025 (class 1259 OID 40462)
-- Dependencies: 6 2024
-- Name: metadata_namespace_metadata_namespace_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE metadata_namespace_metadata_namespace_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metadata_namespace_metadata_namespace_id_seq OWNER TO cleaner;

--
-- TOC entry 2651 (class 0 OID 0)
-- Dependencies: 2025
-- Name: metadata_namespace_metadata_namespace_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_namespace_metadata_namespace_id_seq OWNED BY metadata_namespace.metadata_namespace_id;


--
-- TOC entry 2026 (class 1259 OID 40464)
-- Dependencies: 6
-- Name: metadata_tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_tmg (
    metadata_id integer NOT NULL,
    tmg_id integer NOT NULL
);


ALTER TABLE public.metadata_tmg OWNER TO cleaner;

--
-- TOC entry 2027 (class 1259 OID 40467)
-- Dependencies: 1990 505 6
-- Name: metadata_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_xlink (
    metadata_xlink_id integer NOT NULL,
    metadata_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.metadata_xlink OWNER TO cleaner;

--
-- TOC entry 2028 (class 1259 OID 40473)
-- Dependencies: 2027 6
-- Name: metadata_xlink_metadata_xlink_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE metadata_xlink_metadata_xlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metadata_xlink_metadata_xlink_id_seq OWNER TO cleaner;

--
-- TOC entry 2652 (class 0 OID 0)
-- Dependencies: 2028
-- Name: metadata_xlink_metadata_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_xlink_metadata_xlink_id_seq OWNED BY metadata_xlink.metadata_xlink_id;


--
-- TOC entry 2029 (class 1259 OID 40475)
-- Dependencies: 497 495 6
-- Name: service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service (
    service_id integer NOT NULL,
    name text,
    base text,
    suffix text,
    "desc" text,
    "serviceType_nonstandard" text,
    status status,
    "serviceType" servicetype
);


ALTER TABLE public.service OWNER TO cleaner;

--
-- TOC entry 2030 (class 1259 OID 40481)
-- Dependencies: 6
-- Name: template_datasetroot; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_datasetroot (
    path text,
    location text
);


ALTER TABLE public.template_datasetroot OWNER TO postgres;

--
-- TOC entry 2031 (class 1259 OID 40487)
-- Dependencies: 6 2030
-- Name: service_datasetroot; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_datasetroot (
    service_datasetroot_id integer NOT NULL,
    service_id integer
)
INHERITS (template_datasetroot);


ALTER TABLE public.service_datasetroot OWNER TO cleaner;

--
-- TOC entry 2032 (class 1259 OID 40493)
-- Dependencies: 6 2031
-- Name: service_datasetroot_service_datasetroot_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE service_datasetroot_service_datasetroot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.service_datasetroot_service_datasetroot_id_seq OWNER TO cleaner;

--
-- TOC entry 2653 (class 0 OID 0)
-- Dependencies: 2032
-- Name: service_datasetroot_service_datasetroot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_datasetroot_service_datasetroot_id_seq OWNED BY service_datasetroot.service_datasetroot_id;


--
-- TOC entry 2033 (class 1259 OID 40495)
-- Dependencies: 6 1986
-- Name: service_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_property (
    service_id integer,
    service_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.service_property OWNER TO cleaner;

--
-- TOC entry 2034 (class 1259 OID 40501)
-- Dependencies: 2033 6
-- Name: service_property_service_property_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE service_property_service_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.service_property_service_property_id_seq OWNER TO cleaner;

--
-- TOC entry 2654 (class 0 OID 0)
-- Dependencies: 2034
-- Name: service_property_service_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_property_service_property_id_seq OWNED BY service_property.service_property_id;


--
-- TOC entry 2035 (class 1259 OID 40503)
-- Dependencies: 6
-- Name: service_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_service (
    parent_id integer NOT NULL,
    child_id integer NOT NULL
);


ALTER TABLE public.service_service OWNER TO cleaner;

--
-- TOC entry 2036 (class 1259 OID 40506)
-- Dependencies: 2029 6
-- Name: service_service_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE service_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.service_service_id_seq OWNER TO cleaner;

--
-- TOC entry 2655 (class 0 OID 0)
-- Dependencies: 2036
-- Name: service_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_service_id_seq OWNED BY service.service_id;


--
-- TOC entry 2037 (class 1259 OID 40508)
-- Dependencies: 6
-- Name: template_contact; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_contact (
    url text,
    email text
);


ALTER TABLE public.template_contact OWNER TO postgres;

--
-- TOC entry 2038 (class 1259 OID 40514)
-- Dependencies: 6
-- Name: template_contributor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_contributor (
    name text,
    role text
);


ALTER TABLE public.template_contributor OWNER TO postgres;

--
-- TOC entry 2039 (class 1259 OID 40520)
-- Dependencies: 6
-- Name: template_controlledvocabulary; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_controlledvocabulary (
    vocabulary text,
    value text
);


ALTER TABLE public.template_controlledvocabulary OWNER TO postgres;

--
-- TOC entry 2040 (class 1259 OID 40526)
-- Dependencies: 489 6
-- Name: template_datetypeformatted; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_datetypeformatted (
    format text,
    value text,
    dateenum dateenum,
    dateenum_nonstandard text
);


ALTER TABLE public.template_datetypeformatted OWNER TO postgres;

--
-- TOC entry 2041 (class 1259 OID 40532)
-- Dependencies: 6
-- Name: template_duration; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_duration (
    duration text
);


ALTER TABLE public.template_duration OWNER TO postgres;

--
-- TOC entry 2656 (class 0 OID 0)
-- Dependencies: 2041
-- Name: TABLE template_duration; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_duration IS 'TODO: add check constraints to limit the input';


--
-- TOC entry 2042 (class 1259 OID 40538)
-- Dependencies: 2416 501 6
-- Name: template_geospatialcoverage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_geospatialcoverage (
    upordown upordown DEFAULT 'up'::upordown,
    upordown_nonstandard text
);


ALTER TABLE public.template_geospatialcoverage OWNER TO postgres;

--
-- TOC entry 2657 (class 0 OID 0)
-- Dependencies: 2042
-- Name: TABLE template_geospatialcoverage; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_geospatialcoverage IS 'Be sure to include xx_geospatialcoverage_northsouth, -eastwest, and -updown (all of type spatialrange) as well as -name (of type controlledvocabulary) whenever you include this one.';


--
-- TOC entry 2658 (class 0 OID 0)
-- Dependencies: 2042
-- Name: COLUMN template_geospatialcoverage.upordown; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN template_geospatialcoverage.upordown IS 'zpositive';


--
-- TOC entry 2043 (class 1259 OID 40545)
-- Dependencies: 6
-- Name: template_ncml; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_ncml (
    ncml text
);


ALTER TABLE public.template_ncml OWNER TO postgres;

--
-- TOC entry 2044 (class 1259 OID 40551)
-- Dependencies: 6
-- Name: template_sourcetype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_sourcetype (
);


ALTER TABLE public.template_sourcetype OWNER TO postgres;

--
-- TOC entry 2659 (class 0 OID 0)
-- Dependencies: 2044
-- Name: TABLE template_sourcetype; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_sourcetype IS 'empty. Be sure to include a xx_sourcetype_name and xx_sourcetype_contact when using this table.';


--
-- TOC entry 2045 (class 1259 OID 40554)
-- Dependencies: 6
-- Name: template_spatialrange; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_spatialrange (
    units text,
    start text,
    size text,
    resolution text
);


ALTER TABLE public.template_spatialrange OWNER TO postgres;

--
-- TOC entry 2046 (class 1259 OID 40560)
-- Dependencies: 6
-- Name: template_timecoverage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_timecoverage (
    resolution text
);


ALTER TABLE public.template_timecoverage OWNER TO postgres;

--
-- TOC entry 2660 (class 0 OID 0)
-- Dependencies: 2046
-- Name: TABLE template_timecoverage; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_timecoverage IS 'Be sure to use xx_timecoverage_start, -end, and -duration whenever using this table.';


--
-- TOC entry 2661 (class 0 OID 0)
-- Dependencies: 2046
-- Name: COLUMN template_timecoverage.resolution; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN template_timecoverage.resolution IS 'This is actually supposed to be of type Duration, but the schema doesn''t like it when I try to inherit from Duration, since it expects a ''duration'' field, and this is simpler than create a new subtable.';


--
-- TOC entry 2047 (class 1259 OID 40566)
-- Dependencies: 6
-- Name: template_variable; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_variable (
    name text,
    vocabulary_name text,
    units text
);


ALTER TABLE public.template_variable OWNER TO postgres;

--
-- TOC entry 2048 (class 1259 OID 40572)
-- Dependencies: 503 6
-- Name: template_variables; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_variables (
    vocabulary vocabulary,
    vocabulary_nonstandard text
);


ALTER TABLE public.template_variables OWNER TO postgres;

--
-- TOC entry 2662 (class 0 OID 0)
-- Dependencies: 2048
-- Name: TABLE template_variables; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_variables IS 'Be sure to use xx_variables_variable, -variablemap, and -xlink whenever using this table.';


--
-- TOC entry 2049 (class 1259 OID 40578)
-- Dependencies: 2417 6
-- Name: tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg (
    tmg_id integer NOT NULL,
    not_empty text DEFAULT 'true'::text
);


ALTER TABLE public.tmg OWNER TO cleaner;

--
-- TOC entry 2050 (class 1259 OID 40585)
-- Dependencies: 6
-- Name: tmg_authority; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_authority (
    tmg_authority_id integer NOT NULL,
    tmg_id integer,
    authority text
);


ALTER TABLE public.tmg_authority OWNER TO cleaner;

--
-- TOC entry 2051 (class 1259 OID 40591)
-- Dependencies: 6 2050
-- Name: tmg_authority_tmg_authority_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_authority_tmg_authority_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_authority_tmg_authority_id_seq OWNER TO cleaner;

--
-- TOC entry 2663 (class 0 OID 0)
-- Dependencies: 2051
-- Name: tmg_authority_tmg_authority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_authority_tmg_authority_id_seq OWNED BY tmg_authority.tmg_authority_id;


--
-- TOC entry 2052 (class 1259 OID 40593)
-- Dependencies: 2038 6
-- Name: tmg_contributor; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_contributor (
    tmg_contributor_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_contributor);


ALTER TABLE public.tmg_contributor OWNER TO cleaner;

--
-- TOC entry 2053 (class 1259 OID 40599)
-- Dependencies: 2052 6
-- Name: tmg_contributor_tmg_contributor_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_contributor_tmg_contributor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_contributor_tmg_contributor_id_seq OWNER TO cleaner;

--
-- TOC entry 2664 (class 0 OID 0)
-- Dependencies: 2053
-- Name: tmg_contributor_tmg_contributor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_contributor_tmg_contributor_id_seq OWNED BY tmg_contributor.tmg_contributor_id;


--
-- TOC entry 2054 (class 1259 OID 40601)
-- Dependencies: 2044 6
-- Name: tmg_creator; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator (
    tmg_creator_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_sourcetype);


ALTER TABLE public.tmg_creator OWNER TO cleaner;

--
-- TOC entry 2055 (class 1259 OID 40604)
-- Dependencies: 6 2037
-- Name: tmg_creator_contact; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator_contact (
    tmg_creator_contact_id integer NOT NULL,
    tmg_creator_id integer
)
INHERITS (template_contact);


ALTER TABLE public.tmg_creator_contact OWNER TO cleaner;

--
-- TOC entry 2056 (class 1259 OID 40610)
-- Dependencies: 2055 6
-- Name: tmg_creator_contact_tmg_creator_contact_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_creator_contact_tmg_creator_contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_creator_contact_tmg_creator_contact_id_seq OWNER TO cleaner;

--
-- TOC entry 2665 (class 0 OID 0)
-- Dependencies: 2056
-- Name: tmg_creator_contact_tmg_creator_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_contact_tmg_creator_contact_id_seq OWNED BY tmg_creator_contact.tmg_creator_contact_id;


--
-- TOC entry 2057 (class 1259 OID 40612)
-- Dependencies: 2039 6
-- Name: tmg_creator_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator_name (
    tmg_creator_name_id integer NOT NULL,
    tmg_creator_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_creator_name OWNER TO cleaner;

--
-- TOC entry 2058 (class 1259 OID 40618)
-- Dependencies: 2057 6
-- Name: tmg_creator_name_tmg_creator_name_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_creator_name_tmg_creator_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_creator_name_tmg_creator_name_id_seq OWNER TO cleaner;

--
-- TOC entry 2666 (class 0 OID 0)
-- Dependencies: 2058
-- Name: tmg_creator_name_tmg_creator_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_name_tmg_creator_name_id_seq OWNED BY tmg_creator_name.tmg_creator_name_id;


--
-- TOC entry 2059 (class 1259 OID 40620)
-- Dependencies: 6 2054
-- Name: tmg_creator_tmg_creator_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_creator_tmg_creator_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_creator_tmg_creator_id_seq OWNER TO cleaner;

--
-- TOC entry 2667 (class 0 OID 0)
-- Dependencies: 2059
-- Name: tmg_creator_tmg_creator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_tmg_creator_id_seq OWNED BY tmg_creator.tmg_creator_id;


--
-- TOC entry 2060 (class 1259 OID 40622)
-- Dependencies: 483 6
-- Name: tmg_dataformat; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_dataformat (
    tmg_dataformat_id integer NOT NULL,
    tmg_id integer,
    dataformat dataformat,
    dataformat_nonstandard text
);


ALTER TABLE public.tmg_dataformat OWNER TO cleaner;

--
-- TOC entry 2061 (class 1259 OID 40628)
-- Dependencies: 6 2060
-- Name: tmg_dataformat_tmg_dataformat_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_dataformat_tmg_dataformat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_dataformat_tmg_dataformat_id_seq OWNER TO cleaner;

--
-- TOC entry 2668 (class 0 OID 0)
-- Dependencies: 2061
-- Name: tmg_dataformat_tmg_dataformat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_dataformat_tmg_dataformat_id_seq OWNED BY tmg_dataformat.tmg_dataformat_id;


--
-- TOC entry 2062 (class 1259 OID 40630)
-- Dependencies: 2010 499 6
-- Name: tmg_datasize; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_datasize (
    tmg_datasize_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_datasize);


ALTER TABLE public.tmg_datasize OWNER TO cleaner;

--
-- TOC entry 2063 (class 1259 OID 40636)
-- Dependencies: 6 2062
-- Name: tmg_datasize_tmg_datasize_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_datasize_tmg_datasize_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_datasize_tmg_datasize_id_seq OWNER TO cleaner;

--
-- TOC entry 2669 (class 0 OID 0)
-- Dependencies: 2063
-- Name: tmg_datasize_tmg_datasize_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_datasize_tmg_datasize_id_seq OWNED BY tmg_datasize.tmg_datasize_id;


--
-- TOC entry 2064 (class 1259 OID 40638)
-- Dependencies: 487 6
-- Name: tmg_datatype; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_datatype (
    tmg_datatype_id integer NOT NULL,
    tmg_id integer,
    datatype datatype,
    datatype_nonstandard text
);


ALTER TABLE public.tmg_datatype OWNER TO cleaner;

--
-- TOC entry 2065 (class 1259 OID 40644)
-- Dependencies: 6 2064
-- Name: tmg_datatype_tmg_datatype_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_datatype_tmg_datatype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_datatype_tmg_datatype_id_seq OWNER TO cleaner;

--
-- TOC entry 2670 (class 0 OID 0)
-- Dependencies: 2065
-- Name: tmg_datatype_tmg_datatype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_datatype_tmg_datatype_id_seq OWNED BY tmg_datatype.tmg_datatype_id;


--
-- TOC entry 2066 (class 1259 OID 40646)
-- Dependencies: 489 6 2040
-- Name: tmg_date; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_date (
    tmg_id integer,
    tmg_date_id integer NOT NULL
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_date OWNER TO cleaner;

--
-- TOC entry 2067 (class 1259 OID 40652)
-- Dependencies: 2066 6
-- Name: tmg_date_tmg_date_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_date_tmg_date_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_date_tmg_date_id_seq OWNER TO cleaner;

--
-- TOC entry 2671 (class 0 OID 0)
-- Dependencies: 2067
-- Name: tmg_date_tmg_date_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_date_tmg_date_id_seq OWNED BY tmg_date.tmg_date_id;


--
-- TOC entry 2068 (class 1259 OID 40654)
-- Dependencies: 491 6 1995
-- Name: tmg_documentation; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_documentation (
    documentationenum documentationenum,
    tmg_documentation_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_documentation);


ALTER TABLE public.tmg_documentation OWNER TO cleaner;

--
-- TOC entry 2069 (class 1259 OID 40660)
-- Dependencies: 6 1998
-- Name: tmg_documentation_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_documentation_namespace (
    tmg_documentation_namespace_id integer NOT NULL,
    tmg_documentation_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.tmg_documentation_namespace OWNER TO cleaner;

--
-- TOC entry 2070 (class 1259 OID 40666)
-- Dependencies: 6 2069
-- Name: tmg_documentation_namespace_tmg_documentation_namespace_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_documentation_namespace_tmg_documentation_namespace_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_documentation_namespace_tmg_documentation_namespace_id_seq OWNER TO cleaner;

--
-- TOC entry 2672 (class 0 OID 0)
-- Dependencies: 2070
-- Name: tmg_documentation_namespace_tmg_documentation_namespace_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_namespace_tmg_documentation_namespace_id_seq OWNED BY tmg_documentation_namespace.tmg_documentation_namespace_id;


--
-- TOC entry 2071 (class 1259 OID 40668)
-- Dependencies: 6 2068
-- Name: tmg_documentation_tmg_documentation_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_documentation_tmg_documentation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_documentation_tmg_documentation_id_seq OWNER TO cleaner;

--
-- TOC entry 2673 (class 0 OID 0)
-- Dependencies: 2071
-- Name: tmg_documentation_tmg_documentation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_tmg_documentation_id_seq OWNED BY tmg_documentation.tmg_documentation_id;


--
-- TOC entry 2072 (class 1259 OID 40670)
-- Dependencies: 505 6 1990
-- Name: tmg_documentation_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_documentation_xlink (
    tmg_documentation_xlink_id integer NOT NULL,
    tmg_documentation_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_documentation_xlink OWNER TO cleaner;

--
-- TOC entry 2073 (class 1259 OID 40676)
-- Dependencies: 6 2072
-- Name: tmg_documentation_xlink_tmg_documentation_xlink_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_documentation_xlink_tmg_documentation_xlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_documentation_xlink_tmg_documentation_xlink_id_seq OWNER TO cleaner;

--
-- TOC entry 2674 (class 0 OID 0)
-- Dependencies: 2073
-- Name: tmg_documentation_xlink_tmg_documentation_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_xlink_tmg_documentation_xlink_id_seq OWNED BY tmg_documentation_xlink.tmg_documentation_xlink_id;


--
-- TOC entry 2074 (class 1259 OID 40678)
-- Dependencies: 2431 501 6 2042
-- Name: tmg_geospatialcoverage; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage (
    tmg_geospatialcoverage_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_geospatialcoverage);


ALTER TABLE public.tmg_geospatialcoverage OWNER TO cleaner;

--
-- TOC entry 2075 (class 1259 OID 40685)
-- Dependencies: 6 2045
-- Name: tmg_geospatialcoverage_eastwest; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_eastwest (
    tmg_geospatialcoverage_eastwest_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_eastwest OWNER TO cleaner;

--
-- TOC entry 2076 (class 1259 OID 40691)
-- Dependencies: 2075 6
-- Name: tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq OWNER TO cleaner;

--
-- TOC entry 2675 (class 0 OID 0)
-- Dependencies: 2076
-- Name: tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq OWNED BY tmg_geospatialcoverage_eastwest.tmg_geospatialcoverage_eastwest_id;


--
-- TOC entry 2077 (class 1259 OID 40693)
-- Dependencies: 6 2039
-- Name: tmg_geospatialcoverage_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_name (
    tmg_geospatialcoverage_name_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_geospatialcoverage_name OWNER TO cleaner;

--
-- TOC entry 2078 (class 1259 OID 40699)
-- Dependencies: 6 2077
-- Name: tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq OWNER TO cleaner;

--
-- TOC entry 2676 (class 0 OID 0)
-- Dependencies: 2078
-- Name: tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq OWNED BY tmg_geospatialcoverage_name.tmg_geospatialcoverage_name_id;


--
-- TOC entry 2079 (class 1259 OID 40701)
-- Dependencies: 6 2045
-- Name: tmg_geospatialcoverage_northsouth; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_northsouth (
    tmg_geospatialcoverage_northsouth_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_northsouth OWNER TO cleaner;

--
-- TOC entry 2080 (class 1259 OID 40707)
-- Dependencies: 6 2079
-- Name: tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq OWNER TO cleaner;

--
-- TOC entry 2677 (class 0 OID 0)
-- Dependencies: 2080
-- Name: tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq OWNED BY tmg_geospatialcoverage_northsouth.tmg_geospatialcoverage_northsouth_id;


--
-- TOC entry 2081 (class 1259 OID 40709)
-- Dependencies: 6 2074
-- Name: tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq OWNER TO cleaner;

--
-- TOC entry 2678 (class 0 OID 0)
-- Dependencies: 2081
-- Name: tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq OWNED BY tmg_geospatialcoverage.tmg_geospatialcoverage_id;


--
-- TOC entry 2082 (class 1259 OID 40711)
-- Dependencies: 2045 6
-- Name: tmg_geospatialcoverage_updown; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_updown (
    tmg_geospatialcoverage_updown_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_updown OWNER TO cleaner;

--
-- TOC entry 2083 (class 1259 OID 40717)
-- Dependencies: 6 2082
-- Name: tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq OWNER TO cleaner;

--
-- TOC entry 2679 (class 0 OID 0)
-- Dependencies: 2083
-- Name: tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq OWNED BY tmg_geospatialcoverage_updown.tmg_geospatialcoverage_updown_id;


--
-- TOC entry 2084 (class 1259 OID 40719)
-- Dependencies: 2039 6
-- Name: tmg_keyword; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_keyword (
    tmg_keyword_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_keyword OWNER TO cleaner;

--
-- TOC entry 2085 (class 1259 OID 40725)
-- Dependencies: 6 2084
-- Name: tmg_keyword_tmg_keyword_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_keyword_tmg_keyword_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_keyword_tmg_keyword_id_seq OWNER TO cleaner;

--
-- TOC entry 2680 (class 0 OID 0)
-- Dependencies: 2085
-- Name: tmg_keyword_tmg_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_keyword_tmg_keyword_id_seq OWNED BY tmg_keyword.tmg_keyword_id;


--
-- TOC entry 2086 (class 1259 OID 40727)
-- Dependencies: 6
-- Name: tmg_metadata; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_metadata (
    tmg_id integer NOT NULL,
    metadata_id integer NOT NULL
);


ALTER TABLE public.tmg_metadata OWNER TO cleaner;

--
-- TOC entry 2087 (class 1259 OID 40730)
-- Dependencies: 2039 6
-- Name: tmg_project; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_project (
    tmg_project_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_project OWNER TO cleaner;

--
-- TOC entry 2088 (class 1259 OID 40736)
-- Dependencies: 2087 6
-- Name: tmg_project_tmg_project_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_project_tmg_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_project_tmg_project_id_seq OWNER TO cleaner;

--
-- TOC entry 2681 (class 0 OID 0)
-- Dependencies: 2088
-- Name: tmg_project_tmg_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_project_tmg_project_id_seq OWNED BY tmg_project.tmg_project_id;


--
-- TOC entry 2089 (class 1259 OID 40738)
-- Dependencies: 6 1986
-- Name: tmg_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_property (
    tmg_property_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_property);


ALTER TABLE public.tmg_property OWNER TO cleaner;

--
-- TOC entry 2090 (class 1259 OID 40744)
-- Dependencies: 6 2089
-- Name: tmg_property_tmg_property_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_property_tmg_property_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_property_tmg_property_id_seq OWNER TO cleaner;

--
-- TOC entry 2682 (class 0 OID 0)
-- Dependencies: 2090
-- Name: tmg_property_tmg_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_property_tmg_property_id_seq OWNED BY tmg_property.tmg_property_id;


--
-- TOC entry 2091 (class 1259 OID 40746)
-- Dependencies: 6 2044
-- Name: tmg_publisher; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher (
    tmg_publisher_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_sourcetype);


ALTER TABLE public.tmg_publisher OWNER TO cleaner;

--
-- TOC entry 2092 (class 1259 OID 40749)
-- Dependencies: 6 2037
-- Name: tmg_publisher_contact; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher_contact (
    tmg_publisher_contact_id integer NOT NULL,
    tmg_publisher_id integer
)
INHERITS (template_contact);


ALTER TABLE public.tmg_publisher_contact OWNER TO cleaner;

--
-- TOC entry 2093 (class 1259 OID 40755)
-- Dependencies: 6 2092
-- Name: tmg_publisher_contact_tmg_publisher_contact_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_publisher_contact_tmg_publisher_contact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_publisher_contact_tmg_publisher_contact_id_seq OWNER TO cleaner;

--
-- TOC entry 2683 (class 0 OID 0)
-- Dependencies: 2093
-- Name: tmg_publisher_contact_tmg_publisher_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_contact_tmg_publisher_contact_id_seq OWNED BY tmg_publisher_contact.tmg_publisher_contact_id;


--
-- TOC entry 2094 (class 1259 OID 40757)
-- Dependencies: 2039 6
-- Name: tmg_publisher_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher_name (
    tmg_publisher_name_id integer NOT NULL,
    tmg_publisher_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_publisher_name OWNER TO cleaner;

--
-- TOC entry 2095 (class 1259 OID 40763)
-- Dependencies: 6 2094
-- Name: tmg_publisher_name_tmg_publisher_name_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_publisher_name_tmg_publisher_name_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_publisher_name_tmg_publisher_name_id_seq OWNER TO cleaner;

--
-- TOC entry 2684 (class 0 OID 0)
-- Dependencies: 2095
-- Name: tmg_publisher_name_tmg_publisher_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_name_tmg_publisher_name_id_seq OWNED BY tmg_publisher_name.tmg_publisher_name_id;


--
-- TOC entry 2096 (class 1259 OID 40765)
-- Dependencies: 6 2091
-- Name: tmg_publisher_tmg_publisher_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_publisher_tmg_publisher_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_publisher_tmg_publisher_id_seq OWNER TO cleaner;

--
-- TOC entry 2685 (class 0 OID 0)
-- Dependencies: 2096
-- Name: tmg_publisher_tmg_publisher_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_tmg_publisher_id_seq OWNED BY tmg_publisher.tmg_publisher_id;


--
-- TOC entry 2097 (class 1259 OID 40767)
-- Dependencies: 6
-- Name: tmg_servicename; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_servicename (
    tmg_servicename_id integer NOT NULL,
    tmg_id integer,
    servicename text
);


ALTER TABLE public.tmg_servicename OWNER TO cleaner;

--
-- TOC entry 2098 (class 1259 OID 40773)
-- Dependencies: 2097 6
-- Name: tmg_servicename_tmg_servicename_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_servicename_tmg_servicename_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_servicename_tmg_servicename_id_seq OWNER TO cleaner;

--
-- TOC entry 2686 (class 0 OID 0)
-- Dependencies: 2098
-- Name: tmg_servicename_tmg_servicename_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_servicename_tmg_servicename_id_seq OWNED BY tmg_servicename.tmg_servicename_id;


--
-- TOC entry 2099 (class 1259 OID 40775)
-- Dependencies: 6 2046
-- Name: tmg_timecoverage; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage (
    tmg_timecoverage_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_timecoverage);


ALTER TABLE public.tmg_timecoverage OWNER TO cleaner;

--
-- TOC entry 2100 (class 1259 OID 40781)
-- Dependencies: 2041 6
-- Name: tmg_timecoverage_duration; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_duration (
    tmg_timecoverage_duration_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_duration);


ALTER TABLE public.tmg_timecoverage_duration OWNER TO cleaner;

--
-- TOC entry 2101 (class 1259 OID 40787)
-- Dependencies: 6 2100
-- Name: tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq OWNER TO cleaner;

--
-- TOC entry 2687 (class 0 OID 0)
-- Dependencies: 2101
-- Name: tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq OWNED BY tmg_timecoverage_duration.tmg_timecoverage_duration_id;


--
-- TOC entry 2102 (class 1259 OID 40789)
-- Dependencies: 2040 489 6
-- Name: tmg_timecoverage_end; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_end (
    tmg_timecoverage_end_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_timecoverage_end OWNER TO cleaner;

--
-- TOC entry 2103 (class 1259 OID 40795)
-- Dependencies: 2102 6
-- Name: tmg_timecoverage_end_tmg_timecoverage_end_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_timecoverage_end_tmg_timecoverage_end_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_timecoverage_end_tmg_timecoverage_end_id_seq OWNER TO cleaner;

--
-- TOC entry 2688 (class 0 OID 0)
-- Dependencies: 2103
-- Name: tmg_timecoverage_end_tmg_timecoverage_end_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_end_tmg_timecoverage_end_id_seq OWNED BY tmg_timecoverage_end.tmg_timecoverage_end_id;


--
-- TOC entry 2104 (class 1259 OID 40797)
-- Dependencies: 2041 6
-- Name: tmg_timecoverage_resolution; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_resolution (
    tmg_timecoverage_resolution_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_duration);


ALTER TABLE public.tmg_timecoverage_resolution OWNER TO cleaner;

--
-- TOC entry 2105 (class 1259 OID 40803)
-- Dependencies: 6 2104
-- Name: tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq OWNER TO cleaner;

--
-- TOC entry 2689 (class 0 OID 0)
-- Dependencies: 2105
-- Name: tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq OWNED BY tmg_timecoverage_resolution.tmg_timecoverage_resolution_id;


--
-- TOC entry 2106 (class 1259 OID 40805)
-- Dependencies: 6 489 2040
-- Name: tmg_timecoverage_start; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_start (
    tmg_timecoverage_start_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_timecoverage_start OWNER TO cleaner;

--
-- TOC entry 2107 (class 1259 OID 40811)
-- Dependencies: 6 2106
-- Name: tmg_timecoverage_start_tmg_timecoverage_start_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_timecoverage_start_tmg_timecoverage_start_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_timecoverage_start_tmg_timecoverage_start_id_seq OWNER TO cleaner;

--
-- TOC entry 2690 (class 0 OID 0)
-- Dependencies: 2107
-- Name: tmg_timecoverage_start_tmg_timecoverage_start_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_start_tmg_timecoverage_start_id_seq OWNED BY tmg_timecoverage_start.tmg_timecoverage_start_id;


--
-- TOC entry 2108 (class 1259 OID 40813)
-- Dependencies: 2099 6
-- Name: tmg_timecoverage_tmg_timecoverage_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_timecoverage_tmg_timecoverage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_timecoverage_tmg_timecoverage_id_seq OWNER TO cleaner;

--
-- TOC entry 2691 (class 0 OID 0)
-- Dependencies: 2108
-- Name: tmg_timecoverage_tmg_timecoverage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_tmg_timecoverage_id_seq OWNED BY tmg_timecoverage.tmg_timecoverage_id;


--
-- TOC entry 2109 (class 1259 OID 40815)
-- Dependencies: 6 2049
-- Name: tmg_tmg_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_tmg_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_tmg_id_seq OWNER TO cleaner;

--
-- TOC entry 2692 (class 0 OID 0)
-- Dependencies: 2109
-- Name: tmg_tmg_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_tmg_id_seq OWNED BY tmg.tmg_id;


--
-- TOC entry 2110 (class 1259 OID 40817)
-- Dependencies: 6 503 2048
-- Name: tmg_variables; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables (
    tmg_variables_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_variables);


ALTER TABLE public.tmg_variables OWNER TO cleaner;

--
-- TOC entry 2111 (class 1259 OID 40823)
-- Dependencies: 6 2110
-- Name: tmg_variables_tmg_variables_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_variables_tmg_variables_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_variables_tmg_variables_id_seq OWNER TO cleaner;

--
-- TOC entry 2693 (class 0 OID 0)
-- Dependencies: 2111
-- Name: tmg_variables_tmg_variables_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_tmg_variables_id_seq OWNED BY tmg_variables.tmg_variables_id;


--
-- TOC entry 2112 (class 1259 OID 40825)
-- Dependencies: 6 2047
-- Name: tmg_variables_variable; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_variable (
    tmg_variables_variable_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_variable);


ALTER TABLE public.tmg_variables_variable OWNER TO cleaner;

--
-- TOC entry 2113 (class 1259 OID 40831)
-- Dependencies: 6 2112
-- Name: tmg_variables_variable_tmg_variables_variable_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_variables_variable_tmg_variables_variable_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_variables_variable_tmg_variables_variable_id_seq OWNER TO cleaner;

--
-- TOC entry 2694 (class 0 OID 0)
-- Dependencies: 2113
-- Name: tmg_variables_variable_tmg_variables_variable_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_variable_tmg_variables_variable_id_seq OWNED BY tmg_variables_variable.tmg_variables_variable_id;


--
-- TOC entry 2114 (class 1259 OID 40833)
-- Dependencies: 6 505 1990
-- Name: tmg_variables_variablemap; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_variablemap (
    tmg_variables_variablemap_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_variables_variablemap OWNER TO cleaner;

--
-- TOC entry 2115 (class 1259 OID 40839)
-- Dependencies: 2114 6
-- Name: tmg_variables_variablemap_tmg_variables_variablemap_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_variables_variablemap_tmg_variables_variablemap_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_variables_variablemap_tmg_variables_variablemap_id_seq OWNER TO cleaner;

--
-- TOC entry 2695 (class 0 OID 0)
-- Dependencies: 2115
-- Name: tmg_variables_variablemap_tmg_variables_variablemap_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_variablemap_tmg_variables_variablemap_id_seq OWNED BY tmg_variables_variablemap.tmg_variables_variablemap_id;


--
-- TOC entry 2116 (class 1259 OID 40841)
-- Dependencies: 1990 505 6
-- Name: tmg_variables_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_xlink (
    tmg_variables_xlink_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_variables_xlink OWNER TO cleaner;

--
-- TOC entry 2117 (class 1259 OID 40847)
-- Dependencies: 6 2116
-- Name: tmg_variables_xlink_tmg_variables_xlink_id_seq; Type: SEQUENCE; Schema: public; Owner: cleaner
--

CREATE SEQUENCE tmg_variables_xlink_tmg_variables_xlink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tmg_variables_xlink_tmg_variables_xlink_id_seq OWNER TO cleaner;

--
-- TOC entry 2696 (class 0 OID 0)
-- Dependencies: 2117
-- Name: tmg_variables_xlink_tmg_variables_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_xlink_tmg_variables_xlink_id_seq OWNED BY tmg_variables_xlink.tmg_variables_xlink_id;


--
-- TOC entry 2395 (class 2604 OID 40849)
-- Dependencies: 1984 1983
-- Name: catalog_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog ALTER COLUMN catalog_id SET DEFAULT nextval('catalog_catalog_id_seq'::regclass);


--
-- TOC entry 2396 (class 2604 OID 40850)
-- Dependencies: 1988 1987
-- Name: catalog_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog_property ALTER COLUMN catalog_property_id SET DEFAULT nextval('catalog_property_catalog_property_id_seq'::regclass);


--
-- TOC entry 2397 (class 2604 OID 40851)
-- Dependencies: 1992 1991
-- Name: catalog_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog_xlink ALTER COLUMN catalog_xlink_id SET DEFAULT nextval('catalog_xlink_catalog_xlink_id_seq'::regclass);


--
-- TOC entry 2398 (class 2604 OID 40852)
-- Dependencies: 1994 1993
-- Name: catalogref_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref ALTER COLUMN catalogref_id SET DEFAULT nextval('catalogref_catalogref_id_seq'::regclass);


--
-- TOC entry 2400 (class 2604 OID 40853)
-- Dependencies: 1997 1996
-- Name: catalogref_documentation_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation ALTER COLUMN catalogref_documentation_id SET DEFAULT nextval('catalogref_documentation_catalogref_documentation_id_seq'::regclass);


--
-- TOC entry 2401 (class 2604 OID 40854)
-- Dependencies: 2000 1999
-- Name: catalogref_documentation_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation_namespace ALTER COLUMN catalogref_documentation_namespace_id SET DEFAULT nextval('catalogref_documentation_name_catalogref_documentation_name_seq'::regclass);


--
-- TOC entry 2402 (class 2604 OID 40855)
-- Dependencies: 2003 2002
-- Name: catalogref_documentation_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation_xlink ALTER COLUMN catalogref_documentation_xlink_id SET DEFAULT nextval('catalogref_documentation_xlink_catalogref_documentation_xlink_s'::regclass);


--
-- TOC entry 2403 (class 2604 OID 40856)
-- Dependencies: 2005 2004
-- Name: catalogref_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_xlink ALTER COLUMN catalogref_xlink_id SET DEFAULT nextval('catalogref_xlink_catalogref_xlink_id_seq'::regclass);


--
-- TOC entry 2404 (class 2604 OID 40857)
-- Dependencies: 2015 2006
-- Name: dataset_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset ALTER COLUMN dataset_id SET DEFAULT nextval('dataset_dataset_id_seq'::regclass);


--
-- TOC entry 2405 (class 2604 OID 40858)
-- Dependencies: 2009 2008
-- Name: dataset_access_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_access ALTER COLUMN dataset_access_id SET DEFAULT nextval('dataset_access_dataset_access_id_seq'::regclass);


--
-- TOC entry 2406 (class 2604 OID 40859)
-- Dependencies: 2012 2011
-- Name: dataset_access_datasize_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_access_datasize ALTER COLUMN dataset_access_datasize_id SET DEFAULT nextval('dataset_access_datasize_dataset_access_datasize_id_seq'::regclass);


--
-- TOC entry 2407 (class 2604 OID 40860)
-- Dependencies: 2017 2016
-- Name: dataset_ncml_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_ncml ALTER COLUMN dataset_ncml_id SET DEFAULT nextval('dataset_ncml_dataset_ncml_id_seq'::regclass);


--
-- TOC entry 2408 (class 2604 OID 40861)
-- Dependencies: 2019 2018
-- Name: dataset_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_property ALTER COLUMN dataset_property_id SET DEFAULT nextval('dataset_property_dataset_property_id_seq'::regclass);


--
-- TOC entry 2410 (class 2604 OID 40862)
-- Dependencies: 2023 2022
-- Name: metadata_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata ALTER COLUMN metadata_id SET DEFAULT nextval('metadata_metadata_id_seq'::regclass);


--
-- TOC entry 2411 (class 2604 OID 40863)
-- Dependencies: 2025 2024
-- Name: metadata_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata_namespace ALTER COLUMN metadata_namespace_id SET DEFAULT nextval('metadata_namespace_metadata_namespace_id_seq'::regclass);


--
-- TOC entry 2412 (class 2604 OID 40864)
-- Dependencies: 2028 2027
-- Name: metadata_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata_xlink ALTER COLUMN metadata_xlink_id SET DEFAULT nextval('metadata_xlink_metadata_xlink_id_seq'::regclass);


--
-- TOC entry 2413 (class 2604 OID 40865)
-- Dependencies: 2036 2029
-- Name: service_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service ALTER COLUMN service_id SET DEFAULT nextval('service_service_id_seq'::regclass);


--
-- TOC entry 2414 (class 2604 OID 40866)
-- Dependencies: 2032 2031
-- Name: service_datasetroot_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service_datasetroot ALTER COLUMN service_datasetroot_id SET DEFAULT nextval('service_datasetroot_service_datasetroot_id_seq'::regclass);


--
-- TOC entry 2415 (class 2604 OID 40867)
-- Dependencies: 2034 2033
-- Name: service_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service_property ALTER COLUMN service_property_id SET DEFAULT nextval('service_property_service_property_id_seq'::regclass);


--
-- TOC entry 2418 (class 2604 OID 40868)
-- Dependencies: 2109 2049
-- Name: tmg_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg ALTER COLUMN tmg_id SET DEFAULT nextval('tmg_tmg_id_seq'::regclass);


--
-- TOC entry 2419 (class 2604 OID 40869)
-- Dependencies: 2051 2050
-- Name: tmg_authority_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_authority ALTER COLUMN tmg_authority_id SET DEFAULT nextval('tmg_authority_tmg_authority_id_seq'::regclass);


--
-- TOC entry 2420 (class 2604 OID 40870)
-- Dependencies: 2053 2052
-- Name: tmg_contributor_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_contributor ALTER COLUMN tmg_contributor_id SET DEFAULT nextval('tmg_contributor_tmg_contributor_id_seq'::regclass);


--
-- TOC entry 2421 (class 2604 OID 40871)
-- Dependencies: 2059 2054
-- Name: tmg_creator_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator ALTER COLUMN tmg_creator_id SET DEFAULT nextval('tmg_creator_tmg_creator_id_seq'::regclass);


--
-- TOC entry 2422 (class 2604 OID 40872)
-- Dependencies: 2056 2055
-- Name: tmg_creator_contact_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator_contact ALTER COLUMN tmg_creator_contact_id SET DEFAULT nextval('tmg_creator_contact_tmg_creator_contact_id_seq'::regclass);


--
-- TOC entry 2423 (class 2604 OID 40873)
-- Dependencies: 2058 2057
-- Name: tmg_creator_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator_name ALTER COLUMN tmg_creator_name_id SET DEFAULT nextval('tmg_creator_name_tmg_creator_name_id_seq'::regclass);


--
-- TOC entry 2424 (class 2604 OID 40874)
-- Dependencies: 2061 2060
-- Name: tmg_dataformat_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_dataformat ALTER COLUMN tmg_dataformat_id SET DEFAULT nextval('tmg_dataformat_tmg_dataformat_id_seq'::regclass);


--
-- TOC entry 2425 (class 2604 OID 40875)
-- Dependencies: 2063 2062
-- Name: tmg_datasize_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_datasize ALTER COLUMN tmg_datasize_id SET DEFAULT nextval('tmg_datasize_tmg_datasize_id_seq'::regclass);


--
-- TOC entry 2426 (class 2604 OID 40876)
-- Dependencies: 2065 2064
-- Name: tmg_datatype_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_datatype ALTER COLUMN tmg_datatype_id SET DEFAULT nextval('tmg_datatype_tmg_datatype_id_seq'::regclass);


--
-- TOC entry 2427 (class 2604 OID 40877)
-- Dependencies: 2067 2066
-- Name: tmg_date_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_date ALTER COLUMN tmg_date_id SET DEFAULT nextval('tmg_date_tmg_date_id_seq'::regclass);


--
-- TOC entry 2428 (class 2604 OID 40878)
-- Dependencies: 2071 2068
-- Name: tmg_documentation_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation ALTER COLUMN tmg_documentation_id SET DEFAULT nextval('tmg_documentation_tmg_documentation_id_seq'::regclass);


--
-- TOC entry 2429 (class 2604 OID 40879)
-- Dependencies: 2070 2069
-- Name: tmg_documentation_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation_namespace ALTER COLUMN tmg_documentation_namespace_id SET DEFAULT nextval('tmg_documentation_namespace_tmg_documentation_namespace_id_seq'::regclass);


--
-- TOC entry 2430 (class 2604 OID 40880)
-- Dependencies: 2073 2072
-- Name: tmg_documentation_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation_xlink ALTER COLUMN tmg_documentation_xlink_id SET DEFAULT nextval('tmg_documentation_xlink_tmg_documentation_xlink_id_seq'::regclass);


--
-- TOC entry 2432 (class 2604 OID 40881)
-- Dependencies: 2081 2074
-- Name: tmg_geospatialcoverage_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage ALTER COLUMN tmg_geospatialcoverage_id SET DEFAULT nextval('tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq'::regclass);


--
-- TOC entry 2433 (class 2604 OID 40882)
-- Dependencies: 2076 2075
-- Name: tmg_geospatialcoverage_eastwest_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_eastwest ALTER COLUMN tmg_geospatialcoverage_eastwest_id SET DEFAULT nextval('tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq'::regclass);


--
-- TOC entry 2434 (class 2604 OID 40883)
-- Dependencies: 2078 2077
-- Name: tmg_geospatialcoverage_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_name ALTER COLUMN tmg_geospatialcoverage_name_id SET DEFAULT nextval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq'::regclass);


--
-- TOC entry 2435 (class 2604 OID 40884)
-- Dependencies: 2080 2079
-- Name: tmg_geospatialcoverage_northsouth_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_northsouth ALTER COLUMN tmg_geospatialcoverage_northsouth_id SET DEFAULT nextval('tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq'::regclass);


--
-- TOC entry 2436 (class 2604 OID 40885)
-- Dependencies: 2083 2082
-- Name: tmg_geospatialcoverage_updown_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_updown ALTER COLUMN tmg_geospatialcoverage_updown_id SET DEFAULT nextval('tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq'::regclass);


--
-- TOC entry 2437 (class 2604 OID 40886)
-- Dependencies: 2085 2084
-- Name: tmg_keyword_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_keyword ALTER COLUMN tmg_keyword_id SET DEFAULT nextval('tmg_keyword_tmg_keyword_id_seq'::regclass);


--
-- TOC entry 2438 (class 2604 OID 40887)
-- Dependencies: 2088 2087
-- Name: tmg_project_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_project ALTER COLUMN tmg_project_id SET DEFAULT nextval('tmg_project_tmg_project_id_seq'::regclass);


--
-- TOC entry 2439 (class 2604 OID 40888)
-- Dependencies: 2090 2089
-- Name: tmg_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_property ALTER COLUMN tmg_property_id SET DEFAULT nextval('tmg_property_tmg_property_id_seq'::regclass);


--
-- TOC entry 2440 (class 2604 OID 40889)
-- Dependencies: 2096 2091
-- Name: tmg_publisher_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher ALTER COLUMN tmg_publisher_id SET DEFAULT nextval('tmg_publisher_tmg_publisher_id_seq'::regclass);


--
-- TOC entry 2441 (class 2604 OID 40890)
-- Dependencies: 2093 2092
-- Name: tmg_publisher_contact_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher_contact ALTER COLUMN tmg_publisher_contact_id SET DEFAULT nextval('tmg_publisher_contact_tmg_publisher_contact_id_seq'::regclass);


--
-- TOC entry 2442 (class 2604 OID 40891)
-- Dependencies: 2095 2094
-- Name: tmg_publisher_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher_name ALTER COLUMN tmg_publisher_name_id SET DEFAULT nextval('tmg_publisher_name_tmg_publisher_name_id_seq'::regclass);


--
-- TOC entry 2443 (class 2604 OID 40892)
-- Dependencies: 2098 2097
-- Name: tmg_servicename_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_servicename ALTER COLUMN tmg_servicename_id SET DEFAULT nextval('tmg_servicename_tmg_servicename_id_seq'::regclass);


--
-- TOC entry 2444 (class 2604 OID 40893)
-- Dependencies: 2108 2099
-- Name: tmg_timecoverage_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage ALTER COLUMN tmg_timecoverage_id SET DEFAULT nextval('tmg_timecoverage_tmg_timecoverage_id_seq'::regclass);


--
-- TOC entry 2445 (class 2604 OID 40894)
-- Dependencies: 2101 2100
-- Name: tmg_timecoverage_duration_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_duration ALTER COLUMN tmg_timecoverage_duration_id SET DEFAULT nextval('tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq'::regclass);


--
-- TOC entry 2446 (class 2604 OID 40895)
-- Dependencies: 2103 2102
-- Name: tmg_timecoverage_end_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_end ALTER COLUMN tmg_timecoverage_end_id SET DEFAULT nextval('tmg_timecoverage_end_tmg_timecoverage_end_id_seq'::regclass);


--
-- TOC entry 2447 (class 2604 OID 40896)
-- Dependencies: 2105 2104
-- Name: tmg_timecoverage_resolution_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_resolution ALTER COLUMN tmg_timecoverage_resolution_id SET DEFAULT nextval('tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq'::regclass);


--
-- TOC entry 2448 (class 2604 OID 40897)
-- Dependencies: 2107 2106
-- Name: tmg_timecoverage_start_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_start ALTER COLUMN tmg_timecoverage_start_id SET DEFAULT nextval('tmg_timecoverage_start_tmg_timecoverage_start_id_seq'::regclass);


--
-- TOC entry 2449 (class 2604 OID 40898)
-- Dependencies: 2111 2110
-- Name: tmg_variables_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables ALTER COLUMN tmg_variables_id SET DEFAULT nextval('tmg_variables_tmg_variables_id_seq'::regclass);


--
-- TOC entry 2450 (class 2604 OID 40899)
-- Dependencies: 2113 2112
-- Name: tmg_variables_variable_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_variable ALTER COLUMN tmg_variables_variable_id SET DEFAULT nextval('tmg_variables_variable_tmg_variables_variable_id_seq'::regclass);


--
-- TOC entry 2451 (class 2604 OID 40900)
-- Dependencies: 2115 2114
-- Name: tmg_variables_variablemap_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_variablemap ALTER COLUMN tmg_variables_variablemap_id SET DEFAULT nextval('tmg_variables_variablemap_tmg_variables_variablemap_id_seq'::regclass);


--
-- TOC entry 2452 (class 2604 OID 40901)
-- Dependencies: 2117 2116
-- Name: tmg_variables_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_xlink ALTER COLUMN tmg_variables_xlink_id SET DEFAULT nextval('tmg_variables_xlink_tmg_variables_xlink_id_seq'::regclass);


--
-- TOC entry 2454 (class 2606 OID 40903)
-- Dependencies: 1983 1983
-- Name: catalog_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog
    ADD CONSTRAINT catalog_pkey PRIMARY KEY (catalog_id);


--
-- TOC entry 2456 (class 2606 OID 40905)
-- Dependencies: 1987 1987
-- Name: catalog_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog_property
    ADD CONSTRAINT catalog_property_pkey PRIMARY KEY (catalog_property_id);


--
-- TOC entry 2458 (class 2606 OID 40907)
-- Dependencies: 1991 1991
-- Name: catalog_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog_xlink
    ADD CONSTRAINT catalog_xlink_pkey PRIMARY KEY (catalog_xlink_id);


--
-- TOC entry 2464 (class 2606 OID 40909)
-- Dependencies: 1999 1999
-- Name: catalogref_documentation_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation_namespace
    ADD CONSTRAINT catalogref_documentation_namespace_pkey PRIMARY KEY (catalogref_documentation_namespace_id);


--
-- TOC entry 2462 (class 2606 OID 40911)
-- Dependencies: 1996 1996
-- Name: catalogref_documentation_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation
    ADD CONSTRAINT catalogref_documentation_pkey PRIMARY KEY (catalogref_documentation_id);


--
-- TOC entry 2466 (class 2606 OID 40913)
-- Dependencies: 2002 2002
-- Name: catalogref_documentation_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation_xlink
    ADD CONSTRAINT catalogref_documentation_xlink_pkey PRIMARY KEY (catalogref_documentation_xlink_id);


--
-- TOC entry 2460 (class 2606 OID 40915)
-- Dependencies: 1993 1993
-- Name: catalogref_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalogref_pkey PRIMARY KEY (catalogref_id);


--
-- TOC entry 2468 (class 2606 OID 40917)
-- Dependencies: 2004 2004
-- Name: catalogref_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_xlink
    ADD CONSTRAINT catalogref_xlink_pkey PRIMARY KEY (catalogref_xlink_id);


--
-- TOC entry 2474 (class 2606 OID 40919)
-- Dependencies: 2011 2011
-- Name: dataset_access_datasize_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_access_datasize
    ADD CONSTRAINT dataset_access_datasize_pkey PRIMARY KEY (dataset_access_datasize_id);


--
-- TOC entry 2472 (class 2606 OID 40921)
-- Dependencies: 2008 2008
-- Name: dataset_access_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_access
    ADD CONSTRAINT dataset_access_pkey PRIMARY KEY (dataset_access_id);


--
-- TOC entry 2476 (class 2606 OID 40923)
-- Dependencies: 2016 2016
-- Name: dataset_ncml_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_ncml
    ADD CONSTRAINT dataset_ncml_pkey PRIMARY KEY (dataset_ncml_id);


--
-- TOC entry 2470 (class 2606 OID 40925)
-- Dependencies: 2006 2006
-- Name: dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id);


--
-- TOC entry 2478 (class 2606 OID 40927)
-- Dependencies: 2018 2018
-- Name: dataset_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_property
    ADD CONSTRAINT dataset_property_pkey PRIMARY KEY (dataset_property_id);


--
-- TOC entry 2482 (class 2606 OID 40929)
-- Dependencies: 2024 2024
-- Name: metadata_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata_namespace
    ADD CONSTRAINT metadata_namespace_pkey PRIMARY KEY (metadata_namespace_id);


--
-- TOC entry 2480 (class 2606 OID 40931)
-- Dependencies: 2022 2022
-- Name: metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_pkey PRIMARY KEY (metadata_id);


--
-- TOC entry 2484 (class 2606 OID 40933)
-- Dependencies: 2027 2027
-- Name: metadata_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata_xlink
    ADD CONSTRAINT metadata_xlink_pkey PRIMARY KEY (metadata_xlink_id);


--
-- TOC entry 2488 (class 2606 OID 40935)
-- Dependencies: 2031 2031
-- Name: service_datasetroot_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service_datasetroot
    ADD CONSTRAINT service_datasetroot_pkey PRIMARY KEY (service_datasetroot_id);


--
-- TOC entry 2486 (class 2606 OID 40937)
-- Dependencies: 2029 2029
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (service_id);


--
-- TOC entry 2490 (class 2606 OID 40939)
-- Dependencies: 2033 2033
-- Name: service_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service_property
    ADD CONSTRAINT service_property_pkey PRIMARY KEY (service_property_id);


--
-- TOC entry 2492 (class 2606 OID 40941)
-- Dependencies: 2049 2049
-- Name: threddsmetadatagroup_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg
    ADD CONSTRAINT threddsmetadatagroup_pkey PRIMARY KEY (tmg_id);


--
-- TOC entry 2494 (class 2606 OID 40943)
-- Dependencies: 2050 2050
-- Name: tmg_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_authority
    ADD CONSTRAINT tmg_authority_pkey PRIMARY KEY (tmg_authority_id);


--
-- TOC entry 2496 (class 2606 OID 40945)
-- Dependencies: 2052 2052
-- Name: tmg_contributor_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_contributor
    ADD CONSTRAINT tmg_contributor_pkey PRIMARY KEY (tmg_contributor_id);


--
-- TOC entry 2500 (class 2606 OID 40947)
-- Dependencies: 2055 2055
-- Name: tmg_creator_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator_contact
    ADD CONSTRAINT tmg_creator_contact_pkey PRIMARY KEY (tmg_creator_contact_id);


--
-- TOC entry 2502 (class 2606 OID 40949)
-- Dependencies: 2057 2057
-- Name: tmg_creator_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator_name
    ADD CONSTRAINT tmg_creator_name_pkey PRIMARY KEY (tmg_creator_name_id);


--
-- TOC entry 2498 (class 2606 OID 40951)
-- Dependencies: 2054 2054
-- Name: tmg_creator_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator
    ADD CONSTRAINT tmg_creator_pkey PRIMARY KEY (tmg_creator_id);


--
-- TOC entry 2504 (class 2606 OID 40953)
-- Dependencies: 2060 2060
-- Name: tmg_dataformat_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_dataformat
    ADD CONSTRAINT tmg_dataformat_pkey PRIMARY KEY (tmg_dataformat_id);


--
-- TOC entry 2506 (class 2606 OID 40955)
-- Dependencies: 2062 2062
-- Name: tmg_datasize_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_datasize
    ADD CONSTRAINT tmg_datasize_pkey PRIMARY KEY (tmg_datasize_id);


--
-- TOC entry 2508 (class 2606 OID 40957)
-- Dependencies: 2064 2064
-- Name: tmg_datatype_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_datatype
    ADD CONSTRAINT tmg_datatype_pkey PRIMARY KEY (tmg_datatype_id);


--
-- TOC entry 2510 (class 2606 OID 40959)
-- Dependencies: 2066 2066
-- Name: tmg_date_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_date
    ADD CONSTRAINT tmg_date_pkey PRIMARY KEY (tmg_date_id);


--
-- TOC entry 2514 (class 2606 OID 40961)
-- Dependencies: 2069 2069
-- Name: tmg_documentation_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation_namespace
    ADD CONSTRAINT tmg_documentation_namespace_pkey PRIMARY KEY (tmg_documentation_namespace_id);


--
-- TOC entry 2512 (class 2606 OID 40963)
-- Dependencies: 2068 2068
-- Name: tmg_documentation_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation
    ADD CONSTRAINT tmg_documentation_pkey PRIMARY KEY (tmg_documentation_id);


--
-- TOC entry 2516 (class 2606 OID 40965)
-- Dependencies: 2072 2072
-- Name: tmg_documentation_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation_xlink
    ADD CONSTRAINT tmg_documentation_xlink_pkey PRIMARY KEY (tmg_documentation_xlink_id);


--
-- TOC entry 2520 (class 2606 OID 40967)
-- Dependencies: 2075 2075
-- Name: tmg_geospatialcoverage_eastwest_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_eastwest
    ADD CONSTRAINT tmg_geospatialcoverage_eastwest_pkey PRIMARY KEY (tmg_geospatialcoverage_eastwest_id);


--
-- TOC entry 2522 (class 2606 OID 40969)
-- Dependencies: 2077 2077
-- Name: tmg_geospatialcoverage_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_name
    ADD CONSTRAINT tmg_geospatialcoverage_name_pkey PRIMARY KEY (tmg_geospatialcoverage_name_id);


--
-- TOC entry 2524 (class 2606 OID 40971)
-- Dependencies: 2079 2079
-- Name: tmg_geospatialcoverage_northsouth_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_northsouth
    ADD CONSTRAINT tmg_geospatialcoverage_northsouth_pkey PRIMARY KEY (tmg_geospatialcoverage_northsouth_id);


--
-- TOC entry 2518 (class 2606 OID 40973)
-- Dependencies: 2074 2074
-- Name: tmg_geospatialcoverage_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage
    ADD CONSTRAINT tmg_geospatialcoverage_pkey PRIMARY KEY (tmg_geospatialcoverage_id);


--
-- TOC entry 2526 (class 2606 OID 40975)
-- Dependencies: 2082 2082
-- Name: tmg_geospatialcoverage_updown_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_updown
    ADD CONSTRAINT tmg_geospatialcoverage_updown_pkey PRIMARY KEY (tmg_geospatialcoverage_updown_id);


--
-- TOC entry 2528 (class 2606 OID 40977)
-- Dependencies: 2084 2084
-- Name: tmg_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_keyword
    ADD CONSTRAINT tmg_keyword_pkey PRIMARY KEY (tmg_keyword_id);


--
-- TOC entry 2530 (class 2606 OID 40979)
-- Dependencies: 2087 2087
-- Name: tmg_project_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_project
    ADD CONSTRAINT tmg_project_pkey PRIMARY KEY (tmg_project_id);


--
-- TOC entry 2532 (class 2606 OID 40981)
-- Dependencies: 2089 2089
-- Name: tmg_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_property
    ADD CONSTRAINT tmg_property_pkey PRIMARY KEY (tmg_property_id);


--
-- TOC entry 2536 (class 2606 OID 40983)
-- Dependencies: 2092 2092
-- Name: tmg_publisher_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher_contact
    ADD CONSTRAINT tmg_publisher_contact_pkey PRIMARY KEY (tmg_publisher_contact_id);


--
-- TOC entry 2538 (class 2606 OID 40985)
-- Dependencies: 2094 2094
-- Name: tmg_publisher_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher_name
    ADD CONSTRAINT tmg_publisher_name_pkey PRIMARY KEY (tmg_publisher_name_id);


--
-- TOC entry 2534 (class 2606 OID 40987)
-- Dependencies: 2091 2091
-- Name: tmg_publisher_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher
    ADD CONSTRAINT tmg_publisher_pkey PRIMARY KEY (tmg_publisher_id);


--
-- TOC entry 2540 (class 2606 OID 40989)
-- Dependencies: 2097 2097
-- Name: tmg_servicename_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_servicename
    ADD CONSTRAINT tmg_servicename_pkey PRIMARY KEY (tmg_servicename_id);


--
-- TOC entry 2544 (class 2606 OID 40991)
-- Dependencies: 2100 2100
-- Name: tmg_timecoverage_duration_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_duration
    ADD CONSTRAINT tmg_timecoverage_duration_pkey PRIMARY KEY (tmg_timecoverage_duration_id);


--
-- TOC entry 2546 (class 2606 OID 40993)
-- Dependencies: 2102 2102
-- Name: tmg_timecoverage_end_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_end
    ADD CONSTRAINT tmg_timecoverage_end_pkey PRIMARY KEY (tmg_timecoverage_end_id);


--
-- TOC entry 2542 (class 2606 OID 40995)
-- Dependencies: 2099 2099
-- Name: tmg_timecoverage_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage
    ADD CONSTRAINT tmg_timecoverage_pkey PRIMARY KEY (tmg_timecoverage_id);


--
-- TOC entry 2548 (class 2606 OID 40997)
-- Dependencies: 2104 2104
-- Name: tmg_timecoverage_resolution_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_resolution
    ADD CONSTRAINT tmg_timecoverage_resolution_pkey PRIMARY KEY (tmg_timecoverage_resolution_id);


--
-- TOC entry 2550 (class 2606 OID 40999)
-- Dependencies: 2106 2106
-- Name: tmg_timecoverage_start_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_start
    ADD CONSTRAINT tmg_timecoverage_start_pkey PRIMARY KEY (tmg_timecoverage_start_id);


--
-- TOC entry 2552 (class 2606 OID 41001)
-- Dependencies: 2110 2110
-- Name: tmg_variables_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables
    ADD CONSTRAINT tmg_variables_pkey PRIMARY KEY (tmg_variables_id);


--
-- TOC entry 2554 (class 2606 OID 41003)
-- Dependencies: 2112 2112
-- Name: tmg_variables_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_variable
    ADD CONSTRAINT tmg_variables_variable_pkey PRIMARY KEY (tmg_variables_variable_id);


--
-- TOC entry 2556 (class 2606 OID 41005)
-- Dependencies: 2114 2114
-- Name: tmg_variables_variablemap_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_variablemap
    ADD CONSTRAINT tmg_variables_variablemap_pkey PRIMARY KEY (tmg_variables_variablemap_id);


--
-- TOC entry 2558 (class 2606 OID 41007)
-- Dependencies: 2116 2116
-- Name: tmg_variables_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_xlink
    ADD CONSTRAINT tmg_variables_xlink_pkey PRIMARY KEY (tmg_variables_xlink_id);


--
-- TOC entry 2566 (class 2606 OID 41008)
-- Dependencies: 1993 1983 2453
-- Name: catalog_catalog_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalog_catalog_child_id_fkey FOREIGN KEY (child_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2567 (class 2606 OID 41013)
-- Dependencies: 1993 1983 2453
-- Name: catalog_catalog_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalog_catalog_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2559 (class 2606 OID 43033)
-- Dependencies: 1983 1983 2453
-- Name: catalog_clean_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog
    ADD CONSTRAINT catalog_clean_catalog_id_fkey FOREIGN KEY (clean_catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2560 (class 2606 OID 41018)
-- Dependencies: 1985 1983 2453
-- Name: catalog_dataset_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_dataset
    ADD CONSTRAINT catalog_dataset_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2561 (class 2606 OID 41023)
-- Dependencies: 1985 2006 2469
-- Name: catalog_dataset_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_dataset
    ADD CONSTRAINT catalog_dataset_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2562 (class 2606 OID 41028)
-- Dependencies: 1987 1983 2453
-- Name: catalog_property_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_property
    ADD CONSTRAINT catalog_property_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2563 (class 2606 OID 41033)
-- Dependencies: 1983 1989 2453
-- Name: catalog_service_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_service
    ADD CONSTRAINT catalog_service_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2564 (class 2606 OID 41038)
-- Dependencies: 2485 1989 2029
-- Name: catalog_service_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_service
    ADD CONSTRAINT catalog_service_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2565 (class 2606 OID 41043)
-- Dependencies: 1983 1991 2453
-- Name: catalog_xlink_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_xlink
    ADD CONSTRAINT catalog_xlink_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2568 (class 2606 OID 41048)
-- Dependencies: 2459 1996 1993
-- Name: catalogref_documentation_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation
    ADD CONSTRAINT catalogref_documentation_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2569 (class 2606 OID 41053)
-- Dependencies: 2461 1996 1999
-- Name: catalogref_documentation_names_catalogref_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation_namespace
    ADD CONSTRAINT catalogref_documentation_names_catalogref_documentation_id_fkey FOREIGN KEY (catalogref_documentation_id) REFERENCES catalogref_documentation(catalogref_documentation_id);


--
-- TOC entry 2570 (class 2606 OID 41058)
-- Dependencies: 2461 1996 2002
-- Name: catalogref_documentation_xlink_catalogref_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation_xlink
    ADD CONSTRAINT catalogref_documentation_xlink_catalogref_documentation_id_fkey FOREIGN KEY (catalogref_documentation_id) REFERENCES catalogref_documentation(catalogref_documentation_id);


--
-- TOC entry 2571 (class 2606 OID 41063)
-- Dependencies: 1993 2004 2459
-- Name: catalogref_xlink_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_xlink
    ADD CONSTRAINT catalogref_xlink_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2572 (class 2606 OID 41068)
-- Dependencies: 2008 2469 2006
-- Name: dataset_access_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_access
    ADD CONSTRAINT dataset_access_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2573 (class 2606 OID 41073)
-- Dependencies: 2011 2471 2008
-- Name: dataset_access_datasize_dataset_access_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_access_datasize
    ADD CONSTRAINT dataset_access_datasize_dataset_access_id_fkey FOREIGN KEY (dataset_access_id) REFERENCES dataset_access(dataset_access_id);


--
-- TOC entry 2574 (class 2606 OID 41078)
-- Dependencies: 2459 1993 2013
-- Name: dataset_catalogref_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_catalogref
    ADD CONSTRAINT dataset_catalogref_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2575 (class 2606 OID 41083)
-- Dependencies: 2006 2469 2013
-- Name: dataset_catalogref_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_catalogref
    ADD CONSTRAINT dataset_catalogref_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2576 (class 2606 OID 41088)
-- Dependencies: 2014 2006 2469
-- Name: dataset_dataset_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_dataset
    ADD CONSTRAINT dataset_dataset_child_id_fkey FOREIGN KEY (child_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2577 (class 2606 OID 41093)
-- Dependencies: 2469 2014 2006
-- Name: dataset_dataset_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_dataset
    ADD CONSTRAINT dataset_dataset_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2578 (class 2606 OID 41098)
-- Dependencies: 2006 2469 2016
-- Name: dataset_ncml_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_ncml
    ADD CONSTRAINT dataset_ncml_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2579 (class 2606 OID 41103)
-- Dependencies: 2018 2469 2006
-- Name: dataset_property_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_property
    ADD CONSTRAINT dataset_property_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2580 (class 2606 OID 41108)
-- Dependencies: 2469 2006 2020
-- Name: dataset_service_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_service
    ADD CONSTRAINT dataset_service_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2581 (class 2606 OID 41113)
-- Dependencies: 2029 2485 2020
-- Name: dataset_service_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_service
    ADD CONSTRAINT dataset_service_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2582 (class 2606 OID 41118)
-- Dependencies: 2006 2469 2021
-- Name: dataset_tmg_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_tmg
    ADD CONSTRAINT dataset_tmg_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2583 (class 2606 OID 41123)
-- Dependencies: 2049 2021 2491
-- Name: dataset_tmg_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_tmg
    ADD CONSTRAINT dataset_tmg_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2584 (class 2606 OID 41128)
-- Dependencies: 2479 2022 2024
-- Name: metadata_namespace_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_namespace
    ADD CONSTRAINT metadata_namespace_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2585 (class 2606 OID 41133)
-- Dependencies: 2022 2026 2479
-- Name: metadata_tmg_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_tmg
    ADD CONSTRAINT metadata_tmg_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2586 (class 2606 OID 41138)
-- Dependencies: 2026 2491 2049
-- Name: metadata_tmg_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_tmg
    ADD CONSTRAINT metadata_tmg_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2587 (class 2606 OID 41143)
-- Dependencies: 2479 2022 2027
-- Name: metadata_xlink_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_xlink
    ADD CONSTRAINT metadata_xlink_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2588 (class 2606 OID 41148)
-- Dependencies: 2029 2031 2485
-- Name: service_datasetroot_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_datasetroot
    ADD CONSTRAINT service_datasetroot_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2589 (class 2606 OID 41153)
-- Dependencies: 2485 2029 2033
-- Name: service_property_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_property
    ADD CONSTRAINT service_property_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2590 (class 2606 OID 41158)
-- Dependencies: 2029 2485 2035
-- Name: service_service_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_service
    ADD CONSTRAINT service_service_child_id_fkey FOREIGN KEY (child_id) REFERENCES service(service_id);


--
-- TOC entry 2591 (class 2606 OID 41163)
-- Dependencies: 2035 2485 2029
-- Name: service_service_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_service
    ADD CONSTRAINT service_service_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES service(service_id);


--
-- TOC entry 2592 (class 2606 OID 41168)
-- Dependencies: 2050 2491 2049
-- Name: tmg_authority_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_authority
    ADD CONSTRAINT tmg_authority_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2593 (class 2606 OID 41173)
-- Dependencies: 2052 2491 2049
-- Name: tmg_contributor_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_contributor
    ADD CONSTRAINT tmg_contributor_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2595 (class 2606 OID 41178)
-- Dependencies: 2054 2497 2055
-- Name: tmg_creator_contact_tmg_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator_contact
    ADD CONSTRAINT tmg_creator_contact_tmg_creator_id_fkey FOREIGN KEY (tmg_creator_id) REFERENCES tmg_creator(tmg_creator_id);


--
-- TOC entry 2596 (class 2606 OID 41183)
-- Dependencies: 2497 2054 2057
-- Name: tmg_creator_name_tmg_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator_name
    ADD CONSTRAINT tmg_creator_name_tmg_creator_id_fkey FOREIGN KEY (tmg_creator_id) REFERENCES tmg_creator(tmg_creator_id);


--
-- TOC entry 2594 (class 2606 OID 41188)
-- Dependencies: 2491 2054 2049
-- Name: tmg_creator_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator
    ADD CONSTRAINT tmg_creator_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2597 (class 2606 OID 41193)
-- Dependencies: 2060 2049 2491
-- Name: tmg_dataformat_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_dataformat
    ADD CONSTRAINT tmg_dataformat_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2598 (class 2606 OID 41198)
-- Dependencies: 2062 2049 2491
-- Name: tmg_datasize_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_datasize
    ADD CONSTRAINT tmg_datasize_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2599 (class 2606 OID 41203)
-- Dependencies: 2491 2049 2064
-- Name: tmg_datatype_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_datatype
    ADD CONSTRAINT tmg_datatype_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2600 (class 2606 OID 41208)
-- Dependencies: 2491 2049 2066
-- Name: tmg_date_threddsmetadatagroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_date
    ADD CONSTRAINT tmg_date_threddsmetadatagroup_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2602 (class 2606 OID 41213)
-- Dependencies: 2069 2068 2511
-- Name: tmg_documentation_namespace_tmg_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation_namespace
    ADD CONSTRAINT tmg_documentation_namespace_tmg_documentation_id_fkey FOREIGN KEY (tmg_documentation_id) REFERENCES tmg_documentation(tmg_documentation_id);


--
-- TOC entry 2601 (class 2606 OID 41218)
-- Dependencies: 2068 2491 2049
-- Name: tmg_documentation_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation
    ADD CONSTRAINT tmg_documentation_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2603 (class 2606 OID 41223)
-- Dependencies: 2072 2068 2511
-- Name: tmg_documentation_xlink_tmg_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation_xlink
    ADD CONSTRAINT tmg_documentation_xlink_tmg_documentation_id_fkey FOREIGN KEY (tmg_documentation_id) REFERENCES tmg_documentation(tmg_documentation_id);


--
-- TOC entry 2605 (class 2606 OID 41228)
-- Dependencies: 2075 2517 2074
-- Name: tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_eastwest
    ADD CONSTRAINT tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2606 (class 2606 OID 41233)
-- Dependencies: 2077 2074 2517
-- Name: tmg_geospatialcoverage_name_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_name
    ADD CONSTRAINT tmg_geospatialcoverage_name_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2607 (class 2606 OID 41238)
-- Dependencies: 2517 2079 2074
-- Name: tmg_geospatialcoverage_northsout_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_northsouth
    ADD CONSTRAINT tmg_geospatialcoverage_northsout_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2604 (class 2606 OID 41243)
-- Dependencies: 2049 2074 2491
-- Name: tmg_geospatialcoverage_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage
    ADD CONSTRAINT tmg_geospatialcoverage_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2608 (class 2606 OID 41248)
-- Dependencies: 2082 2074 2517
-- Name: tmg_geospatialcoverage_updown_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_updown
    ADD CONSTRAINT tmg_geospatialcoverage_updown_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2609 (class 2606 OID 41253)
-- Dependencies: 2084 2049 2491
-- Name: tmg_keyword_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_keyword
    ADD CONSTRAINT tmg_keyword_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2610 (class 2606 OID 41258)
-- Dependencies: 2086 2022 2479
-- Name: tmg_metadata_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_metadata
    ADD CONSTRAINT tmg_metadata_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2611 (class 2606 OID 41263)
-- Dependencies: 2491 2086 2049
-- Name: tmg_metadata_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_metadata
    ADD CONSTRAINT tmg_metadata_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2612 (class 2606 OID 41268)
-- Dependencies: 2049 2491 2087
-- Name: tmg_project_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_project
    ADD CONSTRAINT tmg_project_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2613 (class 2606 OID 41273)
-- Dependencies: 2491 2089 2049
-- Name: tmg_property_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_property
    ADD CONSTRAINT tmg_property_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2615 (class 2606 OID 41278)
-- Dependencies: 2533 2092 2091
-- Name: tmg_publisher_contact_tmg_publisher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher_contact
    ADD CONSTRAINT tmg_publisher_contact_tmg_publisher_id_fkey FOREIGN KEY (tmg_publisher_id) REFERENCES tmg_publisher(tmg_publisher_id);


--
-- TOC entry 2616 (class 2606 OID 41283)
-- Dependencies: 2094 2091 2533
-- Name: tmg_publisher_name_tmg_publisher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher_name
    ADD CONSTRAINT tmg_publisher_name_tmg_publisher_id_fkey FOREIGN KEY (tmg_publisher_id) REFERENCES tmg_publisher(tmg_publisher_id);


--
-- TOC entry 2614 (class 2606 OID 41288)
-- Dependencies: 2491 2091 2049
-- Name: tmg_publisher_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher
    ADD CONSTRAINT tmg_publisher_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2617 (class 2606 OID 41293)
-- Dependencies: 2491 2097 2049
-- Name: tmg_servicename_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_servicename
    ADD CONSTRAINT tmg_servicename_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2619 (class 2606 OID 41298)
-- Dependencies: 2100 2541 2099
-- Name: tmg_timecoverage_duration_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_duration
    ADD CONSTRAINT tmg_timecoverage_duration_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2620 (class 2606 OID 41303)
-- Dependencies: 2541 2099 2102
-- Name: tmg_timecoverage_end_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_end
    ADD CONSTRAINT tmg_timecoverage_end_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2621 (class 2606 OID 41308)
-- Dependencies: 2104 2541 2099
-- Name: tmg_timecoverage_resolution_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_resolution
    ADD CONSTRAINT tmg_timecoverage_resolution_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2622 (class 2606 OID 41313)
-- Dependencies: 2541 2106 2099
-- Name: tmg_timecoverage_start_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_start
    ADD CONSTRAINT tmg_timecoverage_start_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2618 (class 2606 OID 41318)
-- Dependencies: 2491 2049 2099
-- Name: tmg_timecoverage_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage
    ADD CONSTRAINT tmg_timecoverage_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2623 (class 2606 OID 41323)
-- Dependencies: 2110 2049 2491
-- Name: tmg_variables_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables
    ADD CONSTRAINT tmg_variables_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2624 (class 2606 OID 41328)
-- Dependencies: 2551 2112 2110
-- Name: tmg_variables_variable_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_variable
    ADD CONSTRAINT tmg_variables_variable_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2625 (class 2606 OID 41333)
-- Dependencies: 2110 2114 2551
-- Name: tmg_variables_variablemap_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_variablemap
    ADD CONSTRAINT tmg_variables_variablemap_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2626 (class 2606 OID 41338)
-- Dependencies: 2110 2116 2551
-- Name: tmg_variables_xlink_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_xlink
    ADD CONSTRAINT tmg_variables_xlink_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2631 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2011-08-29 17:05:11

--
-- PostgreSQL database dump complete
--

