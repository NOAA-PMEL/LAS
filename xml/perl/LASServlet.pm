use LASDB;
use strict;

##
# MySQL driver for generating database representation of XML config
# files for servlet based ui

package LASDB::MySQL::Servlet;

use DBI;
use File::PathConvert qw(rel2abs);
@LASDB::MySQL::Servlet::ISA = qw(LASDB::Utility::MySQL);

#
# Escape SQL. Just handle single quote (for now), and only escape
# 'name' or 'path_name' or 'title'
#
sub escape {
    my ($name,$value) = @_;
    $value =~ s/\'/\\\'/g if $name eq 'name' or $name eq 'path_name' or $name eq 'title';
    $value;
}

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);
    bless $self,$class;
}

sub init {
    my ($self, $file) = @_;
    my $dbh = $self->{dbh};
    $file = rel2abs($file);
    $file = 'file:' . $file if ($file !~ /^file:/);

    $dbh->do(qq(USE las));
    my $dbname;
    my $hash = &LASDB::getHash($file);
    my @rows = $self->selectRow('dbase', 'map', qq(urlid='$hash'));
    if ($#rows < 0){
        die "Map to database not properly intiialized";
    } 
    $dbname = $rows[0];

# Create db schema for XML.
    if (!$self->dbExists($dbname)){
        $dbh->do(qq(CREATE DATABASE $dbname));
    }
    $dbh->do(qq(USE $dbname));
    $self->createSchema;
}

sub createSchema {
    my ($self) = @_;
    my $dbh = $self->{dbh};
    $self->dropTable('Browser');
    $dbh->do(qq{CREATE TABLE Browser(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 agent varchar(255) NOT NULL, \
                 applet char(1) NOT NULL\
                )});
    $self->dropTable('BrowserCandidate');
    $dbh->do(qq{CREATE TABLE BrowserCandidate(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 agent varchar(255) NOT NULL, \
                 applet char(1) NOT NULL\
                )});
    $self->dropTable('Config');
    $dbh->do(qq{CREATE TABLE Config(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 packageid varchar(255) NOT NULL,\
                 href varchar(255) NOT NULL, \
                 serverurl varchar(255) NOT NULL,\
                 contact varchar(255) NOT NULL\
                )});
    $self->dropTable('Category');
    $dbh->do(qq{CREATE TABLE Category (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 name varchar(255) NOT NULL, INDEX(name),\
                 parentid int NOT NULL, INDEX(parentid),\
                 configid int NOT NULL, INDEX(configid),\
                 type char(1) NOT NULL, INDEX(type),\
                 path varchar(255) NOT NULL, \
                 path_name varchar(255) NOT NULL, INDEX(path_name),\
                 grid_type varchar(255),\
                 category_include varchar(255),\
                 variable_include varchar(255),\
                 constrain_include varchar(255),\
                 category_include_header varchar(255),\
                 variable_include_header varchar(255),\
                 constrain_include_header varchar(255)
                )});
    $self->dropTable('PathIndex');
    $dbh->do(qq{CREATE TABLE PathIndex (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 path varchar(255) NOT NULL, INDEX(path)
                )});
    $self->dropTable('DerivedCategory');
    $dbh->do(qq{CREATE TABLE DerivedCategory (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 parentcat int NOT NULL, INDEX(parentcat), \
                 parentid int NOT NULL, INDEX(parentid), \
                 sessionid varchar(255) NOT NULL, INDEX(sessionid), \
                 mask_type int NOT NULL, \
                 name varchar(255) NOT NULL, INDEX(name)
                )});
    $self->dropTable('MetaData');
    $dbh->do(qq{CREATE TABLE MetaData (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 parentid int NOT NULL, INDEX(parentid),\
                 docurl varchar(255) NOT NULL
                )});
    $self->dropTable('VariableInfo');
    $dbh->do(qq{CREATE TABLE VariableInfo (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 categoryid int NOT NULL, INDEX(categoryid),\
                 institution_id int NOT NULL,\
                 ui_id int NOT NULL,\
                 url1 varchar(255) NOT NULL, \
                 url2 varchar(255),\
                 custom_url varchar(255), \
                 dods_url varchar(255), \
                 units varchar(64) NOT NULL, \
                 updated timestamp, \
                 composite int NOT NULL\
                )});
    $self->dropTable('DerivedVariableInfo');
    $dbh->do(qq{CREATE TABLE DerivedVariableInfo (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 categoryid int NOT NULL, INDEX(categoryid), \
                 varinfo_id int NOT NULL, INDEX(varinfo_id), \
                 sessionid varchar(255) NOT NULL, INDEX(sessionid)
                )});
    $self->dropTable('AxisVariableJoin');
    $dbh->do(qq{CREATE TABLE AxisVariableJoin (\
                 varid int NOT NULL, INDEX(varid),\
                 axisid int NOT NULL, INDEX(axisid)\
                )});
    $self->dropTable('Axis');
    $dbh->do(qq{CREATE TABLE Axis (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 lo varchar(64),\
                 hi varchar(64),\
                 name varchar(255) NOT NULL, \
                 units varchar(255), \
                 type char(1) NOT NULL, \
                 size int NOT NULL,
                 category varchar(32) NOT NULL\
                )});

    $self->dropTable('DerivedAxis');
    $dbh->do(qq{CREATE TABLE DerivedAxis (\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 parent_axis int NOT NULL, INDEX(parent_axis), \
                 parent_derived int NOT NULL, INDEX(parent_derived), \
                 lo varchar(64), \
                 hi varchar(64), \
                 type char(1) NOT NULL, \
                 is_analysis int NOT NULL DEFAULT 0,\
                 is_new int NOT NULL DEFAULT 0,\
                 analysis_type varchar(64) NOT NULL, \
                 sessionid varchar(255) NOT NULL, INDEX(sessionid)\ 
                )});
    $self->dropTable('AxisWidgets');
    $dbh->do(qq{CREATE TABLE AxisWidgets(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 axis_id int NOT NULL, INDEX(axis_id),\
                 initial_index_lo varchar(64), \
                 initial_index_hi varchar(64),\
                 default_type varchar(64)\
                )});
    $self->dropTable('AxisWidgetItems');
    $dbh->do(qq{CREATE TABLE AxisWidgetItems(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 axis_widget_id int NOT NULL, INDEX(axis_widget_id),\
                 label varchar(64) NOT NULL,\
                 value varchar(64) NOT NULL\
                )});
    $self->dropTable('Contributor');
    $dbh->do(qq{CREATE TABLE Contributor(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 parentid int NOT NULL, INDEX(parentid),\
                 name varchar(255) NOT NULL, \
                 url varchar(255) NOT NULL, \
                 role varchar(255) NOT NULL
                )});
    $self->dropTable('Institution');
    $dbh->do(qq{CREATE TABLE Institution(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 name varchar(255) NOT NULL, \
                 url varchar(255) NOT NULL
                )});
    $self->dropTable('UI');
    $dbh->do(qq{CREATE TABLE UI(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 title varchar(255) NOT NULL,\
                 z_text varchar(255) NOT NULL,\
                 isConstrained int\
                )});
    $self->dropTable('AnalysisWidgetItem');
    $dbh->do(qq{CREATE TABLE AnalysisWidgetItem(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 ui_id int NOT NULL, INDEX(ui_id),\
                 label varchar(64) NOT NULL,\
                 value varchar(64) NOT NULL\
                )});
    $self->dropTable('Constraints');
    $dbh->do(qq{CREATE TABLE Constraints(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 ui_id int NOT NULL, INDEX(ui_id),\
                 type varchar(32) NOT NULL,\
                 label varchar(64) NOT NULL,\
                 count int NOT NULL,\
                 docurl varchar(255) NOT NULL, \
                 required int NOT NULL DEFAULT 0,\
                 multiselect int NOT NULL DEFAULT 0,\
                 size int DEFAULT 1,\
                 style varchar(255) NOT NULL DEFAULT 'select',\
                 extra varchar(255) NOT NULL
                )});
    $self->dropTable('ConstraintWidget');
    $dbh->do(qq{CREATE TABLE ConstraintWidget(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 constraint_id int NOT NULL, INDEX(constraint_id)\,
                 style varchar(255) NOT NULL DEFAULT 'select',\
                 size int DEFAULT 0
                )});
    $self->dropTable('ConstraintWidgetItem');
    $dbh->do(qq{CREATE TABLE ConstraintWidgetItem(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 constraint_widget_id int NOT NULL, INDEX(constraint_widget_id),\
                 label varchar(64) NOT NULL,\
                 value varchar(64) NOT NULL\
                )});
    $self->dropTable('LiveMap');
    $dbh->do(qq{CREATE TABLE LiveMap(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 ui_id int NOT NULL, INDEX(ui_id),\
                 image_id int NOT NULL, INDEX(image_id),\
                 xlo varchar(64) NOT NULL,\
                 xhi varchar(64) NOT NULL,\
                 ylo varchar(64) NOT NULL,\
                 yhi varchar(64) NOT NULL\
                )});
    $self->dropTable('Images');
    $dbh->do(qq{CREATE TABLE Images(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 image_url varchar(255) NOT NULL,\
                 image mediumblob\
                )});
    $self->dropTable('LiveMapRegionMenu');
    $dbh->do(qq{CREATE TABLE LiveMapRegionMenu(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT, \
                 livemapid int NOT NULL, INDEX(livemapid),\
                 label varchar(64) NOT NULL,\
                 value varchar(64) NOT NULL\
                )});
    $self->dropTable('Op');
    $dbh->do(qq{CREATE TABLE Op(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 ui_id int NOT NULL, INDEX(ui_id),\
                 mode varchar(64) NOT NULL, INDEX(mode),\
                 view varchar(64) NOT NULL, INDEX(view),\
                 label varchar(64) NOT NULL,\
                 value varchar(64) NOT NULL\
                )});
    $self->dropTable('View');
    $dbh->do(qq{CREATE TABLE View(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 ui_id int NOT NULL,INDEX(ui_id),\
                 label varchar(64) NOT NULL,\
                 region varchar(8) NOT NULL,\
                 value varchar(8) NOT NULL\
                )});
    $self->dropTable('Options');
    $dbh->do(qq{CREATE TABLE Options(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 ui_id int NOT NULL, INDEX(ui_id),\
                 op varchar(64) NOT NULL, INDEX(op),\
                 title varchar(255) NOT NULL,\
                 type char(1) NOT NULL,\
                 help text\
                )});
    $self->dropTable('OptionsWidget');
    $dbh->do(qq{CREATE TABLE OptionsWidget(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 option_id int NOT NULL,\
                 name varchar(64)\
                )});
    $self->dropTable('OptionsWidgetItem');
    $dbh->do(qq{CREATE TABLE OptionsWidgetItem(\
                 oid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\
                 options_widget_id int NOT NULL,\
                 value varchar(64),\
                 label varchar(64) NOT NULL\
                )});
}
                  
sub writeAttributes {
    my ($self, $obj, $tname, $atts) = @_;
    my $dbh = $self->{dbh};
    my $statement = qq{insert into $tname set };
    my @out = ();
    if (ref($atts) eq 'ARRAY'){
        foreach my $att (@{$atts}){
            $obj->{$att} = escape($att,$obj->{$att});
            push(@out, "$att='" . $obj->{$att} . "'");
        }
    } elsif (ref($atts) eq 'HASH'){
        foreach my $key (keys %{$atts}){
            $atts->{$key} = escape($key,$atts->{$key});
            my $value = $atts->{$key};
            push(@out, "$key='" . $atts->{$key} . "'") if defined($value);
        }
    } else {
        die "Invalid type for atts argument";
    }
    $statement .= join(',',@out);
    $dbh->do($statement);
}
package LAS::ServletDB::UI;     # Predeclaration

##
# Perl hash that contains all the information describing a browser.
# This information includes the user agent string and a boolean that
# indicates if the browser works with the applet.
package LAS::ServletDB::Browser;
use LAS;

##
# Create a new LAS::ServletDB::Browser object
#
# @param $agent the user agent string
# @param $applet boolean true=works with applet
sub new {
    my ($class,$agent,$applet) = @_;
    my $self = {
        agent => $agent,
        applet => $applet,
    };
    bless $self, $class;
}

##
# Dump the contents of the hash to STDOUT.
sub dump {
    my $self = shift;
    println "LAS::ServletDB::Browser";
    println "\tagent: ", $self->{agent};
    println "\tapplet: ", $self->{applet};
}

##
# Serialize the contents of the hash to following tables in the database
# <p>
# <b>Browser</b>
# 
# @param $db LASDB::MySQL::Servlet object
sub serialize {
    my ($self,$db) = @_;
    my $atts = { agent => $self->{agent},
                 applet => $self->{applet}};

    $db->writeAttributes($self,'Browser',$atts);
    $self->{serializedID} = $db->lastInserted;
}


##
# Perl hash that contains all the information describing a browser.
# This information includes the user agent string and a boolean that
# indicates if the browser works with the applet.
package LAS::ServletDB::Contributor;
use LAS;

##
# Create a new LAS::ServletDB::Contributor object
#
# @param $agent the user agent string
# @param $applet boolean true=works with applet
sub new {
    my ($class,$parentid,$url, $name, $role) = @_;
    my $self = {
        parentid => $parentid,
        url => $url,
        name => $name,
        role => $role,
    };
    bless $self, $class;
}

##
# Set the parentid of the Contributor
#
# @param $id the parent id for this contributor
#
sub setParentid {
   my ($self, $parentid) = @_;
   $self->{parentid}=$parentid;
}

##
# Dump the contents of the hash to STDOUT.
sub dump {
    my $self = shift;
    println "LAS::ServletDB::Contributor";
    println "\tparentid ", $self->{parentid};
    println "\turl: ", $self->{url};
    println "\tname: ", $self->{name};
    println "\trole: ", $self->{role};
}

##
# Serialize the contents of the hash to following tables in the database
# <p>
# <b>Browser</b>
# 
# @param $db LASDB::MySQL::Servlet object
sub serialize {
    my ($self,$db) = @_;
    my $atts = { parentid => $self->{parentid},
                 name => $self->{name},
                 role => $self->{role},
                 url => $self->{url}};

    $db->writeAttributes($self,'Contributor',$atts);
    $self->{serializedID} = $db->lastInserted;
}

##
# Perl hash that contains all the information describing an axis.
# This information includes title, units, type, init_lo, init_hi
# and all LAS::ServletDB::Widgets needed for this axis.
package LAS::ServletDB::Axis;
use LAS;

##
# Create a new LAS::ServletDB::Axis object
#
# @param $config LAS::ServletDB::Config object
# @param $lo lowest value of axis
# @param $hi highest value of axis
# @param $size number of points in axis
# @param $title
# @param $units
# @param $type [x|y|z|t]
# @param $category
sub new {
    my ($class,$config,$lo,$hi,$size, $title,$units,$type,$category) = @_;
    my $self = {
        title => $title,
        units => $units,
        type => $type,
        category => $category,
        config => $config,
        widgets => [],
        size => $size,
        serializedID => -1,
        lo => $lo,
        hi => $hi
    };
    bless $self, $class;
}

##
# Adds a user interface widget to this axis.
#
# @param $widgetName internally generated name for this widget
# @param $initlo as specified in <display_lo> xml tag
# @param $inithi as specified in <display_hi> xml tag
# @param $default ["first"|"last"] to default to the first or last
# valid value with this widget is presented.
sub addWidget {
    my ($self,$widgetName,$initlo,$inithi,$default) = @_;
    my $widget = $self->{config}->{widgetsByName}->{$widgetName};
    die "Can't find widget: $widgetName" if ! $widget;
    push(@{$self->{widgets}}, $widget);
    $widget->{initlo} = $initlo;
    $widget->{inithi} = $inithi;
    $widget->{default} = $default;
}

##
# Dump the contents of the hash to STDOUT.
sub dump {
    my $self = shift;
    println "LAS::ServletDB::Axis";
    println "\ttitle: ", $self->{title};
    println "\tunits: ", $self->{units};
    println "\ttype: ", $self->{type};
    println "\tcategory: ", $self->{category};
    foreach my $widget (@{$self->{widgets}}){
        $widget->dump;
    }
}

##
# Serialize the contents of the hash to following tables in the database
# <p>
# <b>Axis, AxisVariableJoin, AxisWidgets, AxisWidgetItems</b>
# 
# @param $db LASDB::MySQL::Servlet object
# @param $varid variable identifier used in the <b>VariableInfo</b> table in the database
sub serialize {
    my ($self,$db,$varid) = @_;
    my $atts = { name => $self->{title}, units=>$self->{units},
                 type => $self->{type}, category => $self->{category},
                 size => $self->{size},
                 lo => $self->{lo},
                 hi => $self->{hi}};
    my $serializeWidget = 0;
    if ($self->{serializedID} < 0){
        $serializeWidget = 1;
        $db->writeAttributes($self,'Axis',$atts);
        $self->{serializedID} = $db->lastInserted;
    }

    my $axisid = $self->{serializedID};
    $self->{axisid} = $axisid;
    $self->{varid} = $varid;
    $db->writeAttributes($self,'AxisVariableJoin', ['axisid','varid']);

    if ($serializeWidget){
        foreach my $widget (@{$self->{widgets}}){
            $widget->serialize($db, $axisid);
        }
    }
}

package LAS::ServletDB::Variable;
use LAS;
sub new {
    my ($class,$dataset,$vname,$customurl,$ui,
        $varurls,$inst,$units,$dods, $isComposite, $grid_type, $vtype, 
        $category_include, $variable_include, $constrain_include,
        $category_include_header, $variable_include_header, $constrain_include_header) = @_;
    my $self = {
        name => $vname,
        customurl => $customurl,
        ui => $ui,
        varurls => $varurls,
        parent => $dataset,
        config => $dataset->{config},
        axes => [],
        inst => $inst,
        units=>$units,
        dods_url => $dods,
        grid_type => $grid_type,
        type => $vtype,
        is_composite => $isComposite,
        category_include => $category_include,
        variable_include =>$variable_include,
        constrain_include => $constrain_include,
        category_include_header => $category_include_header,
        variable_include_header =>$variable_include_header,
        constrain_include_header => $constrain_include_header
    };
    bless $self, $class;
}

sub addAxis {
    my ($self,$name) = @_;
    my $axis = $self->{config}->{axesByName}->{$name};
    die "Can't find axis: $name" if ! $axis;
    push(@{$self->{axes}}, $axis);
}

sub dump {
    my $self = shift;
    println "LAS::ServletDB::Variable";
    println "\tname:",  $self->{name};
    println "\tcustomurl :",  $self->{customurl};
    println "\tui :", $self->{ui};
    print "\tvarurls : ", join(":", @{$self->{varurls}}), "\n";
    foreach my $axis (@{$self->{axes}}){
        $axis->dump;
    }
}

sub serialize {
    my ($self,$db,$configid, $parentid, $path, $path_name) = @_;
    $path_name .= "/" . $self->{name};
    my $type=$self->{type};
    my $atts = {parentid => $parentid, configid=>$configid,
                type => $type, path => $path,
                name => $self->{name}, path_name => $path_name,
                grid_type => $self->{grid_type},
                category_include => $self->{category_include},
                variable_include => $self->{variable_include},
                constrain_include => $self->{constrain_include},
                category_include_header => $self->{category_include_header},
                variable_include_header => $self->{variable_include_header},
                constrain_include_header => $self->{constrain_include_header}
            };
    $db->writeAttributes($self,'Category',$atts);

    my $categoryid = $db->lastInserted;
    $atts = {categoryid=>$categoryid};
    my @varurls = @{$self->{varurls}};
    die "Can't have more than 2 variable URLs" if $#varurls > 1;
    $atts->{url1} = $varurls[0];
    $atts->{url2} = $varurls[1] if $#varurls > 0;
    $atts->{units} = $self->{units} if defined $self->{units};
    $atts->{custom_url} = $self->{customurl} if defined $self->{customurl};
    $atts->{composite} = $self->{is_composite}
        if defined $self->{is_composite};
    my $dods_url =  $self->{dods_url};
    if (defined $dods_url){
        $dods_url =~ s/\#.*$//; # Eliminate any variable info in URL
        $atts->{dods_url} = $dods_url;
    }
    $db->writeAttributes($self,'VariableInfo',$atts);
    my $varid = $db->lastInserted;

    foreach my $axis (@{$self->{axes}}){
        $axis->serialize($db,$varid);
    }
    $self->{ui}->serialize($db, $varid);
    $self->{inst}->serialize($db, $varid);
}

package LAS::ServletDB::MetaData;
use LAS;

sub new {
    my ($class,$category) = @_;
    my $self = {
        category => $category,
        docurl => undef
    };
    bless $self, $class;
}

sub addDocUrl {
    $_[0]->{docurl} = $_[1];
}

sub getDocUrl {
    $_[0]->{docurl};
}

sub serialize {
    my ($self,$db,$parentid) = @_;
    my $atts = {parentid => $parentid, 
                docurl => $self->{docurl}};
    $db->writeAttributes($self,'MetaData',$atts);
}

package LAS::ServletDB::Category;
use LAS;
sub new {
    my ($class,$sconfig, $name, $metadata, $grid_type, $type, 
        $cat_include, $var_include, $const_include,
        $cat_include_header, $var_include_header, $const_include_header) = @_;
    my $self = {
        variables => [],
        categories => [],
        contributors => [],
        name => $name,
        parentid => 0,
        config => $sconfig,
        metadata => $metadata,
        grid_type => $grid_type,
        type => $type,
        category_include => $cat_include,
        variable_include => $var_include,
        constrain_include => $const_include,
        category_include_header => $cat_include_header,
        variable_include_header => $var_include_header,
        constrain_include_header => $const_include_header
    };
    bless $self, $class;
}

sub addCategory {
    my $self = shift;
    my $newcat = new LAS::ServletDB::Category($self->{config}, @_);
    push (@{$self->{categories}}, $newcat);
    return $newcat;
}

sub addContributor {
   my $self = shift;
   my $newContrib = new LAS::ServletDB::Contributor(@_);
   push (@{$self->{contributors}}, $newContrib);
}

sub addVariable {
    my $self = shift;
    unshift(@_, $self);
    my $rval = new LAS::ServletDB::Variable(@_);
    push(@{$self->{variables}}, $rval);
    return $rval;
}

sub dump {
    my $self = shift;
    println "LAS::ServletDB::Category";
    foreach my $variable (@{$self->{variables}}){
        $variable->dump;
    }
}

sub serialize {
    my ($self,$db,$configid, $parentid, $path, $path_name) = @_;
    $parentid = '0' if ! defined($parentid);
    $path = '0' if ! defined($path);
    if (defined($path_name)){
        $path_name .= "/" . $self->{name};
    } else {
        $path_name = $self->{name};
    }
    $path_name = $self->{name} if ! defined($path_name);
    my $type=$self->{type};
    my $atts = {parentid => $parentid, configid=>$configid,
                type => $type, path => $path, path_name => $path_name,
                name => $self->{name},
                grid_type => $self->{grid_type},
                category_include => $self->{category_include},
                variable_include =>$self->{variable_include},
                constrain_include => $self->{constrain_include},
                category_include_header => $self->{category_include_header},
                variable_include_header =>$self->{variable_include_header},
                constrain_include_header => $self->{constrain_include_header}
            };

    $db->writeAttributes($self,'Category',$atts);
    my $id = $db->lastInserted;
    my $newpath = "$path:$id";
    foreach my $cat (@{$self->{categories}}){
        $cat->serialize($db, $configid, $id, $newpath, $path_name);
    }
    foreach my $contrib (@{$self->{contributors}}){
       $contrib->setParentid($id);
       $contrib->serialize($db);
    }
    foreach my $variable (@{$self->{variables}}){
        $variable->serialize($db,$configid,$id,$newpath, $path_name);
    }
    $self->{metadata}->serialize($db, $id) if $self->{metadata};
}

package LAS::ServletDB::Widget;
use LAS;
sub new {
    my ($class,$name,$labels,$values) = @_;
    my $self = {
        name => $name,
        labels => $labels,
        values => $values
        };
    bless $self,$class;
}

sub dump {
    my $self = shift;
    println "LAS::ServletDB::Widget";
    println "\tname: ", $self->{name};
    my $labels = $self->{labels};
    my $values = $self->{values};
    my $useValues = scalar(@{$values}) > 0;
    for (my $i=0; $i < scalar(@{$labels}); $i++){
        println "\t\tLabel: ", $labels->[$i];
        println "\t\tValue: ", $values->[$i] if $useValues;
    }
}
        
sub serialize {
    my ($self, $db, $axisid) = @_;
    my $labels = $self->{labels};
    my $values = $self->{values};
    my $useValues = scalar(@{$values}) > 0;
    my $atts = {axis_id => $axisid,
                 initial_index_lo => $self->{initlo},
                 initial_index_hi => $self->{inithi},
                 default_type => $self->{default}};
    $db->writeAttributes($self, 'AxisWidgets', $atts);
    my $widget_id = $db->lastInserted;

    $atts = {axis_widget_id => $widget_id};
    for (my $i=0; $i < scalar(@{$labels}); $i++){
        $atts->{label} = $labels->[$i];
        if ($useValues){
            $atts->{value} = $values->[$i];
        } else {
            $atts->{value} = $atts->{label};
        }
        $db->writeAttributes($self,'AxisWidgetItems', $atts);
    }
}

package LAS::ServletDB::Institution;

sub new {
    my ($class, $lasinst) = @_;
    my $self = {inst => $lasinst,
                serialized => -1};
    bless $self,$class;
}

sub serialize {
    my ($self, $db, $varid) = @_;
    
    if ($self->{serialized} < 0){
        my $atts = {
            name => $self->{inst}->getInstName,
            url => $self->{inst}->getURL
            };
        $db->writeAttributes($self,'Institution',$atts);
        $self->{serialized} = $db->lastInserted;
    }
    my $instid = $self->{serialized};
    $db->{dbh}->do(qq{update VariableInfo SET institution_id = $instid where oid=$varid});
        
}

##
# The grand object that contains all of the information that
# will ultimately end up in the LAS database tables.
package LAS::ServletDB::Config;
use LAS;

##
# Create a new LAS::ServletDB::Config object.
#
# @param $config LAS::Config object
# @param $packageid unique identifier associated with a package
# @param $url URL of the LAS product server
# @param $href identifier ('file:...location...') of the las.xml file
sub new {
    my ($class,$config,$packageid,$url,$contact,$href) = @_;
    my $self = {
        serverurl => $url,
        contact => $contact,
        axesByName => {},
        widgetsByName => {},
        packageid => defined($packageid) ? $packageid : "",
        config => $config,
        categories => [],
        href => $href,
        uis => {}
    };
    $self->{axesByName} = {};
    $self->{instByName} = {};
    bless $self, $class;
    $self;
}

sub dump {
    my $self = shift;
    println "\nLAS::ServletDB::Config";
    println "\tserverurl: ", $self->{serverurl};
    println "\tpackageid:", $self->{packageid};
    println "\thref:", $self->{href};
    foreach my $dataset (@{$self->{categories}}){
        $dataset->dump;
    }
    foreach my $key (keys %{$self->{uis}}){
        $self->{uis}->{$key}->dump;
    }
}

sub addInstitution {
    my ($self,$var) = @_;
    my $lasinst = $var->getInstitution;
    $lasinst = $var->getDataset->getInstitution if ! $lasinst;
    $lasinst = $var->getConfig->getInstitution if ! $lasinst;
    die "No institution defined in XML config file" if ! $lasinst;
    my $key = $lasinst->getInstName . $lasinst->getURL;
    my $inst = $self->{instByName}->{$key};
    if (!$inst){
        $inst = new LAS::ServletDB::Institution($lasinst);
        $self->{instByName}->{$key} = $inst;
    }
    return $inst;
}

sub getInstanceByName {
    my ($self,$lasinst) = @_;
    my $key = $lasinst->getInstName . $lasinst->getURL;
    return $self->{instByName}->{$key};
}

sub serialize {
    my ($self, $db) = @_;
    my @atts = qw(serverurl contact packageid href);
    $db->writeAttributes($self, 'Config', \@atts);
    my $lastid = $db->lastInserted;
    foreach my $dataset (@{$self->{categories}}){
        $dataset->serialize($db, $lastid);
    }
}
    
sub addAxis {
    my $self = shift;
    my $name = shift;
    unshift(@_, $self);
    my $rval = new LAS::ServletDB::Axis(@_);
    $self->{axesByName}->{$name} = $rval;
    return $rval;
}

sub addWidget {
    my $self = shift;
    my $name = $_[0];
    my $rval = new LAS::ServletDB::Widget(@_);
    $self->{widgetsByName}->{$name} = $rval;
    return $rval;
}

sub addCategory {
    my $self = shift;
    unshift(@_, $self);
    my $rval = new LAS::ServletDB::Category(@_);
    push (@{$self->{categories}}, $rval);
    return $rval;
}

sub addUIByName {
    my ($self,$gen, $name) = @_;
    my $ui = $self->{uis}->{$name};
    return $ui if $ui;
    $self->{uis}->{$name} = $ui = new LAS::ServletDB::UI($gen,$name);
    return $ui;
}

sub getCategories {
    return @{$_[0]->{categories}};
}

package LAS::ServletDB::UI::MenuItem;

sub new {
    my ($class,$item) = @_;
    my @values = $item->getValues;
    my $self = {
        label => $item->getText,
        values => \@values
        };
    bless $self,$class;
}

package LAS::ServletDB::UI::ViewItem;
@LAS::ServletDB::UI::ViewItem::ISA = qw(LAS::ServletDB::UI::MenuItem);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $item = shift;
    my $self = $class->SUPER::new($item);
    $self->{view} = $item->getAttribute('view');
    bless $self,$class;
}

package LAS::ServletDB::UI::LiveMap;
use LAS;
use TMAPDate;
$LAS::ServletDB::UI::LiveMap::Images = ();
sub new {
    my ($class,$gen,$map) = @_;
    my $self = {
        children => [],
        gen => $gen
    };
    my $href = $map->findChild("image")->getAttribute('href');
    $href =~ s/^#//;
    my $image = $LAS::UI::Generator::Images{$href};
    die "Can't find <image> href: $href" if ! $image;

    $href = $map->findChild("menu")->getAttribute('href');
    $href =~ s/^#//;
    my $menu = $LAS::UI::Generator::Menus{$href};
    die "Can't find <menu> href: $href" if ! $menu;

    my @bounds = $image->getBounds;
    $self->{image_bounds} = \@bounds;
    $self->{image_url} = $image->getUrl;

    foreach my $item ($menu->getChildren){
        push(@{$self->{children}}, new LAS::ServletDB::UI::MenuItem($item));
    }

    bless $self,$class;
}

sub dump {
    my ($self) = @_;
    println "LAS::ServletDB::UI::LiveMap";
    println "\tbounds: ", join(':', @{$self->{image_bounds}});
    println "\timage_url: ", $self->{image_url};
    foreach my $region (@{$self->{children}}){
        println "\tRegion label:",$region->{label}, " : ",
        join(',',@{$region->{values}});
    }
}


# TODO -- Get image from http URL

sub getHttpImage {
    my ($self, $url) = @_;
    die "HTTP images not supported ($url)";
}

sub getFileImage {
    my ($self, $url) = @_;
    my $dir = $self->{gen}->getDir;
    my @paths = ("$dir../../WebContent/classes/$url",
                 "$dir$url");
    my $path;
    foreach my $p (@paths){
        if (-f $p){
            $path = $p;
            last;
        }
    }
    if (! $path){
        die "Can't find image file: $url";
    }

    if (! -r $path){
        die "No read access to image file: $path";
    }
    if (-s $path >= 16384000){  # Max size for mediumblob
        die "Image $path too large: max size is 16 MB";
    }
    open IMAGE, $path or die "Can't open $path for read";
    my $buf;
    my $image = "";
    while(read IMAGE,$buf,8192){
        $image .= $buf;
    }
    close IMAGE;
    return $image;
}

sub getImage {
    my ($self, $db) = @_;
    my $url = $self->{image_url};
    my $image;
    if ($url =~ /^\s*http:/){
        $image = $self->getHttpImage($url);
    } else {
        $url =~ s/^\s*file://;
        $image = $self->getFileImage($url);
    }
    $image;
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    my $image_url = $self->{image_url};
    my $image_id;
    if ($LAS::ServletDB::UI::LiveMap::Images{$image_url}){
        $image_id = $LAS::ServletDB::UI::LiveMap::Images{$image_url};
    } else {
        my $image = $self->getImage($db);
        my $qimage =  $db->{dbh}->quote($image);

#       Blobs cannot be quoted in INSERT statement or the blob is 
#       corrupted by some versions of Perl. So, we special case the
#       insert here.
#       Also, do an insert followed by an update as one update corrupts
#       the blob with some versions of Perl. Oy.

        $db->{dbh}->do(qq{INSERT INTO Images (image) VALUES ($qimage)}) or
            die "Can't insert image in database";
        $image_id = $db->lastInserted;
        $db->{dbh}->do(qq{UPDATE Images set image_url='$image_url' where oid=$image_id}) or die "Can't update Images table";
        $LAS::ServletDB::UI::LiveMap::Images{$image_url} = $image_id;
        my $last_image = $db->lastInserted;

# Code to detect corrupted images
        my $dbh = $db->{dbh};
        my $cursor = $dbh->prepare("SELECT image FROM Images WHERE oid = $last_image");
        $cursor->execute or die;
        my $row = $cursor->fetchrow_arrayref or die;
        $cursor->finish;
        undef $cursor;
        die "Integrity check failed for image URL: \"$image_url\"" if $$row[0] ne $image;
# End test code
        
    }
        

    my $bounds = $self->{image_bounds};
    my $atts = {
                image_id => $image_id,
                xlo => $bounds->[0],
                xhi => $bounds->[1],
                ylo => $bounds->[2],
                yhi => $bounds->[3],
                ui_id => $ui_id
                };
    $db->writeAttributes($self,'LiveMap', $atts);
    my $liveid = $db->lastInserted;


    foreach my $region (@{$self->{children}}){
# Hack for "events" menu
        my @values = @{$region->{values}};
        $values[4] = (new TMAP::Date($values[4]))->toFerretString
            if defined($values[4]);
        $values[5] = (new TMAP::Date($values[5]))->toFerretString
            if defined($values[4]);
        $atts = {livemapid => $liveid,
                 label => $region->{label},
                 value => join(',',@values)
                 };
        $db->writeAttributes($self,'LiveMapRegionMenu',$atts);
    }
}

package LAS::ServletDB::UI::Constraint;

sub new {
    my ($class,$item) = @_;
    my $type = $item->getAttribute('type');
    if ($type eq 'variable'){
        return new LAS::ServletDB::UI::Constraint::Variable($item);
    } elsif ($type eq 'text'){
        return new LAS::ServletDB::UI::Constraint::Text($item);
    } elsif ($type eq 'textfield'){
        return new LAS::ServletDB::UI::Constraint::TextField($item);
    } else {
        die "Unknown type '$type' for <constraint>";
    }
}

package LAS::ServletDB::UI::AbstractConstraint;
use LAS;
sub new {
    my ($class,$item) = @_;
    my $self = {
        type => $item->getAttribute('type'),
        label => $item->getAttribute('label'),
        docurl => $item->getAttribute('docurl'),
        count => $item->getAttribute('count'),
        required => $item->getAttribute('required'),
        multiselect => $item->getAttribute('multiselect'),
        size => $item->getAttribute('size'),
        style => $item->getAttribute('style'),
        children => []
    };
    bless $self,$class;
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    my $atts = {
        ui_id => $ui_id,
        type => $self->{type}
    };
    $atts->{label} = $self->{label} if defined $self->{label};
    $atts->{docurl} = $self->{docurl} if defined $self->{docurl};
    $atts->{count} = $self->{count} if defined $self->{count};
    $atts->{count} = 1 if ! defined $atts->{count};
    $atts->{required} = 0;
    $self->{size} = 1 if ! defined $self->{size};
    $atts->{size} = $self->{size};
    $self->{style} = "select" if ! defined $self->{style};
    $atts->{style} = $self->{style};
    if ($self->{required} && $self->{required} eq 'true'){
        $atts->{required} = 1;
    }
    $atts->{multiselect} = 0;
    if ($self->{multiselect} && $self->{multiselect} eq 'true'){
        $atts->{multiselect} = 1;
    }
    $atts->{extra} = $self->{extra};

    $db->writeAttributes($self, 'Constraints', $atts);
    my $constraint_id = $db->lastInserted;


    foreach my $menu (@{$self->{children}}){
        my $size = $menu->getAttribute("size");
        $size = $self->{size} if ! $size;
        die "size attribute for <constraint> must be >= 1"
            if $size < 1;
        my $style = $menu->getAttribute("style");
        $style = $self->{style} if ! $style;
        die "style attribute for <constraint> must be 'select'|'check'"
            if !($style eq 'select' or $style eq 'check');
        $atts = {constraint_id => $constraint_id,
                 size => $size,
                 style => $style
                 };
        $db->writeAttributes($self,'ConstraintWidget', $atts);
        my $constraint_widget_id = $db->lastInserted;
        foreach my $menu_item ($menu->getChildren){
            $atts = {constraint_widget_id => $constraint_widget_id,
                     label => $menu_item->getText,
                     value => join(',',$menu_item->getValues)
                     };
            $db->writeAttributes($self,'ConstraintWidgetItem',$atts);
        }
    }

}

package LAS::ServletDB::UI::Constraint::Variable;
@LAS::ServletDB::UI::Constraint::Variable::ISA = 
   qw(LAS::ServletDB::UI::AbstractConstraint);

sub new {
    my $proto = shift;
    my ($item) = @_;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);

    my $children = $item->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $e = $iter->next;
        my $tagname = $e->getTagName;
        if ($tagname eq 'extra'){
            my $child = $e->getFirstChild;
            die "<extra> tag can only contain text" if
                ! ref($child) eq "XML::DOM::Text";
            $self->{extra} =  $child->getData;
        } else {
            die "Only <extra> can be child of a variable <constraint>";
        }
    }

    bless $self,$class;
}


package LAS::ServletDB::UI::Constraint::Text;
use LAS;
@LAS::ServletDB::UI::Constraint::Text::ISA = 
   qw(LAS::ServletDB::UI::AbstractConstraint);

sub new {
    my $proto = shift;
    my ($item) = @_;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);

    my $children = $item->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $e = $iter->next;
        my $tagname = $e->getTagName;
        if ($tagname eq 'menu'){
            my $href = $e->getAttribute('href');
            die "<menu> child of <constraint> missing 'href' attribute"
                if ! $href;
            $href =~ s/^#//;
            my $menu = $LAS::UI::Generator::Menus{$href};
            die "Can't find <menu> href: $href" if ! $menu;
            push(@{$self->{children}}, $menu);
        } elsif ($tagname eq 'extra'){
            my $child = $e->getFirstChild;
            die "<extra> tag can only contain text" if
                ! ref($child) eq "XML::DOM::Text";
            $self->{extra} =  $child->getData;
        } else {
            die "Only <menu> or <extra> can be child of a text <constraint>";
        }
                
    }

    bless $self,$class;
}

package LAS::ServletDB::UI::Constraint::TextField;
@LAS::ServletDB::UI::Constraint::TextField::ISA = 
   qw(LAS::ServletDB::UI::AbstractConstraint);

sub new {
    my $proto = shift;
    my ($item) = @_;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(@_);

    my $children = $item->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $e = $iter->next;
        my $tagname = $e->getTagName;
        if ($tagname eq 'extra'){
            my $child = $e->getFirstChild;
            die "<extra> tag can only contain text" if
                ! ref($child) eq "XML::DOM::Text";
            $self->{extra} =  $child->getData;
        } else {
            die "Only <extra> can be child of a textfield <constraint>";
        }
    }

    bless $self,$class;
}



package LAS::ServletDB::UI::Op;
use LAS;
sub new {
    my ($class,$mode,$view,$mitem) = @_;
    my @values = $mitem->getValues;
    my $self = {
        mode => $mode,
        view => $view,
        label => $mitem->getText,
        values => \@values
    };
    bless $self,$class;
}

sub dump {
    my $self = shift;
    println "LAS::ServletDB::UI::Op";
    println "\tmode: ", $self->{mode};
    println "\tview: ", $self->{view};
    println "\tlabel: ", $self->{label};
    println "\tvalues: ", join(',',@{$self->{values}});
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    my $atts = {
        ui_id => $ui_id,
        mode => $self->{mode},
        view => $self->{view},
        label => $self->{label},
        value => join(',', @{$self->{values}})
        };
    $db->writeAttributes($self, 'Op', $atts);
}

package LAS::ServletDB::UI::Ops;
use LAS;
sub new {
    my ($class,$gen,$map) = @_;
    my $self = {
        children => [],
        isConstrained => 0
    };
    foreach my $item ($map->getChildren){
        if ($item->getName eq 'constraint'){
            $self->{isConstrained} = 1;
            push(@{$self->{children}},
                 new LAS::ServletDB::UI::Constraint($item));
        } else {
            my $href = $item->getAttribute('href');
            $href =~ s/^#//;
            my $menu = $LAS::UI::Generator::Menus{$href};
            die "Can't find <menu> href: $href" if ! $menu;
            my $view = $item->getAttribute('view');
            my $mode = $item->getAttribute('mode');
            $mode = "normal" if ! defined($mode);
            my @views = ();
            if (defined($view)){
                @views = split(/\s*,\s*/, $view);
            } else {
                push(@views, '*');
            }
            foreach my $mitem ($menu->getChildren){
                foreach my $view (@views){
                    push (@{$self->{children}},
                          new LAS::ServletDB::UI::Op($mode,$view,$mitem));
                }
            }
        }
    }
    bless $self,$class;
}

sub isConstrained {
    return $_[0]->{isConstrained};
}

sub dump {
    my $self = shift;
    println "LAS::ServletDB::UI::Ops";
    foreach my $child (@{$self->{children}}){
        $child->dump;
    }
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    foreach my $child (@{$self->{children}}){
        $child->serialize($db, $ui_id);
    }
}

package LAS::ServletDB::UI::Views;
use LAS;
sub new {
    my ($class,$gen,$map) = @_;
    my $self = {
        children => []
    };
    foreach my $item ($map->getChildren){
        my $href = $item->getAttribute('href');
        $href =~ s/^#//;
        my $menu = $LAS::UI::Generator::Menus{$href};
        die "Can't find <menu> href: $href" if ! $menu;
        foreach my $mitem ($menu->getChildren){
            push (@{$self->{children}},
                  new LAS::ServletDB::UI::ViewItem($mitem));
        }
    }
    bless $self,$class;
}

sub dump {
    my ($self) = @_;
    println "LAS::ServletDB::UI::Views";
    foreach my $region (@{$self->{children}}){
        println "\tViews label:",$region->{label}, " : ",
        join(',',@{$region->{values}});
    }
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    foreach my $region (@{$self->{children}}){
        my $atts = {
            ui_id => $ui_id,
            label => $region->{label},
            region => join(',',@{$region->{values}}),
            value => $region->{view},
            };
        $db->writeAttributes($self,'View', $atts);
    }
}



package LAS::ServletDB::UI::OptionsItem;
use LAS;
sub new {
    my ($class,$optionsItem) = @_;
    my $self = {
        optionsItem => $optionsItem
    };
    bless $self, $class;
}


sub serialize {
    my ($self, $db, $id) = @_;
    my $optionsItem = $self->{optionsItem};
    my $atts = {
        options_widget_id => $id,
        label => $optionsItem->getText,
        value => $optionsItem->getAttribute("values")
        };
    $db->writeAttributes($self,'OptionsWidgetItem', $atts);
}
    

package LAS::ServletDB::UI::OptionsWidget;
use LAS;
sub new {
    my ($class,$optionsWidget) = @_;
    my $self = {
        children => [],
        optionsWidget => $optionsWidget
    };
    foreach my $optionsItem ($optionsWidget->getChildren){
        if ('LAS::UI::MenuItem' eq ref($optionsItem)){
            push(@{$self->{children}},
                 new LAS::ServletDB::UI::OptionsItem($optionsItem));
        }
    }
    bless $self, $class;
}

sub serialize {
    my ($self, $db, $opt_id) = @_;
    my $optionsWidget = $self->{optionsWidget};
    my $atts = {
        option_id => $opt_id,
        name => $optionsWidget->getAttribute('name')
        };
    $db->writeAttributes($self,'OptionsWidget', $atts);
    my $widget_id = $db->lastInserted;
    
    foreach my $child (@{$self->{children}}){
        $child->serialize($db, $widget_id);
    }
}

package LAS::ServletDB::UI::Option;
use LAS;
sub new {
    my ($class,$option, $op) = @_;
    my $self = {
        children => [],
        option => $option,
        op => $op
    };
    foreach my $optionsWidget ($option->getChildren){
        push(@{$self->{children}},
             new LAS::ServletDB::UI::OptionsWidget($optionsWidget));
    }
    bless $self, $class;
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    my $option = $self->{option};
    my $atts = {
        op => $self->{op},
        ui_id => $ui_id,
        title => $option->getTitle,
        help => $option->getHelp,
        type => $option->getType
        };
    $db->writeAttributes($self,'Options', $atts);
    my $opt_id = $db->lastInserted;
    
    foreach my $child (@{$self->{children}}){
        $child->serialize($db, $opt_id);
    }
}

package LAS::ServletDB::UI::OptionDef;
use LAS;
sub new {
    my ($class,$optiondef, $op) = @_;
    my $self = {
        children => [],
        optiondef => $optiondef
    };
    foreach my $option ($optiondef->getChildren){
        push(@{$self->{children}},
             new LAS::ServletDB::UI::Option($option, $op));
    }
    bless $self,$class;
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    foreach my $child (@{$self->{children}}){
        $child->serialize($db, $ui_id);
    }
}

package LAS::ServletDB::UI::Options;
use LAS;
sub new {
    my ($class,$gen,$map) = @_;
    my $self = {
        children => []
    };
    foreach my $child ($map->getChildren){
        if ($child->getName eq 'ifoptions'){
            my $op = $child->getAttribute('op');
            my $href = $child->getAttribute('href');
            $href =~ s/^#//;
            my $optiondef = $LAS::UI::Generator::OptionDefs{$href};
            die "Can't find <optiondef> href: $href" if ! $optiondef;
            push(@{$self->{children}},
                 new LAS::ServletDB::UI::OptionDef($optiondef, $op));
        }
    }
    bless $self,$class;
}

sub dump {
    my ($self) = @_;
    println "TODO: LAS::ServletDB::UI::Options";
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    foreach my $option (@{$self->{children}}){
        $option->serialize($db, $ui_id);
    }
}


package LAS::ServletDB::UI::AnalysisItem;
@LAS::ServletDB::UI::AnalysisItem::ISA = qw(LAS::ServletDB::UI::MenuItem);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $item = shift;
    my $self = $class->SUPER::new($item);
    bless $self,$class;
}

package LAS::ServletDB::UI::Analysis;

sub new {
    my ($class,$gen,$map) = @_;
    my $self = {
        children => []
        };
    foreach my $item ($map->getChildren){
        my $href = $item->getAttribute('href');
        $href =~ s/^#//;
        my $menu = $LAS::UI::Generator::Menus{$href};
        die "Can't find <menu> href: $href" if ! $menu;
        foreach my $mitem ($menu->getChildren){
            push (@{$self->{children}},
                  new LAS::ServletDB::UI::AnalysisItem($mitem));
        }
    }
    bless $self,$class;
}

sub serialize {
    my ($self, $db, $ui_id) = @_;
    foreach my $item (@{$self->{children}}){
        my $atts = {
            ui_id => $ui_id,
            label => $item->{label},
            value => join(',',@{$item->{values}})
            };
        $db->writeAttributes($self,'AnalysisWidgetItem', $atts);
    }
}

package LAS::ServletDB::UI;
use LAS;
sub new {
    my ($class, $gen, $name) = @_;
    my $self = {
        gen => $gen,
        name => $name,
        serialized => -1,
        isConstrained => 0
    };
    bless $self,$class;
    $self->init;
    $self;
}

sub init {
    my ($self) = @_;
    my $gen = $self->{gen};
    my $name = $self->{name};
    $name = "" if $name eq 'DefaultUI';
    $name = "" if ! defined($name);

    my $def = $gen->getDefaultByName($name);
    die "Can't find a <default> with a name of '$name'" if ! $def;
    $self->{z_text} = $def->getAttribute("z_text");
    $self->{z_text} = "Select depth" if ! defined $self->{z_text};
    die "Can't find <default> with name attribute: '$name'"
        if ! $def;
    foreach my $map ($def->getChildren){
        $self->initMap($map);
    }
}

sub dump {
    my ($self) = @_;
    println "LAS::ServletDB::UI";
    $self->{livemap}->dump;
    $self->{ops}->dump;
    $self->{views}->dump;
    $self->{options}->dump;
}

sub serialize {
    my ($self, $db, $varid) = @_;
    my $ui_id;
    if ($self->{serialized} < 0){
        my $atts = {isConstrained => $self->{isConstrained},
                    z_text => $self->{z_text},
                    title =>$self->{gen}->getTitle};
        $db->writeAttributes($self,'UI',$atts);
        $self->{serialized} = $db->lastInserted;
        $ui_id = $self->{serialized};
        $self->{livemap}->serialize($db,$ui_id);
        $self->{ops}->serialize($db,$ui_id);
        $self->{views}->serialize($db,$ui_id);
        $self->{options}->serialize($db,$ui_id);
        if ($self->{analysis}){
            $self->{analysis}->serialize($db,$ui_id);
        }
    }
    $ui_id = $self->{serialized};
    $db->{dbh}->do(qq{update VariableInfo SET ui_id=$ui_id where oid=$varid});
}

sub initMap {
    my ($self, $map) = @_;
    my @kids = $map->getChildren;
    my $type = $map->getAttribute('type');
    if ($type eq 'ops'){
        $self->{ops} = new LAS::ServletDB::UI::Ops($self->{gen},$map);
        $self->{isConstrained} = 1 if $self->{ops}->isConstrained;
    } elsif ($type eq 'views') {
        $self->{views} = new LAS::ServletDB::UI::Views($self->{gen},$map);
    } elsif ($type eq 'livemap') {
        $self->{livemap} = new LAS::ServletDB::UI::LiveMap($self->{gen},$map);
    } elsif ($type eq 'options'){
        $self->{options} = new LAS::ServletDB::UI::Options($self->{gen},$map);
    } elsif ($type eq 'analysis'){
        $self->{analysis} = new LAS::ServletDB::UI::Analysis($self->{gen},$map);
    } else {
        die "Unknown type attribute for <map>: $type";
    }
}

package LAS::Category::Config;  # Forward declaration

##
# Generator class to create the HTML and javascript components that constitute
# the LAS user interface.  The generate() method of the Generator class is 
# called in las/xml/perl/genLas.pl.

package LAS::ServletDB::Generator;
use LAS;
use LASUI;
use LASServlet;
use TMAPDate;
use File::Basename;
use File::PathConvert qw(rel2abs splitpath);
use vars qw(%AxisWidgets $MaxWidgetSize @months @day_months);
require 'Ferret_config.pl';
# Largest length for date widgets before
# it is broken into multiple components
$LAS::ServletDB::Generator::MaxWidgetSize = 32; 
#Lists of required components for axes
%LAS::ServletDB::Generator::AxisWidgets = (); 
@LAS::ServletDB::Generator::months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
@LAS::ServletDB::Generator::day_months = qw(15-Jan 14-Feb 15-Mar 15-Apr 15-May 15-Jun 15-Jul 15-Aug 15-Sep 15-Oct 15-Nov 15-Dec);

##
# Make sure axes have at least a 'x' and 'y' type axis
# <p>
# <b>TODO</b> - validate composite variables.
#
# @return goodChildren list of children with at least 'x' and 'y' axes
sub validateAxes {
    my $hasBad = 0;
    my @goodChildren;
    foreach my $var (@_){
        my @children;
        my ($hasX, $hasY);
        my $class = ref($var);
        if ($class eq 'LAS::Variable'){
            @children = $var->getChildren;
            foreach my $child (@children){
                my $type = $child->getAttribute('type');
                $hasX = 1 if $type eq 'x';
                $hasY = 1 if $type eq 'y';
            }
            if ($hasX && $hasY){
                push(@goodChildren, $var);
            } else {
                print STDERR "Variable '", $var->getName,
                "' is missing an X or Y axis\n";
                $hasBad = 1;
            }
        } else {
            push(@goodChildren, $var);
        }
    }
    die "Need to remove variables with bad axes from XML configuration file(s)"
        if $hasBad;
    return @goodChildren;
}

##
# Create a new widget consisting of arrays of labels and values.
# These will be used to create an HTML selector when this class
# is serialized to the database.
# @param name identifier for the widget
# @param childref reference to the list of children 
# @param units currently unused
# @param checkForTime flag specifying values are times
# @param checkForIndexOnly flag specifying arbitrary strings to be associated with index values
sub genWidget {
    my ($self,$name, $childref, $units,
        $checkForTime, $checkForIndexOnly) = @_;
    my @children = @{$childref};
    my $isDate = 0;
    if ($checkForTime){
        my $date = new TMAP::Date($children[0]);
        $isDate = 1 if $date->isOK;
    }
    my $isNumber = 1;
    my $count = 0;
    my @labels = ();
    my @values = ();
    foreach my $longName (@children){
        if ($isDate){
            my $date = new TMAP::Date($longName);
            $longName = $date->toFerretString;
        } else {
            if ($isNumber){
#
# Let Perl determine if this is a number
#
                my $lastwarn = $SIG{'__WARN__'};
                $SIG{'__WARN__'} = sub { $@ = $_[0]};
                eval "use strict; no strict 'subs'; my \$foo = $longName";
                $isNumber = 0 if $@;
                $SIG{'__WARN__'} = $lastwarn;
            }
        }
        push(@labels, $longName);
        $count++;
    }
#
# If the value is an arbitrary string, use index values instead of the
# string value

    if ($checkForIndexOnly && !($isNumber || $isDate)){
        for (my $i=1; $i <= $count; $i++){
            push(@values, "$i");
        }
    }
    $self->{servletConfig}->addWidget($name,\@labels,\@values);
}


##
# Creates an axis widget for x, y, z
# @param name identifier for the widget
# @param axis x|y|z|t
# @param units passed through to genWidget
sub genDataAxisWidget {
    my ($self,$name, $axis, $units) = @_;
    my $id = $axis->getName;
    my $lo = $axis->getLo;
    my $size = $axis->getSize;
    my $step = $axis->getStep;
    my $type = $axis->getAttribute("type");
    my $children = [];
    my $value = $lo;

    #
    # X and Y axis just need lo and hi, since the Map Widget is used
    # For now, assume only x axis can be modulo
    #
    if ($type =~ /[x|y]/){
         push(@{$children}, $lo);
         if ($type eq 'y'){
             push(@{$children}, $lo + ($size-1) * $step) if $size > 1;
         } else {               # Modulo 360 check
             my $range = $size * $step;
             my $hi;
             if (($range % 360) == 0){
                                # Map applet only works with -180->180
                 pop(@{$children});
                 push(@{$children}, "-180");
                 $hi = 180;
             } else {
                 $hi = $lo + $range - $step;
             }
             push(@{$children}, $hi) if $size > 1;
         }
    } else {
        for (my $i = 0; $i < $size; $i++){
            push(@{$children}, $value);
            $value += $step;
        }
    }
    $self->genWidget($name, $children, $units,0,1);
    $AxisWidgets{$id} = [[$name], [$children]];
}

##
# Generates a time axis widget with day/month/year selector.
# (Minutes and seconds are not yet supported.)
# @param name identifier for the widget
# @param axis t
# @param units passed through to genWidget
sub genTimeAxisWidget {
    my ($self,$name, $axis, $units) = @_;
    my $id = $axis->getName;
    my $lo = $axis->getLo;
    my $hi = $axis->getHi;
    my $size = $axis->getSize;
    my @step = $axis->getStep;  # OK because this is only called for reg axes
    my $children = [];
    my $date = new TMAP::Date($lo);
    if (! $date->isOK){
        $self->genDataAxisWidget($name, $axis, $units);
        return;
    }

    if ($size < $MaxWidgetSize){
        for (my $i = 0; $i < $size; $i++){
            push(@{$children}, $date->toFerretString);
            $date = $date->addDelta(@step);
        }
        $self->genWidget($name, $children,0,1);
        $AxisWidgets{$id} = [[$name], [$children]];
    } else {
#
# Currently, the step size for regular axes is constrained to be an
# integer, and only one of year/month/day/hour/min/sec can be non-zero
#
        my $hidate = new TMAP::Date($hi);
        my ($hy, $hm, $hd) = $hidate->getYMD;
        my ($ly, $lm, $ld) = $date->getYMD;
        my ($sy, $sm, $sd, $sh, $smin, $ssec) = $axis->getStep;
        die "Don't support (yet) axes with mins and seconds" if $smin || $ssec;
        my $widgetNames = [];
        my $widgetChildren = [];
# Day widget
        if ($sd > 0 || $sh > 0){
            my $deltaDays = $date->getDeltaDays($hidate);
            if ($deltaDays > 31 || $hy > $ly || $hm > $lm){
                push(@{$widgetNames}, "_month_days_axis");
                my @days = map {sprintf("%.2d", $_)} (1..31);
                push(@{$widgetChildren}, \@days);
            } else {
                my @days = ();
                my $newdate = $date;
                my @ymd = $newdate->getYMD;
                push(@days, sprintf("%.2d", $ymd[2] + 0));
                for (my $i=0; $i < $deltaDays; $i++){
                    $newdate = $newdate->addDelta(0,0,1);
                    my @ymd = $newdate->getYMD;
                    last if $ymd[2] == $days[0];
                    push(@days, sprintf("%.2d", $ymd[2] + 0));
                }
                @days = sort {$a <=> $b} @days;
                $self->genWidget("${name}_day", \@days);
                push(@{$widgetNames}, "${name}_day");
                push(@{$widgetChildren}, \@days);
            }
        } else {
            $self->genWidget("${name}_day", [$ld]);
            push(@{$widgetNames}, "${name}_day");
            push(@{$widgetChildren}, [$ld]);
        }

# Month widget
        if ($sm > 0 || $sd > 0 || $sh > 0){
            my $deltaDays = $date->getDeltaDays($hidate);
            if ($deltaDays > 365 || $hy > $ly){
                $self->genWidget("${name}_month_axis", \@months);
                push(@{$widgetNames}, "${name}_month_axis");
                push(@{$widgetChildren}, \@months);
            } else {
                my $deltaMonths = $hm - $lm;
                my %monthHash = ();
                my $newdate = $date;
                my @ymd = $newdate->getYMD;
                my $firstMonth = $ymd[1];
                $monthHash{$firstMonth} = 1;
                for (my $i=0;
                     $i < $deltaMonths && scalar(keys(%monthHash)) < 12; $i++){
                    $newdate = $newdate->addDelta(0,1,0);
                    my @ymd = $newdate->getYMD;
                    my $index = $ymd[1];
                    $monthHash{$index} = 1;
                }
                my @monthList;
                foreach my $index (sort {$a <=> $b} keys(%monthHash)){
                    push(@monthList, $months[$index-1]);
                }
                $self->genWidget("${name}_month", \@monthList);
                push(@{$widgetNames}, "${name}_month");
                push(@{$widgetChildren}, \@monthList);
            }
        } else {
            $self->genWidget("${name}_month", [$months[$lm-1]]);
            push(@{$widgetNames}, "${name}_month");
            push(@{$widgetChildren}, [$months[$lm-1]]);
        }

        # Generate year axis
        if ($ly > 1 || $hy > 1){
            my $lastYear;
            for (my $i = 0; $i < $size; $i++){
                my ($nextYear) = $date->getYMD;
                $nextYear = sprintf "%.4d", $nextYear; # Sigh. Ferret parser...
                if (! $lastYear || $nextYear ne $lastYear){
                    push(@{$children}, $nextYear);
                    $lastYear = $nextYear;
                }
                $date = $date->addDelta(@step);
            }
            $self->genWidget($name, $children);
            push(@{$widgetNames}, $name);
            push(@{$widgetChildren}, $children);
        }

        # Generate hour axis (if needed)
        if ($sh > 0){
            my @hours = ();
            my ($curr) = $date->getHMS;
            my $start = $curr;
            do {
                $curr += $sh;
                $curr %= 24;
                push(@hours, $curr);
            } while ($curr != $start);
            @hours = sort {$a <=> $b} @hours;
            $self->genWidget($name . "_hours", \@hours);
            push(@{$widgetNames}, $name . "_hours");
            push(@{$widgetChildren}, \@hours);
        }


        $AxisWidgets{$id} = [$widgetNames, $widgetChildren];
    }
}

##
# Create an axis widget.  The type of widget to create (Time or Data)
# is determined from the axis->{type} attribute.
#
# @param name identifier for the widget
# @param axis LAS::ServletDB::Axis object
sub genAxisWidget {
    my ($self,$name, $axis) = @_;
    my @children = $axis->getChildren;
    my $wname = $name . "_widget";
    my $id = $axis->getName;
    my $type = $axis->getAttribute("type");
    my $units = $axis->getAttribute("units");
    if (@children){
        if ($type eq "t"){      # Don't need units in time axis widgets
            $units = undef;
        }
        $self->genWidget($wname, \@children, $units, ($type eq "t"), 1);
        $AxisWidgets{$id} = [[$wname], [\@children]];
    } else {
        if ($type eq "t"){
            $self->genTimeAxisWidget($wname, $axis, $units);
        } else {
            $self->genDataAxisWidget($wname, $axis, $units);
        }
    }
}

my $UIcount = 0;
sub getUIProps {
    my ($self,$obj) = @_;
    my $props = $obj->getProperties('ui');
    $props = $obj->getDataset->getProperties('ui') if ! $props;
    $props = $obj->getConfig->getProperties('ui') if ! $props;
    my $jsVarName = $obj->getAttribute('js');
    $jsVarName = $obj->getDataset->getAttribute('js') if ! $jsVarName;
    $jsVarName = $obj->getConfig->getAttribute('js') if ! $jsVarName;
    return ($props,$jsVarName);
}

sub genUI {
    my ($self, $obj, $gen) = @_;
    my ($props,$jsVarName) = $self->getUIProps($obj);
    my $servletConfig = $self->{servletConfig};
    if (! ($props || $jsVarName)){
        return $servletConfig->addUIByName($gen, 'DefaultUI');
    }
    my $default;
    if ($props){
        my $uiprop = $props->{default};
        # Why all this nonnsense?  Just parse
        # the string.  Geez.
        #my $url = new URI::URL($uiprop);
        #if ($url->scheme ne 'file' || $url->path ne 'ui.xml'){
        #    die "UI property in '$url' must refer to file:ui.xml";
        #}
        #$default = $url->frag;
        #die "'$url' missing reference to UI default" if ! $default;
        #$default =~ s/^#//;
        my @parts = split("#", $uiprop);
        $default = $parts[1];
    }
    $default = defined($default) ? $default : $jsVarName;
    my $name = 'UI' . $UIcount++;
    return $servletConfig->addUIByName($gen, $default);
}

sub new {
    my $class = shift;
    my $self = { count => 0};
    $self->{outtemplate} = shift;
    $self->{menusInitialized} = 0;
    bless $self, $class;
}

sub start($) {
    my $self = shift;
#
# Standard month and month day widgets
#
    my @days = map {sprintf("%.2d", $_)} (1..31);
    $self->genWidget("_month_days_axis", \@days);
    $self->genWidget ("_month_with_days_axis",\@day_months);
    $self->genWidget ("_month_axis",\@months);
}


my %ValidateHash = ();
sub printValidateError {
    my ($self, $mess) = @_;
    &LAS::printwarnln($mess) if !$ValidateHash{$mess};
    $ValidateHash{$mess} = 1;
}
        
##
# Validate consistency of XML
# <ul>
#   <li>LiveMap images exist
#   <li>Any custom UI components exist
#   <li>HTML metadata documents exist
#   <li>Ferret init scripts exist
# </ul>
#
# @param $var LAS::Variable object
my %ValidateCache = ();
sub validate {
    my ($self, $var) = @_;
    my $dset = $var->getDataset;
    my $docURL = $dset->getAttribute("doc");
    if ($docURL && !$ValidateCache{$docURL}){
        if (!&LAS::validateURL($docURL, '../../WebContent')){
            $self->printValidateError("*** Can't find documentation URL set by dataset 'doc' attribute: '$docURL'");
        }
        $ValidateCache{$docURL} = 1;
    }
    my $props = {};
    $props = &LAS::mergeProperties($props,
                                   scalar $dset->getConfig->getProperties('custom'),
                                   scalar $dset->getProperties('custom'),
                                   scalar $var->getProperties('custom'));
    my $customURL = $props->{url};
    $customURL = "" if ! defined($customURL);
#
# Validate customURL
# Removed for servlets since there can be an unknown mapping between
# URL and servlet template file
#
#    if ($customURL){
#       $self->printValidateError("*** Can't find custom UI file: $customURL")
#           if ! -f "../ui/$customURL";
#    }
#
# Validate Ferret init_scripts
#
    if (!$self->{isPackage}){
        $props = {};
        $props = &LAS::mergeProperties($props,
                                       scalar $dset->getConfig->getProperties('ferret'),
                                       scalar $dset->getProperties('ferret'),
                                       scalar $var->getProperties('ferret'));
        my $initScript = $props->{init_script};
        $initScript = "" if ! defined($initScript);
        if ($initScript){
            $initScript .= ".jnl" if $initScript !~ /\.jnl$/;
            my $custom = $LAS::Server::Config{custom_include};

            my @searchList = ();
            push(@searchList,$custom) if $custom;
            @searchList = (@searchList,split(/\s+/, $ENV{FER_GO}));
            my $found = 0;
            foreach my $dir (@searchList){
                if (-f "$dir/$initScript"){
                    $found = 1;
                    last;
                }
            }
            if (!$found){
                $self->printValidateError("*** Can't find Ferret init script: $initScript");
            }
        }
    }
}


##
# Generates perl hash representations of all the Axes and axis widgets
# for variables contained in a dataset.
#
# @param $dset LAS::Dataset object
sub genAxesAndWidgets {
    my ($self, $dset) = @_;
#
# Generate widgets and axes
#
    my $servletConfig = $self->{servletConfig};
    my %axes = $dset->getInstances('axis');
    foreach (keys %axes){
        my $axis = $axes{$_};
        my $type = $axis->getAttribute("type");
        my $units = $axis->getAttribute("units");
        $units = "" if $type eq 't';
        $units = "" if ! defined($units);
        my $id = $axis->getName;
        my $var = $_;
        $var =~ s/\//_/g;
#
# Widgets
#
        $self->genAxisWidget($var, $axis);
# 
# Axes
#
        my ($title, $lo, $hi, $category);
        $category = 'normal';
        if ($type eq "t"){
            $title = "Time";
            my $lod = new TMAP::Date($axis->getLo);
            my $hid = new TMAP::Date($axis->getHi);
            if ($lod->isOK && $hid->isOK){
                $category = 'rtime';
                my ($year) = $lod->getYMD;
                my ($hiyear) = $hid->getYMD;
                $category = 'ctime' if ( defined $year && $year == 1 && $hiyear == 1 ); # 040429.clc. added defined check
            } 
        } else {
            if ($type eq 'z'){
                $title = "Depth";
            } else {
                $title = "";
            }
        }

# TODO -- support widget initialization for depth/height as well as date
        my @indexes = ($axis->getAttribute("display_lo"),
                       $axis->getAttribute("display_hi"));
        my $default = $axis->getAttribute("default");
        $default = "first" if ! defined($default);
        $indexes[0] = "first" if ! defined($indexes[0]);
        $indexes[1] = "last" if ! defined($indexes[1]);
        my @initValues = ();
        foreach my $index (@indexes){
          if (0) {
            my $indexDate = new TMAP::Date($index);
            die "display attribute for axis must be 'first' or 'last' or a date"
                if (!($index eq 'first' || $index eq 'last' ||
                      $indexDate->isOK));
# DONE -- Support initialization for hours/min/seconds.
            my @tmpList;
            if ($indexDate->isOK){
                my @YMD = reverse($indexDate->getYMD(1));
                my @HMS = $indexDate->getHMS;
                @tmpList = (@YMD, @HMS);
                #$tmpList[0] = int($tmpList[0]); # Convert day to int
                #$tmpList[3] = int($tmpList[3]); # Convert hour to int
            } else {
                @tmpList = ($index);
            }

            push(@initValues, \@tmpList);
          }
# DONE -- Support initialization for hours/min/seconds.
            my @tmpList;
            if ($index eq 'first' || $index eq 'last') {
              @tmpList = ($index);
            } else {
              my $indexDate = new TMAP::Date($index);
              if ($indexDate->isOK){
                my @YMD = reverse($indexDate->getYMD(1));
                my @HMS = $indexDate->getHMS;
                @tmpList = (@YMD, @HMS);
                #$tmpList[0] = int($tmpList[0]); # Convert day to int
                #$tmpList[3] = int($tmpList[3]); # Convert hour to int
              } else {
                die $indexDate->getError;
              }
            }
            push(@initValues, \@tmpList);
        }

        my @widgetNames = ();
        my @widgetValues = ();
        if ($AxisWidgets{$id}){
            my @widgetInfo = @{$AxisWidgets{$id}};
            @widgetNames = @{$widgetInfo[0]};
            @widgetValues = @{$widgetInfo[1]};
        }
        my $servletAxis =
            $servletConfig->addAxis($var,$axis->getLo, $axis->getHi, $axis->getSize, $title, $units, $type, $category);
        my $vcount=0;
        my $firstLoInit = @{$initValues[0]}[0];
        my $firstHiInit = @{$initValues[1]}[0];
        my @initLoValues= @{$initValues[0]};
        my @initHiValues= @{$initValues[1]};
        foreach my $widget (@widgetNames){
            my ($initLoValue, $initHiValue);
            my $initLo = shift @initLoValues;
            $initLo = $firstLoInit if ! defined($initLo);
            my $initHi = shift @initHiValues;
            $initHi = $firstHiInit if ! defined($initHi);
            my $axisValues = shift @widgetValues;
            $initLoValue = 0;
            if ($initLo eq 'last'){
                $initLoValue = scalar @{$axisValues} - 1;
            } else {
                my ($match, $i); $i=0;
                foreach my $axisValue (@{$axisValues}){
                    if ($initLo eq $axisValue){
                        $initLoValue = $i;
                        last;
                    }
                    $i++;
                }
            }
            $initHiValue = scalar @{$axisValues} - 1;
            if ($initHi eq 'first'){
                $initHiValue = 0;
            } else {
                my ($match, $i); $i=0;
                foreach my $axisValue (@{$axisValues}){
                    if ($initHi eq $axisValue){
                        $initHiValue = $i;
                        last;
                    }
                    $i++;
                }
            }
            $servletAxis->addWidget($widget, $initLoValue, $initHiValue,
                                    $default);
            $vcount++;
        }
    }
}

##
# Create a perl hash representation of a LAS user interface category.
#
# @param $servletConfig LAS::ServletDB::Config object
# @param $config LAS::Config object
# @param $ui LAS::UI::Generator object
# @param $category LAS::Category object
# @param $parent parent category
# @param @in_includes list of UI custom template pieces to include (e.g. "variable_include_header")
sub generateCategory {
    my ($self, $servletConfig, $config, $ui, $category, $parent,
        @in_includes) = @_;
    my $name = $category->getAttribute("name");
    my $id = $category->getName;
    my $type = $category->getType;
    $name = $id if ! $name;

    my @categories = $category->getCategories;
    my @variables = $category->getVariables;
    return if $#variables < 0 && $#categories < 0;

    my @contributors = $category->{element}->getElementsByTagName("contributor",0);

#    warn $#variables," for category ", $name;
    die "A category can't contain both subcategories and variables (yet)"
        if ($#variables >= 0 && $#categories >= 0);

# Set up metadata
    my $metadata = "";
    $metadata = new LAS::ServletDB::MetaData($category);
    my $docURL = $category->getAttribute("doc");
    $metadata->addDocUrl($docURL) if $docURL;

# Get any templates to include
    my @includes = @in_includes;
    my $count = 0;
    foreach my $include
       (qw(category_include variable_include constrain_include
           category_include_header variable_include_header constrain_include_header)){
        my $att = $category->getAttribute($include);
        $includes[$count] = $att if $att;
        $count++;
    }
#
# Before heading off on a recursive loop to generate the sub-categories
# recurs through the sub-categories and find out if any of the variables
# at the bottom of this hierarchy are of grid_type="scattered" or "regridded".
#
   my $grid_type;
   my $subc;
   my $regular = 0;
   my $scattered = 0;
   my $regridded = 0;
   if ($#categories >= 0) {
      foreach $subc (@categories) {
         my $sub_grid_type = getGridType($subc);
         if ( $sub_grid_type eq "regular" ) {
            $regular++;
         }
         elsif ( $sub_grid_type eq "scattered" ) {
            $scattered++;
         }
         elsif ($sub_grid_type eq "regridded" ) {
            $regridded++;
         }
      }
      if ( $regular > 0 ) {
         $grid_type = "regular";
      } elsif ( $scattered > 0 ){
         $grid_type = "scattered";
      } elsif ( $regridded > 0 ){
         $grid_type = "regridded";
      }
         
   }
   # This category has no sub-categories.  Check to see if it has variables
   # with grid_type="scattered".
   else {
         $grid_type = getGridType($category);
   }

# Generate sub categories
    my $servletDataset;
    if ($parent){
        $servletDataset = $parent->addCategory($name, $metadata, $grid_type, $type, @includes);
    } else {
        $servletDataset = $servletConfig->addCategory($name, $metadata, $grid_type, $type, @includes);
    }
    if ($#contributors >= 0) {
       my $contrib;
       foreach $contrib (@contributors) {
          my $url=$contrib->getAttribute('url');
          my $name=$contrib->getAttribute('name');
          my $role=$contrib->getAttribute('role');
          $servletDataset->addContributor("0", $url, $name, $role);
       }
    }
    if ($#categories >= 0){
        foreach my $subcat (@categories){
            $self->generateCategory($servletConfig, $config, $ui, $subcat,
                                    $servletDataset, @includes);
        }
        return;
    }
#
# Screen out bad variables (no X or Y axes)
#
    my @children = validateAxes(@variables);
#
# Find first variable (use to get axes)
#
    my $firstvar = "";
    foreach my $var (@children){
        if (ref($var) eq 'LAS::Variable'){
            $firstvar = $var;
            last;
        }
    }
    die "No variables defined for: ", $category->getAttribute('name')
        if ! $firstvar;

#
# Use axes of first variable for initial axis info
# Composite variables will use this axis, while standard variables
# use the axis associated with the variable.
#   
    my @axes = $firstvar->getChildren;

    foreach my $var (@children){
# Validate the variable
        $self->validate($var);
        my $vname = $var->getAttribute("name");
#
# Inform the poor befuddled user if the js attribute is defined,
# as this indicates that they are -- or were -- using custom JavaScript
# Since we no longer use much JavaScript, this is rather pointless...
#
        my $jsVar = $var->getAttribute("js");
        $jsVar = $var->getDataset->getAttribute("js") if ! $jsVar;
        if ($jsVar){
            print STDERR <<EOL;
Warning: Variable '$vname' uses the 'js' attribute.
    Custom JavaScript is no longer supported.
EOL
            } 
#
# Create array to pass to variable constructor
#
        my $dsetid = $var->getDataset->getName;
        $name = $id if ! $name;
        my @varids = ();
        my $class = ref($var);
        if ($class eq 'LAS::Variable'){
            @axes = $var->getChildren;
            push(@varids, $dsetid . "/variables/" . $var->getName);
        } elsif ($class eq 'LAS::CompVar'){             # LAS::CompVar
            my @childVars = $var->getChildren;
            @axes = $childVars[0]->getChildren;
            foreach (@childVars){
                push(@varids, $dsetid . "/variables/" . $_->getName);
            }
        } else {
            die "Unknown class: $class";
        }
# Generate any user interface info that might be defined
        my $varui = $self->genUI($var, $ui);

#
# Get UI properties
#
        my $props = {};
        $props = &LAS::mergeProperties($props,
                                       scalar $config->getProperties('custom'),
                                       scalar $category->getProperties('custom'),
                                       scalar $var->getProperties('custom'));
#
# Get ferret properties
#
        my $fprops = {};
        my $dset = $var->getDataset;
        $fprops = &LAS::mergeProperties($fprops,
                                       scalar $dset->getConfig->getProperties('ferret'),
                                       scalar $dset->getProperties('ferret'),
                                       scalar $var->getProperties('ferret'));
#
# If this variable contains a "database_access" property assume
# it is scattered data.  If this variable contains a "cf_map_file"
# ferret property, assume it is regridded cuvilinear data.
# Otherwise assume it's gridded data.
#
        
        my $dbaccess = $var->getDataset->getProperties('database_access');
        my $cf_map_file = $fprops->{cf_map_file};
        my $grid_type;
        if ( ! defined($dbaccess) && ! defined($cf_map_file) ) {
           $grid_type = "regular";  # Equilateral and equiangular
        }
        elsif ( defined($dbaccess) ) {
           $grid_type = "scattered";  # Occur irregularly or at random
        }
        else {
            $grid_type = "regridded"; # Uses a mapping variable from another dataset
        }
        my $customURL = $props->{url};
        $customURL = "" if ! defined($customURL);
        my $inst = $servletConfig->addInstitution($var);
        my $units = $var->getAttribute("units");
        $units = "" if ! defined $units;

        my $dods = "";
        my $fdsurl;
        my $isComposite = 0;
        $isComposite = 1 if ref($var) eq 'LAS::CompVar';
        if (ref($var) ne 'LAS::CompVar' && $grid_type ne "scattered"){

           #
           # Set up a DODS url of each and every variable.
           # Always use the FDS URL to avoid problems with
           # the case of the variable name.
           #
            my $padre = $var->getDataset;
            my $dpath = $padre->getName;
            my $vpath = $var->getName;
            my $url = $var->getURL;
            $url =~ s/#.*//g; 
            $url =~ s/http:\/\///g;
            $url =~ s/file:\/\///g;
            $url =~ s/:/_/g;
            $url =~ s/\//_/g;
            $url =~ s/^_//g;
            #
            # Hack off the leading "/" so it becomes a relateive URL.
            # Tack on the magic that points to the FDS.
            if ( $LAS::Server::Config{proxy} eq "yes" ) {
               $fdsurl = "http://".$LAS::Server::Config{serverhost}."/thredds/dodsC".$LAS::Server::Config{uipath};
            }
            else {
               $fdsurl = "http://".$LAS::Server::Config{tomcathost}.":".$LAS::Server::Config{tomcatport}."/thredds/dodsC".$LAS::Server::Config{uipath};
            }
            # Glue it together with the XML path name.
            $dods = $fdsurl."/".$dpath."/"."data_".$url.".jnl";
        }

        my $vtype="";
        if ( $type eq 'c' ) {
           $vtype = 'v';
        }
        else {
           $vtype = 'p';
        }

        my $servletVar = $servletDataset->addVariable($vname, $customURL,
                                                      $varui, \@varids,
                                                      $inst, $units,$dods,
                                                      $isComposite, $grid_type, $vtype, 
                                                      @includes);
#
# Sort axes by type (avoids problems with YX slices vs. XY)
#
        my %sortHash = (x => 0, y => 1, z => 2, t =>3);
        @axes = sort {$sortHash{$a->getAttribute('type')} cmp
                          $sortHash{$b->getAttribute('type')}} @axes;
        foreach my $axis (@axes){
            if ($axis->getLo eq "" || $axis->getHi eq ""){
                &LAS::printerrln("Error: empty axis: ",
                                 $axis->getName, '; ignored');
            } else {
                my $aname = $axis->getFullPath;
                $aname =~ s/\//_/g;
                my $type = $axis->getAttribute('type');
                $servletVar->addAxis($aname);
            }
        }
    }
}

##
# Determines whether any of the variables in this category or one
# of its sub-categories has a "regular" grid type (rectilinear XYZT),
# a "scattered" grid_type (which occurs if this category/dataset 
# has the <b>database_access</b> property set), or a "regridded"
# grid_type (which checks if curvilinear data needs a mapping 
# variable from a second dataset, the ferret property <b>cf_map_file</b>.
# A return of "none" indicates no variables at all -- an error.
# <p>
# This method is used to determine whether a category should be 
# made available for comparison.  
#
# @param kids children of this category
# @return gridType ["regular"|"scattered"|"regridded"|"none"]
sub getGridType {
   my @categories = @_;
   my $grid_type;
   my @kids;
   undef @kids;
   foreach my $cat  (@categories) {
      my $class = ref($cat);
      if ($class eq 'LAS::Variable'){
         my $dba;
         undef $dba;
         $dba = $cat->getDataset->getProperties("database_access");
#
# Get ferret properties
#
         my $fprops = {};
         my $dset = $cat->getDataset;
         $fprops = &LAS::mergeProperties($fprops,
                                       scalar $dset->getConfig->getProperties('ferret'),
                                       scalar $dset->getProperties('ferret'),
                                       scalar $cat->getProperties('ferret'));
        
         my $cf_map_file = $fprops->{cf_map_file};
         if ( !(defined($dba)) && !(defined($cf_map_file)) ) {
            $grid_type = "regular";
            # Category has at least one regular grid.  That's enough
            # to classify it as "regular" and include it in the comparison
            # menu hierarchy.
            last;
         }
         elsif ( !(defined($cf_map_file)) ) {
            $grid_type = "scattered";
         }
         else {
            $grid_type = "regridded";
         }
       }
       else {
          @kids = $cat->getCategories;
          if ( $#kids >= 0 ) {
             $grid_type = getGridType(@kids);
          }
          else {
             @kids = $cat->getVariables;
             if ( $#kids >= 0 ) {
                $grid_type = getGridType(@kids);
             }
             else {
                # Category has no variables at all!  grid_type is nonsense.
                $grid_type = "none";
             }

          }
       }
    }
    return $grid_type;
}
    
##
# Create the perl hash representation of the datasets specified
# in las.xml.
#
# @param $config LAS::Config object
# @param $dsetName (if present) only work on datasets whos longName matches this string
sub generateDatasets {
    my ($self, $config, $dsetName) = @_;
    my @dsets = $config->getChildren;
    foreach my $dset (@dsets){
      my $longName = $dset->getLongName;
      if (!defined($dsetName) or ($dsetName eq $longName)) {
        my $count = $self->{count}++;
        print STDERR "Generating dataset ", $longName, "\n";
        $self->start;

        #
        # Force initialization of variables (through LAS::Dataset::getChildren)
        #
        my @origChildren = $dset->getChildren; # Standard variables
        foreach my $kid (@origChildren){
            $kid->getChildren;
        }

        #
        # Generate axes and widgets
        #
        $self->genAxesAndWidgets($dset);
      }
    }

}

sub generatePathIndex {
   my ($self,$db) = @_;
   my $statement = qq(insert into PathIndex (path) select distinct path from Category where type='v');
   $db->{dbh}->do($statement);
}

##
# Parse the las.xml file and use that information to generate
# components and populate the database tables that will be used
# by the LAS UI servlet to present the user interface.
#
# @param $config LAS::Config object
# @param $isPackage true if this represents a package
# @param $xmlfile location of las.xml file
# @param $ui LAS::UI::Generator object
# @param $db LASDB::MySQL::Servlet object
# @param $multi true if any LAS packages are installed
sub generate {
    my ($self,$config,$isPackage,$xmlfile,$ui,$db, $multi)  = @_;
    my $servletConfig;
    $self->{isPackage} = $isPackage;

# Hack to get package id (if a package)
# Only full packages (not UI packages) should specify a packageid
    my $packageid = "";
    if ($xmlfile =~ /packages\/(.+)\/(.+)/){
        my $dir = dirname($xmlfile);
        my $manifestFile = $dir . "/manifest.xml";
        die "Invalid package in $dir: missing or can't read $manifestFile"
            if ! -r $manifestFile;
        my $parser = new LAS::Parser($manifestFile);
        my $manifest = new LAS::PackageManifest($parser);
        my $isFull = ($manifest->getAttribute('isfull') eq "1");
        $packageid = $1 if $isFull;
    }
    $self->{db} = $db;

    
    my $xmlbase = basename($xmlfile);
    my @ops = $config->getOps;
    #
    # If there is a top level institution defined, get it and check for 
    # contact information.
    my $top_inst = $config->getInstitution;
    my $contact = $top_inst->getAttribute("contact");
    if ( !defined($contact) ) {
       $contact="";
    }
    $self->{servletConfig} = $servletConfig =
        new LAS::ServletDB::Config($config, $packageid, $ops[0]->getURL, $contact,
                                   "file:$xmlbase");
    $self->generateDatasets($config);
#
# Generate categories
#
    my $catConfig = new LAS::Category::Config($config, $multi);
    foreach my $category ($catConfig->getCategories){
        my $c = $category->getLongName();
        $category->setContext;
        $self->generateCategory($servletConfig, $config, $ui, $category);
    }

    $servletConfig->serialize($db);

    $self->generatePathIndex($db);

    my $bparser = new LAS::Parser::Browser('browsers.xml');
    my $rootElement = $bparser->getRoot->getDocumentElement;
    my @browsers = $rootElement->getElementsByTagName("browser", 0);
    foreach my $browser (@browsers){
       my $agent = $browser->getAttribute("agent");
       my $applet = $browser->getAttribute("applet");
       if ( $applet eq "true" ) {
          $applet="1";
       }
       else  {
          $applet="0";
       }

       my $myBrowser = new LAS::ServletDB::Browser($agent, $applet);

       $myBrowser->serialize($db);
    }
}

##
# @private
# TODO: The modify routine is incomplete at the time of our LAS 6.4 
# TODO: code freeze.  It is intended that this routine will enable
# TODO: the modification of LAS tables to attach a named dset/variable
# TODO: to a newly generated time axis and remove the old axis/widget
# TODO: entries.
# @param $config LAS::Config object
# @param $xmlfile location of las.xml file
# @param $db LASDB::MySQL::Servlet object
# @param $multi true if any LAS packages are installed
# @param $dsetName dataset Title
sub modify {
    my ($self,$config,$xmlfile,$db,$multi,$dsetName)  = @_;
    my $servletConfig;

    $self->{db} = $db;

    my $packageid = "";
    my $xmlbase = basename($xmlfile);
    my @ops = $config->getOps;
    #
    # If there is a top level institution defined, get it and check for 
    # contact information.
    my $top_inst = $config->getInstitution;
    my $contact = $top_inst->getAttribute("contact");
    if ( !defined($contact) ) {
       $contact="";
    }
    $self->{servletConfig} = $servletConfig =
        new LAS::ServletDB::Config($config, $packageid, $ops[0]->getURL, $contact,
                                   "file:$xmlbase");
    $self->generateDatasets($config,$dsetName);

print "modify: dumping axesByName\n";
    foreach my $axis (%{$servletConfig->{axesByName}}) {
      my $type = ref($axis);
      if ($type eq 'LAS::ServletDB::Axis'){
        $axis->dump();
        $axis->serialize($db,"9");
      } else {
        print "axis = \"$axis\"\n";
      }
    }

#print "modify: dumping widgetsByName\n";
#    foreach my $widget (@{$servletConfig->{widgetsByName}}) {
#      $axis->dump();
#    }

    #$servletConfig->serialize($db);
}

package LAS::Category::Filter::Dataset;
package LAS::Category::Filter::Variable;
package LAS::Category::Filter;

# @param $config LAS::Category::Config object
sub new {
    my ($proto, $config, $xml) = @_;
    my $self = {
        config => $config,
        children => []
    };
    my $class = ref($proto) || $proto;

    my $children = $xml->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
#TODO -- Don't allow mixed <filter>, <category> children
    while ($iter->hasMore){
        my $e = $iter->next;
        die "Only <filter> and <category> can be child of <filter>"
            if $e->getTagName ne 'filter' && $e->getTagName ne 'category';
        push(@{$self->{children}},
             &LAS::Category::Filter::getInstance($config, $e));
    }
    my $count = 0;
    my $equals = $xml->getAttribute("equals");
    if ($equals){
        $self->{type} = 'equals';
        $self->{value} = $equals;
        $count++;
    }
    my $contains = $xml->getAttribute("contains");
    if ($contains){
        $self->{type} = 'contains';
        $self->{value} = $contains;
        $count++;
    }
    my $contains_tag = $xml->getAttribute("contains-tag");
    if ($contains_tag){
        $self->{type} = 'contains-tag';
        $self->{value} = $contains_tag;
        $count++;
    }
    die "Can't only specify one of 'contains','equals','contains-tag' attribute for <filter>"
        if $count > 1;
    $self->{type} = 'any' if ! defined $self->{type};
    bless $self,$class;
}

sub getCategories {
    my $self = shift;
    my @categories = ();
    foreach my $child (@{$self->{children}}){
        my $type = ref($child);
        if ($type eq 'LAS::Category'){
            push(@categories, $child);
        } else {
            push(@categories, $child->getCategories);
        }
    }
    return @categories;
}

sub getInstance {
    my ($config, $xml) = @_;
    die "Missing LAS::Config argument" if ! $config;
    die "First argument must be LAS::Config" if ref($config) ne "LAS::Category::Config";
#    warn "XML = ", $xml->toString;
    my $filter;
    if ($xml->getTagName eq 'category'){
        $filter = new LAS::Category($config, $xml);
    } else {
        my $filterType = $xml->getAttribute('action') ||
            die "<filter> missing 'action' attribute";
        if ($filterType eq 'apply-dataset'){
            $filter = new LAS::Category::Filter::Dataset($config, $xml);
        } elsif ($filterType eq 'apply-variable'){
            $filter = new LAS::Category::Filter::Variable($config, $xml);
        } else {
            die "Unknown type of action attribute for <filter>: $filterType";
        }
    }
    return $filter;
}

sub apply {
    my ($self, $type, @contextNodes) = @_;
    my $config = $self->{config};
    @contextNodes = () if ! @contextNodes;

    my ($classInstances);
    if ($type eq 'dataset'){
        $classInstances = $config->{datasetInstances};
    } elsif ($type eq 'variable'){
        $classInstances = $config->{variableInstances};
    } else {
        die "Unknown filter type: $type";
    }

# Search rules:
# Context node          Filter type  Condition
# -------------------------------------------------------------------
# LAS::Dataset          dataset       dataset matches condition
# LAS::Dataset          variable      variable contained by dataset matches
# LAS::Variable/CompVar dataset       dataset matching contains variable
# LAS::Variable/CompVar variable      variable matches condition  

    my @newnodes = ();
    my @searchNodes;
    if ($#contextNodes < 0){
        @searchNodes = @{$classInstances};
    } else {
        @searchNodes = @contextNodes;
    }
#    warn "Type: $type Context nodes = ", @contextNodes;
#    warn "Type: $type Search nodes = ", @searchNodes;
    
                                # Use hash to prevent duplicates
    my %nodeHash;
    foreach my $node (@searchNodes){
        my $class = ref($node);
        if ($type eq 'dataset' && $class eq 'LAS::Dataset'){
            $nodeHash{$node} = [$node, $node];
        } elsif ($type eq 'variable' && $class eq 'LAS::Dataset'){
            map {$nodeHash{$_} = [$_,$_]} $node->getVariables;
        } elsif ($type eq 'dataset' && ($class eq 'LAS::Variable' ||
                                         $class eq 'LAS::CompVar')){
            $nodeHash{$node} = [$node->getDataset, $node];
        } elsif ($type eq 'variable' && ($class eq 'LAS::Variable' ||
                                         $class eq 'LAS::CompVar')){
            $nodeHash{$node} = [$node, $node];
        } else {
            die "Internal: Unknown type/class combo: $type/$class";
        }
    }


    my ($op,$status);
    my $value = $self->{value};
    if ($self->{type} eq 'contains'){
        $op = q{$status = $node->getLongName =~ /$value/};
    } elsif ($self->{type} eq 'contains-tag'){
        $op = q{$status = $node->getName =~ /$value/};
    } elsif ($self->{type} eq 'equals'){
        $op = q{$status = $node->getLongName eq "$value"};
    } elsif ($self->{type} eq 'any'){
        $op = q{$status = 1};
    } else {
        die "Unknown type:", $self->{type};
    }
    foreach my $key (keys %nodeHash){
        my ($node, $real_node) = @{$nodeHash{$key}};
        eval "$op";
        die $@ if $@;
        push(@newnodes, $real_node) if $status;
        $status = 0;
    }

    my @children = @{$self->{children}};
    my @allnodes;
    if ($#children < 0){
        @allnodes = @newnodes;
    } elsif ($#newnodes >= 0){
        foreach my $childFilter (@children){
            if (ref($childFilter) eq 'LAS::Category'){
                $childFilter->setContext(@newnodes);
            } else {
                my @childnodes = $childFilter->apply($self, @newnodes);
                push(@allnodes, @childnodes);
            }
        }
    }

    return @allnodes;
}

package LAS::Category::Filter::Dataset;
@LAS::Category::Filter::Dataset::ISA = qw(LAS::Category::Filter);

sub apply {
    my ($self, @contextNodes) = @_;
    my $config = $self->{config};
    return $self->SUPER::apply('dataset',@contextNodes);
}



package LAS::Category::Filter::Variable;
@LAS::Category::Filter::Variable::ISA = qw(LAS::Category::Filter);

sub apply {
    my ($self, @contextNodes) = @_;
    my $config = $self->{config};
    my @variables_context = ();
    foreach my $node (@contextNodes){
        if (ref($node) eq 'LAS::Dataset'){
            push(@variables_context, $node->getVariables);
        } else {
            push(@variables_context, $node);
        }
    }
    return $self->SUPER::apply('variable', @variables_context);
}


##
# Subclass of LAS::Container that represents a category
# in the LAS XML hierarchy.  A category may contain sub-categories
# or variables.  Every 'dataset' in the LAS XML is actually
# a LAS::Category.
package LAS::Category;
@LAS::Category::ISA = qw(LAS::Container);
$LAS::Category::Config = undef;
use vars qw($Config);
#
# $e can be an XML DOM element or, in an extremely ugly hack,
# a LAS::Dataset. KIDS! Don't do this at home.

##
# Create a new LAS::Category
#
# @param $config LAS::Category::Config object
# @param $e XML DOM element (or LAS::Dataset)
sub new {
    my ($proto, $config, $e) = @_;
    my $class = ref($proto) || $proto;
    $Config = $config;
    my $isDataset = ref($e) eq 'LAS::Dataset';
    my $element =  $isDataset ? $e->getElement : $e;
    my $self = $class->SUPER::new(undef, $element);
    $self->{dataset} = $e if $isDataset;
    bless $self, $class;
}

##
# Initialize all categories by recusively descending
# the LAS XMl hierarchy.
# <p>
# The LAS::Category::Config ojbect required for the initialization
# of a LAS::Category object is used in this line in thise routine:
# <pre>
#   LAS::Category::Filter::getInstance($Config, $e));
# </pre>
sub _initialize {
    my $self = shift;
    $self->{categories} = [];
    $self->{filters} = [];
    my $children = $self->{element}->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $e = $iter->next;
        my $name = $e->getTagName;
        if ($name eq 'filter'){
            push(@{$self->{filters}},
                 LAS::Category::Filter::getInstance($Config, $e));
        } elsif ($name eq 'category'){
            my $regular_cat = new LAS::Category($Config, $e);
            $regular_cat->setType('c');
            push (@{$self->{categories}}, $regular_cat);
        } 
    }
}

sub setContext {
    my ($self, @contexts) = @_;
    @contexts = () if ! @contexts;
    $self->{context} = \@contexts;
#    warn "setContext for:", $self->getAttribute("name"),
#    ":", $#contexts;
    foreach my $category (@{$self->{categories}}){
        $category->setContext(@contexts);
    }
    foreach my $filter (@{$self->{filters}}){
        $filter->apply(@contexts);
    }
}

sub getCategories {
    my @results = ();
    foreach my $filter (@{$_[0]->{filters}}){
        push(@results, $filter->getCategories);
    }
    return (@results, @{$_[0]->{categories}});
}

sub setType {
   my ($self, $type) = @_;
   $self->{type} = $type;
}

sub getType{
   my $self = shift;
   return $self->{type};
}

sub getVariables {
    my @variables = ();
    foreach my $filter (@{$_[0]->{filters}}){
#       warn "getVariables:", $_[0]->getAttribute("name"), ":",
#       $#{$_[0]->{context}};
        push(@variables, $filter->apply(@{$_[0]->{context}}));
    }
    if ($_[0]->{dataset}){
        die "Internal: dataset hack can't have any filters"
            if $#variables >= 0;
        @variables = ($_[0]->{dataset});
    } else {
#    warn "getVariables: contextNodes", @{$_[0]->{context}};
        @variables = @{$_[0]->{context}} if $#{$_[0]->{filters}} < 0;
    }
    my @rval = map {ref($_) eq 'LAS::Dataset' ? $_->getVariables : $_}
                  @variables;
    return @rval;
}

##
# Subclass of LAS::Container that represents a category
# in the LAS UI.  A category may contain sub-categories
# or variables.  Every dataset in the LAS UI is actually
# a LAS::Category.
package LAS::Category::Config;
@LAS::Category::Config::ISA = qw(LAS::Container);
$LAS::Category::Config::LasConfig = undef;
use vars qw($LasConfig);

##
# Create a new LAS::Category::Config
#
# @param $lasConfig LAS::ServletDB::Config object 
# @param $multi true if any LAS packages are installed
sub new {
    my ($proto, $lasConfig, $multi) = @_;
    my $class = ref($proto) || $proto;
    my $rootElement = $lasConfig->getCategories($multi);
    die 'Root tag must be <las_categories>'
        if $rootElement->getTagName ne 'las_categories';
    push(@_, $rootElement);
    $LasConfig = $lasConfig;
    my $self = $class->SUPER::new(undef,$rootElement);
    bless $self, $class;
}

##
# Sets up dataset and variable lists based on the children
# of this element.
sub _initialize {
    my $self = shift;
    $self->{lasConfig} = $LasConfig;
    $self->{categories} = [];

# Set up dataset, variable lists
    my (@Dinst, @Vinst);
    foreach my $dset ($LasConfig->getChildren){
        push(@Dinst, $dset);
        foreach my $var ($dset->getVariables){
            push(@Vinst, $var);
        }
    }
    $self->{datasetInstances} = \@Dinst;
    $self->{variableInstances} = \@Vinst;

    my $children = $self->{element}->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
        my $e = $iter->next;
        die "All children of <las_categories> must be <category>"
            if $e->getTagName ne 'category';
        my $regular_category = new LAS::Category($self, $e);
        $regular_category->setType('c');
        push (@{$self->{categories}}, $regular_category);
    }

# Default to all datasets for backward compatibility if no categories
# have been specified. 
    if (scalar(@{$self->{categories}}) == 0){
        foreach my $dset (@Dinst){
            my $ctgry = new LAS::Category($self, $dset);
            $ctgry->setType('c');
            push(@{$self->{categories}}, $ctgry);
        }
    }

# In order to have "stable" links by data set name, we're going to shove
# the data sets into the Category table with a special type.  They won't show
# up in the interface, but you will be able to reference them.
        foreach my $dset (@Dinst){
            my $stableCategory = new LAS::Category($self, $dset);
            $stableCategory->setType('d');
            push(@{$self->{categories}}, $stableCategory);
        }

}

##
# Returns a list of LAS::Category objects that are the children of this category.
sub getCategories {
    return @{$_[0]->{categories}};
}


1;
