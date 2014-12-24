<?php
		

include_once('inc/db_con.inc.php');
$status = "";

$entityName = trim($_GET['entityName']);
//echo strlen($entityName);
if(isset($_GET['time_range'])){
	$time_range = $_GET['time_range'];
	$status = $time_range;
}else
	$time_range = "all";
	
	

if($time_range=="all")
    $sql = "SELECT * FROM logs_aggregate1 where VM_Name=".$entityName." ";
else if($time_range=="hour")
	$sql = "SELECT * FROM logs_aggregate1 where VM_Name=".$entityName." AND Timestamp_log > date_sub(now(), interval 10 hour)";
else if($time_range=="day")
	$sql = "SELECT * FROM logs_aggregate1 where VM_Name=".$entityName." AND (Timestamp_log) >= now()";
else if($time_range=="week")
	$sql = "SELECT * FROM logs_aggregate1 where VM_Name=".$entityName." AND Timestamp_log > date_sub(now(), interval 1 week)";
	//$sql = "SELECT * FROM logs_aggregate1 where VM_Name=".$entityName." ORDER BY Timestamp_log DESC limit 10";
	$cpuUsage[0] = array("Date-time", "%");
	$memoryUsage[0] = array("Date-time", "%");
	$diskUsage[0] = array("Date-time", "%");
	$networkUsage[0] = array("Date-time", "%");

$result = mysqli_query($conn, $sql);
$data[0] = array('name','counts');		
$ind = 1; 
//get the total number of records
$total_records = mysqli_num_rows($result);
$counter = 0;
if (mysqli_num_rows($result) > 0) {
    // output data of each row
    while($row = mysqli_fetch_assoc($result)) {
		//print_r($row);
		if($ind % ceil($total_records/10) == 0){
			++$counter;
/*			$cpuUsage[$counter] = array(date('m-d|H:i',$row['Timestamp_log']),(int)$row['Cpu_Usage']);
			$memoryUsage[$counter] = array(date('m-d|H:i',$row['Timestamp_log']),(int)$row['Memory_Usage']);
			$diskUsage[$counter] = array(date('m-d|H:i',$row['Timestamp_log']),(int)$row['Disk_Usage']);
			$networkUsage[$counter] = array(date('m-d|H:i',$row['Timestamp_log']),(int)$row['Net_Usage']);
	*/		$cpuUsage[$counter] = array((string)$row['Timestamp_log'],(int)$row['Cpu_Usage']);
			$memoryUsage[$counter] = array((string)$row['Timestamp_log'],(int)$row['Memory_Usage']);
			$diskUsage[$counter] = array((string)$row['Timestamp_log'],(int)$row['Disk_Usage']);
			$networkUsage[$counter] = array((string)$row['Timestamp_log'],(int)$row['Net_Usage']);
		}
		$ind++;
    }
} else {
    echo "No Records Found!!";
}
$jsonTable = json_encode($data);
$cpuUsageTable = json_encode($cpuUsage);
print_r($cpuUsageTable);
$memoryUsageTable = json_encode($memoryUsage);
$diskUsageTable = json_encode($diskUsage);
$networkUsageTable = json_encode($networkUsage);
mysqli_close($conn);
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>

  
    <script language="javascript" type="text/javascript"  src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.0/jquery.min.js">
    </script>
    <!-- Load Google JSAPI -->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
	
	
		function showCharts(value){
			window.location.href = "performance.php?time_range="+value+"&entityName=<?=urlencode($entityName)?>";
			document.getElementById('order_status').value = value;
			document.cookie = value;
		}
		
        google.load("visualization", "1", { packages: ["corechart"] });
        google.setOnLoadCallback(drawChart);

        function drawChart() {
			var dataCpuUsageTable = google.visualization.arrayToDataTable(<?=$cpuUsageTable?>, false); 
			var dataMemoryUsageTable = google.visualization.arrayToDataTable(<?=$memoryUsageTable?>, false); 
			var dataDiskUsageTable = google.visualization.arrayToDataTable(<?=$diskUsageTable?>, false); 
			var dataNetworkUsageTable = google.visualization.arrayToDataTable(<?=$networkUsageTable?>, false); 
			// 'false' means that the first row contains labels, not data.
            var options = {
				dateFormat: 'dd.MM.yy',
                title: 'CPU Usage Vs Time',
                pointSize: 5,
                
                 animation:{
        				duration: 1000,
       					 easing: 'out',
     					 },
                
                vAxis: {
                	title: 'CPU Usage (MHz)',
           	  		gridlines: {color: "#6AB5D5", count: 10},
    				baselineColor: '#6AB5D5',
    			},
    					
                hAxis: {
                title: 'Time stamp',
                slantedText: true,
                fontSize: 8,
				
                format: 'M d, y  \n H:i',
    			baselineColor: '#6AB5D5'
			}
                
            };

            var chart1 = new google.visualization.LineChart(document.getElementById('chart_div1'));
            chart1.draw(dataCpuUsageTable, options);
            
            var options = {
				dateFormat: 'dd.MM.yy',
                title: 'Memory Usage Vs Time',
                pointSize: 5,
                //curveType: 'function',
                //backgroundColor: '#CCCCCC',
                
                //chxt: y,x,x,
                //chxl: 1:| $date1 |2:| $time1
                 animation:{
        				duration: 1000,
       					 easing: 'out',
     					 },
                
                vAxis: {
                	title: 'Memory Usage (MB)',
           	  		gridlines: {color: "#6AB5D1", count: 10},
    				baselineColor: '#6AB5D1',
    			},
    					
                hAxis: {
                title: 'Time stamp ',
                slantedText: true,
                fontSize: 8,
                format: 'M d, y  \n H:i',
    			baselineColor: '#6AB5D1'
			}
                
            };

            var chart2 = new google.visualization.LineChart(document.getElementById('chart_div2'));
            chart2.draw(dataMemoryUsageTable, options);

            var options = {
				dateFormat: 'dd.MM.yy',
                title: 'Disk Usage Vs Time',
                pointSize: 5,
                //curveType: 'function',
                //backgroundColor: '#CCCCCC',
                
                //chxt: y,x,x,
                //chxl: 1:| $date1 |2:| $time1
                animation:{
        				duration: 1000,
       					 easing: 'out',
     					 },
                
                vAxis: {
                	title: 'Disk Usage (%)',
           	  		gridlines: {color: "#6AB5D1", count: 10},baselineColor: '#6AB5D1',
    			},
    					
                hAxis: {
                title: 'Time stamp ',
                slantedText: true,
                fontSize: 8,
                format: 'M d, y  \n H:i',
    			baselineColor: '#6AB5D1'
			}
                
            };

            var chart3 = new google.visualization.LineChart(document.getElementById('chart_div3'));
            chart3.draw(dataDiskUsageTable, options);

            var options = {
				dateFormat: 'dd.MM.yy',
                title: 'Network Usage Vs Time',
                pointSize: 5,
                //curveType: 'function',
                //backgroundColor: '#CCCCCC',
                
                //chxt: y,x,x,
                //chxl: 1:| $date1 |2:| $time1
                 animation:{
        				duration: 1000,
       					 easing: 'out',
     					 },
                
                vAxis: {
                	title: 'Network Usage (%)',
           	  		gridlines: {color: "#6AB5D1", count: 10},
    				baselineColor: '#6AB5D1',
    			},
    					
                hAxis: {
					title: 'Time stamp ',
					slantedText: true,
					fontSize: 8,
					gridlines: {color: "#6AB5D1", count: 5},
					format: 'M d, y  \n H:i',
					baselineColor: '#6AB5D1'
				}
                
            };

            var chart4 = new google.visualization.LineChart(document.getElementById('chart_div4'));
            chart4.draw(dataNetworkUsageTable, options);
        }

    </script>
    <body style="background-color:black">
	
	
	<div align ="left" height ="40%">
	<table width="30%"  border="0" cellspacing="0" cellpadding="0" height="30%">
            <tr>
            <td width="97%"><?php include "inc/admin_menu.inc.php"; ?></td>
            <td width="3%">&nbsp;</td>
          </tr>
          </table>
	</div>
    <table width="100%"  border="0" cellpadding="0" cellspacing="0" bgcolor="#99FFCC">
    
      <tr>
        <td width="18%" align="left" valign="top"></td>
        <td align="left" valign="top"><table width="100%"  border="0" cellpadding="0" cellspacing="0" bgcolor="#99FFCC">
            <!--tr>
				<td class="row4"> Manage Makes</td>
			</tr>
			<tr>
				<td class="row4"><a href="add_denomination.php">Add Denomination</a></td>
			</tr-->
            <tr>
            <td><h2 style="
    font-family: Arial, Helvetica, sans-serif;
    line-height: 48px;  font-size: 2em;  

font-weight: bold;  color: #146295;  

margin: 0;  padding: 0;
"> Statistics For <?php echo $entityName;?></h2></td>
          </tr>
            <tr>
            <td><table width="100%"  border="0" cellspacing="0" cellpadding="0" style="background-color:#eaeaea">
                <tr>
                <td>&nbsp;</td>
                <td><?php if(isset($message)) echo $message;?></td>
              </tr>
                <tr>
                <td>&nbsp;</td>
                <td><form id="myForm" method="POST">
                    Time Range :
                    <select name="order_status" id="order_status" onChange="showCharts(this.value)">
                      <option>-Select-</option>
                      <option value="all" <?php if($status=="all") echo " selected" ?>>1 Minute</option>
                      <option value="hour" <?php if($status=="pending") echo " selected" ?>>HOUR</option>
                      <option value="day" <?php if($status=="success") echo " selected" ?>>DAY</option>
                      <option value="week" <?php if($status=="failed") echo " selected" ?>>WEEK</option>
                    </select>
                  </form>
                    &nbsp;&nbsp; </td>
              </tr>
              </table></td>
          </tr>
            <tr>
            <td><div id="chart_div1" class="mapborder" style="width: 98%; height: 500px;"> </div>
                <div id="chart_div2"  class="mapborder"  style="width: 98%; height: 500px;"> </div>
                <div id="chart_div3"  class="mapborder"  style="width: 98%; height: 500px;"> </div>
                <div id="chart_div4"  class="mapborder"  style="width:98%; height: 500px;"> </div></td>
          </tr>
          </table></td>
      </tr>
    </table>
</body>
    <script>

</script>
</html>