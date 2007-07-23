# TMAPDapper.pm
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPDapper.pm,v 1.7 2004/06/02 20:56:35 webuser Exp $

##
# This module inherits methods from <b>TMAP::DBI::Dataset</b> and allows
# access to the EPIC Dapper OPeNDAP Sequence server. 
# When used with custom.pl in the context of LAS it will
# generate the appropriate Dapper request.
# Methods in <b>TMAP::DBI::Dataset</b> will generate a 
# NetCDF file appropriate for use with Ferret and LAS.
package TMAP::Dapper::Dataset;
@TMAP::Dapper::Dataset::ISA = qw(TMAP::DBI::Dataset);
$TMAP::Dapper::VERSION = "0.1";


use Date::Calc qw(Date_to_Time Time_to_Date);
use TMAPDate;

############################################################
#
# Methods for creation and destruction.
#
############################################################

##
# Create a connection to a Dapper EPIC database.  A log of the
# session will be saved in 'las/server/log/Dapper_request.txt'.
# @param DBConfig configuration hash -- all database specific properties
sub new {
  my ($class, $DBIConfig) = @_;
  my $self = {};
  my $config = $self->{config} = $DBIConfig;

  $self->{log_file} = "log/Dapper_request.txt";
  my $logfile = $self->{log_file};
  open DEBUG, ">$logfile" or die "Can't open $logfile";
  my $last = select DEBUG;
  $| = 1; #flush files open for output to enable output as we go
  select $last;
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
# Constraints for the Dapper server are concatenated
# with ampersand rather than ' AND '.
# @param constraint_values array of constraint components [variable op value]
# @return driver specific string used in the output plot labeling
sub addConstraint {
  my ($self, @constraint_values) = @_;

  my $constraint = join('',@constraint_values);

  if (!$self->{constraint}) {
    $self->{constraint} .= $constraint;
  } else {
    $self->{constraint} .= "&$constraint";
  }

  return $constraint;
}

##
# Add axis constraints to the (potentially null) constraint
# that will be part of the Dapper query.
#
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
    $self->{constraint} .= "&";
  } else {
    $self->{constraint} = "&";
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

# JC_TODO: The Dapper server does not support OR logic in constraints
# JC_TODO: This could be a problem if data ever crosses the 'branch cut'.
#
# If the 'xhi < xlo' case ever arises we will have to make two separate
# requres for the data. The Dapper DAS contains an attribute (lon_range)
# that might help decide what to use, but Dapper doesn't handle modulo
# semantics.
#
#  if ($xhi < $xlo) {
#    $self->{constraint} .= "$x<=$xhi&$x>=$xlo";
#  } else {
    $self->{constraint} .= "$x>=$xlo&$x<=$xhi";
#  }

  $self->{constraint} .= "&$y>=$ylo&$y<=$yhi";
  $self->{constraint} .= "&$z>=$zlo&$z<=$zhi";

  my $date_lo = new TMAP::Date($tlo);
  my $date_hi = new TMAP::Date($thi);
  my $tlo_reformatted = 1000*(Date_to_Time($date_lo->{year},$date_lo->{month},$date_lo->{day},$date_lo->{hour},$date_lo->{min},$date_lo->{sec}));
  my $thi_reformatted = 1000*(Date_to_Time($date_hi->{year},$date_hi->{month},$date_hi->{day},$date_hi->{hour},$date_hi->{min},$date_hi->{sec}));

  $self->{constraint} .= "&$t>=$tlo_reformatted&$t<=$thi_reformatted";
}


# This subroutine is called by getData below.
# It is also called in custom.pl and used to create a MD5 digest
# string for the data outputfile.
##
# Return the query string that is sent to Dapper
# @return query portion of the Dapper URL string
sub getQueryString {
  my ($self) = @_;

  my $vars = "";
  my @var_array = ();

  my $x = $self->{config}->{longitude};
  my $y = $self->{config}->{latitude};
  my $z = $self->{config}->{depth};
  my $t = $self->{config}->{time};

  # The Dapper server supports metadata only requests.
  # Generate a request for metadata only variables if this product
  # only requires metadata and if the dataset has been configured
  # to respond to metadata only requests.

  if ( !$self->{isMetaOnly} || !$self->{config}->{metadata} ) {
    @var_array = (sort keys %{$self->{variables}});
  } else {
    my @axes = split(',',$self->{config}->{metadata});
     my $metadata = $self->{config}->{metadata};
     print DEBUG "config->{metadata} = \"$metadata\"\n";
    foreach my $axis (@axes) {
      if ($axis eq 'x') {
        push(@var_array,$x);
      } elsif ($axis eq 'y') {
        push(@var_array,$y);
      } elsif ($axis eq 'z') {
        push(@var_array,$z);
      } elsif ($axis eq 't') {
        push(@var_array,$t);
      }
    }
  }

  foreach my $var (@var_array) {
    $vars .= "$self->{variables}->{$var}->{name},";
  }
  my $queryString = $vars . "_id" . $self->{constraint};

  return $queryString;
}

##
# Dapper specific code to parse the results of 
# <pre>
#   asciival ~Dapper_URL~
# </pre>
# and load the data into the data arrays.
sub getData {
  my ($self) = @_;

  $self->startTimerInternal();

  # Create asciival request to Dapper server
  # url+file+vars+constraints

  my $url = $self->{config}->{db_host} . "/" . $self->{config}->{db_name} .
            "/" . $self->{config}->{dsetname};

  my $queryString = $self->getQueryString();
  my $request = $url . "?" . $queryString;
  my $exe = $self->{config}->{executable};

  die "Can't read file: $exe" if ! -r $exe;

  my $command = "$exe '$request'";
  print DEBUG "$command\n";

  # Make the asciival request to the Dapper Server.  Always get depth for now (meta-only does not apply)
  # Need to reformat the die response string or send back WriteCDFFile null query string
  my $response = readpipe("$command") or die "Can't execute file: $exe";

  my $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to get data from the database\n";

  # Typical asciival response looks like
  # Dataset: race_beringsea_prof_mbt
  # location.lat, location.lon, location.time, location._id, location.profile.depth, location.profile.T_20
  # 60.69, 181.81, 806310240000, 333, 0, 7.7
  # 60.69, 181.81, 806310240000, 333, 1, 7.7
  # 60.69, 181.81, 806310240000, 333, 2, 7.7
  # 60.69, 181.81, 806310240000, 333, 3, 7.7
  # 60.69, 181.81, 806310240000, 333, 4, 7.7
  # 60.69, 181.81, 806310240000, 333, 5, 7.7
  # 60.69, 181.81, 806310240000, 333, 6, 7.7
  # 60.69, 181.81, 806310240000, 333, 7, 7.7
  # [...]

  # The _id variable is a mandatory variable
  # which needs no info from the LAS UI request, so it's set up here
  $self->setVariableAttributes('_id', 'Profile ID', "none",$self->{config}->{missing});
  $self->{variables}->{_id}->{profID} = 1;

  my @response = split("\n",$response);
  my @variables = ();
  my @values = ();
  my $count=0;

  foreach my $line (@response){
    $line =~ s/\s//g;
    @values = split(',',$line);

    if($count == 0){
      if (($dset_name) = $line =~/^Dataset:(\S+)$/) {
        # This is the same name as used for the 'file' attribute in the dataset.xml file.
      } else {
        die "Dapper server returned: $response\n";
      }
    }elsif($count == 1){
      foreach my $var (@values) {
        my ($varname) = $var =~ /location.*\.(\w+)$/;
        push(@variables,$varname);
      }
    }else{
      for (my $i=0; $i<=$#variables; $i++) {
        push(@{$self->{variables}->{$variables[$i]}->{data}}, $values[$i]);
      }
    }
    $count++;
  }

  if ( $count<3 ) {
      die "Dapper server returned: $response\n";
  }

  $self->{length} = $count-2;


  $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to parse $count lines of Dapper output\n";

  foreach my $var (@variables){
      my $bad_count = 0;
      for (my $i=0;$i<$self->{length};$i++){
          if ($self->{variables}->{$var}->{data}->[$i] =~ m/^nan$/){
              $self->{variables}->{$var}->{data}->[$i] = $self->{config}->{missing};
              $bad_count++;
          }
      }
      if ($bad_count == $self->{length}){
          my $message = "The variable '$self->{variables}->{$var}->{long_name}' contained no valid data.\n";
          $message .= "You may widen the search for data by expanding the region of space or time.\n";
          die "$message";
      }
  }

  $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to convert 'nan' to missing value flag.\n";

  # The Dapper server returns milliseconds since 1970
  # Here we set the $time_origin and convert to our own internal
  # 'hours since $time_origin'.

  $self->{time_origin} = new TMAP::Date("1970-01-01 00:00:00");

  my $t = $self->{config}->{time};
  my $tref = $self->{variables}->{$t};

  my $min = 1000000000000000;
  my $max = 0;
  if ($tref) {
    my @hours = ();
    my $secs = 0;
    foreach my $time (@{$tref->{data}}) {
      $secs = $time / 1000;
      if ($secs < $min) {
        $min = $secs;
      }
      if ($secs > $max) {
        $max = $secs;
      }
      push(@hours, $secs / 3600);
    }
    @{$tref->{data}} = ();
    push(@{$tref->{data}}, @hours);
  }

  $elapsed = $self->getElapsedInternal();
  print DEBUG "TMAP::Dapper::getData(): $elapsed secs to calc min and max and convert milliseconds to hours.\n";

  my ($year, $mon, $day, $hr, $min, $sec) = Time_to_Date($min);
  my $start = new TMAP::Date("$year-$mon-$day $hr:$min:$sec");
  ($year, $mon, $day, $hr, $min, $sec) = Time_to_Date($max);
  my $end = new TMAP::Date("$year-$mon-$day $hr:$min:$sec");
  $self->setTimeRange($start,$end);


}


1;

