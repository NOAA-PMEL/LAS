# 
# $Id: insitu.pl,v 1.15.4.8 2005/07/06 16:53:41 callahan Exp $
#
# Code to support insitu data access and visualization methods.
#

use strict;
use LAS;
use TMAPDate;
use TMAPDBI;
use TMAPDapper;
use TMAPJGOFS;
use TMAPOSMC;
use TMAPWODB;

package LAS::Server::Ferret;
use File::Basename;
use MD5;
use LASNetCDF;
require ("cruise.pl");

sub insitu_property {
    my ($self) = @_;
    $self->{props}->{style} = "property";
    $self->{props}->{outputView} = "";
    $self->{props}->{setupScript} = "insitu_property_setup";
    my $numVars = scalar @{$self->{vars}};
    die "Property/property plot requires two (and only two) variables; found $numVars"
	if $numVars != 2;
    $self->graphic_2_var;
}

sub insitu_poly_xy {
    my ($self) = @_;
    $self->{props}->{style} = "poly";
    $self->{props}->{outputView} = "xy";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_poly_xt {
    my ($self) = @_;
    $self->{props}->{style} = "poly";
    $self->{props}->{outputView} = "xt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_poly_yt {
    my ($self) = @_;
    $self->{props}->{style} = "poly";
    $self->{props}->{outputView} = "yt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_waterfall_xz {
    my ($self) = @_;
    $self->{props}->{style} = "waterfall";
    $self->{props}->{outputView} = "xz";
    $self->{props}->{setupScript} = "insitu_setup_stations";
    $self->graphic;
}

sub insitu_waterfall_yz {
    my ($self) = @_;
    $self->{props}->{style} = "waterfall";
    $self->{props}->{outputView} = "yz";
    $self->{props}->{setupScript} = "insitu_setup_stations";
    $self->graphic;
}

sub insitu_waterfall_zt {
    my ($self) = @_;
    $self->{props}->{style} = "waterfall";
    $self->{props}->{outputView} = "zt";
    $self->{props}->{setupScript} = "insitu_setup_stations";
    $self->graphic;
}

sub insitu_gaussian_xy {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "xy";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_gaussian_xz {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "xz";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_gaussian_xt {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "xt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_gaussian_yz {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "yz";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_gaussian_yt {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "yt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_gaussian_zt {
    my ($self) = @_;
    $self->{props}->{style} = "gaussian";
    $self->{props}->{outputView} = "zt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->{props}->{no_sampling} = 1;
    $self->graphic;
}

sub insitu_property_depth {
    my ($self) = @_;
    $self->{props}->{style} = "property_depth";
    $self->{props}->{outputView} = "";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_xy {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "xy";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_xz {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "xz";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_xt {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "xt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_yz {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "yz";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_yt {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "yt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_zt {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "zt";
    $self->{props}->{setupScript} = "insitu_setup";
    $self->graphic;
}

sub insitu_pie_station_depth {
    my ($self) = @_;
    $self->{props}->{style} = "pie";
    $self->{props}->{outputView} = "station_depth";
    $self->{props}->{setupScript} = "insitu_setup_stations";
    $self->graphic;
}

sub insitu_meta_xy{
    my ($self) = @_;
    $self->insitu_meta("xy");
}


sub insitu_meta_xz {
    my ($self) = @_;
    $self->insitu_meta("xz");
}

sub insitu_meta_yz {
    my ($self) = @_;
    $self->insitu_meta("yz");
}

sub insitu_meta {
    my ($self, $outputView) = @_;
    my $props = $self->{props};
    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    $props->{var_title} = $var->getAttribute('name');
    $props->{variable_name_0} = 'TAX';
    $self->setup_region;
    if ($props->{insitu_use_ref_map} eq 'false'){
	$props->{insitu_refmap} = 0;
    }elsif ($props->{insitu_use_ref_map} eq 'true' || $props->{insitu_use_ref_map} eq 'default'){
	$props->{insitu_refmap} = 1;
    }
    my @args0 = qw(dataset_name_0 variable_name_0 dataset_title var_title);
    $self->runJournal('insitu_setup', \@args0, undef, 'insitu_setup', \@args0);
    my $xrange = $props->{x_lo} . ':' . $props->{x_hi};
    my $yrange = $props->{y_lo} . ':' . $props->{y_hi};
    my $zrange = $props->{z_lo} . ':' . $props->{z_hi};
    my $comm = "go insitu_meta_" . $outputView . " \"" . $props->{dataset_name_0} . "\" TAX " . $xrange . " " . $yrange . " " . $zrange . " " . "\"$props->{insitu_palette}\"" . " " . "\"$props->{insitu_fill_levels}\"" . " " . "\"$props->{insitu_refmap}\"";
    $self->command($comm);
    $self->genImage($self->{output_file});
}

sub insitu_tsum{
    my ($self) = @_;
    my $props = $self->{props};
    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    $props->{var_title} = $var->getAttribute('name');

    $self->setup_region;
    my @args0 = qw(dataset_name_0 variable_name_0 dataset_title var_title);
    $self->runJournal('insitu_setup', \@args0, undef, 'insitu_setup', \@args0);
    my $xrange = $props->{x_lo} . ':' . $props->{x_hi};
    my $yrange = $props->{y_lo} . ':' . $props->{y_hi};
    my $comm = "go insitu_tsum" . " \"" . $props->{dataset_name_0} . "\" TAX " . $xrange . " " . $yrange . " " . $props->{ts_bin_size};
    $self->command($comm);
    $self->genImage($self->{output_file});
}

sub insitu_tave{
    my ($self) = @_;
    my $props = $self->{props};
    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    $props->{var_title} = $var->getAttribute('name');

    $self->setup_region;
    my @args0 = qw(dataset_name_0 variable_name_0 dataset_title var_title);
    $self->runJournal('insitu_setup', \@args0, undef, 'insitu_setup', \@args0);
    my $xrange = $props->{x_lo} . ':' . $props->{x_hi};
    my $yrange = $props->{y_lo} . ':' . $props->{y_hi};
    my $comm = "go insitu_tave" . " \"" . $props->{dataset_name_0} . "\" " . " \"" .$props->{variable_name_0} ."\" " . $xrange . " " . $yrange. " " . $props->{ts_bin_size};
    $self->command($comm);
    $self->genImage($self->{output_file});
}

sub graphic {
    my ($self) = @_;
    my $props = $self->{props};

#
# All variables must be from the same dataset
#
    my %DsetHash = ();
    map { $DsetHash{$_->getURL} = 1 } @{$self->{vars}};
    die "Multiple variables must be from same dataset"
	if scalar keys %DsetHash > 1;
    
    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    my $size = $props->{size} ? $props->{size} : "0.25";
    $self->setWinSize($size);
    $self->setup_region;
    if(defined($props->{no_sampling})){
      my $no_sam = $props->{no_sampling};
      $self->command("def symbol no_sampling = `$no_sam`");
    }
    if ($props->{insitu_use_ref_map} eq 'false'){
	$props->{insitu_refmap} = 0;
    }elsif ($props->{insitu_use_ref_map} eq 'true' || $props->{insitu_use_ref_map} eq 'default'){
	$props->{insitu_refmap} = 1;
    }
    my @args1 = qw(dataset_name_0 variable_name_0 dataset_title
                   insitu_fill_levels insitu_palette insitu_refmap
		   outputView style setupScript db_title
                   constraint_0 constraint_1 constraint_2 constraint_3
                   constraint_4 constraint_5 constraint_6 constraint_7);
    my @extraArgs = [];
    
    $self->runJournal('insitu_std_gif', \@args1, \@extraArgs);
    $self->genImage($self->{output_file});
}

sub graphic_2_var {
    my ($self) = @_;
    my $props = $self->{props};

#
# All variables must be from the same dataset
#
    my %DsetHash = ();
    map { $DsetHash{$_->getURL} = 1 } @{$self->{vars}};
    die "Multiple variables must be from same dataset"
	if scalar keys %DsetHash > 1;

    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    my $size = $props->{size} ? $props->{size} : "0.25";
    $self->setWinSize($size);
    $self->setup_region;

    if ($props->{insitu_use_ref_map} eq 'false'){
	$props->{insitu_refmap} = 0;
    }elsif ($props->{insitu_use_ref_map} eq 'true' || $props->{insitu_use_ref_map} eq 'default'){
	$props->{insitu_refmap} = 1;
    }

    my ($data1,$var1) = getDataAndVarName($self->{vars}->[1]);
    $props->{variable_name_1} = $var1;

    my @args1 = qw(dataset_name_0 variable_name_0 dataset_title variable_name_1
                   insitu_fill_levels insitu_palette insitu_refmap
		   outputView style setupScript db_title
                   constraint_0 constraint_1 constraint_2 constraint_3
                   constraint_4 constraint_5 constraint_6 constraint_7);
    my @extraArgs = [];

    $self->runJournal('insitu_std_gif_2_var', \@args1, \@extraArgs);
    $self->genImage($self->{output_file});
}

# Do nothing -- netCDF file has already been created
sub insitu_data_cdf {
    my ($self) = @_;
}

#NetCDF file has been created 
# - place link in DODS dir
# - return message with DODS URL and instructions
sub insitu_data_dods {
    my ($self) = @_;
    my $props = $self->{props};
    my $output_file = $self->{output_file};

    my $LASConfig = &LAS::Server::getConfig;

    ###my $dods_dir = 'http://ferret.pmel.noaa.gov/cgi-bin/nph-dods/data/WODB_LAS/';
    my $dods_dir = $LASConfig->{OPeNDAP_directory};
    my $cdf_file = $props->{dataset_name_0};
    my $dods_url = $dods_dir.$cdf_file;

    open OUT, ">$output_file" or die "Can't open $output_file";
    print OUT "<tr><td align = \"left\">\n";
    print OUT "OPeNDAP is protocol formerly known as DODS, the Distributed Oceanographic Data System.  It is a tool for the sampling and retrieval of remote datasets.  Click <a href=\"http://www.unidata.ucar.edu/packages/dods/index.html\"
onClick=\"newwin=window.open('http://www.unidata.ucar.edu/packages/dods/index.html', '', 'menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes'); newwin.focus();return false\">here</a> for more information about OPeNDAP/DODS.<br><br>\n";

    print OUT "Copy and paste the following into a Ferret session for OPeNDAP access to the data sample which you requested:<br>\n"; 
    print OUT "use \"".$dods_url."\""."<br><br>\n";
    print OUT "OR<br><br>\n";

    print OUT "<p>Click on the links below to access OPeNDAP products for the data.<br>\n";

    print OUT "\n
<a href=\"$dods_url.html\"\n
onClick=\"newwin=window.open('$dods_url.html', '', 'menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes'); newwin.focus();return false\">Data Request Form</a><br>\n";
    print OUT "\n
<a href=\"$dods_url.info\"\n
onClick=\"newwin=window.open('$dods_url.info', '', 'menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes'); newwin.focus();return false\">Information</a><br>\n";

   print OUT "\n
<a href=\"$dods_url.dds\"\n
onClick=\"newwin=window.open('$dods_url.dds', '', 'menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes'); newwin.focus();return false\">DDS</a><br>\n";
   print OUT "\n
<a href=\"$dods_url.das\"\n
onClick=\"newwin=window.open('$dods_url.das', '', 'menubar=yes,toolbar=yes,resizable=yes,scrollbars=yes'); newwin.focus();return false\">DAS</a><br><br><br></p>\n";
    print OUT "</td></tr>\n";

    close OUT;       
}


##
# Create human readable, comma separated, ASCII output.
#
sub insitu_data {
  my ($self) = @_;
  my $props = $self->{props};
  my $db = $self->{db};
  my $LAS_intermediate_file = $props->{dataset_name_0};
  my $LAS_output_file = $self->{output_file};

# Get all variable names.  These will be used to create a
# Ferret symbol for use inside the journal script.

  my @variable_names = @{$props->{variable_name}};
  if ($self->{string_vars}) {
    push(@variable_names,@{$self->{string_vars}});
  }

  my $LAS_variable_names = join(',',@variable_names);

  my $comm;
  $comm = "define symbol LAS_input_file " . $LAS_intermediate_file;
  $self->command($comm);
  $comm = "define symbol LAS_output_file " . $LAS_output_file;
  $self->command($comm);
  $comm = "define symbol LAS_variable_names " . $LAS_variable_names;
  $self->command($comm);

# TODO: The format qualifier should be an LAS option when Ferret supports
# TODO: comma and tab formatted output.  (As of Ferret 5.8.1 it does not
# TODO: support these formats for multiple variable output.)

  $comm = "define symbol LAS_format txt";
  $self->command($comm);

  my @args0 = ();
  $self->runJournal('insitu_txt', \@args0, undef, 'insitu_txt', \@args0);
  my @args0 = ();
}


##
# Create human readable, or comma separated, ASCII output.
#
sub insitu_data_OLD {
  my ($self) = @_;
  my $props = $self->{props};
  my $db = $self->{db};
  my $output_file = $self->{output_file};
  my $db_type = $db->{config}->{db_type};

  open OUT, ">$output_file" or die "Can't open $output_file";
  if ($db_type eq 'WODB') {
    print OUT "Data from World Ocean Data Base\n";
  } elsif ($db_type eq 'JGOFS') {
    print OUT "Data from Joint Global Ocean Flux Study\n";
  }

  print OUT "Output by Live Access Server\n";
  print OUT scalar localtime, "\n";
  print OUT "\n";
  print OUT "  Dataset title: ", $props->{dataset_title}, "\n";
  print OUT "  missing value: ", $db->{config}->{missing}, "\n";

# Display lat/lon/depth/time ranges

  my $lon_start = $self->{props}->{x_lo};
  my $lon_end = $self->{props}->{x_hi};
  my $lat_start = $self->{props}->{y_lo};
  my $lat_end = $self->{props}->{y_hi};
  my $dep_start = $self->{props}->{z_lo};
  my $dep_end = $self->{props}->{z_hi};
  my $time_start = $self->{props}->{t_lo};
  my $time_end = $self->{props}->{t_hi};

# Format metadata values

  if($lon_start > 180 && $lon_start < 360) {
      my $tmp = 360 - $lon_start;
      $lon_start = sprintf("%5.2f",$tmp)."W";
  }elsif($lon_start >= 360){
      my $tmp = $lon_start - 360;
      $lon_start = sprintf("%5.2f",$tmp)."E";
  }else{
      my $tmp = $lon_start;
      $lon_start = sprintf("%5.2f",$tmp)."E";
  }
  if($lon_end > 180 && $lon_end < 360) {
      my $tmp = 360 - $lon_end;
      $lon_end = sprintf("%5.2f",$tmp)."W";
  }elsif($lon_end >= 360){
      my $tmp = $lon_end - 360;
      $lon_end = sprintf("%5.2f",$tmp)."E";
  }else{
      my $tmp = $lon_end;
      $lon_end = sprintf("%5.2f",$tmp)."E";
  }
  if($lat_start < 0){
      my $tmp = $lat_start * -1;
      $lat_start = sprintf("%4.2f",$tmp)."S";
  }else{
      my $tmp = $lat_start;
      $lat_start = sprintf("%4.2f",$tmp)."N";
  }
  if($lat_end < 0){
      my $tmp = $lat_end * -1;
      $lat_end = sprintf("%4.2f",$tmp)."S";
  }else{
      my $tmp = $lat_end;
      $lat_end = sprintf("%4.2f",$tmp)."N";
  }
  my $day_start = $time_start;
  my $day_end = $time_end;

  print OUT "Longitude range: $lon_start to $lon_end\n";
  print OUT " Latitude range: $lat_start to $lat_end\n";
  print OUT "    Depth range: $dep_start to $dep_end\n";
  print OUT "     Time range: $day_start to $day_end\n";

# Here are all the internal variables that all database drivers
# must write into the NetCDF file.  Any other variables found
# are data variables.
# 
#   double trdim(trdim) ;
#   double trange(trdim) ;
#   double tax(index) ;
#   float xax(index) ;
#   float yax(index) ;
#   float zax(index) ;
#   float PROF_ID(index) ;
#   float NUMPROFS(dim_one) ;
#   float NUMOBS(dim_one) ;

  my %varObjects = {};
  my %varData = {};
  my @varnames = @{$props->{variable_name}};

  my $cdf_file = $props->{dataset_name_0};
  my $cdf = new LAS::NetCDF($cdf_file);

# Get internal variables we know we'll need

  my $prof_id = $cdf->getVariable("PROF_ID");
  my $xax = $cdf->getVariable("xax");
  my $yax = $cdf->getVariable("yax");
  my $zax = $cdf->getVariable("zax");
  my $tax = $cdf->getVariable("tax");

# Get all requested variables

  foreach my $var (@varnames) {
    $varObjects{$var} = $cdf->getVariable($var);
  }

# Get all additional <string_vars>

  foreach my $var (@{$self->{string_vars}}) {
    $varObjects{$var} = $cdf->getVariable($var);
  }

  my $time_origin = $tax->getAttribute("time_origin");
  my $t_origin_object = new TMAP::Date($time_origin);

# Print out some more information that we get from the
# data file.

  print OUT "    Time origin: $time_origin\n\n";


  my @dims = $xax->getDims();
  my $index_dim = @dims[0];
  my $size = $index_dim->getSize();

# Get data for internal variables we know we'll need

  my @prof_ids  = $prof_id->getData([0],[$size]);
  my @lons = $xax->getData([0],[$size]);
  my @lats = $yax->getData([0],[$size]);
  my @depths = $zax->getData([0],[$size]);
  my @hours = $tax->getData([0],[$size]);

# Get data for all requested variables

  foreach my $var (@varnames) {
    my $varObj = $varObjects{$var};
    @{$varData{$var}} = $varObj->getData([0],[$size]);
  }
  
# Get data for all additional <string_vars>

  foreach my $var (@{$self->{string_vars}}) {
    my $varObj = $varObjects{$var};
    @{$varData{$var}} = $varObj->getData([0],[$size]);
  }
  
  $cdf->close;

# Format and print the values to OUT

  my @date_strings;
  foreach my $hrs (@hours){
      my $dec_mins = $hrs - int($hrs);
      my $mins = int($dec_mins * 60);
      my $dec_secs = ($dec_mins * 60) - $mins;
      my $secs = int($dec_secs * 60);
      my $date = $t_origin_object->addDelta(0,0,0,$hrs,$mins,$secs);
      my $date_string = $date->toFerretString;
      if (length($date_string) < 12){
 	  $date_string .= " 00:00:00";
      }
      push(@date_strings,$date_string);
  }

  my ($line,$prof_id,$lon,$lat,$hrs,$date,$dep,$val) = "";
  my ($long_name,$units) = "";
  if ($props->{comma_separated}) {
    $line = "prof_ID,longitude,latitude,hours since origin,date,depth"; 
    foreach my $var (@varnames) {
      my $varObj = $varObjects{$var};
      $long_name = $varObj->getAttribute("long_name");
      $units = $varObj->getAttribute("units");
      $line .= ',' . $long_name;
    }
    foreach my $var (@{$self->{string_vars}}) {
      my $varObj = $varObjects{$var};
      $long_name = $varObj->getAttribute("long_name");
      $line .= ',' . $long_name;
    }
  } else {
    $line = "prof_ID,      lon,      lat, hours since,                  date,   depth";
    foreach my $var (@varnames) {
      my $varObj = $varObjects{$var};
      $long_name = sprintf("%14s",$varObj->getAttribute("long_name"));
      $units = $varObj->getAttribute("units");
      $line .= ',' . $long_name;
    }
    foreach my $var (@{$self->{string_vars}}) {
      my $varObj = $varObjects{$var};
      $long_name = sprintf("%14s",$varObj->getAttribute("long_name"));
      $line .= ',' . $long_name;
    }
  }
  $line .= "\n";
  print OUT $line;


  for (my $i=0; $i<$size; $i++) {
    $line = "";

    if ($props->{comma_separated}) {
      $prof_id  = $prof_ids[$i];
      $lon = sprintf("%g",$lons[$i]);
      $lat = sprintf("%g",$lats[$i]);
      $hrs = sprintf("%g",$hours[$i]);
      $date = sprintf("%s",$date_strings[$i]);
      $dep = sprintf("%g",$depths[$i]);
      $hrs = sprintf("%g",$hours[$i]);
      $line = "$prof_id,$lon,$lat,$hrs,$date,$dep";
      foreach my $var (@varnames) {
        $val = sprintf("%g",$varData{$var}[$i]);
        $line .= ',' . $val;
      }
      foreach my $var (@{$self->{string_vars}}) {
        $val = sprintf("%g",$varData{$var}[$i]);
        $line .= ',' . $val;
      }
    } else {
      $prof_id  = sprintf("%7d",$prof_ids[$i]);
      $lon = sprintf("%9.4f",$lons[$i]);
      $lat = sprintf("%9.4f",$lats[$i]);
      $hrs = sprintf("%12.4f",$hours[$i]);
      $date = sprintf("%22s",$date_strings[$i]);
      $dep = sprintf("%8.2f",$depths[$i]);
      $line = "$prof_id,$lon,$lat,$hrs,$date,$dep";
      foreach my $var (@varnames) {
        $val = sprintf("%14g",$varData{$var}[$i]);
        $line .= ',' . $val;
      }
      foreach my $var (@{$self->{string_vars}}) {
        $val = sprintf("%14g",$varData{$var}[$i]);
        $line .= ',' . $val;
      }
    }

    $line .= "\n";
    print OUT $line;
  }

  close OUT;
}


#
# $ENV{REQUEST_URI} will look something like this:
#
#/callahan-bin/LASserver.pl?xml=%3C%3Fxml+version%3D%221.0%22%3F%3E%3ClasRequest+package%3D%22%22+href%3D%22file%3Alas.xml%22+%3E%3Clink+match%3D%22%2Flasdata%2Foperations%2Finsitu_profile_list%22+%2F%3E%3Cproperties+%3E%3Cferret+%3E%3Csize+%3E.5%3C%2Fsize%3E%3Cformat+%3Ehtml%3C%2Fformat%3E%3C%2Fferret%3E%3C%2Fproperties%3E%3Cargs+%3E%3Clink+match%3D%22%2Flasdata%2Fdatasets%2Fdapper_pacific_prof_ctd%2Fvariables%2FT_20%22+%2F%3E%3Cregion+%3E%3Crange+low%3D%22204.0%22+type%3D%22x%22+high%3D%22216.0%22+%2F%3E%3Crange+low%3D%22-39.0%22+type%3D%22y%22+high%3D%22-17.0%22+%2F%3E%3Crange+low%3D%220.0%22+type%3D%22z%22+high%3D%22171%22+%2F%3E%3Crange+low%3D%2201-Jan-1983+00%3A00%3A00%22+type%3D%22t%22+high%3D%2231-Oct-1985+23%3A00%3A00%22+%2F%3E%3C%2Fregion%3E%3C%2Fargs%3E%3C%2FlasRequest%3E
#
sub insitu_profile_list {
  my ($self) = @_;

  my $props = $self->{props};
  my $output_file = $self->{output_file};

  my $z_lo = $self->{props}->{z_lo};
  my $z_hi = $self->{props}->{z_hi};
  my $http_host = $ENV{HTTP_HOST};
  my $query = $ENV{REQUEST_URI};

# $request is the URL request for the html javascript set_link function
# $next_request is the URL request for Next button (next 100 cruises) on the html page

  $query =~ m/(^.+insitu)_profile_list(.+)(%3Cformat....)html(......format%3E)(.+)(%3Cregion.%3E?)(.+)(%3C%2Fregion%3E.+$)/;
  my $reqHead = "http://$http_host";
  $reqHead .= "$1"."_single_profile"."$2$3"."gif".$4;
  debug("insitu_profile_list: reqhead: $reqHead\n");
  my $next_reqHead = "http://$http_host";
  $next_reqHead .= "$1"."_profile_list"."$2$3"."html"."$4";
  my $reqFill = $5;
  my $reqRegion = "$6";
  my $reqOrigRegion = "$7";
  my $reqTail = "$8";
  $reqFill =~ s/%3Cprofile_list_start\+%3E.+%3C%2Fprofile_list_start%3E//g;

  my $reqXRange = "%3Crange+low%3D%22\"+x_lo+\"%22+type%3D%22x%22+high%3D%22\"+x_hi+\"%22+%2F%3E";
  my $reqYRange = "%3Crange+low%3D%22\"+y_lo+\"%22+type%3D%22y%22+high%3D%22\"+y_hi+\"%22+%2F%3E";
  my $reqTRange = "%3Crange+low%3D%22\"+t_lo+\"%22+type%3D%22t%22+high%3D%22\"+t_hi+\"%22+%2F%3E";
  my $reqZRange = "%3Crange+low%3D%22$z_lo%22+type%3D%22z%22+high%3D%22$z_hi%22+%2F%3E";

  my $request = "\""."$reqHead"."$reqFill"."$reqRegion"."$reqXRange"."$reqYRange"."$reqZRange"."$reqTRange"."$reqTail"."\"";

# now open the netCDF file and get all the data
  my $cdf_file = $props->{dataset_name_0};
  my $cdf = new LAS::NetCDF($cdf_file);

  my $prof_id = $cdf->getVariable("PROF_ID");
  my $xax = $cdf->getVariable("xax");
  my $yax = $cdf->getVariable("yax");
  my $tax = $cdf->getVariable("tax");
  my $time_origin = $tax->getAttribute("time_origin");

  my $origin = new TMAP::Date($time_origin);
  my $date = new TMAP::Date($time_origin);

  my @dims = $xax->getDims();
  my $index_dim = @dims[0];
  my $size = $index_dim->getSize();

  my @prof_ids  = $prof_id->getData([0],[$size]);
  my @lons = $xax->getData([0],[$size]);
  my @lats = $yax->getData([0],[$size]);
  my @hours = $tax->getData([0],[$size]);

  my $tot_num_profs = scalar @prof_ids;

  my ($index,$lon,$lat,$hour,$minute,$second) = (0,0,0,0,0,0);
  my $dateString = "dateString";

  open OUT, ">$output_file" or die "Can't open $output_file";
  print OUT "<html>\n";
  print OUT " <head>\n";
  print OUT " <script language=\"javascript\">\n";
  print OUT "  function set_link(lon,lat,date){\n";
  print OUT "   var x_lo = lon-0.001;\n";
  print OUT "   var x_hi = lon+0.001;\n";
  print OUT "   var y_lo = lat-0.001;\n";
  print OUT "   var y_hi = lat+0.001;\n";
  print OUT "   var t_lo = date;\n";
  print OUT "   var t_hi = date;\n";
  print OUT "   var link = $request\n";
  print OUT "   var dataWindow = window.open(link);";
  print OUT "   dataWindow.focus();";
  print OUT "}\n";
  print OUT " </script>\n";
  print OUT " </head>\n";
  print OUT " <!-- LAS: Redirect to browser. -->\n";
  print OUT " <body>\n";

# if the original list is longer than $max_profs, we want to show the list in parts
# to avoid overloading the browser with a huge list
# if we are at the beginning of the list $props->{profile_list_start} doesn't exist
# if we are in the middle or at the end of the list $props->{profile_list_start} does exist

  my $max_profs = 100;
  my $prof_count= 0;
  my $list_start = 0;
  my $list_end = $list_start + $max_profs;
  my $next_request = "";
  my $next_button_html = "";
  if (defined($props->{profile_list_start})){
      $list_start = $props->{profile_list_start};
      $list_end = $list_start + $max_profs;
  }
  if($list_end > $tot_num_profs){
      $list_end = $tot_num_profs;
  }else{
      $next_reqHead .= "%3Cprofile_list_start+%3E".$list_end."%3C%2Fprofile_list_start%3E";
      $next_request =  "$next_reqHead"."$reqFill"."$reqRegion"."$reqOrigRegion"."$reqTail";
      my $next_button_amount = "";
      if ($list_end + 100 > $tot_num_profs){
	  $next_button_amount = $tot_num_profs - $list_end;
      }else{
	  $next_button_amount = 100;
      }
      $next_button_html = "<h2><a href=\"$next_request\">Next $next_button_amount profiles</a></h2>\n";
  }
  my $real_start = $list_start + 1;
  my $real_end = $list_end;
  print OUT "<h2>Listing $real_start thru $real_end of $tot_num_profs profiles found in the region selected.</h2>";
  if (defined($next_button_html)){
      print OUT "$next_button_html";
  }
  print OUT " <h2>Click on a Profile number to request a plot.</h2>\n";

  print OUT <<EOF;
     <table width="90%" border=0>
        <tr>
          <td><div align="center">Profile Number</div></td>
          <td>Longitude</td>
          <td>Latitude</td>
          <td>Time</td>
        </tr>
EOF

  my $minutes = 0;

  for (my $i=$list_start; $i<$list_end; $i++) {
    $prof_id  = $prof_ids[$i];
    $lon = sprintf("%9.4f",$lons[$i]);
    $lon =~ s/ //g;
    $lat = sprintf("%8.4f",$lats[$i]);
    $lat =~ s/ //g;
    $hour  = int($hours[$i]);
    $minutes = ($hours[$i] - $hour) * 60;
    $minute = int($minutes);
    $second = ($minutes - $minute) * 60;
    $date = $origin->addDelta(0,0,0,$hour,$minute,$second);
    $dateString = $date->toFerretString();

    if ($index != $prof_id) {
  
      print OUT "        <tr>\n";
      print OUT "          <td><div align=\"center\">";
      print OUT "<a href=\"javascript:set_link($lon,$lat,\'$dateString\')\">$prof_id</a>";
      print OUT "</div></td>\n";
      print OUT "          <td>$lon</td>\n";
      print OUT "          <td>$lat</td>\n";
      print OUT "          <td>$dateString</td>\n";
      print OUT "        </tr>\n";
 
      $index = $prof_id;
      $prof_count++;
    }
#    last if($prof_count >= $max_profs);

  }
  print OUT "      </table><br>\n";
  print OUT " </body>\n";
  print OUT "</html>\n";

  close OUT;
}


sub insitu_single_profile {
    my ($self) = @_;
    my $outputView = "xy";
    my $props = $self->{props};
    my $var = $self->{vars}->[0];
    $props->{dataset_title} = $var->getDataset->getLongName;
    $props->{var_title} = $var->getAttribute('name');
    my @args0 = qw(dataset_name_0 variable_name_0 dataset_title var_title);
    $self->runJournal('insitu_setup_single', \@args0, undef, 'insitu_setup_single', \@args0);
    my $comm = "go insitu_single" . " \"" . $props->{variable_name_0}."_single\"";
    $self->command($comm);
    $self->genImage($self->{output_file});
}

############################################################
#
# The rest are internal routines used to generate
# requests for and access the non-NetCDF data.
#
############################################################

sub getVariableConstraint {
    my ($self, $constraint) = @_;
    my $value = $constraint->getValue;
    $value =~ s/\s+//g;
    next if (! defined $value || $value eq "");
#
# Let Perl evaluate the validity of the expression for us
#
    {
        my $lastwarn = $SIG{'__WARN__'};
        local($SIG{'__WARN__'}) = sub { $@ = $_[0]};
        eval "use strict; $value";
        die "Invalid constraint expression: '$value' -- must be a number" if $@;
    }
    my $var = $constraint->getVariable->getName;
    my $op = $constraint->getOp;

    my @values = ($var, $op, $value);
    return @values;
}

# TODO 06JAN2005 (jm) move database specific formatting to individual DB drivers
# I mean below only three values and 'Always quote non-numeric strings'
sub getTextConstraint {
    my ($self,$constraint) = @_;
    my @values = $constraint->getValues;
    if (scalar @values != 3){
        die "Text constraints must have three and only three values";
    }

# Always quote non-numeric strings.

    unless ($values[2] =~ /^([+-]?)(?=\d|\.\d)\d*(\.\d*)?([Ee]([+-]?\d+))?$/) {
    	$values[2] = "\"" . $values[2] . "\"";
    }

    return @values;
}

# TODO 06JAN2005 Need to test with DB Drivers other than WODB
sub getTextFieldConstraint {
    my ($self,$constraint) = @_;
    my @values = split(',',$constraint->getValue);
    if (scalar @values != 3){
        die "Textfield constraints must have three and only three comma separated values, i.e. \"name,operator,value\"\n";
    }
    return @values;
}

sub getExternalData {
    my ($self, $dsetname, $isMetaOnly) = @_;
    my $props = $self->{props};
    my @regArgs = ($props->{x_lo}, $props->{x_hi},
		   $props->{y_lo}, $props->{y_hi},
		   $props->{z_lo}, $props->{z_hi},
		   $props->{t_lo}, $props->{t_hi});
    @regArgs = map { defined($_) ? $_ : 0 } @regArgs;

    # In order to enable reuse of existing query results so that a user can look
    # at different visualizations without having to download data every time, we
    # will generate a data file name below that is based on the database query.
    # In the case of data products, rather than visualizations, the qurey results
    # must be in the output file that LAS has specified.  So we will leave the 
    # file name alone in this case.

    my ($name,$output_path,$suffix) = fileparse($self->{output_file}, '\.(nc|cdf)');
    my $output_file = "";
    if ( $suffix ) {
      $output_file = $self->{output_file};
    }

    my $DBConfig = $self->{db_config};

    my $db_type = $DBConfig->{db_type};
    my $db;
    if ($db_type eq "mysql") {
      debug("Creating new TMAP::DBI::Dataset\n");
      $self->{db} = $db = new TMAP::DBI::Dataset($DBConfig);
    } elsif ($db_type eq "Dapper"){
      debug("Creating new TMAP::Dapper::Dataset\n");
      $self->{db} = $db = new TMAP::Dapper::Dataset($DBConfig);
    } elsif ($db_type eq "JGOFS") {
      debug("Creating new TMAP::JGOFS::Dataset\n");
      $self->{db} = $db = new TMAP::JGOFS::Dataset($DBConfig);
    } elsif ($db_type eq "OSMC") {
      debug("Creating new TMAP::OSMC::Dataset\n");
      $self->{db} = $db = new TMAP::OSMC::Dataset($DBConfig);
    } elsif ($db_type eq "WODB") {
      debug("Creating new TMAP::WODB::Dataset\n");
      $self->{db} = $db = new TMAP::WODB::Dataset($DBConfig);
    } else {
      die "getExternalData: Database type \"$db_type\" not recognized.\n";
    }

    $db->{isMetaOnly} = $isMetaOnly;
    
    my @varnames = @{$props->{variable_name}};
    my @vars = @{$self->{vars}};
    foreach my $varname (@varnames){
	my $var = shift @vars;
        $db->setVariableAttributes($varname,$var->getLongName,$var->getAttribute("units"),$DBConfig->{missing});
	$var->setURL($output_file);
    }

    if (defined ($DBConfig->{profID})){
        my $prof_ID = $DBConfig->{profID};
        unless (grep(/$prof_ID/i,@varnames)){
            $db->setVariableAttributes($prof_ID,'Profile ID', "", $DBConfig->{missing});
        }
    }

    if (defined ($DBConfig->{cruiseID})){
        $db->setVariableAttributes($DBConfig->{cruiseID},'CRUISE ID','unitless',$DBConfig->{missing});
    }

# Include any string variables that are mentioned in the <string_vars> property
# First check to see that the correct number of items is listed in each of
# <string_vars>, <string_var_lengths> and <string_var_titles>.
# If these all match then add these variables to the DB driver list of variables
# and also add them to a list of names to be consulted in insitu_data().

  if (defined ($DBConfig->{string_vars})) {
    my @string_vars = split(',',$DBConfig->{string_vars});
    my @string_var_lengths = split(',',$DBConfig->{string_var_lengths});
    my @string_var_titles = split(',',$DBConfig->{string_var_titles});
    if ($#string_vars != $#string_var_lengths) {
      debug("LAS::Server::Ferret getExternLData: error: <string_vars> and <string_var_lengths> don't match");
      die $@;
    }
    if ($#string_vars != $#string_var_titles) {
      debug("LAS::Server::Ferret getExternLData: error: <string_vars> and <string_var_titles> don't match");
      die $@;
    }
    for (my $i=0;$i<=$#string_vars;$i++) {
      $db->setStringVariableAttributes($string_vars[$i],$string_var_titles[$i],$string_var_lengths[$i]);
      push(@{$self->{string_vars}},$string_vars[$i]);
      push(@{$self->{string_var_lengths}},$string_var_lengths[$i]);
      push(@{$self->{string_var_titles}},$string_var_titles[$i]);
    }
  } 


    # Get the longitude domain lo/hi values and do a validity check
    my ($lon_domain_lo,$lon_domain_hi) = split(':',$DBConfig->{lon_domain});
    if (($lon_domain_hi - $lon_domain_lo) ne 360) {
      die "Installation Error:  The longitude domain specified by \n<lon_domain> [" . $lon_domain_lo . ":" . $lon_domain_hi . "] must span 360 degrees.\n";
    }
    $DBConfig->{lon_domain_lo} = $lon_domain_lo;
    $DBConfig->{lon_domain_hi} = $lon_domain_hi;

    $db->setAxisAttributes("x",$DBConfig->{longitude},"Longitude","degrees_east");
    $db->setAxisAttributes("y",$DBConfig->{latitude},"Latitude","degrees_north");
    if ($DBConfig->{depth}) {
      $db->setAxisAttributes("z",$DBConfig->{depth},"Depth",$DBConfig->{depth_units});
    }

    # NOTE:  The time axis units must be set to 'hours'.
    $db->setAxisAttributes("t",$DBConfig->{time},"Time","hours");
    my $start = new TMAP::Date($regArgs[6]);
    die "Invalid date: $start" if ! $start->isOK;
    my $end = new TMAP::Date($regArgs[7]);
    die "Invalid date: $end" if ! $end->isOK;
    $db->setTimeRange($start,$end);
    $db->setTimeOrigin($start); 

    $db->setTable($DBConfig->{db_table});

    $db->addAxisConstraints(@regArgs[0..5], $start->toString, $end->toString);

    # Add any user specified constraints
    #
    my $constraint_num = 0;
    my @constraints = $self->{req}->getConstraints;

    foreach my $constraint (@constraints){

      # Print each constraint to the debug file.

      foreach my $key (keys (%{$constraint})){
	if(ref($constraint->{$key}) eq 'ARRAY'){
          debug("Constraint: $key == array: (",
		 join(',', @{$constraint->{$key}}), ")\n");
	}elsif(ref($constraint->{$key}) eq 'HASH'){
	  debug("Constraint: $key == hash: (");
          while (my ($key,$value) = each %{$constraint->{$key}}) {
            debug(", $key=$value");
          } 
	  debug(")\n");
        }else{
          debug("Constraint: $key == $constraint->{$key}\n");
        }
      }

      # Add each constraint using the driver specific method. 

      my @constraint_values = ();
      if (ref($constraint) eq 'LAS::Constraint::Variable'){
        @constraint_values = $self->getVariableConstraint($constraint);
      } elsif (ref($constraint) eq 'LAS::Constraint::Text'){
        @constraint_values = $self->getTextConstraint($constraint);
      } elsif (ref($constraint) eq 'LAS::Constraint::TextField'){
        @constraint_values = $self->getTextFieldConstraint($constraint);
      } else {
        die "Unknown constraint type:", ref($constraint);
      }
      my $constraint_label = $db->addConstraint(@constraint_values);

      # A text string description of each constraint is returned and needs
      # to be added to the properties for use in labeling the plot
      
      my $constraint_name = "constraint_" . $constraint_num;
      $self->{props}->{$constraint_name} = $constraint_label;
      $constraint_num++;

    }
    
    # For visualization products the output_file has been set to a null string.
    # Here we create a new outputfile name based on the database query.  This
    # will allow multiple visualizations to reuse the same underlying data.

    if ($output_file eq "") {
      my $queryString = $db->getQueryString();
      my $md5 = new MD5;
      $md5->reset;
      $md5->add($queryString);
      my $digest = $md5->digest;
      #$digest = unpack("H*", $digest);
      $digest = unpack("H*", $digest);
      $output_file = $output_path . $digest . ".nc";

      my @varnames = @{$props->{variable_name}};
      my @vars = @{$self->{vars}};
      foreach my $varname (@varnames) {
	my $var = shift @vars;
	$var->setURL($output_file);
      }
    }

    #
    # Only get data if not cached
    #
    if (! -f $output_file) {
      eval {
        my $elapsed = "";
        debug("getExternalData: calling getData\n");
        $db->startTimer();
        $db->getData();
        $elapsed = $db->getElapsed();
        debug("getExternalData: $elapsed secs to get data from the database\n");
        $db->writeCDFFile($output_file);
        $elapsed = $db->getElapsed();
        debug("getExternalData: $elapsed secs to write the CDF file\n");
        $db->disconnect();
        debug("getExternalData: Finished creating CDF file $output_file\n");
      };
      if ($@){
        debug("getExternalData: data read/write error: $@");
        die $@;
      }
    } else {
      debug("getExternalData: Reusing cached data \"$output_file\"\n");
    }

    $self->{props}->{dataset_title} = $self->{props}->{dataset_name_0};
    $self->{props}->{dataset_name_0} = $output_file;
    $self->{props}->{db_title} = $DBConfig->{db_title};

    #
    # Set all of the dataset names to be the same as the netCDF output file
    #
    my @new_names;
    for (@{$props->{dataset_name}}){
	push(@new_names, $output_file);
    }
    $props->{dataset_name} = \@new_names;

    debug("getExternalData: Completely finished, returning now.\n");
    my $tmp = join(',',@new_names);
}

sub accessDatabase {
  my $self = shift;
  my $var = $self->{vars}->[0];
  my $dsetname = $var->getDataset->getAttribute('url');

  # Get the database properties specified in the dataset .xml file.
  # A hash of these properties is passed to the individual database drivers
  # that use this information to interact with various back end datbases.

  $self->{db_config} = {};
  $self->{db_config} = &LAS::mergeProperties($self->{db_config},
                       scalar $var->getDataset->getProperties('database_access'));
  $self->{db_config}->{dsetname} = $dsetname;
  $self->debugProps('database_access', $self->{db_config});

  # Only invoke the getExternalData routine if there is a database type
  # specified in the xml.  In 'normal' cases, the 'db_type' attribute
  # will be empty which means that this is just a normal NetCDF file
  # and the call getExternalData is unnecessary.
  
  if ($self->{db_config}->{db_type}) {

    $self->{method} = $self->{req}->getOp->getAttribute("method");
    debug("method is: ",$self->{method},"\n");
    my $isMetaOnly = 0;
    if($self->{method} =~ m/insitu_meta_xy/     || 
       $self->{method} =~ m/insitu_tsum/        ||
       $self->{method} =~ m/insitu_cruise_list/ ||
       $self->{method} =~ m/insitu_profile_list/){
      $isMetaOnly = 1;
    }

    my $numvars = scalar @{$self->{vars}};

    die "Property/property plot requires two (and only two) variables; found $numvars" if (($self->{method} eq "insitu_property") && $numvars != 2);

    eval{
      return $self->getExternalData($dsetname, $isMetaOnly);
    };
    if ($@){
      debug("LAS::Server::Ferret accessDatabase: error: $@");
      die $@;
    }      

  }

}

1;
