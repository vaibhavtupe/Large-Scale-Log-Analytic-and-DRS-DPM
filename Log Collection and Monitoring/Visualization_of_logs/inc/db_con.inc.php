<?php
	$hostname = "project2-283.cvz5dtczqgms.us-west-1.rds.amazonaws.com";
	$dbuser = "root";
	$dbpassword = "rootroot";
	$dbname = "project283";

	
	$conn = new mysqli($hostname,$dbuser,$dbpassword, $dbname); 
	// Check connection
	if (mysqli_connect_error()){
		echo "Failed to connect to MySQL: " . mysqli_connect_error();
	}
?>