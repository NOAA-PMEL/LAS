# TMAPDBI.pl
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPDBI.pm,v 1.11.4.3 2005/06/10 23:54:56 callahan Exp $

# Objects for handling conversions from databases to netCDF.
#
# TODO -- appropriate error logging
# TODO -- limit max data return size

use strict;
use TMAPDate;
use DBI;
use NetCDF;
use Time::HiRes;


package TMAP::DBI;
$TMAP::DBI::VERSION = "0.2";

##
# Base class driver for datasets that require a connection to external
# software systems.
# <p>
# Typically, this will be some sort of relational database and
# support is provided for these but other systems are possible.
# See <b>TMAP::WODB</b> as an example of a non-SQL data access system.
# <p>
# This class should provide enough functionality to enable
# connections to SQL databases that are supported by the
# perl DBI module.  Others systems can be supported by 
# subclassing this class (i.e. writing a driver) and overriding
# the following methods:
# <pre>
#   DESTROY( )
#   addAxisConstraints( )
#   addConstraint( )
#   disconnect( )
#   getData( )
#   getQueryString( )
#   new( )
#   setTable( )
# </pre>
# Each dataset .xml file must contain 'database_access' 
# configuration under the 'properties' tag telling LAS 
# which driver to use for external data access and setting
# various properties that will be used.  See the
# dataset .xml files in <b>las/contrib/wodb/product_server</b>
# for examples.  When a new <b>TMAP::DBI::Dataset</b> object
# is created, this configuration information is passed in as a 
# 'configuration hash'.
# 
package TMAP::DBI::Dataset;

############################################################
#
# Methods for creation and destruction.
#
############################################################


##
# Create a connection to a relational database.  A log of the
# session will be saved in 'las/server/log/mysql_request.txt'.
# @param DBConfig configuration hash -- all database specific properties
sub new {
  my ($class, $DBConfig) = @_;
  my $self = {};
  my $dbh;
  my $config = $self->{config} = $DBConfig;

  my $dburl = "DBI:" . $config->{db_type} . ":" . $config->{db_name} . ":" .
              $config->{db_host};
  my $login = $config->{db_login};
  my $passwd = $config->{db_passwd};
  $dbh = $self->{dbh} = DBI->connect($dburl, $login, $passwd,
                        {RaiseError => 1, PrintError=>1});

  my $logfile = $self->{log_file} = "log/mysql_request.txt";
  open DEBUG, ">$logfile" or
        die "Can't open $logfile";

  bless $self, $class;
}

BEGIN {
  import Carp;
}

##
# @private
sub DESTROY {
  my $self = shift;
  $self->{dbh}->disconnect if $self->{dbh};
  close DEBUG;
}

##
# Disconnect from a relational database.
sub disconnect {
  my $self = shift;
  $self->{dbh}->disconnect if $self->{dbh};
}

############################################################
#
# Methods to specify information about axes and variables
#
############################################################

##
# Set the attributes associated with an axis variable.
# @param orientation one of x/y/z/t
# @param var axis name as stored in the database
# @param name axis title
# @param units axis units
# @param [missing] missing value flag for the axis variable
sub setAxisAttributes {
  my ($self,$axis,$var,$name,$units,$missing) = @_; 

  die "TMAP::DBI::Dataset : You must include an orientation, variable, name and units\n" if (!$var || !$name || !$units);
  die "TMAP::DBI::Dataset : The orientation must be one of 'x', 'y', 'z', or 't'\n" if ($axis !~ /x|y|z|t/);

  $self->{variables}->{$var}->{axis} = $axis;
  $self->{variables}->{$var}->{name} = $var;
  $self->{variables}->{$var}->{long_name} = $name;
  $self->{variables}->{$var}->{units} = $units;
  $self->{variables}->{$var}->{missing} = $missing ? $missing : -1.0e+34;
  $self->{variables}->{$var}->{data} = [];
}


##
# Set the time origin.  The time axis will be defined
# as hours from this origin.  The netCDF file created
# by the writeCDF() method will use this origin to
# specify its 'time_origin' attribute.
# @param origin TMAP::Date object specifying the beginning of the requested time range.
sub setTimeOrigin {
  my ($self,$origin) = @_;

  die "Bad date: $origin" if !$origin->isOK;
  $self->{time_origin} = $origin;
}

##
# Set the time range for the request.
# If start equals end set point flag.
# @param start earliest time TMAP::Date object
# @param end latest time TMAP::Date object
sub setTimeRange {
  my ($self,$start,$end) = @_;

  die "Bad date: $start" if !$start->isOK;
  die "Bad date: $end" if !$end->isOK;
  if ($start->compareTo($end) == 0){
      $self->{time_point} = 1;
  }else{
      $self->{time_point} = 0;
  }

  $self->{start_time} = $start;
  $self->{end_time} = $end;
}


##
# Set the attributes associated with a data variable.
# @param var variable name as stored in the database
# @param name variable title
# @param units variable units
# @param [missing] missing value flag for the variable
# @param [string_length] defines this variable as a string variable of length string_length
sub setVariableAttributes {
  my ($self,$var,$name,$units,$missing,$string_length) = @_; 

  die "TMAP::DBI::Dataset : setVariableAttributes was called with no variable.\n" if (!$var);

  $self->{variables}->{$var}->{name} = $var;
  $self->{variables}->{$var}->{long_name} = $name ? $name : $var;
  $self->{variables}->{$var}->{units} = $units ? $units : "";
  $self->{variables}->{$var}->{missing} = $missing ? $missing : -1.0e+34;
  if ($string_length) {
    $self->{variables}->{$var}->{string_length} = $string_length;
  }
  $self->{variables}->{$var}->{data} = [];

  my $config = $self->{config};
  if ($config->{profID} eq $var) {
    $self->{variables}->{$var}->{profID} = 1;
  }

  if ($config->{cruiseID} eq $var) {
    $self->{variables}->{$var}->{cruiseID} = 1;
  }
}

##
# Set the attributes associated with a string variable.
# @param var variable name as stored in the database
# @param name variable title
# @param [string_length] defines this variable as a string variable of length string_length
sub setStringVariableAttributes {
  my ($self,$var,$name,$string_length) = @_; 

  die "TMAP::DBI::Dataset : setStringVariableAttributes was called with no variable.\n" if (!$var);

  $self->{variables}->{$var}->{name} = $var;
  $self->{variables}->{$var}->{long_name} = $name ? $name : $var;
  $self->{variables}->{$var}->{string_length} = $string_length;
  $self->{variables}->{$var}->{data} = [];
}

############################################################
#
# Methods for interacting with the SQL database.
#
############################################################

##
# Set the table to be used in the following query:
# <pre>
# SELECT ~vars~ FROM ~table~ WHERE ~constraint~
# </pre>
# @param table name of database table
sub setTable {
  my ($self, $table) = @_; 
  $self->{table} = $table;
}

##
# Initialize the constraint to be used in the following query:
# <pre>
# SELECT ~vars~ FROM ~table~ WHERE ~constraint~
# </pre>
# @param constraint string to be used in ~constraint~
sub setConstraint {
  my ($self, $constraint) = @_;

  $self->{constraint} = $constraint ? $constraint : "";
}

##
# Adds a constraint to the database query
# @param constraint_values array of constraint components [variable op value]
# @return driver specific string used in the output plot labeling
sub addConstraint {
  my ($self, @constraint_values) = @_;

  my $constraint = join('',@constraint_values);

  if (!$self->{constraint}) {
    $self->{constraint} .= $constraint;
  } else {
    $self->{constraint} .= " AND $constraint";
  }

  return $constraint;
}

##
# Add axis constraints to the (potentially null) constraint
# that will be part of the following query.
# <pre>
# SELECT ~vars~ FROM ~table~ WHERE ~constraint~
# </pre>
# Longitudes will be shifted to match the configured 'lon_domain'.
# @param xlo leftmost longitude in degrees
# @param xhi rightmost longitude in degrees
# @param ylo lowest latitude in degrees
# @param yhi highest latitude in degrees
# @param zlo lowest depth/height in z axis units
# @param zhi highest depth/height in z axis units
# @param tlo string representation of earliest time
# @param thi string representation of latest time
sub addAxisConstraints {
  my ($self, $xlo, $xhi, $ylo, $yhi, $zlo, $zhi, $tlo, $thi) = @_;

  my $x = $self->{config}->{longitude};
  my $y = $self->{config}->{latitude};
  my $z = $self->{config}->{depth};
  my $t = $self->{config}->{time};

  if ($self->{constraint}) {
    $self->{constraint} .= " AND ";
  } else {
    $self->{constraint} = "";
  }

  # Account for the configured longitude domain
  if ($xlo < $self->{config}->{lon_domain_lo}) { $xlo += 360; }
  if ($xhi < $self->{config}->{lon_domain_lo}) { $xhi += 360; }
  if ($xlo > $self->{config}->{lon_domain_hi}) { $xlo -= 360; }
  if ($xhi > $self->{config}->{lon_domain_hi}) { $xhi -= 360; }

  # Check for crossing the 'branch cut' and use the
  # appropriate logic
  #
  #             ------------------------------
  #             |                            |
  # xhi < xlo   |----x                 x-----|
  #             |                            |
  # xlo < xhi   |    x-----------------x     |
  #             |                            |
  #             ------------------------------

  my $var_count = 0;
  foreach my $var (keys %{$self->{variables}}){
      if (!$self->{variables}->{$var}->{axis} && 
          !$self->{variables}->{$var}->{profID} &&
          !$self->{variables}->{$var}->{cruiseID}){
          $var_count++;
          my $missing = $self->{config}->{missing};
          if ($var_count == 1){
	      $self->{constraint} .= "($var != $missing)";
          }else{
	      $self->{constraint} .= " AND ($var != $missing)";
	  }
      }
  }
  if ($xhi < $xlo) {
    $self->{constraint} .= " AND ($x<=$xhi OR $x>=$xlo)";
  } else {
    $self->{constraint} .= " AND ($x>=$xlo AND $x<=$xhi)";
  }

  $self->{constraint} .= " AND ($y>=$ylo AND $y<=$yhi)";
  if ($z) {
    $self->{constraint} .= " AND ($z>=$zlo AND $z<=$zhi)";
  }

  my $date_lo = new TMAP::Date($tlo);
  my $date_hi = new TMAP::Date($thi);
  my $tlo_formatted = $date_lo->reformatLike($self->{config}->{time_sample});
  my $thi_formatted = $date_hi->reformatLike($self->{config}->{time_sample});

  $self->{constraint} .= " AND ($t>='$tlo_formatted' AND $t<='$thi_formatted')";
}

##
# Set all data arrays to null.
sub flushData {
  my ($self) = @_;

  foreach my $var (sort keys %{$self->{variables}}) {
      @{$self->{variables}->{$var}->{data}} = ();
  }

  $self->{length} = 0;
}


# This subroutine is called by getData below.
# It is also called in custom.pl and used to create a MD5 digest
# string for the data outputfile.
##
# Return the query string of the form
# <pre>
# SELECT ~vars~ FROM ~table~ WHERE ~constraint~
# </pre>
sub getQueryString {
  my ($self) = @_;

  my $config = $self->{config};
  my $variables = "";
  my $queryString = "";

  foreach my $field (sort keys %{$self->{variables}}) {
    $variables .= "$field,";
  }
  chop $variables;

  $queryString = <<EOL;
SELECT $variables
FROM $self->{table}
WHERE $self->{constraint}
EOL
  return $queryString;
}

##
# Make data request and fill data arrays.  In addition
# to the variables specified in the LAS request, some
# drivers are capable of delivering additional metadata
# variables that are need for some output graphics.
#
sub getData {
  my ($self) = @_;

  $self->startTimerInternal();

  my $config = $self->{config};

  my @columns = ();
  foreach my $field (sort keys %{$self->{variables}}) {
    push(@columns, $field);
  }
  my $statement = $self->{statement} = $self->getQueryString();

  print DEBUG "$statement\n";

  my $sth = $self->{dbh}->prepare($statement);
  $sth->execute;
    
  my %results = ();
  @results{@columns} = ();
  $sth->bind_columns(map { \$results{$_} } @columns);

  my $length = 0;
  while ($sth->fetch) {
    foreach my $column (@columns){
      push(@{$self->{variables}->{$column}->{data}}, $results{$column});
    }
    $length++;
  }
  $self->{length} = $length;

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::DBI::getData(): $elapsed secs to get $length lines of data from the database\n";

# We need to pad string variables to match the specified <string_length>
# as the NetCDF::varput() will not do this for us.  The best way to this
# is with the perl pack() function. The "a#" template specifies that 
# strings should be padded with nulls out to {string_length}.

  foreach my $variable (sort keys %{$self->{variables}}) {
    my $varref = $self->{variables}->{$variable};
    my $packed_val = "";
    if ($varref->{string_length}) {
      my $template = "a" . $varref->{string_length};
      $varref->{packed_data} = [];
      for (my $i=0;$i<$length;$i++) {
        push(@{$varref->{packed_data}}, pack($template, $varref->{data}[$i])); 
      }
    }
  }
  
  # Convert ascii time strings into hours since $time_origin

  my $t = $self->{config}->{time};
  my $tref = $self->{variables}->{$t};
 
  if ($tref) {
    my ($date, $deltaHours);
    my @hours = ();
    foreach my $time (@{$tref->{data}}) {
      $date = new TMAP::Date($time);
      my $errorString = $date->errorString;
      die "Bad date: $errorString" if !$date->isOK;
      my $deltaHours = $self->{time_origin}->getDeltaHours($date);
      push(@hours, $deltaHours);
    }
    @{$tref->{data}} = ();
    push(@{$tref->{data}}, @hours);
  }

  $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::DBI::getData(): $elapsed secs to convert ascii strings to hours-since-origin.\n";

  # The following are additional variables that may be available
  # from some back end data access systems.  Where these are
  # available they can provide information that my be valuable
  # to those attempting to optimize access to data.  By default,
  # the values of these variables are set to '-1' so that Ferret
  # scripts can test for this and omit labels based on these.
  # Individual drivers (perl modules derived from this one) should
  # set these values where possible.

  # Number of profiles requested
  $self->{optimize}->{numprofs}->{val} = -1;
  $self->{optimize}->{numprofs}->{name} = 'NUMPROFS';
  $self->{optimize}->{numprofs}->{long_name} = 'number of profiles';
  $self->{optimize}->{numprofs}->{units} = 'unitless';

  # Total number of observations
  $self->{optimize}->{numobs}->{val} = $length;
  $self->{optimize}->{numobs}->{name} = 'NUMOBS';
  $self->{optimize}->{numobs}->{long_name} = 'number of observations';
  $self->{optimize}->{numobs}->{units} = 'unitless';


}

##
# Returns the length of the data arrays returned by the 
# server.
# @return length of data arrays
sub getLength {
  my ($self) = @_;

  return $self->{length};
}


############################################################
#
# Methods for generating a NetCDF file.
#
############################################################

# JC_TODO: We should differentiate between writing a regular
# JC_TODO: NetCDF file and a metadata-only file.  Using this
# JC_TODO: method for metadata-only requests results in bad
# JC_TODO: data being written.  Luckily it is never used and
# JC_TODO: Ferret doesn't seem to care.  But it should be fixed.
##
# Creates the LAS internal NetCDF file that is read by Ferret
# for generation of graphical products.  This is the file that
# is returned when netCDF data is requested.  In general, this
# method should not be overwritten as the uniformity of this
# intermediate file is what allows LAS to provide a uniform
# interface and output graphics to diverse back end databases.
# @param file relative path of the outputfile 
sub writeCDFFile {
  my ($self, $file) = @_;

  my $special_logfile = "log/DBI_special.txt";

  die "TMAP::DBI::Dataset : No file was specified in writeCDFFile\n" if !$file;

  if ($self->{length} == 0) {
    my $message = <<EOL;
No data was returned with the query:
$self->{statement}
EOL
    die "TMAP::DBI::Dataset : $message\n";
  }

  $self->startTimerInternal();

  my $prof_ID_exists = 0;
  my $cruise_ID_exists = 0;

  my ($xref,$yref,$zref,$tref,$pref,$cruiseref);
  my @varrefs = ();

  # Create two more variables that contain important metadata

  $self->{optimize}->{numprofs}->{val} = -1;
  $self->{optimize}->{numprofs}->{name} = 'NUMPROFS';
  $self->{optimize}->{numprofs}->{long_name} = 'number of profiles';
  $self->{optimize}->{numprofs}->{units} = 'unitless';

  $self->{optimize}->{numobs}->{val} = $self->{length};
  $self->{optimize}->{numobs}->{name} = 'NUMOBS';
  $self->{optimize}->{numobs}->{long_name} = 'number of observations';
  $self->{optimize}->{numobs}->{units} = 'unitless';

  foreach my $var (keys %{$self->{variables}}) {
    
    if ($var) {
      if      (!$self->{variables}->{$var}->{axis} && 
               !$self->{variables}->{$var}->{profID} &&
               !$self->{variables}->{$var}->{cruiseID}) {
        push(@varrefs,$self->{variables}->{$var});
      } elsif ($self->{variables}->{$var}->{axis} eq 'x') {
        $xref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 'y') {
        $yref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 'z') {
        $zref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 't') {
        $tref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{profID}) {
        $pref = $self->{variables}->{$var};
        $prof_ID_exists = 1;
      } elsif ($self->{variables}->{$var}->{cruiseID}) {
        $cruiseref = $self->{variables}->{$var};
        $cruise_ID_exists = 1;
      }
    }

  }

  # If this database doesn't return a variable to be used for 'profID'
  # we synthesize this variable here.  Data are assumed to be returned
  # one profile at a time with increasing ABS(depths).  This format is all
  # we've seen so far and it will be up to the individual driver to
  # put the data in this order if it is not already. 

  print DEBUG "TMAP::DBI::writeCDFFile(): prof_ID_exists = $prof_ID_exists\n";

  my $PROF_ID = 1;  # Make sure we have at least 1 profile even if no new ones are found

  if (!$prof_ID_exists) {

    $self->setVariableAttributes('PROF_ID', 'Profile ID', "", $self->{config}->{missing});
    $self->{variables}->{PROF_ID}->{profID} = 1;
    $pref = $self->{variables}->{PROF_ID};
    if (!$zref) {
      # If there is no Z axis then every data point is a new 'profile'.
      foreach (my $i=0; $i<$self->{length}; $i++) {
        $pref->{data}[$i] = $PROF_ID;
        $PROF_ID++;
      }
      $PROF_ID--; # To make sure $PROF_ID = number of profiles.
    } else {
      # If there is a Z axis, then increment $PROF_ID when ($z_new < $z_old)
      my ($z_new,$z_old) = abs($zref->{data}[0]);
      foreach (my $i=0; $i<$self->{length}; $i++) {
        $z_new = abs($zref->{data}[$i]);
        if ($z_new < $z_old) {
          $PROF_ID++;  
        }
        $z_old = $z_new;
        $pref->{data}[$i] = $PROF_ID;
      }
    }
    my $elapsed = $self->getElapsedInternal();
    print DEBUG "TMAP::DBI::writeCDFFile(): $elapsed secs to create a synthetic 'PROF_ID' variable\n";

  } else {

    my ($id_new,$id_old) = $pref->{data}[0];
    foreach (my $i=0; $i<$self->{length}; $i++) {
      $id_new = $pref->{data}[$i];
      if ($id_new != $id_old) {
        $id_old = $id_new;
        $PROF_ID++;  
      }
    }
    $PROF_ID--; # I don't understand why I need this but I do?!!  040506 JC

  }
    
  # Now put a value in for NUMPROFS
  $self->{optimize}->{numprofs}->{val} = $PROF_ID;


  # Definitions
  my $ftype   = NetCDF::FLOAT  + 0; # Force conversion from string
  my $dtype   = NetCDF::DOUBLE + 0; #
  my $ctype   = NetCDF::CHAR   + 0; #

  my $ncid  = NetCDF::create($file, NetCDF::WRITE);

  # Create the index axis
  my $dimid_X  = NetCDF::dimdef($ncid, "index", $self->{length});

  # Create a one dimensional axis for access information variables
  my $dimid_ONE = NetCDF::dimdef($ncid, "dim_one", "1");

  # Create an additional 'string dimension' axis for each string variable
  foreach my $varref (@varrefs) {
    if ($varref->{string_length}) {
      my $dimname = $varref->{name} . "_string_dim";
      $varref->{string_dim} = NetCDF::dimdef($ncid, $dimname, $varref->{string_length});
    }
  }

  # Variable attributes MUST be passed to attput() as reference
  # A temporary variable named $temp_string is defined here

  my $temp_string = "";

  # Create the trdim axis
  # Is Time a range or a point?
  my $dimid_TR;
  if(! $self->{time_point}){
      $dimid_TR  = NetCDF::dimdef($ncid, "trdim", "2");
  }else{
      $dimid_TR  = NetCDF::dimdef($ncid, "trdim", "1");
  }
  my $axisid_TR = NetCDF::vardef($ncid, "trdim", $dtype, [$dimid_TR]);
  $temp_string = "$tref->{units} since " .
                 $self->{time_origin}->reformatLike("1999-01-01 00:00:00");
  NetCDF::attput($ncid, $axisid_TR, "units", $ctype, \$temp_string);
  $temp_string = $self->{time_origin}->toFerretString;
  NetCDF::attput($ncid, $axisid_TR, "time_origin", $ctype, \$temp_string); 

  # Create the "trange" variable 
  my $varid_TR = NetCDF::vardef($ncid, "trange", $dtype, [$dimid_TR]);
  $temp_string = $tref->{units};
  NetCDF::attput($ncid, $varid_TR, "units", $ctype, \$temp_string);

  # Create the "PROF_ID" variable
  my $varid = NetCDF::vardef($ncid, "PROF_ID", $dtype, [$dimid_X]);
  $temp_string = "Profile ID";
  NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
  $temp_string = "unitless";
  NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
  NetCDF::attput($ncid, $varid, "missing_value", $ftype, $pref->{missing});
  $pref->{varid} = $varid;

  if ($cruise_ID_exists){
  # Create the "CRUISE_ID" variable
      my $varid = NetCDF::vardef($ncid, "CRUISE_ID", $dtype, [$dimid_X]);
      $temp_string = "Cruise ID";
      NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
      $temp_string = "unitless";
      NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
      NetCDF::attput($ncid, $varid, "missing_value", $ftype, $cruiseref->{missing});
      $cruiseref->{varid} = $varid;
  }
 
  # Create the "xax" variable
  my $varid = NetCDF::vardef($ncid, "xax", $ftype, [$dimid_X]);
  $temp_string = $xref->{long_name};
  NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
  $temp_string = $xref->{units};
  NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
  NetCDF::attput($ncid, $varid, "missing_value", $ftype, $xref->{missing});
  $xref->{varid} = $varid;

  # Create the "yax" variable
  $varid = NetCDF::vardef($ncid, "yax", $ftype, [$dimid_X]);
  $temp_string = $yref->{long_name};
  NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
  $temp_string = $yref->{units};
  NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
  NetCDF::attput($ncid, $varid, "missing_value", $ftype, $yref->{missing});
  $yref->{varid} = $varid;

  # Create the "zax" variable
  if ($zref) {
    $varid = NetCDF::vardef($ncid, "zax", $ftype, [$dimid_X]);
    $temp_string = $zref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
    $temp_string = $zref->{units};
    NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, $zref->{missing});
    $zref->{varid} = $varid;
  } else {
    # If the z variable is not part of this dataset, create it anyway and
    # make it all zeros.
    $varid = NetCDF::vardef($ncid, "zax", $ftype, [$dimid_X]);
    $temp_string = "Depth";
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
    $temp_string = "meters";
    NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, -1.0e+34);
    $zref->{varid} = $varid;
    $zref->{data} = [];
    foreach (my $i=1; $i<=$self->{length}; $i++) {
      $zref->{data}[$i-1] = 0;
    }
    my $elapsed = $self->getElapsedInternal();
    print DEBUG "TMAP::DBI::writeCDFFile(): $elapsed secs to create a synthetic 'zax' variable\n";
  } 

  # Create the "tax" variable
  if ($tref) {
    $varid = NetCDF::vardef($ncid, "tax", $dtype, [$dimid_X]);
    $temp_string = $tref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
    $temp_string = "$tref->{units} since " .
                   $self->{time_origin}->reformatLike("1999-01-01 00:00:00");
    NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
    $temp_string = $self->{time_origin}->toFerretString;
    NetCDF::attput($ncid, $varid, "time_origin", $ctype, \$temp_string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, $tref->{missing});
    $tref->{varid} = $varid;
    
  }

# TODO: It might be nicer to have all of the string variables stored in
# TODO: $self->{string_variables} and then have a separate section for
# TODO: writing these.  But this way works fine for now.

  # Create all of the data variables
  foreach my $varref (@varrefs) {
    my $varid;
    if ($varref->{string_length}) {
      $varid = NetCDF::vardef($ncid, $varref->{name}, $ctype, [$dimid_X,$varref->{string_dim}]);
    } else {
      $varid = NetCDF::vardef($ncid, $varref->{name}, $ftype, [$dimid_X]);
    }
    $temp_string = $varref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
    if (!$varref->{string_length}) {
      $temp_string = $varref->{units};
      NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
      NetCDF::attput($ncid, $varid, "missing_value", $ftype, $varref->{missing});
    }
    $varref->{varid} = $varid;
  }

  # Create the optimization variables
  foreach my $optvar (keys %{$self->{optimize}}){
    my $opt = $self->{optimize}->{$optvar};
    my $varid = NetCDF::vardef($ncid, $opt->{name}, $ftype,[$dimid_ONE]);
    $temp_string = $opt->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$temp_string);
    $temp_string = ($opt->{units});
    NetCDF::attput($ncid, $varid, "units", $ctype, \$temp_string);
    $opt->{varid} = $varid;
  }

  # End definition mode
  NetCDF::endef($ncid);

  # Write the trdim data
  my @trdim = ();
  my $deltaHours = $self->{time_origin}->getDeltaHours($self->{start_time});
  push(@trdim, $deltaHours);
  # Is Time a range or a point?
  if(! $self->{time_point}){
      $deltaHours = $self->{time_origin}->getDeltaHours($self->{end_time});
      push(@trdim, $deltaHours);
      NetCDF::varput($ncid, $axisid_TR, [0], [2], \@trdim);

      # Write the trange data 
      # (There isn't any, just need to write something to get access to the axis)
      NetCDF::varput($ncid, $varid_TR, [0], [2], \@trdim);

  }else{ # There is only one value for the TRDIM axis and TRANGE value

      NetCDF::varput($ncid, $axisid_TR, [0], [1], \@trdim);
      # Write the trange data 
      # (There isn't any, just need to write something to get access to the axis)
      NetCDF::varput($ncid, $varid_TR, [0], [1], \@trdim);
  }

  # Write the PROF_ID, lon, lat, depth and time data
  NetCDF::varput($ncid, $pref->{varid}, [0], [$self->{length}], \@{$pref->{data}});
  NetCDF::varput($ncid, $xref->{varid}, [0], [$self->{length}], \@{$xref->{data}});
  NetCDF::varput($ncid, $yref->{varid}, [0], [$self->{length}], \@{$yref->{data}});
  NetCDF::varput($ncid, $zref->{varid}, [0], [$self->{length}], \@{$zref->{data}});
  NetCDF::varput($ncid, $tref->{varid}, [0], [$self->{length}], \@{$tref->{data}});
  if ($cruise_ID_exists){
  # Write the CRUISE_ID
      NetCDF::varput($ncid, $cruiseref->{varid}, [0], [$self->{length}], \@{$cruiseref->{data}});
  }
  
  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::DBI::writeCDFFile(): $elapsed secs to write the metadata variables\n";

# Write all of the data variables

  foreach my $varref (@varrefs) {
    if ($varref->{string_length}) {
      NetCDF::varput($ncid, $varref->{varid}, [0,0], [$self->{length},$varref->{string_length}], \@{$varref->{packed_data}});
    } else {
      NetCDF::varput($ncid, $varref->{varid}, [0], [$self->{length}], \@{$varref->{data}});
    }
  }

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::DBI::writeCDFFile(): $elapsed secs to write the data variables\n";

  # One dimensional variables with information on data access statistics.
  foreach my $optvar (keys %{$self->{optimize}}){
    my $opt = $self->{optimize}->{$optvar};
    NetCDF::varput($ncid, $opt->{varid}, [0], [1], [$opt->{val}]);
  }

  NetCDF::close($ncid);
}

############################################################
#
# Miscellaneous methods 
#
############################################################


# Two methods for timing requests

##
# Starts an internal timer using the Time::HiRes module;
sub startTimer {
    my ($self) = @_;
    $self->{timestamp} = [Time::HiRes::gettimeofday];
}
  
##
# Starts an internal timer using the Time::HiRes module;
# @return seconds elapsed since last call to getElapsed() (or startTimer())
# 
sub getElapsed {
    my ($self) = @_;
    my $timestamp = $self->{timestamp};
    my $elapsed = Time::HiRes::tv_interval($timestamp);
    $self->{timestamp} = [Time::HiRes::gettimeofday];
    return $elapsed;
}


############################################################
#
# Methods for debugging
#
############################################################

##
# @private
sub tableSize {
  my ($self, $table) = @_;

  if ($self->{config}->{db_type} eq 'DBI') {
    my @values = $self->selectRow('count(*)', $table);
    return $values[0];
  }
}

##
# @private
sub dbExists {
  my ($self, $db) = @_;

  if ($self->{config}->{db_type} eq 'DBI') {
    my $rv = $self->{dbh}->prepare(qq(show databases like '$db'));
    $rv->execute;
    return $rv->rows;
  }
}

##
# @private
sub columnExists {
  my ($self, $table, $column) = @_;

  if ($self->{config}->{db_type} eq 'DBI') {
    my $rv = $self->{dbh}->prepare(qq(show columns from $table like '$column'));
    $rv->execute;
    return $rv->rows;
  }
}

# Two internal methods for timing requests

##
# @private
sub startTimerInternal {
    my ($self) = @_;
    $self->{timestamp_internal} = [Time::HiRes::gettimeofday];
}
  
##
# @private
sub getElapsedInternal {
    my ($self) = @_;
    my $timestamp_internal = $self->{timestamp_internal};
    my $elapsed = Time::HiRes::tv_interval($timestamp_internal);
    $self->{timestamp_internal} = [Time::HiRes::gettimeofday];
    return $elapsed;
}


1;
