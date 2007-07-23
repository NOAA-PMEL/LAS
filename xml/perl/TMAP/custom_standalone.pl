#!/usr/bin/perl -w
#
# custom_standalone.pl
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: custom_standalone.pl,v 1.1.1.1 2004/03/01 23:46:48 callahan Exp $

# Standalone version of custom.pl for testing access to
# databases.
#
# NOTE:  THIS CODE SHOULD BE RUN FROM $lasroot/las/server
#

# The following information should be changed to match 
# your database:

my $dsetname = "wodb";
my @regArgs = (-20, 20, 20, 60, 0, 300, "01-jan-1982", "30-jan-1982");
my $output_file = "database_test.nc";
my @variable = ("TEMP");

#
# DO NOT ALTER THE CODE BELOW THIS POINT.

use lib qw(./custom ../xml/perl);
use strict;
use File::Basename;
use TMAPDate;
use TMAPDBI;
use TMAPJGOFS;
use TMAPWODB;

&getExternalData($dsetname);

sub getExternalData {
    my ($dsetname) = @_;

    @regArgs = map { defined($_) ? $_ : 0 } @regArgs;

    require("TMAPDBConfig.pl");
    my $DBIConfig = DBConfigInit($dsetname);

    my $DBConfig = DBConfigInit($dsetname);
    my $db_type = $DBConfig->{db_type};
    my $db;
    if ($db_type eq "mysql") {
      print("Creating new TMAP::DBI::Dataset\n");
      $db = new TMAP::DBI::Dataset($DBConfig);
    } elsif ($db_type eq "JGOFS") {
      print("Creating new TMAP::JGOFS::Dataset\n");
      $db = new TMAP::JGOFS::Dataset($DBConfig);
    } elsif ($db_type eq "WODB") {
      print("Creating new TMAP::WODB::Dataset\n");
      $db = new TMAP::WODB::Dataset($DBConfig);
    } else {
      die "getExternalData: Database type \"$db_type\" not recognized.\n";
    }

    $db->setVariableAttributes(@variable);

    $db->setAxisAttributes("x",$DBConfig->{longitude},"Longitude","degrees_east");
    $db->setAxisAttributes("y",$DBConfig->{latitude},"Latitude","degrees_north");
    if ($DBConfig->{depth}) {
      $db->setAxisAttributes("z",$DBConfig->{depth},"Depth","meters");
    }

    # NOTE: The time axis units must be set to 'hours'.
    $db->setAxisAttributes("t",$DBConfig->{time},"Time","hours");
    my $start = new TMAP::Date($regArgs[6]);
    die "Invalid date: $start" if ! $start->isOK;
    my $end = new TMAP::Date($regArgs[7]);
    die "Invalid date: $end" if ! $end->isOK;
    $db->setTimeRange($start,$end);
    $db->setTimeOrigin($start); 

    $db->setTable($DBConfig->{db_table});

    $db->addAxisConstraints(@regArgs[0..5], $start->toString, $end->toString);

    # Specify an appropriate constraint here

    my $constraint_value = "";
    if ($constraint_value) {
      $db->addConstraint($constraint_value);
    }


    #
    # Only get data if not cached
    #
    if (! -f $output_file) {
      eval {
        print("getExternalData: calling getData\n");
        $db->getData();
        #$db->printData();
        $db->writeCDFFile($output_file);
        $db->disconnect();
        print("getExternalData: Finished creating CDF file $output_file\n");
      };
      if ($@){
        die "getExternalData: data read/write error: $@" ;
      }
    }

}









