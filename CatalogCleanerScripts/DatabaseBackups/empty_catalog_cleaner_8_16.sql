--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0.4
-- Dumped by pg_dump version 9.0.4
-- Started on 2011-08-16 14:03:26

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 790 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- TOC entry 787 (class 1247 OID 39393)
-- Dependencies: 6
-- Name: booltype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE booltype AS ENUM (
    'true',
    'false'
);


ALTER TYPE public.booltype OWNER TO postgres;

--
-- TOC entry 479 (class 1247 OID 37687)
-- Dependencies: 6
-- Name: collectiontype; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE collectiontype AS ENUM (
    'TimeSeries',
    'Stations'
);


ALTER TYPE public.collectiontype OWNER TO postgres;

--
-- TOC entry 481 (class 1247 OID 37691)
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
-- TOC entry 483 (class 1247 OID 37712)
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
-- TOC entry 485 (class 1247 OID 37719)
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
-- TOC entry 487 (class 1247 OID 37726)
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
-- TOC entry 489 (class 1247 OID 37733)
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
-- TOC entry 491 (class 1247 OID 37740)
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
-- TOC entry 493 (class 1247 OID 37753)
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
-- TOC entry 495 (class 1247 OID 37774)
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
-- TOC entry 497 (class 1247 OID 37782)
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
-- TOC entry 499 (class 1247 OID 37794)
-- Dependencies: 6
-- Name: upordown; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE upordown AS ENUM (
    'up',
    'down'
);


ALTER TYPE public.upordown OWNER TO postgres;

--
-- TOC entry 501 (class 1247 OID 37798)
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
-- TOC entry 503 (class 1247 OID 37803)
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
-- TOC entry 131 (class 1255 OID 39695)
-- Dependencies: 790 6
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
-- TOC entry 132 (class 1255 OID 39696)
-- Dependencies: 790 6
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
-- TOC entry 133 (class 1255 OID 39697)
-- Dependencies: 790 6
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
-- TOC entry 134 (class 1255 OID 39698)
-- Dependencies: 790 6
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
-- TOC entry 135 (class 1255 OID 39699)
-- Dependencies: 6 790
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
-- TOC entry 136 (class 1255 OID 39700)
-- Dependencies: 6 790
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
-- TOC entry 137 (class 1255 OID 39701)
-- Dependencies: 6 790
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
-- TOC entry 138 (class 1255 OID 39702)
-- Dependencies: 790 6
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
-- TOC entry 139 (class 1255 OID 39703)
-- Dependencies: 6 790
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
-- TOC entry 140 (class 1255 OID 39704)
-- Dependencies: 6 790
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
-- TOC entry 141 (class 1255 OID 39705)
-- Dependencies: 790 6
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
-- TOC entry 142 (class 1255 OID 39706)
-- Dependencies: 6 790
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
-- TOC entry 143 (class 1255 OID 39707)
-- Dependencies: 6 790
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
-- TOC entry 144 (class 1255 OID 39708)
-- Dependencies: 6 790
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
-- TOC entry 145 (class 1255 OID 39709)
-- Dependencies: 790 6
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
-- TOC entry 146 (class 1255 OID 39710)
-- Dependencies: 6 790
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
-- TOC entry 147 (class 1255 OID 39711)
-- Dependencies: 790 6
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
-- TOC entry 148 (class 1255 OID 39712)
-- Dependencies: 6 790
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
-- TOC entry 149 (class 1255 OID 39713)
-- Dependencies: 790 6
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
-- TOC entry 150 (class 1255 OID 39714)
-- Dependencies: 790 6
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
-- TOC entry 151 (class 1255 OID 39715)
-- Dependencies: 790 6
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
-- TOC entry 152 (class 1255 OID 39716)
-- Dependencies: 6 790
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
-- TOC entry 153 (class 1255 OID 39717)
-- Dependencies: 790 6
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
-- TOC entry 154 (class 1255 OID 39718)
-- Dependencies: 790 6
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
-- TOC entry 155 (class 1255 OID 39719)
-- Dependencies: 790 6
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
-- TOC entry 156 (class 1255 OID 39720)
-- Dependencies: 6 790
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
-- TOC entry 157 (class 1255 OID 39721)
-- Dependencies: 790 6
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
-- TOC entry 158 (class 1255 OID 39722)
-- Dependencies: 790 6
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
-- TOC entry 159 (class 1255 OID 39723)
-- Dependencies: 6 790
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
-- TOC entry 160 (class 1255 OID 39724)
-- Dependencies: 790 6
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
-- TOC entry 161 (class 1255 OID 39725)
-- Dependencies: 6 790
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
-- TOC entry 162 (class 1255 OID 39726)
-- Dependencies: 6 790
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
-- TOC entry 163 (class 1255 OID 39727)
-- Dependencies: 790 6
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
-- TOC entry 164 (class 1255 OID 39728)
-- Dependencies: 790 6
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
-- TOC entry 165 (class 1255 OID 39729)
-- Dependencies: 790 6
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
-- TOC entry 166 (class 1255 OID 39730)
-- Dependencies: 790 6
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
-- TOC entry 167 (class 1255 OID 39731)
-- Dependencies: 6 790
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
-- TOC entry 168 (class 1255 OID 39732)
-- Dependencies: 6 790
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
-- TOC entry 169 (class 1255 OID 39733)
-- Dependencies: 6 790
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
-- TOC entry 170 (class 1255 OID 39734)
-- Dependencies: 6 790
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
-- TOC entry 171 (class 1255 OID 39735)
-- Dependencies: 6 790
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
-- TOC entry 172 (class 1255 OID 39736)
-- Dependencies: 6 790
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
-- TOC entry 173 (class 1255 OID 39737)
-- Dependencies: 6 790
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
-- TOC entry 174 (class 1255 OID 39738)
-- Dependencies: 6 790
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
-- TOC entry 175 (class 1255 OID 39739)
-- Dependencies: 790 6
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
-- TOC entry 176 (class 1255 OID 39740)
-- Dependencies: 790 6
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
-- TOC entry 177 (class 1255 OID 39741)
-- Dependencies: 790 6
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
-- TOC entry 178 (class 1255 OID 39742)
-- Dependencies: 790 6
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
-- TOC entry 179 (class 1255 OID 39743)
-- Dependencies: 790 6
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
-- TOC entry 180 (class 1255 OID 39744)
-- Dependencies: 790 6
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
-- TOC entry 181 (class 1255 OID 39745)
-- Dependencies: 790 6
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
-- TOC entry 182 (class 1255 OID 39746)
-- Dependencies: 790 6
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
-- TOC entry 183 (class 1255 OID 39747)
-- Dependencies: 6 790
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
-- TOC entry 184 (class 1255 OID 39748)
-- Dependencies: 790 6
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
-- TOC entry 185 (class 1255 OID 39749)
-- Dependencies: 6 790
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
-- TOC entry 186 (class 1255 OID 39750)
-- Dependencies: 6 790
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
-- TOC entry 187 (class 1255 OID 39751)
-- Dependencies: 790 6
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
-- TOC entry 188 (class 1255 OID 39752)
-- Dependencies: 790 6
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
-- TOC entry 189 (class 1255 OID 39753)
-- Dependencies: 790 6
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
-- TOC entry 190 (class 1255 OID 39754)
-- Dependencies: 790 6
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
-- TOC entry 191 (class 1255 OID 39755)
-- Dependencies: 6 790
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
-- TOC entry 18 (class 1255 OID 39582)
-- Dependencies: 6 790
-- Name: insert_catalog(text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog(p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog("name", "expires", "version", "base", "xmlns") values (p_name, p_expires, p_version, p_base, p_xmlns);
		select currval('catalog_catalog_id_seq') into id;
		update catalog set status = cast(p_status as status) where catalog_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalog(p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) OWNER TO postgres;

--
-- TOC entry 19 (class 1255 OID 39583)
-- Dependencies: 790 6
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
-- TOC entry 20 (class 1255 OID 39584)
-- Dependencies: 6 790
-- Name: insert_catalog_property(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_property(p_name text, p_value text, p_catalog_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_property("name", "value", "catalog_id") values (p_name, p_value, p_catalog_id);
		select currval('catalog_property_catalog_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalog_property(p_name text, p_value text, p_catalog_id integer) OWNER TO postgres;

--
-- TOC entry 21 (class 1255 OID 39585)
-- Dependencies: 6 790
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
-- TOC entry 22 (class 1255 OID 39586)
-- Dependencies: 790 6
-- Name: insert_catalog_xlink(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalog_xlink(p_value text, p_xlink text, p_catalog_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalog_xlink("value", "catalog_id") values (p_value, p_catalog_id);
		select currval('catalog_xlink_catalog_xlink_id_seq') into id;
		BEGIN
			update catalog_xlink set xlink = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set xlink_nonstandard = p_xlink where catalog_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalog_xlink(p_value text, p_xlink text, p_catalog_id integer) OWNER TO postgres;

--
-- TOC entry 23 (class 1255 OID 39587)
-- Dependencies: 6 790
-- Name: insert_catalogref(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref(p_child_id integer, p_parent_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref("child_id", "parent_id") values (p_child_id, p_parent_id);
		select currval('catalogref_catalogref_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref(p_child_id integer, p_parent_id integer) OWNER TO postgres;

--
-- TOC entry 24 (class 1255 OID 39588)
-- Dependencies: 6 790
-- Name: insert_catalogref_documentation(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation(p_value text, p_documentationenum text, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation("value", "catalogref_id") values (p_value, p_catalogref_id);
		select currval('catalogref_documentation_catalogref_documentation_id_seq') into id;
		BEGIN
			update catalogref_documentation set documentationenum = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set documentationenum_nonstandard = p_documentationenum where catalogref_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref_documentation(p_value text, p_documentationenum text, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 25 (class 1255 OID 39589)
-- Dependencies: 6 790
-- Name: insert_catalogref_documentation_namespace(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation_namespace(p_namespace text, p_catalogref_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation_namespace("namespace", "catalogref_documentation_id") values (p_namespace, p_catalogref_documentation_id);
		select currval('catalogref_documentation_namespace_catalogref_documentation_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref_documentation_namespace(p_namespace text, p_catalogref_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 26 (class 1255 OID 39590)
-- Dependencies: 6 790
-- Name: insert_catalogref_documentation_xlink(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_documentation_xlink(p_value text, p_xlink text, p_catalogref_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_documentation_xlink("value", "catalogref_documentation_id") values (p_value, p_catalogref_documentation_id);
		select currval('catalogref_documentation_xlink_catalogref_documentation_xlink_id_seq') into id;
		BEGIN
			update catalogref_documentation_xlink set xlink = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set xlink_nonstandard = p_xlink where catalogref_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref_documentation_xlink(p_value text, p_xlink text, p_catalogref_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 27 (class 1255 OID 39591)
-- Dependencies: 790 6
-- Name: insert_catalogref_xlink(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_catalogref_xlink(p_value text, p_xlink text, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into catalogref_xlink("value", "catalogref_id") values (p_value, p_catalogref_id);
		select currval('catalogref_xlink_catalogref_xlink_id_seq') into id;
		BEGIN
			update catalogref_xlink set xlink = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set xlink_nonstandard = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_catalogref_xlink(p_value text, p_xlink text, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 28 (class 1255 OID 39592)
-- Dependencies: 6 790
-- Name: insert_dataset(text, text, text, text, text, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset(p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_dataset(p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) OWNER TO postgres;

--
-- TOC entry 29 (class 1255 OID 39593)
-- Dependencies: 6 790
-- Name: insert_dataset_access(text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_access(p_urlpath text, p_servicename text, p_dataformat text, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_access("urlpath", "servicename", "dataset_id") values (p_urlpath, p_servicename, p_dataset_id);
		select currval('dataset_access_dataset_access_id_seq') into id;
		BEGIN
			update dataset_access set dataformat = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set dataformat_nonstandard = p_dataformat where dataset_access_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_dataset_access(p_urlpath text, p_servicename text, p_dataformat text, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 30 (class 1255 OID 39594)
-- Dependencies: 6 790
-- Name: insert_dataset_access_datasize(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_access_datasize(p_value text, p_units text, p_dataset_access_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_access_datasize("value", "dataset_access_id") values (p_value, p_dataset_access_id);
		select currval('dataset_access_datasize_dataset_access_datasize_id_seq') into id;
		BEGIN
			update dataset_access_datasize set units = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set units_nonstandard = p_units where dataset_access_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_dataset_access_datasize(p_value text, p_units text, p_dataset_access_id integer) OWNER TO postgres;

--
-- TOC entry 31 (class 1255 OID 39595)
-- Dependencies: 790 6
-- Name: insert_dataset_catalogref(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_catalogref(p_catalogref_id integer, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_catalogref("catalogref_id", "dataset_id") values (p_catalogref_id, p_dataset_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_dataset_catalogref(p_catalogref_id integer, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 32 (class 1255 OID 39596)
-- Dependencies: 790 6
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
-- TOC entry 33 (class 1255 OID 39597)
-- Dependencies: 790 6
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
-- TOC entry 34 (class 1255 OID 39598)
-- Dependencies: 6 790
-- Name: insert_dataset_property(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_dataset_property(p_name text, p_value text, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into dataset_property("name", "value", "dataset_id") values (p_name, p_value, p_dataset_id);
		select currval('dataset_property_dataset_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_dataset_property(p_name text, p_value text, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 35 (class 1255 OID 39599)
-- Dependencies: 6 790
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
-- TOC entry 36 (class 1255 OID 39600)
-- Dependencies: 6 790
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
-- TOC entry 37 (class 1255 OID 39601)
-- Dependencies: 6 790
-- Name: insert_metadata(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata(p_metadatatype text, p_inherited text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_metadata(p_metadatatype text, p_inherited text) OWNER TO postgres;

--
-- TOC entry 38 (class 1255 OID 39602)
-- Dependencies: 6 790
-- Name: insert_metadata_namespace(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_namespace(p_namespace text, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_namespace("namespace", "metadata_id") values (p_namespace, p_metadata_id);
		select currval('metadata_namespace_metadata_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_metadata_namespace(p_namespace text, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 39 (class 1255 OID 39603)
-- Dependencies: 790 6
-- Name: insert_metadata_tmg(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_tmg(p_tmg_id integer, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_tmg("tmg_id", "metadata_id") values (p_tmg_id, p_metadata_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_metadata_tmg(p_tmg_id integer, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 40 (class 1255 OID 39604)
-- Dependencies: 790 6
-- Name: insert_metadata_xlink(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_metadata_xlink(p_value text, p_xlink text, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into metadata_xlink("value", "metadata_id") values (p_value, p_metadata_id);
		select currval('metadata_xlink_metadata_xlink_id_seq') into id;
		BEGIN
			update metadata_xlink set xlink = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set xlink_nonstandard = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_metadata_xlink(p_value text, p_xlink text, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 41 (class 1255 OID 39605)
-- Dependencies: 790 6
-- Name: insert_service(text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service(p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_service(p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) OWNER TO postgres;

--
-- TOC entry 42 (class 1255 OID 39606)
-- Dependencies: 790 6
-- Name: insert_service_datasetroot(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_datasetroot(p_path text, p_location text, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_datasetroot("path", "location", "service_id") values (p_path, p_location, p_service_id);
		select currval('service_datasetroot_service_datasetroot_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_service_datasetroot(p_path text, p_location text, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 43 (class 1255 OID 39607)
-- Dependencies: 6 790
-- Name: insert_service_property(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_property(p_value text, p_name text, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_property("value", "name", "service_id") values (p_value, p_name, p_service_id);
		select currval('service_property_service_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_service_property(p_value text, p_name text, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 44 (class 1255 OID 39608)
-- Dependencies: 790 6
-- Name: insert_service_service(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_service_service(p_child_id integer, p_parent_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into service_service("child_id", "parent_id") values (p_child_id, p_parent_id);

		return 1;
END;
$$;


ALTER FUNCTION public.insert_service_service(p_child_id integer, p_parent_id integer) OWNER TO postgres;

--
-- TOC entry 45 (class 1255 OID 39609)
-- Dependencies: 790 6
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
-- TOC entry 46 (class 1255 OID 39610)
-- Dependencies: 6 790
-- Name: insert_tmg_authority(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_authority(p_authority text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_authority("authority", "tmg_id") values (p_authority, p_tmg_id);
		select currval('tmg_authority_tmg_authority_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_authority(p_authority text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 47 (class 1255 OID 39611)
-- Dependencies: 790 6
-- Name: insert_tmg_contributor(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_contributor(p_role text, p_name text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_contributor("role", "name", "tmg_id") values (p_role, p_name, p_tmg_id);
		select currval('tmg_contributor_tmg_contributor_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_contributor(p_role text, p_name text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 48 (class 1255 OID 39612)
-- Dependencies: 6 790
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
-- TOC entry 49 (class 1255 OID 39613)
-- Dependencies: 6 790
-- Name: insert_tmg_creator_contact(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_creator_contact(p_email text, p_url text, p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_contact("email", "url", "tmg_creator_id") values (p_email, p_url, p_tmg_creator_id);
		select currval('tmg_creator_contact_tmg_creator_contact_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_creator_contact(p_email text, p_url text, p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 50 (class 1255 OID 39614)
-- Dependencies: 6 790
-- Name: insert_tmg_creator_name(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_creator_name(p_value text, p_vocabulary text, p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_creator_name("value", "vocabulary", "tmg_creator_id") values (p_value, p_vocabulary, p_tmg_creator_id);
		select currval('tmg_creator_name_tmg_creator_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_creator_name(p_value text, p_vocabulary text, p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 51 (class 1255 OID 39615)
-- Dependencies: 790 6
-- Name: insert_tmg_dataformat(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_dataformat(p_dataformat text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_dataformat(p_dataformat text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 52 (class 1255 OID 39616)
-- Dependencies: 790 6
-- Name: insert_tmg_datasize(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_datasize(p_value text, p_units text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_datasize("value", "tmg_id") values (p_value, p_tmg_id);
		select currval('tmg_datasize_tmg_datasize_id_seq') into id;
		BEGIN
			update tmg_datasize set units = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set units_nonstandard = p_units where tmg_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_datasize(p_value text, p_units text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 53 (class 1255 OID 39617)
-- Dependencies: 790 6
-- Name: insert_tmg_datatype(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_datatype(p_datatype text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_datatype(p_datatype text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 54 (class 1255 OID 39618)
-- Dependencies: 790 6
-- Name: insert_tmg_date(text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_date(p_format text, p_value text, p_dateenum text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_date("format", "value", "tmg_id") values (p_format, p_value, p_tmg_id);
		select currval('tmg_date_tmg_date_id_seq') into id;
		BEGIN
			update tmg_date set dateenum = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set dateenum_nonstandard = p_dateenum where tmg_date_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_date(p_format text, p_value text, p_dateenum text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 55 (class 1255 OID 39619)
-- Dependencies: 790 6
-- Name: insert_tmg_documentation(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation(p_value text, p_documentationenum text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation("value", "tmg_id") values (p_value, p_tmg_id);
		select currval('tmg_documentation_tmg_documentation_id_seq') into id;
		BEGIN
			update tmg_documentation set documentationenum = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set documentationenum_nonstandard = p_documentationenum where tmg_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_documentation(p_value text, p_documentationenum text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 56 (class 1255 OID 39620)
-- Dependencies: 790 6
-- Name: insert_tmg_documentation_namespace(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation_namespace(p_namespace text, p_tmg_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation_namespace("namespace", "tmg_documentation_id") values (p_namespace, p_tmg_documentation_id);
		select currval('tmg_documentation_namespace_tmg_documentation_namespace_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_documentation_namespace(p_namespace text, p_tmg_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 57 (class 1255 OID 39621)
-- Dependencies: 6 790
-- Name: insert_tmg_documentation_xlink(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_documentation_xlink(p_value text, p_xlink text, p_tmg_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_documentation_xlink("value", "tmg_documentation_id") values (p_value, p_tmg_documentation_id);
		select currval('tmg_documentation_xlink_tmg_documentation_xlink_id_seq') into id;
		BEGIN
			update tmg_documentation_xlink set xlink = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set xlink_nonstandard = p_xlink where tmg_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_documentation_xlink(p_value text, p_xlink text, p_tmg_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 58 (class 1255 OID 39622)
-- Dependencies: 6 790
-- Name: insert_tmg_geospatialcoverage(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage(p_upordown text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage(p_upordown text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 59 (class 1255 OID 39623)
-- Dependencies: 6 790
-- Name: insert_tmg_geospatialcoverage_eastwest(text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_eastwest(p_size text, p_units text, p_start text, p_resolution text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_eastwest("size", "units", "start", "resolution", "tmg_geospatialcoverage_id") values (p_size, p_units, p_start, p_resolution, p_tmg_geospatialcoverage_id);
		select currval('tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_eastwest_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_eastwest(p_size text, p_units text, p_start text, p_resolution text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 60 (class 1255 OID 39624)
-- Dependencies: 6 790
-- Name: insert_tmg_geospatialcoverage_name(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_name(p_vocabulary text, p_value text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_name("vocabulary", "value", "tmg_geospatialcoverage_id") values (p_vocabulary, p_value, p_tmg_geospatialcoverage_id);
		select currval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_name(p_vocabulary text, p_value text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 61 (class 1255 OID 39625)
-- Dependencies: 6 790
-- Name: insert_tmg_geospatialcoverage_northsouth(text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_northsouth(p_size text, p_resolution text, p_start text, p_units text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_northsouth("size", "resolution", "start", "units", "tmg_geospatialcoverage_id") values (p_size, p_resolution, p_start, p_units, p_tmg_geospatialcoverage_id);
		select currval('tmg_geospatialcoverage_northsouth_tmg_geospatialcoverage_northsouth_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_northsouth(p_size text, p_resolution text, p_start text, p_units text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 62 (class 1255 OID 39626)
-- Dependencies: 6 790
-- Name: insert_tmg_geospatialcoverage_updown(text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_geospatialcoverage_updown(p_start text, p_resolution text, p_size text, p_units text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_geospatialcoverage_updown("start", "resolution", "size", "units", "tmg_geospatialcoverage_id") values (p_start, p_resolution, p_size, p_units, p_tmg_geospatialcoverage_id);
		select currval('tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_geospatialcoverage_updown(p_start text, p_resolution text, p_size text, p_units text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 63 (class 1255 OID 39627)
-- Dependencies: 6 790
-- Name: insert_tmg_keyword(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_keyword(p_value text, p_vocabulary text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_keyword("value", "vocabulary", "tmg_id") values (p_value, p_vocabulary, p_tmg_id);
		select currval('tmg_keyword_tmg_keyword_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_keyword(p_value text, p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 64 (class 1255 OID 39628)
-- Dependencies: 6 790
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
-- TOC entry 65 (class 1255 OID 39629)
-- Dependencies: 790 6
-- Name: insert_tmg_project(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_project(p_value text, p_vocabulary text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_project("value", "vocabulary", "tmg_id") values (p_value, p_vocabulary, p_tmg_id);
		select currval('tmg_project_tmg_project_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_project(p_value text, p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 66 (class 1255 OID 39630)
-- Dependencies: 790 6
-- Name: insert_tmg_property(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_property(p_name text, p_value text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_property("name", "value", "tmg_id") values (p_name, p_value, p_tmg_id);
		select currval('tmg_property_tmg_property_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_property(p_name text, p_value text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 67 (class 1255 OID 39631)
-- Dependencies: 6 790
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
-- TOC entry 68 (class 1255 OID 39632)
-- Dependencies: 6 790
-- Name: insert_tmg_publisher_contact(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_publisher_contact(p_url text, p_email text, p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_contact("url", "email", "tmg_publisher_id") values (p_url, p_email, p_tmg_publisher_id);
		select currval('tmg_publisher_contact_tmg_publisher_contact_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_publisher_contact(p_url text, p_email text, p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 69 (class 1255 OID 39633)
-- Dependencies: 790 6
-- Name: insert_tmg_publisher_name(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_publisher_name(p_value text, p_vocabulary text, p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_publisher_name("value", "vocabulary", "tmg_publisher_id") values (p_value, p_vocabulary, p_tmg_publisher_id);
		select currval('tmg_publisher_name_tmg_publisher_name_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_publisher_name(p_value text, p_vocabulary text, p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 70 (class 1255 OID 39634)
-- Dependencies: 790 6
-- Name: insert_tmg_servicename(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_servicename(p_servicename text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_servicename("servicename", "tmg_id") values (p_servicename, p_tmg_id);
		select currval('tmg_servicename_tmg_servicename_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_servicename(p_servicename text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 71 (class 1255 OID 39635)
-- Dependencies: 790 6
-- Name: insert_tmg_timecoverage(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage(p_resolution text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage("resolution", "tmg_id") values (p_resolution, p_tmg_id);
		select currval('tmg_timecoverage_tmg_timecoverage_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage(p_resolution text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 72 (class 1255 OID 39636)
-- Dependencies: 790 6
-- Name: insert_tmg_timecoverage_duration(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_duration(p_duration text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_duration("duration", "tmg_timecoverage_id") values (p_duration, p_tmg_timecoverage_id);
		select currval('tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_duration(p_duration text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 73 (class 1255 OID 39637)
-- Dependencies: 790 6
-- Name: insert_tmg_timecoverage_end(text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_end(p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_end("format", "value", "tmg_timecoverage_id") values (p_format, p_value, p_tmg_timecoverage_id);
		select currval('tmg_timecoverage_end_tmg_timecoverage_end_id_seq') into id;
		BEGIN
			update tmg_timecoverage_end set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set dateenum_nonstandard = p_dateenum where tmg_timecoverage_end_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_end(p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 74 (class 1255 OID 39638)
-- Dependencies: 790 6
-- Name: insert_tmg_timecoverage_resolution(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_resolution(p_duration text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_resolution("duration", "tmg_timecoverage_id") values (p_duration, p_tmg_timecoverage_id);
		select currval('tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_resolution(p_duration text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 76 (class 1255 OID 39639)
-- Dependencies: 790 6
-- Name: insert_tmg_timecoverage_start(text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_timecoverage_start(p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_timecoverage_start("format", "value", "tmg_timecoverage_id") values (p_format, p_value, p_tmg_timecoverage_id);
		select currval('tmg_timecoverage_start_tmg_timecoverage_start_id_seq') into id;
		BEGIN
			update tmg_timecoverage_start set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set dateenum_nonstandard = p_dateenum where tmg_timecoverage_start_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_timecoverage_start(p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 77 (class 1255 OID 39640)
-- Dependencies: 790 6
-- Name: insert_tmg_variables(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables(p_vocabulary text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.insert_tmg_variables(p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 78 (class 1255 OID 39641)
-- Dependencies: 790 6
-- Name: insert_tmg_variables_variable(text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables_variable(p_units text, p_name text, p_vocabulary_name text, p_tmg_variables_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variable("units", "name", "vocabulary_name", "tmg_variables_id") values (p_units, p_name, p_vocabulary_name, p_tmg_variables_id);
		select currval('tmg_variables_variable_tmg_variables_variable_id_seq') into id;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_variables_variable(p_units text, p_name text, p_vocabulary_name text, p_tmg_variables_id integer) OWNER TO postgres;

--
-- TOC entry 79 (class 1255 OID 39642)
-- Dependencies: 790 6
-- Name: insert_tmg_variables_variablemap(text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION insert_tmg_variables_variablemap(p_value text, p_xlink text, p_tmg_variables_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		insert into tmg_variables_variablemap("value", "tmg_variables_id") values (p_value, p_tmg_variables_id);
		select currval('tmg_variables_variablemap_tmg_variables_variablemap_id_seq') into id;
		BEGIN
			update tmg_variables_variablemap set xlink = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set xlink_nonstandard = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.insert_tmg_variables_variablemap(p_value text, p_xlink text, p_tmg_variables_id integer) OWNER TO postgres;

--
-- TOC entry 80 (class 1255 OID 39643)
-- Dependencies: 790 6
-- Name: update_catalog(integer, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog(p_catalog_id integer, p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) RETURNS integer
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

		return id;
END;
$$;


ALTER FUNCTION public.update_catalog(p_catalog_id integer, p_name text, p_expires text, p_version text, p_base text, p_xmlns text, p_status text) OWNER TO postgres;

--
-- TOC entry 81 (class 1255 OID 39644)
-- Dependencies: 6 790
-- Name: update_catalog_property(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog_property(p_catalog_property_id integer, p_name text, p_value text, p_catalog_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalog_property_id into id from catalog_property where catalog_property_id=p_catalog_property_id;
		if(id is null) then
			return -1;
		end if;
		update catalog_property set "name"=p_name, "value"=p_value, "catalog_id"=p_catalog_id where catalog_property_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalog_property(p_catalog_property_id integer, p_name text, p_value text, p_catalog_id integer) OWNER TO postgres;

--
-- TOC entry 82 (class 1255 OID 39645)
-- Dependencies: 6 790
-- Name: update_catalog_xlink(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalog_xlink(p_catalog_xlink_id integer, p_value text, p_xlink text, p_catalog_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalog_xlink_id into id from catalog_xlink where catalog_xlink_id=p_catalog_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalog_xlink set "value"=p_value, "catalog_id"=p_catalog_id where catalog_xlink_id=id;
		BEGIN
			update catalog_xlink set xlink = cast(p_xlink as xlink) where catalog_xlink_id=id;
		EXCEPTION
			when others then
				update catalog_xlink set xlink_nonstandard = p_xlink where catalog_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalog_xlink(p_catalog_xlink_id integer, p_value text, p_xlink text, p_catalog_id integer) OWNER TO postgres;

--
-- TOC entry 83 (class 1255 OID 39646)
-- Dependencies: 790 6
-- Name: update_catalogref(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref(p_catalogref_id integer, p_child_id integer, p_parent_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_id into id from catalogref where catalogref_id=p_catalogref_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref set "child_id"=p_child_id, "parent_id"=p_parent_id where catalogref_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref(p_catalogref_id integer, p_child_id integer, p_parent_id integer) OWNER TO postgres;

--
-- TOC entry 84 (class 1255 OID 39647)
-- Dependencies: 790 6
-- Name: update_catalogref_documentation(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation(p_catalogref_documentation_id integer, p_value text, p_documentationenum text, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_id into id from catalogref_documentation where catalogref_documentation_id=p_catalogref_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation set "value"=p_value, "catalogref_id"=p_catalogref_id where catalogref_documentation_id=id;
		BEGIN
			update catalogref_documentation set documentationenum = cast(p_documentationenum as documentationenum) where catalogref_documentation_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation set documentationenum_nonstandard = p_documentationenum where catalogref_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_documentation(p_catalogref_documentation_id integer, p_value text, p_documentationenum text, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 85 (class 1255 OID 39648)
-- Dependencies: 6 790
-- Name: update_catalogref_documentation_namespace(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation_namespace(p_catalogref_documentation_namespace_id integer, p_namespace text, p_catalogref_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_namespace_id into id from catalogref_documentation_namespace where catalogref_documentation_namespace_id=p_catalogref_documentation_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation_namespace set "namespace"=p_namespace, "catalogref_documentation_id"=p_catalogref_documentation_id where catalogref_documentation_namespace_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_documentation_namespace(p_catalogref_documentation_namespace_id integer, p_namespace text, p_catalogref_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 86 (class 1255 OID 39649)
-- Dependencies: 790 6
-- Name: update_catalogref_documentation_xlink(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_documentation_xlink(p_catalogref_documentation_xlink_id integer, p_value text, p_xlink text, p_catalogref_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_documentation_xlink_id into id from catalogref_documentation_xlink where catalogref_documentation_xlink_id=p_catalogref_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_documentation_xlink set "value"=p_value, "catalogref_documentation_id"=p_catalogref_documentation_id where catalogref_documentation_xlink_id=id;
		BEGIN
			update catalogref_documentation_xlink set xlink = cast(p_xlink as xlink) where catalogref_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_documentation_xlink set xlink_nonstandard = p_xlink where catalogref_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_documentation_xlink(p_catalogref_documentation_xlink_id integer, p_value text, p_xlink text, p_catalogref_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 87 (class 1255 OID 39650)
-- Dependencies: 6 790
-- Name: update_catalogref_xlink(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_catalogref_xlink(p_catalogref_xlink_id integer, p_value text, p_xlink text, p_catalogref_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select catalogref_xlink_id into id from catalogref_xlink where catalogref_xlink_id=p_catalogref_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update catalogref_xlink set "value"=p_value, "catalogref_id"=p_catalogref_id where catalogref_xlink_id=id;
		BEGIN
			update catalogref_xlink set xlink = cast(p_xlink as xlink) where catalogref_xlink_id=id;
		EXCEPTION
			when others then
				update catalogref_xlink set xlink_nonstandard = p_xlink where catalogref_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_catalogref_xlink(p_catalogref_xlink_id integer, p_value text, p_xlink text, p_catalogref_id integer) OWNER TO postgres;

--
-- TOC entry 88 (class 1255 OID 39651)
-- Dependencies: 6 790
-- Name: update_dataset(integer, text, text, text, text, text, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset(p_dataset_id integer, p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_dataset(p_dataset_id integer, p_harvest text, p_name text, p_alias text, p_authority text, p_d_id text, p_servicename text, p_urlpath text, p_resourcecontrol text, p_collectiontype text, p_status text, p_datatype text, p_datasize_unit text) OWNER TO postgres;

--
-- TOC entry 89 (class 1255 OID 39652)
-- Dependencies: 790 6
-- Name: update_dataset_access(integer, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_access(p_dataset_access_id integer, p_urlpath text, p_servicename text, p_dataformat text, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_id into id from dataset_access where dataset_access_id=p_dataset_access_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access set "urlpath"=p_urlpath, "servicename"=p_servicename, "dataset_id"=p_dataset_id where dataset_access_id=id;
		BEGIN
			update dataset_access set dataformat = cast(p_dataformat as dataformat) where dataset_access_id=id;
		EXCEPTION
			when others then
				update dataset_access set dataformat_nonstandard = p_dataformat where dataset_access_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_dataset_access(p_dataset_access_id integer, p_urlpath text, p_servicename text, p_dataformat text, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 90 (class 1255 OID 39653)
-- Dependencies: 790 6
-- Name: update_dataset_access_datasize(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_access_datasize(p_dataset_access_datasize_id integer, p_value text, p_units text, p_dataset_access_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_access_datasize_id into id from dataset_access_datasize where dataset_access_datasize_id=p_dataset_access_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_access_datasize set "value"=p_value, "dataset_access_id"=p_dataset_access_id where dataset_access_datasize_id=id;
		BEGIN
			update dataset_access_datasize set units = cast(p_units as units) where dataset_access_datasize_id=id;
		EXCEPTION
			when others then
				update dataset_access_datasize set units_nonstandard = p_units where dataset_access_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_dataset_access_datasize(p_dataset_access_datasize_id integer, p_value text, p_units text, p_dataset_access_id integer) OWNER TO postgres;

--
-- TOC entry 91 (class 1255 OID 39654)
-- Dependencies: 790 6
-- Name: update_dataset_ncml(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_ncml(p_dataset_ncml_id integer, p_dataset_id integer) RETURNS integer
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


ALTER FUNCTION public.update_dataset_ncml(p_dataset_ncml_id integer, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 92 (class 1255 OID 39655)
-- Dependencies: 6 790
-- Name: update_dataset_property(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_dataset_property(p_dataset_property_id integer, p_name text, p_value text, p_dataset_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select dataset_property_id into id from dataset_property where dataset_property_id=p_dataset_property_id;
		if(id is null) then
			return -1;
		end if;
		update dataset_property set "name"=p_name, "value"=p_value, "dataset_id"=p_dataset_id where dataset_property_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_dataset_property(p_dataset_property_id integer, p_name text, p_value text, p_dataset_id integer) OWNER TO postgres;

--
-- TOC entry 93 (class 1255 OID 39656)
-- Dependencies: 6 790
-- Name: update_metadata(integer, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata(p_metadata_id integer, p_metadatatype text, p_inherited text) RETURNS integer
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
$$;


ALTER FUNCTION public.update_metadata(p_metadata_id integer, p_metadatatype text, p_inherited text) OWNER TO postgres;

--
-- TOC entry 94 (class 1255 OID 39657)
-- Dependencies: 6 790
-- Name: update_metadata_namespace(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata_namespace(p_metadata_namespace_id integer, p_namespace text, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select metadata_namespace_id into id from metadata_namespace where metadata_namespace_id=p_metadata_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update metadata_namespace set "namespace"=p_namespace, "metadata_id"=p_metadata_id where metadata_namespace_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_metadata_namespace(p_metadata_namespace_id integer, p_namespace text, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 95 (class 1255 OID 39658)
-- Dependencies: 6 790
-- Name: update_metadata_xlink(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_metadata_xlink(p_metadata_xlink_id integer, p_value text, p_xlink text, p_metadata_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select metadata_xlink_id into id from metadata_xlink where metadata_xlink_id=p_metadata_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update metadata_xlink set "value"=p_value, "metadata_id"=p_metadata_id where metadata_xlink_id=id;
		BEGIN
			update metadata_xlink set xlink = cast(p_xlink as xlink) where metadata_xlink_id=id;
		EXCEPTION
			when others then
				update metadata_xlink set xlink_nonstandard = p_xlink where metadata_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_metadata_xlink(p_metadata_xlink_id integer, p_value text, p_xlink text, p_metadata_id integer) OWNER TO postgres;

--
-- TOC entry 97 (class 1255 OID 39659)
-- Dependencies: 790 6
-- Name: update_service(integer, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service(p_service_id integer, p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.update_service(p_service_id integer, p_suffix text, p_name text, p_base text, p_desc text, p_servicetype text, p_status text) OWNER TO postgres;

--
-- TOC entry 98 (class 1255 OID 39660)
-- Dependencies: 790 6
-- Name: update_service_datasetroot(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service_datasetroot(p_service_datasetroot_id integer, p_path text, p_location text, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select service_datasetroot_id into id from service_datasetroot where service_datasetroot_id=p_service_datasetroot_id;
		if(id is null) then
			return -1;
		end if;
		update service_datasetroot set "path"=p_path, "location"=p_location, "service_id"=p_service_id where service_datasetroot_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_service_datasetroot(p_service_datasetroot_id integer, p_path text, p_location text, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 99 (class 1255 OID 39661)
-- Dependencies: 790 6
-- Name: update_service_property(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_service_property(p_service_property_id integer, p_value text, p_name text, p_service_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select service_property_id into id from service_property where service_property_id=p_service_property_id;
		if(id is null) then
			return -1;
		end if;
		update service_property set "value"=p_value, "name"=p_name, "service_id"=p_service_id where service_property_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_service_property(p_service_property_id integer, p_value text, p_name text, p_service_id integer) OWNER TO postgres;

--
-- TOC entry 100 (class 1255 OID 39662)
-- Dependencies: 6 790
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
-- TOC entry 101 (class 1255 OID 39663)
-- Dependencies: 6 790
-- Name: update_tmg_authority(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_authority(p_tmg_authority_id integer, p_authority text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_authority_id into id from tmg_authority where tmg_authority_id=p_tmg_authority_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_authority set "authority"=p_authority, "tmg_id"=p_tmg_id where tmg_authority_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_authority(p_tmg_authority_id integer, p_authority text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 102 (class 1255 OID 39664)
-- Dependencies: 6 790
-- Name: update_tmg_contributor(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_contributor(p_tmg_contributor_id integer, p_role text, p_name text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_contributor_id into id from tmg_contributor where tmg_contributor_id=p_tmg_contributor_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_contributor set "role"=p_role, "name"=p_name, "tmg_id"=p_tmg_id where tmg_contributor_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_contributor(p_tmg_contributor_id integer, p_role text, p_name text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 103 (class 1255 OID 39665)
-- Dependencies: 790 6
-- Name: update_tmg_creator(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator(p_tmg_creator_id integer, p_tmg_id integer) RETURNS integer
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


ALTER FUNCTION public.update_tmg_creator(p_tmg_creator_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 104 (class 1255 OID 39666)
-- Dependencies: 6 790
-- Name: update_tmg_creator_contact(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator_contact(p_tmg_creator_contact_id integer, p_email text, p_url text, p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_creator_contact_id into id from tmg_creator_contact where tmg_creator_contact_id=p_tmg_creator_contact_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_creator_contact set "email"=p_email, "url"=p_url, "tmg_creator_id"=p_tmg_creator_id where tmg_creator_contact_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_creator_contact(p_tmg_creator_contact_id integer, p_email text, p_url text, p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 105 (class 1255 OID 39667)
-- Dependencies: 6 790
-- Name: update_tmg_creator_name(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_creator_name(p_tmg_creator_name_id integer, p_value text, p_vocabulary text, p_tmg_creator_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_creator_name_id into id from tmg_creator_name where tmg_creator_name_id=p_tmg_creator_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_creator_name set "value"=p_value, "vocabulary"=p_vocabulary, "tmg_creator_id"=p_tmg_creator_id where tmg_creator_name_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_creator_name(p_tmg_creator_name_id integer, p_value text, p_vocabulary text, p_tmg_creator_id integer) OWNER TO postgres;

--
-- TOC entry 106 (class 1255 OID 39668)
-- Dependencies: 790 6
-- Name: update_tmg_dataformat(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_dataformat(p_tmg_dataformat_id integer, p_dataformat text, p_tmg_id integer) RETURNS integer
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
			update tmg_dataformat set dataformat = cast(p_dataformat as dataformat) where tmg_dataformat_id=id;
		EXCEPTION
			when others then
				update tmg_dataformat set dataformat_nonstandard = p_dataformat where tmg_dataformat_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_dataformat(p_tmg_dataformat_id integer, p_dataformat text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 107 (class 1255 OID 39669)
-- Dependencies: 790 6
-- Name: update_tmg_datasize(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_datasize(p_tmg_datasize_id integer, p_value text, p_units text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_datasize_id into id from tmg_datasize where tmg_datasize_id=p_tmg_datasize_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_datasize set "value"=p_value, "tmg_id"=p_tmg_id where tmg_datasize_id=id;
		BEGIN
			update tmg_datasize set units = cast(p_units as units) where tmg_datasize_id=id;
		EXCEPTION
			when others then
				update tmg_datasize set units_nonstandard = p_units where tmg_datasize_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_datasize(p_tmg_datasize_id integer, p_value text, p_units text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 108 (class 1255 OID 39670)
-- Dependencies: 790 6
-- Name: update_tmg_datatype(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_datatype(p_tmg_datatype_id integer, p_datatype text, p_tmg_id integer) RETURNS integer
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
			update tmg_datatype set datatype = cast(p_datatype as datatype) where tmg_datatype_id=id;
		EXCEPTION
			when others then
				update tmg_datatype set datatype_nonstandard = p_datatype where tmg_datatype_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_datatype(p_tmg_datatype_id integer, p_datatype text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 110 (class 1255 OID 39671)
-- Dependencies: 790 6
-- Name: update_tmg_date(integer, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_date(p_tmg_date_id integer, p_format text, p_value text, p_dateenum text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_date_id into id from tmg_date where tmg_date_id=p_tmg_date_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_date set "format"=p_format, "value"=p_value, "tmg_id"=p_tmg_id where tmg_date_id=id;
		BEGIN
			update tmg_date set dateenum = cast(p_dateenum as dateenum) where tmg_date_id=id;
		EXCEPTION
			when others then
				update tmg_date set dateenum_nonstandard = p_dateenum where tmg_date_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_date(p_tmg_date_id integer, p_format text, p_value text, p_dateenum text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 111 (class 1255 OID 39672)
-- Dependencies: 790 6
-- Name: update_tmg_documentation(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation(p_tmg_documentation_id integer, p_value text, p_documentationenum text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_id into id from tmg_documentation where tmg_documentation_id=p_tmg_documentation_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation set "value"=p_value, "tmg_id"=p_tmg_id where tmg_documentation_id=id;
		BEGIN
			update tmg_documentation set documentationenum = cast(p_documentationenum as documentationenum) where tmg_documentation_id=id;
		EXCEPTION
			when others then
				update tmg_documentation set documentationenum_nonstandard = p_documentationenum where tmg_documentation_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_documentation(p_tmg_documentation_id integer, p_value text, p_documentationenum text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 112 (class 1255 OID 39673)
-- Dependencies: 790 6
-- Name: update_tmg_documentation_namespace(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation_namespace(p_tmg_documentation_namespace_id integer, p_namespace text, p_tmg_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_namespace_id into id from tmg_documentation_namespace where tmg_documentation_namespace_id=p_tmg_documentation_namespace_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation_namespace set "namespace"=p_namespace, "tmg_documentation_id"=p_tmg_documentation_id where tmg_documentation_namespace_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_documentation_namespace(p_tmg_documentation_namespace_id integer, p_namespace text, p_tmg_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 113 (class 1255 OID 39674)
-- Dependencies: 790 6
-- Name: update_tmg_documentation_xlink(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_documentation_xlink(p_tmg_documentation_xlink_id integer, p_value text, p_xlink text, p_tmg_documentation_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_documentation_xlink_id into id from tmg_documentation_xlink where tmg_documentation_xlink_id=p_tmg_documentation_xlink_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_documentation_xlink set "value"=p_value, "tmg_documentation_id"=p_tmg_documentation_id where tmg_documentation_xlink_id=id;
		BEGIN
			update tmg_documentation_xlink set xlink = cast(p_xlink as xlink) where tmg_documentation_xlink_id=id;
		EXCEPTION
			when others then
				update tmg_documentation_xlink set xlink_nonstandard = p_xlink where tmg_documentation_xlink_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_documentation_xlink(p_tmg_documentation_xlink_id integer, p_value text, p_xlink text, p_tmg_documentation_id integer) OWNER TO postgres;

--
-- TOC entry 114 (class 1255 OID 39675)
-- Dependencies: 6 790
-- Name: update_tmg_geospatialcoverage(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage(p_tmg_geospatialcoverage_id integer, p_upordown text, p_tmg_id integer) RETURNS integer
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
			update tmg_geospatialcoverage set upordown = cast(p_upordown as upordown) where tmg_geospatialcoverage_id=id;
		EXCEPTION
			when others then
				update tmg_geospatialcoverage set upordown_nonstandard = p_upordown where tmg_geospatialcoverage_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage(p_tmg_geospatialcoverage_id integer, p_upordown text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 115 (class 1255 OID 39676)
-- Dependencies: 790 6
-- Name: update_tmg_geospatialcoverage_eastwest(integer, text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_eastwest_id integer, p_size text, p_units text, p_start text, p_resolution text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_eastwest_id into id from tmg_geospatialcoverage_eastwest where tmg_geospatialcoverage_eastwest_id=p_tmg_geospatialcoverage_eastwest_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_eastwest set "size"=p_size, "units"=p_units, "start"=p_start, "resolution"=p_resolution, "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id where tmg_geospatialcoverage_eastwest_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_eastwest(p_tmg_geospatialcoverage_eastwest_id integer, p_size text, p_units text, p_start text, p_resolution text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 116 (class 1255 OID 39677)
-- Dependencies: 790 6
-- Name: update_tmg_geospatialcoverage_name(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_name_id integer, p_vocabulary text, p_value text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_name_id into id from tmg_geospatialcoverage_name where tmg_geospatialcoverage_name_id=p_tmg_geospatialcoverage_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_name set "vocabulary"=p_vocabulary, "value"=p_value, "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id where tmg_geospatialcoverage_name_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_name(p_tmg_geospatialcoverage_name_id integer, p_vocabulary text, p_value text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 117 (class 1255 OID 39678)
-- Dependencies: 790 6
-- Name: update_tmg_geospatialcoverage_northsouth(integer, text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_northsouth_id integer, p_size text, p_resolution text, p_start text, p_units text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_northsouth_id into id from tmg_geospatialcoverage_northsouth where tmg_geospatialcoverage_northsouth_id=p_tmg_geospatialcoverage_northsouth_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_northsouth set "size"=p_size, "resolution"=p_resolution, "start"=p_start, "units"=p_units, "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id where tmg_geospatialcoverage_northsouth_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_northsouth(p_tmg_geospatialcoverage_northsouth_id integer, p_size text, p_resolution text, p_start text, p_units text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 118 (class 1255 OID 39679)
-- Dependencies: 6 790
-- Name: update_tmg_geospatialcoverage_updown(integer, text, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_updown_id integer, p_start text, p_resolution text, p_size text, p_units text, p_tmg_geospatialcoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_geospatialcoverage_updown_id into id from tmg_geospatialcoverage_updown where tmg_geospatialcoverage_updown_id=p_tmg_geospatialcoverage_updown_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_geospatialcoverage_updown set "start"=p_start, "resolution"=p_resolution, "size"=p_size, "units"=p_units, "tmg_geospatialcoverage_id"=p_tmg_geospatialcoverage_id where tmg_geospatialcoverage_updown_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_geospatialcoverage_updown(p_tmg_geospatialcoverage_updown_id integer, p_start text, p_resolution text, p_size text, p_units text, p_tmg_geospatialcoverage_id integer) OWNER TO postgres;

--
-- TOC entry 119 (class 1255 OID 39680)
-- Dependencies: 790 6
-- Name: update_tmg_keyword(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_keyword(p_tmg_keyword_id integer, p_value text, p_vocabulary text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_keyword_id into id from tmg_keyword where tmg_keyword_id=p_tmg_keyword_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_keyword set "value"=p_value, "vocabulary"=p_vocabulary, "tmg_id"=p_tmg_id where tmg_keyword_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_keyword(p_tmg_keyword_id integer, p_value text, p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 75 (class 1255 OID 39681)
-- Dependencies: 790 6
-- Name: update_tmg_project(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_project(p_tmg_project_id integer, p_value text, p_vocabulary text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_project_id into id from tmg_project where tmg_project_id=p_tmg_project_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_project set "value"=p_value, "vocabulary"=p_vocabulary, "tmg_id"=p_tmg_id where tmg_project_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_project(p_tmg_project_id integer, p_value text, p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 96 (class 1255 OID 39682)
-- Dependencies: 790 6
-- Name: update_tmg_property(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_property(p_tmg_property_id integer, p_name text, p_value text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_property_id into id from tmg_property where tmg_property_id=p_tmg_property_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_property set "name"=p_name, "value"=p_value, "tmg_id"=p_tmg_id where tmg_property_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_property(p_tmg_property_id integer, p_name text, p_value text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 109 (class 1255 OID 39683)
-- Dependencies: 6 790
-- Name: update_tmg_publisher(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher(p_tmg_publisher_id integer, p_tmg_id integer) RETURNS integer
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


ALTER FUNCTION public.update_tmg_publisher(p_tmg_publisher_id integer, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 120 (class 1255 OID 39684)
-- Dependencies: 790 6
-- Name: update_tmg_publisher_contact(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher_contact(p_tmg_publisher_contact_id integer, p_url text, p_email text, p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_contact_id into id from tmg_publisher_contact where tmg_publisher_contact_id=p_tmg_publisher_contact_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher_contact set "url"=p_url, "email"=p_email, "tmg_publisher_id"=p_tmg_publisher_id where tmg_publisher_contact_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_publisher_contact(p_tmg_publisher_contact_id integer, p_url text, p_email text, p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 121 (class 1255 OID 39685)
-- Dependencies: 6 790
-- Name: update_tmg_publisher_name(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_publisher_name(p_tmg_publisher_name_id integer, p_value text, p_vocabulary text, p_tmg_publisher_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_publisher_name_id into id from tmg_publisher_name where tmg_publisher_name_id=p_tmg_publisher_name_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_publisher_name set "value"=p_value, "vocabulary"=p_vocabulary, "tmg_publisher_id"=p_tmg_publisher_id where tmg_publisher_name_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_publisher_name(p_tmg_publisher_name_id integer, p_value text, p_vocabulary text, p_tmg_publisher_id integer) OWNER TO postgres;

--
-- TOC entry 122 (class 1255 OID 39686)
-- Dependencies: 790 6
-- Name: update_tmg_servicename(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_servicename(p_tmg_servicename_id integer, p_servicename text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_servicename_id into id from tmg_servicename where tmg_servicename_id=p_tmg_servicename_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_servicename set "servicename"=p_servicename, "tmg_id"=p_tmg_id where tmg_servicename_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_servicename(p_tmg_servicename_id integer, p_servicename text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 123 (class 1255 OID 39687)
-- Dependencies: 790 6
-- Name: update_tmg_timecoverage(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage(p_tmg_timecoverage_id integer, p_resolution text, p_tmg_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_id into id from tmg_timecoverage where tmg_timecoverage_id=p_tmg_timecoverage_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage set "resolution"=p_resolution, "tmg_id"=p_tmg_id where tmg_timecoverage_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage(p_tmg_timecoverage_id integer, p_resolution text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 124 (class 1255 OID 39688)
-- Dependencies: 790 6
-- Name: update_tmg_timecoverage_duration(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_duration(p_tmg_timecoverage_duration_id integer, p_duration text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_duration_id into id from tmg_timecoverage_duration where tmg_timecoverage_duration_id=p_tmg_timecoverage_duration_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_duration set "duration"=p_duration, "tmg_timecoverage_id"=p_tmg_timecoverage_id where tmg_timecoverage_duration_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_duration(p_tmg_timecoverage_duration_id integer, p_duration text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 125 (class 1255 OID 39689)
-- Dependencies: 790 6
-- Name: update_tmg_timecoverage_end(integer, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_end(p_tmg_timecoverage_end_id integer, p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_end_id into id from tmg_timecoverage_end where tmg_timecoverage_end_id=p_tmg_timecoverage_end_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_end set "format"=p_format, "value"=p_value, "tmg_timecoverage_id"=p_tmg_timecoverage_id where tmg_timecoverage_end_id=id;
		BEGIN
			update tmg_timecoverage_end set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_end_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_end set dateenum_nonstandard = p_dateenum where tmg_timecoverage_end_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_end(p_tmg_timecoverage_end_id integer, p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 126 (class 1255 OID 39690)
-- Dependencies: 6 790
-- Name: update_tmg_timecoverage_resolution(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_resolution(p_tmg_timecoverage_resolution_id integer, p_duration text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_resolution_id into id from tmg_timecoverage_resolution where tmg_timecoverage_resolution_id=p_tmg_timecoverage_resolution_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_resolution set "duration"=p_duration, "tmg_timecoverage_id"=p_tmg_timecoverage_id where tmg_timecoverage_resolution_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_resolution(p_tmg_timecoverage_resolution_id integer, p_duration text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 127 (class 1255 OID 39691)
-- Dependencies: 790 6
-- Name: update_tmg_timecoverage_start(integer, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_timecoverage_start(p_tmg_timecoverage_start_id integer, p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_timecoverage_start_id into id from tmg_timecoverage_start where tmg_timecoverage_start_id=p_tmg_timecoverage_start_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_timecoverage_start set "format"=p_format, "value"=p_value, "tmg_timecoverage_id"=p_tmg_timecoverage_id where tmg_timecoverage_start_id=id;
		BEGIN
			update tmg_timecoverage_start set dateenum = cast(p_dateenum as dateenum) where tmg_timecoverage_start_id=id;
		EXCEPTION
			when others then
				update tmg_timecoverage_start set dateenum_nonstandard = p_dateenum where tmg_timecoverage_start_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_timecoverage_start(p_tmg_timecoverage_start_id integer, p_format text, p_value text, p_dateenum text, p_tmg_timecoverage_id integer) OWNER TO postgres;

--
-- TOC entry 128 (class 1255 OID 39692)
-- Dependencies: 6 790
-- Name: update_tmg_variables(integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables(p_tmg_variables_id integer, p_vocabulary text, p_tmg_id integer) RETURNS integer
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
			update tmg_variables set vocabulary = cast(p_vocabulary as vocabulary) where tmg_variables_id=id;
		EXCEPTION
			when others then
				update tmg_variables set vocabulary_nonstandard = p_vocabulary where tmg_variables_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables(p_tmg_variables_id integer, p_vocabulary text, p_tmg_id integer) OWNER TO postgres;

--
-- TOC entry 129 (class 1255 OID 39693)
-- Dependencies: 790 6
-- Name: update_tmg_variables_variable(integer, text, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables_variable(p_tmg_variables_variable_id integer, p_units text, p_name text, p_vocabulary_name text, p_tmg_variables_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variable_id into id from tmg_variables_variable where tmg_variables_variable_id=p_tmg_variables_variable_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variable set "units"=p_units, "name"=p_name, "vocabulary_name"=p_vocabulary_name, "tmg_variables_id"=p_tmg_variables_id where tmg_variables_variable_id=id;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables_variable(p_tmg_variables_variable_id integer, p_units text, p_name text, p_vocabulary_name text, p_tmg_variables_id integer) OWNER TO postgres;

--
-- TOC entry 130 (class 1255 OID 39694)
-- Dependencies: 6 790
-- Name: update_tmg_variables_variablemap(integer, text, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_tmg_variables_variablemap(p_tmg_variables_variablemap_id integer, p_value text, p_xlink text, p_tmg_variables_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	id int;
BEGIN
		select tmg_variables_variablemap_id into id from tmg_variables_variablemap where tmg_variables_variablemap_id=p_tmg_variables_variablemap_id;
		if(id is null) then
			return -1;
		end if;
		update tmg_variables_variablemap set "value"=p_value, "tmg_variables_id"=p_tmg_variables_id where tmg_variables_variablemap_id=id;
		BEGIN
			update tmg_variables_variablemap set xlink = cast(p_xlink as xlink) where tmg_variables_variablemap_id=id;
		EXCEPTION
			when others then
				update tmg_variables_variablemap set xlink_nonstandard = p_xlink where tmg_variables_variablemap_id=id;
		END;

		return id;
END;
$$;


ALTER FUNCTION public.update_tmg_variables_variablemap(p_tmg_variables_variablemap_id integer, p_value text, p_xlink text, p_tmg_variables_id integer) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1982 (class 1259 OID 37808)
-- Dependencies: 6 495
-- Name: catalog; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog (
    catalog_id integer NOT NULL,
    xmlns text,
    name text,
    status status,
    base text,
    version text,
    expires text
);


ALTER TABLE public.catalog OWNER TO cleaner;

--
-- TOC entry 2629 (class 0 OID 0)
-- Dependencies: 1982
-- Name: COLUMN catalog.base; Type: COMMENT; Schema: public; Owner: cleaner
--

COMMENT ON COLUMN catalog.base IS 'deprecated: not working, don''t use (as of 1.0.2)';


--
-- TOC entry 1983 (class 1259 OID 37814)
-- Dependencies: 1982 6
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
-- TOC entry 2630 (class 0 OID 0)
-- Dependencies: 1983
-- Name: catalog_catalog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_catalog_id_seq OWNED BY catalog.catalog_id;


--
-- TOC entry 1984 (class 1259 OID 37816)
-- Dependencies: 6
-- Name: catalog_dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_dataset (
    catalog_id integer NOT NULL,
    dataset_id integer NOT NULL
);


ALTER TABLE public.catalog_dataset OWNER TO cleaner;

--
-- TOC entry 1985 (class 1259 OID 37819)
-- Dependencies: 6
-- Name: template_property; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_property (
    name text,
    value text
);


ALTER TABLE public.template_property OWNER TO postgres;

--
-- TOC entry 2631 (class 0 OID 0)
-- Dependencies: 1985
-- Name: TABLE template_property; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_property IS 'abstract template only for service_property, dataset_property, or catalog_property';


--
-- TOC entry 1986 (class 1259 OID 37825)
-- Dependencies: 1985 6
-- Name: catalog_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_property (
    catalog_id integer,
    catalog_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.catalog_property OWNER TO cleaner;

--
-- TOC entry 1987 (class 1259 OID 37831)
-- Dependencies: 6 1986
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
-- TOC entry 2632 (class 0 OID 0)
-- Dependencies: 1987
-- Name: catalog_property_catalog_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_property_catalog_property_id_seq OWNED BY catalog_property.catalog_property_id;


--
-- TOC entry 1988 (class 1259 OID 37833)
-- Dependencies: 6
-- Name: catalog_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_service (
    catalog_id integer NOT NULL,
    service_id integer NOT NULL
);


ALTER TABLE public.catalog_service OWNER TO cleaner;

--
-- TOC entry 1989 (class 1259 OID 37836)
-- Dependencies: 6 503
-- Name: template_xlink; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_xlink (
    value text,
    xlink xlink,
    xlink_nonstandard text
);


ALTER TABLE public.template_xlink OWNER TO postgres;

--
-- TOC entry 1990 (class 1259 OID 37842)
-- Dependencies: 1989 503 6
-- Name: catalog_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalog_xlink (
    catalog_xlink_id integer NOT NULL,
    catalog_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalog_xlink OWNER TO cleaner;

--
-- TOC entry 1991 (class 1259 OID 37848)
-- Dependencies: 1990 6
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
-- TOC entry 2633 (class 0 OID 0)
-- Dependencies: 1991
-- Name: catalog_xlink_catalog_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalog_xlink_catalog_xlink_id_seq OWNED BY catalog_xlink.catalog_xlink_id;


--
-- TOC entry 1992 (class 1259 OID 37850)
-- Dependencies: 6
-- Name: catalogref; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref (
    parent_id integer NOT NULL,
    child_id integer NOT NULL,
    catalogref_id integer NOT NULL
);


ALTER TABLE public.catalogref OWNER TO cleaner;

--
-- TOC entry 1993 (class 1259 OID 37853)
-- Dependencies: 1992 6
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
-- TOC entry 2634 (class 0 OID 0)
-- Dependencies: 1993
-- Name: catalogref_catalogref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_catalogref_id_seq OWNED BY catalogref.catalogref_id;


--
-- TOC entry 1994 (class 1259 OID 37855)
-- Dependencies: 6 489
-- Name: template_documentation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_documentation (
    documentationenum documentationenum,
    documentationenum_nonstandard text,
    value text
);


ALTER TABLE public.template_documentation OWNER TO postgres;

--
-- TOC entry 2635 (class 0 OID 0)
-- Dependencies: 1994
-- Name: TABLE template_documentation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_documentation IS 'Be sure to use xx_documentation_namespace and -xlink when using this table.';


--
-- TOC entry 1995 (class 1259 OID 37861)
-- Dependencies: 1994 489 6
-- Name: catalogref_documentation; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation (
    catalogref_documentation_id integer NOT NULL,
    catalogref_id integer
)
INHERITS (template_documentation);


ALTER TABLE public.catalogref_documentation OWNER TO cleaner;

--
-- TOC entry 1996 (class 1259 OID 37867)
-- Dependencies: 1995 6
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
-- TOC entry 2636 (class 0 OID 0)
-- Dependencies: 1996
-- Name: catalogref_documentation_catalogref_documentation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_catalogref_documentation_id_seq OWNED BY catalogref_documentation.catalogref_documentation_id;


--
-- TOC entry 1997 (class 1259 OID 37869)
-- Dependencies: 6
-- Name: template_namespace; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_namespace (
    namespace text
);


ALTER TABLE public.template_namespace OWNER TO postgres;

--
-- TOC entry 1998 (class 1259 OID 37875)
-- Dependencies: 6 1997
-- Name: catalogref_documentation_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation_namespace (
    catalogref_documentation_namespace_id integer NOT NULL,
    catalogref_documentation_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.catalogref_documentation_namespace OWNER TO cleaner;

--
-- TOC entry 1999 (class 1259 OID 37881)
-- Dependencies: 6 1998
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
-- TOC entry 2637 (class 0 OID 0)
-- Dependencies: 1999
-- Name: catalogref_documentation_name_catalogref_documentation_name_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_name_catalogref_documentation_name_seq OWNED BY catalogref_documentation_namespace.catalogref_documentation_namespace_id;


--
-- TOC entry 2000 (class 1259 OID 37883)
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
-- TOC entry 2001 (class 1259 OID 37885)
-- Dependencies: 503 1989 6
-- Name: catalogref_documentation_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_documentation_xlink (
    catalogref_documentation_xlink_id integer NOT NULL,
    catalogref_documentation_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalogref_documentation_xlink OWNER TO cleaner;

--
-- TOC entry 2002 (class 1259 OID 37891)
-- Dependencies: 6 2001
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
-- TOC entry 2638 (class 0 OID 0)
-- Dependencies: 2002
-- Name: catalogref_documentation_xlink_catalogref_documentation_xlink_s; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_documentation_xlink_catalogref_documentation_xlink_s OWNED BY catalogref_documentation_xlink.catalogref_documentation_xlink_id;


--
-- TOC entry 2003 (class 1259 OID 37893)
-- Dependencies: 6 503 1989
-- Name: catalogref_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE catalogref_xlink (
    catalogref_xlink_id integer NOT NULL,
    catalogref_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.catalogref_xlink OWNER TO cleaner;

--
-- TOC entry 2004 (class 1259 OID 37899)
-- Dependencies: 6 2003
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
-- TOC entry 2639 (class 0 OID 0)
-- Dependencies: 2004
-- Name: catalogref_xlink_catalogref_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE catalogref_xlink_catalogref_xlink_id_seq OWNED BY catalogref_xlink.catalogref_xlink_id;


--
-- TOC entry 2005 (class 1259 OID 37925)
-- Dependencies: 6 495 479 483 485
-- Name: dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset (
    dataset_id integer NOT NULL,
    name text,
    alias text,
    authority text,
    d_id text,
    servicename text,
    urlpath text,
    status status,
    collectiontype_nonstandard text,
    datatype_nonstandard text,
    collectiontype collectiontype,
    datasize_unit datasize_unit,
    datatype datatype,
    resourcecontrol text,
    harvest text,
    datasize_unit_nonstandard text
);


ALTER TABLE public.dataset OWNER TO cleaner;

--
-- TOC entry 2006 (class 1259 OID 37931)
-- Dependencies: 6 481
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
-- TOC entry 2640 (class 0 OID 0)
-- Dependencies: 2006
-- Name: TABLE template_access; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_access IS 'Be sure to use xx_access_datasize as well.';


--
-- TOC entry 2007 (class 1259 OID 37937)
-- Dependencies: 6 481 2006
-- Name: dataset_access; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_access (
    dataset_access_id integer NOT NULL,
    dataset_id integer
)
INHERITS (template_access);


ALTER TABLE public.dataset_access OWNER TO cleaner;

--
-- TOC entry 2008 (class 1259 OID 37943)
-- Dependencies: 2007 6
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
-- TOC entry 2641 (class 0 OID 0)
-- Dependencies: 2008
-- Name: dataset_access_dataset_access_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_access_dataset_access_id_seq OWNED BY dataset_access.dataset_access_id;


--
-- TOC entry 2009 (class 1259 OID 37945)
-- Dependencies: 497 6
-- Name: template_datasize; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_datasize (
    units units,
    units_nonstandard text,
    value text
);


ALTER TABLE public.template_datasize OWNER TO postgres;

--
-- TOC entry 2010 (class 1259 OID 37951)
-- Dependencies: 2009 6 497
-- Name: dataset_access_datasize; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_access_datasize (
    dataset_access_datasize_id integer NOT NULL,
    dataset_access_id integer
)
INHERITS (template_datasize);


ALTER TABLE public.dataset_access_datasize OWNER TO cleaner;

--
-- TOC entry 2011 (class 1259 OID 37957)
-- Dependencies: 2010 6
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
-- TOC entry 2642 (class 0 OID 0)
-- Dependencies: 2011
-- Name: dataset_access_datasize_dataset_access_datasize_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_access_datasize_dataset_access_datasize_id_seq OWNED BY dataset_access_datasize.dataset_access_datasize_id;


--
-- TOC entry 2012 (class 1259 OID 37959)
-- Dependencies: 6
-- Name: dataset_catalogref; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_catalogref (
    dataset_id integer NOT NULL,
    catalogref_id integer NOT NULL
);


ALTER TABLE public.dataset_catalogref OWNER TO cleaner;

--
-- TOC entry 2013 (class 1259 OID 37962)
-- Dependencies: 6
-- Name: dataset_dataset; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_dataset (
    parent_id integer NOT NULL,
    child_id integer NOT NULL
);


ALTER TABLE public.dataset_dataset OWNER TO cleaner;

--
-- TOC entry 2014 (class 1259 OID 37965)
-- Dependencies: 2005 6
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
-- TOC entry 2643 (class 0 OID 0)
-- Dependencies: 2014
-- Name: dataset_dataset_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_dataset_id_seq OWNED BY dataset.dataset_id;


--
-- TOC entry 2015 (class 1259 OID 37967)
-- Dependencies: 6
-- Name: dataset_ncml; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_ncml (
    dataset_ncml_id integer NOT NULL,
    dataset_id integer
);


ALTER TABLE public.dataset_ncml OWNER TO cleaner;

--
-- TOC entry 2016 (class 1259 OID 37970)
-- Dependencies: 6 2015
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
-- TOC entry 2644 (class 0 OID 0)
-- Dependencies: 2016
-- Name: dataset_ncml_dataset_ncml_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_ncml_dataset_ncml_id_seq OWNED BY dataset_ncml.dataset_ncml_id;


--
-- TOC entry 2017 (class 1259 OID 37972)
-- Dependencies: 6 1985
-- Name: dataset_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_property (
    dataset_id integer,
    dataset_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.dataset_property OWNER TO cleaner;

--
-- TOC entry 2018 (class 1259 OID 37978)
-- Dependencies: 2017 6
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
-- TOC entry 2645 (class 0 OID 0)
-- Dependencies: 2018
-- Name: dataset_property_dataset_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE dataset_property_dataset_property_id_seq OWNED BY dataset_property.dataset_property_id;


--
-- TOC entry 2019 (class 1259 OID 37980)
-- Dependencies: 6
-- Name: dataset_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_service (
    dataset_id integer NOT NULL,
    service_id integer NOT NULL
);


ALTER TABLE public.dataset_service OWNER TO cleaner;

--
-- TOC entry 2646 (class 0 OID 0)
-- Dependencies: 2019
-- Name: TABLE dataset_service; Type: COMMENT; Schema: public; Owner: cleaner
--

COMMENT ON TABLE dataset_service IS 'Depreciated in 1.0.';


--
-- TOC entry 2020 (class 1259 OID 37983)
-- Dependencies: 6
-- Name: dataset_tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE dataset_tmg (
    dataset_id integer NOT NULL,
    tmg_id integer NOT NULL
);


ALTER TABLE public.dataset_tmg OWNER TO cleaner;

--
-- TOC entry 2021 (class 1259 OID 37986)
-- Dependencies: 2408 787 491 6
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
-- TOC entry 2022 (class 1259 OID 37992)
-- Dependencies: 6 2021
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
-- TOC entry 2647 (class 0 OID 0)
-- Dependencies: 2022
-- Name: metadata_metadata_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_metadata_id_seq OWNED BY metadata.metadata_id;


--
-- TOC entry 2023 (class 1259 OID 37994)
-- Dependencies: 6 1997
-- Name: metadata_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_namespace (
    metadata_namespace_id integer NOT NULL,
    metadata_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.metadata_namespace OWNER TO cleaner;

--
-- TOC entry 2024 (class 1259 OID 38000)
-- Dependencies: 6 2023
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
-- TOC entry 2648 (class 0 OID 0)
-- Dependencies: 2024
-- Name: metadata_namespace_metadata_namespace_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_namespace_metadata_namespace_id_seq OWNED BY metadata_namespace.metadata_namespace_id;


--
-- TOC entry 2025 (class 1259 OID 38002)
-- Dependencies: 6
-- Name: metadata_tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_tmg (
    metadata_id integer NOT NULL,
    tmg_id integer NOT NULL
);


ALTER TABLE public.metadata_tmg OWNER TO cleaner;

--
-- TOC entry 2026 (class 1259 OID 38005)
-- Dependencies: 6 503 1989
-- Name: metadata_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE metadata_xlink (
    metadata_xlink_id integer NOT NULL,
    metadata_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.metadata_xlink OWNER TO cleaner;

--
-- TOC entry 2027 (class 1259 OID 38011)
-- Dependencies: 6 2026
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
-- TOC entry 2649 (class 0 OID 0)
-- Dependencies: 2027
-- Name: metadata_xlink_metadata_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE metadata_xlink_metadata_xlink_id_seq OWNED BY metadata_xlink.metadata_xlink_id;


--
-- TOC entry 2028 (class 1259 OID 38013)
-- Dependencies: 493 495 6
-- Name: service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service (
    service_id integer NOT NULL,
    name text,
    base text,
    suffix text,
    "desc" text,
    servicetype_nonstandard text,
    status status,
    servicetype servicetype
);


ALTER TABLE public.service OWNER TO cleaner;

--
-- TOC entry 2029 (class 1259 OID 38019)
-- Dependencies: 6
-- Name: template_datasetroot; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_datasetroot (
    path text,
    location text
);


ALTER TABLE public.template_datasetroot OWNER TO postgres;

--
-- TOC entry 2030 (class 1259 OID 38025)
-- Dependencies: 6 2029
-- Name: service_datasetroot; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_datasetroot (
    service_datasetroot_id integer NOT NULL,
    service_id integer
)
INHERITS (template_datasetroot);


ALTER TABLE public.service_datasetroot OWNER TO cleaner;

--
-- TOC entry 2031 (class 1259 OID 38031)
-- Dependencies: 6 2030
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
-- TOC entry 2650 (class 0 OID 0)
-- Dependencies: 2031
-- Name: service_datasetroot_service_datasetroot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_datasetroot_service_datasetroot_id_seq OWNED BY service_datasetroot.service_datasetroot_id;


--
-- TOC entry 2032 (class 1259 OID 38033)
-- Dependencies: 1985 6
-- Name: service_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_property (
    service_id integer,
    service_property_id integer NOT NULL
)
INHERITS (template_property);


ALTER TABLE public.service_property OWNER TO cleaner;

--
-- TOC entry 2033 (class 1259 OID 38039)
-- Dependencies: 6 2032
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
-- TOC entry 2651 (class 0 OID 0)
-- Dependencies: 2033
-- Name: service_property_service_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_property_service_property_id_seq OWNED BY service_property.service_property_id;


--
-- TOC entry 2034 (class 1259 OID 38041)
-- Dependencies: 6
-- Name: service_service; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE service_service (
    parent_id integer NOT NULL,
    child_id integer NOT NULL
);


ALTER TABLE public.service_service OWNER TO cleaner;

--
-- TOC entry 2035 (class 1259 OID 38044)
-- Dependencies: 2028 6
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
-- TOC entry 2652 (class 0 OID 0)
-- Dependencies: 2035
-- Name: service_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE service_service_id_seq OWNED BY service.service_id;


--
-- TOC entry 2036 (class 1259 OID 38046)
-- Dependencies: 6
-- Name: template_contact; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_contact (
    url text,
    email text
);


ALTER TABLE public.template_contact OWNER TO postgres;

--
-- TOC entry 2037 (class 1259 OID 38052)
-- Dependencies: 6
-- Name: template_contributor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_contributor (
    name text,
    role text
);


ALTER TABLE public.template_contributor OWNER TO postgres;

--
-- TOC entry 2038 (class 1259 OID 38058)
-- Dependencies: 6
-- Name: template_controlledvocabulary; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_controlledvocabulary (
    vocabulary text,
    value text
);


ALTER TABLE public.template_controlledvocabulary OWNER TO postgres;

--
-- TOC entry 2039 (class 1259 OID 38064)
-- Dependencies: 6 487
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
-- TOC entry 2040 (class 1259 OID 38070)
-- Dependencies: 6
-- Name: template_duration; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_duration (
    duration text
);


ALTER TABLE public.template_duration OWNER TO postgres;

--
-- TOC entry 2653 (class 0 OID 0)
-- Dependencies: 2040
-- Name: TABLE template_duration; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_duration IS 'TODO: add check constraints to limit the input';


--
-- TOC entry 2041 (class 1259 OID 38076)
-- Dependencies: 2414 6 499
-- Name: template_geospatialcoverage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_geospatialcoverage (
    upordown upordown DEFAULT 'up'::upordown,
    upordown_nonstandard text
);


ALTER TABLE public.template_geospatialcoverage OWNER TO postgres;

--
-- TOC entry 2654 (class 0 OID 0)
-- Dependencies: 2041
-- Name: TABLE template_geospatialcoverage; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_geospatialcoverage IS 'Be sure to include xx_geospatialcoverage_northsouth, -eastwest, and -updown (all of type spatialrange) as well as -name (of type controlledvocabulary) whenever you include this one.';


--
-- TOC entry 2655 (class 0 OID 0)
-- Dependencies: 2041
-- Name: COLUMN template_geospatialcoverage.upordown; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN template_geospatialcoverage.upordown IS 'zpositive';


--
-- TOC entry 2042 (class 1259 OID 38083)
-- Dependencies: 6
-- Name: template_ncml; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_ncml (
    ncml text
);


ALTER TABLE public.template_ncml OWNER TO postgres;

--
-- TOC entry 2043 (class 1259 OID 38089)
-- Dependencies: 6
-- Name: template_sourcetype; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_sourcetype (
);


ALTER TABLE public.template_sourcetype OWNER TO postgres;

--
-- TOC entry 2656 (class 0 OID 0)
-- Dependencies: 2043
-- Name: TABLE template_sourcetype; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_sourcetype IS 'empty. Be sure to include a xx_sourcetype_name and xx_sourcetype_contact when using this table.';


--
-- TOC entry 2044 (class 1259 OID 38092)
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
-- TOC entry 2045 (class 1259 OID 38098)
-- Dependencies: 6
-- Name: template_timecoverage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_timecoverage (
    resolution text
);


ALTER TABLE public.template_timecoverage OWNER TO postgres;

--
-- TOC entry 2657 (class 0 OID 0)
-- Dependencies: 2045
-- Name: TABLE template_timecoverage; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_timecoverage IS 'Be sure to use xx_timecoverage_start, -end, and -duration whenever using this table.';


--
-- TOC entry 2658 (class 0 OID 0)
-- Dependencies: 2045
-- Name: COLUMN template_timecoverage.resolution; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN template_timecoverage.resolution IS 'This is actually supposed to be of type Duration, but the schema doesn''t like it when I try to inherit from Duration, since it expects a ''duration'' field, and this is simpler than create a new subtable.';


--
-- TOC entry 2046 (class 1259 OID 38104)
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
-- TOC entry 2047 (class 1259 OID 38110)
-- Dependencies: 501 6
-- Name: template_variables; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE template_variables (
    vocabulary vocabulary,
    vocabulary_nonstandard text
);


ALTER TABLE public.template_variables OWNER TO postgres;

--
-- TOC entry 2659 (class 0 OID 0)
-- Dependencies: 2047
-- Name: TABLE template_variables; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE template_variables IS 'Be sure to use xx_variables_variable, -variablemap, and -xlink whenever using this table.';


--
-- TOC entry 2048 (class 1259 OID 38116)
-- Dependencies: 2416 6
-- Name: tmg; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg (
    tmg_id integer NOT NULL,
    not_empty text DEFAULT 'true'::text
);


ALTER TABLE public.tmg OWNER TO cleaner;

--
-- TOC entry 2049 (class 1259 OID 38123)
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
-- TOC entry 2050 (class 1259 OID 38129)
-- Dependencies: 6 2049
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
-- TOC entry 2660 (class 0 OID 0)
-- Dependencies: 2050
-- Name: tmg_authority_tmg_authority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_authority_tmg_authority_id_seq OWNED BY tmg_authority.tmg_authority_id;


--
-- TOC entry 2051 (class 1259 OID 38131)
-- Dependencies: 2037 6
-- Name: tmg_contributor; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_contributor (
    tmg_contributor_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_contributor);


ALTER TABLE public.tmg_contributor OWNER TO cleaner;

--
-- TOC entry 2052 (class 1259 OID 38137)
-- Dependencies: 6 2051
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
-- TOC entry 2661 (class 0 OID 0)
-- Dependencies: 2052
-- Name: tmg_contributor_tmg_contributor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_contributor_tmg_contributor_id_seq OWNED BY tmg_contributor.tmg_contributor_id;


--
-- TOC entry 2053 (class 1259 OID 38139)
-- Dependencies: 6 2043
-- Name: tmg_creator; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator (
    tmg_creator_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_sourcetype);


ALTER TABLE public.tmg_creator OWNER TO cleaner;

--
-- TOC entry 2054 (class 1259 OID 38142)
-- Dependencies: 6 2036
-- Name: tmg_creator_contact; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator_contact (
    tmg_creator_contact_id integer NOT NULL,
    tmg_creator_id integer
)
INHERITS (template_contact);


ALTER TABLE public.tmg_creator_contact OWNER TO cleaner;

--
-- TOC entry 2055 (class 1259 OID 38148)
-- Dependencies: 6 2054
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
-- TOC entry 2662 (class 0 OID 0)
-- Dependencies: 2055
-- Name: tmg_creator_contact_tmg_creator_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_contact_tmg_creator_contact_id_seq OWNED BY tmg_creator_contact.tmg_creator_contact_id;


--
-- TOC entry 2056 (class 1259 OID 38150)
-- Dependencies: 6 2038
-- Name: tmg_creator_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_creator_name (
    tmg_creator_name_id integer NOT NULL,
    tmg_creator_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_creator_name OWNER TO cleaner;

--
-- TOC entry 2057 (class 1259 OID 38156)
-- Dependencies: 6 2056
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
-- TOC entry 2663 (class 0 OID 0)
-- Dependencies: 2057
-- Name: tmg_creator_name_tmg_creator_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_name_tmg_creator_name_id_seq OWNED BY tmg_creator_name.tmg_creator_name_id;


--
-- TOC entry 2058 (class 1259 OID 38158)
-- Dependencies: 6 2053
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
-- TOC entry 2664 (class 0 OID 0)
-- Dependencies: 2058
-- Name: tmg_creator_tmg_creator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_creator_tmg_creator_id_seq OWNED BY tmg_creator.tmg_creator_id;


--
-- TOC entry 2059 (class 1259 OID 38160)
-- Dependencies: 481 6
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
-- TOC entry 2060 (class 1259 OID 38166)
-- Dependencies: 6 2059
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
-- TOC entry 2665 (class 0 OID 0)
-- Dependencies: 2060
-- Name: tmg_dataformat_tmg_dataformat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_dataformat_tmg_dataformat_id_seq OWNED BY tmg_dataformat.tmg_dataformat_id;


--
-- TOC entry 2061 (class 1259 OID 38168)
-- Dependencies: 6 497 2009
-- Name: tmg_datasize; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_datasize (
    tmg_datasize_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_datasize);


ALTER TABLE public.tmg_datasize OWNER TO cleaner;

--
-- TOC entry 2062 (class 1259 OID 38174)
-- Dependencies: 2061 6
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
-- TOC entry 2666 (class 0 OID 0)
-- Dependencies: 2062
-- Name: tmg_datasize_tmg_datasize_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_datasize_tmg_datasize_id_seq OWNED BY tmg_datasize.tmg_datasize_id;


--
-- TOC entry 2063 (class 1259 OID 38176)
-- Dependencies: 6 485
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
-- TOC entry 2064 (class 1259 OID 38182)
-- Dependencies: 2063 6
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
-- TOC entry 2667 (class 0 OID 0)
-- Dependencies: 2064
-- Name: tmg_datatype_tmg_datatype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_datatype_tmg_datatype_id_seq OWNED BY tmg_datatype.tmg_datatype_id;


--
-- TOC entry 2065 (class 1259 OID 38184)
-- Dependencies: 487 2039 6
-- Name: tmg_date; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_date (
    tmg_id integer,
    tmg_date_id integer NOT NULL
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_date OWNER TO cleaner;

--
-- TOC entry 2066 (class 1259 OID 38190)
-- Dependencies: 6 2065
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
-- TOC entry 2668 (class 0 OID 0)
-- Dependencies: 2066
-- Name: tmg_date_tmg_date_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_date_tmg_date_id_seq OWNED BY tmg_date.tmg_date_id;


--
-- TOC entry 2067 (class 1259 OID 38192)
-- Dependencies: 1994 6 489
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
-- TOC entry 2068 (class 1259 OID 38198)
-- Dependencies: 1997 6
-- Name: tmg_documentation_namespace; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_documentation_namespace (
    tmg_documentation_namespace_id integer NOT NULL,
    tmg_documentation_id integer
)
INHERITS (template_namespace);


ALTER TABLE public.tmg_documentation_namespace OWNER TO cleaner;

--
-- TOC entry 2069 (class 1259 OID 38204)
-- Dependencies: 6 2068
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
-- TOC entry 2669 (class 0 OID 0)
-- Dependencies: 2069
-- Name: tmg_documentation_namespace_tmg_documentation_namespace_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_namespace_tmg_documentation_namespace_id_seq OWNED BY tmg_documentation_namespace.tmg_documentation_namespace_id;


--
-- TOC entry 2070 (class 1259 OID 38206)
-- Dependencies: 6 2067
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
-- TOC entry 2670 (class 0 OID 0)
-- Dependencies: 2070
-- Name: tmg_documentation_tmg_documentation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_tmg_documentation_id_seq OWNED BY tmg_documentation.tmg_documentation_id;


--
-- TOC entry 2071 (class 1259 OID 38208)
-- Dependencies: 503 6 1989
-- Name: tmg_documentation_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_documentation_xlink (
    tmg_documentation_xlink_id integer NOT NULL,
    tmg_documentation_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_documentation_xlink OWNER TO cleaner;

--
-- TOC entry 2072 (class 1259 OID 38214)
-- Dependencies: 6 2071
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
-- TOC entry 2671 (class 0 OID 0)
-- Dependencies: 2072
-- Name: tmg_documentation_xlink_tmg_documentation_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_documentation_xlink_tmg_documentation_xlink_id_seq OWNED BY tmg_documentation_xlink.tmg_documentation_xlink_id;


--
-- TOC entry 2073 (class 1259 OID 38216)
-- Dependencies: 2429 6 499 2041
-- Name: tmg_geospatialcoverage; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage (
    tmg_geospatialcoverage_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_geospatialcoverage);


ALTER TABLE public.tmg_geospatialcoverage OWNER TO cleaner;

--
-- TOC entry 2074 (class 1259 OID 38223)
-- Dependencies: 6 2044
-- Name: tmg_geospatialcoverage_eastwest; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_eastwest (
    tmg_geospatialcoverage_eastwest_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_eastwest OWNER TO cleaner;

--
-- TOC entry 2075 (class 1259 OID 38229)
-- Dependencies: 2074 6
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
-- TOC entry 2672 (class 0 OID 0)
-- Dependencies: 2075
-- Name: tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq OWNED BY tmg_geospatialcoverage_eastwest.tmg_geospatialcoverage_eastwest_id;


--
-- TOC entry 2076 (class 1259 OID 38231)
-- Dependencies: 2038 6
-- Name: tmg_geospatialcoverage_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_name (
    tmg_geospatialcoverage_name_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_geospatialcoverage_name OWNER TO cleaner;

--
-- TOC entry 2077 (class 1259 OID 38237)
-- Dependencies: 6 2076
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
-- TOC entry 2673 (class 0 OID 0)
-- Dependencies: 2077
-- Name: tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq OWNED BY tmg_geospatialcoverage_name.tmg_geospatialcoverage_name_id;


--
-- TOC entry 2078 (class 1259 OID 38239)
-- Dependencies: 2044 6
-- Name: tmg_geospatialcoverage_northsouth; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_northsouth (
    tmg_geospatialcoverage_northsouth_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_northsouth OWNER TO cleaner;

--
-- TOC entry 2079 (class 1259 OID 38245)
-- Dependencies: 6 2078
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
-- TOC entry 2674 (class 0 OID 0)
-- Dependencies: 2079
-- Name: tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq OWNED BY tmg_geospatialcoverage_northsouth.tmg_geospatialcoverage_northsouth_id;


--
-- TOC entry 2080 (class 1259 OID 38247)
-- Dependencies: 2073 6
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
-- TOC entry 2675 (class 0 OID 0)
-- Dependencies: 2080
-- Name: tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq OWNED BY tmg_geospatialcoverage.tmg_geospatialcoverage_id;


--
-- TOC entry 2081 (class 1259 OID 38249)
-- Dependencies: 2044 6
-- Name: tmg_geospatialcoverage_updown; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_geospatialcoverage_updown (
    tmg_geospatialcoverage_updown_id integer NOT NULL,
    tmg_geospatialcoverage_id integer
)
INHERITS (template_spatialrange);


ALTER TABLE public.tmg_geospatialcoverage_updown OWNER TO cleaner;

--
-- TOC entry 2082 (class 1259 OID 38255)
-- Dependencies: 2081 6
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
-- TOC entry 2676 (class 0 OID 0)
-- Dependencies: 2082
-- Name: tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq OWNED BY tmg_geospatialcoverage_updown.tmg_geospatialcoverage_updown_id;


--
-- TOC entry 2083 (class 1259 OID 38257)
-- Dependencies: 2038 6
-- Name: tmg_keyword; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_keyword (
    tmg_keyword_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_keyword OWNER TO cleaner;

--
-- TOC entry 2084 (class 1259 OID 38263)
-- Dependencies: 2083 6
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
-- TOC entry 2677 (class 0 OID 0)
-- Dependencies: 2084
-- Name: tmg_keyword_tmg_keyword_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_keyword_tmg_keyword_id_seq OWNED BY tmg_keyword.tmg_keyword_id;


--
-- TOC entry 2085 (class 1259 OID 38265)
-- Dependencies: 6
-- Name: tmg_metadata; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_metadata (
    tmg_id integer NOT NULL,
    metadata_id integer NOT NULL
);


ALTER TABLE public.tmg_metadata OWNER TO cleaner;

--
-- TOC entry 2086 (class 1259 OID 38268)
-- Dependencies: 2038 6
-- Name: tmg_project; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_project (
    tmg_project_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_project OWNER TO cleaner;

--
-- TOC entry 2087 (class 1259 OID 38274)
-- Dependencies: 6 2086
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
-- TOC entry 2678 (class 0 OID 0)
-- Dependencies: 2087
-- Name: tmg_project_tmg_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_project_tmg_project_id_seq OWNED BY tmg_project.tmg_project_id;


--
-- TOC entry 2088 (class 1259 OID 38276)
-- Dependencies: 1985 6
-- Name: tmg_property; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_property (
    tmg_property_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_property);


ALTER TABLE public.tmg_property OWNER TO cleaner;

--
-- TOC entry 2089 (class 1259 OID 38282)
-- Dependencies: 6 2088
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
-- TOC entry 2679 (class 0 OID 0)
-- Dependencies: 2089
-- Name: tmg_property_tmg_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_property_tmg_property_id_seq OWNED BY tmg_property.tmg_property_id;


--
-- TOC entry 2090 (class 1259 OID 38284)
-- Dependencies: 2043 6
-- Name: tmg_publisher; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher (
    tmg_publisher_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_sourcetype);


ALTER TABLE public.tmg_publisher OWNER TO cleaner;

--
-- TOC entry 2091 (class 1259 OID 38287)
-- Dependencies: 6 2036
-- Name: tmg_publisher_contact; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher_contact (
    tmg_publisher_contact_id integer NOT NULL,
    tmg_publisher_id integer
)
INHERITS (template_contact);


ALTER TABLE public.tmg_publisher_contact OWNER TO cleaner;

--
-- TOC entry 2092 (class 1259 OID 38293)
-- Dependencies: 2091 6
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
-- TOC entry 2680 (class 0 OID 0)
-- Dependencies: 2092
-- Name: tmg_publisher_contact_tmg_publisher_contact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_contact_tmg_publisher_contact_id_seq OWNED BY tmg_publisher_contact.tmg_publisher_contact_id;


--
-- TOC entry 2093 (class 1259 OID 38295)
-- Dependencies: 6 2038
-- Name: tmg_publisher_name; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_publisher_name (
    tmg_publisher_name_id integer NOT NULL,
    tmg_publisher_id integer
)
INHERITS (template_controlledvocabulary);


ALTER TABLE public.tmg_publisher_name OWNER TO cleaner;

--
-- TOC entry 2094 (class 1259 OID 38301)
-- Dependencies: 2093 6
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
-- TOC entry 2681 (class 0 OID 0)
-- Dependencies: 2094
-- Name: tmg_publisher_name_tmg_publisher_name_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_name_tmg_publisher_name_id_seq OWNED BY tmg_publisher_name.tmg_publisher_name_id;


--
-- TOC entry 2095 (class 1259 OID 38303)
-- Dependencies: 2090 6
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
-- TOC entry 2682 (class 0 OID 0)
-- Dependencies: 2095
-- Name: tmg_publisher_tmg_publisher_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_publisher_tmg_publisher_id_seq OWNED BY tmg_publisher.tmg_publisher_id;


--
-- TOC entry 2096 (class 1259 OID 38305)
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
-- TOC entry 2097 (class 1259 OID 38311)
-- Dependencies: 6 2096
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
-- TOC entry 2683 (class 0 OID 0)
-- Dependencies: 2097
-- Name: tmg_servicename_tmg_servicename_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_servicename_tmg_servicename_id_seq OWNED BY tmg_servicename.tmg_servicename_id;


--
-- TOC entry 2098 (class 1259 OID 38313)
-- Dependencies: 2045 6
-- Name: tmg_timecoverage; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage (
    tmg_timecoverage_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_timecoverage);


ALTER TABLE public.tmg_timecoverage OWNER TO cleaner;

--
-- TOC entry 2099 (class 1259 OID 38319)
-- Dependencies: 6 2040
-- Name: tmg_timecoverage_duration; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_duration (
    tmg_timecoverage_duration_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_duration);


ALTER TABLE public.tmg_timecoverage_duration OWNER TO cleaner;

--
-- TOC entry 2100 (class 1259 OID 38325)
-- Dependencies: 6 2099
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
-- TOC entry 2684 (class 0 OID 0)
-- Dependencies: 2100
-- Name: tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq OWNED BY tmg_timecoverage_duration.tmg_timecoverage_duration_id;


--
-- TOC entry 2101 (class 1259 OID 38327)
-- Dependencies: 2039 6 487
-- Name: tmg_timecoverage_end; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_end (
    tmg_timecoverage_end_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_timecoverage_end OWNER TO cleaner;

--
-- TOC entry 2102 (class 1259 OID 38333)
-- Dependencies: 2101 6
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
-- TOC entry 2685 (class 0 OID 0)
-- Dependencies: 2102
-- Name: tmg_timecoverage_end_tmg_timecoverage_end_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_end_tmg_timecoverage_end_id_seq OWNED BY tmg_timecoverage_end.tmg_timecoverage_end_id;


--
-- TOC entry 2103 (class 1259 OID 38335)
-- Dependencies: 2040 6
-- Name: tmg_timecoverage_resolution; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_resolution (
    tmg_timecoverage_resolution_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_duration);


ALTER TABLE public.tmg_timecoverage_resolution OWNER TO cleaner;

--
-- TOC entry 2104 (class 1259 OID 38341)
-- Dependencies: 6 2103
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
-- TOC entry 2686 (class 0 OID 0)
-- Dependencies: 2104
-- Name: tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq OWNED BY tmg_timecoverage_resolution.tmg_timecoverage_resolution_id;


--
-- TOC entry 2105 (class 1259 OID 38343)
-- Dependencies: 487 6 2039
-- Name: tmg_timecoverage_start; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_timecoverage_start (
    tmg_timecoverage_start_id integer NOT NULL,
    tmg_timecoverage_id integer
)
INHERITS (template_datetypeformatted);


ALTER TABLE public.tmg_timecoverage_start OWNER TO cleaner;

--
-- TOC entry 2106 (class 1259 OID 38349)
-- Dependencies: 2105 6
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
-- TOC entry 2687 (class 0 OID 0)
-- Dependencies: 2106
-- Name: tmg_timecoverage_start_tmg_timecoverage_start_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_start_tmg_timecoverage_start_id_seq OWNED BY tmg_timecoverage_start.tmg_timecoverage_start_id;


--
-- TOC entry 2107 (class 1259 OID 38351)
-- Dependencies: 2098 6
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
-- TOC entry 2688 (class 0 OID 0)
-- Dependencies: 2107
-- Name: tmg_timecoverage_tmg_timecoverage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_timecoverage_tmg_timecoverage_id_seq OWNED BY tmg_timecoverage.tmg_timecoverage_id;


--
-- TOC entry 2108 (class 1259 OID 38353)
-- Dependencies: 2048 6
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
-- TOC entry 2689 (class 0 OID 0)
-- Dependencies: 2108
-- Name: tmg_tmg_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_tmg_id_seq OWNED BY tmg.tmg_id;


--
-- TOC entry 2109 (class 1259 OID 38355)
-- Dependencies: 501 2047 6
-- Name: tmg_variables; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables (
    tmg_variables_id integer NOT NULL,
    tmg_id integer
)
INHERITS (template_variables);


ALTER TABLE public.tmg_variables OWNER TO cleaner;

--
-- TOC entry 2110 (class 1259 OID 38361)
-- Dependencies: 2109 6
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
-- TOC entry 2690 (class 0 OID 0)
-- Dependencies: 2110
-- Name: tmg_variables_tmg_variables_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_tmg_variables_id_seq OWNED BY tmg_variables.tmg_variables_id;


--
-- TOC entry 2111 (class 1259 OID 38363)
-- Dependencies: 2046 6
-- Name: tmg_variables_variable; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_variable (
    tmg_variables_variable_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_variable);


ALTER TABLE public.tmg_variables_variable OWNER TO cleaner;

--
-- TOC entry 2112 (class 1259 OID 38369)
-- Dependencies: 2111 6
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
-- TOC entry 2691 (class 0 OID 0)
-- Dependencies: 2112
-- Name: tmg_variables_variable_tmg_variables_variable_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_variable_tmg_variables_variable_id_seq OWNED BY tmg_variables_variable.tmg_variables_variable_id;


--
-- TOC entry 2113 (class 1259 OID 38371)
-- Dependencies: 503 6 1989
-- Name: tmg_variables_variablemap; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_variablemap (
    tmg_variables_variablemap_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_variables_variablemap OWNER TO cleaner;

--
-- TOC entry 2114 (class 1259 OID 38377)
-- Dependencies: 2113 6
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
-- TOC entry 2692 (class 0 OID 0)
-- Dependencies: 2114
-- Name: tmg_variables_variablemap_tmg_variables_variablemap_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_variablemap_tmg_variables_variablemap_id_seq OWNED BY tmg_variables_variablemap.tmg_variables_variablemap_id;


--
-- TOC entry 2115 (class 1259 OID 38379)
-- Dependencies: 503 6 1989
-- Name: tmg_variables_xlink; Type: TABLE; Schema: public; Owner: cleaner; Tablespace: 
--

CREATE TABLE tmg_variables_xlink (
    tmg_variables_xlink_id integer NOT NULL,
    tmg_variables_id integer
)
INHERITS (template_xlink);


ALTER TABLE public.tmg_variables_xlink OWNER TO cleaner;

--
-- TOC entry 2116 (class 1259 OID 38385)
-- Dependencies: 2115 6
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
-- TOC entry 2693 (class 0 OID 0)
-- Dependencies: 2116
-- Name: tmg_variables_xlink_tmg_variables_xlink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: cleaner
--

ALTER SEQUENCE tmg_variables_xlink_tmg_variables_xlink_id_seq OWNED BY tmg_variables_xlink.tmg_variables_xlink_id;


--
-- TOC entry 2394 (class 2604 OID 38387)
-- Dependencies: 1983 1982
-- Name: catalog_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog ALTER COLUMN catalog_id SET DEFAULT nextval('catalog_catalog_id_seq'::regclass);


--
-- TOC entry 2395 (class 2604 OID 38388)
-- Dependencies: 1987 1986
-- Name: catalog_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog_property ALTER COLUMN catalog_property_id SET DEFAULT nextval('catalog_property_catalog_property_id_seq'::regclass);


--
-- TOC entry 2396 (class 2604 OID 38389)
-- Dependencies: 1991 1990
-- Name: catalog_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalog_xlink ALTER COLUMN catalog_xlink_id SET DEFAULT nextval('catalog_xlink_catalog_xlink_id_seq'::regclass);


--
-- TOC entry 2397 (class 2604 OID 38390)
-- Dependencies: 1993 1992
-- Name: catalogref_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref ALTER COLUMN catalogref_id SET DEFAULT nextval('catalogref_catalogref_id_seq'::regclass);


--
-- TOC entry 2398 (class 2604 OID 38391)
-- Dependencies: 1996 1995
-- Name: catalogref_documentation_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation ALTER COLUMN catalogref_documentation_id SET DEFAULT nextval('catalogref_documentation_catalogref_documentation_id_seq'::regclass);


--
-- TOC entry 2399 (class 2604 OID 38392)
-- Dependencies: 1999 1998
-- Name: catalogref_documentation_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation_namespace ALTER COLUMN catalogref_documentation_namespace_id SET DEFAULT nextval('catalogref_documentation_name_catalogref_documentation_name_seq'::regclass);


--
-- TOC entry 2400 (class 2604 OID 38393)
-- Dependencies: 2002 2001
-- Name: catalogref_documentation_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_documentation_xlink ALTER COLUMN catalogref_documentation_xlink_id SET DEFAULT nextval('catalogref_documentation_xlink_catalogref_documentation_xlink_s'::regclass);


--
-- TOC entry 2401 (class 2604 OID 38394)
-- Dependencies: 2004 2003
-- Name: catalogref_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE catalogref_xlink ALTER COLUMN catalogref_xlink_id SET DEFAULT nextval('catalogref_xlink_catalogref_xlink_id_seq'::regclass);


--
-- TOC entry 2402 (class 2604 OID 38395)
-- Dependencies: 2014 2005
-- Name: dataset_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset ALTER COLUMN dataset_id SET DEFAULT nextval('dataset_dataset_id_seq'::regclass);


--
-- TOC entry 2403 (class 2604 OID 38396)
-- Dependencies: 2008 2007
-- Name: dataset_access_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_access ALTER COLUMN dataset_access_id SET DEFAULT nextval('dataset_access_dataset_access_id_seq'::regclass);


--
-- TOC entry 2404 (class 2604 OID 38397)
-- Dependencies: 2011 2010
-- Name: dataset_access_datasize_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_access_datasize ALTER COLUMN dataset_access_datasize_id SET DEFAULT nextval('dataset_access_datasize_dataset_access_datasize_id_seq'::regclass);


--
-- TOC entry 2405 (class 2604 OID 38398)
-- Dependencies: 2016 2015
-- Name: dataset_ncml_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_ncml ALTER COLUMN dataset_ncml_id SET DEFAULT nextval('dataset_ncml_dataset_ncml_id_seq'::regclass);


--
-- TOC entry 2406 (class 2604 OID 38399)
-- Dependencies: 2018 2017
-- Name: dataset_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE dataset_property ALTER COLUMN dataset_property_id SET DEFAULT nextval('dataset_property_dataset_property_id_seq'::regclass);


--
-- TOC entry 2407 (class 2604 OID 38400)
-- Dependencies: 2022 2021
-- Name: metadata_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata ALTER COLUMN metadata_id SET DEFAULT nextval('metadata_metadata_id_seq'::regclass);


--
-- TOC entry 2409 (class 2604 OID 38401)
-- Dependencies: 2024 2023
-- Name: metadata_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata_namespace ALTER COLUMN metadata_namespace_id SET DEFAULT nextval('metadata_namespace_metadata_namespace_id_seq'::regclass);


--
-- TOC entry 2410 (class 2604 OID 38402)
-- Dependencies: 2027 2026
-- Name: metadata_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE metadata_xlink ALTER COLUMN metadata_xlink_id SET DEFAULT nextval('metadata_xlink_metadata_xlink_id_seq'::regclass);


--
-- TOC entry 2411 (class 2604 OID 38403)
-- Dependencies: 2035 2028
-- Name: service_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service ALTER COLUMN service_id SET DEFAULT nextval('service_service_id_seq'::regclass);


--
-- TOC entry 2412 (class 2604 OID 38404)
-- Dependencies: 2031 2030
-- Name: service_datasetroot_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service_datasetroot ALTER COLUMN service_datasetroot_id SET DEFAULT nextval('service_datasetroot_service_datasetroot_id_seq'::regclass);


--
-- TOC entry 2413 (class 2604 OID 38405)
-- Dependencies: 2033 2032
-- Name: service_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE service_property ALTER COLUMN service_property_id SET DEFAULT nextval('service_property_service_property_id_seq'::regclass);


--
-- TOC entry 2415 (class 2604 OID 38406)
-- Dependencies: 2108 2048
-- Name: tmg_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg ALTER COLUMN tmg_id SET DEFAULT nextval('tmg_tmg_id_seq'::regclass);


--
-- TOC entry 2417 (class 2604 OID 38407)
-- Dependencies: 2050 2049
-- Name: tmg_authority_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_authority ALTER COLUMN tmg_authority_id SET DEFAULT nextval('tmg_authority_tmg_authority_id_seq'::regclass);


--
-- TOC entry 2418 (class 2604 OID 38408)
-- Dependencies: 2052 2051
-- Name: tmg_contributor_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_contributor ALTER COLUMN tmg_contributor_id SET DEFAULT nextval('tmg_contributor_tmg_contributor_id_seq'::regclass);


--
-- TOC entry 2419 (class 2604 OID 38409)
-- Dependencies: 2058 2053
-- Name: tmg_creator_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator ALTER COLUMN tmg_creator_id SET DEFAULT nextval('tmg_creator_tmg_creator_id_seq'::regclass);


--
-- TOC entry 2420 (class 2604 OID 38410)
-- Dependencies: 2055 2054
-- Name: tmg_creator_contact_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator_contact ALTER COLUMN tmg_creator_contact_id SET DEFAULT nextval('tmg_creator_contact_tmg_creator_contact_id_seq'::regclass);


--
-- TOC entry 2421 (class 2604 OID 38411)
-- Dependencies: 2057 2056
-- Name: tmg_creator_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_creator_name ALTER COLUMN tmg_creator_name_id SET DEFAULT nextval('tmg_creator_name_tmg_creator_name_id_seq'::regclass);


--
-- TOC entry 2422 (class 2604 OID 38412)
-- Dependencies: 2060 2059
-- Name: tmg_dataformat_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_dataformat ALTER COLUMN tmg_dataformat_id SET DEFAULT nextval('tmg_dataformat_tmg_dataformat_id_seq'::regclass);


--
-- TOC entry 2423 (class 2604 OID 38413)
-- Dependencies: 2062 2061
-- Name: tmg_datasize_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_datasize ALTER COLUMN tmg_datasize_id SET DEFAULT nextval('tmg_datasize_tmg_datasize_id_seq'::regclass);


--
-- TOC entry 2424 (class 2604 OID 38414)
-- Dependencies: 2064 2063
-- Name: tmg_datatype_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_datatype ALTER COLUMN tmg_datatype_id SET DEFAULT nextval('tmg_datatype_tmg_datatype_id_seq'::regclass);


--
-- TOC entry 2425 (class 2604 OID 38415)
-- Dependencies: 2066 2065
-- Name: tmg_date_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_date ALTER COLUMN tmg_date_id SET DEFAULT nextval('tmg_date_tmg_date_id_seq'::regclass);


--
-- TOC entry 2426 (class 2604 OID 38416)
-- Dependencies: 2070 2067
-- Name: tmg_documentation_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation ALTER COLUMN tmg_documentation_id SET DEFAULT nextval('tmg_documentation_tmg_documentation_id_seq'::regclass);


--
-- TOC entry 2427 (class 2604 OID 38417)
-- Dependencies: 2069 2068
-- Name: tmg_documentation_namespace_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation_namespace ALTER COLUMN tmg_documentation_namespace_id SET DEFAULT nextval('tmg_documentation_namespace_tmg_documentation_namespace_id_seq'::regclass);


--
-- TOC entry 2428 (class 2604 OID 38418)
-- Dependencies: 2072 2071
-- Name: tmg_documentation_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_documentation_xlink ALTER COLUMN tmg_documentation_xlink_id SET DEFAULT nextval('tmg_documentation_xlink_tmg_documentation_xlink_id_seq'::regclass);


--
-- TOC entry 2430 (class 2604 OID 38419)
-- Dependencies: 2080 2073
-- Name: tmg_geospatialcoverage_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage ALTER COLUMN tmg_geospatialcoverage_id SET DEFAULT nextval('tmg_geospatialcoverage_tmg_geospatialcoverage_id_seq'::regclass);


--
-- TOC entry 2431 (class 2604 OID 38420)
-- Dependencies: 2075 2074
-- Name: tmg_geospatialcoverage_eastwest_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_eastwest ALTER COLUMN tmg_geospatialcoverage_eastwest_id SET DEFAULT nextval('tmg_geospatialcoverage_eastwe_tmg_geospatialcoverage_eastwe_seq'::regclass);


--
-- TOC entry 2432 (class 2604 OID 38421)
-- Dependencies: 2077 2076
-- Name: tmg_geospatialcoverage_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_name ALTER COLUMN tmg_geospatialcoverage_name_id SET DEFAULT nextval('tmg_geospatialcoverage_name_tmg_geospatialcoverage_name_id_seq'::regclass);


--
-- TOC entry 2433 (class 2604 OID 38422)
-- Dependencies: 2079 2078
-- Name: tmg_geospatialcoverage_northsouth_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_northsouth ALTER COLUMN tmg_geospatialcoverage_northsouth_id SET DEFAULT nextval('tmg_geospatialcoverage_norths_tmg_geospatialcoverage_norths_seq'::regclass);


--
-- TOC entry 2434 (class 2604 OID 38423)
-- Dependencies: 2082 2081
-- Name: tmg_geospatialcoverage_updown_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_geospatialcoverage_updown ALTER COLUMN tmg_geospatialcoverage_updown_id SET DEFAULT nextval('tmg_geospatialcoverage_updown_tmg_geospatialcoverage_updown_seq'::regclass);


--
-- TOC entry 2435 (class 2604 OID 38424)
-- Dependencies: 2084 2083
-- Name: tmg_keyword_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_keyword ALTER COLUMN tmg_keyword_id SET DEFAULT nextval('tmg_keyword_tmg_keyword_id_seq'::regclass);


--
-- TOC entry 2436 (class 2604 OID 38425)
-- Dependencies: 2087 2086
-- Name: tmg_project_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_project ALTER COLUMN tmg_project_id SET DEFAULT nextval('tmg_project_tmg_project_id_seq'::regclass);


--
-- TOC entry 2437 (class 2604 OID 38426)
-- Dependencies: 2089 2088
-- Name: tmg_property_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_property ALTER COLUMN tmg_property_id SET DEFAULT nextval('tmg_property_tmg_property_id_seq'::regclass);


--
-- TOC entry 2438 (class 2604 OID 38427)
-- Dependencies: 2095 2090
-- Name: tmg_publisher_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher ALTER COLUMN tmg_publisher_id SET DEFAULT nextval('tmg_publisher_tmg_publisher_id_seq'::regclass);


--
-- TOC entry 2439 (class 2604 OID 38428)
-- Dependencies: 2092 2091
-- Name: tmg_publisher_contact_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher_contact ALTER COLUMN tmg_publisher_contact_id SET DEFAULT nextval('tmg_publisher_contact_tmg_publisher_contact_id_seq'::regclass);


--
-- TOC entry 2440 (class 2604 OID 38429)
-- Dependencies: 2094 2093
-- Name: tmg_publisher_name_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_publisher_name ALTER COLUMN tmg_publisher_name_id SET DEFAULT nextval('tmg_publisher_name_tmg_publisher_name_id_seq'::regclass);


--
-- TOC entry 2441 (class 2604 OID 38430)
-- Dependencies: 2097 2096
-- Name: tmg_servicename_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_servicename ALTER COLUMN tmg_servicename_id SET DEFAULT nextval('tmg_servicename_tmg_servicename_id_seq'::regclass);


--
-- TOC entry 2442 (class 2604 OID 38431)
-- Dependencies: 2107 2098
-- Name: tmg_timecoverage_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage ALTER COLUMN tmg_timecoverage_id SET DEFAULT nextval('tmg_timecoverage_tmg_timecoverage_id_seq'::regclass);


--
-- TOC entry 2443 (class 2604 OID 38432)
-- Dependencies: 2100 2099
-- Name: tmg_timecoverage_duration_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_duration ALTER COLUMN tmg_timecoverage_duration_id SET DEFAULT nextval('tmg_timecoverage_duration_tmg_timecoverage_duration_id_seq'::regclass);


--
-- TOC entry 2444 (class 2604 OID 38433)
-- Dependencies: 2102 2101
-- Name: tmg_timecoverage_end_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_end ALTER COLUMN tmg_timecoverage_end_id SET DEFAULT nextval('tmg_timecoverage_end_tmg_timecoverage_end_id_seq'::regclass);


--
-- TOC entry 2445 (class 2604 OID 38434)
-- Dependencies: 2104 2103
-- Name: tmg_timecoverage_resolution_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_resolution ALTER COLUMN tmg_timecoverage_resolution_id SET DEFAULT nextval('tmg_timecoverage_resolution_tmg_timecoverage_resolution_id_seq'::regclass);


--
-- TOC entry 2446 (class 2604 OID 38435)
-- Dependencies: 2106 2105
-- Name: tmg_timecoverage_start_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_timecoverage_start ALTER COLUMN tmg_timecoverage_start_id SET DEFAULT nextval('tmg_timecoverage_start_tmg_timecoverage_start_id_seq'::regclass);


--
-- TOC entry 2447 (class 2604 OID 38436)
-- Dependencies: 2110 2109
-- Name: tmg_variables_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables ALTER COLUMN tmg_variables_id SET DEFAULT nextval('tmg_variables_tmg_variables_id_seq'::regclass);


--
-- TOC entry 2448 (class 2604 OID 38437)
-- Dependencies: 2112 2111
-- Name: tmg_variables_variable_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_variable ALTER COLUMN tmg_variables_variable_id SET DEFAULT nextval('tmg_variables_variable_tmg_variables_variable_id_seq'::regclass);


--
-- TOC entry 2449 (class 2604 OID 38438)
-- Dependencies: 2114 2113
-- Name: tmg_variables_variablemap_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_variablemap ALTER COLUMN tmg_variables_variablemap_id SET DEFAULT nextval('tmg_variables_variablemap_tmg_variables_variablemap_id_seq'::regclass);


--
-- TOC entry 2450 (class 2604 OID 38439)
-- Dependencies: 2116 2115
-- Name: tmg_variables_xlink_id; Type: DEFAULT; Schema: public; Owner: cleaner
--

ALTER TABLE tmg_variables_xlink ALTER COLUMN tmg_variables_xlink_id SET DEFAULT nextval('tmg_variables_xlink_tmg_variables_xlink_id_seq'::regclass);


--
-- TOC entry 2452 (class 2606 OID 38441)
-- Dependencies: 1982 1982
-- Name: catalog_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog
    ADD CONSTRAINT catalog_pkey PRIMARY KEY (catalog_id);


--
-- TOC entry 2454 (class 2606 OID 38443)
-- Dependencies: 1986 1986
-- Name: catalog_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog_property
    ADD CONSTRAINT catalog_property_pkey PRIMARY KEY (catalog_property_id);


--
-- TOC entry 2456 (class 2606 OID 38445)
-- Dependencies: 1990 1990
-- Name: catalog_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalog_xlink
    ADD CONSTRAINT catalog_xlink_pkey PRIMARY KEY (catalog_xlink_id);


--
-- TOC entry 2462 (class 2606 OID 38447)
-- Dependencies: 1998 1998
-- Name: catalogref_documentation_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation_namespace
    ADD CONSTRAINT catalogref_documentation_namespace_pkey PRIMARY KEY (catalogref_documentation_namespace_id);


--
-- TOC entry 2460 (class 2606 OID 38449)
-- Dependencies: 1995 1995
-- Name: catalogref_documentation_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation
    ADD CONSTRAINT catalogref_documentation_pkey PRIMARY KEY (catalogref_documentation_id);


--
-- TOC entry 2464 (class 2606 OID 38451)
-- Dependencies: 2001 2001
-- Name: catalogref_documentation_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_documentation_xlink
    ADD CONSTRAINT catalogref_documentation_xlink_pkey PRIMARY KEY (catalogref_documentation_xlink_id);


--
-- TOC entry 2458 (class 2606 OID 38453)
-- Dependencies: 1992 1992
-- Name: catalogref_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalogref_pkey PRIMARY KEY (catalogref_id);


--
-- TOC entry 2466 (class 2606 OID 38455)
-- Dependencies: 2003 2003
-- Name: catalogref_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY catalogref_xlink
    ADD CONSTRAINT catalogref_xlink_pkey PRIMARY KEY (catalogref_xlink_id);


--
-- TOC entry 2472 (class 2606 OID 38457)
-- Dependencies: 2010 2010
-- Name: dataset_access_datasize_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_access_datasize
    ADD CONSTRAINT dataset_access_datasize_pkey PRIMARY KEY (dataset_access_datasize_id);


--
-- TOC entry 2470 (class 2606 OID 38459)
-- Dependencies: 2007 2007
-- Name: dataset_access_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_access
    ADD CONSTRAINT dataset_access_pkey PRIMARY KEY (dataset_access_id);


--
-- TOC entry 2474 (class 2606 OID 38461)
-- Dependencies: 2015 2015
-- Name: dataset_ncml_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_ncml
    ADD CONSTRAINT dataset_ncml_pkey PRIMARY KEY (dataset_ncml_id);


--
-- TOC entry 2468 (class 2606 OID 38463)
-- Dependencies: 2005 2005
-- Name: dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id);


--
-- TOC entry 2476 (class 2606 OID 38465)
-- Dependencies: 2017 2017
-- Name: dataset_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY dataset_property
    ADD CONSTRAINT dataset_property_pkey PRIMARY KEY (dataset_property_id);


--
-- TOC entry 2480 (class 2606 OID 38467)
-- Dependencies: 2023 2023
-- Name: metadata_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata_namespace
    ADD CONSTRAINT metadata_namespace_pkey PRIMARY KEY (metadata_namespace_id);


--
-- TOC entry 2478 (class 2606 OID 38469)
-- Dependencies: 2021 2021
-- Name: metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata
    ADD CONSTRAINT metadata_pkey PRIMARY KEY (metadata_id);


--
-- TOC entry 2482 (class 2606 OID 38471)
-- Dependencies: 2026 2026
-- Name: metadata_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY metadata_xlink
    ADD CONSTRAINT metadata_xlink_pkey PRIMARY KEY (metadata_xlink_id);


--
-- TOC entry 2486 (class 2606 OID 38473)
-- Dependencies: 2030 2030
-- Name: service_datasetroot_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service_datasetroot
    ADD CONSTRAINT service_datasetroot_pkey PRIMARY KEY (service_datasetroot_id);


--
-- TOC entry 2484 (class 2606 OID 38475)
-- Dependencies: 2028 2028
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (service_id);


--
-- TOC entry 2488 (class 2606 OID 38477)
-- Dependencies: 2032 2032
-- Name: service_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY service_property
    ADD CONSTRAINT service_property_pkey PRIMARY KEY (service_property_id);


--
-- TOC entry 2490 (class 2606 OID 38479)
-- Dependencies: 2048 2048
-- Name: threddsmetadatagroup_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg
    ADD CONSTRAINT threddsmetadatagroup_pkey PRIMARY KEY (tmg_id);


--
-- TOC entry 2492 (class 2606 OID 38481)
-- Dependencies: 2049 2049
-- Name: tmg_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_authority
    ADD CONSTRAINT tmg_authority_pkey PRIMARY KEY (tmg_authority_id);


--
-- TOC entry 2494 (class 2606 OID 38483)
-- Dependencies: 2051 2051
-- Name: tmg_contributor_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_contributor
    ADD CONSTRAINT tmg_contributor_pkey PRIMARY KEY (tmg_contributor_id);


--
-- TOC entry 2498 (class 2606 OID 38485)
-- Dependencies: 2054 2054
-- Name: tmg_creator_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator_contact
    ADD CONSTRAINT tmg_creator_contact_pkey PRIMARY KEY (tmg_creator_contact_id);


--
-- TOC entry 2500 (class 2606 OID 38487)
-- Dependencies: 2056 2056
-- Name: tmg_creator_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator_name
    ADD CONSTRAINT tmg_creator_name_pkey PRIMARY KEY (tmg_creator_name_id);


--
-- TOC entry 2496 (class 2606 OID 38489)
-- Dependencies: 2053 2053
-- Name: tmg_creator_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_creator
    ADD CONSTRAINT tmg_creator_pkey PRIMARY KEY (tmg_creator_id);


--
-- TOC entry 2502 (class 2606 OID 38491)
-- Dependencies: 2059 2059
-- Name: tmg_dataformat_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_dataformat
    ADD CONSTRAINT tmg_dataformat_pkey PRIMARY KEY (tmg_dataformat_id);


--
-- TOC entry 2504 (class 2606 OID 38493)
-- Dependencies: 2061 2061
-- Name: tmg_datasize_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_datasize
    ADD CONSTRAINT tmg_datasize_pkey PRIMARY KEY (tmg_datasize_id);


--
-- TOC entry 2506 (class 2606 OID 38495)
-- Dependencies: 2063 2063
-- Name: tmg_datatype_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_datatype
    ADD CONSTRAINT tmg_datatype_pkey PRIMARY KEY (tmg_datatype_id);


--
-- TOC entry 2508 (class 2606 OID 38497)
-- Dependencies: 2065 2065
-- Name: tmg_date_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_date
    ADD CONSTRAINT tmg_date_pkey PRIMARY KEY (tmg_date_id);


--
-- TOC entry 2512 (class 2606 OID 38499)
-- Dependencies: 2068 2068
-- Name: tmg_documentation_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation_namespace
    ADD CONSTRAINT tmg_documentation_namespace_pkey PRIMARY KEY (tmg_documentation_namespace_id);


--
-- TOC entry 2510 (class 2606 OID 38501)
-- Dependencies: 2067 2067
-- Name: tmg_documentation_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation
    ADD CONSTRAINT tmg_documentation_pkey PRIMARY KEY (tmg_documentation_id);


--
-- TOC entry 2514 (class 2606 OID 38503)
-- Dependencies: 2071 2071
-- Name: tmg_documentation_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_documentation_xlink
    ADD CONSTRAINT tmg_documentation_xlink_pkey PRIMARY KEY (tmg_documentation_xlink_id);


--
-- TOC entry 2518 (class 2606 OID 38505)
-- Dependencies: 2074 2074
-- Name: tmg_geospatialcoverage_eastwest_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_eastwest
    ADD CONSTRAINT tmg_geospatialcoverage_eastwest_pkey PRIMARY KEY (tmg_geospatialcoverage_eastwest_id);


--
-- TOC entry 2520 (class 2606 OID 38507)
-- Dependencies: 2076 2076
-- Name: tmg_geospatialcoverage_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_name
    ADD CONSTRAINT tmg_geospatialcoverage_name_pkey PRIMARY KEY (tmg_geospatialcoverage_name_id);


--
-- TOC entry 2522 (class 2606 OID 38509)
-- Dependencies: 2078 2078
-- Name: tmg_geospatialcoverage_northsouth_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_northsouth
    ADD CONSTRAINT tmg_geospatialcoverage_northsouth_pkey PRIMARY KEY (tmg_geospatialcoverage_northsouth_id);


--
-- TOC entry 2516 (class 2606 OID 38511)
-- Dependencies: 2073 2073
-- Name: tmg_geospatialcoverage_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage
    ADD CONSTRAINT tmg_geospatialcoverage_pkey PRIMARY KEY (tmg_geospatialcoverage_id);


--
-- TOC entry 2524 (class 2606 OID 38513)
-- Dependencies: 2081 2081
-- Name: tmg_geospatialcoverage_updown_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_geospatialcoverage_updown
    ADD CONSTRAINT tmg_geospatialcoverage_updown_pkey PRIMARY KEY (tmg_geospatialcoverage_updown_id);


--
-- TOC entry 2526 (class 2606 OID 38515)
-- Dependencies: 2083 2083
-- Name: tmg_keyword_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_keyword
    ADD CONSTRAINT tmg_keyword_pkey PRIMARY KEY (tmg_keyword_id);


--
-- TOC entry 2528 (class 2606 OID 38517)
-- Dependencies: 2086 2086
-- Name: tmg_project_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_project
    ADD CONSTRAINT tmg_project_pkey PRIMARY KEY (tmg_project_id);


--
-- TOC entry 2530 (class 2606 OID 38519)
-- Dependencies: 2088 2088
-- Name: tmg_property_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_property
    ADD CONSTRAINT tmg_property_pkey PRIMARY KEY (tmg_property_id);


--
-- TOC entry 2534 (class 2606 OID 38521)
-- Dependencies: 2091 2091
-- Name: tmg_publisher_contact_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher_contact
    ADD CONSTRAINT tmg_publisher_contact_pkey PRIMARY KEY (tmg_publisher_contact_id);


--
-- TOC entry 2536 (class 2606 OID 38523)
-- Dependencies: 2093 2093
-- Name: tmg_publisher_name_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher_name
    ADD CONSTRAINT tmg_publisher_name_pkey PRIMARY KEY (tmg_publisher_name_id);


--
-- TOC entry 2532 (class 2606 OID 38525)
-- Dependencies: 2090 2090
-- Name: tmg_publisher_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_publisher
    ADD CONSTRAINT tmg_publisher_pkey PRIMARY KEY (tmg_publisher_id);


--
-- TOC entry 2538 (class 2606 OID 38527)
-- Dependencies: 2096 2096
-- Name: tmg_servicename_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_servicename
    ADD CONSTRAINT tmg_servicename_pkey PRIMARY KEY (tmg_servicename_id);


--
-- TOC entry 2542 (class 2606 OID 38529)
-- Dependencies: 2099 2099
-- Name: tmg_timecoverage_duration_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_duration
    ADD CONSTRAINT tmg_timecoverage_duration_pkey PRIMARY KEY (tmg_timecoverage_duration_id);


--
-- TOC entry 2544 (class 2606 OID 38531)
-- Dependencies: 2101 2101
-- Name: tmg_timecoverage_end_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_end
    ADD CONSTRAINT tmg_timecoverage_end_pkey PRIMARY KEY (tmg_timecoverage_end_id);


--
-- TOC entry 2540 (class 2606 OID 38533)
-- Dependencies: 2098 2098
-- Name: tmg_timecoverage_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage
    ADD CONSTRAINT tmg_timecoverage_pkey PRIMARY KEY (tmg_timecoverage_id);


--
-- TOC entry 2546 (class 2606 OID 38535)
-- Dependencies: 2103 2103
-- Name: tmg_timecoverage_resolution_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_resolution
    ADD CONSTRAINT tmg_timecoverage_resolution_pkey PRIMARY KEY (tmg_timecoverage_resolution_id);


--
-- TOC entry 2548 (class 2606 OID 38537)
-- Dependencies: 2105 2105
-- Name: tmg_timecoverage_start_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_timecoverage_start
    ADD CONSTRAINT tmg_timecoverage_start_pkey PRIMARY KEY (tmg_timecoverage_start_id);


--
-- TOC entry 2550 (class 2606 OID 38539)
-- Dependencies: 2109 2109
-- Name: tmg_variables_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables
    ADD CONSTRAINT tmg_variables_pkey PRIMARY KEY (tmg_variables_id);


--
-- TOC entry 2552 (class 2606 OID 38541)
-- Dependencies: 2111 2111
-- Name: tmg_variables_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_variable
    ADD CONSTRAINT tmg_variables_variable_pkey PRIMARY KEY (tmg_variables_variable_id);


--
-- TOC entry 2554 (class 2606 OID 38543)
-- Dependencies: 2113 2113
-- Name: tmg_variables_variablemap_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_variablemap
    ADD CONSTRAINT tmg_variables_variablemap_pkey PRIMARY KEY (tmg_variables_variablemap_id);


--
-- TOC entry 2556 (class 2606 OID 38545)
-- Dependencies: 2115 2115
-- Name: tmg_variables_xlink_pkey; Type: CONSTRAINT; Schema: public; Owner: cleaner; Tablespace: 
--

ALTER TABLE ONLY tmg_variables_xlink
    ADD CONSTRAINT tmg_variables_xlink_pkey PRIMARY KEY (tmg_variables_xlink_id);


--
-- TOC entry 2563 (class 2606 OID 38546)
-- Dependencies: 1982 1992 2451
-- Name: catalog_catalog_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalog_catalog_child_id_fkey FOREIGN KEY (child_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2564 (class 2606 OID 38551)
-- Dependencies: 1982 1992 2451
-- Name: catalog_catalog_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref
    ADD CONSTRAINT catalog_catalog_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2557 (class 2606 OID 38556)
-- Dependencies: 1982 1984 2451
-- Name: catalog_dataset_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_dataset
    ADD CONSTRAINT catalog_dataset_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2558 (class 2606 OID 38561)
-- Dependencies: 2005 1984 2467
-- Name: catalog_dataset_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_dataset
    ADD CONSTRAINT catalog_dataset_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2559 (class 2606 OID 38566)
-- Dependencies: 2451 1986 1982
-- Name: catalog_property_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_property
    ADD CONSTRAINT catalog_property_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2560 (class 2606 OID 38576)
-- Dependencies: 1988 1982 2451
-- Name: catalog_service_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_service
    ADD CONSTRAINT catalog_service_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2561 (class 2606 OID 38581)
-- Dependencies: 2483 1988 2028
-- Name: catalog_service_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_service
    ADD CONSTRAINT catalog_service_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2562 (class 2606 OID 38586)
-- Dependencies: 1982 1990 2451
-- Name: catalog_xlink_catalog_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalog_xlink
    ADD CONSTRAINT catalog_xlink_catalog_id_fkey FOREIGN KEY (catalog_id) REFERENCES catalog(catalog_id);


--
-- TOC entry 2565 (class 2606 OID 38596)
-- Dependencies: 1992 1995 2457
-- Name: catalogref_documentation_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation
    ADD CONSTRAINT catalogref_documentation_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2566 (class 2606 OID 38601)
-- Dependencies: 1995 2459 1998
-- Name: catalogref_documentation_names_catalogref_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation_namespace
    ADD CONSTRAINT catalogref_documentation_names_catalogref_documentation_id_fkey FOREIGN KEY (catalogref_documentation_id) REFERENCES catalogref_documentation(catalogref_documentation_id);


--
-- TOC entry 2567 (class 2606 OID 38606)
-- Dependencies: 2001 1995 2459
-- Name: catalogref_documentation_xlink_catalogref_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_documentation_xlink
    ADD CONSTRAINT catalogref_documentation_xlink_catalogref_documentation_id_fkey FOREIGN KEY (catalogref_documentation_id) REFERENCES catalogref_documentation(catalogref_documentation_id);


--
-- TOC entry 2568 (class 2606 OID 38611)
-- Dependencies: 1992 2457 2003
-- Name: catalogref_xlink_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY catalogref_xlink
    ADD CONSTRAINT catalogref_xlink_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2569 (class 2606 OID 38701)
-- Dependencies: 2005 2467 2007
-- Name: dataset_access_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_access
    ADD CONSTRAINT dataset_access_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2570 (class 2606 OID 38711)
-- Dependencies: 2469 2007 2010
-- Name: dataset_access_datasize_dataset_access_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_access_datasize
    ADD CONSTRAINT dataset_access_datasize_dataset_access_id_fkey FOREIGN KEY (dataset_access_id) REFERENCES dataset_access(dataset_access_id);


--
-- TOC entry 2571 (class 2606 OID 38716)
-- Dependencies: 2457 2012 1992
-- Name: dataset_catalogref_catalogref_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_catalogref
    ADD CONSTRAINT dataset_catalogref_catalogref_id_fkey FOREIGN KEY (catalogref_id) REFERENCES catalogref(catalogref_id);


--
-- TOC entry 2572 (class 2606 OID 38721)
-- Dependencies: 2005 2467 2012
-- Name: dataset_catalogref_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_catalogref
    ADD CONSTRAINT dataset_catalogref_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2573 (class 2606 OID 38726)
-- Dependencies: 2005 2013 2467
-- Name: dataset_dataset_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_dataset
    ADD CONSTRAINT dataset_dataset_child_id_fkey FOREIGN KEY (child_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2574 (class 2606 OID 38731)
-- Dependencies: 2005 2013 2467
-- Name: dataset_dataset_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_dataset
    ADD CONSTRAINT dataset_dataset_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2575 (class 2606 OID 38741)
-- Dependencies: 2467 2005 2015
-- Name: dataset_ncml_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_ncml
    ADD CONSTRAINT dataset_ncml_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2576 (class 2606 OID 38751)
-- Dependencies: 2005 2017 2467
-- Name: dataset_property_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_property
    ADD CONSTRAINT dataset_property_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2577 (class 2606 OID 38756)
-- Dependencies: 2019 2467 2005
-- Name: dataset_service_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_service
    ADD CONSTRAINT dataset_service_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2578 (class 2606 OID 38761)
-- Dependencies: 2483 2028 2019
-- Name: dataset_service_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_service
    ADD CONSTRAINT dataset_service_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2579 (class 2606 OID 38766)
-- Dependencies: 2005 2020 2467
-- Name: dataset_tmg_dataset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_tmg
    ADD CONSTRAINT dataset_tmg_dataset_id_fkey FOREIGN KEY (dataset_id) REFERENCES dataset(dataset_id);


--
-- TOC entry 2580 (class 2606 OID 38771)
-- Dependencies: 2048 2489 2020
-- Name: dataset_tmg_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY dataset_tmg
    ADD CONSTRAINT dataset_tmg_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2581 (class 2606 OID 38781)
-- Dependencies: 2477 2021 2023
-- Name: metadata_namespace_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_namespace
    ADD CONSTRAINT metadata_namespace_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2582 (class 2606 OID 38786)
-- Dependencies: 2025 2477 2021
-- Name: metadata_tmg_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_tmg
    ADD CONSTRAINT metadata_tmg_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2583 (class 2606 OID 38791)
-- Dependencies: 2489 2025 2048
-- Name: metadata_tmg_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_tmg
    ADD CONSTRAINT metadata_tmg_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2584 (class 2606 OID 38801)
-- Dependencies: 2026 2477 2021
-- Name: metadata_xlink_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY metadata_xlink
    ADD CONSTRAINT metadata_xlink_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2585 (class 2606 OID 38811)
-- Dependencies: 2483 2028 2030
-- Name: service_datasetroot_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_datasetroot
    ADD CONSTRAINT service_datasetroot_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2586 (class 2606 OID 38821)
-- Dependencies: 2028 2032 2483
-- Name: service_property_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_property
    ADD CONSTRAINT service_property_service_id_fkey FOREIGN KEY (service_id) REFERENCES service(service_id);


--
-- TOC entry 2587 (class 2606 OID 38826)
-- Dependencies: 2028 2034 2483
-- Name: service_service_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_service
    ADD CONSTRAINT service_service_child_id_fkey FOREIGN KEY (child_id) REFERENCES service(service_id);


--
-- TOC entry 2588 (class 2606 OID 38831)
-- Dependencies: 2034 2028 2483
-- Name: service_service_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY service_service
    ADD CONSTRAINT service_service_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES service(service_id);


--
-- TOC entry 2589 (class 2606 OID 38841)
-- Dependencies: 2049 2048 2489
-- Name: tmg_authority_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_authority
    ADD CONSTRAINT tmg_authority_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2590 (class 2606 OID 38851)
-- Dependencies: 2051 2048 2489
-- Name: tmg_contributor_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_contributor
    ADD CONSTRAINT tmg_contributor_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2592 (class 2606 OID 38866)
-- Dependencies: 2054 2053 2495
-- Name: tmg_creator_contact_tmg_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator_contact
    ADD CONSTRAINT tmg_creator_contact_tmg_creator_id_fkey FOREIGN KEY (tmg_creator_id) REFERENCES tmg_creator(tmg_creator_id);


--
-- TOC entry 2593 (class 2606 OID 38876)
-- Dependencies: 2056 2053 2495
-- Name: tmg_creator_name_tmg_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator_name
    ADD CONSTRAINT tmg_creator_name_tmg_creator_id_fkey FOREIGN KEY (tmg_creator_id) REFERENCES tmg_creator(tmg_creator_id);


--
-- TOC entry 2591 (class 2606 OID 38881)
-- Dependencies: 2053 2048 2489
-- Name: tmg_creator_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_creator
    ADD CONSTRAINT tmg_creator_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2594 (class 2606 OID 38891)
-- Dependencies: 2059 2048 2489
-- Name: tmg_dataformat_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_dataformat
    ADD CONSTRAINT tmg_dataformat_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2595 (class 2606 OID 38901)
-- Dependencies: 2061 2048 2489
-- Name: tmg_datasize_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_datasize
    ADD CONSTRAINT tmg_datasize_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2596 (class 2606 OID 38911)
-- Dependencies: 2063 2048 2489
-- Name: tmg_datatype_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_datatype
    ADD CONSTRAINT tmg_datatype_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2597 (class 2606 OID 38921)
-- Dependencies: 2065 2048 2489
-- Name: tmg_date_threddsmetadatagroup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_date
    ADD CONSTRAINT tmg_date_threddsmetadatagroup_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2599 (class 2606 OID 38936)
-- Dependencies: 2068 2067 2509
-- Name: tmg_documentation_namespace_tmg_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation_namespace
    ADD CONSTRAINT tmg_documentation_namespace_tmg_documentation_id_fkey FOREIGN KEY (tmg_documentation_id) REFERENCES tmg_documentation(tmg_documentation_id);


--
-- TOC entry 2598 (class 2606 OID 38941)
-- Dependencies: 2067 2048 2489
-- Name: tmg_documentation_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation
    ADD CONSTRAINT tmg_documentation_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2600 (class 2606 OID 38951)
-- Dependencies: 2071 2067 2509
-- Name: tmg_documentation_xlink_tmg_documentation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_documentation_xlink
    ADD CONSTRAINT tmg_documentation_xlink_tmg_documentation_id_fkey FOREIGN KEY (tmg_documentation_id) REFERENCES tmg_documentation(tmg_documentation_id);


--
-- TOC entry 2602 (class 2606 OID 38966)
-- Dependencies: 2074 2073 2515
-- Name: tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_eastwest
    ADD CONSTRAINT tmg_geospatialcoverage_eastwest_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2603 (class 2606 OID 38976)
-- Dependencies: 2076 2073 2515
-- Name: tmg_geospatialcoverage_name_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_name
    ADD CONSTRAINT tmg_geospatialcoverage_name_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2604 (class 2606 OID 38986)
-- Dependencies: 2078 2073 2515
-- Name: tmg_geospatialcoverage_northsout_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_northsouth
    ADD CONSTRAINT tmg_geospatialcoverage_northsout_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2601 (class 2606 OID 38991)
-- Dependencies: 2073 2048 2489
-- Name: tmg_geospatialcoverage_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage
    ADD CONSTRAINT tmg_geospatialcoverage_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2605 (class 2606 OID 39001)
-- Dependencies: 2073 2081 2515
-- Name: tmg_geospatialcoverage_updown_tmg_geospatialcoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_geospatialcoverage_updown
    ADD CONSTRAINT tmg_geospatialcoverage_updown_tmg_geospatialcoverage_id_fkey FOREIGN KEY (tmg_geospatialcoverage_id) REFERENCES tmg_geospatialcoverage(tmg_geospatialcoverage_id);


--
-- TOC entry 2606 (class 2606 OID 39011)
-- Dependencies: 2083 2048 2489
-- Name: tmg_keyword_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_keyword
    ADD CONSTRAINT tmg_keyword_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2607 (class 2606 OID 39016)
-- Dependencies: 2085 2021 2477
-- Name: tmg_metadata_metadata_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_metadata
    ADD CONSTRAINT tmg_metadata_metadata_id_fkey FOREIGN KEY (metadata_id) REFERENCES metadata(metadata_id);


--
-- TOC entry 2608 (class 2606 OID 39021)
-- Dependencies: 2085 2048 2489
-- Name: tmg_metadata_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_metadata
    ADD CONSTRAINT tmg_metadata_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2609 (class 2606 OID 39031)
-- Dependencies: 2086 2048 2489
-- Name: tmg_project_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_project
    ADD CONSTRAINT tmg_project_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2610 (class 2606 OID 39041)
-- Dependencies: 2088 2048 2489
-- Name: tmg_property_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_property
    ADD CONSTRAINT tmg_property_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2612 (class 2606 OID 39056)
-- Dependencies: 2091 2090 2531
-- Name: tmg_publisher_contact_tmg_publisher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher_contact
    ADD CONSTRAINT tmg_publisher_contact_tmg_publisher_id_fkey FOREIGN KEY (tmg_publisher_id) REFERENCES tmg_publisher(tmg_publisher_id);


--
-- TOC entry 2613 (class 2606 OID 39066)
-- Dependencies: 2090 2093 2531
-- Name: tmg_publisher_name_tmg_publisher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher_name
    ADD CONSTRAINT tmg_publisher_name_tmg_publisher_id_fkey FOREIGN KEY (tmg_publisher_id) REFERENCES tmg_publisher(tmg_publisher_id);


--
-- TOC entry 2611 (class 2606 OID 39071)
-- Dependencies: 2048 2489 2090
-- Name: tmg_publisher_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_publisher
    ADD CONSTRAINT tmg_publisher_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2614 (class 2606 OID 39081)
-- Dependencies: 2096 2048 2489
-- Name: tmg_servicename_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_servicename
    ADD CONSTRAINT tmg_servicename_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2616 (class 2606 OID 39096)
-- Dependencies: 2539 2099 2098
-- Name: tmg_timecoverage_duration_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_duration
    ADD CONSTRAINT tmg_timecoverage_duration_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2617 (class 2606 OID 39106)
-- Dependencies: 2098 2101 2539
-- Name: tmg_timecoverage_end_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_end
    ADD CONSTRAINT tmg_timecoverage_end_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2618 (class 2606 OID 39116)
-- Dependencies: 2539 2098 2103
-- Name: tmg_timecoverage_resolution_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_resolution
    ADD CONSTRAINT tmg_timecoverage_resolution_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2619 (class 2606 OID 39126)
-- Dependencies: 2098 2105 2539
-- Name: tmg_timecoverage_start_tmg_timecoverage_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage_start
    ADD CONSTRAINT tmg_timecoverage_start_tmg_timecoverage_id_fkey FOREIGN KEY (tmg_timecoverage_id) REFERENCES tmg_timecoverage(tmg_timecoverage_id);


--
-- TOC entry 2615 (class 2606 OID 39131)
-- Dependencies: 2489 2098 2048
-- Name: tmg_timecoverage_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_timecoverage
    ADD CONSTRAINT tmg_timecoverage_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2620 (class 2606 OID 39141)
-- Dependencies: 2109 2048 2489
-- Name: tmg_variables_tmg_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables
    ADD CONSTRAINT tmg_variables_tmg_id_fkey FOREIGN KEY (tmg_id) REFERENCES tmg(tmg_id);


--
-- TOC entry 2621 (class 2606 OID 39146)
-- Dependencies: 2109 2111 2549
-- Name: tmg_variables_variable_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_variable
    ADD CONSTRAINT tmg_variables_variable_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2622 (class 2606 OID 39156)
-- Dependencies: 2549 2109 2113
-- Name: tmg_variables_variablemap_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_variablemap
    ADD CONSTRAINT tmg_variables_variablemap_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2623 (class 2606 OID 39166)
-- Dependencies: 2115 2549 2109
-- Name: tmg_variables_xlink_tmg_variables_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: cleaner
--

ALTER TABLE ONLY tmg_variables_xlink
    ADD CONSTRAINT tmg_variables_xlink_tmg_variables_id_fkey FOREIGN KEY (tmg_variables_id) REFERENCES tmg_variables(tmg_variables_id);


--
-- TOC entry 2628 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2011-08-16 14:03:28

--
-- PostgreSQL database dump complete
--

