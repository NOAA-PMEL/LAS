# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

# TODO -- Check for invalid characters in menu values (i.e. double quotes)
# TODO -- Backward compatible with custom code
# TODO -- Only generate referenced menus
# TODO -- Define link from dataset/variable to map
# TODO -- Verify attributes for <ifmenu>, <menu>, <livemap>, <options>

use LAS;
use strict;

# Forward declarations
package LAS::UI::OptionDB;
package LAS::UI::OptionDef;	
package LAS::UI::Option;	

package LAS::UI::Base;
@LAS::UI::Base::ISA = qw(LAS::Container);

package LAS::UI::Image;
@LAS::UI::Image::ISA = qw(LAS::UI::Base);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'image';
    my $name = $self->getAttribute("name");
    die "Duplicate image name: $name" if $LAS::UI::Generator::Images{$name};
    $LAS::UI::Generator::Images{$name} = $self;
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    $self->requireAtts(qw(name url bounds));
    my @bounds = $self->getBounds;
    die "bounds attribute for <image> must contain 4 values"
	if $#bounds != 3;
    my @nums;
    foreach my $num (@bounds){
	if ($num == 0 && $num !~ /0+(\.0+)*/){
	    die "Value: '$num' in <image> bounds attribute is not a number"
	}
	push(@nums, $num);
    }
    die "bounds attribute for <image> defines a region with zero area"
	if $nums[0] == $nums[1] || $nums[2] == $nums[3];
}

sub getUrl {
    my $self = shift;
    my $url = $self->getAttribute("url");
    $url =~ s/\s+//g;
    return $url
}

sub getBounds {
    my $self = shift;
    my $bounds = $self->getAttribute("bounds");
    $bounds =~ s/\s+//g;
    return split(',', $bounds);
}

sub getKey {
    my $self = shift;
    join(',', $self->getBounds) . $self->getUrl;
}


package LAS::UI::MenuItem;
use TMAPDate;
@LAS::UI::MenuItem::ISA = qw(LAS::UI::Base);


sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    my $name = $self->getName;
    die "Internal: Invalid node tag <$name>" if !($name eq "item" ||
						  $name eq "ifitem");
    my $values = $self->getAttribute("values");
    if (!defined($values)) {
        die "<item>/<ifitem> missing values attribute";
    }

    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    if ($self->getName ne 'ifitem'){
	$self->requireAtts(qw(values));
    } else {
	$self->requireAtts(qw(values view));
    }
    my $text = $self->getAttribute('#pcdata'); # Hack for XML RDBMS
    if (!$text){
	my $child = $self->getElement->getFirstChild;
	die "<item>/<ifitem> missing text" if ! defined($child);
	$text = $child->getNodeValue;
    }
    $text =~ s/^\s+//g;
    $text =~ s/\s+$//g;
    die "<item>/<ifitem>  missing text" if $text eq "";
    $self->{text} = $text;
    my $parent = $self->getElement->getParentNode;
    my $ptype = $parent->getAttribute('type');
    if (!$ptype){
	$ptype = $parent->getAttribute('name');
	print STDERR "Deprecated: <menu> named '$ptype' is missing 'type' attribute. Using name attribute to determine type\n" if $ptype;
	$ptype =~ s/_\w+//g;
	$ptype =~ tr/A-Z/a-z/;
    }
    $self->{menu_type} = $ptype;
    if ($self->{menu_type} eq 'views') {
	die "values attribute for <item> in views menu must have only one value"
	    if scalar($self->getValues) != 1;
    } elsif ($self->{menu_type} eq 'ops') {
	my @opvals = $self->getValues;
	die "values attribute for <item> in ops menu must have two or three comma separated values"
	    if $#opvals < 1 || $#opvals > 3;
	if (defined($opvals[2])){
	    die "Invalid third comma separated value attribute for <item>: $opvals[2] in ops menu. Must be positive integer" if ($opvals[2] !~ /\d+/);
	}
	if (defined($opvals[3])){
	    die "Invalid fourth comma separated value attribute for <item>: $opvals[3] in ops menu. Must be positive integer" if ($opvals[3] !~ /\d+/);
	}
    } elsif ($self->{menu_type} eq 'regions') {
	my @values = $self->getValues;
	my $valueCount = scalar(@values);
	die "values attribute for <item> in regions menu can only have four or six comma separated values"
	    if !($valueCount == 4 || $valueCount == 6);
	if ($valueCount > 4){
	    my $date = new TMAP::Date($values[4]);
	    die "Invalid date: $values[4] for <item> in regions menu"
		if !$date->isOK;
	    $date = new TMAP::Date($values[5]);
	    die "Invalid date: $values[5] for <item> in regions menu"
		if !$date->isOK;
	}
    } elsif ($self->{menu_type} eq 'options' ||
	     $self->{menu_type} eq 'analysis'){
	die "values attribute for menu <item> in <options> must have one value"
	    if scalar($self->getValues) != 1;
    } elsif ($self->{menu_type} eq 'constraint'){
				# Do nothing
    } else {
	die "Unknown type of menu: $ptype\n",
	"Menu type is determined by 'type' attribute of <menu> tag";
    }
}

sub getMenuType {return $_[0]->{menu_type}; }

sub getText {return $_[0]->{text}; }

# NOTE: Initially, this routine stripped out all spaces but this prevented
# NOTE: the use of strings with embedded spaces as constraints.  We could
# NOTE: shift to only stripping of beginning and trailing spaces but I see
# NOTE: no reason for that either.  Perhaps some database has a column that
# NOTE: is defined as a constant char array where values have trailing spaces.
sub getValues {
    my $self = shift;
    my $values = $self->getAttribute("values");
#    $values =~ s/\s+//g;
    return split(',', $values);
}

sub getKey {
    my $self = shift;
    join(',', $self->getValues) . $self->getText;
}


package LAS::UI::Menu;
@LAS::UI::Menu::ISA = qw(LAS::UI::Base);


sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'menu';
    my $name = $self->getAttribute("name") or
	die "menu missing name attribute";
    die "Duplicate menu name: $name" if $LAS::UI::Generator::Menus{$name};
    $LAS::UI::Generator::Menus{$name} = $self;
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    $self->requireAtts(qw(name type));
    $self->requireAttInList('type',
			    qw(analysis variables ops views regions options constraint));
    my $children = $self->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    my $firstName = "";
    while ($iter->hasMore){
	my $node = $iter->next;
	my $name = $node->getTagName;
	my $firstName = $name if ! $firstName;
	if ($firstName ne $name){
	    die "All elements contained by <menu> must be the same type";
	}
	if ($name eq 'item' || $name eq 'ifitem'){
	    push(@{$self->{children}},
		 new LAS::UI::MenuItem($node));
	} else {
	    die "Invalid element: ",$name;
	}
    }
}


package LAS::UI::MapItem;
use File::PathConvert qw(rel2abs);
@LAS::UI::MapItem::ISA = qw(LAS::UI::Base);


sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    bless $self, $class;
}

sub _initialize {
    my $name = $_[0]->getName;
    if ($name eq 'ifoptions'){
	$_[0]->requireAtts(qw(op href));
    } elsif ($name eq 'ifmenu'){
	$_[0]->requireAtts(qw(href));
	my $mode = $_[0]->getAttribute('mode');
	if (defined($mode)){
	    die "<ifmenu> can only have one of attributes 'comparison' or 'view'"
		if defined($_[0]->getAttribute('view'));
	    die "<ifmenu> can only have attribute 'mode' = 'comparison'"
		if $mode ne "comparison";
	} else {
	    $_[0]->requireAtts(qw(view));
	}
    } elsif ($name eq 'constraint'){
	$_[0]->requireAtts(qw(type));
	my $type = $_[0]->getAttribute('type');
	die "type attribute of <constraint> must be 'variable','text', or 'textfield'"
	    if !($type eq 'variable' || $type eq 'text' || $type eq 'textfield');
	my $count = $_[0]->getAttribute('count');
	die "count attribute of <constraint> must be > 0 and < 10"
	    if defined $count && ($count <= 0 || $count >= 10);
	my $required = $_[0]->getAttribute('required');
	$required = "false" if ! $required;
	die "'required' attribute of <constraint> must be 'true' or 'false'"
	    if !($required eq "true" || $required eq "false");
	if ($type eq 'text'){
	    my $multi = $_[0]->getAttribute('multiselect');
	    $multi = "false" if ! $multi;
	    die "'multiselect' attribute of <constraint> must be 'true' or 'false'"
		if !($multi eq "true" || $multi eq "false");
	}
    }
}

sub checkUrl {
    my ($self, $image) = @_;
    my $url = $image->getUrl;
    if (!&LAS::validateURL($url, "../ui/LiveMap/classes/")){
	&LAS::printerrln("*** Can't find image at URL: $url");
    }
}

package LAS::UI::Map;
@LAS::UI::Map::ISA = qw(LAS::UI::Base);


sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'map';
    my $name = $self->getAttribute("name") or
	die "map missing name attribute";
    die "Duplicate map name: $name" if $LAS::UI::Generator::Maps{$name};
    my $type = $self->getAttribute('type');
    die "map type attribute must be one of  (ops,views,livemap,options,analysis)"
	if ! ($type eq 'ops' || $type eq 'views' ||
	      $type eq 'livemap' || $type eq 'options' ||
	      $type eq 'analysis');
    $LAS::UI::Generator::Maps{$name} = $self;
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    $self->requireAtts(qw(type name));
    my $children = $self->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    my $type = $self->getAttribute('type');
    die "<map> missing type attribute" if ! $type;
    my $firstName = "";
    while ($iter->hasMore){
        my $node = $iter->next;
        my $name = $node->getTagName;
	if ($type eq 'options'){
	    die "<map> of type $type can only have <ifoptions> or <options> children"
		if !($name eq "ifoptions" || $name eq "options");
	} elsif ($type eq 'ops'){
	    die "<map> of type $type can only have <ifmenu> or <constraint> children"
		if !($name eq "ifmenu" || $name eq 'constraint');
	} elsif ($type eq 'views' || $type eq 'analysis'){
	    die "<map> of type $type can only have <menu> children"
		if $name ne "menu";
	} elsif ($type eq 'livemap'){
	    die "<map> of type $type can only have <image> or <menu>children"
		if !($name eq "menu" || $name eq "image");;
	} else {
	    die "Unknown <map> type attribute: $type";
	}
	push(@{$self->{children}},
	     new LAS::UI::MapItem($node));
    }
    if ($self->getAttribute('type') eq 'livemap') {
	my ($foundImage, $foundMenu);
	foreach my $child (@{$self->{children}}){
	    $foundImage = 1 if $child->getName eq "image";
	    $foundMenu = 1 if $child->getName eq "menu";
	}
	die "map of type livemap missing menu child" if !$foundMenu;
	die "map of type livemap missing image child" if !$foundImage;
    }
}

    
package LAS::UI::Default;
@LAS::UI::Default::ISA = qw(LAS::UI::Base);
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    my $children = $self->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    my %typeHash;
    while ($iter->hasMore){
	my $node = $iter->next;
	die "Only <map> can be child of <default>"
	    if $node->getTagName ne "map";
	my $href = $node->getAttribute('href');
	die "Default <map> missing href attribute"
	    if ! $href;
	$href =~ s/^#//;
	my $map = $LAS::UI::Generator::Maps{$href};
	die "Can't find link to map '$href'" if ! $map;
	push(@{$self->{children}}, $map);
	$typeHash{$map->getAttribute('type')} = 1;
    }
    my @missType = ();
    foreach my $type (qw(views ops livemap options)){
	push(@missType, $type) if ! $typeHash{$type};
    }
    die "<default> missing <map> of type(s):", join(',',@missType)
	if $#missType >= 0;
}

package LAS::UI::Generator;
@LAS::UI::Generator::ISA = qw(LAS::UI::Base);
$LAS::UI::Generator::Dir = undef;
sub new {
    %LAS::UI::Generator::OptionDefs = ();
    %LAS::UI::Generator::Images = ();
    %LAS::UI::Generator::Menus = ();
    %LAS::UI::Generator::Maps = ();
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $parser = shift;
    $LAS::UI::Generator::Dir = shift;
    push(@_, $parser->getRoot->getDocumentElement);
    my $self = $class->SUPER::new(undef, @_);
    $self->{title} = $self->getAttribute("title");
    $self->{title} = "Live Access Server" if ! $self->{title};
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    $self->{dir} =  $LAS::UI::Generator::Dir;
    $self->{optiondefs} = [];
    my @optiondefs = $self->{element}->getElementsByTagName("options");
    foreach my $optiondef (@optiondefs){
        my $optiondefchildren = $optiondef->getChildNodes;
	my $iter = new LAS::ElementIterator($optiondefchildren);
	while ($iter->hasMore){
	    my $node = $iter->next;
	    push(@{$self->{optiondefs}}, new LAS::UI::OptionDef($node));
	}
    }
    $self->{images} = [];
    my @images = $self->{element}->getElementsByTagName("images");
    foreach my $image (@images){
        my $imagechildren = $image->getChildNodes;
	my $iter = new LAS::ElementIterator($imagechildren);
	while ($iter->hasMore){
	    my $node = $iter->next;
	    push(@{$self->{images}}, new LAS::UI::Image($node));
	}
    }
    $self->{menus} = [];
    my @menus = $self->{element}->getElementsByTagName("menus");
    foreach my $menu (@menus){
        my $menuchildren = $menu->getChildNodes;
	my $iter = new LAS::ElementIterator($menuchildren);
	while ($iter->hasMore){
	    my $node = $iter->next;
	    push(@{$self->{menus}}, new LAS::UI::Menu($node));
	}
    }
    $self->{maps} = [];
    my @maps = $self->{element}->getElementsByTagName("maps");
    foreach my $map (@maps){
        my $mapchildren = $map->getChildNodes;
	my $iter = new LAS::ElementIterator($mapchildren);
	while ($iter->hasMore){
	    my $node = $iter->next;
	    push(@{$self->{maps}}, new LAS::UI::Map($node));
	}
    }
    $self->{defaults} = [];
    $self->{defaultsHash} = {};
    my @defaults = $self->{element}->getElementsByTagName("defaults");
    foreach my $default (@defaults){
        my $defaultchildren = $default->getChildNodes;
	my $iter = new LAS::ElementIterator($defaultchildren);
	while ($iter->hasMore){
	    my $node = $iter->next;
	    my $def = new LAS::UI::Default($node);
	    push(@{$self->{defaults}}, $def);
	    my $name = $def->getAttribute('name');
	    $name = "" if ! $name;
	    $self->{defaultsHash}->{$name} = $def;
	}
    }
}

sub getTitle {
    $_[0]->{title};
}

##
# Get the directory that contains the UI configuration file
#
sub getDir {
    $_[0]->{dir};
}

##
# Set the name of the XML file
# @param $file filename
#
sub setFile {
    my ($self, $file) = @_;
    $self->{file} = $file;
}

sub getOptiondefs {
    return @{$_[0]->{optiondefs}};
}

sub getMaps {
    my $self = shift;
    return @{$self->{maps}};
}

sub getMenus {
    my $self = shift;
    return @{$self->{menus}};
}

sub getImages {
    my $self = shift;
    return @{$self->{images}};
}

sub getDefaults {
    my $self = shift;
    return @{$self->{defaults}};
}

sub getDefaultByName {
    my $self = shift;
    my $name = shift;
    return $self->{defaultsHash}->{$name};
}

sub getOptiondefsByName {
    my ($self,$name) = @_;
    return $LAS::UI::Generator::OptionDefs{$name};
}

sub getDefaultMenus {
    my $self = shift;
    my %menus;
    foreach my $def ($self->getDefaults){
	foreach my $map ($def->getChildren){
	    foreach my $link ($map->getChildren){
		my $href = $link->getAttribute('href');
		die $link->getName, " missing href attribute"
		    if ! defined($href);
		$href =~ s/^#//;
		my $lname = $link->getName;
		if ($lname eq 'menu' || $lname eq 'ifmenu'){
		    my $menu = $LAS::UI::Generator::Menus{$href};
		    die "Can't find link to menu $href"
			if ! $menu;
		    $menus{$href} = $menu;
		}
	    }
	}
    }
    return values(%menus);
}

package LAS::UI::TextField;
@LAS::UI::TextField::ISA = qw(LAS::UI::Base);


sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'textfield';
    my $name = $self->getAttribute("name") or
	die "TextField missing name attribute";
    bless $self, $class;
}

sub _initialize {
}

package LAS::UI::OptionDef;
@LAS::UI::OptionDef::ISA = qw(LAS::UI::Base);
$LAS::UI::OptionDef::ID = 1;
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'optiondef';
    die "<optiondef> missing name attribute" if ! $self->getAttribute('name');
    $self->{id} = $LAS::UI::OptionDef::ID++;
    bless $self, $class;
}

sub _inherit {
    my ($self, $linkName) = @_;
    $linkName =~ s/^#//;
    my $base = $LAS::UI::Generator::OptionDefs{$linkName};
    die "Can't find base class named $base to inherit from $linkName"
	if ! $base;
    push(@{$self->{children}}, $base->getChildren);
}

sub _initialize {
    my $self = shift;
    my $inherit = $self->getAttribute("inherit");
    if ($inherit){
	my @inherits = split(',',$inherit);
	foreach my $inherited_thing (@inherits){
	    $self->_inherit($inherited_thing);
	}
    }
    my $children = $self->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    while ($iter->hasMore){
	my $node = $iter->next;
	my $name = $node->getTagName;
	push(@{$self->{children}}, new LAS::UI::Option($node));
    }
    $LAS::UI::Generator::OptionDefs{$self->getAttribute('name')} = $self;
}

package LAS::UI::Option;
@LAS::UI::Option::ISA = qw(LAS::UI::Base);

sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = $class->SUPER::new(undef,@_);
    die "Invalid element: ",$self->getName
	if $self->getName ne 'option';
    bless $self, $class;
}

sub _initialize {
    my $self = shift;
    my $children = $self->getElement->getChildNodes;
    my $iter = new LAS::ElementIterator($children);
    my ($foundHelp, $foundTitle, $foundMenuOrText) = (0,0,0);
    while ($iter->hasMore){
	my $node = $iter->next;
	my $name = $node->getTagName;
	if ($name eq "help" || $name eq "title"){
	    $foundHelp = 1 if $name eq 'help';
	    $foundTitle = 1 if $name eq 'title';
	    my $text = $node->getAttribute('#pcdata'); # Hack for XML RDBMS
	    $text = $node->getFirstChild &&
		$node->getFirstChild->getNodeValue if ! $text;
	    die "Tag <$name> doesn't contain text" if ! defined($text);
	    $text =~ s/^\s+//g;
	    $text =~ s/\s+$//g;
	    $text =~ s/\n+/ /g;
	    $self->{$name} = $text;
	} elsif ($name eq "menu"){
	    my $menu = new LAS::UI::Menu($node);
	    push(@{$self->{children}}, $menu);
	    $self->{type} = 'm';
	    $self->{type} = 'v' if $menu->getAttribute('type') eq 'variables';
	    $foundMenuOrText = 1;
	} elsif ($name eq "textfield"){
	    push(@{$self->{children}}, new LAS::UI::TextField($node));
	    $self->{type} = 't';
	    $foundMenuOrText = 1;
	} else {
	    die "Unknown tag: <$name>";
	}
    }
    die "No <help> definition for <option>" if ! $foundHelp;
    die "No <title> definition for <option>" if ! $foundTitle;
    die "No <menu> or <textfield> definition for <option>" if ! $foundMenuOrText;
}

sub getType {
    $_[0]->{type};
}

sub getHelp {
    $_[0]->{help};
}

sub getTitle {
    $_[0]->{title};
}

##
# LAS::UI::OptionRoot provides an alternative path to the information
# in the <options> portion of the UI database. It is much faster
# then accessing the info through the LAS::UI::Generator object.
# It does require that a separate 'options.xml' file exist.	

package LAS::UI::OptionRoot;
@LAS::UI::OptionRoot::ISA = qw(LAS::UI::Base);
sub new {
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $parser = shift;
    push(@_, $parser->getRoot->getDocumentElement);
    my $self = $class->SUPER::new(undef, @_);
    bless $self, $class;
}

sub getByName {
    my ($self, $name) = @_;
    return $self->{names}->{$name};
}

sub _initialize {
    my $self = shift;
    $self->{names} = {};
    my @optiondefs = $self->{element}->getElementsByTagName("optiondef");
    foreach my $optiondef (@optiondefs){
	my $odef = new LAS::UI::OptionDef($optiondef);
	push(@{$self->{children}}, $odef);
	$self->{names}->{$odef->getAttribute('name')} = $odef;
    }
}

1;
