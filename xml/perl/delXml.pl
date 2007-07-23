#!/usr/bin/perl
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: delXml.pl.in,v 1.6 2002/10/13 00:05:24 sirott Exp $

BEGIN {
    use File::Basename;
    unshift(@INC,dirname $0);
}

sub usage {
    print <<EOF;
Usage: delXml.pl infile outfile url
EOF
    exit 1;
}

use Config;
use lib ('../xml/perl','.', '../xml/perl/install/lib/' . $Config{version},
         '../xml/perl/install/lib/site_perl/' . $Config{version},
         '../xml/perl/install/lib/perl5/' . $Config{version},
         '../xml/perl/install/lib/perl5/site_perl/' . $Config{version});
use LAS;
require "LASNetCDF.pm";

my $inFile = shift @ARGV;
my $outFile = shift @ARGV;

usage if ! $inFile || ! $outFile;

my $parser = new LAS::Parser($inFile);
my $config = new LAS::Config($parser);

my $first = shift @ARGV;

usage if ! $first;

my $urlContainer = $config->findURL($first);
die "Can't find $first in $infile" if !$urlContainer;
my $element = $urlContainer->getElement;

# Eliminate axes
my %killem;
foreach my $var ($urlContainer->getChildren){
    foreach my $axis ($var->getChildren){
	$killem{$axis->getName} = $axis;
    }
}
foreach my $killit (keys %killem){
    my $e = $killem{$killit}->getElement;
    $e->getParentNode->removeChild($e);
}

# Eliminate grid
my $dsetName = $element->getTagName;
my $gridName = $dsetName . "_grid";
my $linkName = "/lasdata/grids/" . $gridName;
my $doc = $element->getOwnerDocument;
my $grid =
    LAS::findElementByPath($doc->getDocumentElement, $element, $linkName, 1);
die "Can't find grid for $dsetName" if ! $grid;
$grid->getParentNode->removeChild($grid);

# Eliminate dataset element
$element->getParentNode->removeChild($element);

close STDOUT;
open STDOUT, ">$outFile" or die "Couldn't open $outFile";

prettyPrint $config->toXML;






