<?php
if( isset($_GET['id']) && $_GET["id"]=="66102644933"){
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
