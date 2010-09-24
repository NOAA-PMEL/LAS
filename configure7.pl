#
# LAS configuration
#
my $PERLBIN = shift;
#
# Extra Perl goodies needed for this script.
#
use Cwd;              # getcwd()
use File::Basename;   # fileparse(), basename(), dirname()
use File::Copy;       # cp()
use File::Path;       # mkpath()

#
# Read previous configure results if they exist.
# 
my $configp = "config7.results";
if (-f $configp){
    do $configp;
}

#
# Ferret environment variable names
#
my @EnvVars = qw(FER_DIR FER_DESCR FER_DATA FER_GRIDS FER_PALETTE
                 FER_GO PLOTFONTS FER_EXTERNAL_FUNCTIONS DODS_CONF);

print "\n\nConfiguring V7.0 User Interface...\nIf you want to install the in-situ examples make sure you've loaded the sample data.\nSee the installation instructions at: http://ferret.pmel.noaa.gov/LAS/documentation/installer-documentation/installation/ for details.\n\n";
#
# Make sure Ferret environment variable has been set up
#   

if (! $ENV{FER_DIR} ){
    print <<EOF; 
  Your FERRET environment has not been properly set up.
  (The environment variable FER_DIR is not defined)

  Have you executed "source your_system_path/ferret_paths" ?
  You need to do this before configuring LAS.
EOF
    exit 1;
}

#
# Check for appropriate Java virtual machine
#

my ($java, $autojava);
$autojava = $LasConfig{java};
if (! $autojava){
    $autojava = getExecutable('java');
}
while (! $java){
    print "Location of java executable: [$autojava] ";
    $java = <STDIN>;
    chomp($java);
    $java = $autojava if ! $java;
    if (! -x $java){
        print "$java is not an executable file\n";
        $java = undef;
    } else {
        print "Verifying Java version...\n";
        my $isJava = 0;
        open STATUS, "$java -version 2>&1|"
            or die "Can't run $java";
        my $line;
        while(<STATUS>){
            if (/^java/){
                $line = $_;
                last;
            }
        }
        close STATUS;
        if (defined $line){
            my @pieces = split(' ', $line);
            my $vstring = $pieces[$#pieces];
            $vstring =~ s/\"//g;
            my ($major,$minor) = split(/\./, $vstring);
            if ($major < 2 && $minor < 6){
                print "Java version is $vstring. Must have at least 1.6\n";
            } else {
                $isJava = 1;
            }
        }
        if (! $isJava){
            print "\n$java is not the Java program or not the right version\n\n";
            $java = undef;
        }
    }
}
$LasConfig{java} = $java;
print "You have a valid version of Java.\n\n";

#
# Search for Ferret, make sure it is correct version
#

$ENV{PATH} = $ENV{PATH} . ":/usr/local/ferret/bin/";
my ($ferret, $autoferret);
$autoferret = $LasConfig{ferret};
if (! $autoferret){
    $autoferret = getExecutable('ferret');
}
while (! $ferret){
    print "\nLocation of ferret executable: [$autoferret] ";
    $ferret = <STDIN>;
    chomp($ferret);
    $ferret = $autoferret if ! $ferret;
    if (! -x $ferret){
        print "$ferret is not an executable file\n";
        $ferret = undef;
    } else {
        print "Verifying Ferret version. This might take a few minutes...\n";
        my $isFerret = 0;
        my $testres = `echo exit | $ferret -nojnl`;
        my @lines = split /^/m, $testres;
        my $ferretVersion = "6.64";
        foreach my $line (@lines){
            my @words = split(/\s+/,$line);
            if ($words[1] =~ /Version|FERRET/){
                $words[2] =~ s/^v//;
                my $version = $words[2];
                if ($version < $ferretVersion){
                    print "\nYou need to upgrade Ferret.\n";
                    print "You need at least version $ferretVersion.\n\n";
                    exit 1;
                } else {
                    $isFerret = 1;
                }

            }
        }
        if (! $isFerret){
            print "\n$ferret is not the Ferret program\n\n";
            $ferret = undef;
        }
    }
}
print "\n\n";

my $autotds4 = $LasConfig{autotds4};
if (! $autotds4){
   $autotds4 = "yes";
}

while (! $tds4){
    print "Do you have a copy of TDS 4 installed?: [$autotds4] ";
    $tds4 = <STDIN>;
    chomp $tds4;
    $tds4 = $autotds4 if ! $tds4;
    if ($tds4 ne "yes" && $tds4 ne "no") {
        print "You must answer 'yes' or 'no'.\n";
        $tds4 = undef;
    }
}
if ( $tds4 eq "no" ) {
                    print "\nYou need to upgrade TDS.\n";
                    print "You need to be using TDS version 4.x.\n\n";
                    exit 1;
}

print "\n\n";

# Make a dummy properties file so the redirect filter can start
my $classesDir = "WebContent/WEB-INF/classes";
if ( ! -d $classesDir ) {
    mkdir $classesDir, 0755 or die "Can't create directory $classesDir: $!\n";
    print "Created a directory for the webapp classes and resources.\n\n";
}
copy ("WebContent/productserver/templates/V7UIHeader_noclassic.vm","WebContent/productserver/templates/V7UIHeader.vm");
copy ("WebContent/index_noclassic.html","WebContent/index.html");

## Write out the initial copy of the las.properties file.
## genLas.pl will read this and add some stuff it needs.
    open OUT,">$classesDir/las.properties" or die "Can't open $classesDir/las.properties'";
    print OUT "las.db.host=dummy_value\n";
    print OUT "las.db.user=dummy_value\n";
    print OUT "las.db.password=dummy_value\n";
    print OUT "las.db.dbase=dummy_value\n";

$LasConfig{ferret} = $ferret;
print "You have a valid version of Ferret.\n\n";

#
# Get the path to access the LAS UI
#
$autopathname = $LasConfig{uipath};
$autopathname = "/las" if ! $autopathname;
print "\nYou must now specify the path name the Web client will use\n";
print "when accessing LAS. Unless you have more than one version of LAS\n";
print "installed, the default of $autopathname should be fine\n";

while (! $pathname){
    print "Enter path name for LAS: [$autopathname] ";
    $pathname = <STDIN>;
    chomp($pathname);
    $pathname = $autopathname if ! $pathname;
    if ($pathname !~ /^\//){
        print "Path name must begin with a '/'\n";
        undef $pathname;
    }
    $pathname =~ s/[\/]+$//g;
    my $count = split('\/', $pathname);
    if ($count > 2){
        print "Path name can only be one level deep (i.e. /las ok, /las/foo not)\n";
        undef $pathname;
    }
}
$LasConfig{uipath} = $pathname;
print "\n\n";
#
# Set up servlet in existing tomcat distribution:
#

$LasConfig{jakarta_home} = "/usr/local/tomcat" if !defined($LasConfig{jakarta_home});

my $jakarta_home = $LasConfig{jakarta_home};
my $different_Tomcat = $jakarta_home eq "las_servlet/jakarta" ? 1 : 0;

# NOTE:  There are no 'auto_' settings for the servlet ports
# NOTE:  'servlet_shutdown_port' and 'servlet_connector_port' don't seem to be used 
my ($servlet_port, $autoservlet_port);
my ($servlet_shutdown_port, $autoservlet_shutdown_port);
my ($servlet_connector_port, $autoservlet_connector_port);
$jakarta_home = getAnswer("Full path of Tomcat JAKARTA_HOME directory where you would like to deploy the servlet", $jakarta_home);
$LasConfig{jakarta_home} = $jakarta_home;
$LasConfig{webapps}="$jakarta_home/webapps";
my $jakarta_lib;
if ( -d "$jakarta_home/common/lib" ) {
   $jakarta_lib = "$jakarta_home/common/lib";
} else {
   $jakarta_lib = "$jakarta_home/lib";
}

my $appname = $LasConfig{uipath};
$appname =~ s/\///g;
$LasConfig{appname} = $appname;

print "\n\n";

$autoservlet_port = $LasConfig{servlet_port};
while (!$servlet_port){
    $servlet_port = getAnswer("Which HTTP port does the Tomcat server use",
                                       $autoservlet_port);
    $LasConfig{servlet_port} = $servlet_port;
}

print "\n\n";

my $hstn = `hostname`;
chomp $hstn;
my $tomcat_hostname;
my $autotomcathostname = $LasConfig{tomcat_hostname};
if (! $autotomcathostname){
    ($autotomcathostname) = gethostbyname $hstn;
    chomp $autotomcathostname;
}

while (! $tomcat_hostname){
    print "Enter the full domain name of the Tomcat Server (do not include the port number): [$autotomcathostname] ";
    $tomcat_hostname = <STDIN>;
    chomp $tomcat_hostname;
    $tomcat_hostname = $autotomcathostname if ! $tomcat_hostname;
    if ($tomcat_hostname !~ /\./){
        print "You must enter a full domain name\n";
        $tomcat_hostname = undef;
    }
}
$LasConfig{tomcat_hostname} = $tomcat_hostname;

print "\n\n";
#
# Get a title for the LAS
#
my $title;
my $autotitle = $LasConfig{title};
print "\nPlease provide a title for your LAS.\n";
print "This title will appear in the upper left hand corner of the LAS interface.\n";
while (! $title){
    print "Enter a title for the LAS server: [$autotitle] ";
    $title = <STDIN>;
    chomp($title);
    $title = $autotitle if ! $title;
}

$LasConfig{title} = $title;
print "\n\n";
#   
# Get an email address(es) for the LAS administrator(s)
#
my $email;
my $autoemail = $LasConfig{email};
print "\nProvide email address(es) for the administrator(s).\n";
    
    print "Enter a blank separated list email address(es): [$autoemail] ";
    $email = <STDIN>;
    chomp($email);
    $email = $autoemail if ! $email;

$LasConfig{email} = $email;
print "\n\n";

$LasConfig{proxy} = $proxy;

my $autoproxy = $LasConfig{proxy};
if (! $autoproxy){
   $autoproxy = "yes";
}

while (! $proxy){
    print "Do you plan to use a proxy pass or connector from the HTTP server to the tomcat server (recommended; instructions below): [$autoproxy] ";
    $proxy = <STDIN>;
    chomp $proxy;
    $proxy = $autoproxy if ! $proxy;
    if ($proxy ne "yes" && $proxy ne "no") {
        print "You must answer 'yes' or 'no'.\n";
        $proxy = undef;
    }
}
$LasConfig{proxy} = $proxy;

print "\n\n";

my $servlet_root_url="";
if ($LasConfig{proxy} eq "yes") {
    #
    # Get the hostname of the LAS server
    #
    my $hostname;
    my $hn = `hostname`;
    chomp $hn;
    my $autohostname = $LasConfig{hostname};
    if (! $autohostname){
        ($autohostname) = gethostbyname $hn;
        chomp $autohostname;
    }
    while (! $hostname){
        print "Enter the full domain name of the HTTP server that will be used as the proxy: [$autohostname] ";
        $hostname = <STDIN>;
        chomp $hostname;
        $hostname = $autohostname if ! $hostname;
        if ($hostname !~ /\./){
            print "You must enter a full domain name\n";
            $hostname = undef;
        }
    }
    $LasConfig{hostname} = $hostname;
    $servlet_root_url = $LasConfig{hostname};
} else {
    $servlet_root_url = $LasConfig{tomcat_hostname} . ":" . $servlet_port;
}
# Get info about the TDS installation.

    my $serverConf = $LasConfig{jakarta_home}."/content".$LasConfig{uipath}."/conf/server";
    if ( !(-d $serverConf) ) {
       &File::Path::mkpath($serverConf);
       print "Creating the $serverConf directory.\n";
    }

#
# Get the temp dir.
#
my $tds_temp = $serverConf."/temp";
if ( !(-d $tds_temp) ) {
   &File::Path::mkpath($tds_temp);
   print "Creating the $tds_temp directory.\n";
}

print "\n\n";

$LasConfig{tds_temp} = $tds_temp;

#
# Get the data dir.
#
my $tds_data = $serverConf."/data";
if ( !(-d $tds_data) ) {
   &File::Path::mkpath($tds_data);
   print "Creating the $tds_data directory.\n";
}
   $LasConfig{tds_data} = $tds_data;

# Make the dynamic data dir.
my $tds_dynadata = $tds_data."/dynamic";

if ( !(-d $tds_dynadata) ) {
   &File::Path::mkpath($tds_dynadata);
   print "Creating the $tds_dynadata directory for the dynamic data for user defined variables and comparison regridding.\n\n";
}
   $LasConfig{tds_dynadata} = $tds_dynadata;

# End of TDS info.


#
# Scripts to edit
#

my @Scripts = qw(build.xml
                 bin/initialize_check.sh
                 bin/addXML.sh
                 conf/example/sample_las.xml
                 conf/example/sample_ui.xml
                 conf/example/sample_insitu_las.xml
                 conf/example/sample_insitu_ui.xml
                 conf/example/productserver.xml
                 JavaSource/resources/ferret/FerretBackendConfig.xml.base
                 JavaSource/resources/kml/KMLBackendConfig.xml
                 JavaSource/resources/database/DatabaseBackendConfig.xml
                 WebContent/WEB-INF/struts-config.xml
                 WebContent/WEB-INF/web.xml
                 WebContent/TestLinks.html
                 test/LASTest/las_test_config.xml
                 );
my $mode = 0644;
foreach my $script (@Scripts){
    my $template = "$script.in";
    open INSCRIPT, $template or die "Can't open template file $template";
    if (-f $script){
        chmod $mode, '$script';
    }
    open OUTSCRIPT, ">$script" or die "Can't create output file $script";
    my $cust_name = $LasConfig{custom_name};
    my $cname;
    $cname = qq(src="$cust_name/custom.js") if $cust_name;
    my $cplinclude = "";
    $cplinclude = "$cust_name" if $cust_name;
    my $java_home = dirname(dirname($java));
    my $cdir = &Cwd::cwd();
    while (<INSCRIPT>){
        s/\@JAKARTA_HOME\@/$jakarta_home/g;
        s/\@JAKARTA_LIB\@/$jakarta_lib/g;
        s/\@JAVA_HOME\@/$java_home/g;
        s/\@FERRET\@/$LasConfig{ferret}/g;
        s/\@OUTPUT_ALIAS\@/$LasConfig{output_alias}/g;
        s/\@UIPATH\@/$LasConfig{uipath}/g;
        s/\@APPNAME\@/$LasConfig{appname}/g;
        s/\@SERVERHOST\@/$LasConfig{hostname}/g;
        s/\@TOMCATHOST\@/$LasConfig{tomcat_hostname}/g;
        s/\@PROXY\@/$LasConfig{proxy}/g;
        s/\@JSINCLUDE\@/$cname/g;
        s/\@CUSTOM_PERL_INCLUDE\@/$cplinclude/g;
        s/\@DB_HOST\@/$host/g;
        s/\@SERVLET_PORT\@/$servlet_port/g;
        s/\@SERVLET_SHUTDOWN_PORT\@/$servlet_shutdown_port/g;
        s/\@SERVLET_CONNECTOR_PORT\@/$servlet_connector_port/g;
        s/\@SERVLET_ROOT_URL\@/$servlet_root_url/g;
        s/\@MODULES_LIST\@/$modules_list/g;
        s/\@TITLE\@/$LasConfig{title}/g;
        s/\@ADMIN_EMAIL\@/$LasConfig{email}/g;
        s/\@WEBAPPS\@/$LasConfig{webapps}/g;
        s/\@TDS_DATA\@/$LasConfig{tds_data}/g;
        s/\@TDS_DYNADATA\@/$LasConfig{tds_dynadata}/g;
        s/\@TDS_TEMP\@/$LasConfig{tds_temp}/g;
        s/\@PWD\@/$cdir/g;
        print OUTSCRIPT $_;
    }
    close INSCRIPT;
    close OUTSCRIPT;
    chmod $mode, '$script';
}



#
# Set up Ferret paths
#

$ferretConfig = "JavaSource/resources/ferret/FerretBackendConfig.xml";

print <<EOF;

Now setting up the Ferret environment variables for the server...
If you want to change them, edit 'JavaSource/resources/ferret/FerretBackendConfig.xml'


EOF

if (-f "JavaSource/resources/ferret/FerretBackendConfig.xml"){
    print "You already have a config file for your Ferret backend environment.\n";
    

    if ( getYesOrNo("Do you want to use this file") ) {
    } else {
print <<EOF;

Creating a  new 'JavaSource/resources/ferret/FerretBackendConfig.xml'
based on your current environment variable settings.  

The current file has been saved in: JavaSource/resources/ferret/FerretBackendConfig.xml.old

EOF
       system("mv $ferretConfig $ferretConfig.old");
       copy ("$ferretConfig.base","$ferretConfig") or
       die "Could not get FerretBackendConfig.xml initialization file";
       printENV($ferretConfig, @EnvVars);
    }
} else {
       copy("$ferretConfig.base","$ferretConfig") or
       die "Could not get FerretBackendConfig.xml initialization file";
       printENV($ferretConfig, @EnvVars);
}


if ( getYesOrNo("Do you want to install the example data set configuration") ) {

    my @sample_in = ();
    my @sample_out = ();

    my @insitu_in = ();
    my @insitu_out = ();

    $sample_in[0] = "conf/example/sample_las.xml";
    $sample_out[0] = $serverConf."/las.xml";
    $sample_in[1] = "conf/example/productserver.xml";
    $sample_out[1] = $serverConf."/productserver.xml";
    $sample_in[2] = "conf/example/operationsV7.xml";
    $sample_out[2] = $serverConf."/operationsV7.xml";
    $sample_in[3] = "conf/example/sample_ui.xml";
    $sample_out[3]= $serverConf."/ui.xml";
    $sample_in[4] = "xml/perl/coads.xml";
    $sample_out[4] = $serverConf."/coads.xml";
    $sample_in[5] = "conf/example/DODS_IRI_NOAA_NCEP_EMC_CMB_Pac_ocean.xml";
    $sample_out[5] = $serverConf."/DODS_IRI_NOAA_NCEP_EMC_CMB_Pac_ocean.xml";
    $sample_in[6] = "xml/perl/levitus.xml";
    $sample_out[6] = $serverConf."/levitus.xml";
    $sample_in[7] = "conf/example/ocean_atlas_subset.xml";
    $sample_out[7] = $serverConf."/ocean_atlas_subset.xml";
    $sample_in[8] = "conf/example/options.xml";
    $sample_out[8] = $serverConf."/options.xml";

    $insitu_in[0] = "conf/example/insitu_demo_1.xml";
    $insitu_out[0] = $serverConf."/insitu_demo_1.xml";
    $insitu_in[1] = "conf/example/insitu_demo_2.xml";
    $insitu_out[1] = $serverConf."/insitu_demo_2.xml";
    $insitu_in[2] = "conf/example/insitu_demo_ui.xml";
    $insitu_out[2] = $serverConf."/insitu_demo_ui.xml";
    $insitu_in[3] = "conf/example/insitu_ui.xml";
    $insitu_out[3] = $serverConf."/insitu_ui.xml";
    $insitu_in[4] = "conf/example/insitu_options.xml";
    $insitu_out[4] = $serverConf."/insitu_options.xml";

    # Overwrite gridded only las.xml and ui.xml for insitu demo.
    $insitu_in[5] = "conf/example/sample_insitu_las.xml";
    $insitu_out[5] = $serverConf."/las.xml";
    $insitu_in[6] = "conf/example/sample_insitu_ui.xml";
    $insitu_out[6] = $serverConf."/ui.xml";
    $insitu_in[7] = "conf/example/nwioos_hake98.xml";
    $insitu_out[7] = $serverConf."/nwioos_hake98.xml";
    $insitu_in[8] = "conf/example/pfeg.xml";
    $insitu_out[8] = $serverConf."/pfeg.xml";


    my $insitu = 0;
       if (getYesOrNo("Are the sample in-situ datasets loaded into your mySQL database")) {
          $insitu=1;
       }

    for ( my $i = 0; $i <= $#sample_in; $i++ ) {
       if ( -f $sample_out[$i] ) {
           print "You already have this XML configuration file for your\n";
           print "product server in $sample_out[$i].\n";
           if (! getYesOrNo("Overwrite this file", 1)){
              print "I will not generate a sample configuration\n";
          }
       }
       if (-f $sample_out[$i] && !unlink $sample_out[$i]){
           print "Couldn't delete $sample_out[$i]\n"; return;
       }
       if (!copy($sample_in[$i], $sample_out[$i])){
           print "Couldn't copy $sample_in[$i] to $sample_out[$i]\n";
       }
    }
    if (!copy("WebContent/luis/web/levitus_monthly.html","WebContent/docs/levitus_monthly.html")){
       print "Couldn't copy levitus_monthly.html.\n";
    }
    if ($insitu) {
       for ( my $i = 0; $i <= $#insitu_in; $i++ ) {
          if ( -f $insitu_out[$i] && 
                 !$insitu_out[$i] =~ "/las.xml" &&
                 !$insitu_out[$i] =~ "/ui.xml" ) {
              print "You already have this XML configuration file for your\n";
              print "product server in $insitu_out[$i].\n";
              if (! getYesOrNo("Overwrite this file", 1)){
                 print "I will not generate a sample configuration\n";
             }
          }
          if (-f $insitu_out[$i] && !unlink $insitu_out[$i]){
              print "Couldn't delete $insitu_out[$i]\n"; return;
          }
          if (!copy($insitu_in[$i], $insitu_out[$i])){
              print "Couldn't copy $insitu_in[$i] to $insitu_out[$i]\n";
          }
       }
    }
 }

    print "Building servlet war file.\n";
    system("ant deploy");

    print "\n\n";

    createScripts();

    print "You can test your F-TDS setup by running the LASTest suite.\n";
    print "To run the tests run these commands.\n";
    print "cd test/LASTest\n";
    print "ant lastest -Df=1\n";

    print "\n\n";

    print "You must restart your Tomcat server.\n";
    print "We've created some scripts to help you do that.  See: stopserver.sh, startserver.sh and rebootserver.sh\n";

    print "\n\n";
    my $app = $LasConfig{appname};
    print "Your user interface to LAS is at: http://$servlet_root_url/$app/\n";


#
# Save configuration
#

if (!&saveConfig){
    print <<EOF;

Couldn't save configuration results. The next time you run the
configuration script, you will have to reenter all of the
configuration data.

EOF
}




sub saveConfig {
    my $status = open CONFIG, ">$configp";
    if (! $status){
        print "\nCan't write $configp file\nIf you rerun the configuration, you will have to reenter all of the configuration parameters.\n";
        return 0;
    }
    foreach my $key (keys %LasConfig){
        print CONFIG '$LasConfig{',$key,'} = \'',$LasConfig{$key},"';\n";
    }
    print CONFIG "1;\n";
    close CONFIG;
}

sub createScripts {

my $jakarta_home = $LasConfig{jakarta_home};
my $java_home = dirname(dirname($java));
my $workdir = $LasConfig{uipath};
my $removeWork = $jakarta_home."/work/Catalina/localhost".$workdir;

open SCRIPT_OUT, ">startserver.sh" or die "Can't open startserver.sh";
print SCRIPT_OUT <<EOL;
#!/bin/sh
JAVA_HOME="$java_home"
JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"
CATALINA_PID="$LasConfig{webapps}/UI_PID"
CATALINA_HOME="$LasConfig{jakarta_home}"
export JAVA_HOME JAVA_OPTS CLASSPATH CATALINA_PID CATALINA_HOME
rm -rf $removeWork
exec $jakarta_home/bin/catalina.sh start
EOL
my $mode = 0755;
close SCRIPT_OUT;
chmod $mode,"startserver.sh";

open SCRIPT_OUT, ">stopserver.sh" or die "Can't open stopserver.sh";
print SCRIPT_OUT <<EOL2;
#!/bin/sh
JAVA_HOME="$java_home"
JAVA_OPTS="-Djava.awt.headless=true -Xmx256M -Xms256M"
CATALINA_PID="$LasConfig{webapps}/UI_PID"
CATALINA_HOME="$LasConfig{jakarta_home}"
export JAVA_HOME JAVA_OPTS CLASSPATH CATALINA_PID CATALINA_HOME
exec $jakarta_home/bin/catalina.sh stop
EOL2

close SCRIPT_OUT;
chmod $mode,"stopserver.sh";

my $stopsrv = &Cwd::cwd()."/stopserver.sh";
my $startsrv = &Cwd::cwd()."/startserver.sh";
open SCRIPT_OUT, ">rebootserver.sh" or die "Can't open rebootserver.sh";
print SCRIPT_OUT <<EOL3;
#! /bin/sh
#

# Attempt to shutdown the server gracefully.
$stopsrv

# Wait to let things shutdown.
sleep 5

# Check to see if it is still running.
ps `cat $LasConfig{webapps}/UI_PID`
STATUS=$?
 if [ \$STATUS -eq 0 ] ; then
     # LAS UI did not shutdown ok.  Kill it.
     kill -9 `cat $LasConfig{webapps}/UI_PID`
 else
     # LAS UI shutdown ok.
     continue
 fi

# Start the server again.  Answer the question 'yes' if necessary.
$startsrv
EOL3

close SCRIPT_OUT;
chmod $mode,"rebootserver.sh";
}

sub getExecutable {
    my ($file) = @_;
    foreach my $path (split ':',$ENV{PATH}){
        my $checkfile = "$path/$file";
        if (-x "$checkfile"){
            return $checkfile;
        }
    }
    "";
}
sub getAnswer($$) {
    my ($mess, $default) = @_;
    print "$mess: [$default] ";
    my $answer = <STDIN>;
    chomp($answer);
    $answer = $default if ! $answer;
    return $answer;
}
    sub getYesOrNo {
        my $prompt = shift;
        my $useNo = shift;
        my $default = $useNo ? "no" : "yes";
        print "$prompt? [$default] ";
        my $ans = <STDIN>; 
        chomp($ans);
        $ans = $default if ! $ans;
        if ($ans !~ /^[yY]/){
            return 0;
        }
        return 1; 
    }    

sub trim($)
{
        my $string = shift;
        $string =~ s/^\s+//;
        $string =~ s/\s+$//;
        return $string;
}

sub printENV($ferretConfig, @EnvVars) {
       open CONFIGFILE, ">>$ferretConfig"
           or die "Couldn't open config file $ferretConfig";

        print CONFIGFILE '    <environment>',"\n";
        foreach my $var (@EnvVars){
            $ENV{$var} = ". " . $ENV{$var} if $var !~ /PLOTFONT/;
            if ($var =~ /FER_GO|FER_PALETTE/){
                $ENV{$var} = "scripts jnls jnls/insitu jnls/section " . $ENV{$var};
                $ENV{$var} = $LasConfig{custom_name} . " " . $ENV{$var}
                    if $LasConfig{custom_name};
            } elsif ($var =~ /FER_DATA/){
                $ENV{$var} = "./data " . $ENV{$var};
            } elsif ($var =~ /FER_DESCR/){
                $ENV{$var} = "des " . $ENV{$var};
            } elsif ($var =~ /DODS_CONF/){
                $ENV{$var} = $serverConf."/dods/.dodsrc";
            }

            my @values = split(' ',$ENV{$var});

            # Trim so ". " and "." match
            foreach my $value (@values) {
               $value = trim($value);
            }

            #Extract unique entries, see perl FAQ
            undef %saw;
            @saw{@values} = ();
            @out = sort keys %saw;  # remove sort if undesired
    
            print CONFIGFILE '        <variable>',"\n";
            print CONFIGFILE '            <name>',$var,'</name>',"\n";
            foreach my $value (@out) {
               $value = trim($value);
               print CONFIGFILE '             <value>',$value,'</value>',"\n";
            }
            print CONFIGFILE '        </variable>',"\n";
        }
        print CONFIGFILE '    </environment>',"\n";
        print CONFIGFILE '</application>',"\n";
        close CONFIGFILE;
     }
