<?php
// you can 'protect' this page with a id url parameter
// the call to this script must then be: https://yourserver/path-to-script/scanexapp.php?id=your-identifier
// it is however not a secure way
// Also create an upload directory and adjust permissons.
if( isset($_GET['id']) && $_GET["id"]=="IDENTIFIER"){
	$file_path = "uploads/";
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
        echo "success";
    } else{
        echo "fail";
    }
}else{
	echo "FAILURE";
}
 ?>
