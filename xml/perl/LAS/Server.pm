#
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: Server.pm,v 1.21 2005/03/21 23:47:42 callahan Exp $
#

use Time::HiRes;

require("LAS_config.pl");

##
# @private
package LAS::Server::Log;

$self = {};

sub new {
    my $log_file = $LAS::Server::Config{log_file};
    open(LOGFILE, ">>$log_file") ||
	die "Can't create log file $log_file (protection problem?)";
    $self->{file} = \*LOGFILE;
    $last = select LOGFILE;
    $| = 1;
    select $last;
    &writeHeaders;
}

sub writeHeaders {
    doLog("\nDATE: ", scalar localtime);
    doLog("RQST: ", $ENV{REQUEST_METHOD}, ' ', $ENV{REQUEST_URI});
    doLog("HOST: ", $ENV{REMOTE_ADDR});
    doLog("CLNT: ", $ENV{HTTP_USER_AGENT});
}

sub doLog {
    print {$self->{file}} @_, "\n";
}

##
# @private
package LAS::Server::Error;

$self = {};

sub new {
    my $error_file = $LAS::Server::Config{error_file};
    open(ERRORFILE, ">>$error_file") ||
	die "Can't create error file $error_file (protection problem?)";
    $self->{file} = \*ERRORFILE;
    $last = select ERRORFILE;
    $| = 1;
    select $last;
}

sub doError {
    foreach (@_){
	foreach (split(/^/m)){
	    chop;
	    print {$self->{file}} "[", scalar localtime, "] ",$_, "\n";
	}
    }
}

##
# Timer object to help in profiling the code.  This object
# uses the Time::HiRes module to calculate elapsed times.
package LAS::Server::Time;

##
# Creates a new timer and initializes the internal timestamp with the current time.
sub new {
    my $class = shift;
    my $self = {};
    $self->{timestamp} = [Time::HiRes::gettimeofday];
    bless $self, $class;
    return $self;
}

##
# Calculates the elapsed time since one of
# <ol>
#  <li>creation of the timer</li>
#  <li>last call to getElapsed()</li>
# </ol>
# and resets the timestamp to the current time.
#
# @return seconds elapsed
sub getElapsed {
    my ($self) = @_;
    my $timestamp = $self->{timestamp};
    my $elapsed = Time::HiRes::tv_interval($timestamp);
    $self->{timestamp} = [Time::HiRes::gettimeofday];
    return $elapsed;
}


##
# Top level class which exports the following methods 
#
# <ul>
#   <li>getConfig</li>
#   <li>dumpConfig</li>
#   <li>debug</li>
#   <li>getDebugFile</li>
#   <li>debugTrace</li>
# </ul>
#
# In a completely object oriented setting, these should be called
# from an object reference rather than exported.  The exporting
# of methods may be removed in a future version of the code.
# <p>
#
# This class also sets the mime types for all file types produced
# by LAS.
package LAS::Server;

use vars qw(@ISA @EXPORT);
use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw(getConfig dumpConfig debug getDebugFile debugTrace);


##
# Returns the Config hash which contains information about server directories
# and aliases, Ferret versions and arguments, etc.  Currently these are set
# in $LASROOT/server/LAS_config.pl and $LASROOT/server/Ferret_config.pl.  In 
# version 6.5 of LAS this information will be stored in an XML file.
#
# @return the hash of configuration information. 
sub getConfig {
    return \%Config;
}

##
# Prints debugging information.
# @param @_ perl list to be written as output.
sub debug {
    print DBGFILE @_;
}

##
# Print debugging information from within a particular method.
# @param type [1/0] prepends "Calling:"/"Returning from:"
# @param @_ perl list to be written as output.
sub debugTrace {
    my $type = shift;
    debug("\n--- Calling: ", @_, "\n") if ! $type;
    debug("\n--- Returning from: ", @_, "\n") if $type;
}

##
# Prints the Config hash to the debug file.
sub dumpConfig {
    debug("Dumping config file --- \n");
    foreach (keys %Config){
	debug("$_ : $Config{$_}\n");
    }
    debug("\n");
}

##
# @return a file handle for the debug file.
sub getDebugFile {
    return \*DBGFILE;
}

#
# Mime types to associate with file types

%LAS::Server::MimeTypes = (
   html=>"text/html",
   jnl=>"text/plain",
   gif =>"image/gif",
   jpg =>"image/jpeg",
   png =>"image/png",
   ps => "application/postscript",
   compare => "image/gif",
   overlay => "image/gif",
   line =>"image/gif",
   shade =>"image/gif",
   vector => "image/gif",
   contour =>"image/gif",
   texture=>"image/gif",
   fill =>"image/gif",
   txt =>"text/plain",
   cdf =>"application/x-netcdf",
   nc => "application/x-netcdf",
   tsv =>"text/plain",
   csv =>"application/csv",
   arc =>"text/plain",
   asc =>"text/plain",
   globe =>"x-world/x-vrml",
   wrl =>"text/plain",
   v5d =>"application/vis5d"
		 );

# Compress these types before sending
%LAS::Server::CompressTypes = (
  ps => 1
);		     

##
# Adds a new mime type.
# @param suffix filename extension
# @param mime_type (e.g. "text/plain" or "image/gif")
sub add_mime($$) {
    $LAS::Server::MimeTypes{$_[0]} = $_[1];
}

##
# @private
#
sub get_suffix {
    my $instr = $_[0];
    my $pos = rindex $instr, '.';
    die "Bad mime type: $instr" if $pos < 0;
    my $newstr = substr $instr, $pos+1;
    return $newstr;
}


##
# Returns the appropriate mime type based on the argument.
# @param output filename ending in ".suffix"
sub mime_type {
    my $newstr = get_suffix($_[0]);
    my $rval = $LAS::Server::MimeTypes{$newstr};
    $rval = "image/gif" if ! $rval;
    return $rval;
}

##
# @private
# True if type needs compressing
sub do_compress {
    my $newstr = get_suffix($_[0]);
    return $LAS::Server::CompressTypes{$newstr};
}


##
# @private
# An object representation of the LAS output product filestream.
# This object is created in the <b>LAS::Server:HTTP</b> run() method.
# <p>
# The <b>LAS::Server::FileStream</b> will format the server
# output in a suitable form as a function of the MIME type.
#
package LAS::Server::FileStream;

use File::Basename;
use File::Copy;
use Compress::Zlib;
use CGI qw/:push/;
use Template;
use URI::URL;

##
# Create new FileStream object.
# @param server <b>LAS::Server::HTTP</b> object.
# @param [file] output file
sub new {
    my ($class, $server, $file) = @_;
    my $self = {
	file => $file,
	server => $server
    };
    bless $self, $class;
    return $self;
}

##
# Prints HTML output to STDOUT or a FileHandle.  File extensions listed
# in the CompressTypes hash will be compressed and returned as ".gz" binary.
# <p>
# After sending the HTTP header information, the LAS product is simply
# copied from its location on disk to the output filehandle.
# @param query incoming query object as obtained from the CGI module
# @param fh output filehandle (STDOUT by default)
sub print {
    my ($self, $query, $fh) = @_;
    my $file = $self->{file};
    my $type = &LAS::Server::mime_type($file);
    $fh = \*STDOUT if ! $fh;

    if ($type eq "image/gif"){
	my $out_file = dirname($file) . "/" .
	    basename($file, ".gif") . '.html';
  	genHTML($out_file, [$file] );
	print $fh $query->header(-type=>"text/html");
	copy($out_file, $fh) || die "Copy of $file to STDOUT failed";
	return;
    }

    my $suffix;
    if (&LAS::Server::do_compress($file)){ # Compress it
	my $outfile = $file . ".gz";
	if (! -f $outfile){
	    my $gz = gzopen($outfile, "wb") ||
		die "Can't open compressed version of $file";
	    open INFILE, $file || die "Can't open $file";
	    while(<INFILE>){
		$gz->gzwrite($_) || die "Write error when compressing $file";
	    }
	    $gz->gzclose;
	    close INFILE;
	    unlink($file);
	}
	$file = $outfile;
	$type = "application/x-gzip";
	$suffix = 'ps.gz';
    } else {
	$suffix = &LAS::Server::get_suffix($file);
    }

    die "Can't read file: $file" if ! -r $file;
    my $length = -s $file;
    my ($mainType) = split(/\//, $type);
    my $filename = "LASoutput." . $suffix;
    my $content;
    if ($mainType eq "application"){
  	$content = qq/attachment; filename=$filename/;
    } else {
  	$content = qq/inline; filename=$filename/;
    }
    print $fh $query->header(-type=>"$type",
  			     -'Content-length'=>$length,
  			     -'Content-Disposition'=>$content);
    copy($file, $fh) || die "Copy of $file to STDOUT failed";
}


##
# @private
# Processes the $LASROOT/server/plot.tmpl template to create
# the HTML output page returned to the browser.  LAS images are placed in table
# cells in either a horizontal or vertical alignment.
# <p>
# This method is called by the FileStream <b>print</b> method.
# @param out_file file to which output is directed [STDOUT is default]
# @param files list of images to be organized in table cells
# @param orientation [horizontal/vertical] alignment of HTML table cells
sub genHTML {
    my ($out_file, $files, $orientation) = @_;
    $orientation = "horizontal" if ! $orientation;
    my $loc = $LAS::Server::Config{output_alias};
    die "Config error: no output_alias defined" if ! $loc;

    my @imageUrls = map {
	"http://" . $ENV{SERVER_NAME} . ":" . $ENV{SERVER_PORT} .
	    $loc . basename($_) } @{$files};

    my $images = \@imageUrls;

    my $file = 'plotout.tmpl';
    my $info = {
	imageURLS => $images,
	docRoot => $url,
	orientation => $orientation
	};

    my $vars = {
	'info' => $info
	};

    my $templateConfig = {
	POST_CHOMP => 1,
	PRE_CHOMP => 1
	};
    my $template = Template->new($templateConfig);
    $template->process($file, $vars, $out_file)
	or die $template->error();

# Version 1.06 of Template on OSF appears to have a bug where templates are not
# immediately flushed to a file. Work around this by flushing to the cache file
# (for later access) and also sending data directly to stdout
#    $template->process($file,$vars) or die $template->error();
}

##
# Visualizer class to handle <b>all</b> the details.  This makes the
# actual CGI script ($LASROOT/LASserver.pl) quite small.  This class
# is instantiated and used in the last six lines of LASserver.pl:
# <pre>
#   my $visualizer = new LAS::Server::HTTP;
#   my $output = $visualizer->setup($query);
#   if (! $output){
#     $output = $visualizer->run;
#   }
#   $output->print($query);
# </pre>
# The BEGIN block of this class is where the error handler is set and
# where error strings are parsed and redirection to error specific HTML
# pages is handled.  If you wish to add error specific web pages you 
# should put that code in this class.
package LAS::Server::HTTP;

use MD5;
use File::PathConvert qw(rel2abs);

use CGI::Carp qw(carpout fatalsToBrowser set_message);
BEGIN {
    sub badOption {
	my $message = shift;
	$message =~ s/ at.*//g;
	my @parts = split(':', $message);
	$message = $parts[3];
	print <<EOF;
<html>
<head>
</head>
<body bgcolor="white">
<h3>Invalid expression</h3>
You tried to customize LAS output with the following invalid expression:
<pre>
    <b>$message</b>
</pre>
Please return to the LAS Options page and enter a valid expression. 
</body>
</html>
EOF
}

    sub illegal_limit {
        my $message = shift;
        my @parts = split('\n', $message);
        foreach $parts (@parts){
          if ($parts =~ /ERR/){
            $parts =~ s/(^.*T=)(.*)/There are no data available /;
            my @timepart = split(':', $2);
            if (@timepart == 2){
              $message = "$parts"."at "."$2"."\n";
            }
            else{
              $message = "$parts"."in the range "."$timepart[0]".":"."$timepart[1]"." to "."$timepart[2]".":"."$timepart[3]"."\n";
            }
            
          }
          if ($parts =~ /Axis extremes/){
            $parts =~ s/(^.*T=)(.*)/    The data begin at /; 
            my @timepart = split(':', $2);
            $message = "$message"."$parts"."$timepart[0]".":"."$timepart[1]"." and continue to "."$timepart[2]".":"."$timepart[3]";
          }
        }
        print <<EOF;
<html>
<head>
</head>
<body bgcolor="white">
<h3>Illegal Limits</h3>
Sorry ...
<pre>
    <b>$message</b>
</pre>
Please try again with a time that lies within this range.
</body>
</html>
EOF
}

sub script_error {
    my $message  = shift;
    my $heading  = '';
    my $msg_text = '';
    &LAS::Server::debug("LAS::Server::HTTP::script_error Parsed Error:\n");
    my @parts = split('las:', $message);
    foreach my $part (@parts){
	&LAS::Server::debug("part: $part\n");
	if ($part =~ m/^heading/){
	    $heading = substr($part,8);
	}elsif ($part =~ m/^text/){
	    $msg_text .= substr($part,5)."\n";
	}
    }
    print <<EOF;
<html>
<head>
</head>
<body bgcolor="white">
<h3>$heading</h3>
<pre>
<b>$msg_text</b>
</pre>
</body>
</html>
EOF
}

    my %ErrorRedirect;
    $ErrorRedirect{"all data have same value"} = "missingData.html";
    $ErrorRedirect{"No data was returned with the query"} = "nullQuery.html";
    $ErrorRedirect{"Timeout after"} = "timeOut.html";
    #$ErrorRedirect{"illegal limit"} = "illegalLimits.html";
    $ErrorRedirect{"illegal limit"} = \&illegal_limit;
    $ErrorRedirect{"dimensions improperly specified"} = "subGrid.html";
    $ErrorRedirect{"Invalid option"} = \&badOption;
    $ErrorRedirect{"command syntax.*contour"} = "badContour.html";
    $ErrorRedirect{"script_error"} = \&script_error;
    sub sendRedirect {
	my $file = shift;
	open INFILE,"html/$file" || return 0;
	while(<INFILE>){
	    print $_;
	}
	close INFILE;
	return 1;
    }	
#
# Set up error message for CGI::Carp
    sub handle_errors {
	my $msg = shift;
	my $didRedirect = 0;
	$msg =~ s/&quot;/\"/g;
	foreach my $key (keys %ErrorRedirect){
	    my $value = $ErrorRedirect{$key};
	    if ($msg =~ /$key/is){
		if (ref($value) eq "CODE"){
		    &{$value}($msg);
		    $didRedirect = 1;
		    last;
		}
		if (sendRedirect($value)){
		    $didRedirect = 1;
		    last;
		}
	    }
	}
	if (! $didRedirect){
	    print <<EOF;
	    <html><head><title>LAS Error</title></head><body bgcolor="white">
	    <h3>LAS Error</h3>
	    The following error message was received from LAS:
	    <pre><b>$msg</b></pre>
	    </body></html>
EOF
        }

	&LAS::Server::Error::doError($msg);
    }
    set_message(\&handle_errors);
}

##
# Creates a new visualizer with debugging output and error handling.
sub new {
    my ($class) = @_;
    my $self = {
	queryParams => {}
    };
    bless $self,$class;

# Set log level to errors (no warning) only

    &LAS::setLogLevel('ERROR');


# Debug output

    my $debug_file = $LAS::Server::Config{debug_file};
    open(LAS::Server::DBGFILE, ">$debug_file") ||
	die "Can't create debug file $debug_file (protection problem?)";
    my $last = select LAS::Server::DBGFILE;
    $| = 1;
    select $last;
    print LAS::Server::DBGFILE "Browser = $ENV{'HTTP_USER_AGENT'}\n\n";
    &LAS::Server::dumpConfig;

# Log output
# Error output

    new LAS::Server::Log;
    new LAS::Server::Error;

    return $self;
}

##
# Extracts the LAS Request object XML from the query and returns the
# result if it is has been previously been generated and still exists
# in the cache ($LASROOT/server/output).
# @param query CGI query object
sub setup {
    my ($self, $query) = @_;

    my $xml = $query->param('xml');
    $self->{xml} = $xml;

    if ($xml){
	$self->setupXML($xml);
    } else {
	die "Missing 'xml' parameter";
    }
    if ($self->{cached}){
	my $output = $self->{queryParams}->{output_file};
	return new LAS::Server::FileStream($self, $output);
    }
}

##
# @private
# Checks to see if the file has already been created.  If it has
# and the useCache parameter is true, the {cached} property is set.
# This method is called by setupOutput().
# (This implementation needs to be reworked.)
# <p>
# Information on whether the cache was used or not will appear in
# the log/access file with lines beginning with "CACHE: ".
#
# @param file output file being searched for
# @param useCache parameter directing this routine to use/ignore
# results in the LAS cache.
sub checkForCache {
    my ($self, $file, $useCache) = @_;

    die "checkForCache: file arg missing" if ! defined($file);
    die "checkForCache: useCache arg missing" if ! defined($useCache);

    my $output_directory = $LAS::Server::Config{output_directory};
    die "Can't write to output directory '$output_directory' (protection problem?)"
	if ! -w $output_directory;

    my $output = $output_directory . "/$file";
    $output =~ s:/+:/:g;	# Eliminate redundant '/' to aid the Ferret parser
    $self->{queryParams}->{output_file} = $output;
    $self->{cached} = 0;
    my $lock_file = $output;
    $lock_file =~ s/\.[a-z]+$/_batch\.lock/g;
    my $err_file = $output;
    $err_file =~ s/\.[a-z]+$/_batch\.err/g;
    $output = $output . ".gz" if &LAS::Server::do_compress($file);

    if (! $useCache || ! -r $output){

	if (! $useCache){
	    unlink $output;
	    &LAS::Server::Log::doLog("CACHE: disabled");
            $ENV{DODS_CONF} = "dods/.dodsrc_no_cache";
	} else {
	    &LAS::Server::Log::doLog("CACHE: false");
	}

    } else {

# The output file is found and is readable, but is it finished?
# 1) If the lock file still exists then someone is still working 
#    on this (e.g. netCDF) file.  -- OR -- 
# 2) It is possible that the file exists and the process creating 
#    it is done but that things did not go well.  (Again, this only
#    seems possible for data listings).  To account for this we 
#    check for a non-zero size in the err_file.
# If either of the above is true, do not use the cache.

        if (-e $lock_file || -s $err_file ) {
	    $self->{cached} = 0;
	    &LAS::Server::Log::doLog("CACHE: lock_file or err_file exists");
        } else {
	    $self->{cached} = 1;
	    &LAS::Server::Log::doLog("CACHE: true");
        }
    }
}


##
# This method accepts an LAS XML request and does the following:
# <ol>
#   <li>creates an LAS Request object from the incoming XML request</li>
#   <li>creates an MD5 output filename from the XML request</li>
#   <li>instantiates an appropriate handler for the requested operation</li>
# </ol>
#
# @param xml XML version of an LAS request
sub setupXML {
    my ($self, $xml) = @_;
    &LAS::Server::debug("Got XML request:\n$xml\n");
    &LAS::Server::Log::doLog("XML: $xml");

    $self->{parser} = my $parser = new LAS::Parser($xml, 1);
    $self->{req} = my $req = new LAS::Request($parser);

    # Add insitu support code if this server is configured for it.
    # 1) Support for in-situ data.
    if ($LAS::Server::Config{insitu_support}){
      unless (my $return = do "../xml/perl/TMAP/insitu.pl"){
	die $@ if $@;
	die $! if $!;
	die "Couldn't run ../xml/perl/TMAP/insitu.pl";
      }
    }

    # Add any custom code
    my $customInclude = $req->getCustomInclude(1);
    if ($customInclude){
	my $path = $customInclude . "/custom.pl";
	my $root = $req->getPackageRoot(1);
	if ($root){
	    my $cdir = rel2abs('.');
	    chdir("$root/server") or die "Couldn't chdir to $root/server";
	    $path = "$customInclude/custom.pl";
	    unless (my $return = do $path){
		die $@ if $@;
		die $! if $!;
		die "Couldn't run $path";
	    }
	    chdir($cdir) or die "Couldn't chdir to $cdir";
	} else {
	    unless (my $return = do $path){
		die $@ if $@;
		die $! if $!;
		die "Couldn't run $path";
	    }
	}
    }

    $req->resolveLinks;

    $self->setupOutput($xml, $req);
    return if $self->{cached};

    my $op = $req->getOp;
    die "Missing 'op' parameter" if ! $op;
    my $opname = $req->getOp->getName;

    $self->getHandler($op);
}


##
# Adds parameters for the output directory and filenames.  The XML version
# of the LAS request object is used to create a MD5 hash that will be used
# as a unique filename for this product. Processes image_format option to
# decide if the output graphic should be in GIF or PostScript format.
#
# @param xml XML version of LAS Request object
# @param req LAS Request object
sub setupOutput {
    my ($self, $xml, $req) = @_;

    die "Can't find config info" if undef $LAS::Server::Config;
    die "setupOutput: Request arg missing" if ! $req;

    my $md5 = new MD5;
    $md5->reset;
    $md5->add($xml);
    my $digest = $md5->digest;
    $digest = unpack("H*", $digest);
    my $fer_props = $self->{req}->getProperties('ferret');
    my $suffix;
    if ($fer_props){
        my $image_suf = $self->{req}->getProperties('ferret')->{'image_format'};
        # After XML schema re-write, maybe Options can be set up differently and we wont need multiple option menu names.
        if (!defined $image_suf){$image_suf = $self->{req}->getProperties('ferret')->{'insitu_image_format'};}
        if (defined $image_suf){
	    if ($image_suf eq 'default'){
		$suffix = $self->{req}->getProperties('ferret')->{'format'};
	    }elsif($image_suf eq 'ps'){
		$suffix = $image_suf;
                my $format = $self->{req}->getProperties('ferret')->{'format'} = $image_suf;
	    }elsif($image_suf eq 'gif'){
		$suffix = $image_suf;
		my $format = $self->{req}->getProperties('ferret')->{'format'};
	    }else{
                die "Image Format: $image_suf not supported, check the image format options menu definition\n";
	    }
	}else{
	    $suffix = $self->{req}->getProperties('ferret')->{'format'};
	}
    } else {
	$suffix = "txt";
    }
    if($suffix eq 'asc'){
       my $data_format = $self->{req}->getProperties('ferret')->{'data_format'};
       if(!defined ($data_format)){
          $data_format = $self->{req}->getProperties('ferret')->{'compare_data_format'};
       }
       $suffix = $data_format;
    }
    $suffix = 'nc' if $suffix eq 'cdf';
    my $type = &LAS::Server::mime_type(".$suffix");
    if ($type eq "image/gif"){
	$suffix = 'gif';
    }
    die "Need to specify output return type (format property)"
	if ! $suffix;
    $digest .= ".$suffix";
    $self->checkForCache($digest, $req->useCache);
}


##
# Uses the "class" and "method" properties of the incoming operation
# to set the 'handler' and 'method' properties by instantiating a handler of
# the appropriate type with the following line:
# <pre>
#     $self->{handler} = eval("new $class");
# </pre>
#
# @param op operation as defined in operations.xml
sub getHandler {
    my ($self, $op) = @_;
    my $class = $op->getAttribute("class");
    my $method = $op->getAttribute("method");
    die "Undefined 'class' attribute for operation: ", $op->getName
	if ! defined $class;
    die "Undefined 'method' attribute for operation: ", $op->getName
	if ! defined $method;
    $self->{handler} = eval("new $class");
    die $@ if $@;
    die "Error in new sub of $class" if ! $self->{handler};
    $self->{method} = $method;
}


##
# Uses the handler initialized in setupXML to run the operation
# specified in the LAS Request.
#
# If you get to this routine you know that the requested product is
# not in the cache and must be generated.  We begin by forking a
# process so that the child can generate the product while the parent
# keeps tabs on the progress of the child.  The parent checks on
# the child every second for the first <ui_timeout> seconds and 
# returns the request whenever it is done.
#
# If the child takes longer than <ui_timeout> seconds, an HTML
# response is sent back indicating that LAS has shifted this request
# to 'batch mode'.  The response page has links for resubmitting the
# request, for requesting email notification and for killing the job.
# We fork again at this point to get a Subparent and a Grandchild.
# The Subparent then goes to sleep and wakes up once a minute to check
# check on the Grandchild's progress.  If the Grandchild is done, the Subparent
# does some cleanup and then dies.  If the Grandchild is still working on
# the job after more than <ps_timeout> seconds it is killed by the Subparent
# as a runaway job.
#
# The MD5 hash used to generate the output product filename is used
# to create a lock file, error file and HTML 'batch mode' response associated
# with this product request.
#
# @return LAS::Server::FileStream
sub run {
  my ($self) = @_;

  my $handler = $self->{handler};
  die "setup method never called for ", ref($self) if ! $handler;
  my $output_file = $self->{queryParams}->{output_file};
  $handler->init($self->{req}, $output_file);

  my $LASConfig = &LAS::Server::getConfig;
  my $output_dir = $LASConfig->{output_alias};
  my $file_name = $output_file;
  $file_name =~ s/output\///g;
  my $permalink = "http://".$ENV{SERVER_NAME} . $ENV{REQUEST_URI};

  my $pid = 0;
  my $response_type = "first";

# We need two <product_server> properties to determine the
# behavior of batch mode:
#
# ui_timeout -- seconds to wait before returning the 'batch mode' page
# ps_timeout -- seconds to wait before killing the Ferret process
#
# Default settings are 20 secs for ui_timeout and 1 hr for ps_timeout
#
# All <product_server> properties are independent of the LAS::Server::Handler
# and are added to the LAS::Request object during its initialization.

  my $ui_timeout = $self->{req}->{properties}->{product_server}->{ui_timeout};
  $ui_timeout = 20 if ! defined($ui_timeout);
  &LAS::Server::debug("LAS::Request prop: ui_timeout = $ui_timeout\n");

  my $ps_timeout = $self->{req}->{properties}->{product_server}->{ps_timeout};
  $ps_timeout = 3600 if ! defined($ps_timeout);
  &LAS::Server::debug("LAS::Request prop: ps_timeout = $ps_timeout\n");

# The ui_timeout should be ignored whenever the handler is a LAS::Util.
# The utilites in this package are typically for non-UI requests and 
# the batch mode response is not want the requesting software wants.
# Take care of this by resetting the ui_timeout to be equal to ps_timeout.

  my $class = ref($handler);
  if ($class eq "LAS::Util") {
    $ui_timeout = $ps_timeout;
  }


# Create the response file name by converting any of the standard LAS file type
# extensions to '_batch.html'.

  my $batch_response = $output_file;
  $batch_response =~ s/\.[a-z]+$/_batch\.html/g;

# Create a lock file name

  my $lock_file = $batch_response;
  $lock_file =~ s/\.html/\.lock/g;

# Create a timeout file name

  my $timeout_file = $batch_response;
  $timeout_file =~ s/\.html/\.timeout/g;

# Create an error file name

  my $err_file = $batch_response;
  $err_file =~ s/\.html/\.err/g;

# Check for the existence of the timeout file to see if a
# previous request for this product timed out.

  if (-e "$timeout_file") {

# Send back an informational page with a link to resubmit the request
# Remove the old timeout file after it is used by generateBatchResponse

    $response_type = "timeout";
    $self->generateBatchResponse($response_type,$output_file,$permalink);
    unlink $timeout_file;
    return new LAS::Server::FileStream($self, $batch_response);

  } elsif (-s "$err_file") {

# Send back the error file if it is non-zero.
# NOTE: This error file stays around so it is up to the installer
# NOTE: to clean this item from the cache after the cause of the
# NOTE: error has been fixed.

      return new LAS::Server::FileStream($self, $err_file);

  } elsif (-e "$lock_file") {

# Product is already being created by another job.
# Just return the HTML page and exit.

    my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,
        $atime,$mtime,$ctime,$blksize,$blocks) = stat($lock_file);
    my $now = time();
    my $seconds = $now - $mtime;

    $response_type = "lock";
    $self->generateBatchResponse($response_type,$output_file,$permalink,$seconds);
    return new LAS::Server::FileStream($self, $batch_response);

  } else {

# The product doesn't exist so we need to create it

# Create the lock file for this product.
# (The '>>' takes care of the possibility that another product
# request created the file since we tested for its existence.)

    open OUT, ">>$lock_file" or die "Can't open lock file \"$lock_file\"";

# Now fork a process to run the product method

    $pid = fork();

############################################################
#                                                          #
#                      Parent Process                      #
#                                                          #
# Watches over the child process and returns the product   #
# if it finishes in less than ui_timeout seconds.          #
# Otherwise the parent returns a batch response page.      # 
#                                                          #
############################################################

    if ($pid) {
		
# Reap child to prevent zombies

      local $SIG{'CHLD'} = \&reaper;

# Close unneded file handles

      close OUT;

# Parent process checks in on the child every second.
# The parent process stops checking when either 
#  o the ui_timeout has been reached -or-
#  o the child has finished

      use POSIX ":sys_wait_h";
      my $secs_elapsed = 0;
      my $child = waitpid($pid,&WNOHANG);
      do {
        sleep(1);
        $child = waitpid($pid,&WNOHANG);
        $secs_elapsed++;
      } while ( ($secs_elapsed < $ui_timeout) && ($child == 0) );

# If the child process has ended, check on the existence of
# the output file to determine what happened.

      if ($child != 0) {

# Get rid of files associated with this request (except possibly the error file)

        unlink $lock_file;
        unlink $err_file if (-z $err_file);
  
        if (-s "$err_file") {

# If the $err_file is non-zero,  return it.
# This check comes before the $output_file check because it seems
# to be possible, when generating very large data listings, for 
# Ferret to create an output file with _FillValue contents and 
# only later run into problems and quit with an error.

          return new LAS::Server::FileStream($self, $err_file);

        } elsif (-e "$output_file") {

# Output file exists --> return it

          return new LAS::Server::FileStream($self, $output_file);

        } else {

# No output file --> the child died prematurely

# No error file exists (it would have been returned above) so
# we just generate the HTML error message and return it.

          $response_type = "error";
          $self->generateBatchResponse($response_type,$output_file,$permalink);
          return new LAS::Server::FileStream($self, $batch_response);

        }

      } else {

# Child is still working on the product
# Generate the HTML page and return it

        $response_type = "first";
        $self->generateBatchResponse($response_type,$output_file,$permalink);
        return new LAS::Server::FileStream($self, $batch_response);

      }

    } else {

############################################################
#                                                          #
#                      Child Process                       #
#                                                          #
############################################################

# Fork another process that watches over the Grandchild and
# takes care of ps_timeout and any cleanup afterwards

      my $pid2 = fork();

      if ($pid2) {
		
########################################
#                                      #
#       Sub Parent Process             #
#                                      #
# Watches over the product creation    #
# and cleans up _batch files after the #
# grandchild is done or kills the      #
# grandchild after ps_timeout seconds. #
#                                      #
########################################

# Reap child to prevent zombies

        local $SIG{'CHLD'} = \&reaper;

# Catch QUIT signal delivered from LAS::Util::cancelRequest

        local $SIG{'QUIT'} = \&quitter;

# Write the child process ID into the lock file so LAS::Util::cancelRequest()
# can kill the child if desired.

        print OUT "PID=$pid2\n";
        print OUT "PPID=$$\n";
        close OUT;

# Sub parent process checks on Grandchild.
# The Sub Parent process checks every second so that it
# can exit in a timely fashion if the Grandchild finishes.
# NOTE: Difficult to debug conflicts arise when the sleep 
# NOTE: cycle is longer than this.
# We stop checking when either:
#  o the ps_timeout has been reached -or-
#  o the child has finished

        $secs_elapsed = 0;
        $child = waitpid($pid2,&WNOHANG);
        do {
          sleep(1);
          $child = waitpid($pid2,&WNOHANG);
          $secs_elapsed++;
        } while ( ($secs_elapsed < $ps_timeout) && ($child == 0) );

        if ($child != 0) {

# The child finished in time.

# Get the file base by removing the 'lock' extension from the lock file
# Remove all the '_batch' files associated with this request

          my $file_base = $lock_file;
          $file_base =~ s/lock//;
          unlink glob("$file_base*");

        } else {

# If the child is still alive at this point it should be killed.
# The proper way to do this is to send it the QUIT signal so that
# the child can kill it's entire process group.

          kill(QUIT,$pid2);

# Get the file base by removing '_batch.lock' from the lock file
# Remove all the files associated with this request

          my $file_base = $lock_file;
          $file_base =~ s/_batch\.lock//;
          unlink glob("$file_base*");

# Open a timeout file so that when the user returns to get this file they 
# know what happened.

          open TIMEOUT, ">$timeout_file" or die "Can't open timeout file \"$timeout_file\"";
          print TIMEOUT "ps_timeout=$ps_timeout\n";
          close TIMEOUT;

        }

# Subparent's job is now done so exit.

        exit;

      } else {

########################################
#                                      #
#        Grandchild Process            #
#                                      #
# creates the actual product           #
#                                      #
########################################

# Catch QUIT signal delivered from the Subparent

        local $SIG{'QUIT'} = \&quitter;

# Set the process to a lower priority - nice to 10

        setpriority 0, 0, getpriority(0,0) + 10;

# Send all STDERR messages to the error file

        open (STDERR, ">$err_file");
		
# Create the desired product
# Remove lock, batch_response and error files
# Exit

        $handler->run($self->{method});
        unlink $lock_file;
        unlink $batch_response;
        unlink $err_file if (-z $err_file);
        exit;

      } # End of grandchild
    }   # End of child
  }     # End of lockfile test
}


##
# Process a template to careate an HTML page with the 
# 'batch mode' response describing where to pick up the 
# results that are being generated.
#
# @param response_type flag specifying "first|lock|error"
# @param output_file filename of the requested product
# @param permalink URL that will resubmit the request
# @param seconds seconds since request was submitted
sub generateBatchResponse {
  my ($self,$response_type,$output_file,$permalink,$seconds) = @_;

  my $file = 'jnls/batchout.tmpl';

# Create the response file name by converting any of the standard LAS file type
# extensions to '_batch.html'.

  my $batch_response = $output_file;
  $batch_response =~ s/\.[a-z]+$/_batch\.html/g;

# Create a lock file name

  my $lock_file = $batch_response;
  $lock_file =~ s/\.html/\.lock/g;

# Create a timeout file name

  my $timeout_file = $batch_response;
  $timeout_file =~ s/\.html/\.timeout/g;

# If you can open the timeout file, use the information inside
# to alert the user that this product request timed out before.

  if (-e $timeout_file) {

    if (open(TIMEOUT,$timeout_file)) {

      my $line = <TIMEOUT>;
      close TIMEOUT;
      chomp($line);

      if ($line =~ /(ps_timeout=)(\d+)/) {
        $seconds = $2;
      }
    }
  }

  my $minutes = int($seconds / 6) / 10;
  my $hours = int($seconds / 144) / 10;

# All the <properties> should be available in the handler

  my $handler = $self->{handler};

  my $view = $handler->{props}->{view};
  my $format = $handler->{props}->{format};
  my $dataset_name = $handler->{props}->{dataset_name};
  my $datasets = join(',', @{$dataset_name});
  my $variable_name = $handler->{props}->{variable_name};
  my $variables = join(',', @{$variable_name});

# Generate the start_time string

  my ($sec,$min,$hour,$mday,$mon,$yr,$wday,$yday,$isdst) = localtime;
  my $year = $yr + 1900;
  my $day = (Sun,Mon,Tue,Wed,Thu,Fri,Sat)[$wday];
  my $month = (Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec)[$mon];
  if (length($hour) == 1) { $hour = '0' . $hour; }
  if (length($min) == 1) { $min = '0' . $min; }
  if (length($sec) == 1) { $sec = '0' . $sec; }
  my $start_time = "$day $month $mday $hour:$min:$sec $year";

# Generate a description of the requested product type
# Default to the product method if it isn't one of the standard types.

  my $product_type = $self->{method};

  if ($format =~ /gif/ || $format =~ /ps/ || $format =~ /line/ ||
      $format =~ /shade/ || $format =~ /compare/ || $format =~ /overlay/) {
    $product_type = $view . " plot";
  } elsif ($format =~ /cdf/ || $format =~ /txt/ || $format =~ /tsv/ ||
           $format =~ /csv/ || $format =~ /asc/ || $format =~ /arc/) {
    $product_type = "data file";
  } elsif ($format =~ /jnl/) {
    $product_type = "journal script";
  }

  use URI::Escape;

# Generate a URL bits and pieces for the "email" request

# For the email request we don't want to uri_escape() anything
# as the form submission in las/server/jnls/batchout.tmpl will
# do that automatically.  If we do a uri_escape() at this point
# then the form submission will escape our escape characters.

  my $action_url = "http://" . $ENV{SERVER_NAME} . $ENV{SCRIPT_NAME};

  my $xmlHead = qq{<?xml version="1.0"?><lasRequest href="file:las.xml">};
  my $xmlLink = qq{<link match="/lasdata/operations/requestEmail"/>};
  my $xmlProp = qq{<properties><util>};
  $xmlProp .= qq{<output_file>$output_file</output_file><email>};
  my $email_xml_head = $xmlHead . $xmlLink . $xmlProp;

  $xmlProp = qq{</email>};
  $xmlProp .= qq{<start_time>$start_time</start_time><datasets>$datasets</datasets>};
  $xmlProp .= qq{<variables>$variables</variables><product_type>$product_type</product_type>};
  $xmlProp .= qq{</util></properties>};
  my $xmlFoot = qq{</lasRequest>};
  my $email_xml_tail = $xmlProp . qq{<args></args>} . $xmlFoot;

# Generate a URL for the "cancel" request

  $xmlHead = qq{<?xml version="1.0"?><lasRequest href="file:las.xml">};
  $xmlLink = qq{<link match="/lasdata/operations/cancelRequest"/>};
  $xmlProp = qq{<properties><util><lock_file>$lock_file</lock_file></util></properties>};
  $xmlFoot = qq{</lasRequest>};
  my $cancel_xml = $xmlHead . $xmlLink . $xmlProp . qq{<args></args>} . $xmlFoot;

# Generate a URL for the 

  my $status_xml = $self->{xml};
  $status_xml =~ tr/\n\r\f//d;

  my $args = {
    response_type => $response_type,
    product_type => $product_type,
    view => $view,
    datasets => $datasets,
    variables => $variables,
    action_url => $action_url,
    status_xml => $status_xml,
    email_xml_head => $email_xml_head,
    email_xml_tail => $email_xml_tail,
    cancel_xml => $cancel_xml,
    seconds => $seconds,
    minutes => $minutes,
    hours => $hours
  };

  my $vars = {
    'args' => $args
  };

  my $templateConfig = {
    POST_CHOMP => 1,
    PRE_CHOMP => 1
  };

  my $template = Template->new($templateConfig);
  $template->process($file, $vars, $batch_response)
    or die $template->error();
}


##
# Reaper for preventing zombie child processes.
#
# This from Programming perl, 3rd ed, p. 416 -kob-
#
use POSIX ":sys_wait_h";
sub reaper { 1 until (waitpid(-1, WNOHANG) == -1) }

##
# Kill self and all decendents
sub quitter { kill(HUP,-$$); }


##
# Base class for handler classes (drivers) that run visualization code.
# <p>
# The default LAS installation uses Ferret to handle all visualization
# requests.  The file Ferret.pl defines a new package, <b>LAS::Server::Ferret</b>,
# which inherits from this base class.  The Ferret.pl file begins with
# <pre>@LAS::Server::Ferret::ISA = qw(LAS::Server::Handler);
# </pre>
#
# When setting up a new handler you will begin with something
# like this:
# <pre>
# package LAS::Server::Newserver;
# 
# # Set up inheritance from LAS::Server::Handler base class
# </pre><pre>@LAS::Server::Newserver::ISA = qw(LAS::Server::Handler);
# 
# # The init method is required.
# # init is passed the LAS::Request object as well as the
# # output file
# sub init {
#     my ($self, $req, $output) = @_;
#     $self->{req} = $req;
#     $self->{output} = $output;
# }
# </pre>
#
# The <b>init()</b> method is the first method that must be defined by the
# new handler.  Other methods in the handler should match the
# methods named in <b>operations.xml</b>.  As an example, the Ferret.pl
# file declares the <b>LAS::Server::Ferret</b> package and defines the <b>data()</b>
# method.  This method will be called whenever a user requests the 'Return variable data'
# product from the user interface.  This is specified in
# the default <b>operations.xml</b> file:
# <pre>
#   data name="Return variable data" class="LAS::Server::Ferret" method="data"
# </pre>
# 

package LAS::Server::Handler;

##
# Create a new handler.
sub new {
    my ($class) = @_;
    my $self = {
	props => {},
	mime_type => undef
    };
    bless $self, $class;
    return $self;
}

##
# Execute the handler method with
# <pre>
#   eval('$self->' . $method);
# </pre>
# @param method named in operations.xml file
sub execute {
    my ($self, $method) = @_;
    &LAS::Server::debugTrace(0,ref($self), '::',$method);
    eval('$self->' . $method);
    if ($@){
	unlink $self->{output_file};
	die $@;
    }
    &LAS::Server::debugTrace(1,ref($self), '::',$method);
}

##
# Method prototype for any activities that must take place prior
# to the execute() method.  This method is empty by default.
sub preExecute {}

##
# Method prototype for any activities that must take place after
# the execute() method.  This method is empty by default.
sub postExecute {}

##
# Method prototype for any activities that must take place immediately
# prior to finishing.  This method is empty by default.
sub close {}

##
# Execute the preExecute(), execute(), postExecute() and close() methods within
# a perl 'eval' block, logging timing information to the debug file. 
# @param method named in operations.xml file
sub run {
    my ($self, $method) = @_;
    eval {
        my $time = new LAS::Server::Time;
	$self->preExecute;
        my $elapsed = $time->getElapsed();
        &LAS::Server::debug("LAS::Server::Handler::run(): $elapsed secs to perform all 'preExecute' activities (database access and parsing)\n");
	$self->execute($method);
        $elapsed = $time->getElapsed();
        &LAS::Server::debug("LAS::Server::Handler::run(): $elapsed secs to perform all 'execute' activities (Ferret)\n");
	$self->postExecute;
        $elapsed = $time->getElapsed();
        &LAS::Server::debug("LAS::Server::Handler::run(): $elapsed secs to perform all 'postExecute' activities (none typically)\n");
    };
    if ($@){
	$self->close(1);
#	$@ =~ s/\[.+\]//g;	# Strip extraneous CGI::Carp junk
	die $@;
    }
}


1;
