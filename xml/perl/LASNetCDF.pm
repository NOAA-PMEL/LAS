#
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: LASNetCDF.pm,v 1.46 2005/03/21 23:47:42 callahan Exp $
#
use strict;
package LAS::NetCDF::Dim;
use NetCDF;

sub new {
    my ($class, $cdf, $dimid) = @_;
    my $ncid = $cdf->getId;
    my $self = {};
    bless $self, $class;

    my $name = "";
    my $size = "";
    NetCDF::diminq($ncid, $dimid, \$name, \$size);
    $self->{size} = $size;
    $self->{name} = $name;
    $self->{parent} = $cdf;
    return $self;
}

sub getName {
    my $self = shift;
    return $self->{name};
}

sub getSize {
    my $self = shift;
    return $self->{size};
}

sub getVar {
    my $self = shift;
    return $self->{parent}->getVariable($self->{name});
}

sub getData {
    my $self = shift;
    my $cdf = $self->{parent};
    my %vars = $cdf->getVariables;
    my $var = $vars{$self->{name}};
    my @data;
    if ($var){
	my $start = [0];
	my $count = [$self->getSize];
	@data = $var->getData($start, $count);
    }
    return @data;
}

package LAS::NetCDF::Var;
use NetCDF;
use Carp;

no strict 'subs';
my %DataConverter = (
		     scalar(NetCDF::BYTE), "byte",
		     scalar(NetCDF::SHORT), "short",
		     scalar(NetCDF::CHAR), "char",
		     scalar(NetCDF::INT), "int",
		     scalar(NetCDF::FLOAT), "float",
		     scalar(NetCDF::DOUBLE), "double"
		     );
sub typeToString {
    my $datatype = shift;
    return $DataConverter{$datatype};
}

sub new {
    my ($class, $cdf, $varid) = @_;
    my $ncid = $cdf->getId;
    my $self = {};
    bless $self, $class;
    my ($name, $datatype, $ndims, @dimids, $natts);
    NetCDF::varinq($ncid, $varid, \$name, \$datatype, \$ndims, \@dimids, \$natts);
    $self->{parent} = $cdf;
    $self->{varid} = $varid;
    $self->{type} = $datatype;
    $self->{typeString} = typeToString($datatype);
    $self->{name} = $name;
    $self->{atts} = {};
    $self->{dims} = [];
#
# Get dimensions
#
    my $dims = $self->{dims};
    for (my $i=0; $i < $ndims; $i++){
	my $dimid = $dimids[$i];
	my $dim = new LAS::NetCDF::Dim($cdf, $dimid);
	unshift(@{$dims}, $dim);
    }

#
# Get all attributes
#
    my $attHash = $self->{atts};
    for (my $i=0; $i < $natts; $i++){
	my $name = "";
	my $value = "";
	my $atttype="";
	my $len = "";
	NetCDF::attname($ncid, $varid, $i, \$name);
	NetCDF::attinq($ncid, $varid, $name, \$atttype, \$len);
#
# The NetCDF module doesn't properly handle passing an array
# to attget. If the attribute is a scalar, it should return the
# scalar as the first (and only) element of the array. But, it
# doesn't. You can't use the length of the attribute to determine
# whether or not to use an array because character attributes
# have a length > 1, yet are really scalars. So, just turn off the
# annoying error message...
	my $lastwarn = $SIG{'__WARN__'};
	$SIG{'__WARN__'} = sub {};
	NetCDF::attget($ncid, $varid, $name, \$value);
	$SIG{'__WARN__'} = $lastwarn;
   $value =~ s/\x00//g;
	$attHash->{$name} = $value;
    }
    return $self;
}

sub getType {
    my $self = shift;
    return $self->{type};
}

sub getTypeString {
    my $self = shift;
    return $self->{typeString};
}

sub isCoord {
    my ($self) = @_;
    my @dims = @{$self->{dims}};
    return 0 if (scalar @dims != 1);
    my $dim = $dims[0];
    return 1 if $dim->getName eq $self->getName;
    return 0;
}

sub getData {
    my ($self, $start, $count) = @_;
    my @data = ();
    my $ncid = $self->{parent}->getId;
    my $varid = $self->{varid};
    NetCDF::varget($ncid, $varid, @{$start}, @{$count}, \@data);
    return @data;
}

sub getName {
    my $self = shift;
    return $self->{name};
}

sub getAttribute {
    my ($self, $key) = @_;
    return $self->{atts}->{$key};
}

sub getAttributes {
    my $self = shift;
    return %{$self->{atts}};
}

sub getDims {
    my $self = shift;
    return @{$self->{dims}};
}

sub getRank {
    return scalar @{$_[0]->{dims}};
}

package LAS::NetCDF;

use Carp;
#use strict;
use vars qw($VERSION @ISA @EXPORT);
use NetCDF;
use LAS;

sub new {
    my $self = {};
    my ($class, $fileName) = @_;
    my $ncid = NetCDF::open($fileName, NetCDF::NOWRITE);
    NetCDF::opts(NetCDF::VERBOSE);
    die "Couldn't open netCDF file $fileName" if ($ncid < 0);
    bless $self, $class;
    $self->{ncid} = $ncid;
    $self->{globalAtts} = {};
    $self->{variables} = {};
    my ($foo, $path) = split(/\:\/\//, $fileName);
    if ($path){
	$self->{url} = $fileName;
    } else {
	$self->{url} = "file:" . $fileName;
    }
    $self->_initialize;

    return $self;
}

sub DESTROY {
    my $self = shift;
    $self->close;
}

sub close {
    my $self = shift;
    if ($self->{ncid} >= 0){
	NetCDF::close($self->{ncid});
    }
    $self->{ncid} = -1;
}


sub getURL {
    my $self = shift;
    return $self->{url};
}

sub getId {
    my $self = shift;
    return $self->{ncid};
}

sub _initialize {
    my $self = shift;
    my $ncid = $self->{ncid};
    my ($ndims, $nvars, $ngatts, $natts, $recdim);
    NetCDF::inquire($ncid, \$ndims, \$nvars, \$ngatts, \$recdim);

#
# Get all global attributes
#
    my $attHash = $self->{globalAtts};
    for (my $i=0; $i < $ngatts; $i++){
	my $name = "";
	my $value = "";
	NetCDF::attname($ncid, NetCDF::GLOBAL, $i, \$name);
	NetCDF::attget($ncid, NetCDF::GLOBAL, $name, \$value);
   $value =~ s/\x00//g;
	$attHash->{$name} = $value;
    }

#
# Get all variables
#
    my $varHash = $self->{variables};
    for (my $i=0; $i < $nvars; $i++){
	my $var = new LAS::NetCDF::Var($self, $i);
	my $name = $var->getName;
	$varHash->{$name} = $var;
    }
}

sub getVariable {
    my ($self,$name) = @_;
    return $self->{variables}->{$name};
}

sub getVariables {
    my $self = shift;
    return %{$self->{variables}};
}

sub getAttribute {
    my ($self, $key) = @_;
    return $self->{globalAtts}->{$key};
}

sub getAttributes {
    my $self = shift;
    return %{$self->{globalAtts}};
}

package LAS::NetCDF::DateConvert;
#
# Parse time units string of form:
# <units> since YYYY-MM-DD HH:MM:SS
# and translate netCDF axis values to date/time string. 
# Does not currently handle pre-Gregorian calendar dates correctly
#
use Date::Calc qw(check_date Add_Delta_YMD Add_Delta_DHMS);
use Date::Manip;
use TMAPDate;
use Carp;
my %TimeMults = (
    sec=>86400.0,
    min=>1440.0,
    hour=>24.0,
    day=>1.0,
    mon=>0.0328549, # = (12/365.2425)
    year=>0.0027379 # = ( 1/365.2425)
    );
my @SpecialUnits = qw(mon year);



# calculate the julian day using Gregorian calendar
sub julian_day_gregorian {
    my ($y,$m,$d,$h,$min,$s)= @_;
    if ($m < 3){
	$y--;
	$m += 12;
    }
    my $a = int($y/100);
    my $b = int($a/4);
    my $c = int(2-$a+$b);
    my $e = int(365.25*($y+4716));
    my $f = int(30.6001*($m+1));
    return $c+$d+$e+$f-1524.5;
}

sub julian_day_julian {
    my ($y,$m,$d,$h,$min,$s)= @_;
    if ($m < 3){
	$y--;
	$m += 12;
    }
    return  $d + int((153 * $m - 457) / 5) + 365 * $y + int($y/4)+ 1721116.5;
}

sub julian_day {
    my ($y,$m,$d,$h,$min,$s)= @_;
    if ($y > 1582 || ($y == 1582 && ($m > 10 || ($m==10 && $d >=15)))){
	return julian_day_gregorian(@_) +  + &HMSToFracDay($h,$min,$s);
    } else {
	return julian_day_julian(@_) +  + &HMSToFracDay($h,$min,$s);
    }
}

# calculate the inverse julian day using Gregorian calendar
sub inverse_julian_day_gregorian {
    my($jd) = @_;
    my($month,$day,$year);
    my $z = int($jd - 1721118.5) ;
    my $r = $jd - 1721118.5 - $z ;
    my $h = 100*$z - 25 ;
    my $a = int($h / 3652425) ;
    my $b = $a - int($a / 4) ;
    $year = int((100*$b+$h) / 36525) ;
    my $c = $b + $z - 365 * $year - int( $year/4) ;
    $month = int((5 * $c + 456) / 153) ;
    $day = int($c - int((153 * $month - 457) / 5) + $r) ;
    if ($month > 12) {
	$year = $year + 1 ;
	$month = $month - 12 ;
    }

#    warn "$month,$day,$year,$jd";
    return (sprintf("%.4d",$year), sprintf("%.2d",$month),
	    sprintf("%.2d", $day));
}

sub inverse_julian_day_julian {
    my($jd) = @_;
    my($month,$day,$year);
    my $z = int($jd - 1721116.5) ;
    my $r = $jd - 1721116.5 - $z ;
    $year = int(($z - 0.25) / 365.25) ;
    my $c = $z - int(365.25 * $year) ;
    $month = int((5 * $c + 456) / 153) ;
    $day = $c - int((153 * $month - 457) / 5) + $r ;
    if ($month > 12){
	$year = $year + 1 ;
	$month = $month - 12 ;
    }
#    warn "$month,$day,$year,$jd";
    return (sprintf("%.4d",$year), sprintf("%.2d",$month),
	    sprintf("%.2d", $day));
}

sub inverse_julian_day {
    my($jd) = @_;
    my @inverse = ();
    if ($jd < 2299160.5){	# Gregorian start date of 1582-10-14
	@inverse = inverse_julian_day_julian($jd);
    } else {
	@inverse = inverse_julian_day_gregorian($jd);
    }
    return (@inverse, &fracDayToHMS($jd));
}

sub new {
    my ($class, $instr) = @_;
    my $self = {};
    bless $self, $class;

#
# Either a LAS::Var reference, or a string for $instr
#
    if (ref($instr)){
	my $units = $instr->getAttribute("units");
	my $origin = $instr->getAttribute("time_origin");
	croak "No units defined for ", $instr->getName if ! $units;
	$_ = $units;
	if ($units =~ "yyyymmddhhmmss"){
	    $self->{is_yyyymmdd} = 1;
	    return $self;
	} elsif ($units !~ /since/){
	    croak "Invalid time units: '$units'" if ! $origin;
	    $units = "$units since $origin";
	    $_ = $units;
	}
    } else {
	$_ = $instr;
    }
    tr/A-Z/a-z/;
    my ($units, $since, $dateStr, $time) = split;

    #Support for yyyy-mm-dd:hh:mm:ss format 
    if ($dateStr =~ /:\d\d:\d\d:\d\d/){
	my @temp = split(/:/,$dateStr);
        $dateStr = shift(@temp);
        $time = join(':',@temp);
    }

# Check to see if the date is valid

    my $date = new TMAP::Date($dateStr);
    croak "Bad date '$dateStr'" if ! $date->isOK;
    my ($y,$mo,$d) = $date->getYMD;

    my ($h,$m,$s);
   ($h,$m,$s) = split(/:/,$time) if $time;
    $h = 0 if ! $h;
    $m = 0 if ! $m;
    $s = 0 if ! $s;

    $self->{jd} = julian_day($y, $mo, $d, $h, $m, $s);
    $self->{yr} = $y;
    $self->{mo} = $mo;
    $self->{d} = $d;
    $self->{h} = $h;
    $self->{m} = $m;
    $self->{s} = $s;
    $self->{units} = $units;

#
# See if special date handling is required
#

    $self->{useSpecial} = 0;
    foreach (@SpecialUnits){
	if ($units =~ /$_/){
	    $self->{useSpecial} = 1;
	    last;
	}
    }

#
# We can just multiply by appropriate constant
#
    my $trans;
      foreach (keys %TimeMults){
        if ($units =~ /$_/){
    	$trans = $TimeMults{$_};
    	last;
        }
    }
    croak "Can't find translator for units string '$units'"
        if ! $trans;
    $self->{trans} = $trans;

    return $self;
}

sub HMSToFracDay {
    my ($h,$m,$s) = @_;
    return ($s + 60 * $m + 3600 * $h/86400.);
}

sub fracDayToHMS {
    my $jd = shift;
    my $value = $jd - int($jd);
    $value -= 0.5;
    $value += 1 if ($value < 0);
    croak "Bad fractional day: $value" if $value < 0 || $value >= 1;
    my $frac = $value * 24.0;
    my $h = int($frac);
    my $rem = $frac - $h;

    $frac = $rem * 60;
    my $m = int($frac);
    $rem = $frac - $m;

    my $s = $rem * 60;
    $s = int($s + 0.5);
    if ($s == 60){
	$s = 0;
	++$m;
    }
    if ($m == 60){
	$m = 0;
	++$h;
    }
    return (sprintf("%.2d", $h), sprintf("%.2d", $m), sprintf("%.2d", $s));
}

sub calcDeltaYMD {
    my ($self, $jd0, $jd1) = @_;
    my $err;
    my @ymd0 = inverse_julian_day($jd0);
    my @ymd1 = inverse_julian_day($jd1);
#
# Hack to get parse date to work with years <= 1
    if ($ymd0[0] <= 1){
	$ymd0[0] += 3000;
	$ymd1[0] += 3000;
    }
    my ($y0,$m0,$d0,$h0,$min0,$s0) = @ymd0;
    my ($y1,$m1,$d1,$h1,$min1,$s1) = @ymd1;
    my $da0 = ParseDate("$y0-$m0-$d0 $h0:$min0:$s0");
#    warn "$y0-$m0-$d0 $h0:$min0:$s0";
    die "Can't parse date" if !$da0;
    my $da1 = ParseDate("$y1-$m1-$d1 $h1:$min1:$s1");
    die "Can't parse date" if !$da1;
    my $delta = DateCalc($da0,$da1, \$err, 1);
#    warn join(':',@ymd0), ",", join(':', @ymd1), ",", $delta;
    if ($ymd0[0] <= 1){
	$ymd0[0] -= 3000;
	$ymd1[0] -= 3000;
    }
    my ($y,$m,$wk,$d, $h,$mi,$s) = split(/:/, $delta);
#
# Hack alert -- a month is not always a month with climate datasets.
# Consider a month to be 4 wks and 0-3 days or 1 month and 0-3 day
#
    if ($y == 0){
	if ($wk == 4 && ($d >= 0 && $d <= 3)){
	    ($y,$m,$wk,$d) = (0,$m+1,0,0);
	} elsif ($wk == 0 && $m > 0 && ($d >= 0 && $d <= 3)){
	    ($y,$m,$wk,$d) = (0,$m,0,0);
	}
    }
    if ($wk != 0){
	$d = $d + $wk*7;
    }
    if ($delta =~ /^-/){
	return (-$y,-$m,-$d,$h,$mi,$s);
    } else {
	return ($y,$m,$d,$h,$mi,$s);
    }

    if ($y == 0 && $m == 0 && $d == 0 && $h == 0 && $mi == 0 && $s == 0){
	croak "Can't have delta time of zero";
    }
}

sub deltaDays {
    my ($self, $date0, $date1) = @_;

    my $base = $self->{jd};
    my $trans = $self->{trans};
    $date0 /= $trans;		# Convert to days
    $date0 += $base;		# Add base date
    $date1 /= $trans;		# Convert to days
    $date1 += $base;		# Add base date
    my $delta = $date1 - $date0;
    die "Zero date step" if abs($delta) < 1.157e-8; # 1 ms
    return $self->calcDeltaYMD($date0, $date1);
}

#
# For this format, everything is from 1900
#
sub timeTrans_yyyymmdd {
    my ($self, $value) = @_;
    my @results = ();
    my $start = 1e10;
    while ($start >= 1){
	my $nval = int($value/$start);
	push(@results, $nval);
	$value  -= $nval * $start;
	$start /= 100.0;
    }
    $results[2] += 1;		# Days are counted from 1, not 0
    return @results;
}

sub timeTrans {
    my ($self, $value) = @_;
    if ($self->{is_yyyymmdd}){
	return $self->timeTrans_yyyymmdd($value);
    }
    my $base = $self->{jd};
    my $trans = $self->{trans};
    $value /= $trans;		# Convert to days
    $value += $base;		# Add base date
    my ($y, $mo, $d, $h, $m, $s) = inverse_julian_day($value);
#    warn "$y, $mo, $d, $h, $m, $s,", inverse_julian_day($value);
    return ($y, $mo, $d, $h, $m, $s);
}

sub dateTrans {
    my ($self, $value) = @_;
    my $units = $self->{units};
    my $val_int = int($value);
    my $val_rem = $value - $val_int;
    my $days = 0;

    my ($y,$m,$d) = ($self->{yr}, $self->{mo}, $self->{d});
    my @result;
    if ($units =~ /mo/){	# months
        $days = int($val_rem * 365.2425/12);
	@result = Add_Delta_YMD($y,$m,$d,0,$value,$days);
    } else {			# years
        $days = int($val_rem * 365.2425);
	@result = Add_Delta_YMD($y,$m,$d,$value,0,$days);
    }
    push @result, ($self->{h},$self->{m},$self->{s});
    return @result;
}

sub translate {
    my ($self, $value) = @_;
    if ($self->{useSpecial}){
	$self->dateTrans($value);
    } else {
	$self->timeTrans($value);
    }
}

package LAS::NetCDF::XML;
use Carp;
my @TimeUnits = qw(sec min hour day mon yr year);
use LAS;

sub validXML {
    my ($in) = @_;
    my $out = $in;
    $out =~ s/[\-\.]/_/g;
    return $out;
}
    

sub new {
    my ($class, $cdf, $config,$parser, $isUpdate, $longName, $docurl) = @_;
    my $self = {
	cdf => $cdf,
	config => $config,
	parser => $parser,
	longName => defined $longName && $longName,
	docurl => defined $docurl && $docurl,
	isUpdate => defined $isUpdate && $isUpdate,
	updatedGrid => 0,	# Grid updated (only for isUpdate mode)
    };
    bless $self, $class;

    $self->genDataset;
    
    return $self;
}

sub updateDataset {
    my $self = shift;
    die "Can't update datasets (yet)";
}


sub isValidTimeDim {
    my ($dim) = @_;
    my $dimvar = $dim->getVar;
    my $units = $dimvar->getAttribute("units");
    my $origin = $dimvar->getAttribute("time_origin");
    my $rval = $units && ($units =~ "yyyymmddhhmmss" || $units =~ /since/
			  || $origin);
    return $rval;
}

sub compressTimeAxis {
    my ($self, $axis, $dim, $type, $howMany) = @_;

    my $canCompress = 1;

    my $dimvar = $dim->getVar;
    my $units = $dimvar->getAttribute("units");
    my @data = $dimvar->getData([0], [$howMany]);
    my $date = new LAS::NetCDF::DateConvert($dimvar);
    my @first = $date->translate($data[0]);
    my @last = $date->translate($data[$#data]);

    # All year 1 and <=12 items -- must be monthly climatology
    return if $first[0] == 1 && $last[0] == 1 && $#data<=11;

    my $formattedFirst = sprintf("%.4d-%.2d-%.2d %.2d:%.2d:%.2d",
	    @first);

    my $data1 = shift(@data);
    my $data2 = shift(@data);
    my @delta = $date->deltaDays($data1,$data2);
    foreach (@data){
	$data1 = $data2;
	$data2 = $_;
	my @ndelta = $date->deltaDays($data1, $data2);
	for (my $i = 0; $i < scalar @delta; $i++){
	    if ($ndelta[$i] != $delta[$i]){
		$canCompress = 0;
		last;
	    }
	}
	last if ! $canCompress;
    }

    if ($canCompress){
#  # Only one of y,m,d,h can have changed
  	my $count = 0;
  	foreach (@delta){
  	    $count++ if $_ > 0;
  	}
  	return if ($count > 1);

        return ($formattedFirst, $delta[0], "year") if $delta[0] > 0;
        return ($formattedFirst, $delta[1], "month") if $delta[1] > 0;
        return ($formattedFirst, $delta[2], "day") if $delta[2] > 0;
        return ($formattedFirst, $delta[3], "hour");
    }
}

sub compressRegAxis {
    my ($self, $axis, $dim, $type, $howMany) = @_;

    my $canCompress = 1;

    my $dimvar = $dim->getVar;
    my @data = $dimvar->getData([0], [$howMany]);
    my $ffirst = my $first = shift @data;
    my $second = shift @data;
    my $delta = $second - $first;
    foreach (@data){
	$first = $second;
	$second = $_;
	my $ndelta = $second - $first;
	if ($ndelta != $delta){
	    $canCompress = 0;
	    last;
	}
	last if ! $canCompress;
    }

    if ($canCompress){
	return ($ffirst, $delta, $dimvar->getAttribute("units"));
    }
}

sub compressAxis {
    my ($self, $axis, $dim, $type, $howMany) = @_;
    my $size = $dim->getSize;
    return if $size < 2;

    $howMany = $size < $howMany ? $size : $howMany;
    if ($type eq 't' && isValidTimeDim($dim)){
	return $self->compressTimeAxis($axis, $dim, $type, $howMany);
    } else {
	return $self->compressRegAxis($axis, $dim, $type, $howMany);
    }
}

sub genAxisData {
    my ($self, $var, $axis, $dim, $type) = @_;
#
# Eliminate all appropriate axis children if in update mode
#
    if ($self->{isUpdate}){
	foreach my $child ($axis->getElement->getChildNodes){
	    if ($child->getNodeType == XML::DOM::ELEMENT_NODE){
		my $tagname = $child->getTagName;
		next if ! ($tagname eq "v" || $tagname eq "arange");
		$axis->getElement->removeChild($child);
	    }
	}
    }

    my $dimvar = $dim->getVar;
    my @months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
    croak "Dimension ", $dim->getName, " doesn't have a coordinate variable"
	if ! $dimvar;

    $dimvar = new LAS::NetCDF::DummyVar($dim->getName, $dim->getSize)
	if ! $dimvar;

    my $spacing;
    if (ref($dimvar) =~ /Dummy/){
	$spacing = "even";
    } else {
	$spacing = $dimvar->getAttribute("point_spacing");
	$spacing = "uneven" if !$spacing;
    }
    my $size = $dim->getSize;

#
# See if axis info can be compressed
#
    if ($spacing ne "even"){
	my ($start, $delta,$units) =
	    $self->compressAxis($axis, $dim, $type,50);
	if ($units){
	    $spacing = "even";
	    $axis->getElement->setAttribute("units",$units);
	    $axis->addRegularData($start, $delta, $size);
	    return;
	}
    }
    if ($size > 1 && $spacing eq "even"){
	my ($start, $delta, $units) =
	    $self->compressAxis($axis, $dim, $type,50);
	if ($units){
	    $axis->getElement->setAttribute("units",$units);
	} else {
	    if (!$start){
		carp "Warning: Dimension: ", $dim->getName, " has an axis ",
		"incorrectly marked as regular.\n",
		"Using first two data points and assuming axis is regular";
		($start, $delta,$units) =
		    $self->compressAxis($axis, $dim, $type,2);
	    }
	}
	$axis->addRegularData($start, $delta, $size);
	return;
    } 

    my $start = [0];
    my $count = [$size];
    my @data = $dimvar->getData($start, $count);
#
# Format time axes as time strings
#
    my $formattedData = \@data;
    my $units = $dimvar->getAttribute('units');
    if ($type eq 't' && $units){
	my @newdates = ();
	my $date = new LAS::NetCDF::DateConvert($dimvar);
	$axis->getElement->setAttribute("units",$date->{units});
	foreach (@data){
	    my ($y,$mo,$d,$h,$m,$s) = $date->translate($_);
	    my $cmo = $months[$mo-1];
	    if ($y == 1 &&$#data <= 11){	# Monthly climatology
		push(@newdates, $mo);
	    } else {
		if ( $h != 0 || $m != 0 || $s!= 0 ){
		    push(@newdates, sprintf("%s-%s-%s %.2d:%.2d:%.2d",
					    $y, $mo, $d,
					    int($h), int($m), int($s)));
		} else {
		    push(@newdates, "$y-$mo-$d");
		}
	    }
	}
	$formattedData = \@newdates;
    }
    $axis->addIrregularData($formattedData);
}

#
# Use same heuristics as Ferret in guessing axis type
#
sub guessAxisType {
    my ($name, $dimvar) = @_;
    my $units = $dimvar->getAttribute("units");
    my $type;
    $name =~ tr/A-Z/a-z/;
    if ($units){
	$units =~ tr/A-Z/a-z/;
#
# Guess based on units
#
	foreach (@TimeUnits){
	    if ($units =~ /$_/){
		return 't';
	    }
	}
	if ($units =~ /deg/){
	    if ($units =~ /north|south/){
		$type='y';
	    } elsif ($units =~ /east|west/){
		$type='x';
	    }
	} elsif ($units =~ /lat/){
	    $type = 'y';
	} elsif ($units =~ /lon/){
	    $type = 'x';
	} elsif ($units =~ /mb|decibar|level|layer/){
	    $type = 'z';
	}
    }
    return $type if $type;

#
# If positive attribute set to up or doen, must be Z axis
#
    if ($dimvar->getAttribute("positive")) {
      if ($dimvar->getAttribute("positive") =~ /up|down/i) {
	return 'z';
      }
    }

#
# Guess based on name
#
    if ($name =~ /^x|lon/){
	$type = 'x';
    } elsif ($name =~ /^y|lat/){
	$type = 'y';
    } elsif ($name =~ /time|date|^t/){
	$type = 't';
    } elsif ($name =~ /level|depth|elev|height|^z/){
	$type = 'z';
    }
    return $type;
}

sub genAxes {
    my ($self, $varobj, $var, $gridElem) = @_;
    my @dims = $var->getDims;
    my $count = 0;
    my @guessAxes = qw(t z y x);

# Insert check for curvilinear variables - let user know they will have to hand edit
# the axis definitions in the xml file  
# This is based on the assumption that curvilinear variables have a coordinate attribute
# *kob* 1/2005
    my $curvilinear = 0;
    if ($var->getAttribute("coordinates")) {
	my $varname = $var->getName;
	$curvilinear = 1;
	print "\n***\tIt appears that the variable $varname is a curvilinear variable.\n";
        print "***\tAxes ranges for $varname will have to be modified by hand\n\n";
    }


    foreach (@dims){
	my $dim = $_;
	my $dimvar = $dim->getVar;
	my $atts = {};
	if ($dimvar) {
# check for any bounds dimension - addXML should just ignore these for now because LAS
# doesn't need to know about them.  *kob* 1/2005
	    if (($dimvar->{name} !~ /bnds/) ){
		my $units = $dimvar->getAttribute("units");
		$atts->{units} = $units if $units;
		$atts->{type} = guessAxisType($dim->getName, $dimvar);
		if (!$atts->{type}){
		    if ($#dims == 3){
			$atts->{type} = $guessAxes[$count];
		    } else {
			croak "Can't determine orientation of axis '", $dim->getName,
			"'";
		    }
		}
	    
		my $axis =
		    $varobj->addAxis($self->{config}, $dim->getName, $gridElem, $atts);
		$self->genAxisData($varobj, $axis, $dim, $atts->{type});
		$count++;
	    }
	}
    }
}

sub createGrid {
    my ($self, $dset, $var, $newobj) = @_;
    my $dsetName = $dset->getElement->getTagName;
    my $gridName = "${dsetName}_";
    foreach my $dim ($var->getDims){
	$gridName .= $dim->getName . '_';
    }
    $gridName .= "grid";
    my ($gridElem, $gridExists) = $newobj->addGrid($gridName);
    $self->genAxes($newobj, $var, $gridElem)
	if ! $gridExists || ($self->{isUpdate} && ! $self->{updatedGrid});
    $self->{updatedGrid} = 1;
}


sub genVariables {
    my ($self) = @_;
    my $cdf = $self->{cdf};
    my $dset = $self->{dset};
    my %vars = $cdf->getVariables;
    foreach (keys %vars){
	my $var = $vars{$_};
	next if $var->getRank < 1 || $var->isCoord;
	my $atts = {};
	$atts->{name} = $var->getName;
# Check for any bnds variables - LAS doesn't want to know about these for now
#  *kob* 1/2005
	if ($atts->{name} !~ /bnds/ ) {
	    my $longName = $var->getAttribute("long_name");
	    $atts->{name} = $longName if $longName;
	    my $units = $var->getAttribute("units");
	    $atts->{units} = $units if $units;
	    my $varname = $var->getName;
	    $varname = validXML($varname);
	    my $obj;
	    if ($self->{isUpdate}){
		$obj = $dset->findChild($varname);
	    }
	    if (!$obj){
		$obj = $dset->addVariable($varname, $atts);
	    }
	    my $gridElem = $self->createGrid($dset, $var, $obj);
	}
    }
}

#
# Add a virtual variable to a dataset.
# Virtual variables are ways of grouping multiple netCDF files
# together. addVirtualVariable is passed a reference to a LAS::NetCDF
# object (which represents a netCDF file). When XML is generated
# the netCDF file will be represented as a file, not a separate dataset
#

sub addVirtualVariable {
    my ($self, $cdf) = @_;
    my %vars = $cdf->getVariables;
    my $dset = $self->{dset};
    foreach (keys %vars){
	my $var = $vars{$_};
	next if $var->getRank < 1 || $var->isCoord;
	my $atts = {};
	my $longName = $var->getAttribute("long_name");
	$atts->{name} = $longName if $longName;
	my $units = $var->getAttribute("units");
	$atts->{units} = $units if $units;
	$atts->{url} = $cdf->getURL;
	my $varname = $var->getName;
	my %vars = $self->{config}->getInstances('variable');
	my $fullpath = $dset->getFullPath;
	if (defined $vars{$fullpath . "/variables/$varname"}){
	    $varname = $varname . int(rand(2000000000));
	}
	my $newobj = $dset->addVariable($varname, $atts);
	$self->createGrid($dset, $var, $newobj);
    }
}

# Translate to legal XML name if first char not legal
sub xmlTranslate {
    my $in = shift;
    my $out = $in;
    if ($in !~ /^[a-zA-Z_]/){
	$out = '_' . $out;
    }
    return $out;
}

sub genDataset {
    my $self = shift;
    my $cdf = $self->{cdf};
    my $parser = $self->{parser};
    my $url = $cdf->getURL;
    my $update = $self->{isUpdate};
    $self->{dset} = $self->{config}->findURL($url);
    if (!$update){
	die "Duplicate URL '", $url, "' in source file"
	    if $self->{dset};
    }
    if (!$self->{dset}){
#
# Uniquely name the datsaet
#
	my $dsetName = new URI::URL($url)->path;
	my @fields = split(/\//, $dsetName);
	$dsetName = pop @fields;
	$dsetName = validXML($dsetName);

	my %dsets = $self->{config}->getInstances('dataset');
	if (defined $dsets{"/lasdata/datasets/$dsetName"}){
	    $dsetName = $dsetName . int(rand(2000000000));
	}
	

	my $name = $self->{longName};
	$name = $cdf->getAttribute("title") if ! $name;
        my $version = $LAS::VERSION;
        my $generator = "LASNetCDF.pm";
	my $atts = {
	    name=>$name,
	    url=>$url,
	    doc=>defined $self->{docurl} && $self->{docurl},
            version=>$version,
            generator=>$generator
	};

	$dsetName = xmlTranslate($dsetName);

	$self->{dset} = $self->{config}->addDataset($dsetName, $atts);
    }
    $self->genVariables;
}


1;
