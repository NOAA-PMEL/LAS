# TMAPJGOFS.pm
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPJGOFS.pm,v 1.5 2004/05/07 15:49:32 callahan Exp $

##
# This module inherits methods from <b>TMAP::DBI::Dataset</b> and allows
# access to the JGOFS database.  When used with custom.pl
# in the context of LAS it will generate the appropriate
# JGOFS request.  Methods in <b>TMAP::DBI::Dataset</b> will generate a 
# NetCDF file appropriate for use with Ferret and LAS.
package TMAP::JGOFS::Dataset;
@TMAP::JGOFS::Dataset::ISA = qw(TMAP::DBI::Dataset);

$TMAP::JGOFS::VERSION = "0.2";

use TMAPDate;

############################################################
#
# Methods for creation and destruction.
#
############################################################

##
# Create a connection to a JGOFS database.  A log of the
# session will be saved in 'las/server/log/JGOFS_request.txt'.
# @param DBConfig configuration hash -- all database specific properties
sub new {
  my ($class, $DBIConfig) = @_;
  my $self = {};
  my $config = $self->{config} = $DBIConfig;

  my $logfile = $self->{log_file} = "log/JGOFS_request.txt";
  open DEBUG, ">$logfile" or
        die "Can't open $logfile";

  bless $self, $class;
}

##
# @private
sub DESTROY {
  my $self = shift;
#  $self->{dbh}->disconnect if $self->{dbh};
  close DEBUG;
}

##
# @private
sub disconnect {
  my $self = shift;
#  $self->{dbh}->disconnect if $self->{dbh};
}

############################################################
#
# Methods overriding those in TMAPDBI.pm
#
############################################################

##
# Constraints for the JGOFS server are concatenated
# with comma rather than ' AND '.
# @param constraint_values array of constraint components [variable op value]
# @return driver specific string used in the output plot labeling
sub addConstraint {
  my ($self, @constraint_values) = @_;

  my $constraint = join('',@constraint_values);

  if (!$self->{constraint}) {
    $self->{constraint} .= $constraint;
  } else {
    $self->{constraint} .= ",$constraint";
  }

  return $constraint;
}

##
# Add axis constraints to the (potentially null) constraint
# that will be part of the JGOFS query.
# <p>
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
    $self->{constraint} .= ",";
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

  if ($xhi < $xlo) {
    $self->{constraint} = "$x<=$xhi|$x>=$xlo";
  } else {
    $self->{constraint} .= "$x>=$xlo,$x<=$xhi";
  }
  $self->{constraint} .= ",$y>=$ylo,$y<=$yhi";
  $self->{constraint} .= ",$z>=$zlo,$z<=$zhi";

  my $date_lo = new TMAP::Date($tlo);
  my $date_hi = new TMAP::Date($thi);
  my $tlo_reformatted = $date_lo->reformatLike($self->{config}->{time_sample});
  my $thi_reformatted = $date_hi->reformatLike($self->{config}->{time_sample});

  $self->{constraint} .= ",$t>=$tlo_reformatted,$t<=$thi_reformatted";
}

# This is what a JGOFS request and response looks like:
#
# bin/list -z -m -f -n '//usjgofs.whoi.edu/US_JGOFS/Southern_Ocean/ctd_southern(lon,lat,depth,date,sta,cast,temp,lon>=-175,lon<=-165,lat>=-74.0,lat<=-56.0,depth>=0,depth<=900,date>=19960101,date<=19981231,cruise_id=NBP-96_4)'
# #  version  03 July 2002  
# #  combined data object for Southern Ocean CTD profiles  
# #   
# date[units=NA],sta[units=NA],cast[units=NA],lon[units=NA],lat[units=NA],depth[units=meters
# ],temp[units=degrees_C]
# 19960906,1,1,-169.7557,-64.1155,2,-1.732
# 19960906,1,1,-169.7557,-64.1155,4,-1.732
# 19960906,1,1,-169.7557,-64.1155,5.9,-1.732
# 19960906,1,1,-169.7557,-64.1155,7.9,-1.735
# 19960906,1,1,-169.7557,-64.1155,9.9,-1.734
# 19960906,1,1,-169.7557,-64.1155,11.9,-1.734
# ...
#
# Note:  variables aren't necessarily returned in request order. 

# On January 17, 2003 the header looks like this:
#date[units=various],sta[units=dimensionless],cast[units=dimensionless],lon[units=decimal_degrees],lat[units=decimal_degrees],depth[units=meters],NO3[units=micromoles/liter]


# This subroutine is called by getData below.
# It is also called in custom.pl and used to create a MD5 digest
# string for the data outputfile.
##
# Return the query portion of the JGOFS request URL.
# @return query portion of the JGOFS URL string
sub getQueryString {
  my ($self) = @_;

  my $vars = "";
  foreach my $var (sort keys %{$self->{variables}}) {
    $vars .= "$self->{variables}->{$var}->{name},";
  }
  my $queryString = $vars . $self->{constraint};

  return $queryString;
}

##
# JGOFS specific code to parse the results of
# <pre>
#   bin/list -z -m -f -n ~JGOFS_URL~
# </pre>
# convert ascii time strings into hours since time_origin
# and load the data into the data arrays.
# <p>
# To access the JGOFS database, datasets must be configured
# to use the JGOFS 'list' command and this command must be
# available from the las/server/ directory.
sub getData {
  my ($self) = @_;

  $self->startTimerInternal();

  my $url = "//" . $self->{config}->{db_host} . "/" . $self->{config}->{db_name} .
            "/" . $self->{config}->{dsetname};

  # The JGOFS server is guaranteed to have the following two variables.
  # These are used to create a unique ID for each profile.
  $self->SUPER::setVariableAttributes("sta","Station","");
  $self->SUPER::setVariableAttributes("cast","Cast","");

  my $queryString = $self->getQueryString();
  my $request = $url . "(" . $queryString . ")";

  my $exe = $self->{config}->{executable};
  die "Can't read file: $exe" if ! -r $exe;

  $request =~ tr/\042//d;		# 040506.clc. JGOFS request constraints must not be quoted (octal 042)
  my $command = "$exe -z -m -f -n '$request'";

  print DEBUG "$command\n";

  open IN, "$command|" or die "Can't execute file: $exe";

  my @variables = ();
  my $length = 0;

  # There are six mandatory variables:  date,sta,cast,lon,lat,depth
  # We are interested in filtering out lines where the user selected
  # variables are all missing.
  #
  my $all_missing = "";
  my $num_vars = scalar(keys(%{$self->{variables}})) - 6;
  for (my $i=0; $i<$num_vars; $i++) {
    $all_missing .= ',' . $self->{config}->{missing};
  }


  while(<IN>) {

    chomp;

    die "Bad data request, JGOFS server returned: $_" if /Bad Name:/;
    next if /#/;
    next if /$all_missing/;  # Skip any line with all missing values

    my @values = split(',');


    if (/\[units=/) {
      foreach my $var_string (@values){
    	my ($var,$units) = $var_string =~ /(\w+)\[units=(.*)\]/;
        if ($units ne "NA" &&
            $units !~ /various/ &&
            $units ne "YYYYMMDD" &&
            $units ne "file dependent" &&
            $units ne "dimensionless" &&
            $units ne "decimal_degrees" ) {
          $self->{variables}->{$var}->{units} = $units;
        }
        push(@variables, $var);
      }
    } else {
      for (my $i=0; $i<=$#variables; $i++) {
        push(@{$self->{variables}->{$variables[$i]}->{data}}, $values[$i]);
      }
      $length++;
    }
  }

  $self->{length} = $length;
  close IN;

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to get $length lines of data from the database\n";

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

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to convert ascii time strings to hours.\n";

  # Now that we've made the request we can add a new variable that we will 
  # synthesize from 'sta'(tion) and 'cast'.   This variable will be detected
  # as the profID variable by the writeCDF() method in TMAP::DBI.

  $self->setVariableAttributes('sta_cast', 'Profile ID', "none",$self->{config}->{missing});
  $self->{variables}->{sta_cast}->{profID} = 1;

  my ($sta,$cast) = "";
  foreach (my $i=1; $i<=$self->{length}; $i++) {
    $sta = sprintf("%03d",$self->{variables}->{sta}->{data}[$i-1]);
    $cast = sprintf("%03d",$self->{variables}->{cast}->{data}[$i-1]);
    $self->{variables}->{sta_cast}->{data}[$i-1] = "$sta$cast";
  }

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to create a synthetic 'PROF_ID' variable.\n";

}

1;
