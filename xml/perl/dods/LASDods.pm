package LAS::DODS::Object;
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id $

use URI::Escape;

sub new {
    my ($class, $name) = @_;
    my $self =
    {
	name => uri_unescape($name),
	atts => {}
    };
    bless $self, $class;
}

sub getName {
    $_[0]->{name};
}

sub getAttribute {
    my $atts = $_[0]->{atts}->{$_[1]};
    if ($atts){
	return wantarray ? @{$atts} : $atts->[0];
    }
}

sub getAttributes {
    $_[0]->{atts};
}

sub addAttribute {
    $_[0]->{atts}->{$_[1]} = $_[2];
}

package LAS::DODS;
@LAS::DODS::ISA = qw(LAS::DODS::Object);
use Parse::Lex;
use LWP::UserAgent;
use das;
use dds;


$LAS::DODS::DebugLex = 0;
@LAS::DODS::DDSTokens = (
	      qw(
                 SCAN_DATASET [Dd][Aa][Tt][Aa][Ss][Ee][Tt]\b
		 SCAN_INDEPENDENT [Ii][Nn][Dd][Ee][Pp][Ee][Nn][Dd][Ee][Nn][Tt]\b
		 SCAN_DEPENDENT [Dd][Ee][Pp][Ee][Nn][Dd][Ee][Nn][Tt]\b
		 SCAN_ARRAY [Aa][Rr][Rr][Aa][Yy]\b
		 SCAN_MAPS [Mm][Aa][Pp][Ss]\b
		 SCAN_LIST [Ll][Ii][Ss][Tt]\b
		 SCAN_GRID [Gg][Rr][Ii][Dd]\b
		 SCAN_SEQUENCE [Ss][Ee][Qq][Uu][Ee][Nn][Cc][Ee]\b
		 SCAN_STRUCTURE [Ss][Tt][Rr][Uu][Cc][Tt][Uu][Rr][Ee]\b
		 SCAN_ALIAS [Aa][Ll][Ii][Aa][Ss]\b
		 SCAN_BYTE [Bb][Yy][Tt][Ee]\b
		 SCAN_INT16 [Ii][Nn][Tt]16\b
		 SCAN_UINT16 [Uu][Ii][Nn][Tt]16\b
		 SCAN_INT32 [Ii][Nn][Tt]32\b
		 SCAN_UINT32 [Uu][Ii][Nn][Tt]32\b
		 SCAN_FLOAT32 [Ff][Ll][Oo][Aa][Tt]32\b
		 SCAN_FLOAT64 [Ff][Ll][Oo][Aa][Tt]64\b
		 SCAN_STRING [Ss][Tt][Rr][Ii][Nn][Gg]\b
		 SCAN_URL [Uu][Rr][Ll]\b
		 SCAN_ERROR [Ee][Rr][Rr][Oo][Rr]\b
		 LB  {
   	         RB  }
		 SEMICOLON  ;
	     ),
                 'COMMA', ',',
	     qw(
		 COLON :
		 LSB \[
		 RSB \]
		 EQUALS =
		 SCAN_FLOAT [-+]?(([0-9]+\.?[0-9]*(E|e)[-+]?[0-9]+)|([0-9]+\.[0-9]*))
		 SCAN_INT  [-+]?[0-9]+
		 SCAN_ID  [a-zA-Z_%][a-zA-Z0-9_./:%+\-()]*
		 SCAN_NAME  [/]?[a-zA-Z_%][a-zA-Z0-9_./:%+\-()]*
		 ),
	      qw(SCAN_STR), [qw(" (?:[^"]+|"")* ")],
	      'NEVER','[^a-zA-Z0-9_/.+\-{}:;\,%]',
  	      );

@LAS::DODS::DASTokens = (
	      qw(
		 SCAN_ATTR attributes|Attributes|ATTRIBUTES\b
		 SCAN_ALIAS ALIAS|Alias|alias\b
		 SCAN_BYTE BYTE|Byte|byte\b
		 SCAN_INT16 INT16|Int16|int16\b
		 SCAN_UINT16 UINT16|UInt16|Uint16|uint16\b
		 SCAN_INT32 INT32|Int32|int32\b
		 SCAN_UINT32 UINT32|UInt32|Uint32|uint32\b
		 SCAN_FLOAT32 FLOAT32|Float32|float32\b
		 SCAN_FLOAT64 FLOAT64|Float64|float64\b
		 SCAN_STRING STRING|String|string\b
		 SCAN_URL URL|Url|url\b
		 LB  {
   	         RB  }
		 SEMICOLON  ;
             ), 'COMMA', ',',
             qw(
		 COLON :
		 LSB \[
		 RSB \]
		 EQUALS =
		 SCAN_FLOAT [-+]?(([0-9]+\.?[0-9]*(E|e)[-+]?[0-9]+)|([0-9]+\.[0-9]*))
		 SCAN_INT  [-+]?[0-9]+
		 SCAN_ID  [a-zA-Z_%][a-zA-Z0-9_./:%+\-()]*
		 ),
	      qw(SCAN_STR), [qw(" (?:[^"]+|"")* ")],
	      'NEVER','[^a-zA-Z0-9_/.+\-{}:;\,%]',
  	      );

$LAS::DODS::Lexer = undef;

sub ErrorHandler {
    die "Parse error near: '", $_[0]->YYCurval, "'", "in:",
    $_[0]->YYData->{url};
}

sub Lexer {
    my $token = $LAS::DODS::Lexer->next;
    if (not $LAS::DODS::Lexer->eoi) {
	print "Type: ", $token->name, "\t" if $DebugLex;
	print "Content:->", $token->text, "<-\n" if $DebugLex;
	return ($token->name, $token->text) ;
    } else {
	return ('',undef);
    }
}

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    my ($path) = @_;
    $self->{variables} = {};
    $self->{orderedVars} = [];
    $self->{path} = $path;
    bless $self, $class;
    my $ddspath = "$path.dds";
    my $daspath = "$path.das";

    my $req = new HTTP::Request(GET, $ddspath);
    my $ra = new LWP::UserAgent;
    my $resp = $ra->request($req);
    my ($ddsstr, $dasstr);
    if ($resp->is_success){
	$ddsstr = $resp->content;
    } else {
	die "Error in accessing $ddspath";
    }
    $req = new HTTP::Request(GET, $daspath);
    $resp = $ra->request($req);
    if ($resp->is_success){
	$dasstr = $resp->content;
    } else {
	die "Error in accessing $daspath";
    }


    my $lexer = $LAS::DODS::Lexer = Parse::Lex->new(@DDSTokens);
    $lexer->skip('[ \t\n]+');
    $lexer->from($ddsstr);
    my $ddsparser = new dds;
    $ddsparser->YYData->{dods} = $self;
    $ddsparser->YYData->{url} = $ddspath;
    $ddsparser->YYParse(yylex => \&Lexer, yyerror => \&ErrorHandler);
    $lexer->reset;

# Need to turn off warning messages from Lex while we add new tokens
    my $lastwarn = $SIG{'__WARN__'};
    $SIG{'__WARN__'} = sub {};
    $lexer = $LAS::DODS::Lexer = Parse::Lex->new(@DASTokens);
    $SIG{'__WARN__'} = $lastwarn;
    $lexer->skip('[ \t\n]+');
    $lexer->from($dasstr);
    my $dasparser = new das;
    $dasparser->YYData->{dods} = $self;
    $dasparser->YYData->{url} = $daspath;
    $dasparser->YYParse(yylex => \&Lexer, yyerror => \&ErrorHandler);
    $self;
}

sub getVariables {
    wantarray ? %{$_[0]->{variables}} :  $_[0]->{variables};
}

sub getVariablesInOrder {
    @{$_[0]->{orderedVars}};
}

sub getVariable {
    $_[0]->{variables}->{$_[1]};
}

sub getURL {
    $_[0]->{path};
}

# Dummy routine
sub close {
}

package LAS::DODS::Variable;
use Config;
use URI::URL;
use LWP::UserAgent;
@LAS::DODS::Variable::ISA = qw(LAS::DODS::Object);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    $self->{dods} = $_[1];
    $self->{type} = $_[2];
    $self->{dims} = [];
    $_[1]->{variables}->{$_[0]} = $self;
    push(@{$_[1]->{orderedVars}}, $self);
    bless $self, $class;
}

sub getDims {
    wantarray ? @{$_[0]->{dims}} : $_[0]->{dims};
}

sub isCoord {
    my ($self) = @_;
    my @dims = @{$self->{dims}};
    return 0 if (scalar @dims != 1);
    my $dim = $dims[0];
    return 1 if $dim->getName eq $self->getName;
    return 0;
}

sub getSize {
    my $self = shift;
    my $size = 0;
    foreach my $dim (@{$self->{dims}}){
	$size += $dim->getSize;
    }
    $size;
}

sub addDim {
    push(@{$_[0]->{dims}}, $_[1]);
}

sub getRank {
    return scalar @{$_[0]->{dims}};
}

sub getType {
    return $_[0]->{type};
}

sub getData {
    my ($self, $startArray, $countArray) = @_;
    die "getData only supports 1D arrays"
	if scalar @{$startArray} > 1 || scalar @{$countArray} > 1;

# Bugzilla 408 -- caching caused truncated data if getData was
# called with a different $countArray size
#      return (wantarray ? @{$self->{data}} : $self->{data})
#  	if defined($self->{data});

    my @dimList = @{$self->{dims}};
    my $type = $self->{type};
    die "getData should only be used with 1D variables"
	if $#dimList > 0;
    die "getData only supports Int16,Int32,Float32,Float64"
	if $type ne 'Float32' && $type ne 'Float64' &&
	    $type ne 'Int32' && $type ne 'Int16';
# Construct appropriate URL
    my $path = $self->{dods}->{path} . ".dods";
    my $dim = $dimList[0];
    my $size = int($dim->getSize);
    my $index = $size - 1;
    my $start = $startArray ? $startArray->[0] : 0;
    my $count = $countArray ? $countArray->[0] : $index;
    $count = $index if $count > $index;
    $size = $count + 1;

# DODS server doesn't translate escaped '[',']',':'
# Have to hack URI::URL to not escape these
    $URI::Escape::escapes{'['} = '[';
    $URI::Escape::escapes{']'} = ']';
    $URI::Escape::escapes{':'} = ':';
    my $url = new URI::URL($path);
    $url->query($self->{name} . "[$start:1:$count]");

# Get the data
    my $req = new HTTP::Request(GET, $url);
    my $ra = new LWP::UserAgent;
    my $resp = $ra->request($req);
    my $result;
    if ($resp->is_success){
	$result = $resp->content;
    } else {
	die "Error obtaining DODS data from $path";
    }
# TODO -- Look for DODS error code
# Look for Data delimiter
    my $dataStart = index($result, 'Data:');
    die "Invalid data return for $path" if $dataStart < 0;
    $result = substr($result, $dataStart+14);

# Convert to list. Assumed to be in network order. 
    my ($iform, $fform);
    my $size_half = int($size/2);
    my $size2 = $size*2;
    my @rval;
    if ($type eq 'Float32'){
	$iform = "N${size}";
	$fform = "f${size}";
	@rval = unpack($fform, pack('i*',unpack($iform, $result)));
    } elsif ($type eq 'Float64'){
	$iform = "N${size2}";
	$fform = "d${size}";
	my @junk = unpack($iform, $result);
	if ($Config{byteorder} =~ /^1234/){
	    for (my $i=0; $i < $size2; $i += 2){
	        my $tmp = $junk[$i];
	        $junk[$i] = $junk[$i+1];
	        $junk[$i+1] = $tmp;
	    }
	}
	@rval = unpack($fform, pack('i*', @junk));
    } elsif ($type eq 'Int32'){
	@rval = unpack("N${size}", $result);
    } elsif ($type eq 'Int16'){
        # This next seems like it should be incorrect but works with the 
        # only Int16 dataset I've found:
        #   http://ferret.pmel.noaa.gov/cgi-bin/nph-nc/data/hadcrut.nc
	@rval = unpack("N${size}", $result);
    } else {
	die "Invalid type";
    }
    $self->{data} = \@rval;
    return (wantarray ? @{$self->{data}} : $self->{data});
}

sub printDim {
    my $dim = shift;
    my $rval = "";
    $rval .= "[";
    my $dimvar = $dim->getVar;
    if ($dimvar){
	$rval .= $dimvar->getName . ' = ';
    }
    $rval .= $dim->getSize;
    $rval .= "]";
    $rval;
}

sub toString {
    my $self = shift;
    my $rval = "";
    my @dims = @{$self->{dims}};
    if ($#dims > 0){
	$rval .= "    Grid {\n";
	$rval .= "    ARRAY:\n";
	$rval .= "        " . $self->getType . " " . $key;
	foreach my $dim (@dims){
	    $rval .= printDim($dim);
	}
	$rval .= ";\n    MAPS:\n";
	foreach my $dim (@dims){
	    my $dimVar = $dim->getVar;
	    if ($dimVar){
		$rval .= "        " . $dimVar->getType . " " . $dimVar->getName ."[" .
		$dim->getSize . "];\n";
	    }
	}
	$rval .= "    } " . $key . ";\n";
    } elsif ($#dims == 0){
	$rval .= "    " . $self->getType . " " . $key;
	foreach my $dim (@dims){
	    $rval .= printDim($dim);
	}
	$rval .= ";\n";
    }
    $rval;
}

# Emulate a DODS variable. Used for dimensions without coordinate
# variables

package LAS::DODS::DummyVar;

sub new {
    my ($class, $name, $size) = @_;
    my $self = {
	name => $name,
	size => $size
    };
    bless $self, $class;
}

sub getData {
    my $self = shift;
    my $size = $self->{size};
    return (1..$size);
}

sub getAttribute {
    undef;
}

sub getName {
    $_[0]->{name};
}

sub isCoord {
    return 1;
}

package LAS::DODS::Dim;
@LAS::DODS::Dim::ISA = qw(LAS::DODS::Object);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    $self->{dods} = $_[1];
    $self->{size} = $_[2];
    bless $self, $class;
}

sub getSize {
    $_[0]->{size};
}

sub setSize {
    $_[0]->{size} = $_[1];
}

sub getVar {
    my $dods = $_[0]->{dods};
    my $theVar = $dods->getVariable($_[0]->getName);
    if (!$theVar){
	$theVar = new LAS::DODS::DummyVar($_[0]->getName, $_[0]->getSize);
    }
    $theVar;
}
    
