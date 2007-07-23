# TMAPDate.pl
# Copyright (c) 2002 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: TMAPDate.pm,v 1.15 2005/03/21 23:47:42 callahan Exp $
# 040429.clc. add (defined YEAR) check to prevent perl compiler msgs

#
# TMAP extensions to Date::Calc
#

use strict;

package TMAP;
$TMAP::VERSION = "0.1";


############################################################
#
# TMAP::Date object for converting between date formats
# 
############################################################

##
# Generic LAS date handling class<p>
#
# Supported formats:<ul>
# <li>YYYYmmDDHHMMSS
# <li>YYYY-MMM-DD
# <li>MMM-DD-YYYY
# <li>DD-MMM-YYYY
# <li>YYYY-MMM-D
# <li>D-MMM-YYYY
# <li>YYYY-mm-DD
# <li>YYYY-mm-D
# <li>YYYY-m-DD
# <li>YYY-mm-DD
# <li>YYYY-m-D
# <li>YYYYmmDD
# <li>YYY-m-DD
# <li>YY-mm-DD
# <li>YYY-m-D
# <li>YY-mm-D
# <li>YY-m-DD
# <li>Y-mm-DD
# <li>Y-mm-D
# <li>Y-m-DD
# <li>DD-MMM
# <li>Y-m-DD
# <li>MMM-DD
# <li>Y-m-D
# <li>D-MMM
# <li>MMM-D
# <li>MMM
# </ul>
# The field separator (shown above as '-') can also be '.' or '/'
#

package TMAP::Date;
BEGIN {
    import Carp;
    use Date::Calc qw(Add_Delta_YMD Delta_DHMS Add_Delta_DHMS 
                      Decode_Month check_date);
}


$TMAP::Date::DEBUG = 0;
my @Months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);

##
# Constructs a new date
# @param $dateStr Date string to parse

sub new {
    my ($class, $dateStr) = @_;
    my $self = {};
    bless $self, $class;
    $self->parseDate($dateStr);
    return $self;
}

##
# @private
#
sub parseDate {
    my ($self,$dateString) = @_;

    return if ( $dateString =~  /[^a-zA-Z0-9\ \-\.\/\:]/);

    my ($y,$m,$d,$h,$min,$s) = (1,1,1,0,0,0);
    my ($datePart, $timePart) = split(' ', $dateString);

    if ($TMAP::Date::DEBUG == 1) {
      print "New TMAP::Date datePart = \"$datePart\"";
      if (defined $timePart){
        print ", timePart = \"$timePart\"";
      }
      print "\n";
    }


    $self->{format} = "";
    $self->{separator} = "";
    $self->{error} = 0;
    $self->{errorString} = "";

    if (defined $timePart){
        ($h,$min,$s) = split(':', $timePart);
        $h = 0 if ! defined $h;
        $min = 0 if ! defined $min;
        $s = 0 if ! defined $s;
        # This logic checks to see if the various time parts are integers.
        # It avoids the problem of error generated when using the int function
        # on on-numeric values.  This comes up in the case of an axis that has
        # random string for labels that should be assigned to index values.
        #
        # The regular expression for determining if something is an integer
        # comes from Perl FAQ #4.
        if (!($h =~ /^-?\d+$/)) {
           $self->{error} = 1;
           $self->{errorString} =  "Non-integer hour value $h.\n";
           return;
        }
        if (!($min =~ /^-?\d+$/)) {
           $self->{error} = 1;
           $self->{errorString} =  "Non-integer minute value $min.\n";
           return;
        }
        if (!($s =~ /^-?\d+$/)) {
           $self->{error} = 1;
           $self->{errorString} =  "Non-integer second value $s.\n";
           return;
        }

        if ($h > 24 || $min > 60 || $s > 60) {
           $self->{error} = 1;
           $self->{errorString} =  "Value out of range, Hour: $h>24 or minute: $min>60 or second: $s>60.\n";
           return;
        }
    }
      
    if ($datePart =~ /\-/) {
      $self->{separator} = '-';
    } elsif ($datePart =~ /\./) {
      $self->{separator} = '.';
    } elsif ($datePart =~ /\//) {
      $self->{separator} = '/';
    }

    # Here are all the acceptable $datePart formats.
    # It's easiest to differentiate based on the length and the 
    # first occurrence of the separator (e.g. '-').
    #
    # YYYYmmDDHHMMSS
    # YYYY-MMM-DD
    # MMM-DD-YYYY
    # DD-MMM-YYYY
    # YYYY-MMM-D
    # MMM-D-YYYY
    # D-MMM-YYYY
    # YYYY-mm-DD
    # YYYY-mm-D
    # YYYY-m-DD
    # YYY-mm-DD
    # YYYY-m-D
    # YYYYmmDD
    # YYY-m-DD
    # YY-mm-DD
    # YYY-m-D
    # YY-m-DD
    # YY-mm-D
    # Y-mm-DD
    # YY-m-D
    # Y-mm-D
    # Y-m-DD
    # DD-MMM
    # MMM-DD
    # Y-m-D
    # D-MMM
    # MMM-D
    # MMM

    if (length($datePart) == 14) {
      if (($y,$m,$d,$h,$min,$s) = $datePart =~ /(\d\d\d\d)(\d\d)(\d\d)(\d\d)(\d\d)(\d\d)/) {
        $self->{format} = "YYYYmmDDHHMMSS";
      }
    } elsif (length($datePart) == 11) {
      if (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\w\w\w).(\d\d)/) {
        $self->{format} = "YYYY-MMM-DD";
      } elsif (($m,$d,$y) = $datePart =~ /(\w\w\w).(\d\d).(\d\d\d\d)/) {
        $self->{format} = "MMM-DD-YYYY";
      } elsif (($d,$m,$y) = $datePart =~ /(\d\d).(\w\w\w).(\d\d\d\d)/) {
        $self->{format} = "DD-MMM-YYYY";
      }
    } elsif (length($datePart) == 10) {
      if (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\w\w\w).(\d)/) {
        $self->{format} = "YYYY-MMM-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\d\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($m,$d,$y) = $datePart =~ /(\w\w\w).(\d).(\d\d\d\d)/) {
        $self->{format} = "MMM-DD-YYYY";
      } elsif (($d,$m,$y) = $datePart =~ /(\d).(\w\w\w).(\d\d\d\d)/) {
        # e.g. "1-Jan-1999"
        $self->{format} = "DD-MMM-YYYY";
      }
    } elsif (length($datePart) == 9) {
      if (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\d\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d).(\d\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      }
    } elsif (length($datePart) == 8) {
      if (($y,$m,$d) = $datePart =~ /(\d\d\d\d)(\d\d)(\d\d)/) {
        $self->{format} = "YYYYmmDD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d\d).(\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d).(\d\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d).(\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d).(\d\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      }
    } elsif (length($datePart) == 7) {
      if (($y,$m,$d) = $datePart =~ /(\d).(\d\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d).(\d\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d).(\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d\d).(\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      }
    } elsif (length($datePart) == 6) {
      if (($y,$m,$d) = $datePart =~ /(\d).(\d\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d\d).(\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d).(\d).(\d\d)/) {
        $self->{format} = "YYYY-mm-DD";
      } elsif(($d,$m) = $datePart =~ /(\d\d).(\w\w\w)/) {
        $y = 1;
        $self->{format} = "DD-MMM";
      } elsif (($m,$d) = $datePart =~ /(\w\w\w).(\d\d)/) {
        $y = 1;
        $self->{format} = "MMM-DD";
      }
    } elsif (length($datePart) == 5) {
      if (($d,$m) = $datePart =~ /(\d).(\w\w\w)/) {
        $y = 1;
        $self->{format} = "DD-MMM";
      } elsif (($m,$d) = $datePart =~ /(\w\w\w).(\d)/) {
        $y = 1;
        $self->{format} = "MMM-DD";
      } elsif (($y,$m,$d) = $datePart =~ /(\d).(\d).(\d)/) {
        $self->{format} = "YYYY-mm-DD";
      }
    } elsif (length($datePart) == 3) {
      if (($m) = $datePart =~ /(\w\w\w)/) {
        $y = 1;
        $d = 15;
        $self->{format} = "MMM";
      }
    } else {
      $self->{error} = 1;
      $self->{errorString} =  "Cannot make sense of \"$datePart\"\n";
      return;
      #die "Cannot make sense of \"$dateString\"\n";
    }

    if ($self->{format} =~ /MMM/) {
      my $dm = Decode_Month($m);
      if (!$dm && $m =~ /^[0-9]+$/){
        $dm = int $m;
      }
      $m = $dm;
    }

    # The year 0000 implies a climatology as does the year 0001.
    # As Date::Calc croaks on year=0000, this will be converted
    # to year=0001.
    if (!$y || $y == 0){
        $y = 1;
    }

    # JC_HACK:  Despite the initializations at the top, perl 5.6.1 is
    # JC_HACK:  complaining that these are 'uninitialized' at this point.
    if (!$y) {$y=0};
    if (!$m) {$m=0};
    if (!$d) {$d=0};

    # Changing months with user interface selection objects
    # often results in invalid dates like "April 31".  Assume
    # the user wants the last valid day in the current month.
    while ( ($d > 28) && !(check_date($y,$m,$d)) ) {
      $d--;
    }

    if (! check_date($y,$m,$d)) {
      $self->{error} = 1;
      $self->{errorString} =  "Invalid date \"$datePart\"\n";
      return;
      #die "Invalid date \"$datePart\"\n";
    }

# Ensure a 4 digit year
# TODO: What if the year is negative?

    if ($y > 0) {
      while (length($y) < 4) {
        $y = "0$y";
      }
    }
    $self->{year} = $y;
    $self->{month} = $m;
    $self->{day} = $d;
    $self->{hour} = $h;
    $self->{min} = $min;
    $self->{sec} = $s;

}   

##
# @return true if constructor was passed parseable date
#
sub isOK {
    my $self = shift;
    return !($self->{error});
    #return 1 if ($self->{year} && $self->{month} && $self->{day});
}

##
# @return true if an error ocurred
#
sub isError {
    my $self = shift;
    return $self->{error};
}

##
# @return the string associated with the error.
#
sub errorString {
    my $self = shift;
    return $self->{errorString};
    #return 1 if ($self->{year} && $self->{month} && $self->{day});
}

##
# Returns a formatted date string
# @return date in YYYY-MMM-DD [HH:MM::SS] format
sub toString {
    my $self = shift;
    return $self->reformatLike("2001-Jan-01");
}

##
# Returns a formatted date string
# @return date in DD-MMM-YYYY [HH:MM::SS] format
sub toFerretString {
    my $self = shift;
    if ( defined $self->{year} ) {		# added 040429.clc.
       if ($self->{year} != 1) {
         return $self->reformatLike("01-Jan-2001");
       } else {
         return $self->reformatLike("01-Jan");
       }
    } else {
      print "WARNING: unable to determine year \n" if ($TMAP::Date::DEBUG > 1);	# DEBUG:CLC
    }
}

##
# Reformats the date to the desired string.
# @param $outputFormat format of output
# @return date string in new format
sub reformatLike {
  my $self = shift;
  my $outputFormat = shift;
  my $outDate = new TMAP::Date($outputFormat);
  my $returnString = "";

  my $year = $self->{year};
  my $month = $self->{month};
  my $day = $self->{day};
  my $hour = $self->{hour};
  my $min = $self->{min};
  my $sec = $self->{sec};
  my $timePart = "";
  my $displayTime = 1;

  while (length($year) < 4) {
    $year = "0$year";
  }

  if ($outDate->{format} =~ /MMM/) {
    $month = $Months[$month-1];
  } else {
    if (length($self->{month}) eq 1) {
      $month = "0" . $self->{month};
    }
  }

  while (length($day) < 2) {
    $day = "0$day";
  }

  if (length($hour) < 2) {
      if (length($hour) < 1) {
	  $hour = "00";
      }else{
	  $hour = "0$hour";
      }
  }
  if (length($min) < 2) {
      if (length($min) < 1) {
	  $min = "00";
      }else{
	  $min = "0$min";
      }
  }
  if (length($sec) < 2) {
      if (length($sec) < 1) {
	  $sec = "00";
      }else{
	  $sec = "0$sec";
      }
  }

  $timePart = "$hour:$min:$sec";

  # YYYYmmDDHHMMSS
  # YYYY-MMM-DD
  # MMM-DD-YYYY
  # DD-MMM-YYYY
  # YYYY-mm-DD
  # YYYYmmDD
  # DD-MMM
  # MMM-DD
  # MMM

  # Note: Climatological dates drop the timePart

  if ($outDate->{format} eq 'YYYYmmDDHHMMSS') {
    $returnString = $year . $month . $day . $hour . $min . $sec;
  } elsif ($outDate->{format} eq 'YYYY-MMM-DD') {
    $returnString = join($outDate->{separator},$year,$month,$day);
  } elsif ($outDate->{format} eq 'MMM-DD-YYYY') {
    $returnString = join($outDate->{separator},$month,$day,$year);
  } elsif ($outDate->{format} eq 'DD-MMM-YYYY') {
    $returnString = join($outDate->{separator},$day,$month,$year);
  } elsif ($outDate->{format} eq 'YYYY-mm-DD') {
    $returnString = join($outDate->{separator},$year,$month,$day);
  } elsif ($outDate->{format} eq 'YYYYmmDD') {
    $returnString = $year . $month . $day;
  } elsif ($outDate->{format} eq 'DD-MMM') {
    $displayTime = 0;
    $returnString = join($outDate->{separator},$day,$month);
  } elsif ($outDate->{format} eq 'MMM-DD') {
    $displayTime = 0;
    $returnString = join($outDate->{separator},$month,$day);
  } elsif ($outDate->{format} eq 'MMM') {
    $displayTime = 0;
    $returnString = join($outDate->{separator},$month);
  }

  if ($TMAP::Date::DEBUG == 1) {
    print "TMAP::DATE::reformatLike() returnString = \"$returnString\", timePart = \"$timePart\"\n";
  }

  # Add the timePart if appropriate
  if ($displayTime) { $returnString .= " $timePart"; }

  return $returnString;
}

##
# Returns the year, month, and day
# @param $useMonthString (optional )return month as a string (default is number)
# @return year, month day as @ ($year,$month, $day)
sub getYMD {
    my $self = shift;
    my $useMonthString = shift;
    return ($self->{year}, $self->{month}, $self->{day}) if ! $useMonthString;
    return ($self->{year}, $Months[$self->{month}-1], $self->{day});
}

##
# Returns the hour, minute, and second
# @return hour, minute, second as @ ($hour,$min,$sec)
sub getHMS {
    my $self = shift;
    return ($self->{hour}, $self->{min}, $self->{sec});
}

   
##
# Add a time interval to a date and return a new TMAP::Date object
# @param $yr delta year
# @param $mo delta month
# @param $dy delta day
# @param $hr delta hour (optional)
# @param $mi delta minute (optional)
# @param $sec delta second (optional)
# @return TMAP::Date
sub addDelta {
    my ($self, $dy, $dm, $dd, $dh, $dmin, $dsec) = @_;
    $dh = 0 if ! defined $dh;
    $dmin = 0 if ! defined $dmin;
    $dsec = 0 if ! defined $dsec;
    my ($eh, $emin, $esec);
    my ($ey, $em, $ed) =
        Add_Delta_YMD($self->{year}, $self->{month}, $self->{day},
                      $dy,$dm,$dd);
    ($ey, $em, $ed, $eh, $emin, $esec) =
        Add_Delta_DHMS($ey, $em, $ed, $self->{hour}, $self->{min}, $self->{sec},
                       0, $dh, $dmin, $dsec);
    return new TMAP::Date("$ey-$em-$ed $eh:$emin:$esec");
}

##
# Returns the number of hours between two dates
# @param $hidate the TMAP::Date object to compare
# @return the difference in hours
sub getDeltaHours {
    my ($self, $hidate) = @_;
    my @fields = Delta_DHMS($self->getYMD, $self->getHMS, $hidate->getYMD,
                            $hidate->getHMS);
    return $fields[0]*24.0 + $fields[1] + $fields[2]/60.0 + $fields[3]/3600.0;
}

##
# Returns the number of days between two dates
# @param $hidate the TMAP::Date object to compare
# @return the difference in days
sub getDeltaDays {
    my ($self, $hidate) = @_;
    return (Delta_DHMS($self->getYMD, $self->getHMS, $hidate->getYMD,
                      $hidate->getHMS))[0];
}

##
# Compares two Date objects chronologically
# @param $date the TMAP::Date object to compare
# @return -1 if self is less than $date
# @return  0 if self is equal to $date
# @return  1 if self is greater than $date

sub compareTo {
    my ($self, $date) = @_;
    my $diff = $self->getDeltaHours($date);
    return -1 if $diff > 0;
    return  0 if $diff == 0;
    return  1 if $diff < 0;
}

1;
