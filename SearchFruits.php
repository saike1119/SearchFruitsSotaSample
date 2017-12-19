<?php

include_once('db_def.php');

$speech_txt = "";
$fruitsName = "";

$mysqli = new mysqli($dbhost, $dbuser, $dbpasswd, $dbname);

if ($mysqli->connect_error) {
    echo $mysqli->connect_error;
    exit();
}
if (isset($_REQUEST['fruitsName'])) {
    $fruitsName = $_REQUEST['fruitsName'];
    $fruitsName = mysqli_escape_string($mysqli, $fruitsName);
    $speech_txt = $fruitsName;
}

$query = "select * from fruits WHERE fruit = '{$fruitsName}';";
$result = $mysqli->query($query);
if ($result->num_rows >= 1) {
    while ($row = $result->fetch_assoc()) {
        $speech_txt .= 'は' . $row["description"];
    }
    $result->free();
} else {
    $speech_txt .= 'は見つかりませんでした。';
}

$mysqli->close();

echo $speech_txt;
