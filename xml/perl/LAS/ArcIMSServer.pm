#
# $Id: ArcIMSServer.pm,v 1.2 2005/01/21 23:31:15 callahan Exp $
#
# ArcIMSServer.pm
#
# Contributed code from John Cartwright at NGDC.
#
# Handler for an ArcIMS server back end that formats an ArcIMS
# request, sends it, parses the response and retrieves an
# image.

use XML::Parser;
use XML::SimpleObject;
use LWP::UserAgent;
use LWP::Simple;   # not sure why the get() method is not recognized
use HTTP::Request::Common;
use Date::Calc qw(:all);


# Declare a new package
# Set up inheritance from LAS::Server::Handler base class

package LAS::Server::ArcIMSserver;
@LAS::Server::ArcIMSserver::ISA = qw(LAS::Server::Handler);


#TODO replace this with TMAP::Date
# utility method to convert a date in format "dd-mmm-yyyy" to ISO8601 style "yyyy-mm-dd"
sub iso8601 {
	# this is a standin for a pre-built and more functional DateTime::Format::ISO8601
	my $in = shift;  # expect format of "dd-mmm-yyyy"
	my ($year,$month,$day) = &Date::Calc::Decode_Date_EU($in);
	return($year . "-" . sprintf("%02d",$month) . "-" . sprintf("%02d",$day));
}

# The init method is required.
# init is passed the LAS::Request object as well as the
# output file
sub init {
    my ($self, $req, $output) = @_;
    $self->{req} = $req;
    $self->{output} = $output;
}


# Operation referenced by <aims> operation in XML config file
# Dump the variable and region info
sub aims {
   my $self = shift;
   my $output = $self->{output};
   my $is_error = 0;

# Hack to map back to image sizes listed in options.xml

   my %image_sizes = (
       "0.5"     => [800,600],
       "0.06667" => [300,200],
       "0.25"    => [600,400],
       "0.8333"  => [1000,800],
       "0.3333"  => [800,400],
       "0.2552"  => [700,350],
       "0.1875"  => [600,300]
    );

   open OUTPUT, ">$output" or die "Can't open ", $self->{output};
   open LOG, ">log/arcims" or die "Can't open logfile\n";
   print LOG "output file is $output\n";

   # Line buffer the output
   #my $last = select OUTPUT;
   #$| = 1; select $last;

   #use the last Variable as a handle to the Dataset
   #assume all variables share same axis
   my $dataset,%props,@layers,@ids,$mapservice,$url,$description;
   foreach ($self->{req}->getChildren) {
      my $class = ref ($_);
      if ($class eq "LAS::Variable") {
         # LAS Variable == ArcIMS Layer
         push @layers, $_->getName();
         $description = $_->getLongName();
         $url = $_->getURL;
         push @ids, $_->getProperties('aims')->{'id'};
 
         #the only type of children that a variable can have are axes
         $dataset = $_->getDataset();
 
      } elsif ($class eq "LAS::Region"){
         # only expecting LAS::Range objects
         foreach $child ($_->getChildren){
            if (ref($child) eq "LAS::Range") {
               if ($child->getAttribute("type") eq "x") {
                  $minx = $child->getLo;
                  $maxx = $child->getHi;
               } elsif ($child->getAttribute("type") eq "y") {
                  $miny = $child->getLo;
                  $maxy = $child->getHi;
               }
            }
         }
      }
   }
   print LOG "Dataset: ",$dataset->getName(),"\n";
   $mapservice = $dataset->getProperties('aims')->{'mapservice'};
 
   # set parameters that apply to all layers
   print LOG "mapservice: $mapservice\n";
   print LOG "Layers: ",join(",",@ids),"\n";
 
 
   #
   # get output map image size
   #
   $ferret_size = $self->{req}->getProperties('ferret')->{size};
   print LOG "ferret imageSize = $ferret_size\n";
 
   $map_size = $self->{req}->getProperties('ferret')->{map_size};
   my ($width,$height) = split(/;/,$map_size);
 
   # TODO
   if ($height eq 'auto') {
      # calc image height based on latitude range
 
   }
 
   print LOG "map imageSize = $map_size\n";
   $props = $self->{props};
 
   $width = $self->{req}->getProperties('ferret')->{imgWidth};
   $height = $self->{req}->getProperties('ferret')->{imgHeight};
 
   #TODO hardcoded width,height
   $width = 800;
   $height = 400;
 
   # create AXL request
   $request_axl = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
   $request_axl .= "<ARCXML version=\"1.1\">\n";
   $request_axl .= "<REQUEST>\n";
   $request_axl .= "<GET_IMAGE>\n";
   $request_axl .= "<PROPERTIES>\n";
   $request_axl .= "<ENVELOPE ";
   $request_axl .= "minx=\"$minx\" ";
   $request_axl .= "miny=\"$miny\" ";
   $request_axl .= "maxx=\"$maxx\" ";
   $request_axl .= "maxy=\"$maxy\" />\n";
   $request_axl .= "<IMAGESIZE width=\"$width\" height=\"$height\" />\n";
   $request_axl .= "<LAYERLIST nodefault=\"true\" order=\"true\">\n";
   foreach $id (reverse @ids) {
      $request_axl .= "<LAYERDEF id=\"$id\" visible=\"true\" />\n";
   }
   $request_axl .= "</LAYERLIST>\n";
   $request_axl .= "</PROPERTIES>\n";
   $request_axl .= "</GET_IMAGE>\n";
   $request_axl .= "</REQUEST>\n";
   $request_axl .= "</ARCXML>\n";
   print LOG "Request AXL:\n$request_axl\n";
 
  # convert the request xml to html and add to diagnostics string
   my $s = $request_axl;
   $s =~ s/</&lt;/g;
   $s =~ s/>/&gt;/g;
   $s =~ s/\n/<br>/g;
   my $diag = "<h4>AXL Request:</h4>";
   $diag .= $s;
   $diag .= "<br><br>";
 
   # transmit request and receive response
   my $ua = new LWP::UserAgent;
   my $req = new HTTP::Request POST => "$url?ServiceName=$mapservice";
   $req->content($request_axl);
   $diag .=  "<br><br>".$req->as_string()."<br><br>";
   #print LOG "\n\n",$self->{output};
   my $res = $ua->request($req);
 
   # Check the outcome of the response
   if ($res->is_success) {
      $response_axl = $res->content;
      # response returns with UTF8 despite request being encoded as "UTF-8"
      $response_axl =~ s/UTF8/UTF-8/;
      print LOG "Response AXL: $response_axl\n";
   } else {
      $diag .= "<font color=\"red\">Yikes - looks like there has been a problem communicating with the ArcIMS server</font><br>";
      $is_error = 1;
      print LOG "Yikes - looks like there has been a problem communicating with the ArcIMS server\n";
      print LOG "URL: $url?ServiceName=$mapservice\n";
   }
 
   # convert the response xml to html and add to diagnostics string
   $s = $response_axl;
   $s =~ s/</&lt;/g;
   $s =~ s/>/&gt;<br>/g;
   $diag .= "<h4>AXL Response:</h4>";
   $diag .= $s;
 
   # parse response
   my $img_url;
   my $parser = new XML::Parser(ErrorContext=>2, Style=>"Tree");
   my $xso = new XML::SimpleObject($parser->parse($response_axl));
   if ($xso->child('ERROR')) {
      print LOG "Error in parsing the response\n";
      $is_error = 1;
   } elsif ($xso->child('ARCXML')->child('RESPONSE')->child('ERROR')) {
      print LOG "Error in parsing the response\n";
      $is_error = 1;
   } else {
      $img_url = $xso->child('ARCXML')->child('RESPONSE')->child('IMAGE')->child('OUTPUT')->attribute('url');
      print LOG "Image URL: $img_url\n";
   }
   if ($format eq 'html') {
      # print diagnostics, do not request map image from server
      print OUTPUT  "<HTML><HEAD>";
      print OUTPUT "<TITLE>ArcIMS Request Diagnostics Page</TITLE>";
      print OUTPUT "</HEAD><BODY>";
      print OUTPUT "$diag";
   } else {
      # retrieve the image
      #print OUTPUT LWP::Simple->get($img_url);   # not sure why this does not work
      $req = HTTP::Request->new(GET => "$img_url");
      $res = $ua->request($req);
      if ($res->is_success) {
         print OUTPUT $res->content;
      } else {
         print LOG "ERROR: problem retrieving response\n";
      }
   }
   close OUTPUT;
   close LOG;
}
	

#
# Operation referenced by <wms> operation in XML config file
# calls to WMS server to generate a map image
#
sub wms {
   my $self = shift;
   my $output = $self->{output};

   open OUTPUT, ">$output" or die "Can't open ", $self->{output};
   open LOG, ">log/arcims" or die "Can't open logfile\n";

   # Line buffer the output
   #my $last = select OUTPUT;
   #$| = 1;
	
   print LOG "output file is $output\n";

   #grab the first variable as a handle to the dataset
   #assume all variable share same axis
   my $dataset,%props;
   foreach ($self->{req}->getChildren) {
      my $class = ref ($_);
      if ($class eq "LAS::Variable") {
         #the only type of children that a variable can have are axes

         # calculate array indices for x, y, t
         foreach $child ($_->getChildren){
            if ($child->getAttribute("type") eq "x") {
               $xAxisStart = $child->getLo;
               $xAxisEnd   = $child->getHi;
               $xAxisSize  = $child->getSize;
               $xAxisStep  = $child->getStep;
               print LOG "X Axis (start,end,size,step): $xAxisStart, $xAxisEnd, $xAxisSize, $xAxisStep\n";
            }
         }

         $dataset = $_->getDataset();
	 print LOG "Dataset: ",$dataset->getName(),"\n";
         %props = $dataset->getProperties('wms');
         last;
      }
   }
	
   # set parameters that apply to all layers
   my $mapservice =  $props{'mapservice'};
	print LOG "mapservice: $mapservice\n";
   my $srs = $props{'srs'};
	print LOG "srs: $srs\n";
   my $wmtver = $props{'wmtver'};
	if ($wmtver eq "") {
	   $wmtver = "1.0";
	}
	print LOG "wmtver: $wmtver\n";
   my $exceptions = $props{'exceptions'};
   my $bgcolor = $props{'bgcolor'};
   my $transparent = $props{'transparent'};
   
   my @layers,@styles,$style;
   foreach ($self->{req}->getChildren) {
      my $class = ref ($_);
      if ($class eq "LAS::Variable"){
         $varname = $_->getName();
         $layerName = $_->getLayerName;
         if ($layerName) {
            push @layers, $layerName;
         } else {
            push @layers, $varname;
         }
         $description = $_->getLongName();
         $url = $_->getURL;
         # get layer-specific properties
         %props = $_->getProperties('wms');
         push @styles,$props{'style'};
      } elsif ($class eq "LAS::Region") {
         # only expecting LAS::Range objects
         foreach $child ($_->getChildren){
            if (ref($child) eq "LAS::Range") {
               if ($child->getAttribute("type") eq "x") {
                  $minx = $child->getLo;
                  $maxx = $child->getHi;
               } elsif ($child->getAttribute("type") eq "y") {
                  $miny = $child->getLo;
                  $maxy = $child->getHi;
               }
            } elsif (ref($child) eq "LAS::Point") {
               if ($child->getAttribute("type") eq "t") {
                  #datetime formatted as "dd-MMM-YYYY"
                  print LOG $child->getValue(),"\n";
                  $dt = iso8601($child->getValue());
                  print LOG "$dt\n";
               }
            }
         }
      }
   } 

   print LOG "Layers: ",join(",",@layers),"\n";
   print LOG "Styles: ",join(",",@styles),"\n";
   
   $width = $self->{req}->getProperties('ferret')->{imgWidth};
   $height = $self->{req}->getProperties('ferret')->{imgHeight};
   #todo get actual values from options
   $width = 800;
   $height = 600;

   # WMS likes "JPEG" instead of "JPG"?
   $format = $self->{req}->getProperties('ferret')->{format};
   
   $bbox = "$minx,$miny,$maxx,$maxy";
   print LOG "bbox: $bbox\n";

   if ($format eq 'html') {
      # print diagnostics, do not request map image from server
      print OUTPUT  "<HTML><HEAD>";
      print OUTPUT "<TITLE>WMS Request Diagnostics Page</TITLE>";
      print OUTPUT "</HEAD><BODY>";
      print OUTPUT "<b>URL: </b>$url<br>";
      print OUTPUT "<ul>";
      print OUTPUT "<li>servicename=$mapservice";
      print OUTPUT "<li>WMTVER=$wmtver";
      print OUTPUT "<li>BBOX=$bbox";
      print OUTPUT "<li>FORMAT=$format";
      print OUTPUT "<li>WIDTH=$width";
      print OUTPUT "<li>HEIGHT=$height";
      print OUTPUT "<li>LAYERS=",join(",",@layers);
      print OUTPUT "<li>SRS=$srs";
      print OUTPUT "<li>EXCEPTIONS=$exceptions";
      print OUTPUT "<li>BGCOLOR=$bgcolor";
      print OUTPUT "<li>TRANSPARENT=$transparent";
      print OUTPUT "<li>STYLES=",join(",",@styles);
      print OUTPUT "<li>TIME=$dt";
      print OUTPUT "</ul>";
      print OUTPUT "<br>Output file name: $output<br>";
      print OUTPUT  "</BODY></HTML>";
   } else {
      # build URL string
      # digitalearth does not seem to like parameters unless part of URL string
      # add the required parameters common to both 1.0 and 1.1
      $url .= "?BBOX=$bbox&WIDTH=$width&HEIGHT=$height&LAYERS=".join(",",@layers)."&SRS=$srs";
      
      #TODO match partial version e.g. "1.1"
      if ($wmtver eq "1.1.1") {
         $url .= "&REQUEST=GetMap&VERSION=$wmtver&FORMAT=image/".$format;
         #TODO add 1.1-specific exception MIME types
         $url .= "&EXCEPTIONS=INIMAGE";
         #$url .= "&EXCEPTIONS=XML";
         # add the 1.1-specific optional parameters
         if ($dt ne "") {
            $url .= "&TIME=$dt";
         }
      } else {
         $url .= "&REQUEST=map&WMTVER=$wmtver&FORMAT=$format&EXCEPTIONS=INIMAGE";
         # add the 1.0-specific optional parameters
      }
      # add the optional parameters common to all versions
      if ($mapservice ne "") {
         $url .= "&SERVICENAME=$mapservice";
      }
      if ($transparent ne "") {
         $url .= "&TRANSPARENT=$transparent";
      }
      if ($bgcolor ne "") {
         $url .= "&BGCOLOR=$bgcolor";
      }
      if (scalar(@styles) > 0) {
         $url .= "&STYLES=" . join(",",@styles);
      }
         

      # transmit request and receive response
      $ua = new LWP::UserAgent;
      #$ua->agent("LiveAccessServer/0.1 " . $ua->agent);

      my $req = HTTP::Request->new(GET => "$url");
      print LOG "Request: ",$req->as_string();
      print LOG "\n\nOutput file is: ",$self->{output},"\n\n";
      my $res = $ua->request($req);
      print LOG "Response ContentType: ",$res->content_type,"\n";

      # Check the outcome of the response
      if ($res->is_success) {

         #TODO check content_type instead?
         if ($res->content =~ /<?xml/) {
            
            print LOG "Error detected\n";
            #print LOG $res->content(),"\n";
            # parse response
            my $parser = new XML::Parser(ErrorContext=>2, Style=>"Tree");
            my $xso = new XML::SimpleObject($parser->parse($res->content()));
            print LOG $xso->child('ServiceExceptionReport')->child('ServiceException')->value(), "\n";
            
         #TODO generate an error image instead - way to control the HTML that gets written?
         }
         # write the image into the output file directly
         print OUTPUT  $res->content;
      } else {
         #TODO  should generate an error image instead
         print OUTPUT "<HTML>\n";
         print OUTPUT "<HEAD><TITLE>Error Generating Map</TITLE></HEAD>\n";
         print OUTPUT "<BODY>\n";
         print OUTPUT "   <font color=\"red\"><b>Error creating map</b></font>\n";
         print OUTPUT "</BODY>\n";
         print OUTPUT "</HTML>\n";
      }
   }

   close OUTPUT;
   close LOG;
}

#
# Following are utility routines for this demo
#
sub printVariable {
    my $var = shift;
    print OUTPUT "<h3>Variable</h3>\n";
    print OUTPUT "<b>Name</b>  ", $var->getName, " <b>URL</b> ", $var->getURL;
}

sub printRegion {
    my $region = shift;
    print OUTPUT "<h3>Region</h3>\n";
    foreach my $child ($region->getChildren){
   my $class = ref($child);
   if ($class eq "LAS::Range"){
       print OUTPUT "<b>Range</b>  ", $child->getLo, ":", $child->getHi;
   } elsif ($class eq "LAS::Point"){
       print OUTPUT "<b>Point</b>  ", $child->getLo;
   } else {
       die "Unknown region child:", $class;
   }
   print OUTPUT " <b>type</b> ", $child->getAttribute("type"), "<br>";
    }
}

#
# dump the contents of the given properties hash
sub printProps {
   print "Props Hash for variable $varname:\n";
   foreach my $key (keys %props) {
      if (ref($props{$key}) eq 'ARRAY') {
         print "props: $key => array: (",join(", ",@{$props{$key}}), ")\n";
      } elsif (ref($props{$key}) eq 'HASH') {
         print "props: $key => hash: (";
         while (my ($key,$value) = each %{$props->{$key}}) {
            print ", $key => $value";
         }
         print ")\n";
      } else {
         print "prop: $key => $props{$key}\n";
      }
   }
}


1;
