# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution
# TODO -- Validate XML. This includes items such as checking for variables
#         or datasets with same name
# TODO -- Make sure element name/path and attribute name/value not too long for
#         database
# TODO -- Eliminate validateAxes from LASTemplate.pm and mode axis check
#         for both X and Y axis is moved here
# $Id$

package LASDB::Container;	# Predeclaration
package LASDB::DOM::NodeList; # Predeclaration
package LASDB::DOM::NamedNodeMap; # Predeclaration
package LASDB;
use Cwd;
use Digest::MD5  qw(md5 md5_hex md5_base64);
use URI::URL;

$LASDB::VERSION = "0.5";
$LASDB::DEBUG_DB = 0;
$LASDB::OID = 1;		# Counter for object ids
$LASDB::PCDATA_NAME = '#pcdata'; # Map PC data to this attribute name in db
$LASDB::Debug = undef;
$LASDB::Debug = new LAS::UI::Debug if $LASDB::DEBUG_DB;

sub debug {
    if ($LASDB::DEBUG_DB){
	$LASDB::Debug->debug(@_);
    }
}

sub getOID {
    return $LASDB::OID++;
}

sub getHash {
    my $data = shift;
    if (ref($data) eq "URI::URL"){
	$data = $data->as_string;
    }
    return md5_base64($data);
}

sub getURL {
    my $infile = shift;
    my $url = new URI::URL($infile);
    $url->scheme('file') if ! defined($url->scheme);
    if ($url->scheme ne 'file'){
	die "Bad URL $url: only file URLs supported";
    }
    my $file = $url->path;
    if ($file !~ /^\//){
	my $dir = cwd();
	$file = $dir . "/$file";
	$url->path($file);
    }
    return $url;
}

##
# Low level driver for the MySQL database.
#
package LASDB::Utility::MySQL;
use DBI;

##
# Creates a new LASDB::Utility::MySQL object
#
# @param $db database name
# @param $login 
# @param $password
# @param $host
# @param $useCache turns on/off database caching
sub new {
    my ($class, $db, $login, $password, $host, $useCache) = @_;
    $useCache = 1 if ! defined($useCache);
    my $self = {
	cache => {},
	useCache => $useCache
    };
    $host = "localhost" if !$host;
    my $dburl = 'DBI:mysql:' . $db . ":$host";
    my $dbh = $self->{dbh} =
	DBI->connect($dburl, $login, $password,
		     {RaiseError => 1, PrintError=>1});

    bless $self, $class;
}

##
# Map a URL to the appropriate database in MySQL
# @param $file URL of LAS configuration file (las.xml)
# @return string containing database name
#
sub mapURL {
    my ($self, $file) = @_;
    my $dbh = $self->{dbh};
    my $hash = &LASDB::getHash($file);
    $dbh->do(qq(use las));
    my @results =
	$dbh->selectrow_array(qq(SELECT dbase FROM map WHERE urlid = '$hash'));
    die "Database error: No match for URL $file" if $#results < 0;
    $dbh->do(qq(use $results[0]));
    $results[0];
}

##
# Execuate a prepared statement
sub preparedRows {
    my ($self, $id, @params) = @_;
    my $sth = $self->{$id};
    die "Can't find prepared statement '$id'" if ! $sth;
    $sth->execute(@params);
    return $sth->fetchall_arrayref;
}

##
# Create and send a query as determined from the arguments
# in the form
# <pre>
#   SELECT $what FROM $source WHERE $where
# </pre>
# If called in a list context, this method returns the first
# row of data from the statement. If called in a scalar
# context, it returns the first field of the first row.
#
# @param $what
# @param $source
# @param $where
#
sub selectRow {
    my ($self, $what, $source, $where) = @_;
    my $statement;
    if (defined($where)){
	$statement = qq{SELECT $what FROM $source WHERE $where};
    } else {
	$statement = qq{SELECT $what FROM $source};
    }
    if ($self->{useCache}){
	my $cacheValue = $self->{cache}->{$statement};
	if ($cacheValue){
	    &LASDB::debug("selectRow: $statement (cached)");
	    return @{$cacheValue};
	}
    }
    &LASDB::debug("selectRow: $statement");

    my @values = $self->{dbh}->selectrow_array($statement);
    $self->{cache}->{$statement} = \@values if $self->{useCache};
    return @values;
}

##
# Delete rows with
# <pre>
#   DELETE FROM $source WHERE $where
# </pre>
#
# @param $source
# @param $where
sub deleteRows {
    my ($self, $source, $where) = @_;
    $self->{dbh}->do(qq{DELETE FROM $source WHERE $where});
}

##
# Returns the last  ID used in the AUTO_INCREMENT column of a table
# In LAS tables this will be 'oid'.  From the MySQL documentation:
# <block_quote>
#   When a new AUTO_INCREMENT value has been generated, you can also obtain
#   it by executing a SELECT LAST_INSERT_ID() statement mysql_query() and 
#   retrieving the value from the result set returned by the statement. 
# </block_quote>
#
# @return ID
sub lastInserted {
    my ($self) = @_;
    my @values = $self->{dbh}->selectrow_array(qq{SELECT LAST_INSERT_ID()});
    return $values[0];
}

##
# Create and send a query as determined from the arguments
# in the form
# <pre>
#   SELECT $what FROM $source WHERE $where
# </pre>
# This method returns a reference to an array containing a 
# reference to an array for each row of data fetched.
#
#
# @param $what
# @param $source
# @param $where
#
sub selectRows {
    my ($self, $what, $source, $where) = @_;
    my $statement;
    if (defined($where)){
	$statement = qq{SELECT $what FROM $source WHERE $where};
    } else {
	$statement = qq{SELECT $what FROM $source};
    }
    my $cacheValue = $self->{cache}->{$statement};
    if ($self->{useCache}){
	if ($cacheValue){
	    &LASDB::debug("selectRows: $statement (cached)");
	    return $cacheValue;
	}
    }
    &LASDB::debug("selectRows: $statement");

    $cacheValue = $self->{dbh}->selectall_arrayref($statement);
    $self->{cache}->{$statement} = $cacheValue if $self->{useCache};
    return $cacheValue;
}

##
# Drops a table with
# <pre>
#   DROP TABLE IF EXISTS $table
# </pre>
# @param $table table name
sub dropTable {
    my ($self, $table) = @_;
    $self->{dbh}->do(qq{DROP TABLE IF EXISTS $table});
}

##
# Check if given table exists with
# <pre>
#   SHOW TABLES LIKE '$table'
# </pre>
# @param $table
# @return 0 if table exists, non-zero otherwise
sub tableExists {
    my ($self, $table) = @_;
    my $c = $self->{dbh}->prepare(qq(SHOW TABLES LIKE '$table'));
    $c->execute;
    return $c->rows;
}

##
# Returns the size of a given table
#
# @param $table
# @return size
sub tableSize {
    my ($self, $table) = @_;
    my @values = $self->selectRow('count(*)', $table);
    return $values[0];
}

##
# Check if a given column exists with 
# <pre>
#   SHOW COLUMNS FROM $table LIKE '$column'
# </pre>
# @param $table 
# @param $column
# @return 0 if no such columns are found, non-zero otherwise
sub columnExists {
    my ($self, $table, $column) = @_;
    my $c = $self->{dbh}->prepare(qq(SHOW COLUMNS FROM $table LIKE '$column'));
    $c->execute;
    return $c->rows;
}

##
# Check if given database exists with
# <pre>
#   SHOW DATABASES LIKE '$db'
# </pre>
# @param $db
# @return 0 if the database is not found, non-zero otherwise
sub dbExists {
    my ($self, $db) = @_;
    my $c = $self->{dbh}->prepare(qq(SHOW DATABASES LIKE '$db'));
    $c->execute;
    return $c->rows;
}

sub DESTROY {
    my $self = shift;
    $self->{dbh}->disconnect if $self->{dbh};
}

##
# Copy all of the tables (except for Confg) from one LAS
# database to another.  This is called in genLas.pl when
# the '-b' [background] option is given.  Populating a 
# temporary database first and then copying all of the 
# tables over reduces LAS downtime at sites that have
# frequent updates.
# @param $temp_db temporary database name
# @param $orig_db original database name
sub swapTempTables {
    my ($self, $temp_db, $orig_db) = @_;

# First, drop all of the tables (except Config) from $orig_db

    my $dbh = $self->{dbh};
    $dbh->do(qq(USE $orig_db));

    $self->dropTable('AnalysisWidgetItem');
    $self->dropTable('Axis');
    $self->dropTable('AxisVariableJoin');
    $self->dropTable('AxisWidgetItems');
    $self->dropTable('AxisWidgets');
    $self->dropTable('Browser');
    $self->dropTable('Category');
    $self->dropTable('ConstraintWidget');
    $self->dropTable('ConstraintWidgetItem');
    $self->dropTable('Constraints');
    $self->dropTable('Contributor');
    $self->dropTable('DerivedAxis');
    $self->dropTable('DerivedCategory');
    $self->dropTable('DerivedVariableInfo');
    $self->dropTable('Images');
    $self->dropTable('Institution');
    $self->dropTable('LiveMap');
    $self->dropTable('LiveMapRegionMenu');
    $self->dropTable('MetaData');
    $self->dropTable('Op');
    $self->dropTable('Options');
    $self->dropTable('OptionsWidget');
    $self->dropTable('OptionsWidgetItem');
    $self->dropTable('PathIndex');
    $self->dropTable('UI');
    $self->dropTable('VariableInfo');
    $self->dropTable('View');
    $self->dropTable('attributes');
    $self->dropTable('children');
    $self->dropTable('elements');

# Then, copy all of the tables (except Config) from $temp_db to $orig_db

    $dbh->do(qq{RENAME TABLE $temp_db.AnalysisWidgetItem TO $orig_db.AnalysisWidgetItem,\
                             $temp_db.Axis TO $orig_db.Axis,\
                             $temp_db.AxisVariableJoin TO $orig_db.AxisVariableJoin,\
                             $temp_db.AxisWidgetItems TO $orig_db.AxisWidgetItems,\
                             $temp_db.AxisWidgets TO $orig_db.AxisWidgets,\
                             $temp_db.Browser TO $orig_db.Browser,\
                             $temp_db.Category TO $orig_db.Category,\
                             $temp_db.ConstraintWidget TO $orig_db.ConstraintWidget,\
                             $temp_db.ConstraintWidgetItem TO $orig_db.ConstraintWidgetItem,\
                             $temp_db.Constraints TO $orig_db.Constraints,\
                             $temp_db.Contributor TO $orig_db.Contributor,\
                             $temp_db.DerivedAxis TO $orig_db.DerivedAxis,\
                             $temp_db.DerivedCategory TO $orig_db.DerivedCategory,\
                             $temp_db.DerivedVariableInfo TO $orig_db.DerivedVariableInfo,\
                             $temp_db.Images TO $orig_db.Images,\
                             $temp_db.Institution TO $orig_db.Institution,\
                             $temp_db.LiveMap TO $orig_db.LiveMap,\
                             $temp_db.LiveMapRegionMenu TO $orig_db.LiveMapRegionMenu,\
                             $temp_db.MetaData TO $orig_db.MetaData,\
                             $temp_db.Op TO $orig_db.Op,\
                             $temp_db.Options TO $orig_db.Options,\
                             $temp_db.OptionsWidget TO $orig_db.OptionsWidget,\
                             $temp_db.OptionsWidgetItem TO $orig_db.OptionsWidgetItem,\
                             $temp_db.PathIndex TO $orig_db.PathIndex,\
                             $temp_db.UI TO $orig_db.UI,\
                             $temp_db.VariableInfo TO $orig_db.VariableInfo,\
                             $temp_db.View TO $orig_db.View,\
                             $temp_db.attributes TO $orig_db.attributes,\
                             $temp_db.children TO $orig_db.children,\
                             $temp_db.elements TO $orig_db.elements
                 });

}

##
# Subclass of LASDB::Utility::MySQL with # a few added methods.
#
package LASDB::MySQL;
use DBI;
use File::PathConvert qw(rel2abs);
@LASDB::MySQL::ISA = qw(LASDB::Utility::MySQL);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
# Prepared statements
    $self->{attSelect} =
	$self->{dbh}->prepare(qq{select name,value from attributes where owner_id=?});
    bless $self,$class;
}

##
# Get the attributes associated with this object id
#
sub getAttributes {
    my ($self, $oid) = @_;
    my $rows = $self->preparedRows('attSelect', $oid);
    my %AttHash;
    foreach my $row (@{$rows}){
	$AttHash{$row->[0]} = $row->[1];
    }
    return \%AttHash;
}

##
# Get the path of the element associated with this object id
#
sub getPathName {
    my ($self, $oid) = @_;
    my @row = $self->selectRow('path', 'elements', qq(oid=$oid));
    return $row[0];
}

## 
# Get the name of the element associated with this object id
#
sub getElementName {
    my ($self, $oid) = @_;
    my @row = $self->selectRow('name', 'elements', qq(oid=$oid));
    return $row[0];
}


##
# Get named elements
# Only return immediate descendants of parent_id if defined
#
sub findElements {
    my ($self, $name, $parent_id) = @_;
    my $result = $self->selectRows('oid,name', 'elements', qq(name='$name'));
    my @elements;
    if ($parent_id){
	my %ehash;
	my $children = $self->selectRows('childid,childname', 'children',
					qq(parentid=$parent_id));
	foreach my $child (@{$children}){
	    $ehash{$child->[0]} = $child->[1];
	}
	foreach my $val (@{$result}){
	    my $cname = $ehash{$val->[0]};
	    if ($cname){
		push(@elements, [$val->[0], $cname]);
	    }
	}
    } else {
	foreach my $val (@{$result}){
	    push(@elements, $val->[0]);
	}
    }
    return @elements;
}

##
# Get the parent of this object id
#
sub getParent {
    my ($self, $oid) = @_;
    my @row = $self->selectRow('parentid', 'children', qq(childid=$oid));
    die "Database error: no parent for $oid" if ! @row;
    my $pid = $row[0];
    my @namerow = $self->selectRow('name', 'elements', qq(oid=$pid));
    return [$pid, $namerow[0]];
}

##
# Get the children associated with this object id
#
sub getChildren {
    my ($self, $oid) = @_;
    return $self->selectRows('childid,childname', 'children',
			     qq(parentid=$oid));
}

##
# Set the root of the XML hierarchy associated with the url
sub setRoot {
    my ($self, $url) = @_;
    my $hash = &LASDB::getHash($url);
    my @rvals = $self->selectRow(qw(dbase map), qq(urlid='$hash'));
    die "url = $url -- hash = $hash -- Database doesn't appear to contain $url" if ($#rvals < 0);

    $self->{dbh}->do(qq(use $rvals[0]));
}

##
# Add a list of XML files that need to be serialized to the database
#
sub addURLs {
    my ($self, @files) = @_;
    my $dbh = $self->{dbh};

# Create mapping table if necessary
    if (!$self->tableExists('map')){
	$dbh->do(qq{create table map (urlid char(128) NOT NULL primary key, path varchar(255) NOT NULL, dbase varchar(32))});
    } 
    my $dbid = $self->tableSize('map');
    foreach my $file (@files){
	$dbh->do(qq(use las));
	my $dbname;
	my $hash = &LASDB::getHash($file);
	my @rows = $self->selectRow('dbase', 'map', qq(urlid='$hash'));
	if ($#rows < 0){
	    $dbname = 'las' . $dbid++;
	    $dbh->do(qq(INSERT INTO map SET urlid='$hash', path='$file', dbase='$dbname'));
	} else {
	    $dbname = $rows[0];
	}

# Create db schema for XML. Trashes any old database
	eval {			# Ignore any errors
	    $dbh->do(qq(DROP DATABASE $dbname));
	};
	$dbh->do(qq(CREATE DATABASE $dbname));
	$dbh->do(qq(USE $dbname));
	$dbh->do(qq{CREATE TABLE elements (oid int NOT NULL primary key, name varchar(128) NOT NULL, path varchar(255) NOT NULL)});
	$dbh->do(qq{CREATE TABLE attributes (oid int NOT NULL primary key, owner_id int NOT NULL, name varchar(128) NOT NULL, value text NOT NULL)});
	$dbh->do(qq{CREATE TABLE children (oid int NOT NULL, parentid int NOT NULL, childid int NOT NULL, childname varchar(128) NOT NULL)});
    }
# Prepare commonly used SQL statements
    $self->{addElement} =
	$dbh->prepare(qq(INSERT INTO elements VALUES (?,?,?)));
    $self->{addChildren} =
	$dbh->prepare(qq(INSERT INTO children VALUES (?,?,?,?)));
    $self->{addAttribute} = 
	$dbh->prepare(qq(INSERT INTO attributes VALUES (?,?,?,?)));
    $self->{selectMatches} =
	$dbh->prepare(qq(select value from attributes where owner_id=? and name='match'));
}


##
# Add a XML element to the database
#
sub addElement{
    my ($self, $name, $oid, $pid, $path) = @_;
    my $linkoid = &LASDB::getOID;
    $self->{addElement}->execute($oid, $name, $path);
#    print STDERR "Adding children: linkoid: $linkoid: oid: $oid\n";
    $self->{addChildren}->execute($linkoid, $pid, $oid, $name);
}

##
# Add a list of attributes for an element to the database
#
sub addAttributes {
    my ($self, $ownerid, @atts) = @_;
    return if scalar @atts == 0;
    while(scalar @atts > 0){
	my $name = shift @atts;
	my $value = shift @atts;
	my $oid = &LASDB::getOID;
	$self->{addAttribute}->execute($oid, $ownerid, $name,$value);
    }
}

##
# Build all required indexes
sub buildIndexes {
    my $self = shift;
    my $dbh = $self->{dbh};
    $dbh->do(qq{alter table elements add index (name)});
    $dbh->do(qq{alter table elements add index (path)});
    $dbh->do(qq{alter table attributes add index (owner_id)});
    $dbh->do(qq{alter table attributes add index (name)});
    $dbh->do(qq{alter table children add index (parentid)});
    $dbh->do(qq{alter table children add index (childid)});
}

##
# Replace all link elements with the actual element that is pointed to by
# the link
sub resolveLinks {
    my $self = shift;
    my $firstTime = 1;
    my $dbh = $self->{dbh};
    my $rows =
	$dbh->selectall_arrayref(qq{select oid,path from elements where name='link'});
    my %MatchCache;
    foreach my $row (@{$rows}){
	my ($oid,$path) = @{$row};
# Get value of match attribute
	$self->{selectMatches}->execute($oid);
	my $results = $self->{selectMatches}->fetchall_arrayref;
	die "Link missing 'match' attribute" if scalar @{$results} < 1;
	my $match = $results->[0]->[0];
#	print "Resolving link matching $match\n";

# Process any '..' or '.' in path
	$path =~ s/\/link$//;
	$match = $path . '/' . $match if ($match !~ /^\//);
	my @pieces = split(/\/+/, $match);
	my @newpieces = ();
	foreach my $chunk (@pieces){
	    next if $chunk eq "" || $chunk eq '.';
	    if ($chunk eq ".."){
		pop @newpieces;
	    } else {
		push(@newpieces, $chunk);
	    }
	}
	$match = '/' . join('/', @newpieces);
	

# Get oid of referenced element
	if (!defined($MatchCache{$match})){
	    $results = $dbh->selectall_arrayref(qq{select oid,name from elements where path='$match'});
	    die "Match attribute for link href '$match' doesn't point to valid element"
		if scalar @{$results} < 1;
	    die "Match attribute for link href '$match' has multiple matches"
		if scalar @{$results} > 1;
	    die "Link href '$match' points to link element -- not allowed"
		if $results->[0]->[1] eq 'link';
	    $MatchCache{$match} = $results->[0];
	}
	my $newoid = $MatchCache{$match}->[0];
	my $newname = $MatchCache{$match}->[1];

# Replace childid of link element with referenced element in children table
# MySQL 3.23.33 has a strange bug where the first time that an update is called
# even if it is successful, the returned value is 0E0. Calling an update the
# first time through that forces a fail seems to resolve this problem.
	if ($firstTime){
	    $results = $dbh->do(qq{update children set childid=$newoid,childname='$newname' where childid=1234567890});
	    $firstTime = 0;
	}
	$results = $dbh->do(qq{update children set childid=$newoid,childname='$newname' where childid=$oid});
	die "link not child of element"
	    if $results < 1;
	die "link child of more than one element"
	    if $results > 1;

    }
}

package LASDB::Config;

sub new {
    my ($class, $dbClass, $url) = @_;
    die 'MySQL only database currently supported'
	if $dbClass ne 'LASDB::MySQL';
    my $self = {
    };
    bless $self, $class;
    $self->initialize($url);
    $self;
}

sub initialize {
    my ($self, $url) = @_;
    $self->{db} = new LASDB::MySQL('las', $LAS::Server::Config{db_login},
				   $LAS::Server::Config{db_password},
				   $LAS::Server::Config{db_host});
    $self->{db}->setRoot($url);
}

sub findElements {
    my ($self, $name, $parent_id) = @_;
    my @elements;
    foreach my $kid ($self->{db}->findElements($name, $parent_id)){
	push(@elements, new LASDB::Container($self, @{$kid}));
    }
    return @elements;
}

sub getRoot {
    my $self = shift;
    my $kids = $self->{db}->getChildren(0);
    return new LASDB::Container($self, @{$kids->[0]});
}

package LASDB::Container;
use Carp;

sub new {
    my ($class, $config, $oid, $name) = @_;
    die "Missing name argument" if ! $name;
    my $self = {
	config => $config,
	oid => $oid,
	db => $config->{db},
	name => $name
	};
    bless $self, $class;
}

sub getPath {
    my $self = shift;
    return $self->{db}->getPathName($self->{oid});
}

# Implements XML::DOM::Document method
sub getDoctype {
    return undef;
}

# Implements XML::DOM::Document method
sub getDocumentElement {
    my $self = shift;
    return $self->{config}->getRoot;
}

# Implements XML::DOM::Node method
sub getChildNodes {
    my $self = shift;
    if (wantarray){
	return $self->getChildren;
    } else {
	return new LASDB::DOM::NodeList($self->getChildren);
    }
}

# Implements XML::DOM::Node method
sub getElementsByTagName {
    my $self = shift;
    my $name = shift;
    if (wantarray){
	return $self->{config}->findElements($name, $self->{oid});
    } else {
	return new LASDB::DOM::NodeList($self->{config}->findElements($name, $self->{oid}));
    }
}

# Implements XML::DOM::Node method
sub getFirstChild {
    my $self = shift;
    my @children = $self->getChildren;
    return $children[0];
}

# Implements XML::DOM::Node method
sub getParentNode {
    my $self = shift;
    return new LASDB::Container($self->{config},
				  @{$self->{db}->getParent($self->{oid})});
}

sub setupAttributes {
    my $self = shift;
    if (!defined $self->{attributes}){
	$self->{attributes} = $self->{db}->getAttributes($self->{oid});
    }
}

# Implements XML::DOM::Element method
sub getAttributes {
    my $self = shift;
    $self->setupAttributes;
    return new LASDB::DOM::NamedNodeMap($self->{attributes});
}

# Implements XML::DOM::Element method
sub getAttribute {
    my $self = shift;
    my $key = shift;
    $self->setupAttributes;
    return $self->{attributes}->{$key};
}

# Implements XML::DOM::Element method
sub getNodeName {
    my $self = shift;
    return $self->getName;
}

# Implements XML::DOM::Element method
sub getNodeType {
    my $self = shift;
    return 1;			# XML::DOM::ELEMENT_NODE
}

# Implements XML::DOM::Element method
sub getTagName {
    my $self = shift;
    return $self->getName;
}

sub getChildren {
    my $self = shift;
    if (!defined $self->{children}){
	$self->{children} = [];
	my $kids = $self->{db}->getChildren($self->{oid});
	foreach my $kid (@{$kids}){
	    push(@{$self->{children}},
		 new LASDB::Container($self->{config},@{$kid}));
	}
    }
    return @{$self->{children}};
}

sub getLongName {
}

sub getName {
    my $self = shift;
    return $self->{name};
}

sub getProperties {
    die;
}
$LASDB::Container::Depth = 0;
$LASDB::Container::Indent = 0;

sub toString {
    my $e = shift;
    my $maxDepth = shift;
    my $rval = "";
    return "" if defined($maxDepth) && $LASDB::Container::Depth > $maxDepth;

    $rval .=  " "x$LASDB::Container::Indent . "<" . $e->getName;
    my $attributes = $e->getAttributes;
    my @keys = keys (%{$attributes});
    foreach my $name (@keys){
	next if $name eq $LASDB::PCDATA_NAME;
	my $value = $attributes->{$name};
	$rval .=  qq( $name="$value");
    }
    $rval .=  ">";
    $rval .=  $attributes->{$LASDB::PCDATA_NAME} if defined $attributes->{$LASDB::PCDATA_NAME};
    $rval .=  "\n";
    $LASDB::Container::Indent+=4;
    foreach my $child ($e->getChildren){
	$LASDB::Container::Depth++;
	$rval .= $child->toString($child, $maxDepth);
	$LASDB::Container::Depth--;
    }
    $LASDB::Container::Indent-=4;
    $rval .=  " "x$LASDB::Container::Indent . "</" . $e->getName . ">\n";
}


#
# Serialize a set of XML files to the database
#
package LASDB::Serializer;
use XML::Parser;
use URI::URL;

sub new {
    my ($class, $dbClass, $login, $password, $host) = @_;
    die 'MySQL only database currently supported'
	if $dbClass ne 'LASDB::MySQL';
    my $self = {
	path => [],
	parent => [0],
	char_data => ""	# Current character data
    };
    bless $self, $class;
    $self->initialize($login, $password, $host);
    $self;
}

sub clearChar {
    $_[0]->{char_data} = "";
}

sub appendChar {
    $_[0]->{char_data} .= $_[1];
}
    
sub flushChar {
    my ($self) = @_;
    my $id = $self->{oid};
    my $data = $self->{char_data};
    return if ! $id || $data eq "";
    $data =~ s/^\s+//g;
    $data =~ s/\s+$//g;
    $self->{db}->addAttributes($id, $LASDB::PCDATA_NAME, $data);
}

sub initialize{
    my ($self, $login, $password, $host) = @_;
    $self->{db} = new LASDB::MySQL('las',$login, $password, $host);
}

# Static method
# The Perl Expat XML parser uses callback routines when events occur
# such as the start of an element or the end of an element.
# The callback is called with a reference to the XML::Parser::Expat object
# This routine allows the callback routine to get a reference to the
# LASDB::Serializer object that invoked the XML parser
sub getOwner {
    my $parser = shift;
    return $parser->{__owner};
}

# Static method
# Callback for the start of an XML element
sub element_start {
    my $parser = shift;
    my $e = shift;
    my @atts = @_;
    my $serializer = getOwner($parser);
    my $oid = &LASDB::getOID;
    $serializer->{oid} = $oid;
    $serializer->pushPath($e);
    $serializer->clearChar;
    my $db = $serializer->{db};
    $db->addElement($e, $oid, $serializer->getParent, $serializer->getPath);
    if (@atts){
	$db->addAttributes($oid, @atts);
    }
    $serializer->pushParent($oid);
#    print 'element_start:', $e, join(':', @atts), "\n";
}

# Static method
# Callback for the end of an XML element
sub element_end {
    my $parser = shift;
    my $owner = getOwner($parser);
# Write any accumulated character data
    $owner->flushChar;
    $owner->{oid} = undef;
    $owner->popPath;
    $owner->popParent;

}

# Static method
# Callback for the PCDATA
sub char_data {
    my $parser = shift;
    my $data = shift;
    return if ! defined($data) || $data eq "";
    my $owner = getOwner($parser);
    my $owner_id = $owner->{oid};
    return if ! defined($owner_id);
    $owner->appendChar($data);
#    print 'char_data:',$data, "\n";
}

# Serialize the XML files to the database
sub serialize {
    my ($self) = @_;
    foreach my $fileName (@{$self->{urls}}){
	$self->{db}->mapURL($fileName);
	print STDERR "Serializing $fileName to database\n";
	my $parser = new XML::Parser(ErrorContext => 2);
	$parser->setHandlers(Start => \&element_start,
			     End => \&element_end,
			     Char => \&char_data);
	$parser->{__owner} = $self;
	my $url = new URI::URL($fileName);
	die "Bad url: $url -- only support file: scheme"
	    if $url->scheme ne "file";
	$parser->parsefile($url->path);
	print STDERR "Building indexes...\n";
	$self->{db}->buildIndexes;
	print STDERR "Resolving links...\n";
	$self->{db}->resolveLinks;
    }
}

sub pushPath {
    my $self = shift;
    my $e = shift;
    push(@{$self->{path}}, $e);
}

sub popPath {
    my $self = shift;
    pop(@{$self->{path}});
}

sub getPath {
    my $self = shift;
    return '/' . join('/', @{$self->{path}});
}

sub pushParent {
    my $self = shift;
    my $oid = shift;
    push(@{$self->{parent}}, $oid);
}

sub popParent {
    my $self = shift;
    pop(@{$self->{parent}});
}

sub getParent {
    my $self = shift;
    my @parents = @{$self->{parent}};
    return $parents[$#parents];
}

# Add a list of files that are to be serialized to the
# database
sub addURLs {
    my ($self, @names) = @_;
    $self->{urls} = [];
    my %DupHash;
    my @urls;
    foreach my $name (@names){
	my $url = &LASDB::getURL($name);
	die "Can't read file $url" if ! -r $url->path;
	die "Duplicate URL: $url" if $DupHash{$url};
	$DupHash{$url} = $url;
	push @{$self->{urls}}, $url;
    }
    $self->{db}->addURLs(@{$self->{urls}});
}

#-----------------------------------------------------------
# 
# LASDB::DOM::* classes are XML DOM emulation classes
# that emulate a small part of the XML DOM API
#-----------------------------------------------------------

package LASDB::DOM::Parser;

sub new {
    my $class = shift;
    my $self = {
    };
    bless $self, $class;
}

sub parsefile {
    my ($self, $fname) = @_;
    my $url = &LASDB::getURL($fname);
    $self->{dbconfig} = new LASDB::Config('LASDB::MySQL', $url);
    return $self->{dbconfig}->getRoot;
}

package LASDB::DOM::Element;
sub new {
    my $class = shift;
    my $self = {};
    bless $self, $class;
}

package LASDB::DOM::NodeList;
sub new {
    my $class = shift;
    my @foo = @_;		# Need this for Perl 5.005. Works w/o in 5.6
    bless \@foo, $class;
}

sub item {
    $_[0]->[$_[1]];
}

sub getLength {
    scalar (@{$_[0]});
}

package LASDB::DOM::NamedNodeMap;
sub new {
    bless $_[1], $_[0];
}

sub getNamedItem {
    $_[0]->{$_[1]};
}


1;
