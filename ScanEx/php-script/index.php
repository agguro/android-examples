<?php
$path = "uploads/";
$files = array_diff(scandir($path), array('.', '..'));
foreach($files as $file) {
  echo '<a href="https://agguro.be/ScanEx/'.$path.$file.'"><img src="https://agguro.be/ScanEx/uploads/'.$file.'" class="shrinkToFit" width="150" height="150">'.$file.'</a><br />';
}
?>
