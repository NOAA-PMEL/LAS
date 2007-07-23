# Support for section plots
# 
# $Id: section.pl,v 1.1 2004/03/12 21:34:19 callahan Exp $

use strict;
use LAS;

package LAS::Server::Ferret;


sub section_xy {
    my ($self) = @_; 
    my $props = $self->{props}; 

    my $size = $props->{size} ? $props->{size} : "0.25";
    $self->setWinSize($size);
    $self->setup_region;
    $props->{magnify} = $self->magnification;

    # XY plots are always at a single depth
    my @overlay_labels = ();
    my $depth = "Depth : " . $props->{z_lo};
    push(@overlay_labels, $depth);
# JC_TODO: I can't get overlay_labels to work as an array
# JC_TODO: but this hack seems to work.
    #$props->{overlay_labels} = \@overlay_labels;
    $props->{overlay_labels} = $depth;

    my @args0 = qw(variable_name_0 x_lo x_hi y_lo y_hi magnify overlay_labels 
                   insitu_palette insitu_fill_levels);

    $self->runJournal('section_xy', \@args0, undef, 'section_xy', \@args0);
    $self->genImage($self->{output_file});
}

sub section_sz {
    my ($self) = @_; 
    my $props = $self->{props}; 

    my $size = $props->{size} ? $props->{size} : "0.25";
    $self->setWinSize($size);
    $self->setup_region;
    $props->{magnify} = $self->magnification;

    my @args0 = qw(variable_name_0 x_lo x_hi y_lo y_hi magnify overlay_labels 
                   insitu_palette insitu_fill_levels);

    $self->runJournal('section_sz', \@args0, undef, 'section_sz', \@args0);
    $self->genImage($self->{output_file});
}

sub section_z {
    my ($self) = @_; 
    my $props = $self->{props}; 

    my $size = $props->{size} ? $props->{size} : "0.25";
    $self->setWinSize($size);
    $self->setup_region;
    $props->{magnify} = $self->magnification;

# JC_TODO: I can't get overlay_labels to work as an array
# JC_TODO: but this hack seems to work.
    my @overlay_labels = ();
    $props->{overlay_labels} = \@overlay_labels;
    #$props->{overlay_labels} = "";

    my @args0 = qw(variable_name_0 x_lo x_hi y_lo y_hi magnify overlay_labels 
                   insitu_palette insitu_fill_levels);

    $self->runJournal('section_z', \@args0, undef, 'section_z', \@args0);
    $self->genImage($self->{output_file});
}

sub section_data {
    my ($self) = @_; 
    my $props = $self->{props}; 

    $self->setup_region;

    my @args0 = qw(variable_name_0 x_lo x_hi y_lo y_hi dset_num output_file format);

    $self->runJournal('section_data', \@args0, undef, 'section_data', \@args0);
}


1;
