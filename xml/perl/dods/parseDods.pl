#!/usr/bin/perl -w
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

use strict;


use LASDods;

my @names =
    qw(http://ferret.wrc.noaa.gov/cgi-bin/nph-nc/data/coads_climatology.nc);

my @candidates = ();
if ($ARGV[0]){
    @names = ();
    @candidates = @ARGV;
}
foreach my $id (@candidates){
    if (-f $id){
	open FOO, $id or die "Can't open $id";
	while(<FOO>){
	    chomp;
	    next if /^\s*#|^\s*$/;
	    die "Invalid URL: $_" if ! /^\s*http:/;
	    push(@names,$_);
	}
	close FOO;
    } else {
	push(@names, $id);
    }
}

foreach my $name (@names){
    my $dods = new LAS::DODS($name);

    print "Dataset URL: $name\n";
    my %varHash = %{$dods->getVariables};
    foreach my $key (keys %varHash){
	my $var = $varHash{$key};
	print "Var: ", $var->getName, "\n";
	
	print "\tDims:\n";
	foreach my $dim (@{$var->getDims}){
	    print "\t\t", $dim->getName, ':', $dim->getSize, "\n";
	    if ($dim->getVar){
		my $data = $dim->getVar->getData;
		print "\t\tData:\n\t\t", join(':', @{$data}), "\n";
	    }
	}
	print "\tAttributes:\n";
	my $atts = $var->getAttributes;
	foreach my $name (keys %{$atts}){
	    my $value = $atts->{$name};
	    print "\t\tName: $name Values:", join(':',@{$value}), "\n";
	}
    }

# Global attributes last
    print "Global attributes\n";
    my $atts = $dods->getAttributes;
    foreach my $name (keys %{$atts}){
	my $value = $atts->{$name};
	print "\tName: $name Values:", join(':',@{$value}), "\n";
    }
}    
