# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: Util.pm,v 1.10 2005/03/21 23:47:42 callahan Exp $
#

# General documentation on the LAS::Util package:
#
# ...
#
# Product server properties (as of LAS 6.5):
#
# <properties>
#  <product_server>
#   <ui_timeout>10</ui_timeout>
#   <ps_timeout>600</ps_timeout>
#   <use_cache>true</use_cache>
#  </product_server>
# </properties>
#   
# The methods in LAS::Util define a set of 'web services' for internal 
# (and potential external) use.  Typically, requests specifying LAS::Util
# methods will not be issued by the LAS UI but instead by other web pages,
# scripts or other software.  Because of this, the LAS::Server::HTTP->run()
# method resets <ui_timeout> to be the same as <ps_timeout> whenever a 
# LAS::Server::Handler has a class of type LAS::Util.  This has the effect
# of guaranteeing that a 'batch mode response' is never returned when an 
# LAS::Request references any of the methods defined in this package.


##
# @private
package LAS::Util;
use FileHandle;

# Error returns
#   DISABLED           -- this server is configured to deny sister requests
#   SYSTEM: ~string~   -- any system error caught by perl
#   LAS: ~string~      -- all other errors

sub new {
    my $self = {
        useLong => 0
    };
    bless $self;
}

sub init {
    my ($self,$req,$output) = @_;
    $self->{req} = $req;
    $self->{output} = $output;

    require('./LAS_config.pl');
    $self->{allowSisters} = 1 if lc($LAS::Server::Config{allow_sisters}) eq "true";
    $self->{allowSisterDriven} = 1 if lc($LAS::Server::Config{allow_sister_driven}) eq "true";
}

sub run {
    my ($self,$method) = @_;
    my $output = $self->{output};
    open OUTPUT, ">$output" || die "Can't open output file $output";

    if ($method eq 'ls') {
        $self->initLs;
    }

    if ($method eq 'getRegion') {
        $self->initRegion;
    }

    eval('$self->' . $method);
    if ($@){
        close OUTPUT;
        die $@;
    }
    close OUTPUT;
}

sub getRegion {

    my ($self) = @_;
    if ($self->{variable}){
        my $doc = &lsRegion;
        $doc->printToFileHandle(\*OUTPUT);
        return;
    }
    if ($self->{dataset}){
        $self->printError("No variable defined!  Need both dataset and variable.");
        return;
    }
    $self->printError("No variable defined!  Need both dataset and variable.");
    return;
}

sub lsRegion {

    my ($self) = @_;
    my $config = $self->{req}->getConfig;
    my @datasets = grep {$_->getName eq $self->{dataset}} $config->getChildren;
    if (scalar @datasets == 0){
        print OUTPUT "Can't find dataset $self->{dataset}\n";
        return;
    }
    my $dataset = $datasets[0];
    my @variables = grep {$_->getName eq $self->{variable}}
                          $dataset->getChildren;
    if (scalar @variables == 0){
        print OUTPUT "Can't find variable $self->{variable}\n";
        return;
    }

    my $var = $variables[0];

    my $dom_parser = new XML::DOM::Parser;            # create a parser object

    my $rtc = makeResponseStub();

    if ( ! $rtc ) {

       die "Failed to make XML stub file for constructing the response.\n";

    }
    
    my $doc = $dom_parser->parsefile("output/las_response.xml");
    my $regionElement = $doc->createElement("region");

    foreach my $child ($var->getChildren){

        my $rangeElement = $doc->createElement("arange");

        my $type = $child->getAttribute("type");
        my $low = $child->getLo;
        my $high = $child->getHi;
        my $size = $child->getSize;
        my $units = $child->getUnits;
        # If $step is undefined, irregular axis.
        # Get children and add them as <v> elements!
        my $step = $child->getStep;
        $rangeElement->setAttribute("type", $type);
        $rangeElement->setAttribute("low", $low);
        $rangeElement->setAttribute("high", $high);
        $rangeElement->setAttribute("size", $size);
        $rangeElement->setAttribute("units", $units);
        if (!defined($step)) {
           my @points = $child->getChildren;
           foreach my $point (@points) {
              $vElement = $doc->createElement("v");
              $vElement->setContents($point);
              $rangeElement->appendChild($vElement);
           }
        }
        else {
           $rangeElement->setAttribute("step", $step);
        }

        $regionElement->appendChild($rangeElement);

    }
    my $root = $doc->getDocumentElement;
    $root->appendChild($regionElement);

    return $doc;

}


sub makeResponseStub {
    my $ret=0;
    if (! -r "output/las_response.xml") {
       open RESPONSE, ">output/las_response.xml" || die "Can't open output/las_response.xml.\n";
       RESPONSE->autoflush(1);
       print RESPONSE <<END
<?xml version="1.0" encoding="UTF-8"?>
<las_response>
</las_response>
END
    }
# Did it get made successfully?
    if ( -r "output/las_response.xml") 
    { 
       $ret=1; 
    }
    else {
       $ret=0;
    }
    return $ret;
}


sub getOps {
   print OUTPUT "<?xml version=\"1.0\"?>\n";
   print OUTPUT "<las_response>\n";
   my $rt = &lsOps;
   print OUTPUT "</las_response>\n";
}


sub lsOps {

use FileHandle;

# This is the main workhorse routine for producing the list of Operations.
#
    my ($self) = @_;
    my $config = $self->{req}->getConfig;
    my @ops = $config->getOps;

    # Loop through Ops.
    foreach my $op (@ops){
       print OUTPUT $op->toXML(1);
    }


    if (-r 'packages.xml'){
       my $parser = new LAS::Parser('packages.xml');
       my $package_root = new LAS::PackageConfig($parser);
       foreach my $package ($package_root->getChildren){
           @files = $package->getFiles;
           foreach $file (@files) {
              my $parser = new LAS::Parser($file);
              my $config = new LAS::Config($parser);
              my @ops = $config->getOps;

              # Loop through Ops.

              foreach my $op (@ops){
                 print OUTPUT $op->toXML(1);
              }


           }
       }
   } # if readable packages.xml file
   return 1; 
}

sub getThredds {
   my $doc = &lsThredds;
   $doc->printToFileHandle(\*OUTPUT);
}


sub lsThredds {

use FileHandle;

# This is the main workhorse routine for producing the THREDDS catalog.
#
# It uses the XML::DOM package to construct a document object which
# is returned.
#
# Only the product server is listed as a service for each operation, rather than
# listing the individual operations.  
#
# To actually use this product server, the client will likely have to use the
# getOps product request to figure out what the server can do.
#
    my ($self) = @_;
    my $dom_parser = new XML::DOM::Parser;            # create a parser object

    if (! -f "output/catalog.xml") {
       open CATALOG, ">output/catalog.xml" || die "Can't open output/catalog.xml file\n";
       CATALOG->autoflush(1);
       print CATALOG <<END
<?xml version="1.0"?>
<catalog name="LAS THREDDS Inventory Catalog" version="1.0" 
         xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0"
         xmlns:xlink="http://www.w3.org/1999/xlink">
</catalog>
END
    }
    
    my $doc = $dom_parser->parsefile("output/catalog.xml");  # read in the document stub
    my $config = $self->{req}->getConfig;
    my $parserURL = $config->getParser->getURL;
    my $inst = $config->getInstitution->getInstName;
    my @ops = $config->getOps;

    foreach $op (@ops) {
       push @allServiceURLs,$op->getURL;
    }

    # Get unique members of array (from Perl Data Manipulation FAQ (perlfaq4))

    undef %saw;
    my @uniqueServiceURLs = grep(!$saw{$_}++, @allServiceURLs);

    my $catalogDataset = $doc->createElement("dataset");
    $catalogDataset->setAttribute("name", $inst);

    foreach my $serviceBase ( @uniqueServiceURLs ) {
       my $thisService = $doc->createElement("service");
       $thisService->setAttribute("name",$parserURL);
       my $base;
       if ( $LAS::Server::Config{proxy} eq "yes" ) {
          $base = "http://".$LAS::Server::Config{serverhost}.$LAS::Server::Config{uipath}."-FDS/LAS/";
       }
       else {
          $base = "http://".$LAS::Server::Config{tomcathost}.":".$LAS::Server::Config{tomcatport}.$LAS::Server::Config{uipath}."-FDS/LAS/";
       }
       $thisService->setAttribute("base",$base);
       $thisService->setAttribute("serviceType","DODS");
       $catalogDataset->appendChild($thisService);
    }

    addDatasetsToCatalog($config, $doc, $catalogDataset, $parserURL);

    if (-r 'packages.xml'){
       my $parser = new LAS::Parser('packages.xml');
       my $package_root = new LAS::PackageConfig($parser);
       foreach my $package ($package_root->getChildren){
           @files = $package->getFiles;
           foreach $file (@files) {
              my $parser = new LAS::Parser($file);
              my $config = new LAS::Config($parser);
              $inst = $config->getConfig->getInstitution->getInstName;
              my $catalogDataset = $doc->createElement("dataset");
              $catalogDataset->setAttribute("name", $inst);
              my @ops = $config->getOps;

              foreach $op (@ops) {
                 push @allServiceURLs,$op->getURL;
              }

              # Get unique members of array (from Perl Data Manipulation FAQ (perlfaq4))

              undef %saw;
              my @uniqueServiceURLs = grep(!$saw{$_}++, @allServiceURLs);

              foreach my $serviceBase ( @uniqueServiceURLs ) {
                 my $thisService = $doc->createElement("service");
                 $thisService->setAttribute("name", $file);
                 my $base;
                 if ( $LAS::Server::Config{proxy} eq "yes" ) {
                    $base = "http://".$LAS::Server::Config{serverhost}.$LAS::Server::Config{uipath}."-FDS/LAS/";
                 }
                 else {
                    $base = "http://".$LAS::Server::Config{tomcathost}.":".$LAS::Server::Config{tomcatport}.$LAS::Server::Config{uipath}."-FDS/LAS/";
                 }
                 
                 $thisService->setAttribute("base",$base);
                 $thisService->setAttribute("serviceType","DODS");
                 $catalogDataset->appendChild($thisService);
              }

              addDatasetsToCatalog($config, $doc, $catalogDataset, $file);  
           }
       }
    } # if -r packages.xml
    return $doc;
}
sub addDatasetsToCatalog {
    my ($config, $doc, $catalogDataset, $parserURL) = @_;
    foreach my $ds ($config->getChildren){
       my $longname = $ds->getLongName;
       my $dsname = $ds->getName;
       my $docUrl = $ds->getAttribute("doc");
       my $ds_dataUrl = $ds->getAttribute("url");
       my $subDataset = $doc->createElement("dataset");
       $subDataset->setAttribute("name",$longname);

       if ( defined($docUrl) ) {
          if ( $docUrl ne "" ) {
             my $documentation = $doc->createElement("documentation");
             $documentation->setAttribute("xlink:href", $docUrl);
             $subDataset->appendChild($documentation);
          }
       }

       foreach my $varchild ($ds->getChildren) {

          # Start an dataset element for this variable
          my $varname = $varchild->getName;
          my $varlongname = $varchild->getLongName;
          my $variable = $doc->createElement("dataset");

          $variable->setAttribute("name", $varlongname);

          # Always use the FDS URL to avoid problems with variable case.
          my $path = $dsname."/".$varname;
          $variable->setAttribute("urlPath", "$path");
          $variable->setAttribute("serviceName", $parserURL);
          $subDataset->appendChild($variable);

       }

       $catalogDataset->appendChild($subDataset);
    }

    my $root = $doc->getDocumentElement;
    $root->appendChild($catalogDataset);
}

sub getVersion {
    my ($self) = @_;

    if (! $self->{allowSisters}) {
      $self->printError("DISABLED");
      return;
    }

    print OUTPUT "<?xml version=\'1.0\' ?>\n<las_response>\n  <version>$LAS::VERSION<\/version>\n</las_response>\n";
}


sub getConfig {
    my ($self) = @_;

    if (! $self->{allowSisters}) {
      $self->printError("DISABLED");
      return;
    }

    my $config = $self->{req}->getConfig;
    my $configString = $config->toXML(1);
    print OUTPUT "<?xml version=\'1.0\' ?>\n<las_response>\n <config>\n$configString <\/config>\n</las_response>\n";
}


sub getPackage {
    my ($self) = @_;
    my $xmlFile = "las.xml";
    
    require('../config.results');
    my $hostname = $LAS::Util::LasConfig{hostname};
    my $pathname = $LAS::Util::LasConfig{pathname};

    $pathname =~ tr/\///d;
    $pathname =~ s/-bin//;
    my $pfile = $hostname . "_" . $pathname . ".tar";
    my $output_path = "http://" . $hostname .
                      $LAS::Util::LasConfig{output_alias} . "/" . $pfile;

    if (! $self->{allowSisters}) {
      $self->printError("DISABLED");
      return;
    }

    $self->{properties} = $self->{req}->getProperties('get_package');
    my $forceNew = 1 if lc($self->{properties}->{force_new}) eq "true";

    # Ignore <force_new> value if sisters aren't allowed to drive
    # 
    # In LAS_config.pl, this can be set to false:
    #
    #   allow_sister_driven = 'false'
    #
    # This lets someone with a real-time update LAS site create a package
    # whenever they know they've got the new data and ignore outside
    # requests for package creation.  Otherwise a popular site with 
    # hourly updates might have many requests per hour to generate
    # a new package.  If the package doesn't exist at all, we still
    # create it.

    if (! $self->{allowSisterDriven}) {
      $forceNew = 0;
    }

    # Create a new file if it doesn't exist or 'force_new' is used
    if (! -f "output/$pfile" || $forceNew) {
      my $comm = qq{../bin/livepack.pl --create output/$pfile $xmlFile};
      my $status = `$comm`;
      if ($status !~ /Created livepack file/) {
        $self->printError("SYSTEM: $status");
      }
    }

    use File::stat;

    $sb = stat("output/$pfile");
    if (! $sb) {
      printError("LAS: File \"output/$pfile\" was not created.");
      return;
    }
    my $size = $sb->size;
    my $mtime = $sb->mtime;
    print OUTPUT "<?xml version=\'1.0\' ?>\n<las_response>\n";
    print OUTPUT "  <package url=\"$output_path\" mtime=\"$mtime\" size=\"$size\"/>\n";
    print OUTPUT "</las_response>\n";
}

sub printError {
    my ($self,$error) = @_;
    print OUTPUT "<?xml version=\'1.0\' ?>\n<las_response>\n";
    print OUTPUT "  <error>$error</error>\n";
    print OUTPUT "</las_response>\n";
}

sub initRegion {
my ($self) = @_;
    $self->{properties} = $self->{req}->getProperties('getRegion');
    $self->{dataset} = $self->{properties}->{dataset}
        if $self->{properties}->{dataset};
    $self->{variable} = $self->{properties}->{variable}
        if $self->{properties}->{variable};
}

sub initLs {
    my ($self) = @_;
    $self->{properties} = $self->{req}->getProperties('ls');
    $self->{useLong} = 1 if $self->{properties}->{modifiers} eq "long";
    $self->{dataset} = $self->{properties}->{dataset}
        if $self->{properties}->{dataset};
    $self->{variable} = $self->{properties}->{variable}
        if $self->{properties}->{variable};
    $self->{separator} = $self->{properties}->{separator};
}

sub ls {
    my ($self) = @_;

    if ($self->{variable}){
        $self->lsVariable;
        return;
    }
    if ($self->{dataset}){
        if ($self->{dataset} eq 'LAS_VERSION') {
          $self->lsVersion;
        } else {
          $self->lsDataset;
        }
        return;
    }
    my $config = $self->{req}->getConfig;
    foreach my $child ($config->getChildren){
        if ($self->{useLong}){
            $self->pr($child->getName, $child->getLongName);
        } else {
            $self->pr($child->getName);
        }
    }
}

    format DSETOUTPUT =
@<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< @<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
@_
.

    format VAROUTPUT = 
@<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< @<<< @<<<<<<<<<<<<<<<< @<<<<<<<<<<<<<<<< @<<<<<<<<<<<<<<<<
@_
.


sub writeit {
    my $sep = shift;
    if ($sep ne ""){
        print OUTPUT join($sep, @_),"\n";
    } elsif (scalar(@_) > 1){
        write OUTPUT;
    } else {
        print OUTPUT @_, "\n";
    }
}

sub varpr {
    my $self = shift;
    OUTPUT->format_name("VAROUTPUT");
    writeit($self->{separator}, @_);
}

sub pr {
    my $self = shift;
    OUTPUT->format_name("DSETOUTPUT");
    writeit($self->{separator}, @_);
}

sub lsVariable {
    my ($self) = @_;
    my $config = $self->{req}->getConfig;
    my @datasets = grep {$_->getName eq $self->{dataset}} $config->getChildren;
    if (scalar @datasets == 0){
        print OUTPUT "Can't find dataset $self->{dataset}\n";
        return;
    }
    my $dataset = $datasets[0];
    my @variables = grep {$_->getName eq $self->{variable}}
                          $dataset->getChildren;
    if (scalar @variables == 0){
        print OUTPUT "Can't find variable $self->{variable}\n";
        return;
    }
    my $var = $variables[0];
    foreach my $child ($var->getChildren){
        if ($self->{useLong}){
            $self->varpr($child->getName, $child->getAttribute("type"),
            $child->getAttribute("units"), $child->getLo,
            $child->getHi);
        } else {
            $self->varpr($child->getName);
        }
    }
}

sub lsDataset {
    my ($self) = @_;
    my $config = $self->{req}->getConfig;
    my @datasets = grep {$_->getName eq $self->{dataset}} $config->getChildren;
    if (scalar @datasets == 0){
        print OUTPUT "Can't find dataset $self->{dataset}\n";
        return;
    }
    foreach my $child ($datasets[0]->getChildren){
        if ($self->{useLong}){
            $self->pr($child->getName, $child->getLongName);
        } else {
            $self->pr($child->getName);
        }
    }
}

sub lsVersion {
    my ($self) = @_;
    print OUTPUT "$LAS::VERSION\n";
}


##
# Kills a product server process group based on the process ID.
#
# Clicking on the "CANCEL request" link in the batch mode response
# page will send an XML request for this operation and will include
# the lock file of the process in question in a 
# <properties><util><lock_file> tag.
#
# Inside the lock file is the process ID of the child working on
# the product.  This is read in and the process group associated
# with this child is killed.  (Need to kill the entire process
# group as the child forks twice:  once to have a monitor for
# the ps_timeout and again to run Ferret.)
#
sub cancelRequest {
  my ($self) = @_;

  my $properties = $self->{req}->getProperties('util');
  my $lock_file = $properties->{lock_file};

# Open the lock file, returning if we cannot

  if (!open(LOCKFILE,$lock_file)) {
    print OUTPUT "Request seems to have finished by itself.\n";
    return;
  }

# Get the file base by removing '_batch.lock' from the lock file
# This will be used to unlink all the files associated with this request

  my $file_base = $lock_file;
  $file_base =~ s/_batch\.lock//;
  
# Read in a line, test the pid and kill the process if it's valid
# (Need to kill the negative $pid to kill the whole process group.)
# (See "Programming in Perl" 3rd edition p. 415: 'Signaling Process Groups')

  my $line = <LOCKFILE>;
  close LOCKFILE;
  chomp($line);

  my $pid = 0;
  my $cnt = 0;

  if ($line =~ /(PID=)(\d+)/) {
    $pid = $2;
    $cnt = kill(QUIT,$pid);
    if ($cnt !=1) {
      print OUTPUT "Unable to cancel request:  process $pid cannot be signalled.\n";
      return;
    }
  } else {
    print OUTPUT "Unable to cancel request:  lock file content \"$line\" does not match \"PID=###\".\n";
    return;
  }

# Check on the $pid to see if it still exists with kill(0,$pid)
# Unlink all associated files if it has been killed.
# Leave them around if we are unable to kill the process.

  sleep(1);
  my $still_alive = kill(0,$pid);
  if ($still_alive) {
    print OUTPUT "Unable to cancel request:  process $pid is still alive.\n";
    return;
  } else {
    print OUTPUT "Request successfully cancelled.\n";
    unlink glob("$file_base*");
    return;
  }

}


##
# Fork a process that will wait for the named product request to 
# finish and then send an email notification to the user.  The
# parent process returns a web confirmation that an email will
# be sent in the future.
#
# This method must be ready for all possible scenarios associated
# with the product request:
# - request finished
# - request resulted in an error
# - request timed out
# - request was cancelled
#
# Several properties must be available inside of <properties><ferret>
# in the LAS request object that specifies this method.
#   <output_file>  -- name of the product to watch
#   <email>        -- email address to which to send notification
#   <start_time>   -- start time of product request
#   <datasets>     -- dataset names in product request
#   <variables>    -- variable names in product request
#   <product_type> -- product type requested
#
# This method requires the perl module Mail::Sendmail in order to send email.
#
# (Based on Kevin O'Brien's original example.)
#
sub requestEmail {
  my ($self) = @_;

  use Mail::Sendmail;

# Get email address and output file from <properties>

  my $properties = $self->{req}->getProperties('util');
  my $email = $properties->{email};
  my $output_file = $properties->{output_file};
  my $start_time = $properties->{start_time};
  my $datasets = $properties->{datasets};
  my $variables = $properties->{variables};
  my $product_type = $properties->{product_type};

# Create the lock file name by converting any of the standard 
# LAS file type extensions to '_batch.lock'.

  my $lock_file = $output_file;
  $lock_file =~ s/\.[a-z]+$/_batch\.lock/g;

# Create err and timeout files from the lock file

  my $err_file = $lock_file;
  $err_file =~ s/\.lock/\.err/g;

  my $timeout_file = $lock_file;
  $timeout_file =~ s/\.lock/\.timeout/g;

# Check the validity of the email

  $valid_email = $self->validateEmail($email);

  if (!$valid_email) {

# Invalid email address entered. Go back and try again.....

    print OUTPUT "The email address you submitted -- $email -- is invalid.  Please hit the back button and try again.";

  } else {

# If the email address is valid, fork a child process which will wait until
# the file is created (or the lock file is removed) and then inform the user.

    if (!defined (my $pid = fork)) {

        die "SYSTEM ERROR: Unable to fork a process:  $!\n";

    } elsif ($pid) {

############################################################
#                                                          #
#                      Parent Process                      #
#                                                          #
# Let the user know they will be informed and exit.        #
#                                                          #
############################################################

      print OUTPUT "Thank you.\n\nAn email will be sent to $email when the requested file is available.";

      return;

    } else {

############################################################
#                                                          #
#                       Child Process                      #
#                                                          #
# Check in on the output file every 60 seconds until       #
# the result is ready or the lock file has been removed.   #
# Send back an appropriate response.                       #
#                                                          #
############################################################

      close(STDIN); close(STDOUT); close(STDERR);

      while (1) {

        sleep 5;

# If the lock file still exists, don't do anything at all.
# Otherwise we should return one of
# o result
# o error file
# o timeout message
# o message that the request was cancelled

        if (! -e $lock_file) {

          my %mail;

          my $from = 'LAS@localhost';
            
          my $subject = "LAS request notification:  NULL RESPONSE";

          my $message = "\nGreetings from the Live Access Server,\n\n\n";
          $message .= "The following LAS request was submitted on $start_time:\n\n";
          $message .= "\tfile type = $product_type\n";
          $message .= "\tdataset = $datasets\n";
          $message .= "\tvariable = $variables\n\n";

          if (-e "$output_file") {

# File name exists --> notify user where to pick up the file.

# Create the URL of the product

            my $LASConfig = &LAS::Server::getConfig;
            my $output_dir = $LASConfig->{output_alias};
            my $file_name = $output_file;
            $file_name =~ s/output\///g;
            my $product_url = "http://".$ENV{SERVER_NAME} . $output_dir . $file_name;

            $subject = "LAS request notification:  Product Ready!";
            $message .= "Your file is now ready at\n\n$product_url\n\n";

          } elsif (-s "$err_file") {

# Error file exists --> send it back to the user.

# Create the URL of the error_file

            my $LASConfig = &LAS::Server::getConfig;
            my $output_dir = $LASConfig->{output_alias};
            my $file_name = $err_file;
            $file_name =~ s/output\///g;
            my $error_url = "http://".$ENV{SERVER_NAME} . $output_dir . $file_name;

            $subject = "LAS request notification:  ERROR";
            $message .= "There was an error creating your product.  An error file can be found at\n\n$error_url\n\n";

          } elsif (-s "$timeout_file") {

# Timeout file exists --> alert user that the product request timed out.

            if (open(TIMEOUT,$timeout_file)) {
              my $line = <TIMEOUT>;
              close TIMEOUT;
              chomp($line);
        
              if ($line =~ /(ps_timeout=)(\d+)/) {
                $seconds = $2;
              }
            }

            $subject = "LAS request notification:  TIMED OUT";
            $message .= "Your request has timed out after $seconds seconds.";

          } else {

# No files exist --> alert user that product request was cancelled or killed.

            $subject = "LAS request notification:  CANCELLED";
            $message .= "Your request was cancelled or the process creating it was killed.\n\n";


          } # End of test for product/error/timeout file

          %mail = ( To      => $email,
                    From    => $from,
                    Message => $message,
                    Subject => $subject
                  );

          sendmail(%mail) or die $Mail::Sendmail::error;

          exit;

        }   # End of test for lock file
      }     # End of child 60 sec loop
    }       # End of fork/parent/child
  }         # End of test for valid email
}


##
# Validate a text string as a conforming email address or not.
#
# @param email incoming text string 
# @result 1|0 if the email string is valid|invalid
#
sub validateEmail {
  my ($self,$email) = @_;
    
  # If the e-mail address contains:                                      #

  if ($email =~ /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/ ||
        
    # the e-mail address contains an invalid syntax.  Or, if the         #
    # syntax does not match the following regular expression pattern     #
    # it fails basic syntax verification.                                #

    $email !~ /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/) {

    # Basic syntax requires:  one or more characters before the @ sign,  #
    # followed by an optional '[', then any number of letters, numbers,  #
    # dashes or periods (valid domain/IP characters) ending in a period  #
    # and then 2 or 3 letters (for domain suffixes) or 1 to 3 numbers    #
    # (for IP addresses).  An ending bracket is also allowed as it is    #
    # valid syntax to have an email address like: user@[255.255.255.0]   #
    # Return a false value, since the e-mail address did not pass valid  #
    # syntax.                                                            #

    return 0;

  } else {
        
    # Return a true value, e-mail verification passed.                   #

    return 1;

  }
}


############################################################


1;
