#!/usr/bin/perl -w

#
# Rudimentary Perl version of Javadoc documentation generator
# Generates XML file that is then translated via XSLT to HTML
#
# Comments that are to be included as documentation are started with
# '##'. The following JavaDoc tags are supported:
# @param
# @return
#
# TODO -- Allow &lt; in comments (this, because of XML processing)
#         comes out as a real '<', not an escaped '<')
# TODO -- Process @return
# TODO -- Automatically extract superclass
# $Id: printMethods.pl.in,v 1.2 2004/03/13 01:17:14 webuser Exp $

use strict;
use Getopt::Long;
use XML::LibXSLT;
use XML::LibXML;

my $PrintPrivate;
sub printComment {
    my ($comment, $indent) = @_;
    $comment =~ s/\</&lt;/g;	# Escape all '<'
    my @comments = split(/\n/,$comment);
    @comments = grep {s/^#+\s//} @comments;
    my $info="";
    while (@comments){
        my $line = shift @comments;
        if ($line =~ /^\s*\@/){
            unshift(@comments, $line);
            last;
        }
        $info .= "$line\n";
    }
# Get initial comment
    if ($info){
	print OUT "\t"x$indent . "<info>\n";
	print OUT $info;
	print OUT "\t"x$indent . "</info>\n";
    }

   while (@comments){
       my $line = shift @comments;
       if ($line =~ /\@param/){
           my @results = split(/\s+/, $line,3);
           print OUT "\t"x$indent . qq{<param name="$results[1]">} if $results[1];
           if ($results[2]){
	       $results[2] =~ s/\s*$//;
	       print OUT $results[2],"\n";
	   }
           print OUT "\t"x$indent . qq{</param>\n} if $results[1];
       } elsif ($line =~ /\@return/){
           my @results = split(/\s+/, $line,2);
	   if ($results[1]){
	       print OUT "\t"x$indent . qq{<return>};
	       $results[1] =~ s/\s*$//;
	       print OUT $results[1],"\n";
	       print OUT "\t"x$indent . qq{</return>\n};
	   }
       }
   }
}
	

my $Dir;
GetOptions ('dir=s' => \$Dir, 'private' => \$PrintPrivate);

sub usage {
    print <<EOL;
    Usage: printMethods.pl [--private] --dir output_directory file [file...]
	      Creates HTML documentation files from Perl comments
              Poor Perl man version of Javadoc
                           --private   print methods labeled as private
                           --dir dir   send results to directory dir
                           file        Perl source file
    Example: printMethods.pl --dir /foo/doc LAS.pm
EOL
    exit 1;
}

if ($#ARGV < 0 || !$Dir){
    usage;
}
if (! -d $Dir){
    die "$Dir is not a directory";
}

if (! -w $Dir){
    die "Cannot write to directory $Dir";
}
    

my %PackageHash;
#my @Packages;
my %FileHash;
my $CurrPackage = {name=>"main", comment => ""};
my $CurrComment = "";
my $ReadingComment = 0;
foreach my $file (@ARGV){
    if (!open(FILE,$file)){
	warn "Cannot open $file";
	next;
    }
    $FileHash{$file}->{packages} = [];
    while(<FILE>){
	$ReadingComment = 0 if ! /^#/;
	if (/^package/){
	    chomp;
	    $CurrPackage = {};
	    my ($junk,$name) = split(/[\s;]+/);
#            $CurrPackage->{file} = $file;
	    $CurrPackage->{name} = $name;
	    $CurrPackage->{comment} = $CurrComment;
	    push(@{$FileHash{$file}->{packages}}, $CurrPackage);
	    $PackageHash{$name} = [];
	    $CurrComment = "";
	} elsif (/^sub/){
	    chomp;
	    my ($sub, $mname) = split(/[\t \{]+/);
            $mname =~ s/\(.*\)$//;    # Eliminate any prototype info
	    my $name = $CurrPackage->{name};
	    $PackageHash{$name} = []
		if ! $PackageHash{$name};
	    push(@{$PackageHash{$name}},
                 {name => $mname, comment => $CurrComment});
	    $CurrComment = "";
	} elsif (/^##/){
	    $CurrComment = $_; $ReadingComment = 1;
        } elsif (/^#/){
	    $CurrComment .= $_ if $ReadingComment;
        }
    }
    close FILE;
}

my $tmpfile = "/tmp/lasxmlout$$";
open OUT, ">$tmpfile" or die "Can't open $tmpfile";
print OUT "<lasdoc>\n";
my $fname = " ";
foreach my $file (sort keys %FileHash){
        print OUT qq{<file_name name="$file">\n};
        print OUT qq{</file_name>\n};
foreach my $package (sort {$a->{name} cmp $b->{name}} @{$FileHash{$file}->{packages}}){
    my $pname = $package->{name};
    next if !$PrintPrivate && $package->{comment} =~ /\@private/;
    print OUT qq{<class name="$pname">\n};
    printComment($package->{comment}, 1);
    foreach my $subinfo (sort {$a->{name} cmp $b->{name}} @{$PackageHash{$pname}}){
        next if !$PrintPrivate && $subinfo->{comment} =~ /\@private/;
	my $sub = $subinfo->{name};
	print OUT qq{\t<method name="$sub">\n};
	printComment($subinfo->{comment},2);
	print OUT qq{\t</method>\n};
    }
    print OUT qq{</class>\n};
}
}
print OUT "</lasdoc>\n";
close OUT;

my $parser = XML::LibXML->new();
my $xslt = XML::LibXSLT->new();

my $source = $parser->parse_file($tmpfile);

my $style_doc = $parser->parse_file('classindex.xsl');
my $stylesheet = $xslt->parse_stylesheet($style_doc);
my $results = $stylesheet->transform($source);
$stylesheet->output_file($results, "$Dir/classindex.html");

$style_doc = $parser->parse_file('class.xsl');
$stylesheet = $xslt->parse_stylesheet($style_doc);
$results = $stylesheet->transform($source);
$stylesheet->output_file($results, "$Dir/classes.html");

unlink($tmpfile);




