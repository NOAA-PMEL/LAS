# TMAPOSMC.pm
# Copyright (c) 2004 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPOSMC.pm,v 1.1.2.1 2005/06/07 21:38:25 callahan Exp $

##
# This module inherits methods from <b>TMAP::DBI::Dataset</b> and allows
# access to the Oracle based OSMC database server at NOAA/NDBC.
# All methods defined here override those inherited from TMAP::DBI::Dataset.

package TMAP::OSMC::Dataset;
@TMAP::OSMC::Dataset::ISA = qw(TMAP::DBI::Dataset);
$TMAP::OSMC::VERSION = "0.1";


############################################################
#
# Methods for creation and destruction.
#
############################################################


##
# Create a connection to a relational database.  A log of the
# session will be saved in 'las/server/log/OSMC_request.txt'.
# @param DBConfig configuration hash -- all database specific properties
sub new {
  my ($class, $DBConfig) = @_;
  my $self = {};
  my $dbh;
  my $config = $self->{config} = $DBConfig;

  my $dburl = "dbi:Oracle:" . $config->{db_host};
  my $login = $config->{db_login};
  my $passwd = $config->{db_passwd};

# Connect to Oracle database

  $dbh = $self->{dbh} = DBI->connect($dburl, $login, $passwd,
             {
              AutoCommit => 0,
              PrintError => 0,
              RaiseError => 0
             } ) or die "Can't connect to the database: $DBI::errstr\n";

  my $logfile = $self->{log_file} = "log/OSMC_request.txt";
  open DEBUG, ">$logfile" or
        die "Can't open $logfile";

  bless $self, $class;
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

# NOTE: As described in getQueryString(), the table name must be
# NOTE: prefixed onto the longitude and latitude variables to
# NOTE: prevent 'column ambiguously defined' complaints from
# NOTE: Oracle.

  $x = "osmc_observation\.$x";
  $y = "osmc_observation\.$y";

  if ($self->{constraint}) {
    $self->{constraint} .= " AND ";
  } else {
    $self->{constraint} = "";
  }

# TODO: We should bring back the 'AND ~var~ is not null' logic.
# TODO: To do this we'll need to prefix the table names as in
# TODO: getQueryString() below.

#  my $var_count = 0;
#  foreach my $var (keys %{$self->{variables}}){
#      if (!$self->{variables}->{$var}->{axis} && 
#          !$self->{variables}->{$var}->{profID} &&
#          !$self->{variables}->{$var}->{cruiseID}){
#          $var_count++;
#          my $missing = $self->{config}->{missing};
#          if ($var_count == 1){
#	      $self->{constraint} .= "($var != $missing)";
#          }else{
#	      $self->{constraint} .= " AND ($var != $missing)";
#	  }
#      }
#  }

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

# TODO: If the missing value code above is resurrected then
# TODO: the X constraints will need an ' AND '.

  if ($xhi < $xlo) {
    $self->{constraint} .= "($x<=$xhi OR $x>=$xlo)";
  } else {
    $self->{constraint} .= "($x>=$xlo AND $x<=$xhi)";
  }

  $self->{constraint} .= " AND ($y>=$ylo AND $y<=$yhi)";
  if ($z) {
    $self->{constraint} .= " AND ($z>=$zlo AND $z<=$zhi)";
  }

  my $date_lo = new TMAP::Date($tlo);
  my $date_hi = new TMAP::Date($thi);
  my $tlo_formatted = $date_lo->reformatLike($self->{config}->{time_sample});
  my $thi_formatted = $date_hi->reformatLike($self->{config}->{time_sample});

  $self->{constraint} .= " AND osmc_observation.ob_date BETWEEN to_date('$tlo_formatted',\'YYYY-MM-DD HH24:MI:SS\') AND to_date('$thi_formatted',\'YYYY-MM-DD HH24:MI:SS\')";

}


##
# Adds a constraint to the database query
# @param constraint_values array of constraint components [variable op value]
# @return driver specific string used in the output plot labeling
sub addConstraint {
  my ($self, @constraint_values) = @_;

  my $constraint_name = "";
  my $constraint_type = "";
  my $constraint_value = "";


  while (@constraint_values) {
      $constraint_name = shift @constraint_values;
      $constraint_type = shift @constraint_values;
      $constraint_value = shift @constraint_values;
  }

  if ($constraint_value =~ /SHIP/) {
      $constraint_value = "SHIP";
  } elsif ($constraint_value =~ /DRIFTING/) {
      $constraint_value = "DRIFTING BUOY";
  } elsif ($constraint_value =~ /MOORED/) {
      $constraint_value = "MOORED BUOY";
  } elsif ($constraint_value =~ /CMAN/) {
      $constraint_value = "CMAN";
  }

  if ($constraint_value =~ /US/) {
      $constraint_value = "US";
  } elsif ($constraint_value =~ /FR/) {
      $constraint_value = "FR";
  } elsif ($constraint_value =~ /CA/) {
      $constraint_value = "CA";
  } elsif ($constraint_value =~ /JP/) {
      $constraint_value = "JP";
  } elsif ($constraint_value =~ /GB/) {
      $constraint_value = "GB";
  }


  my $constraint = "$constraint_name $constraint_type \'$constraint_value\'";

  if (!$self->{constraint}) {
    $self->{constraint} .= $constraint;
  } else {
    $self->{constraint} .= " AND $constraint";
  }

  return $constraint;
}



############################################################
#
# Methods for interacting with the SQL database.
#
############################################################

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

# NOTE: The names of the tables are prefixed onto the variable names
# NOTE: in the section immediately below.
# NOTE: 
# NOTE: Bugzilla entries #1265 #1266 describe the underlying issues
# NOTE: in Ferret and the LAS insitu scripts that prevent us from 
# NOTE: being able to use names like 'osmc_observation.sst' in the
# NOTE: dataset configuration file.

  foreach my $field (sort keys %{$self->{variables}}) {

      if ($field !~ /type/ && $field !~ /country/ && $field !~ /last_report/) {
	  $field = "osmc_observation\.$field";
	  if ($field =~ /ob_date/) {
	      $field = "to_char($field,'YYYYMMDDHH24MISS')";
	  }
      } else {
	  $field = "osmc_platform\.$field";
	  if ($field =~ /last_report/) {
	      $field = "to_char($field,'YYYYMMDDHH24MISS')";
	  }
      }
      $variables .= "$field,";
  }
  
  chop $variables;

# NOTE: The OSMC database stores information in two separate tables.
# NOTE: We need to access information from both tables and thus must
# NOTE: hardcode the FROM and WHERE clauses of the query statement.

  $queryString = <<EOL;
SELECT $variables
FROM osmc_observation, osmc_platform
WHERE osmc_observation.id = osmc_platform.id 
AND $self->{constraint}
ORDER by osmc_observation.id
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

  $self->setVariableAttributes('id', "Platform ID", "none", $self->{config}->{missing});
  $self->setVariableAttributes('type', "Platform TYPE", "none", $self->{config}->{missing});
  $self->setVariableAttributes('country', "Country", "none", $self->{config}->{missing}, 2);
  $self->setVariableAttributes('last_report', "Last Report", "none", $self->{config}->{missing});

  my @columns = ();
  foreach my $field (sort keys %{$self->{variables}}) {
    push(@columns, $field);
  }
  my $statement = $self->{statement} = $self->getQueryString();

  print DEBUG "$statement\n";

  my $sth = $self->{dbh}->prepare($statement) or die "Can't prepare SQL statement: $DBI::errstr\n";

  $sth->execute or die "Can't execute SQL statement: $DBI::errstr\n";
    
  my %results = ();
  @results{@columns} = ();
  $sth->bind_columns(map { \$results{$_} } @columns);

  my $length = 0;
  while ($sth->fetch) {
    foreach my $column (@columns){

# Add logic to number the platforms for the netCDF file, rather than trying to have strings
# *kob* 	
	if ($column =~ /type/) {
	    my $tmp_var = $results{$column};
	    for ($tmp_var) {
		$results{$column} = /SHIP/     ? 1 :
		                    /DRIFTING/ ? 2 :
				    /MOORED/   ? 3 :
				    /CMAN/     ? 4 : 0;
	    }
	}

# TODO: Need to determine whether the check below is necessary.  It may be expensive to 
# TODO: go through every single returned value.

# KEVIN: I think we need to check for missing values here.
      if (!$results{$column}) {
        #die "Missing value at row \"$length\", column \"$column\"";
        #push(@{$self->{variables}->{$column}->{data}}, $self->{variables}->{$var}->{missing});
        push(@{$self->{variables}->{$column}->{data}}, -1e+34);
      } else {
	  push(@{$self->{variables}->{$column}->{data}}, $results{$column});
      }
    }
    $length++;
  }
  $self->{length} = $length;

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::OSMC::getData(): $elapsed secs to get $length lines of data from the database\n";

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
  print DEBUG "TMAP::OSMC::getData(): $elapsed secs to convert ascii strings to hours-since-origin.\n";

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


1;
