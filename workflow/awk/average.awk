BEGIN {
	count = 0;
	sum = 0;
}

{
	count += 1;
	sum += log($4);
}

END {
	if (count != 0) {
		print count/100.0 * 100, sum/count
	} else {
		print 0, 0
	}
}
