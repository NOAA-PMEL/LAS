# TMAPWODB.pm
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPWODB.pm,v 1.12 2005/03/21 23:47:43 callahan Exp $

##
# This module inherits methods from <b>TMAP::DBI::Dataset</b> and allows
# access to the World Ocean DataBase dataset put out by NOAA/NODC.  When used with custom.pl
# in the context of LAS it will generate the appropriate
# WODB request.  Methods in <b>TMAP::DBI::Dataset</b> will generate a 
# NetCDF file appropriate for use with Ferret and LAS.
# <p>
# The WODB dataset was constructed by Joe Mclean at NOAA/PMEL/TMAP
# to provide quick access to the nine million profile database.
# It is constructed as a set of three, tiered NetCDF files rather
# than a software based database like MySQL or JGOFS.
package TMAP::WODB::Dataset;
@TMAP::WODB::Dataset::ISA = qw(TMAP::DBI::Dataset);

$TMAP::WODB::VERSION = "0.1";

use Time::HiRes qw ( gettimeofday tv_interval );


############################################################
#
# Methods for creation and destruction.
#
############################################################

##
# Create a connection to the WODB database.  A log of the
# session will be saved in 'las/server/log/JGOFS_request.txt'.
# @param DBConfig configuration hash -- all database specific properties
sub new {
  my ($class, $DBIConfig) = @_;
  my $self = {};
  my $config = $self->{config} = $DBIConfig;

  my $logfile = $self->{log_file} = "log/WODB_request.txt";
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
# Set the attributes associated with a data variable.
# @param var variable name as stored in the database
# @param name variable title
# @param units variable units
# @param [missing] missing value flag for the variable

# Overrides TMAP::DBI because we don't want to add 'CRUISE' as regular variable

sub setVariableAttributes {
  my ($self,$var,$name,$units,$missing) = @_; 

  die "TMAP::WODB::Dataset : setVariableAttributes was called with no variable.\n" if (!$var);

  my $config = $self->{config};
  if ($config->{cruiseID} ne $var){
      $self->{variables}->{$var}->{name} = $var;
      $self->{variables}->{$var}->{long_name} = $name ? $name : $var;
      $self->{variables}->{$var}->{units} = $units ? $units : "";
      $self->{variables}->{$var}->{missing} = $missing ? $missing : -1.0e+34;
      $self->{variables}->{$var}->{data} = [];
  }
  if ($config->{profID} eq $var) {
    $self->{variables}->{$var}->{profID} = 1;
  }

}

##
# Add a constraint to the WODB query.
# @param constraint_values array of constraint components [variable op value]
# @return driver specific string used in the output plot labeling
sub addConstraint {
  my ($self, $c_name, $c_op, $c_val) = @_;
  $self->{constraint}->{$c_name} = { 'c_op' => $c_op, 'c_val' => $c_val };
  $self->{constraint}->{$c_name}->{temp_array} = [];
  print DEBUG "Constraint: name,op,val: $c_name, $c_op, $c_val\n";

  my $constraint = $c_name . $c_op . $c_val;
  return $constraint;
}

##
# Add axis constraints to the (potentially null) constraint
# that will be part of the WODB query.
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

  print DEBUG "addAxisConstraints incoming lon domain: $xlo:$xhi\n";
  # Account for the configured longitude domain
  # The WODB server can handle longitudes 0:720
  if ($xlo < $self->{config}->{lon_domain_lo}) { $xlo += 360; }
  if ($xhi < $self->{config}->{lon_domain_lo}) { $xhi += 360; }
  if ($xlo > $self->{config}->{lon_domain_hi}) { $xlo -= 360; }
  if ($xhi > $self->{config}->{lon_domain_hi}) { $xhi -= 360; }

  # The findIndex() method below can handle $xhi > 360.

  if ($xhi <= $xlo) { $xhi += 360; }

  print DEBUG "addAxisConstraints final lon domain: $xlo:$xhi\n";

  $self->{xlo} = $xlo;
  $self->{xhi} = $xhi;
  $self->{ylo} = $ylo;
  $self->{yhi} = $yhi;
  $self->{zlo} = $zlo;
  $self->{zhi} = $zhi;

  my $date_lo = new TMAP::Date($tlo);
  my $date_hi = new TMAP::Date($thi);
  my $Jdays_lo = $self->findJday($date_lo->reformatLike("01-Jan-1999"));
  my $Jdays_hi = $self->findJday($date_hi->reformatLike("01-Jan-1999"));
  my $Jdays_origin = $self->findJday("01-Jan-1700");

  $self->{tlo} = $Jdays_lo - $Jdays_origin;
  $self->{thi} = $Jdays_hi - $Jdays_origin;

}

# This subroutine is called by getData below.
# It is also called in custom.pl and used to create a MD5 digest
# string for the data outputfile.
##
# Returns a unique query string for this request.
# @return unique query string
sub getQueryString {
  my ($self) = @_;

  my $config = $self->{config};
  my $variables = "";
  my $constraints = "";
  my $queryString = "";

  foreach my $field (sort keys %{$self->{variables}}) {
    $variables .= "$field,";
  }
  chop $variables;

  foreach my $c_name (sort keys %{$self->{constraint}}) {
      $constraints .= $c_name;
      $constraints .= $self->{constraint}->{$c_name}->{c_op};
      $constraints .= $self->{constraint}->{$c_name}->{c_val}."\n";
  }
  $queryString  = "VARS: $variables\n";
  $queryString .= "RANGE: x=$self->{xlo}:$self->{xhi},y=$self->{ylo}:$self->{yhi},z=$self->{zlo}:$self->{zhi},t=$self->{tlo}:$self->{thi}\n";
  $queryString .= "CONSTRAINTS: $constraints";
  $queryString .= "isMetaOnly=$self->{isMetaOnly}";

  print DEBUG "Unique Query string\n";
  print DEBUG "TMAPWODB: $queryString\n-------------------\n";
  return $queryString;
}

# TODO: I believe that multiple variables should be handled
# TODO: in the following manner.  Have the getData subroutine
# TODO: just be a loop through all the variables, each time
# TODO: calling getData_sub.  The first time through getData_sub
# TODO: all of the following should be stored: 
# TODO:   @lon, @lat, @jday, @depth, @data
# TODO: All subsequent times, only @data should be stored.

# NOTE: look at the way constraints are handled throughout
#       this code, it may be a better way than looping at
#       this level  *jm*

##
# WODB specific code to read data from the NetCDF files
# that together make up the TMAP WODB database.
sub getData {
  my ($self) = @_; 

  $self->startTimerInternal();

  my $first = 1;
  foreach my $var (sort keys %{$self->{variables}}) {
    if ($var ne 'x' && $var ne 'y' && $var ne 'z' && $var ne 't') {
      eval{
      $self->getData_sub($var, $first);
      };
      if ($@){
        $elapsed = $self->getElapsedInternal;
        print DEBUG "$elapsed secs elapsed to die\n";
        print DEBUG ("TMAP::WODB::getData: error: $@");
        die $@;
      }
      $first = 0;
    }
  }

}

##
# @private
sub getData_sub {
  my ($self,$var_name,$first) = @_; 
  my $metapointer_file  = 'data/wod_XYT_ptr.nc';
  my $meta_file         = 'data/wod_meta.nc';
  # jm - data_file set below, using variable name
  my $data_file         = '';
  my $err_code = 0;
  
  die "Can't read file: $metapointer_file" if ! -r $metapointer_file;
  die "Can't read file: $meta_file" if ! -r $meta_file;
  
  my $lon_lo = $self->{xlo};
  my $lon_hi = $self->{xhi};
  my $lat_lo = $self->{ylo};
  my $lat_hi = $self->{yhi};
  my $dep_lo = $self->{zlo};
  my $dep_hi = $self->{zhi};
  my $time_lo = $self->{tlo};
  my $time_hi = $self->{thi};

  # Use Time::HiRes module to log decimal seconds
  my $starttime  = [gettimeofday];
  $self->{t0} = [gettimeofday];
  my $elapsed;

  #  1. Process 3D XYT META_POINTER file
  #-------------------------------

  print DEBUG "x, y, t values: ";
  print DEBUG "[$lon_lo,$lon_hi]" ;
  print DEBUG "[$lat_lo,$lat_hi]" ;
  print DEBUG "[$time_lo,$time_hi]\n" ;

  print DEBUG "\nOpening and Reading meta pointer file $metapointer_file .....\n";
  my $ncid  = NetCDF::open($metapointer_file, NetCDF::NOWRITE);
   
  # Get dimension ID's
  my $XLON_id = NetCDF::varid($ncid,"XLON");
  my $YLAT_id = NetCDF::varid($ncid,"YLAT");
  my $TDATE_id = NetCDF::varid($ncid,"TDATE");
  my $TEDGES_id = NetCDF::varid($ncid,"TEDGES");
  
  # Get Dimension names and lengths
  my $XLON_len=0;
  my $YLAT_len=0;
  my $TDATE_len=0;
  my $TEDGES_len=0;
  my $XLON_name="";
  my $YLAT_name="";
  my $TDATE_name="";
  my $TEDGES_name="";

  NetCDF::diminq($ncid,$XLON_id,$XLON_name,$XLON_len);
  NetCDF::diminq($ncid,$YLAT_id,$YLAT_name,$YLAT_len);
  NetCDF::diminq($ncid,$TDATE_id,$TDATE_name,$TDATE_len);
  NetCDF::diminq($ncid,$TEDGES_id,$TEDGES_name,$TEDGES_len);

  # Get Dimension values
  my @xlons = ();
  my @ylats = ();
  my @tdates = ();
  my @tedges = ();
 
  NetCDF::varget($ncid,$XLON_id,0,$XLON_len,\@xlons);
  NetCDF::varget($ncid,$YLAT_id,0,$YLAT_len,\@ylats);
  NetCDF::varget($ncid,$TDATE_id,0,$TDATE_len,\@tdates);
  NetCDF::varget($ncid,$TEDGES_id,0,$TEDGES_len,\@tedges);
  
  # Calculate index and count from range args
  my $x_lo = $self->findIndex(@xlons,$lon_lo,0);
  my $x_hi;
  if ($lon_hi <= 360) {
    $x_hi = $self->findIndex(@xlons,$lon_hi,1);
  } else {
    $x_hi = $self->findIndex(@xlons,360,1);
  }
  my $y_lo = $self->findIndex(@ylats,$lat_lo,0);
  my $y_hi = $self->findIndex(@ylats,$lat_hi,1);
  my $t_lo = $self->findTindex(@tedges,$time_lo,0);
  my $t_hi = $self->findTindex(@tedges,$time_hi,1);
  my $x_cnt = $x_hi - $x_lo +1;
  my $y_cnt = $y_hi - $y_lo +1;
  my $t_cnt = $t_hi - $t_lo +1;

  # Get variable ID's
  my $NPROF_id = NetCDF::varid($ncid,"NPROF");
  my $META_POINTER_id  = NetCDF::varid($ncid,"META_POINTER");
  
  # define start and count vectors for reading data
  my @count = ($t_cnt,$y_cnt,$x_cnt);
  my @start = ($t_lo,$y_lo,$x_lo);
  
  # Get data values
  my @mpointer =();
  my @nprof = ();
  
  NetCDF::varget($ncid,$META_POINTER_id,\@start,\@count,\@mpointer);
  NetCDF::varget($ncid,$NPROF_id,\@start,\@count,\@nprof);
  
  if ($lon_hi > 360) {
    $x_lo = $self->findIndex(@xlons,0,0);
    $x_hi = $self->findIndex(@xlons,$lon_hi-360,1);
    $x_cnt = $x_hi - $x_lo +1;
    @count = ($t_cnt,$y_cnt,$x_cnt);
    @start = ($t_lo,$y_lo,$x_lo);
    my @mpointer_extra =();
    my @nprof_extra = ();
    NetCDF::varget($ncid,$META_POINTER_id,\@start,\@count,\@mpointer_extra);
    NetCDF::varget($ncid,$NPROF_id,\@start,\@count,\@nprof_extra);
    push(@mpointer,@mpointer_extra);
    push(@nprof,@nprof_extra);
  }

  # Close XYT NetCDF file
  NetCDF::close($ncid);
  $elapsed = $self->getElapsedInternal;
  print DEBUG "$elapsed secs elapsed to read $metapointer_file\n\n";
  
  # Strip out mpointers which came in as zero
  # Translate indices to Perl array start=0 instead of 1
  # Create hash
  my %valid_mpointers; #mpointer=>nprofs hash
  my $nprof_tot=0;
  print DEBUG "before masking out zeroes number of mpointer blocks: ".@mpointer."\n";

  for (my $i=0; $i<=$#mpointer; $i++) {
    if ($mpointer[$i] != 0) {
      $valid_mpointers{$mpointer[$i]-1} = $nprof[$i];
      $nprof_tot=$nprof_tot+$nprof[$i];
    }
  }

  # Temporary limit until IPC (inter-process commo) code is implemented 
  if ($nprof_tot> 3000000) {
    my $size = int(($nprof_tot/1000000)+ .5);
    print DEBUG "3000000 Limit was reached.  Num profs: $nprof_tot\n";
    my $message = "Sorry, but your request was too large.\n\n".
       "The server will not search more than 3 million profiles.\n".
       "Your request would require searching through about $size million.\n\n".
       "Use your browser's Back button or the Constraints link\n".
       "in the navigation frame and narrow your request parameters\n".
       "(Constraints, Lat/Lon or Time select range).";
    die "$message\n";
  }

  # Total number of mpointer blocks
  my $num_reads = keys(%valid_mpointers);
  print DEBUG "number of mpointer blocks: $num_reads\n";
  
  $elapsed = $self->getElapsedInternal;
  print DEBUG "$elapsed secs elapsed to mask meta pointers\n\n";

  # 2. Process 1D Metadata/DATA_POINTER file
  #----------------------------------------
  # This part reads only metadata or metadata, depth, and data
  # dependent on $self->{isMetaOnly} flag

  # Get the list of variables to read
  ###my @var_names = ();
  ###foreach my $var (sort keys %{$self->{variables}}) {
  ###  if ($var ne 'x' && $var ne 'y' && $var ne 'z' && $var ne 't') {
  ###    push(@var_names,$var);
  ###  }
  ###}

  # Open NetCDF file READ only
  print DEBUG "Opening $meta_file and reading/masking $nprof_tot profiles.....\n";
  $ncid  = NetCDF::open("$meta_file", NetCDF::NOWRITE);

  # Get variable ID's
  my $jday_id = NetCDF::varid($ncid,"JDAY");
  my $lat_id  = NetCDF::varid($ncid,"LAT");
  my $lon_id  = NetCDF::varid($ncid,"LON");
  my $cruise_id = NetCDF::varid($ncid,"CRUISE");
  my $nlevels_id = NetCDF::varid($ncid,"NLEVELS");
  my $constraints = $self->{constraint};
  foreach my $constr (keys(%{$constraints})){
    $constraints->{$constr}->{varid} = NetCDF::varid($ncid,"$constr");
  }
  # TODO:  This is where I need to be able to handle multiple 
  # TODO:  variables.
  #foreach $var_name (@var_names) {

  my $dpointer_id;
  ###my $var_name = pop(@var_names);
  my $var_name_uc = uc($var_name);
  my $ptr_name = $var_name_uc . "_PTR";
  my $qc_name = $var_name_uc . "_QC";

  $dpointer_id = NetCDF::varid($ncid,$ptr_name);
  $var_qc_id = NetCDF::varid($ncid,$qc_name);
 
  # TODO: This is where I need logic to figure out which
  # TODO: variable the person wants and the the appropriate
  # TODO: NetCDF varid.

  # Get metadata values
  my @jday =();
  my @lat = ();
  my @lon = ();
  my @cruise = ();
  my @nlevels = ();
  my @var_qc = ();
  my @dpointer = ();
  my @prof_index = ();
  
  my @temp_jday =();
  my @temp_lat = ();
  my @temp_lon = ();
  my @temp_cruise = ();
  my @temp_nlevels = ();
  my @temp_var_qc = ();
  my @temp_dpointer = ();

  my $tot_nlevels = 0;

  foreach my $start (sort keys(%valid_mpointers)) {
    my $count = $valid_mpointers{$start};

    NetCDF::varget($ncid,$jday_id,$start,$count,\@temp_jday);
    NetCDF::varget($ncid,$lat_id,$start,$count,\@temp_lat);
    NetCDF::varget($ncid,$lon_id,$start,$count,\@temp_lon);
    NetCDF::varget($ncid,$cruise_id,$start,$count,\@temp_cruise);
    NetCDF::varget($ncid,$nlevels_id,$start,$count,\@temp_nlevels);
    NetCDF::varget($ncid,$var_qc_id,$start,$count,\@temp_var_qc);

    NetCDF::varget($ncid,$dpointer_id,$start,$count,\@temp_dpointer);
    foreach my $constr (keys(%{$constraints})){
      my $temp_constr = $constraints->{$constr}->{temp_array};
      my $constr_id = $constraints->{$constr}->{varid};
      NetCDF::varget($ncid,$constr_id,$start,$count,$temp_constr);
    } 

    for (my $i=0; $i<@temp_dpointer; $i++) {
      my $inside_x;
      if ($lon_hi <= 360) {
        $inside_x = ($temp_lon[$i] >= $lon_lo && $temp_lon[$i] <= $lon_hi);
      } else {
        $inside_x = ($temp_lon[$i] >= $lon_lo || $temp_lon[$i] <= $lon_hi-360);
      }
      my $inside_y = ($temp_lat[$i] >= $lat_lo && $temp_lat[$i] <= $lat_hi);
      my $inside_t = ($temp_jday[$i] >= $time_lo && $temp_jday[$i] <= $time_hi);
      my $valid_ptr =($temp_dpointer[$i] > 0);

      my $valid_constr = 1;
      foreach my $constr (keys(%{$constraints})){
	my $c_val = $constraints->{$constr}->{c_val};
        my $val = $constraints->{$constr}->{temp_array}->[$i];
        if ($val != $c_val){
           $valid_constr = 0;
        }
      }
 
      if ($inside_x && $inside_y && $inside_t && $valid_ptr && $valid_constr) {

         push(@dpointer,$temp_dpointer[$i]-1);
         push(@cruise,$temp_cruise[$i]);
         push(@nlevels,$temp_nlevels[$i]);
         push(@var_qc,$temp_var_qc[$i]);
         push(@jday,($temp_jday[$i]-$time_lo)*24);
         push(@lat,$temp_lat[$i]);
         push(@lon,$temp_lon[$i]);

	 $tot_nlevels = $tot_nlevels + $temp_nlevels[$i];

      }
    }

    @temp_jday =();
    @temp_lat  =();
    @temp_lon  =();
    @temp_cruise =();
    @temp_nlevels  =();
    @temp_var_qc = ();
    @temp_dpointer =();
    foreach my $constr (keys(%{$constraints})){
        $constraints->{$constr}->{temp_array} = [];
    }
    eval{
    $self->checkTimeout($starttime);
    };
    if ($@){
      $elapsed = $self->getElapsedInternal;
      NetCDF::close($ncid);
      die $@;
   }
  }

  # Close NetCDF file
  NetCDF::close($ncid);
  if ($tot_nlevels > 3000000  && !$self->{isMetaOnly}) {
     my $size = int(($tot_nlevels/1000000)+ .5);
     print DEBUG "3000000 Limit was reached.  Total Nlevels: $tot_nlevels\n";
     my $message = "Sorry, but your request was too large.\n\n".
        "The server will not search more than 3 million data points.\n".
        "Your request would require searching through about $size million.\n\n".
        "Use your browser's Back button or the Constraints link\n".
        "in the navigation frame and narrow your request parameters\n".
	"(Constraints, Lat/Lon,Time or Depth select range).";

              die "$message\n";
  }
  $elapsed = $self->getElapsedInternal;
  print DEBUG "$elapsed secs and $num_reads NetCDF::varget calls\n";
  print DEBUG "to read and mask meta file resulting in ";
  print DEBUG @dpointer." profiles.\n\n";
  
  # Create performance testing variable
  $self->{optimize}->{numprofs}->{val} = @dpointer;
  $self->{optimize}->{numprofs}->{name} = 'NUMPROFS';
  $self->{optimize}->{numprofs}->{long_name} = 'number of profiles';
  $self->{optimize}->{numprofs}->{units} = 'unitless';

  # Create profile indexing variable
  $self->{variables}->{prof_index}->{name} = 'PROF_ID';
  $self->{variables}->{prof_index}->{long_name} = 'Profile Index';
  $self->{variables}->{prof_index}->{units} = 'unitless';

  # Create cruise id variable
  $self->{variables}->{cruise}->{name} = 'CRUISE_ID';
  $self->{variables}->{cruise}->{long_name} = 'CRUISE ID';
  $self->{variables}->{cruise}->{units} = 'unitless';

  # x,y,t attributes assigned in sub writeCDFFile
  # nlevels attributes to be assigned here
  # TODO may use NetCDF::attget
  $self->{variables}->{nlevels}->{name} = 'NLEVELS';
  $self->{variables}->{nlevels}->{long_name} = 'Number of Levels';
  $self->{variables}->{nlevels}->{units} = 'none';
  $self->{variables}->{nlevels}->{missing} = '-999';

  $self->{variables}->{var_qc}->{name} = $qc_name;
  $self->{variables}->{var_qc}->{long_name} = ' Profile Quality Flag`';
  $self->{variables}->{var_qc}->{units} = 'none';
  $self->{variables}->{var_qc}->{missing} = '-999';


  # Metadata Only block (else block below for observation data access)
  if($self->{isMetaOnly}){
    
    # Assign arrays to database properties
    $self->{variables}->{x}->{data} = \@lon;
    $self->{variables}->{y}->{data} = \@lat;
    my $Jdays_origin = $self->findJday("01-Jan-1700");
    $self->{variables}->{t}->{data} = \@jday;
    $self->{variables}->{cruise}->{data} = \@cruise;
    $self->{variables}->{nlevels}->{data} = \@nlevels;
    $self->{variables}->{var_qc}->{data} = \@var_qc;

    # used for writeCDFFile dimension length
    $self->{length} = $#lon + 1;

    for(my $i=0; $i<$self->{length}; $i++){
        $prof_index[$i] = $i+1;
    }
    $self->{variables}->{prof_index}->{data} = \@prof_index;

    $elapsed = $self->getElapsedInternal;
    print DEBUG "$elapsed secs elapsed to prepare Meta Data for writeCDFFile\n\n";
    my $accesstime = tv_interval($starttime);
    print DEBUG "$accesstime total secs elapsed to access metadata only\n\n";

    $self->{optimize}->{numobs_req}->{val} = $tot_nlevels;
    $self->{optimize}->{numobs_req}->{name} = 'NUMOBS';
    $self->{optimize}->{numobs_req}->{long_name} = 'number of observations';
    $self->{optimize}->{numobs_req}->{units} = 'unitless';

   }else{
    
    $num_reads = @dpointer;
    print DEBUG "number of dpointer blocks: $num_reads\n";

    # 3. Process 1D Data,Dep, and QC files
    # ---------------------------------------

    my $dim_name = "I_".$var_name_uc . "DIM";
    my $depth_name = $var_name_uc . "_DEP";
    #  Data level err codes not needed now
    #  my $err_name = $var_name_uc . "_ERR";
       my $orig_err_name = $var_name_uc . "_ORIG_ERR";
    #  my $depth_err_name = $var_name_uc . "_DEP_ERR";
    #  my $depth_orig_err_name = $var_name_uc . "_DEP_ORIG_ERR";

    # Open NetCDF file READ only
    $data_file = "data/wod_".$var_name_uc."_DATA.nc";
    $dep_file  = "data/wod_".$depth_name.".nc";
    #  Data level err codes not needed now
    #  $err_file  = "data/wod_".$err_name.".nc";
       $orig_err_file = "data/wod_".$orig_err_name.".nc";
    #  $dep_err_file = "data/wod_".$depth_err_name.".nc";
    #  $dep_orig_err_file = "data/wod_".$depth_orig_err_name.".nc";

    # Get data values
    my @depth =();
    my @temp_depth =();
    my $dncid;
    
    my @data  =();
    my @temp_data=();

    # NOTE:  These four arrays are not declared private with 'my'
    # NOTE:  because their names will be generated dynamically
    # NOTE:  from the incoming constraint.  If they are declared
    # NOTE:  to be private, this dynamic access will not work.
    # NOTE:  (See the 'Apply constraints' section below.)
    #  Data level err codes not needed now
    #  @err  =();
    my @orig_err  =();
    my @temp_orig_err = ();
    #  @depth_err  =();
    #  @depth_orig_err  =();
    
    print DEBUG "Opening and reading $tot_nlevels from $data_file and $dep_file .....\n";
    $ncid  = NetCDF::open("$data_file", NetCDF::NOWRITE);
    $dncid = NetCDF::open("$dep_file", NetCDF::NOWRITE);
    $oerrncid = NetCDF::open("$orig_err_file", NetCDF::NOWRITE); 
    # Get variable ID
    my $data_id  = NetCDF::varid($ncid,$var_name_uc);
    my $depth_id = NetCDF::varid($dncid,$depth_name);
    my $oerr_id  = NetCDF::varid($oerrncid,$orig_err_name);    
    # Get and constrain values
    my $data_iterator = 0;
    for(my $i=0;$i<@dpointer;$i++){
      my $count = $nlevels[$i];
      my $start = $dpointer[$i];
       
      NetCDF::varget($ncid,$data_id,$start,$count,\@temp_data);
      NetCDF::varget($dncid,$depth_id,$start,$count,\@temp_depth);
      NetCDF::varget($oerrncid,$oerr_id,$start,$count,\@temp_orig_err);
      my $counter = 0;

      for (my $j=0; $j<@temp_data; $j++) {
        if ( ($temp_depth[$j] != $self->{config}->{missing}) &&
           ($temp_depth[$j] >= $self->{zlo}) &&
           ($temp_depth[$j] <= $self->{zhi}) &&
           ($temp_data[$j] != -999) ) {

	  $counter++;
          push(@data,$temp_data[$j]);
          push(@depth,$temp_depth[$j]);
          push(@orig_err,$temp_orig_err[$j]);
          push(@temp_lon,$lon[$i]);
          push(@temp_lat,$lat[$i]);
          push(@temp_jday,$jday[$i]);
          push(@temp_var_qc,$var_qc[$i]);
          push(@temp_cruise,$cruise[$i]);
          push(@prof_index,$i + 1);
        }
      
      }
      # make nlevels just the number of valid data points
      for (my $j=$data_iterator; $j < $data_iterator+$counter; $j++){
          $temp_nlevels[$j] = $counter;
      }

      $data_iterator = $data_iterator + $counter;

      @temp_data  =();
      @temp_depth =();
      @temp_orig_err =();
    }
    if (@data > 2000000){
	my $size = int((@data/1000000)+ .5);
       print DEBUG "2000000 Limit was reached.  Total NumObs: ".@data."\n";
       my $message = "Sorry, but your request was too large.\n".
        "The server will not process more than 2 million data points.\n".
        "You requested about $size million.\n\n".
        "Use your browser's Back button or the Constraints link\n".
        "in the navigation frame and narrow your request parameters\n".
        "(Constraints, Lat/Lon or Time select range).";
       die "$message\n";
    }

    # Close NetCDF file
    NetCDF::close($ncid);
    NetCDF::close($dncid);
    NetCDF::close($oerrncid);

    $elapsed = $self->getElapsedInternal;
    print DEBUG "$num_reads NetCDF::varget calls and\n";
    print DEBUG "$elapsed secs elapsed to read and constrain \n";
    print DEBUG "$tot_nlevels data and depth measurements\n";
    print DEBUG "resulting in ".@data." measurements.\n\n";

    # TODO Will get val attributes here for now, but needs to be
    # TODO incorporated in get_data_vals subroutine for multi-variable efficiency
    # TODO  - how to deal with depth and error vals if this is done?
    #######
    $ncid  = NetCDF::open("$data_file", NetCDF::NOWRITE);
    $data_id =NetCDF::varid($ncid,$var_name_uc);
    my $units = "";
    my $long_name = "";
    NetCDF::attget($ncid,$data_id,'units',\$units);
    NetCDF::attget($ncid,$data_id,'long_name',\$long_name);
    chop $long_name;
    chop $units;
    $self->{variables}->{$var_name}->{units} = $units;
    $self->{variables}->{$var_name}->{long_name} = $long_name;

    NetCDF::close($ncid);

    $oerrncid  = NetCDF::open("$orig_err_file", NetCDF::NOWRITE);
    $oerr_id =NetCDF::varid($oerrncid,$orig_err_name);
    $units = "";
    $long_name = "";
    NetCDF::attget($oerrncid,$oerr_id,'units',\$units);
    NetCDF::attget($oerrncid,$oerr_id,'long_name',\$long_name);
    chop $long_name;
    chop $units;
    $self->{variables}->{$orig_err_name}->{units} = $units;
    $self->{variables}->{$orig_err_name}->{long_name} = $long_name;
    $self->{variables}->{$orig_err_name}->{name} = $orig_err_name;
    $self->{variables}->{$orig_err_name}->{missing} = -999;
    NetCDF::close($oerrncid);

    # Number of obs requested
    $self->{optimize}->{numobs_req}->{val} = @data;
    $self->{optimize}->{numobs_req}->{name} = 'NUMOBS';
    $self->{optimize}->{numobs_req}->{long_name} = 'Total num obs';
    $self->{optimize}->{numobs_req}->{units} = 'unitless';


    # Additional data level constraints may be applied here

  
    # Data are stored in @lon, @lat, @jday, @depth, @data
    if ($first) {

      $self->{variables}->{x}->{data} = \@temp_lon;

      $self->{variables}->{y}->{data} = \@temp_lat;

      $self->{variables}->{z}->{data} = \@depth;

      my $Jdays_origin = $self->findJday("01-Jan-1700");
      $self->{variables}->{t}->{data} = \@temp_jday;
    }

    $self->{variables}->{cruise}->{data}     = \@temp_cruise;
    $self->{variables}->{prof_index}->{data} = \@prof_index;
    $self->{variables}->{nlevels}->{data}    = \@temp_nlevels;
    $self->{variables}->{var_qc}->{data}     = \@temp_var_qc;

    $self->{variables}->{$var_name}->{data}   = \@data;
    $self->{variables}->{$orig_err_name}->{data} = \@orig_err;

    # used for writeCDFFile dimension length
    $self->{length} = @data;

    $elapsed = $self->getElapsedInternal;
    print DEBUG "$elapsed secs elapsed to prepare data for writeCDFFile\n\n";
    my $accesstime = tv_interval($starttime);
    print DEBUG "$accesstime total secs elapsed to access data\n\n";

  }
}

############################################################
#
# Method for generating a NetCDF file.
# overrides TMAPDBI.pm
#
############################################################


##
# We override this method because the superclass method assumes
# dates are returned as ASCII strings.  The TMAP/WODB database
# returns, instead, the hours since the time origin.
# @param file relative path of the output file
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

  my ($xref,$yref,$zref,$tref);
  my @varrefs = ();

  foreach my $var (keys %{$self->{variables}}) {
    
    if ($var) {
      if      (!$self->{variables}->{$var}->{axis}) {
        push(@varrefs,$self->{variables}->{$var});
      } elsif ($self->{variables}->{$var}->{axis} eq 'x') {
        $xref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 'y') {
        $yref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 'z') {
        $zref = $self->{variables}->{$var};
      } elsif ($self->{variables}->{$var}->{axis} eq 't') {
        $tref = $self->{variables}->{$var};
      }
    }

  }

  # Definitions
  my $ftype   = NetCDF::FLOAT  + 0; # Force conversion from string
  my $dtype   = NetCDF::DOUBLE + 0; #
  my $ctype   = NetCDF::CHAR   + 0; #

  my $ncid  = NetCDF::create($file, NetCDF::WRITE);

  # Create the index axis
  my $dimid_X  = NetCDF::dimdef($ncid, "index", $self->{length});

  # Create a one dimensional axis for access information variables
  my $dimid_ONE = NetCDF::dimdef($ncid, "dim_one", "1");

  # Variable attributes MUST be passed to attput() as reference
  # The $string variable is defined here

  my $string = "";

  # Create the trdim axis
  # Is Time a range or a point?
  my $dimid_TR;
  if(! $self->{time_point}){
      $dimid_TR  = NetCDF::dimdef($ncid, "trdim", "2");
  }else{
      $dimid_TR  = NetCDF::dimdef($ncid, "trdim", "1");
  }
  my $axisid_TR = NetCDF::vardef($ncid, "trdim", $dtype, [$dimid_TR]);
  $string = "$tref->{units} since " .
                 $self->{time_origin}->reformatLike("1999-01-01 00:00:00");
  NetCDF::attput($ncid, $axisid_TR, "units", $ctype, \$string);
  $string = $self->{time_origin}->toFerretString;
  NetCDF::attput($ncid, $axisid_TR, "time_origin", $ctype, \$string); 

  # Create the "trange" variable 
  my $varid_TR = NetCDF::vardef($ncid, "trange", $dtype, [$dimid_TR]);
  $string = $tref->{units};
  NetCDF::attput($ncid, $varid_TR, "units", $ctype, \$string);

  # Create the "xax" variable
  my $varid = NetCDF::vardef($ncid, "xax", $ftype, [$dimid_X]);
  $string = $xref->{long_name};
  NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
  $string = $xref->{units};
  NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
  NetCDF::attput($ncid, $varid, "missing_value", $ftype, $xref->{missing});
  $xref->{varid} = $varid;

  # Create the "yax" variable
  $varid = NetCDF::vardef($ncid, "yax", $ftype, [$dimid_X]);
  $string = $yref->{long_name};
  NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
  $string = $yref->{units};
  NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
  NetCDF::attput($ncid, $varid, "missing_value", $ftype, $yref->{missing});
  $yref->{varid} = $varid;

  # Create the "zax" variable
  if ($zref) {
    $varid = NetCDF::vardef($ncid, "zax", $ftype, [$dimid_X]);
    $string = $zref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
    $string = $zref->{units};
    NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, $zref->{missing});
    $zref->{varid} = $varid;
  } else {
    # If the z variable is not part of this dataset, create it anyway and
    # make it all zeros.
    $varid = NetCDF::vardef($ncid, "zax", $ftype, [$dimid_X]);
    $string = "Depth";
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
    $string = "meters";
    NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, -1.0e+34);
    $zref->{varid} = $varid;
    $zref->{data} = [];
    foreach (my $i=1; $i<=$self->{length}; $i++) {
      $zref->{data}[$i-1] = 0;
    }
  } 

  # Create the "tax" variable
  if ($tref) {
    $varid = NetCDF::vardef($ncid, "tax", $dtype, [$dimid_X]);
    $string = $tref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
    $string = "$tref->{units} since " .
                   $self->{time_origin}->reformatLike("1999-01-01 00:00:00");
    NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
    $string = $self->{time_origin}->toFerretString;
    NetCDF::attput($ncid, $varid, "time_origin", $ctype, \$string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, $tref->{missing});
    $tref->{varid} = $varid;
    
  }

  # Create all of the data variables
  foreach my $varref (@varrefs) {
    my $varid = NetCDF::vardef($ncid, $varref->{name}, $ftype, [$dimid_X]);
    $string = $varref->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
    $string = $varref->{units};
    NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
    NetCDF::attput($ncid, $varid, "missing_value", $ftype, $varref->{missing});
    $varref->{varid} = $varid;
  }

  # Create the optimization variables
  foreach my $optvar (keys %{$self->{optimize}}){
    my $opt = $self->{optimize}->{$optvar};
    my $varid = NetCDF::vardef($ncid, $opt->{name}, $ftype,[$dimid_ONE]);
    $string = $opt->{long_name};
    NetCDF::attput($ncid, $varid, "long_name", $ctype, \$string);
    $string = ($opt->{units});
    NetCDF::attput($ncid, $varid, "units", $ctype, \$string);
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

  # The only change relative to the superclass method is here.  In the WODB
  # case, the time axis data is exactly as we want it and no reformatting is
  # needed.
  #
  # Write the lon, lat, depth and time data
  NetCDF::varput($ncid, $xref->{varid}, [0], [$self->{length}], \@{$xref->{data}});
  NetCDF::varput($ncid, $yref->{varid}, [0], [$self->{length}], \@{$yref->{data}});
  NetCDF::varput($ncid, $zref->{varid}, [0], [$self->{length}], \@{$zref->{data}});
  NetCDF::varput($ncid, $tref->{varid}, [0], [$self->{length}], \@{$tref->{data}});



  foreach my $vref (@varrefs) {
    NetCDF::varput($ncid, $vref->{varid}, [0], [$self->{length}], \@{$vref->{data}});
  }

  # One dimensional variables with information on data access statistics.
  foreach my $optvar (keys %{$self->{optimize}}){
    my $opt = $self->{optimize}->{$optvar};
    NetCDF::varput($ncid, $opt->{varid}, [0], [1], [$opt->{val}]);
  }

  NetCDF::close($ncid);
}


# ------------------------------------------------
# subroutine
# usage &findIndex(@dimarray,$val)

#
# This subroutine is used to find the index values
# associated with a netCDF dimension.  There is no
# guarantee that the values on the axis are regularly
# spaced.
#
# LO indices will always match left boundaries [-->).
# HI indices will always match right boundaries (<--].
#
##
# @private
sub findIndex{
  my ($self,@dim) = @_; 
  my $hi_lo = pop(@dim);
  my $val = pop(@dim);

  my $index = -1;

  if ($hi_lo == 0) {  # LO

    # March through to the penultimate index.
    for (my $i=0; $i<$#dim; $i++) {
      my $right_edge = ($dim[$i] + $dim[$i+1])/2;
      if ($val < $right_edge) {
        $index = $i;
        $i = $#dim;
      }
    }
    # If $val is still greater than $right_edge,
    # use the last index.
    if ($index == -1) {
      $index = $#dim;
    }

  } else {  # HI

    # March through backwards to the second index
    for (my $i=$#dim; $i>0; $i--) {
      my $left_edge = ($dim[$i] + $dim[$i-1])/2;
      if ($val > $left_edge) {
        $index = $i;
        $i = 0;
      }
    }
    # If $val is still less than $left_edge,
    # use the first index.
    if ($index == -1) {
      $index = 0;
    }

  }
  return $index;
}

# ------------------------------------------------
# subroutine
# usage &findTindex(@dimarray,$val)

#
# This subroutine is used to find the index values
# associated with a netCDF edge axis.  This is needed
# for axes which have boundaries between coordinates
# at locations other than the midpoint between coordinates 
#
# LO indices will always match left boundaries [-->).
# HI indices will always match right boundaries (<--].
#
##
# @private
sub findTindex{
  my ($self,@dim) = @_; 
  my $hi_lo = pop(@dim);
  my $val = pop(@dim);

  my $index = -1;

  if ($hi_lo == 0) {  # LO

    # March through to the penultimate index.
    for (my $i=0; $i<$#dim; $i++) {
	my $right_edge = $dim[$i+1];
      if ($val < $right_edge) {
        $index = $i;
        $i = $#dim;
      }
    }
    # If $val is still greater than $right_edge,
    # there is a problem
#    if ($index == -1) {
#      $index = $#dim;
#    }

  } else {  # HI

    # March through backwards to the second index
    for (my $i=$#dim-1; $i>0; $i--) {
      my $left_edge = $dim[$i];
      if ($val > $left_edge) {
        $index = $i;
        $i = 0;
      }
    }
    # If $val is still less than $left_edge,
    # use the first index.
    if ($index == -1) {
      $index = 0;
    }

  }
  return $index;
}

##
# @private
sub dateFromJday {
  my ($self,$Jdays) = @_;
  my $temp = $Jdays;

  $Jdays = int($Jdays);

  my $days_in_year = 365;
  my $days_in_four_years = 4 * $days_in_year + 1;
  my $days_in_century = 100 * $days_in_year + 24;
  my @days_before_month = (0,31,59,90,120,151,181,212,243,273,304,334,365);
  my @l_days_before_month = (0,31,60,91,121,152,182,213,244,274,305,335,366);
  my @months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec Jan);
 
  my $aref = \@days_before_month;
  my $is_leap = 0;
  my $year  = 0;
  my $month = "jan";

  $Jdays += 11; #11 day shift for dates after 1582

  while ($Jdays > $days_in_century) {
    $year+=100; $Jdays-=$days_in_century;
    if ($year % 400) { $Jdays-=1; }
  }
  while ($Jdays > $days_in_four_years) {
    $year+=4; $Jdays-=$days_in_four_years;
  }
  while ($Jdays > $days_in_year) {
    $year+=1; $Jdays-=$days_in_year;
  }

  if ($year % 4 != 0) { 
    $aref = \@days_before_month;
  } elsif  (($year % 400) == 0) {
    $aref = \@l_days_before_month;
  } elsif (($year % 100) == 0) {
    $aref = \@days_before_month;
  } else {
    $aref = \@l_days_before_month;
  }

  foreach (my $i=0; $i<13; $i++) {
    if ($Jdays <= $$aref[$i]) {
      $month =  $months[$i-1];
      $Jdays -= $$aref[$i-1];
      if (length($Jdays) == 1) {
        $Jdays = "0" . $Jdays;
      }
      return "$Jdays-$month-$year";
    }
  }

}


# subroutine
# usage %findJday($dateString) [I|II]-CCC-IIII ex: 1-jan-1998
##
# @private
sub findJday{
  my ($self,$dateString) = @_;
  my ($datePart,$timePart) = split(/ /,$dateString);
  my ($day,$month,$year) = split(/-/,$datePart);
  my ($hour,$minute,$second) = (0,0,0);
  if ($timePart) {
    ($hour,$minute,$second) = split(/:/,$timePart);
  }

  $month =~ tr/A-Z/a-z/; #set all characters to lower case

  my @days_before_month = (0,31,59,90,120,151,181,212,243,273,304,334);
  my @days_in_month = (31,28,31,30,31,30,31,31,30,31,30,31);
  my %months = ("jan" => 1,
                "feb" => 2,
                "mar" => 3,
                "apr" => 4,
                "may" => 5,
                "jun" => 6,
                "jul" => 7,
                "aug" => 8,
                "sep" => 9,
                "oct" => 10,
                "nov" => 11,
                "dec" => 12);
  my $mon_index = $months{$month}-1;
  my $mon_val = $months{$month};

  # set leap year flag
  my $is_leap_year;
  if(($year % 400) == 0 || (($year % 4) == 0 && ($year % 100 != 0))){
    $is_leap_year = 1;
  }else{
    $is_leap_year = 0;
  }

  my $days_in_year = 365;
  my $days_in_lp_year = 366;
  my $days_in_cent = 76*$days_in_year + 24*$days_in_lp_year; # no leap at 100
 
  # ADD LOTS OF DAYS FOR EACH COMPLETED CENTURY SINCE 01-Jan-0000 
  # (century = 01-jan-0000:31-dec-0099)
  my $total_days = $days_in_cent * int ($year/100);

  # ADD A DAY FOR MULTIPLES OF 400 YEARS (LEAP YEAR AT QUADRICENTENNIAL)
  $total_days = $total_days + int (($year-1)/400);

  # ADD A YEAR FOR EACH YEAR SINCE TURN OF CENTURY
  $total_days = $total_days + $days_in_year * ($year % 100);

  # ADD A DAY FOR EACH LEAP YEAR SINCE CENTENNIAL(EXCEPT FOR CENTENNIAL)
  $total_days = $total_days + int ((($year % 100)-1)/4);

  # ADD DAYS FOR NUMBER OF MONTHS
  $total_days = $total_days + $days_before_month[$mon_index];

  # ADD 1 DAY IF THIS IS LEAP YEAR AND PAST FEBRUARY
  if($mon_val > 2 && $is_leap_year){
    $total_days = $total_days + 1;
  }

  # ADD day arg
  $total_days = $total_days + $day + $hour/24 + $minute/1440 + $second/86400;

  return $total_days;
}

#-------------------------------------
# subroutine
##
# @private
sub get_data_vals{
  my ($self,$name,$file,$dim_name,$dptrs_hash_ref,$array_ref) = @_;
  my @temp_array;

  print DEBUG "Opening and reading data file $file .....\n";
  $ncid  = NetCDF::open("$file", NetCDF::NOWRITE);

  # Get dimension ID
  my $datadim_id = NetCDF::dimid($ncid,$dim_name);

  # Get Dimension name and length
  my $datadim_len=0;
  my $datadim_name="";
  
  $err_code = NetCDF::diminq($ncid,$datadim_id,$datadim_name,$datadim_len);

  # Get variable ID
  my $data_id  = NetCDF::varid($ncid,$name);

  # Get data values
  foreach my $start (sort keys(%{$dptrs_hash_ref})){
    my $count = $dptrs_hash_ref->{$start}->{nlevels};
    NetCDF::varget($ncid,$data_id,$start,$count,\@temp_array);

    push(@{$array_ref},@temp_array);
  
    @temp_array  =();
  }

  # Close NetCDF file
  $err_code = NetCDF::close($ncid);
  if ($err_code == -1){
      print DEBUG "error: can't close ncid $ncid\n";
  }
}

# subroutine usage &checkTimeout($starttime)

##
# @private
sub checkTimeout {
    my ($self, $starttime) = @_;
    my $startread = tv_interval($starttime);

    if($startread > $self->{config}->{timeout}){
      print DEBUG "\n$self->{config}->{timeout} secs timeout was reached.  stopped at $startread seconds\n";

      my $message = <<EOL;
Sorry, but your request took too much time.  It could be 
that this computer is very busy right now with other requests.

Use your Browser's back button or the Constraints link in the
navigation frame to try again.  You may need to narrow your
request parameters (Constraints, Lat/Lon, or Time select range).

EOL

      die "$message\n";
    }
}
1;

