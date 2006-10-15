#!/usr/bin/perl

open(FILE, "$ARGV[0]") or die $!;

$fname = $ARGV[0];
$fname =~ s/\..*$//;

mkdir $fname or die $!;

print "$fname\n";

$fnum = 1000;

foreach $line(<FILE>){
	if($line =~ /^\.W/){
		$generate = 1;
		next;
	}

	if($line =~ /^\.I \d+/){
		$ofile = sprintf("%s\\%s.%d.txt", $fname, $fname, $fnum++);
		open(OUTPUT, ">$ofile") or die $!;
		print OUTPUT $buf;
		close(OUTPUT);
		$generate = 0;
		$buf = "";
		next;
	}

	if($generate == 1){
		$buf .= $line;
	}
}

close(FILE);
