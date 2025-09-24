# LAS

# N.B. There is a serious vulnerabilty in this software (CVE-2025-59782).
# If you are still running LAS 8, you should apply the following fix.

## There are no release packages available. You should not be intalling new instances of the application.

If you have other options for providing similar services to you users, I suggest you move to those tools and stop using LAS.

### To fix the issue that was in the last publically available release.

  1. Get the latest version of [gov.noaa.pmel.tmap.las.filter.RequestFilter.java](https://github.com/NOAA-PMEL/LAS/blob/main/JavaSource/gov/noaa/pmel/tmap/las/filter/RequestInputFilter.java) from here. It has the necessary fix.
  2. Or if you have your own filter criteria, you can apply [this diff](https://github.com/NOAA-PMEL/LAS/commit/e69afb1898ae7e69f3e047513fc1e5570373912b) to your existing source.
  3. Compile it as appropriate for your local environment.From LAS directory from your original install:
      1. Put the new source in place, JavaSource/gov/noaa/pmel/tmpa/las/filter/RequestFilter.java:
      2. ant compile
      3. ant deploy (or just copy .class to .
      4. restart your tomcat

#### Legal Disclaimer
*This repository is a software product and is not official communication 
of the National Oceanic and Atmospheric Administration (NOAA), or the 
United States Department of Commerce (DOC).  All NOAA GitHub project 
code is provided on an 'as is' basis and the user assumes responsibility 
for its use.  Any claims against the DOC or DOC bureaus stemming from 
the use of this GitHub project will be governed by all applicable Federal 
law.  Any reference to specific commercial products, processes, or services 
by service mark, trademark, manufacturer, or otherwise, does not constitute 
or imply their endorsement, recommendation, or favoring by the DOC. 
The DOC seal and logo, or the seal and logo of a DOC bureau, shall not 
be used in any manner to imply endorsement of any commercial product 
or activity by the DOC or the United States Government.*


