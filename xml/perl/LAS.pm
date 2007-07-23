# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# $Id: LAS.pm,v 1.136 2005/06/10 16:49:34 rhs Exp $
#

##
# Perl modules for parsing Live Access Server (LAS) XML configuration files 
# and XML data requests. The class hierarchy for these modules is availble
# in an <a href="las1_raster.gif">UML diagram</a>.
# <h5>Parse a configuration file:</h5>
# <pre>
# use LAS;
# # Parse a LAS configuration file and
# # print all datasets, variables and axes
#  my $parser = new LAS::Parser('las.xml');
#  my $config = new LAS::Config($parser);
#  foreach my $dataset ($config->getChildren){
#      print "Dataset:", $dataset->getLongName, "\n";
#      foreach my $var ($dataset->getChildren){
#          print "\tVariable:", $var->getLongName, "\n";
#          foreach my $axis ($var->getChildren){
#              print "\t\tAxis:", $axis->getName, ':',
#               $axis->getLo, ':', $axis->getHi, "\n";
#          }
#      }
# }
# </pre><h5>Parse a XML request string:</h5>
# <pre>
# # $xml is a string containing an XML data request:
#   my $parser = new LAS::Parser($xml, 1);
#   my $req = new LAS::Request($parser);
#   $req->resolveLinks;
#   println "Request properties:";
#   my %props = $req->getProperties('ferret');
#   foreach (keys %props){
#       println "Property: $_ = $props{$_}";
#   }
#    
#   println "Operation properties:";
#   %props = $req->getOp->getProperties('ferret');
#   foreach (keys %props){
#       my $value = $props{$_};
#       if (defined $value){
#           println "Property: $_ = $props{$_}";
#       } else {
#           println "Property: $_ = (null)";
#       }
#   }
#
#   println "Arguments:";
#   my @children = $req->getChildren;
#   foreach (@children){
#       my $class = ref($_);
#       if ($class eq "LAS::Variable"){
#           $_->print;
#       } elsif ($class eq "LAS::Region"){
#           $_->print;
#       }
#   }
#
# </pre>
#
package LAS;

$LAS::ParserClass = 'LASDB::DOM::Parser';
use Carp;
use URI::URL;
use LWP::UserAgent;
use File::PathConvert qw(rel2abs);
use TMAPDate;
use vars qw($VERSION @ISA @EXPORT);

BEGIN {
    require XML::DOM;
    my $needVersion = '1.23';
    die "need at least XML::DOM version $needVersion"
        unless $XML::DOM::VERSION >= $needVersion;
    
    @ISA = qw(Exporter);
    @EXPORT = qw(prettyPrint println printlni printwarnln printerrln assert create findElementByPath createFullPath setLogLevel);
    # $LAS::RCS_VERSION = '$Name:  $';
    # my @parts = split('\s+',$LAS::RCS_VERSION);
    # my $tmp = '';
    # if ($#parts == 2){
    #     my @versions = split('_',$parts[1]);
    #     $tmp = join('.', @versions[1..3]);
    # }
    #$LAS::VERSION = $tmp;
    $LAS::VERSION = '6.5';
}

my $IndentLevel = 0;
my %LogHash = ( NONE=>0, ERROR=>1, WARN=>2, ALL=>3);
my $LogLevel = 2;   

##
# Prints a line to STDOUT with an appended newline. Static method
# @param args strings to print out
sub println {
    print @_,"\n";
}

##
# Validate a URL
# @param url url to validate
# @param path path to preface to a file URL if the path isn't absolute
# @return 1 if URL exists, 0 otherwise
sub validateURL {
    my ($inurl,$prepath) = @_;
    $prepath = "" if ! $prepath;
    my $url = new URI::URL($inurl);
    my $scheme = $url->scheme;
    if (!$scheme || $scheme eq 'file'){
        my $path = $url->path;
        if ($path !~ /^\//){
            $prepath .= "/" if $prepath !~ /\/$/;
            $path = $prepath . $path;
        }
        my $fullpath = rel2abs($path);
        return -f $fullpath;
    } elsif ($scheme eq 'http'){
        my $req = new HTTP::Request('GET', $url);
        my $ua = new LWP::UserAgent;
        $ua->max_size(256);
        my $resp = $ua->request($req);
        return $resp->is_success;
    } else {
        die "Unknown URL type: $scheme";
    }
}

##
# @private
sub dumpStack {
    my $i = 1;
    my $result = "";
    my ($pack, $file, $line, $subname);
    while (($pack, $file, $line, $subname) = caller($i++)){
        $result .= qq{$file: $line: $file::$subname\n};
    }
    $result;
}

##
# @private
sub prettyPrint {
    $_ = shift;
    my $indent = -1;
    s/[\n\r]//g;
    s/\>\s+\</\>\</g;
    my @lines = split(/\</);
    foreach (@lines){
        my $flag = 0;
        if (/^\//) {
            $indent--;
            $flag = 1;
        }
        if (/\]\>$/) {
            my @parts = split(/\]/);
            println " " x $indent,"<",$parts[0];
            println " " x ($indent-1),"]>";
            $indent++ if (! /\/>/ && ! $flag);
            next;
        }
        println " " x $indent,"<",$_ if $_;
        $indent++ if (! /\/>/ && ! $flag);
    }
}

##
# Returns true if the string is a valid XML tag.<br>
# Only supports Western character set.
# 
sub isValidXMLTag {
    my $in = shift;
    return $in =~ /^([\w\.][\w\-\.\:]*)+$/;
}


##
# Merges all of the properties in the hashes passed in the
# argument list. Static method.<pre>
#   $props = &amp;LAS::mergeProperties($propsin,
#                          scalar $config->getProperties('ferret'),
#                          scalar $var->getDataset->getProperties('ferret'),
#                          scalar $var->getProperties('ferret'),
#                          scalar $req->getProperties('ferret'));
#
# A property with a value of "default" is only used if no previous
# property in the chain has been defined
# </pre>
# @param $props Reference to a hash that contains properties
# @param @hashes Array of hash references to merge
# @return Reference to a hash that contains merged properties
sub mergeProperties {
    my ($props, @hashes) = @_;
    foreach my $hash (@hashes){
        next if ! $hash;
        foreach my $key (keys %{$hash}){
            my $val = $hash->{$key};
            if (defined($val) && $val !~ /^\s*$/){
                if ($val =~ /^\s*default\s*$/){
                    $props->{$key} = "default" if ! defined($props->{$key});
                } else {
                    $props->{$key} = $hash->{$key};
                }
            }
        }
    }
    return $props;
}

##
# @private
sub printlni{
    for (my $i=0; $i < $IndentLevel; $i++){
        print "\t";
    }
    println @_;
}


##
# @private
sub printerrln {
    if ($LogLevel >= $LogHash{'ERROR'}){
        print STDERR "Error:",@_,"\n";
    }
}

##
# @private
sub printwarnln {
    if ($LogLevel >= $LogHash{'WARN'}){
        print STDERR "Warning: ",@_,"\n";
    }
}

##
# @private
sub setLogLevel {
    my $str = shift;
    my $level = $LogHash{$str};
    if (! defined $level || $level < 0 || $level > 2){
        printerrln("setLogLevel: invalid level ", $level, " ignored");
        return;
    }
    $LogLevel = $level;
}
    
##
# @private

sub assert {
    if (! shift){
        croak "Assertion failed";
    }
}

##
# @private
sub createElement {
    my ($doc, $name, $atts) = @_;
    croak "Create requires argument hash" if ref($atts) ne "HASH";
    my %attlist = %{$atts};
    my $e = $doc->createElement($name);
    foreach my $key (keys %attlist){
        $e->setAttribute($key, $attlist{$key});
    }
    return $e;
}

##
# @private
sub createFullDOMPath {
    my $e = shift;
    my $fullPath = "";
    while($e && $e->getNodeType == XML::DOM::ELEMENT_NODE){
        $fullPath = '/' . $e->getNodeName . $fullPath;
        $e = $e->getParentNode;
    }
    return $fullPath;
}

##
# @private
sub createFullPath {
    my $e = shift;
    return createFullDOMPath($e) if ref($e) =~ /^XML::DOM/;
    return $e->getPath;
}

##
# @private
sub _findNamedChild {
    my ($parents, $name) = @_;
    my @nodeList = ();
    if ($name eq ".."){
        foreach my $parent (@{$parents}){
            push(@nodeList, $parent->getParentNode);
        }
    } else {
        foreach my $parent (@{$parents}){
            my @children = $parent->getChildNodes;
            foreach my $e (@children){
                if ($e->getNodeType == XML::DOM::ELEMENT_NODE &&
                    $name eq $e->getTagName){
                    push(@nodeList, $e);
                }
            }
        }
    }
    return @nodeList;
}
##
# @private
sub findElementByPath {
    my ($root, $element, $path, $returnIfNotFound) = @_;
    my @pathElements = split /\//, $path;
    croak "Invalid target '$path'" if scalar @pathElements <= 0;

    my $start = shift @pathElements;
    my $isRelPath = ($start && $start !~ /\s+/);
    my $parent;
    if ($isRelPath){
        $parent = $element->getParentNode;
    } else {
        $parent = $root;
        $start = shift @pathElements;
    }

    if ($start eq ".."){
        $parent = $parent->getParentNode;
        $start = $parent->getTagName;
    }

    croak "Can't find match target '$path'\n",
    "Failed match of '$start' to '",
    $parent->getTagName, "'"
        if $start ne $parent->getTagName;
    my @parents = ($parent);
    foreach (@pathElements){
        @parents = _findNamedChild(\@parents, $_);
        if (scalar @parents == 0){
            if ($returnIfNotFound){
                return "";
            }
            croak "Can't find match target '$path' at '$_'" if
                scalar @parents == 0;
        }
    }
    die "Multiple matches for match target '$path'" if
        scalar @parents > 1;
    return $parents[0];
}


#
# Recursively add XML elements to the hash table defined by $ids
# Only elements with parent element tag names in $searchNodesRef hash
# reference are added.
# TODO -- check for cycles
#

##
# @private
sub traverseLinks {
    my ($ids, $elem, $searchNodesRef) = @_;
    my $parent = $elem->getParentNode;
    if ($parent->getNodeType == XML::DOM::ELEMENT_NODE){
        my $parentId = $elem->getParentNode->getTagName;
        if ($searchNodesRef->{$parentId}){
            my $id = $elem->getTagName;
            if ($id){
                my $key = $ids->{$id};
                if ($key){
                    croak("Duplicate element '$id'");
                }
#               LAS::printerrln("traverseLinks: adding: $id");
                $ids->{$id} = $elem;
            }
        }
    }
    my $nodes = $elem->getChildNodes;
    for (my $i=0; $i < $nodes->getLength; $i++){
        my $node = $nodes->item($i);
        if ($node->getNodeType == XML::DOM::ELEMENT_NODE){
            traverseLinks($ids, $node, $searchNodesRef);
        }
    }
}

##
# @private
sub createLinks {
    my $ids = {};
    traverseLinks($ids, $_[0]->getDocumentElement, $_[1]);
    return $ids;
}


#
# Static function to create a new LAS object
#
##
# @private
sub create {
    my ($class, $config, $elName, $atts, $argref) = @_;
    my $parser = $config->getParser;
    my $doc = $parser->getRoot;
    my $e = createElement($doc, $elName, $atts, $argref);
    my $id = $e->getTagName;
    my %ids = $parser->getIds;
    if ($id && $ids{$id}){
        croak "Duplicate id " . $id . " for new element " . $elName;
    }
    my @args = ();
    @args = @{$argref} if $argref;
    unshift @args, $e;
    return new $class($config, @args);
}

#
# Process any institution definitions
# Make the bureaucrats happy
# Probably shouldn't be in this base class...
#
##
# @private
sub processInstitution {
    my $config = shift;
    my $e = shift;
    my @institutions = $e->getElementsByTagName("institution", 0);
    croak "Only one 'institute' element allowed at top level"
        if scalar @institutions > 1;
    if (@institutions){
        return new LAS::Institution($config, $institutions[0]);
    }
}

##
# Iterate over a list of XML elements: <pre>
# my $iter = new LAS::ElementIterator($children);
# while ($iter->hasMore){
#   my $node = $iter->next;
#   print $node->getNodeName, "\n";
# }  
# </pre>
#
package LAS::ElementIterator;

##
# Creates a new iterator. 
# @param list Reference to XML::DOM::NodeList containing all of the nodes
# @return Reference to LAS::ElementIterator
sub new {
    my $self = [];
    my $class = shift;
    bless $self, $class;
    my $list = shift;
    foreach my $node (@{$list}){
        if ($node->getNodeType == XML::DOM::ELEMENT_NODE){
            push(@{$self}, $node);
        }
    }
    return $self;
}

##
# Returns true if any nodes remain in the iterator
# @return true if nodes remain
sub hasMore() {
    my $self = shift;
    return scalar @{$self} > 0;
}

##
# Gets the next node in the list
# @return next node or undef if at end of node list
sub next() {
    my $self = shift;
    if ($self->hasMore()){
        return shift @{$self};
    } else {
        return undef;
    }
}

##
# Parse a LAS XML configuration file.
# <pre>
# my $parser = new LAS::Parser('las.xml');
# my $config = new LAS::Config($parser);
# foreach my $child ($config->getChildren){
#     print "Dataset name is: ", $child->getLongName, "\n";
# }
# </pre>
# 

package LAS::Parser;
use Carp;
use File::Basename;
use Cwd;
#
# Nodes that contains element tag names that correspond to their
# id
my $SearchNodes = {datasets=>1, grids=>1, axes=>1, operations=>1};

##
# Creates a new LAS parser
# @param $url URL of file or a string to parse. Only file: urls are supported
# @param $isString (optional) true if $fname is a string
# @param $parserClass (optional) use this object as the XML parser
# @param @args (optional) optional arguments to pass to XML parser

sub new {
    my $self = {};
    bless $self, shift;
    my ($fname, $isString, $parserClass, @args) = @_;
    $parserClass = 'XML::DOM::Parser' if ! $parserClass  ;
    @args = (ErrorContext => 2)if ! @args;
    my $parser;
    eval {
        $parser = new $parserClass(@args);
    };
    die $@ if $@;
    if ($isString){
        $self->{doc} = $parser->parse($fname) or croak "Can't parse $fname";
    } else {
# Need to change directories because there's no way I know of
# to set include paths with XML
        my $changeDir = $fname =~ /\//;
        my $dir;
        if ($changeDir){
            $dir = cwd() or die "Couldn't get current working directory";
            chdir(dirname($fname)) or
                die "Couldn't change to directory ", dirname($fname);
            $fname = basename($fname);
        }
        $self->{doc} = $parser->parsefile($fname) or croak "Can't parse $fname";
        if ($changeDir){
            chdir($dir) or
                die "Couldn't change back to directory ", $dir;
        }
    }
    $self->{ids} = LAS::createLinks($self->{doc}, $SearchNodes)   
      if $parserClass eq 'XML::DOM::Parser'; 
    $self->{pathCache} = {};
    $self->{url} = $fname;
    return $self;
}

##
# Returns the URL associated with the parser
# @return URL of parser
# 

sub getURL {
    $_[0]->{url};
}

##
# Returns the root document of the XML DOM tree
# @return XML::DOM::Document

sub getRoot {
    my $self = shift;
    return $self->{doc};
}

##
# @private

sub getIds {
    my $self = shift;
    return %{$self->{ids}};
}

##
# @private
sub dumpIds {
    my $self = shift;
    my $hash = $self->{ids};
    foreach (%{$hash}){
        LAS::println "Key: $_ Value: $hash->{$_}";
    }
}
##
# @private
sub resolveLink {
    my $self = shift;
    my $element = shift;
    my $rval = $element;
    if ($element->getNodeType == XML::DOM::ELEMENT_NODE &&
        $element->getNodeName eq "link"){
        my $href = $element->getAttribute("match");
        if (! $href){
          croak "Link without 'match' attribute";
        }
        my $root = $self->getRoot->getDocumentElement;

# Only use path cache for absolute paths
# TODO: This could be very inefficient if a lot of relative paths are used
#       Need a better fix (someday)
        my $linkedTo;
        $linkedTo = $self->{pathCache}->{$href} if $href =~ /^\//;

        if (!$linkedTo){
            $linkedTo = LAS::findElementByPath($root, $element, $href);
            $self->{pathCache}->{$href} = $linkedTo;
        }
        if (! $linkedTo){
            croak "Can't find element with name $href";
        }
        $rval = $linkedTo;
    }
    return $rval;
}

package LAS::Parser::Browser;
@LAS::Parser::Browser::ISA = qw(LAS::Parser);
use Carp;
use File::Basename;
use Cwd;

##
# Creates a new LAS parser for the browser list.
# @param $url URL of file or a string to parse. Only file: urls are supported
# @param $isString (optional) true if $fname is a string
# @param $parserClass (optional) use this object as the XML parser
# @param @args (optional) optional arguments to pass to XML parser

sub new {
    my $self = {};
    bless $self, shift;
    my ($fname, $isString, $parserClass, @args) = @_;
    $parserClass = 'XML::DOM::Parser' if ! $parserClass  ;
    @args = (ErrorContext => 2)if ! @args;
    my $parser;
    eval {
        $parser = new $parserClass(@args);
    };
    die $@ if $@;
    if ($isString){
        $self->{doc} = $parser->parse($fname) or croak "Can't parse $fname";
    } else {
# Need to change directories because there's no way I know of
# to set include paths with XML
        my $changeDir = $fname =~ /\//;
        my $dir;
        if ($changeDir){
            $dir = cwd() or die "Couldn't get current working directory";
            chdir(dirname($fname)) or
                die "Couldn't change to directory ", dirname($fname);
            $fname = basename($fname);
        }
        $self->{doc} = $parser->parsefile($fname) or croak "Can't parse $fname";
        if ($changeDir){
            chdir($dir) or
                die "Couldn't change back to directory ", $dir;
        }
    }
    $self->{ids} = LAS::createLinks($self->{doc}, $SearchNodes)
        if $parserClass eq 'XML::DOM::Parser';
    $self->{pathCache} = {};
    $self->{url} = $fname;
    return $self;
}

##
# A LAS::Container object is a general purpose utility class that 
# encapsulates an element of the Live Access Server XML hierarchy. An
# XML element in the XML configuration file can be represented
# by a subclass of LAS::Container. For instance, a <b>dataset</b> tag in
# the XML configuration file is represented by the LAS::Dataset object
# which inherits from this LAS::Container object.<p>
# 
# All containers contain a list of <i>children</i> (possibly empty). 
# The type of the children
# depends on the parent container -- LAS::Dataset has children of type 
# LAS::Variable, while a LAS::Variable object will have children of type
# LAS::Axis.<p>
#
# All containers have a set of (possibly empty) attributes. These attributes
# represent the attributes defined for the encapsulated XML tag. 

package LAS::Container;
use Carp;
use vars qw(@ISA @EXPORT);

BEGIN {
    use Exporter ();
    @ISA = qw(Exporter);

};

##
# Create a new LAS::Container
# @param $config The LAS::Config object containing this container
# @param $e A XML::DOM::Element object

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $config = shift;
    if ($config && ref($config) ne 'LAS::Config'){
        croak 'Invalid argument to LAS::Container; type was ',ref($config);
    }
    my $e = shift;
    croak "Internal:Missing XML DOM element argument" if ! $e;
    my $self = {
        config => defined $config && $config,
        element => $e,
        args => \@_,
        children => [],
        attributes => $e->getAttributes,
        cachedAttributes => {},
        class => $class
    };
    bless $self, $class;

# Special hack to speed up access to longName
    my $name = $self->getAttribute("name");
    $name = $self->getName if ! $name;
    $name =~ s/^\s+//;
    $name =~ s/\s+$//g;
    $self->{longName} = $name;


    $self->_initFullPath;
    $self->_setupProperties;
    $self->_initialize;

    if ($self->{config}){
        croak "Duplicate element ",$e->getTagName
            if $self->{config}->getElementToContainer($e);
        $self->{config}->setElementToContainer($e,$self);
    }

    return $self;
}

##
# Check that an XML attribute is one of a list of possibilities
# @param requireAttInList
sub requireAttInList {
    my ($self, $attname, @list) = @_;
    my $att = $self->getAttribute($attname);
    my $name = $self->getName;
    die "XML tag <$name> missing attribute:$attname" if ! defined($att);
    die "Unknown attribute '$att' for XML tag <$name> must be one of ",
         join(',',@list) if ! grep {/^${att}$/} @list;
}


##
# Check that the XML element has all of the required elements
# Expat isn't a validating XML parser...so we have to do this
# @param @atts names of attributes to check for

sub requireAtts {
    my ($self, @atts) = @_;
    my @bad = ();
    foreach my $att (@atts){
        push(@bad, $att) if ! defined($self->getAttribute($att));
    }
    if ($#bad >= 0){
        my $name = $self->getName;
        die "XML tag <$name> missing attributes:",
        join(',',@bad);
    }
}

##
# Copy a LAS::Container object<p>
# This is a shallow clone. Properties are copied, but also only
# a shallow copy
#

sub clone {
    my $self = shift;
    my $newself = {
        properties => {}
    };
    foreach my $key (%{$self}){
        if ($key eq 'properties'){
            foreach my $propkey (%{$self->{properties}}){
                $newself->{properties}->{$propkey} =
                    $self->{properties}->{$propkey};
            }
        } else {
            $newself->{$key} = $self->{$key};
        }
    }
    bless $newself, $self->{class};
}

##
# @private
sub getInstances {
    my $self = shift;
    my $name = shift;
    my $rval = $self->{$name . 'Instances'};
    die (ref($self) . "::getInstances: no value for $name") if ! $rval;
    if (wantarray){
        return %{$rval};
    } else {
        return $rval;
    }
}

##
# Remove a child of this node
# @param $child child node

sub removeChild {
    $_[0]->getElement->removeChild($_[1]->getElement);
}

##
# Finds a child of this container object with the requested tag name
# @param $name Name of tag to find
# @return LAS::Container object or undef

sub findChild($$) {
    my ($self, $name) = @_;
    foreach my $child (@{$self->{children}}){
        my $cname = $child->getName;
        return $child if $name eq $cname;
    }
    0;
}

##
# Get the LAS::Parser object that created this container
# @return LAS::Parser

sub getParser {
    my $self = shift;
    return if ! $self->{config};
    return $self->{config}->{parser};
}

##
# Get the LAS::Config object that contains this container
# @return LAS::Config
sub getConfig {
    my $self = shift;
    croak 'LAS::Container has null config' if ! $self->{config};
    return $self->{config};
}

##
# @private
sub _initFullPath {
    my $self = shift;
    $self->{fullPath} = &LAS::createFullPath($self->{element});
}

##
# @private
sub resetFullPath {
    my $self = shift;
    my $instances = $self->{instances};
    if ($instances){
        $instances->{$self->{fullPath}} = undef;
        $self->_initFullPath;
        $self->addToUnique;
    }
}

##
# Gets the long name of this container. This is the value of the XML "name"
# attribute
# @return String containing the "name" attribute
sub getLongName {
    my $self = shift;
    return $self->{longName};
}
    

##
# Gets the XML::DOM::Element contained by this container
# @return XML::DOM::Element

sub getElement {
    my $self = shift;
    return $self->{element};
}

##
# @private
sub getFullPath {
    my $self = shift;
    return $self->{fullPath};
}

##
# @private
sub _initPropertyHash {
    my ($self, $propHash, $parent) = @_;
    my $children = $parent->getChildNodes;
    return if ! $children;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $node = $iter->next;
        my $hash = {};
        my $moreChildren = $node->getChildNodes;
        if ($moreChildren){
            my $iter1 = new LAS::ElementIterator($moreChildren);
            while ($iter1->hasMore){
                my $pnode = $iter1->next;
                                # Look for '#pcdata' attribute first, as this 
                                # might be from serialized RDBMS XML
                my $value = $pnode->getAttribute('#pcdata');
                if (!(defined($value) && $value ne "")){
                    if ($pnode->getFirstChild){
                        $value = $pnode->getFirstChild->getNodeValue;
                    }
                }
                if (defined($value)){
                    $value =~ s/^\s*//g;
                    $value =~ s/\s*$//g;
                }
                $hash->{$pnode->getNodeName} = $value;

# NOTE: Test properties by commenting out the next 4 lines
#               &LAS::Server::debug("Adding property ",
#                             $node->getNodeName, ':', $pnode->getNodeName,
#                                   " for ", ref($self), " value ",
#                                   $value, "\n");

            }
        }
        $propHash->{$node->getNodeName} = $hash;
    }
}


##
# @private
sub _setupProperties {
    my ($self) = @_;
    my $children = $self->{element}->getChildNodes;
    my $nodes = new LAS::ElementIterator($children);
    while ($nodes->hasMore){
        my $e = $nodes->next;
        if ($e->getNodeName eq "properties"){
            my $hash = {};
            $self->_initPropertyHash($hash, $e);
            $self->{properties} = $hash;
        }
    }
}

##
# @private
sub addElement {
    my ($self, $parent, $class, $name, $atts, $argref) = @_;
    my $config = $self->getConfig;
    my $parser = $config->getParser;
    my $newobj = LAS::create($class, $config, $name, $atts, $argref);
    my @children = $self->{element}->getElementsByTagName($parent);
#    carp "Warning: duplicate parent elements for '$parent'; using first"
#       if scalar @children > 1;
    if (! scalar @children){
        $children[0] = $parser->getRoot->createElement($parent);
        $self->{element}->appendChild($children[0]);
    }
    $self->addChild($newobj, $children[0]);
    return $newobj;
}

##
# Gets the properties contained by the XML tag in this container.<pre>
# # Return all Ferret properties for the object:
# my $props = $var->getProperties('ferret');
# </pre>
# @param $name Name of property to retrieve (e.g. ferret)
# @return If in array context a hash; otherwise, a reference to a hash
sub getProperties {
    my ($self, $name) = @_;
    croak "Must specify a property element name in 'getProperties'" if ! $name;
    my $rval;
    if ($self->{properties}){
        $rval = $self->{properties}->{$name};
    } 
    if (wantarray){
        return $rval ? %{$rval} : ();
    } else {
        return $rval;
    }
}

##
# @private
sub setAttribute {
    my ($self, $attribute, $value) = @_;
    $self->{element}->setAttribute($attribute, $value);
}

##
# Gets the named XML attribute associated with this XML tag
# @param $name Name of attribute
# @return Value of attribute
sub getAttribute {
    my ($self, $name) = @_;
    my $cachedAttribute = $self->{cachedAttributes}->{$name};
    return $cachedAttribute if defined $cachedAttribute;

    my $item = $self->{attributes}->getNamedItem($name);
    if ($item){
# Hack to distinguish between value from XML::DOM::Node (which should be
# an attribute) and emulation in LASDB routines
        my $rval;
        if (ref($item)){        # From XML::DOM
            $rval = $item->getValue;
        } else {
            $rval = $item;
        }
        my $name = $self->getName;
        $rval =~ s/\$e/$name/g; # Replace any $e with the element name
        $self->{cachedAttributes}->{$name} = $rval;
        return $rval;
    } else {
        return undef;
    }
}

##
# @private
sub addChild {
    my ($self, $child, $parent) = @_;
    my $e;
    if ($parent){
        $e = $parent;
    } else {
        $e = $self->{element};
    }
    $e->appendChild($child->{element});
    $child->resetFullPath;
    push(@{$self->{children}}, $child);
}

##
# Get all of the child elements of this container<p>
# Example:
# <pre>
# foreach my $child ($container->getChildren){
#   if (ref($child) eq "LAS::Variable"){
#      print "Got a variable named: ", $child->getLongName, "\n";
#   }
# }
# </pre>
# @return List of children

sub getChildren {
    my $self = shift;
    return @{$self->{children}};
}

##
# Add to hash table of unique ids associated with this object
# @private
#
sub addToUnique {
    my $self = shift;
    my $unique = $self->{instances};
    if ($unique){
        my $name = $self->getFullPath;
        croak "No fullpath for ", $self->getName if ! $name;
        if ($name){
            my $class = ref($self);
            if ($class ne 'LAS::Axis'){
                croak "addToUnique: duplicate element id: $name"
                    if $unique->{$name};
                $unique->{$name} = $self;
            }
        } else {
            croak "This node is not an element";
        }
    }
}

##
# Returns the tag name of the XML DOM element
# @return XML element name
sub getName {
    return shift->{element}->getTagName;
}

##
# @private
sub toXML {
    my $self = shift;
    return $self->{element}->toString;
}

##
# @private
sub addURL {
    my ($self) = @_;
    my $url = $self->getAttribute("url");
    my $urls = $self->getConfig->getInstances('url');
    $urls->{$url} = $self if defined $url;
}

##
# @private
sub findURL {
    my ($self, $key) = @_;
    my ($foo, $path) = split(/\:\//, $key);
    if (!$path){
        $key = "file:" . $key;
    }
    return $self->getConfig->getInstances('url')->{$key};
}


##
# An axis contains coordinate information for a given variable.<br>
# An axis can be <i>regular</i> or <i>irregular</i>. All axes have
# upper and lower bounds and a size. Regular axes also have a step
# size. If the axis is irregular, a call to <b>getChildren</b> 
# will return all of the axis values

package LAS::Axis;
@LAS::Axis::ISA = qw(LAS::Container);
BEGIN {
    import Carp;
    use vars qw($Instances);
}

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{instances} = $self->getConfig->getInstances('axis');
    $self->addToUnique;
    return $self;
}

##
# @private
sub _initialize {
    my $self = shift;
    my $children = $self->{element}->getElementsByTagName("arange");
    if ($children->getLength > 0){
        $self->_initArange($children);
        return;
    } 
    $children = $self->{element}->getElementsByTagName("v");
    if ($children->getLength > 0){
        $self->_initValues($children);
        return;
    } 
        
#    croak("Axis id=", $self->getName,
#                " missing 'v' or 'arange' elements");
}

##
# Returns the lower bound of the axis
# @return lower bound of axis
sub getLo {
    my $self = shift;
    return $self->{lo};
}

##
# Returns the upper bound of the axis
# @return upper bound of axis
sub getHi {
    my $self = shift;
    return $self->{hi};
}

##
# Returns the size of the axis
# @return size of axis
sub getSize {
    my $self = shift;
    return $self->{size};
}

##
# Returns step size. Undefined if irregular axis
# @return step size
#
sub getStep {
    my $self = shift;
    if (ref $self->{step}){
        return @{$self->{step}};
    } else  {
        return $self->{step};
    }
}

##
# Returns the units of the axis
# @return the units of the axis
sub getUnits {
    my $self = shift;
    $self->{element}->getAttribute("units");
}

##
# @private
sub _initValues {
    my $self = shift;
    my $children = shift;
    my $nodes = new LAS::ElementIterator($children);
    my $values = $self->{children};
    while ($nodes->hasMore){
        my $node = $nodes->next;
        $node = $self->getParser->resolveLink($node);
        if ($node->getNodeName eq "v"){
                                # Look for '#pcdata' attribute first, as this 
                                # might be from serialized RDBMS XML
            my $text = $node->getAttribute('#pcdata');
            $text = $node->getFirstChild->getNodeValue if ! (defined($text) && $text ne "");
            $text =~ s/^\s+//g;
            $text =~ s/\s+$//g;
            push(@{$values}, $text);
            ++$self->{size};
        }
    }
    $self->{lo} = $values->[0];
    $self->{hi} = $values->[$#{$values}];
}


#
# This only supports "month", "year", and "day" units
#


##
# @private
sub handleTime {
    my ($self,$start, $step, $size, $units) = @_;
    my $date = new TMAP::Date($start);
    if (! $date->isOK){
        $self->handleNumber($start, $step, $size, $units);
        return;
    }
    die "Error in axis:", $self->getName,
    ":arange for time must have integer step value"
        if $step != int($step);
    my ($sy, $sm, $sd) = $date->getYMD();
    my ($sh, $smin, $ssec) = $date->getHMS;
    my ($sty, $stm, $std, $sth, $stmin, $stsec) = (0,0,0,0,0,0);
    my $total = $size-1;
    if ($units =~ /month/){
        $stm = $step;
    } elsif ($units =~ /year/){
        $sty = $step;
    } elsif ($units =~ /day/){
        $std = $step;
    } elsif ($units =~ /hr|hour/){
        $sth = $step;
    } elsif ($units =~ /min/){
        $stmin = $step;
    } elsif ($units =~ /sec/){
        $stsec = $step;
    } else {
        croak "Unknown time unit: $units.";
    }
    my $endDate = $date->addDelta($sty*$total,$stm*$total,$std*$total,
                                  $sth*$total,$stmin*$total,$stsec*$total);
    $self->{lo} = $date->toFerretString;
    $self->{hi} = $endDate->toFerretString;
    $self->{step} = [$sty, $stm, $std, $sth, $stmin, $stsec];
}

##
# @private
sub handleNumber {
    my ($self,$start, $step, $size, $units) = @_;
    $self->{lo} = $start;
    $self->{hi} = $start + ($size-1)*$step;
    $self->{step} = $step;
}

##
# @private
sub _initArange {
    my $self = shift;
    my $children = shift;
    my $nodes = new LAS::ElementIterator($children);
    my $node = $nodes->next;
    my $start = $node->getAttribute("start");
    my $step = $node->getAttribute("step");
    my $size = $node->getAttribute("size");
    my $units = $self->{element}->getAttribute("units");
    my $type = $self->getAttribute("type");
    my $name = $self->getName;
    $units = lc($units);
    (defined $start && defined $step && defined $size) ||
        croak "Axis $name: arange must contain all of start,step,size attributes";
    defined $units ||
        croak "Axis $name: axis must contain units attribute";
    $step != 0 ||
        croak "Axis $name: axis step (currently '$step')  must be != 0";
    if ($type eq 't' && $step < 0){
        croak "Axis $name: axis step (currently '$step')  must be > 0";
    }
    $self->{size} = $size;
#
# Units of month, year, and day are assumed to be time, everything else
# is a number
#
    my $isTime = ($units =~ /month|day|year|hr|hour|min|sec/);
    if ($isTime){
        $self->handleTime($start, $step, $size, $units);
    } else {
        $self->handleNumber($start, $step, $size, $units);
    }
}

##
# @private
sub addIrregularData {
    my ($self, $data) = @_;
    my $e = $self->{element};
    my @children = $e->getElementsByTagName("v");
    my $doc = $e->getOwnerDocument;
    return if (scalar @children > 0);
    foreach (@{$data}){
        my $newtag = $doc->createElement("v");
        $e->appendChild($newtag);
        my $newtext = $doc->createTextNode;
        $newtext->appendData($_);
        $newtag->appendChild($newtext);
    }
}

##
# @private
sub addRegularData {
    my ($self, $start, $delta, $size) = @_;
    my $e = $self->{element};
    my @children = $e->getElementsByTagName("arange");
    return if (scalar @children > 0);
    my $newtag = $e->getOwnerDocument->createElement("arange");
    $newtag->setAttribute("start", $start);
    $newtag->setAttribute("step", $delta);
    $newtag->setAttribute("size", $size);
    $e->appendChild($newtag);
}

##
# A LAS variable contains metadata about a scalar variable.

package LAS::Variable;
@LAS::Variable::ISA = qw(LAS::Container);
use Carp qw(croak carp);
use LAS qw(println printlni);


##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{instances} = $self->getConfig->getInstances('variable');
    $self->addToUnique;
    $self->{units} = $self->getAttribute("units");
    $self->{layer} = $self->getAttribute("layer");
    if (! $self->{units}){
        my $dset = $self->getDataset;
        my $fullName = $self->getDataset->getName . ':' . $self->getName;
#       &LAS::printwarnln("No units specified for ", $fullName);
    }
    $self->{institution} = &LAS::processInstitution($self->getConfig, $self->{element});
    if (! $self->{institution}){
        $self->{institution} = $self->getDataset->getInstitution;
    }
    return $self;
}

##
# Returns the LAS::Analysis object associated with this variable (if present)
# @return LAS::Analysis
sub getAnalysis {
    return $_[0]->{analysis};
}

##
# Returns the LAS::Institution object associated with this variable
# @return LAS::Institiution
sub getInstitution {
    my $self = shift;
    my $inst = $self->{institution};
    return $inst;
}

##
# Returns the units string associated with this variable
# @return string containing units
sub getUnits {
    my $self = shift;
    my $units = $self->{units};
    return $units;
}

##
# Returns the optional layer attribute of this variable.
# This is need to support the ArcIMS driver contributed
# by John Cartwright.
# @return string containing ArcIMS layer name
sub getLayerName {
    my $self = shift;
    my $layer = $self->{layer};
    return $layer;
}

##
# Sets the units string associated with this variable
# @param $units variable unit string
sub setUnits {
    my $self = shift;
    my $units = shift;
    $self->{units} = $units;
}

##
# Returns the parent dataset that contains this variable
# @return LAS::Dataset

sub getDataset {
    my $self = shift;
    return $self->{parent};
}

##
# @private
sub setURL {
    my $self = shift;
    $self->{url} = new URI::URL(shift);
}

##
# Returns the URL associated with this variable. If no URL is associated
# with the variable, returns the URL of the parent dataset
# @return string containing URL
sub getURL {
    my $self = shift;
    return $self->{url} if defined($self->{url});
    my $dataset = $self->getDataset;
    my $url1 = $dataset->getAttribute("url");
    my $url2 = $self->getAttribute("url");
    my $url;
    if (defined($url1)){
        if (defined($url2)){
            $url = new URI::URL($url2, $url1);
            $url = $url->abs();
        } else {
            $url = new URI::URL($url1);
        }
    } elsif (defined($url2)){
        $url = new URI::URL($url2);
    } else {
        croak "No 'url' attribute specified for 'var' element";
    }
    $self->{url} = $url;
    return $url;
}    

# Returns the URL associated with this variable without the URL fragment
# @return string containing URL
sub getURL_nofrag {
    my $self = shift;
    my $newurl = new URI::URL($self->getURL);
    $newurl->frag(undef);
    $newurl;
}

# Children of variable are axes, not grid i.e. the hierarchy
# is flattened one level (probably an unfortunate decision though more 
# convenient).
# The grid element can be obtained through getGridElement
##
# @private
sub _initialize {
    my $self = shift;
    $self->{parent} = $self->{args}->[0];
    $self->addURL;

}

##
# Returns the children of this variable
# @return @LAS::Axis

sub getChildren {
    my $self = shift;
    if (scalar @{$self->{children}} == 0){ # optimization hack
        my $children = $self->{element}->getChildNodes;
        my $nodes = new LAS::ElementIterator($children);
        while ($nodes->hasMore){
            my $node = $nodes->next;
            next if $node->getTagName eq "properties";
            $node = $self->getParser->resolveLink($node);
            $self->{gridElement} = $node;
            my $newchildren = $node->getChildNodes;
            my $axes = new LAS::ElementIterator($newchildren);
            while ($axes->hasMore){
                my $e = $axes->next;
                my $newnode = $self->getParser->resolveLink($e);
                my $axis = $self->{config}->getElementToContainer($newnode);
                $axis = new LAS::Axis $self->getConfig,$newnode if ! $axis;
                $self->{parent}->addAxisInstance($axis);
                push(@{$self->{children}}, $axis);
            }
        }
    }
    return @{$self->{children}};
}

##
# @private
sub getGridElement {
    my ($self) = shift;
    return $self->{gridElement};
}

##
# @private
sub print {
    my $var = shift;
    my $url = $var->getURL;
    $IndentLevel++;
    printlni "Variable: URL: ", $url->scheme, ':',$url->full_path;
    printlni "Variable: Name: ", $var->getName;
    $IndentLevel--;
}

##
# @private
sub addAxis {
    my ($self, $config, $name, $gridElem, $atts) = @_;
    my $dsetName = $self->getDataset->getElement->getTagName;
    my $axisName = $dsetName . "_$name";
    my $linkName = "/lasdata/axes/" . $axisName;
    my $doc = $gridElem->getOwnerDocument;

    my %axes = $self->getConfig->getInstances('axis');
    my $axis = $axes{$linkName};
    if (!$axis){
        $axis = $config->addElement("axes", "LAS::Axis", $axisName, $atts);
        $self->getConfig->getInstances('axis')->{$linkName} = $axis;
    }


# Add links to grid definition if not there
    my @children = $gridElem->getChildNodes;
    my $found = 0;
    foreach my $child (@children){
        if ($child->getNodeType == XML::DOM::ELEMENT_NODE &&
            $child->getTagName eq "link" &&
            $child->getAttribute("match") eq $linkName){
            $found = 1;
            last;
        }
    }
    if (! $found){
        my $newlink = $doc->createElement("link");
        $newlink->setAttribute("match", $linkName);
        $gridElem->appendChild($newlink);
    }
    return $axis;
}

##
# @private
sub addGrid {
    my ($self, $gridName) = @_;
    my $dsetName = $self->getDataset->getElement->getTagName;
    die "Must specify grid name" if ! $gridName;
    my $linkName = "/lasdata/grids/" . $gridName;
    my $doc = $self->{element}->getOwnerDocument;
    my $grid =
        LAS::findElementByPath($doc->getDocumentElement, $self->getElement,
                               $linkName, 1);
# Already have link?
    my $newe;
    my @linkChildren = $self->getElement->getElementsByTagName("link", 0);
    if (scalar @linkChildren > 0){
        die "Only one link node allowed for variable"
            if scalar @linkChildren > 1;
        $newe = $linkChildren[0];
        die "Bad grid link for ", $self->getName if
            $newe->getAttribute("match") ne $linkName;
    } else {
        my $newe = $doc->createElement("link");
        $newe->setAttribute("match", $linkName);
        $self->{element}->appendChild($newe);
    }
    return ($grid,1) if $grid;

#
# Create grid if it doesn't exist
#
    my $e = $doc->getDocumentElement;
    my @children = $e->getElementsByTagName("grids");
    my $grids;
    if (scalar @children == 0){
        $grids = $doc->createElement("grids");
        $e->appendChild($grids);
    } else {
        $grids = $children[0];
    }
    my $newgrid = $doc->createElement($gridName);
    $grids->appendChild($newgrid);
    return ($newgrid,0);
}

##
# Contains metadata about a <i>composite</i> variable -- a
# variable that contains a number of LAS scalar variables. This
# allows LAS to represent vector quanties.<p>
# The children of a LAS:Compvar are a list of LAS::Var

package LAS::CompVar;
@LAS::CompVar::ISA = qw(LAS::Container);
use Carp qw(croak);
use LAS qw(println printlni);


##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{parent} = $_[2];
    $self->{instances} = $self->getConfig->getInstances('compVar');
    $self->addToUnique;
    $self->{institution} = &LAS::processInstitution($self->getConfig,$self->{element});
    if (! $self->{institution}){
        $self->{institution} = $self->getDataset->getInstitution;
    }
    return $self;
}

##
# Returns the institution contained by this variable
# @return LAS::Institution

sub getInstitution {
    my $self = shift;
    my $inst = $self->{institution};
    return $inst;
}

##
# Returns the parent dataset of this variable
# @return LAS::Dataset

sub getDataset {
    my $self = shift;
    return $self->{parent};
}

##
# @private
sub _initialize {}

##
# Returns the children of this variable
# @return @LAS::Variable

sub getChildren {
    my $self = shift;
    if (scalar @{$self->{children}} == 0){
        my $children = $self->{element}->getChildNodes;
        my $nodes = new LAS::ElementIterator($children);
        while ($nodes->hasMore){
            my $node = $nodes->next;
            $node = $self->getParser->resolveLink($node);
            next if $node->getTagName eq "properties";
            my $var = $self->{config}->getElementToContainer($node);
            if (!$var){
                $self->{parent}->getChildren; # Need to initialize
                $var = $self->{config}->getElementToContainer($node);
                die "Internal: Can't find container for node ",
                     $node->getTagName
                         if ! $var;
            }
            push(@{$self->{children}}, $var);
        }
    }
    return @{$self->{children}};
}

##
# A <i>dataset</i> is a container for a group of LAS variables. 

package LAS::Dataset;
@LAS::Dataset::ISA = qw(LAS::Container);
use Carp qw(carp croak);

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{instances} = $self->getConfig->getInstances('dataset');
    $self->addToUnique;
    $self->{institution} = &LAS::processInstitution($self->getConfig,$self->{element});
    if (! $self->{institution}){
        $self->{institution} = $self->getConfig->getInstitution;
    }
    return $self;
}


##
# Returns the LAS::Institution object associated with this variable
# @return LAS::Institiution
sub getInstitution {
    my $self = shift;
    my $inst = $self->{institution};
    return $inst;
}

##
# Returns all of the child variables of a dataset that are <i>composite</i>
# (LAS::CompVar)
# @return @LAS::CompVar

sub getComposite() {
    my $self = shift;
    die "getComposite called before getChildren"
        if ! defined $self->{composite};
    return @{$self->{composite}};
}

##
# Returns all child variables of a dataset (LAS::CompVar and LAS::Variable)
#

sub getVariables {
    return ($_[0]->getChildren, $_[0]->getComposite);
}


##
# @private
sub _initComposite {
    my $self = shift;
    return if defined $self->{composite} && scalar @{$self->{composite}} > 0;
    $self->{composite} = [];
    my $composite = $self->{element}->getElementsByTagName("composite", 0);
    my $length = $composite->getLength;
    croak "Only one composite element allowed" if $length > 1;
    if ($length > 0){
        my $compChildren = $composite->item(0)->getChildNodes;
        my $iter = new LAS::ElementIterator($compChildren);
        my $composite = $self->{composite};
        while ($iter->hasMore){
            my $e = $iter->next;
            push(@{$composite}, new LAS::CompVar($self->getConfig, $e, $self));
        }
    }
}

# Strange bug in 5.6 where sort won't work with the old method of 
# using the global variables $a, $b but will work with the prototyped form
# that used $_[0], etc. Following is a hack for this -- also, method
# has to be in same package as sort call for 5.005 to work (sigh).
##
# @private
sub mysort($$) {
    if (defined($a)){           # Perl 5.005
        return lc($a->{longName}) cmp lc($b->{longName});
    } else {
        return lc($_[0]->{longName}) cmp lc($_[1]->{longName});
    }
        
}

##
# @private
sub getVarCount {
    my $self = shift;
    return $self->{count} if defined $self->{count};
    my $variables = $self->{element}->getElementsByTagName("variables", 0);
    my $length = $variables->getLength;
    croak "Only one variables element allowed" if $length > 1;
    if ($length > 0){
        my @varChildren = $variables->item(0)->getChildNodes;
        $self->{count} = scalar @varChildren;
    } else {
        $self->{count} = 0;
    }

    my $composite = $self->{element}->getElementsByTagName("composite", 0);
    $length = $composite->getLength;
    croak "Only one composite element allowed" if $length > 1;
    if ($length > 0){
        my @compChildren = $composite->item(0)->getChildNodes;
        $self->{count} = $self->{count} + scalar @compChildren;
    }
    return $self->{count};
}

##
# Returns the children of this variable
# @return @LAS::Variable
sub getChildren {
    my $self = shift;
    my $children = $self->{children};
    if (scalar @{$children} == 0){
        my $variables = $self->{element}->getElementsByTagName("variables", 0);
        my $length = $variables->getLength;
        croak "Only one variables element allowed" if $length > 1;
        if ($length > 0){
            my $varChildren = $variables->item(0)->getChildNodes;
            my $iter = new LAS::ElementIterator($varChildren);
            my $children = $self->{children};
            while ($iter->hasMore){
                my $e = $iter->next;
                push(@{$children}, new LAS::Variable($self->{config},
                                                     $e, $self));
            }
            @{$children} = sort mysort @{$children};
        }
        $self->_initComposite;
    }
    return @{$self->{children}};
}

##
# @private
sub _initialize {
    my $self = shift;
    $self->addURL;
}

#
# Add this axis to hash of axes associated with dataset
#
##
# @private
sub addAxisInstance($$) {
    my ($self,$axis) = @_;
    $self->{axisInstances} = {} if ! defined($self->{axisInstances});
    my $name = $axis->getFullPath;
    die "No fullpath for ", $axis->getName if ! $name;
    $self->{axisInstances}->{$name} = $axis;
}

##
# @private
sub addVariable {
    my ($self, $varName, $atts) = @_;
    my $newvar = $self->addElement("variables", "LAS::Variable", $varName, $atts,
                                   [$self]);
    return $newvar;
}


##
# Contains metadata about an argument in a LAS::Request object.<p>
package LAS::Arg;
@LAS::Arg::ISA = qw(LAS::Container);
use Carp qw(carp croak);

##
# @private
sub _initialize {
    my $self = shift;
    my $type = $self->{element}->getAttribute("type");
    if (! $type){
        croak "'arg' element missing 'type' attribute";
    }
    $self->{type} = $type;
}

##
# Returns the type of the argument
# @return string containing argument type

sub getType {
    my $self = shift;
    return $self->{type};
}

##
# Contains metadata about a LAS operation. A LAS operation is defined
# in a LAS XML configuration file with the <operation> tag. The
# children of a LAS operation are objects of type LAS::Arg

package LAS::Op;
@LAS::Op::ISA = qw(LAS::Container);
BEGIN {
    import Carp;
}

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{instances} = $self->getConfig->getInstances('op');
    my $e = $self->{element};
    my $url = $e->getAttribute("url");
    $url = $e->getParentNode->getAttribute("url") if ! $url;
    croak "No 'url' attribute specified for operation ", $self->getName
        if ! $url;
    $self->{url} = $url;
    $self->addToUnique;
    return $self;
}

##
# Returns the URL of this operation
# @return string containing URL

sub getURL {
    my $self = shift;
    return $self->{url};
}

##
# @private
sub _initialize {
    my $self = shift;
    my $args = $self->{element}->getElementsByTagName("arg");
    my $nodes = new LAS::ElementIterator($args);
    while ($nodes->hasMore){
        my $e = $nodes->next;
        push(@{$self->{children}}, new LAS::Arg($self->{config}, $e));
    }
}

##
# Encapsulates information in a LivePack manifest file
#

package LAS::PackageManifest;
@LAS::PackageManifest::ISA = qw(LAS::Container);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    $LAS::Config::Parser = my $parser = shift;
    my $rootElement = $parser->getRoot->getDocumentElement;
    die 'Root tag must be <packageinfo>'
        if $rootElement->getTagName ne 'packageinfo';
    push(@_, $rootElement);
    my $self = $class->SUPER::new(undef,@_);
    $self->{dtd} = $parser->getRoot->getDoctype;
    $self->{files} = [];
    bless $self, $class;

    return $self;
}

sub _initialize {
    my $self = shift;
    my @files = $self->{element}->getElementsByTagName("file", 0);
    my $children = $self->{files};
    foreach my $file (@files){
        my $name = $file->getAttribute('name');
        die "<file> missing name attribute in manifest.xml"
            if ! $name;
        push(@{$children}, $name);
    }
}

sub validateFiles {
    my ($self, $tmpdir) = @_;
    foreach my $file ($self->getFiles){
        my $fullFile = $tmpdir . "/$file";
        die "Package corrupted: missing file: $file" if ! -f $fullFile;
    }
        
}

sub getPackageDirName {
    my $self = shift;
    my $url = $self->getAttribute("url") or die "Missing url attribute";
    $url =~ s/^http:\/\///;
    $url =~ s/\//\_/g;
    $url;
}

sub getFiles {
    @{$_[0]->{files}};
}


##
# Contains info about an installed LAS package
#

package LAS::Package;
@LAS::Package::ISA = qw(LAS::Container);
##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    my $url = $self->getAttribute("base") or
        die "<package> missing base attribute";
    my @files = ();
    my @configs = $self->{element}->getElementsByTagName("config", 0);
    foreach my $config (@configs){
        my $name = $config->getAttribute("name") or
            die "<config> missing name attribute";
        $url =~ s/file://;
        $url =~ s:/+$::;
        push(@files, $url . "/$name");
    }
    $self->{files} = \@files;
}

##
# Returns the full path to the XML configuration file(s) for this package
# @return @files

sub getFiles {
    @{$_[0]->{files}};
}

##
# Return the manifest for this package
# @return LAS::PackageManifest object
#
sub getManifest {
    my ($self) = @_;
    my $base = $self->getAttribute("base");
    my $fname = $base . "/manifest.xml";
    die "Can't open package manifest files $fname" if ! -r $fname;
    my $parser = new LAS::Parser($fname);
    return new LAS::PackageManifest($parser);
}

##
# LAS packages are used to manage shared configuration
# data from different LAS server. This info is stored in an XML
# configuration file that is parsed by LAS::Parser object and
# passed to LAS::PackageConfig for further processing.
#
package LAS::PackageConfig;
@LAS::PackageConfig::ISA = qw(LAS::Container);

##
# Create a new package object
# @param $parser parser object (LAS::Parser)

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    $LAS::Config::Parser = my $parser = shift;
    my $rootElement = $parser->getRoot->getDocumentElement;
    die 'Root tag must be <laspackages>'
        if $rootElement->getTagName ne 'laspackages';
    push(@_, $rootElement);
    my $self = $class->SUPER::new(undef,@_);
    $self->{dtd} = $parser->getRoot->getDoctype;
    bless $self, $class;

    return $self;
}

sub _initialize {
    my $self = shift;
    my @packages = $self->{element}->getElementsByTagName("package", 0);
    my $children = $self->{children};
    foreach my $package (@packages){
        push(@{$children}, new LAS::Package($package));
    }
}

sub findPackageByName {
    my ($self,$name) = @_;
    foreach my $package (@{$self->{children}}){
        return $package if $package->getAttribute("base") eq $name;
    }
}

##
# A <i>config</i>uration object is the root object containing the contents of 
# a XML DOM tree in a XML configuration file.<p>
# The children of LAS::Config are of type LAS::Dataset

package LAS::Config;
@LAS::Config::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS;
$LAS::Config::Parser = undef;   # Hack to get Parser variable to _initialize

##
# Create a new configuration object
# @param $parser parser object (LAS::Parser)

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    $LAS::Config::Parser = my $parser = shift;
    my $rootElement = $parser->getRoot->getDocumentElement;
    die 'Root tag must be <lasdata>' if $rootElement->getTagName ne 'lasdata';
    push(@_, $rootElement);
    my $self = $class->SUPER::new(undef,@_);
    $self->{dtd} = $parser->getRoot->getDoctype;
    bless $self, $class;

    return $self;
}

##
# Get categorization info. Create category info if none found
#

sub getCategories {
    my $self = shift;
    my $multi = shift;
    my @elements = $self->{element}->getElementsByTagName("las_categories", 0);
    my $element;
    if ($#elements < 0){
        my $defaultCatalog = "<las_categories/>";
        if ($multi){
            $defaultCatalog = "<las_categories>\n";
            my $inst = $self->getInstitution;
            my $name = defined($inst) && defined($inst->getInstName) ?
                $inst->getInstName : "(No institution name)";
            $name = "Datasets from: $name";
            $defaultCatalog .= qq{<category name="$name">\n};
            foreach my $dset ($self->getChildren){
                my $dname = $dset->getLongName;
                my $doc = $dset->getAttribute("doc");
                $doc = "" if ! $doc;
                $defaultCatalog .= qq{<category name="$dname" doc="$doc">\n};
                $defaultCatalog .= qq{<filter action="apply-dataset" equals="$dname">};
                $defaultCatalog .= qq{</filter>};
                $defaultCatalog .= qq{</category>\n};
            }
            $defaultCatalog .= qq{</category>\n};
            $defaultCatalog .= "</las_categories>\n";
        }
        my $catParser = new XML::DOM::Parser(ErrorContext => 2);
        my $cat_doc = $catParser->parse($defaultCatalog);
        @elements = ($cat_doc->getDocumentElement);
        #There's only one in this case
        $element=$elements[0];
    } else {

       # Collect all categories from various <las_categories> elements in the XML.
       # Previously extra <las_categories> elements were ignored without error or warning.

       $element=$elements[0];

       if ( $#elements > 0 ) {
          foreach my $las_categories (@elements){
              my $cats = $las_categories->getChildNodes;
              my $iter = new LAS::ElementIterator($cats);
              while ($iter->hasMore){
                  my $e = $iter->next;
                  $element->appendChild($e);
              }
          }
       }
    }
    $element;
}

##
# Create an XML string representation of the LAS configuration hierarchy
# @return string containing XML for LAS configuration

sub toXML {
    my ($self,$noHeader) = @_;

    my $rval = '<?xml version=\'1.0\' ?>' . "\n";
    if ($noHeader) { $rval = ""; }
    $rval .= $self->{dtd}->toString if $self->{dtd};
    return $rval . $self->{element}->toString;
}

# Strange bug in 5.6 where sort won't work with the old method of 
# using the global variables $a, $b but will work with the prototyped form
# that used $_[0], etc. Following is a hack for this -- also, method
# has to be in same package as sort call for 5.005 to work (sigh).
##
# @private
sub mysort($$) {
    if (defined($a)){           # Perl 5.005
        return lc($a->getLongName) cmp lc($b->getLongName);
    } else {
        return lc($_[0]->getLongName) cmp lc($_[1]->getLongName);
    }
        
}
##
# @private
sub _initialize {
    my $self = shift;
    $self->{config} = $self;
    $self->{parser} = $LAS::Config::Parser;
    $self->{ops} = [];
    $self->{axisInstances} = {};
    $self->{variableInstances} = {};
    $self->{compVarInstances} = {};
    $self->{datasetInstances} = {};
    $self->{opInstances} = {};
    $self->{urlInstances} = {};
    $self->{elementToContainer} = {};
#
# Process all of the dataset definitions
#
    my @datasets = $self->{element}->getElementsByTagName("datasets", 0);
    foreach my $dataset (@datasets){
        my $dsets = $dataset->getChildNodes;
        my $iter = new LAS::ElementIterator($dsets);
        my $children = $self->{children};
        while ($iter->hasMore){
            my $e = $iter->next;
            push(@{$children}, new LAS::Dataset($self, $e));
        }
        @{$children} = sort mysort @{$children};
    }

#
# Process all of the operations definitions
#
    my @operations = $self->{element}->getElementsByTagName("operations", 0);
    croak "Only one 'operations' element allowed" if scalar @operations > 1;
    if (@operations){
        my $ops = $operations[0];
        $self->_initOperations($ops);
    }

    $self->{institution} = &LAS::processInstitution($self->getConfig,$self->{element});

}


##
# @private
sub getElementToContainer {
    my $self = shift;
    my $e = shift;
    return $self->{elementToContainer}->{$e};
}

##
# @private
sub setElementToContainer {
    my $self = shift;
    my $e = shift;
    my $obj = shift;
    $self->{elementToContainer}->{$e} = $obj;
}

##
# Returns the institution
# @return LAS::Institution

sub getInstitution {
    my $self = shift;
    my $inst = $self->{institution};
    return $inst;
}


##
# @private
sub _initOperations {
    my ($self, $parent) = @_;
    my $children = $parent->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $node = $iter->next;
        push(@{$self->{ops}}, new LAS::Op($self, $node));
    }
}
    
##
# @private
sub _initProperties {
    my ($self, $parent, $propHash) = @_;
    $self->_initPropertyHash($propHash, $parent);
}
    

##
# Gets all of the defined operations for this configuration
# @return @LAS::Op

sub getOps {
    my $self = shift;
    return @{$self->{ops}};
}

##
# @private
sub addDataset {
    my ($self, $dsetName, $atts) = @_;
    return $self->addElement("datasets", "LAS::Dataset", $dsetName, $atts);
}

##
# Contains a data range


# TODO - Validate  against href
package LAS::Range;
@LAS::Range::ISA = qw(LAS::Container);
use Carp qw(carp croak);

##
# @private
sub _initialize {
    my $self = shift;
    my $low = $self->getAttribute("low");
    my $hi = $self->getAttribute("high");
    if (!(defined($low) && defined($hi))){
        croak "'range' element must have both 'low' and 'high' attributes";
    }
    $self->{lo} = $low;
    $self->{hi} = $hi;
}

##
# Get the low value of a data range
# @return string containing low value

sub getLo {
    my $self = shift;
    return $self->{lo};
}

##
# Get the high value of a data range
# @return string containing low value
sub getHi {
    my $self = shift;
    return $self->{hi};
}

##
# @private
sub setLo {
    my ($self, $lo) = @_;
    $self->{lo} = $lo;
}

##
# @private
sub setHi {
    my ($self, $hi) = @_;
    $self->{hi} = $hi;
}

# TODO - Validate  against href

##
# Contains a 1D point
#
package LAS::Point;
@LAS::Point::ISA = qw(LAS::Container);
use Carp qw(carp croak);

##
# @private
sub _initialize {
    my $self = shift;
    my $value = $self->getAttribute("v");
    if (!$value){
        croak "'point' element must have 'v' attribute" if $value ne '0';
    }
    $self->{value} = $value;
}

##
# Returns the value of the point
# @return string containing point value
#

sub getValue {
    my $self = shift;
    return $self->{value};
}

##
# Returns the low value of the point. Same as getValue
# @return string containing point value
#
sub getLo {
    my $self = shift;
    return $self->{value};
}

##
# Returns the high value of the point. Same as getValue
# @return string containing point value
#
sub getHi {
    my $self = shift;
    return $self->{value};
}

##
# @private
sub setLo {
    my ($self, $lo) = @_;
    $self->{value} = $lo;
}

##
# @private
sub setHi {
    my ($self, $hi) = @_;
    $self->setLo($hi);
}

##
# Factory for generating constraint objects

package LAS::Constraint;
use Carp qw(carp croak);
%LAS::Constraint::OpHash = (lt => '<', le => '<=', eq => '=',
                            gt => '>', ge => '>=');

sub new {
    my $proto = shift;
    my ($req,$e) = @_;
    my $class = ref($proto) || $proto;
    my $type = $e->getAttribute('type');
    die "Missing type for <constraint>" if ! $type;
    if ($type eq 'variable'){
        return new LAS::Constraint::Variable(@_);
    } elsif ($type eq 'text'){
        return new LAS::Constraint::Text(@_);
    } elsif ($type eq 'textfield'){
        return new LAS::Constraint::TextField(@_);
    }
    die "Unknown type for <constraint>: ", $type;
    
}

##
# In-situ data constraint request of the LAS data server

package LAS::Constraint::Variable;
@LAS::Constraint::Variable::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS qw(println printlni);
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $req = shift;
    my $self = $class->SUPER::new($req->getConfig, @_);
}

##
# @private
sub _initialize {
    my $self = shift;
    my $childnodes = $self->{element}->getChildNodes;
    my $children = $self->{children};
    my $iter = new LAS::ElementIterator($childnodes);
    my $op = $self->getAttribute('op');
    die "Missing op attribute for <constraint>: $op"
        if ! $op;
    my $realOp = $LAS::Constraint::OpHash{$op};
    die "Unknown op type for <constraint>: $op"
        if ! $realOp;
    $self->{op} = $realOp;
    my ($var, $text);
    while($iter->hasMore){
        my $node = $iter->next;
        if ($node->getNodeName eq "link"){
            $var = $self->getParser->resolveLink($node);
        } elsif ($node->getNodeName eq "v"){
                                # Look for '#pcdata' attribute first, as this 
                                # might be from serialized RDBMS XML
            $text = $node->getAttribute('#pcdata');
            my $child = $node->getFirstChild;
            $text = $child->getNodeValue
                if $child && ! (defined($text) && $text ne "");
            $text =~ s/^\s+//g;
            $text =~ s/\s+$//g;
        }
    }
    die "No matching variable or missing variable for <constraint>"
        if !$var;
    $self->{value} = $text;
    $self->{variable} = $var;
}

sub getValue {
    return $_[0]->{value};
}

sub getVariable {
    return $_[0]->{variable};
}

sub getOp {
    return $_[0]->{op};
}

##
# In-situ data constraint request of the LAS data server

package LAS::Constraint::Text;
@LAS::Constraint::Text::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS qw(println printlni);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $req = shift;
    my $self = $class->SUPER::new($req->getConfig, @_);
    bless $self,$class;
}

##
# @private
sub _initialize {
    my $self = shift;
    $self->{values} = [];
    my $childnodes = $self->{element}->getChildNodes;
    my $children = $self->{children};
    my $iter = new LAS::ElementIterator($childnodes);
    while($iter->hasMore){
        my $node = $iter->next;
        if ($node->getNodeName eq "v"){
                                # Look for '#pcdata' attribute first, as this 
                                # might be from serialized RDBMS XML
            $text = $node->getAttribute('#pcdata');
            my $child = $node->getFirstChild;
            $text = $child->getNodeValue
                if $child && ! (defined($text) && $text ne "");
            $text =~ s/^\s+//g;
            $text =~ s/\s+$//g;
            push(@{$self->{values}}, $text);
        } else {
            die "Unknown child of <constraint type='text'> : ",$node->getNodeName;
        }
    }
}

sub getValues {
    return @{$_[0]->{values}};
}

##
# In-situ data constraint request of the LAS data server

package LAS::Constraint::TextField;
@LAS::Constraint::TextField::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS qw(println printlni);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $req = shift;
    my $self = $class->SUPER::new($req->getConfig, @_);
    bless $self,$class;
}

##
# @private
sub _initialize {
    my $self = shift;
    $self->{value} = $self->{element}->getAttribute("value");
}

sub getValue {
    return $_[0]->{value};
}

package LAS::Region;
@LAS::Region::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS qw(println printlni);

##
# @private
sub _initialize {
    my $self = shift;
    my $childnodes = $self->{element}->getChildNodes;
    my $children = $self->{children};
    my $iter = new LAS::ElementIterator($childnodes);
    while($iter->hasMore){
        my $node = $iter->next;
        if ($node->getNodeName eq "range"){
            push(@{$children}, new LAS::Range($self->{config}, $node));
        } elsif ($node->getNodeName eq "point"){
            push(@{$children}, new LAS::Point($self->{config}, $node));;
        }
    }
}

##
# @private
sub print{
    my $region = shift;
    $IndentLevel++;
    my @children = $region->getChildren;
    foreach (@children){
        my $class = ref($_);
        if ($class eq "LAS::Range"){
            printlni "Range: ",$_->getLo,':',$_->getHi;
        } else {
            printlni "Point: ",$_->getLo;
        }
    }
    $IndentLevel--;
}

package LAS::AnalysisAxis;
@LAS::AnalysisAxis::ISA = qw(LAS::Container);
##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
}

##
# @private
sub _initialize {
    $_[0]->requireAtts(qw(op lo hi));
    $_[0]->requireAttInList("type", qw(x y z t));
}

sub getOp {
    return $_[0]->getAttribute("op");
}

sub getLo {
    return $_[0]->getAttribute("lo");
}

sub getHi {
    return $_[0]->getAttribute("hi");
}

sub getType {
    return $_[0]->getAttribute("type");
}

package LAS::Analysis;
@LAS::Analysis::ISA = qw(LAS::Container);

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
}

##
# Returns the Variable Name as set in the LAS Analysis UI  
sub getLabel {
    $_[0]->getAttribute("label");
}

##
# returns the Analysis Variable Title or Variable Name
# if Variable Title does not exist
sub getAnalTitle {
    my $self = shift;
    if (defined ($self->{AnalTitle})){
        return $self->{AnalTitle};
    }else{
        $self->getAttribute("label");
    }
}

##
# Sets the Analysis Variable Title 
sub setAnalTitle {
    my $self = shift;
    $self->{AnalTitle} = shift;
}

##
# Given an axis type ('x'|'y'|'z'|'t') returns a label with
# the Analysis operation and range associated with that axis
# i.e. 'ave(x=-180.0:-120.0)'. if no axis of given type exists
# will return undefined
sub getAnalOpsLabel_byAxis {
    my $self = shift;
    my $type = shift;
    # TODO check type with die statement if type not x|y|z|t
    my $opsStr;
    foreach my $axis ($self->getChildren){
        my $anAxis = $axis->getType;
        if($anAxis eq $type){
            my ($anOp, $anLo,$anHi) = ($axis->getOp, $axis->getLo, $axis->getHi);
            $opsStr = qq{$anOp\($anAxis="$anLo":"$anHi"\)};
        }
    }
    $opsStr =~ s/\"//g;
    return $opsStr;
}


sub isLandMask {
    $_[0]->getAttribute("landmask");
}

sub isOceanMask {
    $_[0]->getAttribute("oceanmask");
}

##
# @private
sub _initialize {
    my $self = shift;
    $self->requireAtts(qw(label));
    my @children = $self->{element}->getElementsByTagName("axis", 0);
    die "<analysis> doesn't have any <axis> children" if $#children < 0;
    foreach my $child (@children){
        push(@{$self->{children}}, new LAS::AnalysisAxis($self->getConfig, $child));
    }
}

##
# A client requests data from a LAS server by assembling a XML request
# containing a link to an operation in a LAS configuration file
# and a set of arguments to the operation
# and passing the request to the server through a HTTP POST or GET. LAS parses
# this request and encapsulates it with a LAS::Request object.<p>
# A LAS::Request object contains links to objects in a XML configuration
# file. These links are not resolved until the <b>resolveLinks</b> method
# is invoked.

package LAS::Request;
@LAS::Request::ISA = qw(LAS::Container);
use Carp qw(carp croak);
my $RequestParser;
use LAS qw(println printlni);
use File::PathConvert qw(rel2abs);

##
# Create a new XML request object.
# @param $parser parser object (LAS::Parser)

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $parser = shift;
    $RequestParser = $parser;
    push(@_, $parser->getRoot->getDocumentElement);
    my $self = $class->SUPER::new(undef,@_);
    bless $self, $class;
    return $self;
}

##
# @private

sub getConfigAndParser {
    my ($self,$e) = @_;
    my $config = $self->getConfig;
    my $parser = $self->getParser;
#
# Arguments may have references to XML files other than the 
# one specified in the <lasrequest> tag. Need to resolve these if
# present.
# TODO -- See if the href has already been parsed
    my $href = $e->getAttribute('href');
    if ($href){
        my $package = $e->getAttribute("package");
        my $url = new URI::URL($self->getAttribute("href"));
        if ($url->scheme() ne "file"){
            croak qq/Only 'file' href protocol supported (for now)/;
        }
        my $path = $url->epath;
        my $proot = $self->getPackageRoot(0, $package);
        if ($proot){
            $path = "$proot/$path";
        }
        
        if (! -f $path){
            croak "Can't open XML config file '$path'";
        }
        $parser = new LAS::Parser($path, 0, $LAS::ParserClass);
        $config = new LAS::Config($parser);
    }
    return ($config,$parser);
}

##
# @private
sub _addArgRef {
    my ($self, $e) = @_;
    my $children = $self->{children};
    my $name = $e->getNodeName;
    if ($name eq "link"){
        last if ! $self->getParser;
        my ($config,$parser) = $self->getConfigAndParser($e);

#       die $self->getParser->getURL;
#       die $e->getAttribute('href');
        my $node = $parser->resolveLink($e);

        if (! $node){
            return;
        }
        my $id = &LAS::createFullPath($node);
        my %instances = $config->getInstances('variable');
        my $var = $instances{$id};
        if (!$var){
#
# Special hack for variables -- force initialization which is now deferred
# by calling getChildren on dataset
#
            my $dsetname = $id;
            $dsetname =~ s:/[^/]+/[^/]+$::;
            my $dset = $config->getInstances('dataset')->{$dsetname};
            die "Can't find variable named $id or dataset named $dsetname"
                if !$dset;
            $dset->getChildren;
            %instances = $config->getInstances('variable');
            $var = $instances{$id};
            die "Can't find variable with element name $id" if ! $var;
        }
# Clone this variable
        $var = $var->clone;


# Merge any link properties with variable properties
        my @linkProperties = $e->getElementsByTagName("properties", 0);
        die "Multiple link properties" if scalar @linkProperties > 1;
        my $linkHash = {};
        if (scalar @linkProperties > 0){
            $self->_initPropertyHash($linkHash, $linkProperties[0]);
        }
        $var->{properties} = &LAS::mergeProperties($var->{properties},
                                                   $linkHash);

# Now merge all the 'product_server' properties
        $linkHash = $var->{properties};
        $var->{properties} = &LAS::mergeProperties($linkHash,
                                            scalar $config->getProperties('product_server'),
                                            scalar $var->getDataset->getProperties('product_server'),
                                            scalar $var->getProperties('product_server'),
                                            scalar $self->getProperties('product_server'));

        push(@{$children}, $var);

# TODO: The useCache property should be moved from 
# TODO: LAS::Request->{useCache} to
# TODO: LAS::Request->{properties}->{product_server}->{useCache}

        # The 'useCache' property is set during LAS::Request::_initialize()
        # based on the value of the 'useCache' attribute to the <lasRequest>
        # XML. If, and only if, caching is enabled at the LAS Request level
        # will the dataset configuration file XML be checked to see if
        # caching is turned off at the dataset/variable level.
        #
        if ($self->{useCache}) {
          my $useCache = $var->{properties}->{use_cache};
          if ($useCache && lc($useCache) eq "false"){
            $self->{useCache} = 0;
            &LAS::Server::debug("CACHE: Disabled in configuration file.\n");
          } else {
            &LAS::Server::debug("CACHE: Enabled for this request.\n");
          }
        } else {
          &LAS::Server::debug("CACHE: Disabled in <lasRequest>.\n");
        }

# The 'ui_timeout' and 'ps_timeout' properties are used in the 
# LAS::Server::HTTP->run() method to determine 1) how long to wait for
# product generation before sending back a 'conversion to batch mode' 
# response and 2) how long to wait before killing the request as a
# runaway process.

        $self->{properties}->{product_server}->{ui_timeout} = $var->{properties}->{ui_timeout};
        $self->{properties}->{product_server}->{ps_timeout} = $var->{properties}->{ps_timeout};

# Debug lines

        if ($LAS::DEBUG == 1) {
            my $dsetXML = $var->getDataset->toXML();
            &LAS::Server::debug("Inside _addArgRef: Dset XML ---\n$dsetXML\n\n");
            my $varXML = $var->toXML();
            &LAS::Server::debug("Inside _addArgRef: Var XML ---\n$varXML\n\n");
        }

# See if any analysis needs to be performed on variable
        my @analysisElements = $e->getElementsByTagName("analysis");
        die "Only one analysis element allowed per variable"
            if $#analysisElements > 0;
        if ($#analysisElements == 0){
            $var->{analysis} = new LAS::Analysis($self->{config},
                                                 $analysisElements[0]);
        }
    } elsif ($name eq 'constraint'){
        push(@{$self->{constraints}}, new LAS::Constraint($self,$e));
    }
}

##
# @private
sub _initialize {
    my $self = shift;
    my $unprocessed = $self->{unprocessed} = [];
    my @args = $self->{element}->getElementsByTagName("args");
    my $children = $self->{children};

    # The 'useCache' attribute to the <lasRequest> XML tag determines whether
    # caching is enabled or not.  It is important to be able to disable caching
    # at the request level so that test scripts can be guaranteed their request
    # will generate a brand new product.
    #
    # If caching is not disabled at this level, LAS::Request::_addArgRef() will
    # check properties in the dataset configuration files to see whether caching
    # is disabled on a per dataset/variable bassis using the following XML:
    #
    # <properties>
    #  <product_server>
    #   <use_cache>false</use_cache>
    #  </product_server>
    # </properties>

    my $useCache = $self->getAttribute("useCache");
    if ($useCache && lc($useCache) eq "false"){
      $self->{useCache} = 0;
    } else {
      $self->{useCache} = 1;
    }

    foreach my $arg (@args){
        my $argchildren = $arg->getChildNodes;
        my $iter = new LAS::ElementIterator($argchildren);
        while ($iter->hasMore){
            my $e = $iter->next;
            my $name = $e->getNodeName;
            if ($name eq "region"){
                push(@{$children}, new LAS::Region($self->{config},$e));
            } else {
                push(@{$unprocessed}, $e); # Deferred
            }
        }
    }
#
# Process any packages
#
    if (-r 'packages.xml'){
        my $parser = new LAS::Parser('packages.xml');
        my $pconfig = $self->{packageConfig} = new LAS::PackageConfig($parser);
        
    }
}

##
# Get constraint list (if any)  associated with this request
# @return Array of LAS::Constraint
sub getConstraints {
    @{$_[0]->{constraints}};
}


##
# Get any package config info 
# @return LAS::PackageConfig object (if packages are defined for this config)
#
sub getPackageConfig {
    $_[0]->{packageConfig};
}

sub getPackageRoot {
    my ($self, $useRel, $pname) = @_;
    $useRel = 0 if ! defined($useRel);
    my $pconfig = $self->getPackageConfig;
    my $rval= "";
    if ($pconfig){
        $pname = $self->getAttribute("package") if ! defined($pname);
        return $rval if ! $pname;
        my $pcwd = rel2abs('..');
        $fullpname = "$pcwd/packages/$pname";
        my $package = $pconfig->findPackageByName($fullpname);
        die "Requested package: $fullpname doesn't seem to be installed"
            if ! defined $package;
        if ($useRel){
            $rval = "packages/$pname";
        } else {
            $rval = $fullpname;
        }
    }
    return $rval;
}

sub getCustomInclude {
    my ($self, $isRel) = @_;
    $isRel = 0 if ! defined($isRel);
    my $pconfig = $self->getPackageConfig;
    my $rval= $LAS::Server::Config{custom_include};
    if ($pconfig){
        my $pname = $self->getAttribute("package");
        return $rval if ! $pname;
        my $pcwd = rel2abs('..');
        $fullpname = "$pcwd/packages/$pname";
        my $package = $pconfig->findPackageByName($fullpname);
        die "Requested package: $fullpname doesn't seem to be installed"
            if ! defined $package;
        my $manifest = $package->getManifest;
        die "Can't get manifest for package: $fullpname"
            if ! $manifest;
        my $loc = $manifest->getAttribute("custom");
        if ($isRel){
            $rval = $loc;
        } else {
            $rval = "packages/$pname/server/$loc";
        }
    }
    return $rval;
}

##
# Indicates if server should use cached value for this request
# @return 1 if true

sub useCache {
    return $_[0]->{useCache};
}

##
# Parse the specified XML configuration file and resolve all links
# from the request object to the configuration file.

sub resolveLinks {
    my $self = shift;
    $self->{config} = shift;    # Optional argument if parsing done
    if (! $self->{config}){
#
# Get configuration file
#
        my $url = new URI::URL($self->getAttribute("href"));
        if (! $url || $url->full_path eq ""){
            croak qq/All requests must have an 'href' attribute/;
        }
        if ($url->scheme() ne "file"){
            croak qq/Only 'file' href protocol supported (for now)/;
        }
        my $path = $url->epath;
        my $proot = $self->getPackageRoot;
        if ($proot){
            $path = "$proot/$path";
        }
                
        if (! -f $path){
            croak "Can't open XML config file '$path'";
        }
#
# Parse config file if not set
#
        my $parser = new LAS::Parser($path, 0, $LAS::ParserClass);
        $self->{config} = new LAS::Config($parser);
    }

#
# Find the referenced operation
# Should be one and only one link, and that is to an operation
#
    my @links = $self->{element}->getElementsByTagName("link");
    if (scalar @links == 0){
        croak "Invalid number of links in lasRequest: ", scalar @links;
    }
    my $node = $self->getParser->resolveLink($links[0]);
    my $name = &LAS::createFullPath($node);
    my %ops = $self->getConfig->getInstances('op');
    my $op = $ops{$name};
    if (! $op ){
        croak "Can't find '$name' operation definition";
    }
    $self->{op} = $op;

    foreach my $e (@{$self->{unprocessed}}){
        $self->_addArgRef($e);
    }
    $self->{unprocessed} = undef;
}

##
# Get the path to the package associated with the request(if any)
# @return relative path to package directory
sub getPackagePath {
    my $self = shift;
    my $pack = $self->getAttribute("package");
    return "" if ! $pack;
    return "packages/$pack";
}

##
# Gets the LAS::Config object associated with this request
# @return LAS::Config or undef if resolveLinks has not been invoked

sub getConfig {
    my $self = shift;
    return $self->{config};
}

##
# Gets the LAS::Op object associated with this request
# @return LAS::Op or undef if resolveLinks has not been invoked
sub getOp {
    my $self = shift;
    return $self->{op};
}

##
# Dump debug info about the request object to STDOUT
sub print {
    my $req = shift;
    println "Request properties:";
    my %props = $req->getProperties('ferret');
    $IndentLevel++;
    foreach (keys %props){
        printlni "Property: $_ = $props{$_}";
    }
    
    $IndentLevel--;
    println "Operation properties:";
    %props = $req->getOp->getProperties('ferret');
    $IndentLevel++;
    foreach (keys %props){
        my $value = $props{$_};
        if (defined $value){
            printlni "Property: $_ = $props{$_}";
        } else {
            printlni "Property: $_ = (null)";
        }
    }

    $IndentLevel--;
    println "Arguments:";
    my @children = $req->getChildren;
    foreach (@children){
        my $class = ref($_);
        if ($class eq "LAS::Variable"){
            $_->print;
        } elsif ($class eq "LAS::Region"){
            $_->print;
        }
    }
    
}

##
# Encapsulates LAS analysis operations
#

##
# LAS allows dataset providers to provide information about
# contributors to the dataset .  Each contributor has a name, a role and a Web page link
# that is to be associated with a given dataset. This information is
# contained in the LAS::Contributor object.

package LAS::Contributor;
@LAS::Contributor::ISA = qw(LAS::Container);

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
}

##
# @private
sub _initialize {
    my $self = shift;
    my $name = $self->getAttribute("name");
    die "Contributor tag is missing name attribute"
        if ! $name;
    $self->{name} = $name;
    my $urlStr = $self->getAttribute("url");
    if ($urlStr){
        $self->{url} = $urlStr;
    }
    my $roleStr = $self->getAttribute("role");
    if ($roleStr){
        $self->{role} = $roleStr;
    }
}

##
# Gets the URL of the contributor Web page
# @return string containing URL

sub getURL {
    my $self = shift;
    return new URI::URL($self->{url});
}

##
# Gets the name of the contributor
# @return string containing institution name

sub getName {
    my $self = shift;
    return $self->{name};
}

##
# Gets the role of the contributor
# @return string containing institution role

sub getRole {
    my $self = shift;
    return $self->{role};
}

##
# LAS allows dataset providers to provide a name and a Web page link
# that is to be associated with a given dataset. This information is
# contained in the LAS::Institution object.

package LAS::Institution;
@LAS::Institution::ISA = qw(LAS::Container);

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
}

##
# @private
sub _initialize {
    my $self = shift;
    my $name = $self->getAttribute("name");
    die "Institution tag is missing name attribute"
        if ! $name;
    $self->{institution} = $name;
    my $urlStr = $self->getAttribute("url");
    if ($urlStr){
        $self->{url} = $urlStr;
    }
}

##
# Gets the URL of the instituation Web page
# @return string containing URL

sub getURL {
    my $self = shift;
    return new URI::URL($self->{url});
}

##
# Gets the name of the institution
# @return string containing institution name

sub getInstName {
    my $self = shift;
    return $self->{institution};
}

##
# An browser contains information a particular browser.<br>

package LAS::Browser;
@LAS::Browser::ISA = qw(LAS::Container);
BEGIN {
    import Carp;
    use vars qw($Instances);
}

##
# @private
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self, $class;
    $self->{instances} = $self->getConfig->getInstances('las_browsers');
    $self->addToUnique;
    return $self;
}

##
# @private
sub _initialize {
    my $self = shift;
    my $children = $self->{element}->getElementsByTagName("browser");
    if ($children->getLength > 0){
        $self->_initBrowser($children);
        return;
    } 
        
}

##
# Returns the agent string of the browser
# @return agent string
sub getAgent {
    my $self = shift;
    return $self->{agent};
}

##
# Returns the applet boolean (true if works with browser)
# @return applet boolean
sub getApplet {
    my $self = shift;
    return $self->{applet};
}

#
# Serialize an object and associated package variables
# Deprecated (replaced by RDMBS serialization of XML)
#
##
# @private
package LAS::Serializer;
use vars qw($sym @sym %sym);
use Carp;

sub new {
    my $class = shift;
    my $obj = shift;
    my $self = {
        packageName => ref($obj) || $obj,
        object => $obj,
        packageVars => {}
    };
    bless $self, $class;
}



sub serialize {
    my ($self, $filename) = (@_);
    my $evalStr = '%' . $self->{packageName} . '::';
    my %pref = eval($evalStr);
    my $pvars = $self->{packageVars};
    foreach my $symname (keys %pref){
        next if $symname eq 'SUPER::';
        local *sym = $pref{$symname};
        if (defined $sym){
            $pvars->{$symname} = \$sym;
        } elsif (defined @sym){
            $pvars->{$symname} = \@sym;
        } elsif (defined %sym){
            $pvars->{$symname} = \%sym;
        } 
    }
    nstore($self, $filename);
}

sub deserialize {
    my ($self, $filename) = @_;
    $self = retrieve($filename);
    my $pvars = $self->{packageVars};
    my $evalStr = '\%' . $self->{packageName} . '::';
    my $pref = eval($evalStr);
    foreach my $symname (keys %{$pvars}){
        my $tmp = $pref->{$symname};
        if ($tmp){
            local *sym = $tmp;
            my $invar = $pvars->{$symname};
            my $refval = ref($invar);
            if ($refval eq 'HASH'){
                %sym = %{$invar};
            } elsif ($refval eq 'ARRAY'){
                @sym = @{$invar};
            } elsif ($refval eq 'SCALAR'){
                $sym = ${$invar};
            } elsif ($refval eq 'REF'){
                $sym = $$invar;   
            } else {
                die "Can't deserialize ", $self->{packageName},'::',$symname,
            ": deserialized package variables must be scalar, ref, or hash";
            } 
        }
    }
    return $self->{object};
}

##
# @private
package LAS::UI::Debug;
use Exporter ();
@LAS::UI::Debug::ISA = qw(Exporter);

@LAS::UI::EXPORT = qw(debug);

$LAS::UI::Debug::self = {};

sub new {
    my $name = $0;
    $name =~ s/[\.\/]/_/g;
    my $debug_file = "log/debug_" . $name;
    my $self = {};
    open(DEBUGFILE, ">>$debug_file") ||
        die "Can't create debug file $debug_file (protection problem?)";
    $self->{file} = \*DEBUGFILE;
    my $last = select DEBUGFILE;
    $| = 1;
    select $last;
    bless $self;
}

sub debug {
    my $self = shift;
    print {$self->{file}} @_, "\n";
}

sub dumpCGI {
    my ($self, $cgi) = @_;
    $self->debug('[' . scalar(localtime) . ']', $0);
    $self->debug("CGI params:");
    my @params = $cgi->param;
    foreach my $param (@params){
        $self->debug("\t", $param, ':', $cgi->param($param));
    }
    $self->debug("Cookies:");
    my %cookies = fetch CGI::Cookie;
    foreach my $cookie (keys %cookies){
        $self->debug("\t", $cookie, ':', $cookies{$cookie}->value);
    }
    $self->debug('[' . scalar(localtime) . ']', $0);
}

##
# @private
package LAS::UI;
@LAS::UI::ISA = qw(LAS::Container);
use Carp qw(carp croak);
use LAS qw(println printlni);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $parser = shift;
    push(@_, $parser->getRoot->getDocumentElement);
    my $self = $class->SUPER::new(undef, @_);
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    $self->{urls} = [];
    my @urls = $self->{element}->getElementsByTagName("url");
    my $children = $self->{children};
    foreach my $url (@urls){
        my @urlchildren = $url->getChildNodes;
        foreach my $urlchild (@urlchildren){
            my $value = $urlchild->getNodeValue;
            $value =~ s/^\s*//g;
            $value =~ s/\s*$//g;
            push(@{$self->{urls}}, $value);
        }
    }
}

sub getURLs {
    my $self = shift;
    return @{$self->{urls}};
}



# Package method to get a list of LAS::Configs for the UI

%LAS::UI::ConfigHash = ();
$LAS::UI::Base = ();
use LASDB;
sub getConfigs {
    my $parser = new LAS::Parser('config.xml');
    my $uiInfo = new LAS::UI($parser);
    $LAS::UI::Base = $uiInfo->getAttribute('base');
    my @urls = $uiInfo->getURLs;
    my @configs;
    foreach my $url (@urls){
        my $fname = $url;
        die 'Non-file URLS are not supported' if $fname !~ /^file:/;
        $fname =~ s/^file://;
        my $config = $LAS::UI::ConfigHash{$fname};
        if (!$config){
            my $parser = new LAS::Parser($fname, 0, $LAS::ParserClass);
            $config = new LAS::Config($parser);
            $LAS::UI::ConfigHash{$fname} = $config;
        }
        push(@configs, $config);
    }
    return \@configs;
}

sub getBase {
    if (! $LAS::UI::Base){
        my $parser = new LAS::Parser('config.xml');
        my $uiInfo = new LAS::UI($parser);
        $LAS::UI::Base = $uiInfo->getAttribute('base');
    }
    $LAS::UI::Base =~ s/\/+$//;
    $LAS::UI::Base;
}


1;
