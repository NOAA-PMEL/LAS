#
# $Id: SGDServer.pm,v 1.2 2005/01/21 23:35:01 callahan Exp $
#
# SGDerver.pm
#
# Contributed code from John Cartwright at NGDC.
#
# Handler for a SGD server back end that formats a SGD
# request, sends it, parses the response and retrieves an
# image.

use XML::Parser;
use XML::SimpleObject;
use LWP::UserAgent;
use LWP::Simple;   # not sure why the get() method is not recognized
use HTTP::Request::Common;
use Date::Calc qw(:all);

# Define a new SGDServer package
# Set up inheritance from LAS::Server::Handler base class

package LAS::Server::SGDServer;
@LAS::Server::SGDServer::ISA = qw(LAS::Server::Handler);

# The init method is required.
# init is passed the LAS::Request object as well as the
# output file
sub init {
    my ($self, $req, $output) = @_;
    $self->{req} = $req;
    $self->{output} = $output;
    &LAS::Server::add_mime('geotiff','text/html'); #Add a new mime type for 'geotiff' op
}


sub geotiff {
   my $self = shift;
   my $output = $self->{output};
   open OUTPUT, ">$output" or die "Can't open ", $self->{output};
   # Line buffer the output
   #my $last = select OUTPUT; $| = 1; select $last;

   open LOG, ">log/geotiff" or die "Can't open logfile\n";


   my @children = $self->{req}->getChildren;
   my $var;
   foreach (@children){
      my $class = ref ($_);
      if ($class eq "LAS::Variable"){
         $var = $_;
         break;
      }
   }

   my $url = "http://lynx.ngdc.noaa.gov/SDG/servlet/SDG?data=";
   $url .= $var->getURL();
   print LOG "url = $url\n";

   #my $document = get($url);
   # HACK - do a little screen-scraping to get the parts of interest
   my @lines = split(/\n/,get($url));
   print OUTPUT "<br><br>";
   print OUTPUT @lines[17..22];


   #
   # Dump the variable and region info.
   #
   my @children = $self->{req}->getChildren;
   foreach (@children){
      my $class = ref ($_);
      if ($class eq "LAS::Variable"){
         print LOG "calling getGeoTIFF() with $_\n";
         #getGeoTIFF($_);
         #printVariable($_);
      } elsif ($class eq "LAS::Region"){
         printRegion($_);
      } else {
         die "Unknown argument type: $class";
      }
   }
   close OUTPUT;
}


#
# Following are private utility routines for this Server
#
sub getGeoTIFF {
   use LWP::Simple;
   my $var = shift;
   open OUTPUT, ">$output" or die "Can't open ", $self->{output};
   my $url = "http://www.cdc.noaa.gov/SDG/servlet/SDG?data=";
   $url .= $var->getURL();
   $url .= "?lat,lon,";
   $url .= $var->getName();
   #my $document = get($url);
   # HACK - do a little screen-scraping to get the parts of interest
   my @lines = split(/\n/,get($url));
   print OUTPUT "<br><br>";
    print OUTPUT @lines[17..22];
    print OUTPUT "<br>";
    #print OUTPUT "$document<br>\n";
}


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


1;
