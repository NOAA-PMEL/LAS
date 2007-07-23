# Cruise section list and plot methods
# include in insitu.pl with
# require ("cruise.pl");

package LAS::Server::Ferret;

## Subroutines in cruise.pl
# sub insitu_cruise_list {} Creates an html page listing cruises and links to section plots
# sub insitu_poly_dist_z {} sets up insitu properties (style, setup script, etc)
# sub cruise_graphic {} passes arguments to insitu_cruise drawing templates

# Insitu data xml configuration database_access element must include
# <cruiseID> and <equal_op> properties for insitu_cruise_list method
# <cruiseID> is the name of the Cruise ID variable in your database
# <equal_op> is the name of the equal operator which your database interface uses to
# signify an equality in a query, i.e. Ferret uses 'EQ' and mysql uses '='
#
# Add the "ops" menu to your insitu ui xml by adding
# <ifmenu view="z" href="#Ops_CruiseProfileZ"/> to the ui ops map
#
# The insitu database driver must include a query for Cruise_ID and commands in the 
# writeCDFFile() method to include the field in the insitu intermediate NetCDF file.
# see TMAPDBI.pm for examples

 
## sub insitu_cruise_list
# Creates an html page with a list of cruises found in the insitu database on the region requested
# The page includes links to individual cruise section plots.
sub insitu_cruise_list {
  my ($self) = @_;

  my $props = $self->{props};
  my $output_file = $self->{output_file};

# set up new LAS query, changing a couple of things from the old query
# TODO create LAS package to create and edit LAS queries instead of regex parsing
# Typical request
# xml=%3C%3Fxml+version%3D%221.0%22%3F%3E%3ClasRequest+package%3D%22%22+href%3D%22file%3Alas.xml%22+%3E%3Clink+match%3D%22%2Flasdata%2Foperations%2Finsitu_cruise_list%22+%2F%3E%3Cproperties+%3E%3Cferret+%3E%3Csize+%3E.5%3C%2Fsize%3E%3Cformat+%3Ehtml%3C%2Fformat%3E%3C%2Fferret%3E%3C%2Fproperties%3E%3Cargs+%3E%3Clink+match%3D%22%2Flasdata%2Fdatasets%2Fwodb%2Fvariables%2Ftemp%22+%2F%3E%3Cregion+%3E%3Crange+low%3D%2246.0%22+type%3D%22x%22+high%3D%22102.0%22+%2F%3E%3Crange+low%3D%22-11.0%22+type%3D%22y%22+high%3D%2217.0%22+%2F%3E%3Crange+low%3D%220%22+type%3D%22z%22+high%3D%22700%22+%2F%3E%3Crange+low%3D%2216-Jan-1991%22+type%3D%22t%22+high%3D%2227-Feb-1991%22+%2F%3E%3C%2Fregion%3E%3C%2Fargs%3E%3C%2FlasRequest%3E
  my $z_lo = $self->{props}->{z_lo};
  my $z_hi = $self->{props}->{z_hi};
  my $http_host = $ENV{HTTP_HOST};
  my $query = $ENV{REQUEST_URI};

# $request is the URL request for the html javascript set_link function
# $next_request is the URL request for Next button (next 100 cruises) on the html page

  $query =~ m/(^.+insitu)_cruise_list(.+)(%3Cformat....)html(......format%3E)(.+)(%3Cregion.%3E?)(.+%3C%2Fregion%3E.+$)/;
  my $reqHead = "http://$http_host";
  $reqHead .= "$1"."_poly_dz"."$2$3"."gif"."$4";
  my $next_reqHead = "http://$http_host";
  $next_reqHead .= "$1"."_cruise_list"."$2$3"."html"."$4";
  my $reqFill = $5;
  my $reqRegion = "$6";
  my $reqTail = "$7";
  $reqFill =~ s/%3Ccrus_list_start\+%3E.+%3C%2Fcrus_list_start%3E//g;
  my $cruise_id_name = $self->{db_config}->{cruiseID};
  my $equal_op =  $self->{db_config}->{equal_op};
  my $reqSection = "%3Cconstraint+type%3D%22textfield%22+value%3D%22$cruise_id_name,$equal_op,\"+cruise_id+\"%22+%2F%3E";
  my $request = "\""."$reqHead"."$reqFill"."$reqSection"."$reqRegion"."$reqTail"."\"";

# now open the netCDF file and get all the data
  my $cdf_file = $props->{dataset_name_0};
  my $cdf = new LAS::NetCDF($cdf_file);
 
  my $cruise_id = $cdf->getVariable("CRUISE_ID");
  my $xax = $cdf->getVariable("xax");
  my $yax = $cdf->getVariable("yax");
  my $tax = $cdf->getVariable("tax");

  my $time_origin = $tax->getAttribute("time_origin");
  my $origin = new TMAP::Date($time_origin);
  my $date = new TMAP::Date($time_origin);

  my @dims = $xax->getDims();
  my $index_dim = $dims[0];
  my $size = $index_dim->getSize();

  my @cruise_ids  = $cruise_id->getData([0],[$size]);
  my @lons = $xax->getData([0],[$size]);
  my @lats = $yax->getData([0],[$size]);
  my @hours = $tax->getData([0],[$size]);
  my ($index,$lon,$lat,$hour,$minute,$second) = (0,0,0,0,0,0);


# sort the data by cruise_id
  my @sorted_indices = sort {$cruise_ids[$a] <=> $cruise_ids[$b]} (0 .. $#cruise_ids);
  my @sorted_cruises = @cruise_ids[@sorted_indices];
  my @sorted_lons = @lons[@sorted_indices];
  my @sorted_lats = @lats[@sorted_indices];
  my @sorted_hours = @hours[@sorted_indices];

# create a list of the cruise_ids, eliminating duplicates and attach parameters
# range lon,lat,time
# range lon,lat,time
# number of profiles per cruise = scalar @cr_numprofs


  my @cruise_list = ();
  my @cr_numprofs = ();
  my @lon_range = ();
  my @lat_range = ();
  my @time_range = ();

# 
# start with assigning the first value in the lists, max and min will be calculated in following loop
push(@cruise_list,$sorted_cruises[0]);
  my $cr_profs_count = 1;
  my $lon_min = $sorted_lons[0];
  my $lon_max = $sorted_lons[0];
  my $lat_min = $sorted_lats[0];
  my $lat_max = $sorted_lats[0];
  my $time_min = $sorted_hours[0];
  my $time_max = $sorted_hours[0];
  my $list_index = 0  ;

# Now go through and compare in sorted order, if a new cruise number, record all of the accumulated info
  for(my $i=1;$i<@sorted_cruises;$i++){
      if($sorted_cruises[$i] != $sorted_cruises[$i-1]){ # A new cruise
	push(@cruise_list,$sorted_cruises[$i]);
        $cr_numprofs[$list_index] = $cr_profs_count;
	$lon_range[$list_index] = sprintf("%6.2f","$lon_min")." to ".sprintf("%6.2f","$lon_max");
	$lat_range[$list_index] = sprintf("%6.2f","$lat_min")." to ".sprintf("%6.2f","$lat_max");
	$time_range[$list_index] = sprintf("%6.2f","$time_min")." to ".sprintf("%6.2f","$time_max");

# reset all of the parameters with the first of the new cruise
	$lon_min = $sorted_lons[$i];
	$lon_max = $sorted_lons[$i];
	$lat_min = $sorted_lats[$i];
	$lat_max = $sorted_lats[$i];
	$time_min = $sorted_hours[$i];
	$time_max = $sorted_hours[$i];

        $list_index++;
	$cr_profs_count = 1;

      }else{ # it wasn't a new cruise, keep looking for max and min and increase the count by 1
	$cr_profs_count++;
	$lon_min = $sorted_lons[$i] if $lon_min > $sorted_lons[$i];
	$lon_max = $sorted_lons[$i] if $lon_max < $sorted_lons[$i];
	$lat_min = $sorted_lats[$i] if $lat_min > $sorted_lats[$i];
	$lat_max = $sorted_lats[$i] if $lat_max < $sorted_lats[$i];
	$time_min = $sorted_hours[$i] if $time_min > $sorted_hours[$i];
	$time_max = $sorted_hours[$i] if $time_max < $sorted_hours[$i];
      }
# This is the last profile so it wasn't processed in above loop
      if ($i == $#sorted_cruises){
        $cr_numprofs[$list_index] = $cr_profs_count;
	$lon_range[$list_index] = sprintf("%6.2f","$lon_min")." to ".sprintf("%6.2f","$lon_max");
	$lat_range[$list_index] = sprintf("%6.2f","$lat_min")." to ".sprintf("%6.2f","$lat_max");
	$time_range[$list_index] = sprintf("%6.2f","$time_min")." to ".sprintf("%6.2f","$time_max");
      }
  }

  my $tot_num_cruises = scalar @cruise_list;

# Now we format the html page
  open OUT, ">$output_file" or die "Can't open $output_file";
  print OUT "<html>\n";
  print OUT " <head>\n";
  print OUT " <script language=\"javascript\">\n";
  print OUT "  function set_link(cruise_id){\n";
  print OUT "   var link = $request\n";
  print OUT "   var dataWindow = window.open(link);\n";
  print OUT "   dataWindow.focus();\n";
  print OUT "}\n";
  print OUT " </script>\n";
  print OUT " </head>\n";
  print OUT " <!-- LAS: Redirect to browser. -->\n";
  print OUT " <body>\n";


# if the original list is longer than $max_cruises, we want to show the list in parts
# to avoid overloading the browser with a huge list
# if we are at the beginning of the list $props->{cruise_list_start} doesn't exist
# if we are in the middle or at the end of the list $props->{cruise_list_start} does exist

  my $max_cruises = 100;
  my $list_start = 0;
  my $list_end = $list_start + $max_cruises;
  my $next_request = "";
  my $next_button_html = "";
  if (defined($props->{crus_list_start})){
      $list_start = $props->{crus_list_start};
      $list_end = $list_start + $max_cruises;
  }
  if($list_end > $tot_num_cruises){
      $list_end = $tot_num_cruises;
  }else{
      $next_reqHead .= "%3Ccrus_list_start+%3E".$list_end."%3C%2Fcrus_list_start%3E";
      $next_request =  "$next_reqHead"."$reqFill"."$reqRegion"."$reqTail";
      my $next_button_amount = "";
      if ($list_end + 100 > $tot_num_cruises){
	  $next_button_amount = $tot_num_cruises - $list_end;
      }else{
	  $next_button_amount = 100;
      }
      $next_button_html = "<h2><a href=\"$next_request\">Next $next_button_amount cruises</a></h2>\n";
  }
  my $real_start = $list_start + 1;
  my $real_end = $list_end;
  print OUT "<h2>Listing $real_start thru $real_end of $tot_num_cruises cruises found in the region selected.</h2>";
  if (defined($next_button_html)){
      print OUT "$next_button_html";
  }
  print OUT " <h2>Click on a Cruise number to request a plot.</h2>\n";

  print OUT <<EOF;
     <table width="90%" border=0>
        <tr>
          <td>Number</td>
          <td><div align="center">Cruise Number</div></td>
          <td>Number of Profiles</td>
          <td>Longitude Range</td>
          <td>Latitude Range</td>
        </tr>
EOF
  my $minutes = 0;

  my $cruise_count = $list_start;
  for (my $i=$list_start; $i<$list_end; $i++) {
    $cruise_count++;
    my $cruise_id = $cruise_list[$i];
    my $cr_numprofs = $cr_numprofs[$i];
    my $lon_string = $lon_range[$i];
    my $lat_string = $lat_range[$i];

    print OUT "        <tr>\n";
    print OUT "          <td>$cruise_count</td>\n";
    print OUT "          <td><div align=\"center\">";
    print OUT "<a href=\"javascript:set_link($cruise_id)\">$cruise_id</a>";
    print OUT "</div></td>\n";

    print OUT "          <td>$cr_numprofs</td>\n";
    print OUT "          <td>$lon_string</td>\n";
    print OUT "          <td>$lat_string</td>\n";
    print OUT "        </tr>\n";

  }
  print OUT "      </table><br>\n";

  print OUT " </body>\n";
  print OUT "</html>\n";
  close OUT;
}


## sub insitu_poly_dist_z
# setup some graphic choice parameters
sub insitu_poly_dist_z {
    my ($self) = @_;
    $self->{props}->{style} = "poly_dist_z";
    $self->{props}->{outputView} = "";
    $self->{props}->{setupScript} = "insitu_cruise_setup";
    $self->cruise_graphic;
}

## sub cruise_graphic
# send args to ferret templates and draw the image
sub cruise_graphic {
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
    my @args1 = qw(dataset_name_0 variable_name_0 dataset_title
                   insitu_fill_levels insitu_palette
		   outputView style setupScript db_title
                   constraint_0 constraint_1 constraint_2 constraint_3
                   constraint_4 constraint_5 constraint_6 constraint_7);
 
    
    $self->runJournal('insitu_cruise_gif', \@args1, \@extraArgs);
    $self->genImage($self->{output_file});
}

1;
