# A perl module for command line communication (CLC)
# Copyright (c) 2001 TMAP, NOAA
# ALL RIGHTS RESERVED
#
# Please read the full copyright notice in the file COPYRIGHT
# included with this code distribution

#
# CLC.pm
#
# Jonathan Callahan 3/99
# $Id: CLC.pm,v 1.4 2004/11/02 19:07:00 callahan Exp $
#
# based on perl code by Mike Knezevitch at PMEL
# that was based on C code by Steve Hankin at PMEL
#
# Address bug reports and comments to:
# callahan@pmel.noaa.gov


# ABSTRACT:
# This is a perl5 module that can be used to open communications
# between a perl program, perhaps a CGI, and a command line program.
# The CLC package will handle creating pipes, forking a process,
# issuing commands to the child program and dealing with error 
# returns from the child.


# METHODS:
#   new($program_name, $prompt, $debug_file) -- create a new CLC object
#   accept_error($error_string)              -- add an error a list of error strings which should not cause termination of processing
#   start($arg_string)                       -- open pipes and start a command line program
#   send_command($command_string)            -- send a command string to the program
#   wait_for_prompt                          -- wait for the command line prompt
#   error_check($error_string)               -- see if errors were generated
#   synch                                    -- wait for the prompt (no error checking)
#   close                                    -- kill the program and close the pipes


# EXAMPLE:
#
# #!/usr/local/bin/perl 
# #
# # This code takes a CGI query object and uses the parameters
# # to start a Ferret session, generate some output and then 
# # returns the output to the web.
# 
# use English;
# 
# use lib '.';
# use CGI;
# use CLC;
#
# $query = new CGI;
# 
# # Use unbuffered output.
# # Open a file for debugging output.
# # Redirect stderr to stdout so we can see err msgs on the web.
# # Save the query contents for debugging purposes.
# 
# $OUTPUT_AUTOFLUSH = 1;
# open(DBGFILE, ">$debug_file") || 
#   &mydie("Can't open $debug_file\n<P>\nAre the permissions set properly?");
# open (STDERR, ">&STDOUT") || &mydie("Can't dup stdout ($OS_ERROR)\n");
# $query->save(DBGFILE);
# print DBGFILE "\n";
# 
# # Create variables with short names from the query object 
#
# $ouput_file = $query->param('output_file');
# $dataset = $query->param('dataset');
# $variable = $query->param('variable');
# 
# # Create communication with ferret
# 
# $ferret = new CLC($which_ferret, "yes? ", \*DBGFILE);
# 
# # Start Ferret with "-gif" as an argument.
# 
# $ferret->start("-gif");
# $ferret->wait_for_prompt;
# 
# $ferret->send_command("use $dataset");
# $ferret->wait_for_prompt;
# 
# $ferret->send_command("shade $variable\[k=1,l=1\]");
# $ferret->wait_for_prompt;
# 
# $ferret->send_command("FRAME/FORMAT=gif/FILE=\"$output_file\"");
# $ferret->wait_for_prompt;
# 
# $ferret->send_command("quit");
# $ferret->error_check("error on quit");
# 
# # Save a record of the transaction and close the communication
# 
# print DBGFILE $ferret->{TRANSCRIPT};
# 
# # Close the communication
# 
# $ferret->close;
# 
# open(FILE, $output_file) || &mydie("Can't open $output_file: $OS_ERROR\n");
# while (<FILE>) { print STDOUT; }
# close(FILE);
# 
# }


# ------------------ START OF THE LIBRARY ------------
package CLC;
#use strict;
use vars qw($VERSION);
use Template;

my $IsCrash = 0;
my $DeadPID = 0;
$VERSION = 0.01;

#### Method: new($program_name, $prompt, $debug_file)
# The new routine.  A new CLC object is created and initialized.
####
sub new {
  my($class, $program_name, $prompt, $dbgfile) = @_;
  my $self = {
      timeout => 60
  };
  bless $self, $class;
  die "No program name" if ! $program_name;
  die "No prompt" if ! $prompt;
  $self->initialize($program_name, $prompt, $dbgfile);
  return $self;
}

sub set_timeout {
  $_[0]->{timeout} = $_[1];
}
    


#### Method: accept_error($error_string)
# Add a string to the @{$self->{ACCEPTABLE_ERRORS}} array.  When the 
# command line program responds on the error stream, this
# array will be checked for strings which should NOT cause
# termination of execution.
#
# Note that $self->{ACCEPTABLE_ERRORS} is a scalar.  We're using
# @{$scalar_value} to force it to be interpreted as an array.
####
sub accept_error {
  my $self = shift;
  my $error_string = shift;
  push @{$self->{ACCEPTABLE_ERRORS}}, $error_string;
  $self->{TRANSCRIPT} .= "Adding an acceptable error string: \"$error_string\".\n";
}


#### Method: start($arg_string)
# The CLC object is used to open input, output and error pipes
# to the program.  A child process is forked and becomes the 
# program, which is then ready to receive commands.
####
sub start {
  my $self = shift;
  my $arg_string = shift;
  my @program_args = split / /, $arg_string;

# create pipes to handle communication

  pipe (FROM_PERL, TO_PROGRAM);
  pipe (FROM_PROGRAM, TO_PERL);
  pipe (READ_STDERR, WRITE_STDERR);

# create a child process

  my $program_pid = fork;

  if (!defined $program_pid) {
    $self->{ERROR} = "Couldn't fork: $!\n";;
    $self->{TRANSCRIPT} .= "Couldn't fork: $!\n";
    return;
  }

# child process becomes the program

  if ($program_pid == 0)  {

# close unused parts of pipes
# (From the child's point of view.)

    close STDIN;
    close STDOUT;
    close STDERR;
    close FROM_PROGRAM;
    close TO_PROGRAM;
    close READ_STDERR;

# attach standard input/output/error to the pipes
# (From the child's point of view.)

    open (STDIN,  '<&FROM_PERL')    || die ("open: $!");
    open (STDOUT, '>&TO_PERL')      || die ("open: $!");
    open (STDERR, '>&WRITE_STDERR') || die ("open: $!");

# unbuffer the outputs

    select STDOUT; $| = 1;
    select STDERR; $| = 1;

# execute a program

    exec $self->{EXECUTABLE}, @program_args;

# shouldn't get here!!!

    if (!$program_pid) {
      $self->{ERROR} = "System error: $!\n";
      $self->{TRANSCRIPT} .= "System error: $!\n";
      return;
    }

  } # end of child 

# parent process does this

# close unused filehandles
# (From the parent's point of view.)

  close FROM_PERL;
  close TO_PERL;
  close WRITE_STDERR;

# set up signal handlers

  $SIG{'CHLD'} = \&crash;
  $SIG{'PIPE'} = \&crash;

# This next hunk of code courtesy of Mike Knezevich at NOAA/PMEL/CNSD
# ============================================================
# Trick to make sure the LAS_ filehandles get low numbers
# to work around apparent vec bug

  open (SAVEOUT, '>&STDOUT');
  open (SAVEIN, '<&STDIN');
  open (SAVERR, '>&STDERR');
  close STDOUT;
  close STDIN;
  close STDERR;

# For communication with the program

  open (LAS_STDOUT, '>&TO_PROGRAM')   || die ("open: $!");
  open (LAS_STDIN,  '<&FROM_PROGRAM') || die ("open: $!");
  open (LAS_STDERR, '<&READ_STDERR')  || die ("open: $!");

# unbuffer

  my $currfh = select LAS_STDERR; $| = 1;
  select LAS_STDOUT; $| = 1;
  select $currfh;

# put stdin,stdout, and stderr back

  open (STDOUT,'>&SAVEOUT');
  open (STDIN, '<&SAVEIN');
  open (STDERR, '>&SAVERR');

# ============================================================
# Thanks, Mike.

# assign object properties

  $self->{OUT} = 'LAS_STDOUT';
  $self->{IN}  = 'LAS_STDIN';
  $self->{ERR} = 'LAS_STDERR';
  $self->{PID} = $program_pid;
}

#### Method: do_command($command_string)
# Sends a command string, waits for output
####

sub do_command {
    my ($self, $command) = @_;
    $self->send_command($command);
    &LAS::Server::debug("Sent command: $command");
    $self->wait_for_prompt;
}


#### Method: send_command($command_string)
# Sends a command string to the program.
####
sub send_command {
  my $self = shift;
  my $command = shift;

# Remove any new lines
  chomp($command);

  $self->{TRANSCRIPT} .= "$command\n";

  my $out = $self->{OUT};
  print $out "$command\n";
}


#### Method: wait_for_prompt
# Waits until the program's prompt is seen and
# then returns all text generated by the program.
####
sub wait_for_prompt {
  my $self = shift;

  my $reply = $self->synch;
  $self->{response} = $reply;
  &LAS::Server::debug("Got reply: $reply\n");
  
  $self->error_check($reply);
}

#### Method: get_response
# Returns Ferret response string
####
sub get_response {
  my $self = shift;
# get rid of prompt on last line of response
  my @response = split("\n",$self->{response});
  pop @response if $response[$#response] eq $self->{PROMPT};
  my $response = join("\n",@response);

  return $response;
}

#### Method: error_check($message_string)
# Checks for presence of an error string and, if one is 
# found dumps the contents of the transcript to the debug file.
# An HTML error message is sent to STDOUT.
####
sub error_check {
  my $self = shift;
  my $message = shift;

  if ( defined $self->{ERROR} ) {

    # Write out the transcript to the debug file

    if ( defined $self->{DBGFILE} ) {
      my $out = $self->{DBGFILE};
      print $out "$self->{TRANSCRIPT}\n";
    }
    &LAS::Server::debug("Error: transcript is\n" . $self->{TRANSCRIPT}
			. "\n");
    $self->close(1);		# Kill program process
    if ($message =~ /\s*\<script_error\>(.+)\<\/script_error\>/s) {
      $message = "$1";
      die $message;
    } else {
      die $self->{TRANSCRIPT};
    }
  }
}


#### Method: synch
# Uses select() to listen to the output and error pipes.
# Successive reads on the input pipe continue until
# there is an error or the program's prompt is encountered.
# If an error is encountered, the ERROR string is returned.
# Otherwise, all output from the program is returned.
####
sub synch {
  my $self = shift;

  my $rin = "";
  my $rout = "";
  my $ein = "";
  my $eout = "";
  my $stdinin = "";
  my $stderrin = "";
  my $timeout = $self->{timeout};
  my $nfound = 0;

  my $reply = "";
  my $no_prompt_yet = -1;
  my $return_value = "";

  my $isrep = 0;
  my $iserr = 0;
  my $isexc = 0;

#
# Loop until you get a command line prompt: $self->{PROMPT}
#

  while ($no_prompt_yet == -1) {

  if (defined $self->{ERROR}) {
    return $self->{ERROR};
  }

# The next couple of lines are described in the perl books
# under the select() function.  We ignore the "write" file
# descriptor.

# Block until the input or error file descriptor is ready

    $rin = $ein ="";
    vec($stdinin, fileno($self->{IN}),1) = 1;
    vec($stderrin, fileno($self->{ERR}),1) = 1;
    $rin = $stdinin | $stderrin;
    $ein = $rin;
    $nfound = select($rout=$rin,undef,$eout=$ein,$timeout);

    if ($IsCrash){
	my $pid = $self->{PID};
	if ($pid == $DeadPID){
	    $self->{ERROR} = "Program quit unexpectedly.$DeadPID:$pid";
	    $self->{TRANSCRIPT} .= "Program quit unexpectedly.$DeadPID:$pid";
	    return $self->{ERROR};
	} else {
	    $IsCrash = 0;
	    $SIG{'CHLD'} = \&crash;
	    $SIG{'PIPE'} = \&crash;
	}
    }

    if ($nfound == -1) {
      $self->{ERROR} = "System error: interrupted output\n";
      $self->{TRANSCRIPT} .= "System error: interrupted output\n";
      return $self->{ERROR};

    } elsif ($nfound == 0) {
      $self->{ERROR} = "Timeout after $timeout seconds\n";
      $self->{TRANSCRIPT} .= "Timeout after $timeout seconds\n";
      return $self->{ERROR};

    } else {

# More help from Mike K. 
# ============================================================
      # Check if stdin or stderr
      $isrep=ord($rout & $stdinin);
      $iserr=ord($rout & $stderrin);
      $isexc=ord($eout & $stderrin);

      if ($iserr != 0) {
	select(undef,undef,undef,0.25); # Hack to allow buffer to fill
        sysread($self->{ERR},$reply,10000,0);
        $self->{TRANSCRIPT} .= "$reply";
	if ($self->error_OK($reply)){
	    $no_prompt_yet = index($reply,$self->{PROMPT});
	} else {
	    $self->{ERROR} = $reply;
	    return $self->{ERROR};
	}
      } elsif ($isrep != 0) {
        $reply = "";
        sysread($self->{IN},$reply,10000,0);
        $return_value .= $reply;
        $no_prompt_yet = index($reply,$self->{PROMPT});

      } elsif ($isexc != 0) {
        sysread($self->{ERR},$reply,10000,0);
        $self->{ERROR} = $reply;
        $self->{TRANSCRIPT} .= $reply;
        return $self->{ERROR};
      }
# ============================================================

    }

  }

  $self->{TRANSCRIPT} .= $return_value;
  return $return_value;

}


#### Method: close
# Closes the pipes and waits for child program to die.
####
sub close {
  my ($self,$dokill) = (@_);
  close($self->{OUT});
  close($self->{IN});
  $SIG{'PIPE'} = \&reaper;
  $SIG{'CHLD'} = \&reaper;
  if ($dokill){
      &LAS::Server::debug("killing...");
      my $num = kill 9, $self->{PID};
      &LAS::Server::debug("Waiting for program to exit...\n");
      waitpid $self->{PID},0 if $num;
  } else {
      &LAS::Server::debug("Waiting for program to exit...\n");
      waitpid $self->{PID},0;
  }
  &LAS::Server::debug("program exited...\n");
}


########################################
# THESE METHODS ARE MORE OR LESS PRIVATE
########################################

#### Method: initialize($program_name, $prompt, $debug_file)
# Initialize the CLC object with the program name, prompt and
# a debug file to write to.
####
sub initialize {
  my $self = shift;
  my $program_name = shift;
  my $prompt = shift;
  my $dbgfile = shift;

  die "CLC: $program_name is not an executable file"
      if ! -x $program_name;
  $self->{EXECUTABLE} = $program_name;
  $self->{PROMPT} = $prompt;
  $self->{DBGFILE} = $dbgfile;
  $self->{ERROR} = undef;
  $self->{TRANSCRIPT} = "";
  @{$self->{ACCEPTABLE_ERRORS}} = ();
}


##### Method: error_OK($reply_string)
# Check the list of acceptable errors and return
# true if the string contains one.
#
# JC_TODO: This assumes that only one error message
# JC_TODO: is returned at a time.  Not necessarily true.
####
sub error_OK {
  my $self = shift;
  my $reply_string = shift;
  my $acceptable_error = "";

  foreach $acceptable_error ( @{$self->{ACCEPTABLE_ERRORS}} ) {
    if ( index($reply_string,$acceptable_error) > -1 ) {
      return 1;  # Return TRUE.
    }
  }

  return 0;      # Return FALSE.
}


##### Method: reaper
# This from Programming perl, 2nd ed, p. 340
####
sub reaper {
#  &LAS::Server::debug("Reaping...");
  $waitedpid = wait;
#  &LAS::Server::debug("Done Reaping...");
  $SIG{CHLD} = \&reaper;
}


#### Method: crash
# Write error strings.  This routine is used as the
# signal handler routine for SIG_CHLD and SIG_PIPE.
####
sub crash {
  $IsCrash = 1;
  $DeadPID = wait;
}

return 1; # so that require() returns true

