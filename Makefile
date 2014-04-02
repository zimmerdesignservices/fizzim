# kill make's automatic rcs co rule
% : RCS/%,v
% : v

SHELL = bash

SETVERSION ?= false

all: 
	   ls *.java > /dev/null \
	&& if [ "${SETVERSION}" == "true" ]; then \
	     ver=`date +%g.%m.%d`; \
	     echo "Changing FizzimGui.java to set the version to $${ver}" ; \
	     perl -p -i -e 's/(String\s+currVer\s+=\s+").*(".*$$)/$${1}'$${ver}'$${2}/' FizzimGui.java; \
	   fi \
	&& version=`perl -n -e 'if (m/currVer\s*=\s*"([\d\.]+)"/) {print "$$1";}' FizzimGui.java` \
	&& echo "Creating $${version}_jar directory" \
	&& if [ -d $${version}_jar ]; then \
	     rm -fr $${version}_jar; \
	   fi \
	&& mkdir $${version}_jar \
	&& cd $${version}_jar \
	&& echo "Copying java files to $${version}_jar directory" \
	&& cp -pr ../*.java ../org ../*.png ../Makefile ./ \
	&& echo "Running javac " \
	&& javac -target 1.5 *.java \
	&& echo "Main-Class: FizzimGui" > manifest.txt \
	&& echo "Creating jar file " \
	&& mkdir src \
	&& mv *.java src/ \
	&& jar cvfm fizzim_v$${version}.jar manifest.txt *.class splash.png icon.png org/ src/ Makefile > jar.log \
	&& echo "Copying jar file back to main directory" \
	&& cp fizzim*.jar ../ \
	&& echo done


help: 
	@echo "Makefile for fizzim gui:"
	@echo ""
	@echo "  make or make all : build jar file"
	@echo "  clean            : remove generated files"
	@echo "  help             : this output"
	@echo ""
	@echo "  options:"
	@echo "    SETVERSION=true|false  : Override the version (datestamp) in FizzimGui.java"
	@echo "                             (defaults false)"
	@echo ""
	@echo ""

clean:
	rm -fr *jar *errors.log
